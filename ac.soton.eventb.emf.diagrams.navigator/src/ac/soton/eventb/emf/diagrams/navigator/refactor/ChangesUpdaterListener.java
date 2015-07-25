package ac.soton.eventb.emf.diagrams.navigator.refactor;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ui.IStartup;
import org.eventb.core.IEventBRoot;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.persistence.EMFRodinDB;
import org.rodinp.core.ElementChangedEvent;
import org.rodinp.core.IElementChangedListener;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinElementDelta;
import org.rodinp.core.IRodinFile;

public class ChangesUpdaterListener implements IElementChangedListener, IStartup {

	
	/**
	 * Create an EMFRodinDB for loading extensions into EMF
	 */
	private final static EMFRodinDB emfRodinDB = new EMFRodinDB();
	
	private static final String QUALIFIER = "ac.soton.eventb.emf.diagrams.navigator";
//	private static final QualifiedName RODIN_PROJECT = new QualifiedName(QUALIFIER, "PROJECT");
//	private static final QualifiedName OLD_PROJECT_NAME = new QualifiedName(QUALIFIER, "OLD_PROJECT_NAME");
	private static final QualifiedName RODIN_COMPONENT = new QualifiedName(QUALIFIER, "COMPONENT");
	private static final QualifiedName OLD_COMPONENT_NAME = new QualifiedName(QUALIFIER, "OLD_COMPONENT_NAME");
	
	@Override
	public void earlyStartup() {
		// nothing to do - the listener is added by the plugin start so that it can remove it if the plug-in is stopped
	}
	
		   
	/**
	 * Listens for changes to the Rodin database so that changes files can be kept in step with changes to machines, contexts, etc.
	 * Any required updates are scheduled as Jobs so that they can run in the background as a separate thread.
	 * (This is necessary as the resource tree is locked at the time of notification).
	 * 
	 */
	@Override
	public void elementChanged(ElementChangedEvent event) {
    	for (IRodinElementDelta affectedProject : event.getDelta().getAffectedChildren()){
    		for (IRodinElementDelta affectedComponent : affectedProject.getAffectedChildren()){
    			IRodinElement component = affectedComponent.getElement();   
				if (component.getElementName().endsWith("_tmp")){ //many renames from temp files which we must ignore
					continue;
				}
    			if (component instanceof IRodinFile){
    				IRodinFile rodinFile = (IRodinFile)component;
    				if (rodinFile.getRoot() instanceof IEventBRoot){
    					if ((affectedComponent.getFlags() & IRodinElementDelta.F_MOVED_FROM) != 0){	
    						//FIXME: The following 'if' is a workaround as the Rodin builder copies files and is stopped if we schedule a job
    					}else if (affectedComponent.getKind() == IRodinElementDelta.REMOVED){
    						Job diagramUpdaterJob = new Job("Updating diagram references for new component name") {
    						      public IStatus run(IProgressMonitor monitor) {
    						     	//String oldComponentName = (String)getProperty(OLD_COMPONENT_NAME);
    								EventBElement eventBElement = emfRodinDB.loadEventBComponent((IEventBRoot)getProperty(RODIN_COMPONENT));
    						    	if (eventBElement  instanceof EventBNamedCommentedComponentElement){
    						    		URI uri = EcoreUtil.getURI(eventBElement);
    						    		//String fileExtension = uri.fileExtension();
    						    		//uri = uri.trimSegments(1).appendSegment(oldComponentName).appendFileExtension(fileExtension);
    						    		RefactorAssistant refactor = new RefactorAssistant(uri, emfRodinDB.getEditingDomain());	
    						    		refactor.deleteChangeRecords();
    						    	}
    								if (eventBElement!=null && eventBElement.eResource()!= null && eventBElement.eResource().isLoaded()){
    									eventBElement.eResource().unload();
    								}
    						        return Status.OK_STATUS;
    						      }
    						   };
    						diagramUpdaterJob.setRule(component.getRodinProject().getProject()); // the job will need to lock the project
    						diagramUpdaterJob.setPriority(Job.LONG);  // low priority
    						diagramUpdaterJob.setProperty(RODIN_COMPONENT, ((IRodinFile) component).getRoot());
    						IRodinElementDelta ac = affectedComponent;
    						IRodinFile mfe = (IRodinFile) (ac==null? null : ac.getMovedFromElement());
    						String bn = mfe==null? "" : mfe.getBareName();
    						diagramUpdaterJob.setProperty(OLD_COMPONENT_NAME, bn); //((IRodinFile) affectedComponent.getMovedFromElement()).getBareName());				
    						diagramUpdaterJob.schedule();
    					}else if (affectedComponent.getKind() == IRodinElementDelta.ADDED){
    						//Do nothing
    					}
    				}
    			}
    		}
			if ((affectedProject.getFlags() & IRodinElementDelta.F_MOVED_FROM) != 0){
				//DiagramJobs.ScheduleDiagramUpdateForProjectRename((IRodinProject)affectedProject.getElement(), ((IRodinProject) affectedProject.getMovedFromElement()).getElementName());
			}else if (affectedProject.getKind() == IRodinElementDelta.REMOVED){
				//Do nothing
			}else if (affectedProject.getKind() == IRodinElementDelta.ADDED){
				// this notification may result from a copy-paste of the project. 
				// Since we do not know the pre-name, we set oldName to null which will match all project name references
				//DiagramJobs.ScheduleDiagramUpdateForProjectRename((IRodinProject)affectedProject.getElement(), null);
			}
    	}
	}

}
