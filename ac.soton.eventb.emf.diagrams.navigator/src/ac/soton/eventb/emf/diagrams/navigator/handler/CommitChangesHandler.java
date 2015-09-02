/**
 * Copyright (c) 2012 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.navigator.handler;

import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.core.IMachineRoot;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.machine.Machine;
import org.eventb.emf.persistence.EMFRodinDB;

import ac.soton.eventb.emf.diagrams.generator.actions.GenerateAllHandler;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;
import ac.soton.eventb.emf.diagrams.navigator.refactor.CommitAssistant;
import ac.soton.eventb.emf.diagrams.navigator.refactor.Recorder;
import ac.soton.eventb.emf.diagrams.navigator.refactor.persistence.RefactorPersistence;
import ac.soton.eventb.emf.diagrams.navigator.refactor.persistence.TextFile;


/**
 * Commit changes handler.
 * This handler commits the recorded changes made to the selected machine.
 * This consists of:
 * a) archiving the project
 * b) propagating the changes made in the current selected component to each lower refinement
 * c) delete the change record for the current selected component
 * d) generate all diagrams in the current selected component and all lower level refinements
 * 
 * Committing is NOT enabled if ...
 * There is no change record for this refinement level OR
 * ANY of the lower refinement levels have outstanding change records.
 * 
 * @author cfs 
 *
 */
public class CommitChangesHandler extends AbstractHandler {
	
	public static final String[] reportsFolder = {"iumlb","reports"};
	
	/**
	 * Create an EMFRodinDB for loading extensions into EMF
	 */
	private static final EMFRodinDB emfRodinDB = new EMFRodinDB();
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {	
		
		IProgressMonitor monitor = new NullProgressMonitor(); // for now no progress shown
		
		Shell shell = HandlerUtil.getActiveShell(event);
		
		//check refactoring preference is enabled
		Boolean refactoringEnabled =  DiagramsNavigatorExtensionPlugin.getDefault().getPreferenceStore().getBoolean(DiagramsNavigatorExtensionPlugin.PREFERENCES_REFACTORING_ENABLED);
		if (!refactoringEnabled) {
			MessageDialog.open(MessageDialog.INFORMATION, shell,
		    		  "Refactoring is switched off", 
		    		  "Changes are not being recorded because refactoring is disabled in preferences. "+
		    		  " Therefore there are no changes to commit. "+
		    		  " To switch refactoring on, go to Rodin Platform - Preferences and select iUML-B.", SWT.NONE 
		    			);
			return null;
		}

		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IMachineRoot) {
				EventBNamedCommentedComponentElement component = (EventBNamedCommentedComponentElement) emfRodinDB.loadEventBComponent((IMachineRoot)element);
				Resource res = component.eResource();
				IProject project = WorkspaceSynchronizer.getFile(res).getProject();
				String projectName = project.getName(); //emfRodinDB.getProjectName(component); 
				List<EventBNamedCommentedComponentElement> components = emfRodinDB.loadAllComponents(projectName);

				// Check that there is a change record for this refinement level
				if (RefactorPersistence.INSTANCE.hasChanges(EcoreUtil.getURI(component), emfRodinDB.getResourceSet())){
					
					if (!hasLowerLevelChanges(component,components)){
					
						boolean ok = MessageDialog.openConfirm(shell,
					    		  "Confirm Commit", 
					    		  "The Commit operation will propagate the changes accumulated for this refinement level.\n"+
					    		  "This consists of:\n"+
				    			  	  "* the containing project will be archived as a tar file\n"+
					    			  "* the changes made in the current selected component will be propagated to each lower refinement\n"+
					    			  "* the change record for the current selected component will be deleted\n"+
					    			  "* all iUML-B diagrams in the current selected component and all lower level refinements will be re-generated as Event-B.\n"+
					    			  "(all editors will be closed automatically before commit begins)"
					    			);
						if (ok){
							
							//save/close all diagrams
							for(IWorkbenchPage pg : HandlerUtil.getActiveWorkbenchWindow(event).getPages()){
								//pg.closeAllEditors(true);
						    	for(IEditorReference editorRef: pg.getEditorReferences()){
						    		IEditorPart editor = editorRef.getEditor(true);
						    		if (editor instanceof DiagramEditor) {
						    			pg.closeEditor(editor, true);
//						    			DiagramEditor diagramEditor = (DiagramEditor) editor;
//						    			EObject diagramElement = diagramEditor.getDiagram().getElement();
//						    			Resource resource = diagramElement.eResource();
//						    			if (diagramElement instanceof EventBObject){
//					    					EventBObject c = ((EventBObject)diagramElement).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
//					    					if (c!= null && components.contains(c)){
//					    						if (diagramEditor.isDirty()){
//					    							diagramEditor.doSave(new NullProgressMonitor());
//					    						}
//					    						editorRef.getPage().closeAllEditors(true);
//					    						diagramEditor.dispose();
//					    					}
//						    			}
					    	    	}
						    	}
							}
							
							//make an archive of the project just in case
							ArchiveProjectHandler.archiveProject(project, monitor);

							//propagate the changes made in the current selected component to each lower refinement
							GenerateAllHandler genAll = new GenerateAllHandler();
							EventBNamedCommentedComponentElement cp = component;
							EventBNamedCommentedComponentElement rcp = null;
							TextFile report = new TextFile();
							String timeStamp = getTimeStamp();
							report.addLine("Propagation report for commit of changes to "+res.getURI().toPlatformString(true)+" :: "+timeStamp+"\n");

							do {														
								CommitAssistant commitAssistant = new CommitAssistant(cp);			
								if ((rcp=getRefinement(cp, components))!=null 
									&& commitAssistant.hasChanges()){
									Recorder ecr = Recorder.getNewRecorder(rcp);
									ecr.resumeRecording();
									report.addLine("------------------------------");
									report.addText(
											commitAssistant.applyChangesTo(rcp)
											);
									ecr.endRecording();
									ecr.saveChanges();
									ecr.disposeChangeRecorder();
								}
								//delete the change record for the current selected component
								commitAssistant.deleteChangeRecords();
								
								//now generate diagrams in the abstract machine (needs to be done after propagation of its changes)
								report.addLine("");
								report.addLine("------------------------------");
								report.addLine("Generating diagrams for "+cp.getReference());
								report.addText(
									genAll.generateAllDiagrams(cp, shell, emfRodinDB.getEditingDomain(), null)
								);
								//save generated
								try {
									if (!cp.eIsProxy()){
										cp.eResource().save(Collections.EMPTY_MAP);
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
									report.addLine("");
									report.addLine(e.getLocalizedMessage());
									report.addText(e.getStackTrace().toString());
									report.addLine("");
								}
								
							}while ((cp=rcp)!=null);
							
							//save the report
							URI commitReportURI = RefactorPersistence.INSTANCE.getRelatedURI(res, reportsFolder, timeStamp, "report");
							report.save(project, commitReportURI, null);

						}
					}else{
						MessageDialog.openError(shell,
					    		  "Commit Changes", 
					    		  "There are lower level refinements with outstanding changes.\n"
					    		  + "Please commit or revert lower levels first"
					    			  );
					}
				}else
					MessageDialog.openError(shell,
				    		  "Commit Changes", 
				    		  "There are no changes to commit on this refinement.\n"
				    			  );
			}
		}
		return null;
	}

	private EventBNamedCommentedComponentElement getRefinement(EventBNamedCommentedComponentElement abs, List<EventBNamedCommentedComponentElement> components){
		for (EventBNamedCommentedComponentElement cp : components){
			if (isDirectRefinementOf(abs,cp)) {
				if (cp.eIsProxy()){		 //FIXME: needed because commit assistant unloads components in 'components'
					cp = (EventBNamedCommentedComponentElement) emfRodinDB.loadElement(EcoreUtil.getURI(cp));
				}
				return cp;
			}
		}
		return null;
	}
	
	private boolean hasLowerLevelChanges(EventBNamedCommentedComponentElement abs, List<EventBNamedCommentedComponentElement> components){	
		for (EventBNamedCommentedComponentElement cp : components){
			if (isRefinementOf(abs,cp) && RefactorPersistence.INSTANCE.hasChanges(EcoreUtil.getURI(cp),emfRodinDB.getResourceSet())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isRefinementOf(EventBNamedCommentedComponentElement abs, EventBNamedCommentedComponentElement ref){
		if (abs.getReference().equals(ref.getReference())) return false;
		if (abs instanceof Machine && ref instanceof Machine && !((Machine)ref).getRefines().isEmpty()){
			if (isDirectRefinementOf(abs,ref)) return true;
			else return isRefinementOf(abs,((Machine)ref).getRefines().get(0));
		}else return false;
	}
	
	private boolean isDirectRefinementOf(EventBNamedCommentedComponentElement abs, EventBNamedCommentedComponentElement ref){
		if (abs instanceof Machine && ref instanceof Machine 
				&& ((Machine)ref).getRefines().size()>0){
			Machine refined = ((Machine)ref).getRefines().get(0);
			if (abs.getReference().equals(refined.getReference())) return true;
		}
		return false;
	}

	/**
	 * generates a string from the current time
	 * @return
	 */
	public static String getTimeStamp() {
		Calendar cal = Calendar.getInstance();
		String m = ""+(cal.get(Calendar.MONTH)+1);
		m=m.length()<2? "0"+m : m;
		String d = ""+(cal.get(Calendar.DAY_OF_MONTH));
		d=d.length()<2? "0"+d : d;
		String h = ""+(cal.get(Calendar.HOUR_OF_DAY));
		h=h.length()<2? "0"+h : h;
		String n = ""+(cal.get(Calendar.MINUTE));
		n=n.length()<2? "0"+n : n;
		return ""+cal.get(Calendar.YEAR)+m+d+h+n;
	}
}
