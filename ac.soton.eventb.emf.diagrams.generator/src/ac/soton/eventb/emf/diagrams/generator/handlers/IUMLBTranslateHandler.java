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
/**
 * 
 */
package ac.soton.eventb.emf.diagrams.generator.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.emf.persistence.EMFRodinDB;
import org.rodinp.core.IInternalElement;

import ac.soton.emf.translator.Activator;
import ac.soton.emf.translator.TranslatorFactory;
import ac.soton.emf.translator.eventb.handler.EventBTranslateHandler;
import ac.soton.eventb.emf.diagrams.generator.DiagramsGeneratorIdentifiers;
import ac.soton.eventb.emf.diagrams.generator.impl.ValidatorRegistry;


/**
 * <p>
 * 
 * </p>
 * 
 * @author cfs
 * @version
 * @see
 * @since 4.0
 */
public class IUMLBTranslateHandler extends EventBTranslateHandler {	
	
	/**
	 * Override getEObject to allow for the selection of rodin elements
	 * as well as EMF ones.
	 * Also look up the containment hierarchy to find the root of the 
	 * selected element that is within the same translator. I.e.
	 * First find the nearest parent that can be translated and then
	 * find a parent of the parent that has the same type.
	 * 
	 * @param Object - selected object
	 * @return EObject - object to be translated
	 */
	@Override
	protected EObject getEObject (Object obj){
		
		EObject eObject = null;
		if (obj instanceof IInternalElement){
			eObject = (new EMFRodinDB()).loadEventBComponent((IInternalElement)obj) ;
		}else{
			eObject =  super.getEObject(obj);
		}
		
		EObject	next; 
		while ((next=getTranslatable(eObject))!=null && next.eClass()==eObject.eClass()){
			eObject = next;
		}
		
		return eObject;
	}
	
	/**
	 * get the next containing translatable element 
	 * or null if there is no containing translatable element
	 * 
	 * @param eObject
	 * @return translatable parent or null
	 */
	protected EObject getTranslatable(EObject eObject){
		EObject ret = eObject.eContainer();
		
		try {
			while (ret!= null && !TranslatorFactory.getFactory().canTranslate(DiagramsGeneratorIdentifiers.COMMAND_ID, ret.eClass())){
				ret = ret.eContainer();
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ret;
	}
	
	
	/**
	 * This is overridden to add some validation of the diagram before the translation
	 * 
	 * @param ExecutionEvent
	 * @param monitor
	 * @return status = OK to continue translation, INFO to report validation errors and cancel translation
	 * @throws ExecutionException 
	 */
	protected IStatus validate(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {

		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
		IStatus status = Status.CANCEL_STATUS;

		if (editor instanceof DiagramDocumentEditor) {
			final DiagramDocumentEditor diagramDocumentEditor = (DiagramDocumentEditor)editor;
				
				if (ValidatorRegistry.validate(diagramDocumentEditor)){
					status = Status.OK_STATUS;
				}else{
					// didn't validate so show feedback
					String errors = ValidatorRegistry.getValidationErrors(diagramDocumentEditor);
					status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, ValidationFailedMessage+errors );
				}
		}
        monitor.done();
        return status;
	}
				
	final static String ValidationFailedMessage = "Translation cancelled because validation failed with the following errors : \n";
}
