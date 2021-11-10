/*******************************************************************************
 * Copyright (c) 2014, 2020 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.util.custom;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.notation.View;
import org.eventb.emf.core.Annotation;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBObject;

import ac.soton.eventb.emf.diagrams.DiagramsPackage;
import ac.soton.eventb.emf.diagrams.UMLB;

public class DiagramUtils {

	/**
	 * Unwraps eobject from passed diagram object.
	 * 
	 * @param object
	 * @return
	 */
	public static EObject unwrap(Object object) {

		if (object instanceof EditPart) {
			Object model = ((EditPart) object).getModel();
			return model instanceof View ? ((View) model).getElement() : null;
		}
		if (object instanceof View) {
			return ((View) object).getElement();
		}
		if (object instanceof IAdaptable) {
			View view = (View) ((IAdaptable) object).getAdapter(View.class);
			if (view != null) {
				return view.getElement();
			}
		}
		if (object instanceof EObject)
			return (EObject) object;
		return null;
	}
	
	/**
	 * Convenience util method to get the value of the named feature from the model element represented by an edit part.
	 * This can be used in customised gmf code where a model value needs to be examined
	 * e.g. when called from an editpart, DiagramUtils.getModelFeatureValue(this, "elaborates");
	 * 
	 * @param editpart
	 * @param featureName
	 * @return
	 */
	public static Object getModelFeatureValue(EditPart editpart, String featureName){
		EObject element =  DiagramUtils.unwrap(editpart.getModel());
		EStructuralFeature feature = element.eClass().getEStructuralFeature(featureName);
		return feature==null? null : getModelFeatureValue(editpart,feature);
	}

	/**
	 * Convenience util method to get the value of the given feature from the model element represented by an edit part.
	 * This can be used in customised gmf code where a model value needs to be examined
	 * e.g. when called from an editpart, DiagramUtils.getModelFeatureValue(this, "elaborates");
	 * 
	 * @param editpart
	 * @param featureName
	 * @return
	 */
	public static Object getModelFeatureValue(EditPart editpart, EStructuralFeature feature){
		EObject element =  DiagramUtils.unwrap(editpart.getModel());
		return element==null? null : element.eGet(feature);
	}
	
	private static final String DIAGRAMS_TRANSLATION_TARGET = "ac.soton.diagrams.translationTarget";

	/**
	 * This gets the Event-B component to be used as the target for translation.
	 * This component can be used to select Event-B elements to reference.
	 * 
	 * If the 'owner' is contained in a project, the project is returned
	 * Otherwise, If the 'owner' is contained in a Event-B component, the component is returned
	 * Otherwise, If the 'owner' is contained in an UML-B the elaborated component (of the UML-B) is return
	 * Otherwise, If the 'owner' has an "ac.soton.diagrams.translationTarget" annotation that references an EventBObject,
	 * 	that referenced object is returned.
	 * 
	 * @return
	 * @since 3.0
	 */
	public static EventBObject getTranslationTarget(EventBObject owner) {
		EventBObject container = owner.getContaining(CorePackage.Literals.PROJECT);
		// this is the old way when diagrams are contained inside the target machine/context
		if (container == null){
			container = owner.getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
		}
		//this is the new way when diagrams are contained in a UMLB diagram owner element
		if (container == null){
			EventBObject umlb = owner.getContaining(DiagramsPackage.Literals.UMLB);
			if (umlb instanceof UMLB) {
				container = ((UMLB) umlb).getElaborates();
			}
		}
		//this is another new way that we tried - when diagrams have their own resource and an Annotation that references the machine/context
		// (in the long-run we might replace annotation with a dedicated reference in the Diagram element).
		if (container == null) {
			EObject root = EcoreUtil.getRootContainer(owner);
			if (root instanceof EventBElement) {
				Annotation annotation = ((EventBElement)root).getAnnotation(DIAGRAMS_TRANSLATION_TARGET);
				if (annotation!=null){
					EList<EObject> references = annotation.getReferences();
					if (!references.isEmpty() && references.get(0) instanceof EventBObject) {
						container = (EventBObject) references.get(0);
					}
				}
			}
		}
		return container;
	}
}
