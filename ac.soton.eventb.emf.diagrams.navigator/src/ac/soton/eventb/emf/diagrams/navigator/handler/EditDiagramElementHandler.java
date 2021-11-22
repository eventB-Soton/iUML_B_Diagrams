/*******************************************************************************
 * Copyright (c) 2021, 2021 University of Southampton.
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

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.persistence.EMFRodinDB;

import ac.soton.eventb.emf.diagrams.UMLB;
import ac.soton.eventb.emf.diagrams.navigator.UmlbDiagramUtils;

/**
 * This edits the properties of the selected diagram model
 * 
 * @author cfs
 * @since 3.3
 */
public class EditDiagramElementHandler extends AbstractHandler implements IHandler {

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
			EventBElement eventBElement = loadEventBElement(element);
			
			if (!(eventBElement instanceof UMLB)) {
				return null;
			}
			final UMLB umlb = (UMLB) eventBElement;
			final URI resourceURI = eventBElement.eResource().getURI();
			//final String oldUmlbName = resourceURI.trimFileExtension().lastSegment();
			final String projectName = resourceURI.segment(resourceURI.segmentCount()-2);
			final EventBNamedCommentedComponentElement oldElaborates = umlb.getElaborates();
			final UMLB oldRefines = umlb.getRefines();
			
			//TODO: Would be better to have a single custom dialogue to enter all details
			//TODO: do we allow name changes.. it has to change the resource name as well as the internal name since they should always match
			
			//Ask user for a new elaborated component
			InputDialog dialog = new InputDialog(
					Display.getCurrent().getActiveShell(), 
					"Target Machine or Context", 
					"file name: ",
					oldElaborates.getName() + ".bum",
					nameValidator);
			if (dialog.open() != InputDialog.CANCEL) {
				try {
					umlb.setElaborates(UmlbDiagramUtils.createElaboratedComponentProxy(projectName, dialog.getValue().trim()));
				} catch (CoreException e) {
					throw new ExecutionException("failed to set elaborates for umlb element", e);
				}
			}
			
			//ask user for a new refined UML-B
			dialog = new InputDialog(
					Display.getCurrent().getActiveShell(), 
					"Refined UML-B Diagram Model", 
					" name.ext: ",
					oldRefines.getName()+".umlb",
					nameValidator);
			if (dialog.open() == InputDialog.CANCEL) {
				try {
					umlb.setRefines(UmlbDiagramUtils.createRefinedUmlbProxy(projectName, dialog.getValue().trim()));
				} catch (CoreException e) {
					throw new ExecutionException("failed to set refines for umlb element", e);
				}
			}
			
			// Do the save within an operation
			WorkspaceModifyOperation operation =
				new WorkspaceModifyOperation() {
					@Override
					protected void execute(IProgressMonitor progressMonitor) {
						try {						
							//Map<Object, Object> options = new HashMap<Object, Object>();
							//options.put(XMLResource.OPTION_ENCODING, "UTF-8" ); //elaboratedComponentPage.getEncoding());
							eventBElement.eResource().save(Collections.EMPTY_MAP); //options);
						}
						catch (Exception exception) {
							exception.printStackTrace();
						}
						finally {
							progressMonitor.done();
						}
					}
				};

			try {
				operation.run(monitor);
			} catch (InvocationTargetException | InterruptedException e) {
				throw new ExecutionException("failed to save umlb element", e);
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

}
