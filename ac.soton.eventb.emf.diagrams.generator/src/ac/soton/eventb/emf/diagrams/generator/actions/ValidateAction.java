/*
 * Copyright (c) 2010 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.generator.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ac.soton.eventb.emf.diagrams.generator.impl.ValidatorRegistry;

/**
 * This provides a generic action for validating diagram extensions.
 *  It is a wrapper for the GMF Validate Action so that we can display messages about the validation.
 */
public class ValidateAction extends AbstractHandler {

	private boolean enabled = false;
	@Override
	public boolean isEnabled(){
		return enabled && super.isEnabled();
	}

	@Override
	public void setEnabled(Object evaluationContext) {		
		super.setEnabled(evaluationContext);
		enabled = false;
		if (evaluationContext instanceof EvaluationContext){
			EvaluationContext ec = (EvaluationContext)evaluationContext;
			
			Object editor = ec.getVariable("activeEditor");
			//Object editorID = ec.getVariable("activeEditorId");
			if (editor instanceof DiagramDocumentEditor) {
				final DiagramDocumentEditor diagramDocumentEditor = (DiagramDocumentEditor)editor;
				if (ValidatorRegistry.hasValidator(diagramDocumentEditor)){
					enabled = true;
				}
			}
		}	
	}
	
	/**
	 * 
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
		if (editor instanceof DiagramDocumentEditor) {
			try {
				DiagramDocumentEditor diagramDocumentEditor = (DiagramDocumentEditor)editor;
				// run validation and return if ok
				ValidatorRegistry.validate(diagramDocumentEditor);
			
				// didn't validate so show feedback
				String errors = ValidatorRegistry.getValidationErrors(diagramDocumentEditor);
				if (errors.isEmpty())
					MessageDialog.openInformation(editor.getSite().getShell(),
							"Validation Information",
							"Validation completed successfully with no errors found");
				else
					MessageDialog.openError(editor.getSite().getShell(),
							"Validation Information",
							"Validation discovered the following problems in the model:\n"
									+ errors);
			} catch (Exception e) {
				throw new ExecutionException(
						"Validation did not complete:\n", e);
			}
		}

		return null;
	}

}
