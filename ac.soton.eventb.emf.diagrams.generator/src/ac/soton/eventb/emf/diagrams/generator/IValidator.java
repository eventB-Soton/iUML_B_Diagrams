/**
 * Copyright (c) 2012, 2015 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 	University of Southampton - Initial implementation
 *  
 */
package ac.soton.eventb.emf.diagrams.generator;

import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;


/**
 * Clients should implement this interface to define Validators
 * 
 * [Note that GMF can generate a Validation Action automatically so that an implementation of this interface merely acts as a wrapper.
 * If GMF is used to generate a  Validation Action,  a marker provider class is also generated. Then getMarkerType() should return 
 * the Marker type defined in the GMF marker provider.]
 * 
 * @author cfs
 *
 */

public interface IValidator {
	
	/**
	 * Execute the validation for the given editor (which should be currently active).
	 * 
	 * @param diagramDocumentEditor
	 * @return true if validation completes without detecting errors
	 */
	public boolean validate (DiagramDocumentEditor diagramDocumentEditor) throws Exception;
	
	/**
	 * Returns a string describing the validation errors currently detected in the resource currently open by the editor
	 * 
 	 * @param diagramDocumentEditor
	 * @return String describing validation errors
	 */
	public String getValidationErrors(DiagramDocumentEditor diagramDocumentEditor);
	
}