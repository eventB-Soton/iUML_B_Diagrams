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
			for (final IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(Identifiers.EXTPT_VALIDATOR_ID).getExtensions()) {
				for (final IConfigurationElement validatorExtensionElement : extension.getConfigurationElements()) {
					//String eID = validatorExtensionElement.getAttribute(Identifiers.EXTPT_VALIDATORID);
					try {
						edClass = (Class<DiagramDocumentEditor>) validatorExtensionElement.createExecutableExtension(Identifiers.EXTPT_VALIDATOR_EDITORCLASS).getClass();
						v = (IValidator) validatorExtensionElement.createExecutableExtension(Identifiers.EXTPT_VALIDATOR_VALIDATORCLASS);
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
