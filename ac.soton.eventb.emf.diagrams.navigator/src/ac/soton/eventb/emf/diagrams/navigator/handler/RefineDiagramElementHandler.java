/*******************************************************************************
 * Copyright (c) 2020-2020 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package ac.soton.eventb.emf.diagrams.navigator.handler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.persistence.EMFRodinDB;
import org.eventb.emf.persistence.SaveResourcesCommand;

import ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner;
import ac.soton.eventb.emf.core.extension.navigator.refiner.ElementRefinerRegistry;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;

/**
 * This creates a refinement copy of the selected diagram model
 * 
 * @author cfs
 * @since 3.2
 */
public class RefineDiagramElementHandler extends AbstractHandler implements IHandler {

	private final IProgressMonitor monitor = new NullProgressMonitor();
	
	/**
	 * a name validator for the input dialog that gets a new name
	 */
	static final IInputValidator nameValidator = new IInputValidator(){
		@Override
		public String isValid(String name) {
			if (name.trim().isEmpty())
				return "";
			return null;
		}
	};
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			
			//load the EventBElement corresponding to the element selected in the navigator
			final EventBElement eventBElement = loadEventBElement(element);
			
			//Ask user for a new name
			InputDialog dialog = new InputDialog(
					Display.getCurrent().getActiveShell(), 
					"Refined UML-B Diagram Model", 
					" name: ",
					eventBElement.eResource().getURI().trimFileExtension().lastSegment()+"0",
					nameValidator);
			if (dialog.open() == InputDialog.CANCEL) return null;
			
			//work out the URI of the refined resource using the new name
			URI concreteResourceUri = getRefinedResourceURI(eventBElement.eResource(), dialog.getValue().trim());
			
			//get a resource for the refined element (it will be created if it does not exist already)
			EMFRodinDB emfRodinDB = new EMFRodinDB();
			final Resource targetResource = emfRodinDB.getResource(concreteResourceUri);

			//find a suitable refiner
			AbstractElementRefiner refiner = ElementRefinerRegistry.getRegistry().getRefiner(eventBElement);
			if (refiner!=null) {
				
				//make the refined element
				Map<EObject,EObject> copier = refiner.refine(eventBElement, concreteResourceUri);
				EventBElement refinedElement = (EventBElement) copier.get(eventBElement);

				//put the refined element in the resource
				emfRodinDB.setContent(targetResource, refinedElement);
			
				//make sure the resource is marked as modified
				targetResource.setModified(true); 
				
				// save all resources that have been modified
				SaveResourcesCommand saveCommand = new SaveResourcesCommand(emfRodinDB.getEditingDomain());
				if (saveCommand.canExecute()){
						saveCommand.execute(monitor, null);
				}
			}else{
				DiagramsNavigatorExtensionPlugin.logError("Failed to refine diagram - aborted refine of : "+eventBElement);
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
	private EventBElement loadEventBElement(Object element) {
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
	
	/**
	 * returns the URI for a new resource, based on the given resource but with the filename changed to newName
	 * @param resource
	 * @param newName
	 * @return
	 */
	private URI getRefinedResourceURI(Resource resource, String newName) {
		URI newUri = resource.getURI();
		String fileExtension = newUri.fileExtension();
		newUri = newUri.trimSegments(1).appendSegment(newName).appendFileExtension(fileExtension);
		return newUri; 
	}
}
