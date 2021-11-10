/*******************************************************************************
 * Copyright (c) 2014, 2020 University of Southampton.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    University of Southampton - initial API and implementation
 *******************************************************************************/
package ac.soton.eventb.emf.diagrams.navigator.jobs;

import org.eclipse.ui.IStartup;
import org.eventb.core.IEventBRoot;
import org.rodinp.core.ElementChangedEvent;
import org.rodinp.core.IElementChangedListener;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.IRodinElementDelta;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;

public class DiagramUpdaterListener implements IElementChangedListener, IStartup {

	@Override
	public void earlyStartup() {
		// nothing to do - the listener is added by the plugin start so that it can remove it if the plug-in is stopped
	}
	
		   
	/**
	 * Listens for changes to the Rodin database so that diagram files and diagram models can be kept in step with changes to machines, contexts, etc.
	 * Any required diagram updates are scheduled as Jobs (using DiagramJobs) so that they can run in the background as a separate thread.
	 * (This is necessary as the resource tree is locked at the time of notification).
	 * 
	 */
	@Override
	public void elementChanged(ElementChangedEvent event) {
    	for (IRodinElementDelta affectedProject : event.getDelta().getAffectedChildren()){
    		for (IRodinElementDelta affectedComponent : affectedProject.getAffectedChildren()){
    			IRodinElement component = affectedComponent.getElement();    			
    			if (component instanceof IRodinFile){
    				IRodinFile rodinFile = (IRodinFile)component;
    				if (rodinFile.getRoot() instanceof IEventBRoot){
    					if ((affectedComponent.getFlags() & IRodinElementDelta.F_MOVED_FROM) != 0){	
    						//FIXME: The following 'if' is a workaround as the Rodin builder copies files and is stopped if we schedule a job
    						if (!((IRodinFile) affectedComponent.getMovedFromElement()).getElementName().endsWith("_tmp")){ //clean causes many renames from temp files which we must ignore
    							DiagramJobs.ScheduleDiagramUpdateForComponentRename((IRodinProject)affectedProject.getElement(),(IEventBRoot)rodinFile.getRoot(),((IRodinFile) affectedComponent.getMovedFromElement()).getBareName());
    						}
    					}else if (affectedComponent.getKind() == IRodinElementDelta.REMOVED){
    						//FIXME: Since we only get POST notifications, it is not possible to determine the content of the removed component to see whether any diagrams need to be deleted.
    						//FIXME:	Tried several workarounds but nothing is very satisfactory or safe (there is a danger of deleting the wrong diagrams) - therefore leave it to the user.
    						//FIXME:	Have raised a feature request to notify PRE changes.
							//	DiagramJobs.ScheduleDiagramUpdateForComponentDeletion((IRodinProject)affectedProject.getElement(),rodinFile.getBareName());
    					}else if (affectedComponent.getKind() == IRodinElementDelta.ADDED){
    						//Do nothing
    					}
    				}
    			}
    		}
			if ((affectedProject.getFlags() & IRodinElementDelta.F_MOVED_FROM) != 0){
				DiagramJobs.ScheduleDiagramUpdateForProjectRename((IRodinProject)affectedProject.getElement(), ((IRodinProject) affectedProject.getMovedFromElement()).getElementName());
			}else if (affectedProject.getKind() == IRodinElementDelta.REMOVED){
				//Do nothing
			}else if (affectedProject.getKind() == IRodinElementDelta.ADDED){
				// this notification may result from a copy-paste of the project. 
				// Since we do not know the pre-name, we set oldName to null which will match all project name references
				DiagramJobs.ScheduleDiagramUpdateForProjectRename((IRodinProject)affectedProject.getElement(), null);
			}
    	}
	}


}
