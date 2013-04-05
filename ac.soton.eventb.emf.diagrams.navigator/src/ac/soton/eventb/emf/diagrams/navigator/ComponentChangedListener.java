package ac.soton.eventb.emf.diagrams.navigator;

import org.eclipse.core.resources.IProject;
import org.eventb.core.IEventBRoot;
import org.rodinp.core.ElementChangedEvent;
import org.rodinp.core.IElementChangedListener;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinElementDelta;
import org.rodinp.core.IRodinFile;

public class ComponentChangedListener implements IElementChangedListener {

	/**
	 * Listens for changes to the Rodin database so that diagram files can be kept in step with changes to machines, contexts, etc.
	 * 
	 */
	@Override
	public void elementChanged(ElementChangedEvent event) {
		//we are only interested in POST_CHANGE events (thats all thats implemented at current anyway)
    	//if (event.getType() != ElementChangedEvent.POST_CHANGE) return;
    	
    	for (IRodinElementDelta affectedProject : event.getDelta().getAffectedChildren()){
    		for (IRodinElementDelta affectedComponent : affectedProject.getAffectedChildren()){
    			IRodinElement component = affectedComponent.getElement();    			
    			if (component instanceof IRodinFile){
    				IRodinFile rf = (IRodinFile)component;
    				if (rf.getRoot() instanceof IEventBRoot){
    					if ((affectedComponent.getFlags() & IRodinElementDelta.F_MOVED_FROM) != 0){
    						String oldMachineName = ((IRodinFile) affectedComponent.getMovedFromElement()).getBareName();		
    						DiagramUtil.renameDiagramFiles((IEventBRoot)rf.getRoot(), oldMachineName);
    					}else if (affectedComponent.getKind() == IRodinElementDelta.REMOVED){
    						IProject project = affectedProject.getElement().getResource().getProject();
    						DiagramUtil.deleteDiagramFiles(project, rf.getBareName());
    					}else if (affectedComponent.getKind() == IRodinElementDelta.ADDED){
    						//Do nothing
    					}
    				}
    			}
    		}
    	}
	}
	
}
