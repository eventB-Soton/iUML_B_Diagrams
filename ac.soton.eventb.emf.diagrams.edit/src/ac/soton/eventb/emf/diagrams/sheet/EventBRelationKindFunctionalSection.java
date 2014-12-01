package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.IFilter;

import ac.soton.eventb.emf.core.extension.coreextension.CoreextensionPackage;
import ac.soton.eventb.emf.core.extension.coreextension.EventBRelationKind;
import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

public class EventBRelationKindFunctionalSection extends AbstractBooleanEnumerationPropertySection {

	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBRelationKind;
		}
	}

	@Override
	protected String getFeatureAsText() {
		return Boolean.toString(((EventBRelationKind)(eObject)).isFunctional());
	}

	@Override
	protected String getLabelText() {
		return "Functional:";
	}

	@Override
	protected EStructuralFeature getFeature() {
		return CoreextensionPackage.Literals.EVENT_BRELATION_KIND__FUNCTIONAL;
	}
}
