package ac.soton.eventb.emf.diagrams.util.custom;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.notation.View;

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
	
	public static Object getModelFeatureValue(EditPart editpart, String featureName){
		EObject element =  DiagramUtils.unwrap(editpart.getModel());
		EStructuralFeature feature = element.eClass().getEStructuralFeature(featureName);
		return getModelFeatureValue(editpart,feature);
	}
	
	public static Object getModelFeatureValue(EditPart editpart, EStructuralFeature feature){
		EObject element =  DiagramUtils.unwrap(editpart.getModel());
		EClass ec = element.eClass();
		EList<EStructuralFeature> fs = ec.getEStructuralFeatures();
		return element.eGet(feature);
	}
	
}
