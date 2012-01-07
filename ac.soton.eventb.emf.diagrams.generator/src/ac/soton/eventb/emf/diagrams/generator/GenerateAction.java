/**
 * Copyright (c) 2011
 * University of Southampton.
 * All rights reserved. This program and the accompanying materials  are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 * 
 */
package ac.soton.eventb.emf.diagrams.generator;

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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;


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
		
		if (editor instanceof IDiagramWorkbenchPart) {
			
			final IDiagramWorkbenchPart diagramEditor = (IDiagramWorkbenchPart) editor;
			
			if (diagramEditor.getDiagram().getElement() instanceof EventBElement){
				final EventBElement eventBElement = (EventBElement) diagramEditor.getDiagram().getElement();

				// save before transformation
				if (editor.isDirty())
					editor.doSave(new NullProgressMonitor());

				// first validate, then transform
				if (IStatus.OK == validate(diagramEditor.getDiagramEditPart(), diagramEditor.getDiagram())) {

					final GenerateCommand generateCommand = new GenerateCommand(
							diagramEditor.getDiagramEditPart().getEditingDomain(), 
							eventBElement);
					
				//	final Command command = new ICommandProxy(generateCommand);
				//	if (command.canExecute()) {
					if (generateCommand.canExecute()) {	
						// run with progress
						ProgressMonitorDialog dialog = new ProgressMonitorDialog(diagramEditor.getSite().getShell());
						try {
							dialog.run(true, true, new IRunnableWithProgress(){
							     public void run(IProgressMonitor monitor) {
							         monitor.beginTask("Generating Event-B ...", IProgressMonitor.UNKNOWN);
							         try {
										generateCommand.execute(monitor, diagramEditor);
									} catch (ExecutionException e) {
										Activator.logError("Generation failed", e);
									}
							         monitor.done();
							     }
							 });
						} catch (InvocationTargetException e) {
							Activator.logError("Generation failed", e);
							return null;
						} catch (InterruptedException e) {
							Activator.logError("Generation interrupted", e);
							return null;
						} 
	
						// error feedback
						if (false == generateCommand.getCommandResult().getStatus().isOK())
							MessageDialog
									.openError(editor.getSite().getShell(),
											"Generation Information",
											"Generation encountered problems.\n\nSee log for details.");
					}
				}
			}
		}
		return null;
	}

	
	/**
	 * Validates diagram and shows errors found;
	 * return IStatus constant as validation result.
	 * 
	 * @return IStatus constant
	 */
	private int validate(DiagramEditPart diagramEditPart, View view) {
		// first validate the diagram
//		try {
//			ValidateAction.runValidation(diagramEditPart, view);
//		} catch (Exception e) {
//			TransformationPlugin.getDefault().logError("Validation action failed for: " + diagramEditPart.toString(), e);
//			return IStatus.ERROR;
//		}
//		
//		IFile file = WorkspaceSynchronizer.getFile(view.eResource());
//		if (file != null) {
//			try {
//				// get errors
//				String errors = ValidateAction.getValidationErrors(file);
//				
//				if (errors.isEmpty()) {
//					return IStatus.OK;
//				} else {
//					MessageDialog.openError(null, "Translation interrupted", "Validation has found problems in your model:\n" + errors);
//				}
//			} catch (CoreException e) {
//				TransformationPlugin.getDefault().logError("Cannot read markers from file: " + file.getFullPath().toString(), e);
//			}
//		}
//		
//		return IStatus.ERROR;
		return IStatus.OK;
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
			super(editingDomain, "Generate Command", null);
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
						
						String elementName = element instanceof EventBNamed ? ((EventBNamed)element).getName() : element.toString();
						String elementType = element.eClass().getName();
						monitor.beginTask("",10);		
						monitor.setTaskName("Running Event-B Generator for "+	elementType+" : "+elementName);

						// flush the command stack as this is unprotected and has no undo/redo
						editingDomain.getCommandStack().flush();
						
				        monitor.subTask(" Configuring Generator");
						//try to create an appropriate generator
						Generator generator = new Generator(element.eClass());

				        monitor.subTask(" Generating new Event-B elements");
				        
				        //try to run the generation
						modifiedResources = generator.generate(editingDomain,element);

						if (modifiedResources == null){
							
							//ErrorDialog errorDialog = new ErrorDialog(diagramEditor.getSite().getShell(), label, label, null, 0); 
							//should display a message here 
							//"Generation Failed - see error log for details"
							
						}else{
						
							//try to save all the modified resources
					        monitor.subTask(" Saving modified resources");
							for (Resource resource : modifiedResources){
								try {
									resource.save(Collections.emptyMap());
								} catch (IOException e) {
									Activator.logError("Save operation failed after transformation for: " + resource.toString(), e);
									//throw this as a CoreException
									throw new CoreException(
											new Status(Status.ERROR, Activator.PLUGIN_ID, "Save operation failed after transformation for: " + resource.toString(), e));
								}					
							}
							
						}
					monitor.done();
					}
				},monitor);
				
				return CommandResult.newOKCommandResult();

			} catch (RodinDBException e) {
				Activator.logError("Generation Exception", e);
				return CommandResult.newErrorCommandResult(e);
			} catch (WrappedException e) {
				Activator.logError("Unable to load resource for transformation", e);
				return CommandResult.newErrorCommandResult(e);

			} finally {
				monitor.done();
			}
		}
	}
}
