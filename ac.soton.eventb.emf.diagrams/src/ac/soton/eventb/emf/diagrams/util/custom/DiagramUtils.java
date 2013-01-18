package ac.soton.eventb.emf.diagrams.util.custom;

import org.eclipse.core.runtime.IAdaptable;
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
		return getModelFeatureValue(editpart,feature);
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
	
}
