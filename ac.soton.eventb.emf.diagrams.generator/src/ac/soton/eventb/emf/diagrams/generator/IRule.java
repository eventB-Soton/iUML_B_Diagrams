package ac.soton.eventb.emf.diagrams.generator;

import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;


public interface IRule {
	
	/**
	 * This should return a boolean to indicate whether or not the rule should be used for the given source element
	 * 
	 * @param sourceElement
	 * @param generatedElements			(already generated so far)
	 * @return
	 */
	public boolean enabled (final EventBElement sourceElement, final List<GenerationDescriptor> generatedElements);
	
	/**
	 * This does the generation from the given source element producing a List of generated elements.
	 * If there are no generated elements and empty list should be returned. Do not return null.
	 * 
	 * [Note that The generator will set generated properties and generator ID attributes for all generated elements. It
	 * is not necessary for rules to do this].
	 * 
	 * @param sourceElement
	 * @return
	 */
	public List<GenerationDescriptor> fire(EventBElement sourceElement);
	
	/**
	 * This returns the EventB component (i.e. a particular Machine or Context etc.) where the generated elements
	 * should be added.
	 * 
	 * @param sourceComponent
	 * @param sourceElement
	 * @return
	 */
	public EventBNamedCommentedComponentElement getTargetEventBComponent(EventBNamedCommentedComponentElement sourceComponent, EventBElement sourceElement);

	
}
