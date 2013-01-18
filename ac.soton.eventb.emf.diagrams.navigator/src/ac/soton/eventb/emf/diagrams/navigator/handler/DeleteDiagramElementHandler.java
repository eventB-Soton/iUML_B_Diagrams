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
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ac.soton.eventb.emf.diagrams.generator.Activator;
import ac.soton.eventb.emf.diagrams.generator.command.DeleteGeneratedCommand;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;
import ac.soton.eventb.emf.diagrams.navigator.provider.IDiagramProvider;


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
				// find diagram provider
				Map<String, IDiagramProvider> registry = DiagramsNavigatorExtensionPlugin.getDefault().getDiagramProviderRegistry();
				String type = eobject.eClass().getName();
				IDiagramProvider provider = registry.get(type);
				
				if (resource != null && resource.isLoaded()) {
					TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(resource);
					if (editingDomain != null) {
						// execute as command
						Command cmd = new RecordingCommand(editingDomain, "Delete Diagram Command") {
							protected void doExecute() {
								deleteGeneratedElements(eobject);
								EcoreUtil.delete(eobject, true);
							}
						};
		
						try {
							if (cmd.canExecute()){
								DeleteGeneratedCommand deleteGeneratedCommand = new DeleteGeneratedCommand(editingDomain, eobject);
								if (deleteGeneratedCommand.canExecute()){
									deleteGeneratedCommand.execute(null, null);
								}else{
									Activator.logError("Failed to delete generated elements - aborted delete of : "+eobject);
									return null;
								}
								deleteDiagramFile(resource, provider, eobject);
								editingDomain.getCommandStack().execute(cmd);
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

	protected void deleteGeneratedElements(EObject eobject) {
		
		
	}

	private void deleteDiagramFile(Resource resource, IDiagramProvider provider, EObject element) {
		if (provider == null) return;
		String filename = provider.getDiagramFileName(element);
		IFile domainFile = WorkspaceSynchronizer.getFile(resource);
		IProject project = domainFile.getProject();
		IFile diagramFile = project.getFile(filename);
		if (diagramFile.exists()) {
			try {
				diagramFile.delete(false,false,new NullProgressMonitor());
			} catch (CoreException e) {
				DiagramsNavigatorExtensionPlugin.getDefault().getLog().log(new Status(Status.ERROR, DiagramsNavigatorExtensionPlugin.PLUGIN_ID, "Failed deleting diagram File", e));
			}
		}

	}

}
