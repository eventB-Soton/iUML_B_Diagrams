/**
 * Copyright (c) 2012 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.generator;

import java.util.List;

import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;

/**
 * Clients should implement this interface to define Rules for their generator
 * 
 * @author cfs
 *
 */

public interface IRule {
	
	/**
	 * This should return a boolean to indicate whether or not the rule is applicable for the given source element
	 * 
	 * @param sourceElement
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
	 * CURRENTLY NOT USED
	 * 
	 * @param sourceComponent
	 * @param sourceElement
	 * @return
	 */
	public EventBNamedCommentedComponentElement getTargetEventBComponent(EventBNamedCommentedComponentElement sourceComponent, EventBElement sourceElement) throws Exception;

	/**
	 * This should return a boolean to indicate whether dependencies needed by this source Element/Rule combination
	 * have already been generated. If not the rule will be tried again later.
	 * N.b. Do not include conditions here, only implement ordering dependencies which will at some point be true.
	 * If the dependency does not become true during the generation, the generation has failed and is not saved. 
	 * 
	 * @param sourceElement
	 * @param generatedElements
	 * @return
	 * @throws Exception
	 */
	boolean dependenciesOK(EventBElement sourceElement,List<GenerationDescriptor> generatedElements) throws Exception;

	/**
	 * This should return a boolean to indicate whether the rule should be fired late.
	 * Late rules will not be fired until no progress is being made firing non-late rules.
	 * Late rules are usually used when some generation of elements is conditional on whether other rules have generated elements.
	 * (This is different from dependencies which is un-conditional but requires order)
	 * 
	 */
	boolean fireLate();
	
}
