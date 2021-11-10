/*******************************************************************************
 * Copyright (c) 2014, 2017 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.generator.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ac.soton.eventb.emf.diagrams.generator.impl.ValidatorRegistry;

/**
 * This provides a generic action for validating diagram extensions.
 *  It is a wrapper for the GMF Validate Action so that we can display messages about the validation.
 * @since 4.0
 */
public class IUMLBValidateHandler extends AbstractHandler {
	
	/**
	 * 
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
		if (editor instanceof DiagramDocumentEditor) {
			if (validate((DiagramDocumentEditor)editor)){
				MessageDialog.openInformation(editor.getSite().getShell(),
						"Validation Information",
						"Validation completed successfully with no errors found");;
			}
		}
		return null;
	}
	
	/**
	 * This provides a method for programmatic invocation of the validator.
	 * In this case, no feedback is given for successful, error free completion.
	 * 
	 * @param diagramDocumentEditor
	 * @return
	 * @throws ExecutionException
	 */
	public static boolean validate(DiagramDocumentEditor diagramDocumentEditor) throws ExecutionException{
		boolean result = ValidatorRegistry.validate(diagramDocumentEditor);
		if (result==false){
			// didn't validate so show feedback
			String errors = ValidatorRegistry.getValidationErrors(diagramDocumentEditor);
			MessageDialog.openError(diagramDocumentEditor.getSite().getShell(),
					"Validation Information",
					"Validation discovered the following problems in the model:\n"
							+ errors);
		}
		return result;
	}

}
