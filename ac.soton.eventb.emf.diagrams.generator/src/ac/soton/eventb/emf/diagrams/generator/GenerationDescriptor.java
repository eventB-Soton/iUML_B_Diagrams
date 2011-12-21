package ac.soton.eventb.emf.diagrams.generator;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eventb.emf.core.EventBElement;


	/**
	 * A Descriptor for generated model changes. 
	 * The feature of the parent will be changed in the following ways:
	 * 1) If the feature is a containment and the value is an element of the correct kind, the 
	 *    value will be added to the containment in a position according to the priority
	 * 2) If the feature is a reference and the value is an element of the correct kind, the 
	 *    value will be added to the reference in a position according to the priority
	 * 3) If the feature is an EAttribute and the value is of the correct type, the 
	 *    feature will be set to the value
	 *    
	 * @author cfs
	 *
	 */
public class GenerationDescriptor{
	public EventBElement parent;
	public EStructuralFeature feature;
	public Object value;
	public Integer priority;
	
	public GenerationDescriptor(EventBElement parent, EStructuralFeature feature, Object value, Integer priority){
		this.parent = parent; this.feature = feature; this.value = value; this.priority = priority;
	}
	
	public GenerationDescriptor(EventBElement parent, EStructuralFeature feature, Object value){
		this.parent = parent; this.feature = feature; this.value = value; this.priority = 0;
	}

	
}
