/*******************************************************************************
 * Copyright (c) 2014, 2018 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.navigator.refiner;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eventb.core.IEventBRoot;
import org.eventb.core.basis.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.persistence.EMFRodinDB;
import org.eventb.emf.persistence.SaveResourcesCommand;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRefinementParticipant;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import ac.soton.eventb.emf.diagrams.generator.commands.TranslateAllCommand;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;

/**
 * This refinement participant is intended to be used with any Rodin component type (including 
 * Machines and Contexts) which contains Serialised Extension models (as used in iUML-B)
 * 
 * The refinement participant deletes all previously generated elements 
 * and then schedules a job to regenerate from all diagrams in the component.
 * 
 * Other refinement participants will in parallel to this, copy the diagram extension models into the 
 * refined component, and give them a new extension ID.
 * 
 * Note that the main Rodin refinement participant will have already copied generated elements 
 * into the refined component and they will have the old abstract diagram extension ID
 * (so will not be removed by re-generation from the refined diagram extensions that have new IDs). 
 * The Rodin refinement participant does NOT copy the 'generated' boolean attribute.
 * 
 * 
 * @author cfs
 *
 */
public class DiagramRegenerator implements IRefinementParticipant {

	private static final String QUALIFIER = "ac.soton.eventb.emf.diagrams.navigator";
	private static final QualifiedName RODIN_COMPONENT = new QualifiedName(QUALIFIER, "COMPONENT");
	private static final IAttributeType GENERATOR_ID_ATTRIBUTETYPE = RodinCore.getAttributeType("org.eventb.emf.persistence.generator_ID");

	@Override
	public void process(IInternalElement targetRoot,
			IInternalElement sourceRoot, IProgressMonitor monitor)
			throws RodinDBException {
		deleteGenerated((EventBElement)targetRoot, monitor);
		ScheduleDiagramGeneration(targetRoot);
	}


	/**
	 * Delete all generated elements in the component being refined.
	 * They must be regenerated after the the refinement has completed and the extensions have new identifiers
	 * 
	 * @param sourceElement
	 * @param element
	 * @param monitor
	 * @throws RodinDBException
	 */
	private void deleteGenerated (EventBElement element, IProgressMonitor monitor) throws RodinDBException {
		IRodinElement[] children = element.getChildren();
		for (IRodinElement childElement : children){
			if (childElement instanceof EventBElement){
				deleteGenerated ((EventBElement)childElement, monitor);
			}
		}
		if (element.hasAttribute(GENERATOR_ID_ATTRIBUTETYPE) ){	//element.isGenerated() && 
			element.delete(true,monitor);
		}
	}
	
	/**
	 * Schedule a job to run after the refinement has completed. The job
	 * will re-generate all diagrams in the refined component and then save the component
	 * 
	 * @param targetRoot
	 */
	
	private static void ScheduleDiagramGeneration (IInternalElement targetRoot) {
		Job diagramUpdaterJob = new Job("Updating diagram references for new component name") {
			public IStatus run(IProgressMonitor monitor) {
				final EMFRodinDB emfRodinDB = new EMFRodinDB();
		    	IStatus status = Status.OK_STATUS;
				EventBNamedCommentedComponentElement component = 
						(EventBNamedCommentedComponentElement) emfRodinDB.loadEventBComponent((IEventBRoot)getProperty(RODIN_COMPONENT));
				
				//translate all diagrams
				TranslateAllCommand translateAllCmd = new TranslateAllCommand(emfRodinDB.getEditingDomain(),component);
				if (translateAllCmd.canExecute()){
					try {
						status = translateAllCmd.execute(null, null);
					} catch (ExecutionException e) {
						e.printStackTrace();
						DiagramsNavigatorExtensionPlugin.logError("Failed to generated elements: "+e.getMessage());					
					}
					if (status.isOK()){
						try {
							// save all resources that have been modified
							SaveResourcesCommand saveCommand = new SaveResourcesCommand(emfRodinDB.getEditingDomain());
							if (saveCommand.canExecute()){
									status = saveCommand.execute(monitor, null);
							}
						} catch (Exception e) {
							String statusMessage = status.getMessage();
							for (IStatus childStatus : status.getChildren()){
								statusMessage = statusMessage+"\n"+childStatus.getMessage();
							}
							DiagramsNavigatorExtensionPlugin.logError("Failed to save elements: "+statusMessage);
						}
					}else{
						String statusMessage = status.getMessage();
						for (IStatus childStatus : status.getChildren()){
							statusMessage = statusMessage+"\n"+childStatus.getMessage();
						}
						DiagramsNavigatorExtensionPlugin.logError("Failed to generated elements: "+statusMessage);
					}
					
				}else{
					status = Status.CANCEL_STATUS;
				}
		        return status;
		      }
		   };
		diagramUpdaterJob.setRule(targetRoot.getSchedulingRule());
		diagramUpdaterJob.setPriority(Job.LONG);  // low priority
		diagramUpdaterJob.setProperty(RODIN_COMPONENT, targetRoot);				
		diagramUpdaterJob.schedule();
	}
}
