package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.IFilter;

import ac.soton.eventb.emf.core.extension.coreextension.CoreextensionPackage;
import ac.soton.eventb.emf.core.extension.coreextension.EventBRelationKind;
import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

public class EventBRelationKindSurjectiveSection extends AbstractEnumerationPropertySection {

	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBRelationKind;
		}
	}

	protected Object getFeatureByValue(Object value) {
		return (Boolean)value;
	}

	@Override
	protected boolean isEqual(String selection) {
		return Boolean.toString(((EventBRelationKind)(eObject)).isSurjective()).equals(selection);
	}

	@Override
	protected String[] getEnumerationFeatureValues() {
		return new String[]{"true", "false"};
	}

	@Override
	protected String getFeatureAsText() {
		return Boolean.toString(((EventBRelationKind)(eObject)).isSurjective());
	}
	
	@Override
	protected Object getFeatureValue(String selection) {
		return Boolean.valueOf(selection);
	}

	@Override
	protected String getLabelText() {
		return "Surjective:";
	}

	@Override
	protected EStructuralFeature getFeature() {
		return CoreextensionPackage.Literals.EVENT_BRELATION_KIND__SURJECTIVE;
	}

}
