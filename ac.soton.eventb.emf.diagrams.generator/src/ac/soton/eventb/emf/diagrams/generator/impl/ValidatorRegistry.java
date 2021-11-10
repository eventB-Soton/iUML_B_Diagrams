/*******************************************************************************
 * Copyright (c) 2014, 2018 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.generator.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;

import ac.soton.eventb.emf.diagrams.generator.IValidator;



public class ValidatorRegistry {

	/**
	 * Registry holding declared Diagram Validators
	 */
	
	//cached store of validators that have been loaded from extension points
	private static Map<Class<DiagramDocumentEditor>,IValidator> validators = null;

	@SuppressWarnings("unchecked")
	private static IValidator getValidator(DiagramDocumentEditor editor){
		Class<DiagramDocumentEditor> editorClass = (Class<DiagramDocumentEditor>) editor.getClass();
		if (validators == null){
			validators = new HashMap<Class<DiagramDocumentEditor>, IValidator>();
			// populate validators from registered extensions
			Class<DiagramDocumentEditor> edClass;
			IValidator v;
			for (final IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(ValidatorExtPtIdentifiers.EXTPT_VALIDATOR_ID).getExtensions()) {
				for (final IConfigurationElement validatorExtensionElement : extension.getConfigurationElements()) {
					try {
						edClass = (Class<DiagramDocumentEditor>) validatorExtensionElement.createExecutableExtension(ValidatorExtPtIdentifiers.EXTPT_VALIDATOR_EDITORCLASS).getClass();
						v = (IValidator) validatorExtensionElement.createExecutableExtension(ValidatorExtPtIdentifiers.EXTPT_VALIDATOR_VALIDATORCLASS);
						validators.put(edClass, v);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}		
		}
		
		if (validators.containsKey(editorClass)){
			return validators.get(editorClass);
		}else{
			return null;
		}
		
	}
	
	
	/**
	 * The Registry provides the following static interface for convenience
	 * These methods can be called by passing the appropriate DiagramDocumentEditor editor.
	 * 
	 */
	
/**
 * 
 */
	public static boolean hasValidator(DiagramDocumentEditor diagramDocumentEditor){
		return getValidator(diagramDocumentEditor) != null;
	}
	
/**
 * 	
 * @param editor
 * @return
 */
	public static boolean validate(DiagramDocumentEditor diagramDocumentEditor){
		IValidator validator = getValidator(diagramDocumentEditor);
		if (validator ==null) return false;
		try {
			return validator.validate(diagramDocumentEditor);
		}catch(Exception e){
			return false;
		}
	}

	/**
	 * 
	 * @param diagramDocumentEditor
	 * @return
	 */
	public static String getValidationErrors(DiagramDocumentEditor diagramDocumentEditor) {
		IValidator validator = getValidator(diagramDocumentEditor);
		try {
			return validator.getValidationErrors(diagramDocumentEditor);
		}catch(Exception e){
			return null;
		}
	}

}
