/*******************************************************************************
 * Copyright (c) 2014, 2021 University of Southampton.
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
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.persistence.EMFRodinDB;
import org.eventb.emf.persistence.SaveResourcesCommand;

import ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner;
import ac.soton.eventb.emf.core.extension.navigator.refiner.ElementRefinerRegistry;
import ac.soton.eventb.emf.diagrams.UMLB;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;

/**
 * This creates a refinement copy of the selected umlb diagram model
 * 
 * If the user given target Event-B component does not exist already, it is also created by refining the 
 * component that is elaborated by the abstract UMLB that is being refined.
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
			//final EventBElement eventBElement = loadEventBElement(element);
			final UMLB umlb = (UMLB)loadEventBElement(element);
			final EventBNamedCommentedComponentElement targetEventBComponent = umlb.getElaborates();
			
			EMFRodinDB emfRodinDB = new EMFRodinDB();
			
			//Ask user for a new name for the refined UML-B
			InputDialog dialog = new InputDialog(
					Display.getCurrent().getActiveShell(), 
					"Refined UML-B Diagram Model", 
					"Name of refined UML-B: ",
					umlb.eResource().getURI().trimFileExtension().lastSegment(),
					nameValidator);
			if (dialog.open() == InputDialog.CANCEL) return null;
			String refinedUmlbName = dialog.getValue().trim();
			//work out the URI of the refined resource using the new name
			URI refinedUMLBUri = getRefinedResourceURI(umlb.eResource(), refinedUmlbName);
			//get a resource for the refined UMLB (it will be created if it does not exist already)
			final Resource refinedUMLBResource = emfRodinDB.getResource(refinedUMLBUri);
			
			//Ask user for name of the refined EventB component (to be elaborated)
			dialog = new InputDialog(
					Display.getCurrent().getActiveShell(), 
					"Refined Event-B component to set as elaborated target (will be created if doesn't exist)", 
					"Name of refined Event-B component: ",
					targetEventBComponent.eResource().getURI().lastSegment(),
					nameValidator);
			if (dialog.open() == InputDialog.CANCEL) return null;
			String refinedTargetName = dialog.getValue().trim();
			//work out the URI of the refined resource using the new name
			URI refinedTargetUri = getRefinedResourceURI(targetEventBComponent.eResource(), refinedTargetName);
			//get a resource for the refined EventB target (it will be created if it does not exist already)
			final Resource refinedTargetResource = emfRodinDB.getResource(refinedTargetUri);
			

			//find a suitable refiner for the UMLB
			AbstractElementRefiner refiner = ElementRefinerRegistry.getRegistry().getRefiner(umlb);
			if (refiner!=null) {
				
				//make the refined UMLB element
				Map<EObject,EObject> copier = refiner.refine(umlb, refinedUMLBUri);
				UMLB refinedUMLB = (UMLB) copier.get(umlb);
				
				EventBNamedCommentedComponentElement refinedTargetEventBComponent = null;
				
				// if the user given target resource contains a valid Event-B component we use it
				EventBElement content = emfRodinDB.loadEventBComponent(refinedTargetUri); 
				if (content instanceof EventBNamedCommentedComponentElement) {
						//!refinedTargetResource.getContents().isEmpty() && 
						//refinedTargetResource.getContents().get(0) instanceof EventBNamedCommentedComponentElement) {
					refinedTargetEventBComponent = (EventBNamedCommentedComponentElement)content;  //refinedTargetResource.getContents().get(0);
				}else {
				//if not, refine the abstract target and put it in the user given target resource
					refiner = ElementRefinerRegistry.getRegistry().getRefiner(targetEventBComponent);
					if (refiner!= null){
						copier = refiner.refine(targetEventBComponent, refinedTargetUri);
						refinedTargetEventBComponent = (EventBNamedCommentedComponentElement) copier.get(targetEventBComponent);
						//put the refined element in the resource
						emfRodinDB.setContent(refinedTargetResource, refinedTargetEventBComponent);
						refinedTargetResource.setModified(true);
					}
				}
				//set whatever Event-B component we found/made as the elaborated target in the new refined UMLB
				refinedUMLB.setElaborates(refinedTargetEventBComponent);
				
				
				//put the refined umlb element in the resource
				emfRodinDB.setContent(refinedUMLBResource, refinedUMLB);
				//make sure the resource is marked as modified
				refinedUMLBResource.setModified(true); 
				
				// save all resources that have been modified
				SaveResourcesCommand saveCommand = new SaveResourcesCommand(emfRodinDB.getEditingDomain());
				if (saveCommand.canExecute()){
						saveCommand.execute(monitor, null);
				}
			}else{
				DiagramsNavigatorExtensionPlugin.logError("Failed to refine diagram - aborted refine of : "+umlb);
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
		if (newName.endsWith("."+fileExtension)) {
			newName=newName.substring(0, newName.lastIndexOf("."+fileExtension));
		}
		newUri = newUri.trimSegments(1).appendSegment(newName).appendFileExtension(fileExtension);
		return newUri; 
	}
}
