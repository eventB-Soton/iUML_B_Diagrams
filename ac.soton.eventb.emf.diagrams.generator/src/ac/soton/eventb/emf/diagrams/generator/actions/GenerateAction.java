/**
 * Copyright (c) 2012 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.generator.actions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.emf.core.EventBElement;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import ac.soton.eventb.emf.diagrams.generator.Activator;
import ac.soton.eventb.emf.diagrams.generator.impl.Generator;
import ac.soton.eventb.emf.diagrams.generator.impl.Messages;
import ac.soton.eventb.emf.diagrams.generator.impl.ValidatorRegistry;


//import ac.soton.eventb.emf.components.diagram.part.ValidateAction;

/**
 * Generate action handler.
 * 
 * @author colin 
 *
 */
public class GenerateAction extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
		if (editor instanceof DiagramDocumentEditor) {
			final DiagramDocumentEditor diagramDocumentEditor = (DiagramDocumentEditor)editor;
			
			//final IDiagramWorkbenchPart diagramEditor = (IDiagramWorkbenchPart) editor;
			
			if (diagramDocumentEditor.getDiagram().getElement() instanceof EventBElement){
				final EventBElement eventBElement = (EventBElement) diagramDocumentEditor.getDiagram().getElement();

				// save before transformation
				if (editor.isDirty())
					editor.doSave(new NullProgressMonitor());

				// first validate, then transform
				if (ValidatorRegistry.validate(diagramDocumentEditor)){
						//IStatus.OK == ValidateAction.validate(diagramEditor.getDiagramEditPart(), diagramEditor.getDiagram())) {

					final GenerateCommand generateCommand = new GenerateCommand(
							diagramDocumentEditor.getDiagramEditPart().getEditingDomain(), 
							eventBElement);
					if (generateCommand.canExecute()) {	
						// run with progress
						ProgressMonitorDialog dialog = new ProgressMonitorDialog(diagramDocumentEditor.getSite().getShell());
						try {
							dialog.run(true, true, new IRunnableWithProgress(){
							     public void run(IProgressMonitor monitor) {
							         monitor.beginTask(Messages.GENERATOR_MSG_05, IProgressMonitor.UNKNOWN);
							         try {
										generateCommand.execute(monitor, diagramDocumentEditor);
									} catch (ExecutionException e) {
										Activator.logError(Messages.GENERATOR_MSG_06, e);
									}
							         monitor.done();
							     }
							 });
						} catch (InvocationTargetException e) {
							Activator.logError(Messages.GENERATOR_MSG_07, e);
							return null;
						} catch (InterruptedException e) {
							Activator.logError(Messages.GENERATOR_MSG_08, e);
							return null;
						} 
	
						// error feedback
						if (false == generateCommand.getCommandResult().getStatus().isOK())
							MessageDialog
									.openError(editor.getSite().getShell(),
											Messages.GENERATOR_MSG_09,
											Messages.GENERATOR_MSG_10);
					}
				}else{
					//validation failed - get errors
					String errors = ValidatorRegistry.getValidationErrors(diagramDocumentEditor);
					if (errors.isEmpty()) {
						MessageDialog.openError(null, "Generator interrupted", "Validation failed but no errors were reported");
					} else {
						MessageDialog.openError(null, "Generator interrupted", "Validation failed with the following errors:\n" + errors);
					}
				}
			}
		}
		return null;
	}

	

	//////////////////////GENERATE COMMAND//////////////////////////
	private static class GenerateCommand extends AbstractTransactionalCommand {

		private EventBElement element;

		/**
		 * @param domain
		 * @param label
		 * @param affectedFiles
		 */
		public GenerateCommand(TransactionalEditingDomain editingDomain, EventBElement element) {
			super(editingDomain, Messages.GENERATOR_MSG_11, null);
			setOptions(Collections.singletonMap(Transaction.OPTION_UNPROTECTED, Boolean.TRUE));
			this.element = element;
		}
		
		@Override
		public boolean canRedo(){
			return false;
		}

		@Override
		public boolean canUndo(){
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand#doExecuteWithResult(org.eclipse.core.runtime.IProgressMonitor, org.eclipse.core.runtime.IAdaptable)
		 */
		@Override
		protected CommandResult doExecuteWithResult(IProgressMonitor monitor,
				IAdaptable info) throws ExecutionException {
			
			try {
				
				RodinCore.run(new IWorkspaceRunnable() {
					public void run(final IProgressMonitor monitor) throws CoreException{
						TransactionalEditingDomain editingDomain = getEditingDomain();
						final List<Resource> modifiedResources;
						
						monitor.beginTask(Messages.GENERATOR_MSG_12,10);		
						monitor.setTaskName(Messages.GENERATOR_MSG_13(element)); 

						// flush the command stack as this is unprotected and has no undo/redo
						editingDomain.getCommandStack().flush();
						
				        monitor.subTask(Messages.GENERATOR_MSG_14);
						//try to create an appropriate generator
						Generator generator = new Generator(element.eClass());

				        monitor.subTask(Messages.GENERATOR_MSG_15);
				        
				        //try to run the generation
						modifiedResources = generator.generate(editingDomain,element);

						if (modifiedResources == null){
							
							//ErrorDialog errorDialog = new ErrorDialog(diagramEditor.getSite().getShell(), label, label, null, 0); 
							//should display a message here 
							//"Generation Failed - see error log for details"
							
						}else{
						
							//try to save all the modified resources
					        monitor.subTask(Messages.GENERATOR_MSG_16);
							for (Resource resource : modifiedResources){
								try {
									resource.save(Collections.emptyMap());
								} catch (IOException e) {
									//throw this as a CoreException
									throw new CoreException(
											new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.GENERATOR_MSG_18(resource), e));
								}					
							}
							
						}
					monitor.done();
					}
				},monitor);
				
				return CommandResult.newOKCommandResult();

			} catch (RodinDBException e) {
				Activator.logError(Messages.GENERATOR_MSG_19, e);
				return CommandResult.newErrorCommandResult(e);
			} catch (WrappedException e) {
				Activator.logError(Messages.GENERATOR_MSG_20, e);
				return CommandResult.newErrorCommandResult(e);

			} finally {
				monitor.done();
			}
		}
	}
}
