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
package ac.soton.eventb.emf.diagrams.navigator.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.persistence.EMFRodinDB;
import org.eventb.emf.persistence.SaveResourcesCommand;

import ac.soton.eventb.emf.diagrams.generator.commands.DeleteGeneratedCommand;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;
import ac.soton.eventb.emf.diagrams.navigator.jobs.DiagramUtil;


/**
 * Command handler for deleting a diagram element and its associated diagram
 * as well as all the elements that have been generated from it.
 * 
 * @author cfsnook
 *
 */
public class DeleteDiagramElementHandler extends AbstractHandler {
	
	private final IProgressMonitor monitor = new NullProgressMonitor();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			final EventBElement eventBElement = getDiagramElementToDelete(element);
			final Resource resource = eventBElement.eResource();
			TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(resource);
			if (editingDomain != null) {
				// command to delete the diagram element from the model
				Command deleteDiagramCommand = 
						new RecordingCommand(editingDomain, "Delete Diagram Command") {
							protected void doExecute() {
								EcoreUtil.delete(eventBElement, true);}
						};
				//command to delete any model elements that have been generated from the diagram element
				// (this command is provided by the generator plug-in)
				DeleteGeneratedCommand deleteGeneratedCommand = new DeleteGeneratedCommand(editingDomain, eventBElement);
				if (deleteDiagramCommand.canExecute() && deleteGeneratedCommand.canExecute()){
					//delete the diagram layout file
					DiagramUtil.deleteDiagramFile(eventBElement);
					//delete the elements that have been generated from the diagram
					deleteGeneratedCommand.execute(monitor, null);
					//delete the diagram model element (done last as elements and layout can be re-generated)
					deleteDiagramCommand.execute();
					resource.setModified(true);
					// save all resources that have been modified
					SaveResourcesCommand saveCommand = new SaveResourcesCommand(editingDomain);
					if (saveCommand.canExecute()){
							saveCommand.execute(monitor, null);
					}
				}else{
					DiagramsNavigatorExtensionPlugin.logError("Failed to delete diagram and generated elements - aborted delete of : "+eventBElement);
				}
			}
		}
		return null;
	}

/**
 * if element is adaptable, adapts the element to an EObject and then if necessary loads it
 * and returns it as an EventBElement
 * @param element
 * @return EventBElement (loaded)
 */
	private EventBElement getDiagramElementToDelete(Object element) {
		if (element instanceof IAdaptable) {
			EObject eobject = (EObject) ((IAdaptable) element).getAdapter(EObject.class);
			if (eobject.eIsProxy()) {
				EMFRodinDB emfrdb = new EMFRodinDB();
				eobject = emfrdb.loadElement(((InternalEObject)eobject).eProxyURI());
			}
			if (eobject instanceof EventBElement) {
				return (EventBElement)eobject;
			}
		}
		return null;
	}
	
}
