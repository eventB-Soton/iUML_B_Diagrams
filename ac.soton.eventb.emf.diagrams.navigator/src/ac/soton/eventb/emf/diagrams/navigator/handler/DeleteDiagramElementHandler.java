/*******************************************************************************
 * Copyright (c) 2011 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package ac.soton.eventb.emf.diagrams.navigator.handler;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ac.soton.eventb.emf.diagrams.generator.Activator;
import ac.soton.eventb.emf.diagrams.generator.command.DeleteGeneratedCommand;
import ac.soton.eventb.emf.diagrams.navigator.DiagramUtil;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;


/**
 * Command handler for deleting a diagram element and its associated diagram.
 * 
 * @author cfsnook
 *
 */
public class DeleteDiagramElementHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IAdaptable) {
				final EObject eobject = (EObject) ((IAdaptable) element).getAdapter(EObject.class);
				final Resource resource = eobject.eResource();

				
				if (resource != null && resource.isLoaded()) {
					TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(resource);
					if (editingDomain != null) {
						// command to delete the diagram element from the model
						// TODO: could use a DeleteCommand here
						Command cmd = 
								new RecordingCommand(editingDomain, "Delete Diagram Command") {
									protected void doExecute() {EcoreUtil.delete(eobject, true);}
								};
						//command to delete any model elements that have been generated from the diagram element
						// (this command is provided by the generator plug-in)
						DeleteGeneratedCommand deleteGeneratedCommand = new DeleteGeneratedCommand(editingDomain, eobject);
						try {
							if (cmd.canExecute()){
								//try to execute the command to delete generated elements
								if (deleteGeneratedCommand.canExecute()){
									deleteGeneratedCommand.execute(null, null);
								}else{
									Activator.logError("Failed to delete generated elements - aborted delete of : "+eobject);
									return null;
								}
								//delete the diagram layout file
								DiagramUtil.deleteDiagramFile(eobject);
								//delete the diagram model element
								editingDomain.getCommandStack().execute(cmd);
								//save the model
								resource.save(Collections.emptyMap());
							}
						} catch (IOException e) {
							DiagramsNavigatorExtensionPlugin.getDefault().getLog().log(new Status(Status.ERROR, DiagramsNavigatorExtensionPlugin.PLUGIN_ID, "Failed saving after delete", e));
						}
					}
				}
			}
		}
		return null;
	}

}
