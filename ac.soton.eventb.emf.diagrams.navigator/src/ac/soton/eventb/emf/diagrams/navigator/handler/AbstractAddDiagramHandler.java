/*******************************************************************************
 * Copyright (c) 2020-2020 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package ac.soton.eventb.emf.diagrams.navigator.handler;

import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.core.IEventBRoot;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;

import ac.soton.eventb.emf.core.extension.navigator.provider.ExtensionNavigatorItem;
import ac.soton.eventb.emf.diagrams.Diagram;
import ac.soton.eventb.emf.diagrams.DiagramOwner;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;

/**
 * Abstract Command handler for adding a new diagram to an Event-B Root or a diagram owner.
 * This can be extended to easily create a handler to add a specific kind of diagram
 * The specific handler should then be used in an command extension
 * 
 *
 * @author cfsnook
 * @since 3.2
 *
 */
public abstract class AbstractAddDiagramHandler extends AbstractHandler {

	/**
	 * This should return the new instance of the diagram to be added to the selected container
	 * @return
	 */
	public abstract Diagram createNewDiagram();
	
	// name validator
	/**
	 * @since 3.2
	 */
	static final IInputValidator nameValidator = new IInputValidator(){

		@Override
		public String isValid(String name) {
			if (name.trim().isEmpty())
				return "";
			return null;
		}
	};
	
	/**
	 * EMF command for adding a diagram to a machine or other container file.
	 * 
	 *
	 */
	public class AddDiagramCommand extends AbstractEMFOperation {

		private URI uri;
		private Diagram diagram;
		private String diagramKind;

		public AddDiagramCommand(URI uri, Diagram diagram, String diagramKind) {
			super(TransactionalEditingDomain.Factory.INSTANCE.createEditingDomain(), diagramKind);
			this.diagramKind = diagramKind;
			this.uri = uri;
			this.diagram = diagram;
		}

		@Override
		protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info)
				throws ExecutionException {
			monitor.beginTask("Creating "+diagramKind, IProgressMonitor.UNKNOWN);
			
			try {
				Resource resource = getEditingDomain().getResourceSet().getResource(uri, true);
				
				if (resource != null && resource.isLoaded()) {
					EventBElement container =  (EventBElement) resource.getContents().get(0);
					if (container instanceof DiagramOwner) {
						((DiagramOwner)container).getDiagrams().add(diagram);
					}else if (diagram instanceof AbstractExtension){
						container.getExtensions().add((AbstractExtension)diagram);
					}
					resource.save(Collections.emptyMap());
				}
			} catch (Exception e) {
				return new Status(Status.ERROR, DiagramsNavigatorExtensionPlugin.PLUGIN_ID, "Failed to add "+diagramKind, e);
			} finally {
				monitor.done();
			}
			return Status.OK_STATUS;
		}

	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (selection instanceof IStructuredSelection) {
			URI uri = getFileURI(((IStructuredSelection) selection).getFirstElement());
				if (uri != null) {
					
					Diagram diagram = createNewDiagram();
					String diagramKind = diagram.eClass().getName();
					
					if (diagram instanceof EventBNamed) {
						InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(), 
								"New "+diagramKind, 
								diagramKind+" name: ",
								null, nameValidator);
						if (dialog.open() == InputDialog.CANCEL)
							return null;
						String name = dialog.getValue().trim();
						((EventBNamed)diagram).setName(name);
					}
					
					try {
						AddDiagramCommand command = new AddDiagramCommand(uri, diagram, diagramKind);
						if (command.canExecute())
							command.execute(new NullProgressMonitor(), null);
					} catch (Exception e) {
						DiagramsNavigatorExtensionPlugin.logError("Creating "+diagramKind+" failed", e);
					}
				}
		}
		return null;
	}

	private URI getFileURI(Object element) {
		if (element instanceof IEventBRoot) {
			IFile file = ((IEventBRoot)element).getResource();
			if (file==null || !file.exists()) return null;
			return URI.createPlatformResourceURI(file.getFullPath().toOSString(), true);
		}else if (element instanceof ExtensionNavigatorItem) {
			if (((ExtensionNavigatorItem)element).getEObject() instanceof DiagramOwner) {
				return ((ExtensionNavigatorItem)element).getEObject().eResource().getURI();
			}
		}
		return null;
	}

	
}
