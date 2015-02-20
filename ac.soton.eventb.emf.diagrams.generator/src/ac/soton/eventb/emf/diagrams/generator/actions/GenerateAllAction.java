/**
 * Copyright (c) 2012 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.generator.actions;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBObject;

import ac.soton.eventb.emf.diagrams.generator.Activator;
import ac.soton.eventb.emf.diagrams.generator.command.GenerateCommand;
import ac.soton.eventb.emf.diagrams.generator.impl.GeneratorFactory;
import ac.soton.eventb.emf.diagrams.generator.impl.Messages;

/**
 * Generate action handler.
 * 
 * @author cfs
 *
 */
public class GenerateAllAction extends AbstractHandler {

	List<EventBElement> generateList = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
		if (editor instanceof DiagramDocumentEditor) {
			final DiagramDocumentEditor diagramDocumentEditor = (DiagramDocumentEditor)editor;
			if (editor.isDirty()) editor.doSave(new NullProgressMonitor());	 // save before transformation
			if (ValidateAction.validate(diagramDocumentEditor)) {			//do not proceed if validation of current diagram fails
				EObject element = diagramDocumentEditor.getDiagram().getElement();
				if (element instanceof EventBElement){
					EventBObject component = ((EventBElement)element).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
					generateList = new ArrayList<EventBElement>();
					getDiagramRoots(component, null);
					for (EventBElement eventBElement : generateList){
						if (validateElement(eventBElement, editor.getSite().getShell())){ //validate this element
							final GenerateCommand generateCommand = new GenerateCommand(
									diagramDocumentEditor.getDiagramEditPart().getEditingDomain(), 
									eventBElement); 
							if (generateCommand.canExecute()) {	
								// run with progress
								ProgressMonitorDialog dialog = new ProgressMonitorDialog(diagramDocumentEditor.getSite().getShell());
								try {
									dialog.run(false, true, new IRunnableWithProgress(){
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
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean validateElement(EventBElement eventBElement, Shell shell){
	    Diagnostic diagnostic = Diagnostician.INSTANCE.validate(eventBElement);
	    if (diagnostic.getSeverity() == Diagnostic.ERROR || diagnostic.getSeverity() == Diagnostic.WARNING){
	      MessageDialog.openError(shell,
	    		  Messages.GENERATOR_MSG_12(eventBElement), 
	    		  Messages.GENERATOR_MSG_17(eventBElement));
	      return false;
	    }
	    return true;
	  }
	  
	  
	/* This recursively traverses the contents tree looking for the root diagram elements that can be generated
	 * Note that generate-able elements may be nested in others and not these should not be generated unless 
	 * there is an intermediate generate-able element of another type
	 */
	private void getDiagramRoots(EObject element, EClass lastType) {
		if (element instanceof EventBElement && element.eClass()!=lastType && GeneratorFactory.getFactory().canGenerate(element.eClass())){
			generateList.add((EventBElement)element);
			lastType = element.eClass();
		}
		for (EObject eObject : element.eContents()){
			getDiagramRoots(eObject, lastType);
		}
	
	}
}
