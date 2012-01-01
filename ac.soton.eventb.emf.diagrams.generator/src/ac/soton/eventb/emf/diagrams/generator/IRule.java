package ac.soton.eventb.emf.diagrams.generator;

import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;


public interface IRule {
	
	/**
	 * This should return a boolean to indicate whether or not the rule is applicable for the given source element
	 * 
	 * @param sourceElement
	 * @param generatedElements			(already generated so far)
	 * @return
	 */
	public boolean enabled (final EventBElement sourceElement) throws Exception;
	
	/**
	 * This does the generation from the given source element producing a List of generated elements.
	 * If there are no generated elements an empty list should be returned. 
	 * Return null only if the rule is fired when dependencies are not ok.
	 * Throw an exception if this rule is fired when it is not enabled.
	 * 
	 * [Note that The generator will set generated properties and generator ID attributes for all generated elements. It
	 * is not necessary for rules to do this].
	 * 
	 * @param sourceElement
	 * @return
	 * @throws Exception 
	 */
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception;
	
	/**
	 * This returns the EventB component (i.e. a particular Machine or Context etc.) where the generated elements
	 * should be added.
	 * 
	 * @param sourceComponent
	 * @param sourceElement
	 * @return
	 */
	public EventBNamedCommentedComponentElement getTargetEventBComponent(EventBNamedCommentedComponentElement sourceComponent, EventBElement sourceElement) throws Exception;

	/**
	 * This should return a boolean to indicate whether dependencies needed by this source Element/Rule combination
	 * have already been generated. If not the rule will be tried again later.
	 * 
	 * @param sourceElement
	 * @param generatedElements
	 * @return
	 * @throws Exception
	 */
	boolean dependenciesOK(EventBElement sourceElement,List<GenerationDescriptor> generatedElements) throws Exception;

	
}
