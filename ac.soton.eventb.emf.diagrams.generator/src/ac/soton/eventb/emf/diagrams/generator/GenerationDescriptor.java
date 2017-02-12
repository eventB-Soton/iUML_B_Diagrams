/**
 * Copyright (c) 2012 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.generator;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eventb.emf.core.EventBElement;


	/**
	 * A Descriptor for generated model changes. 
	 * The feature of the parent will be changed in the following ways:
	 * 
	 * 	If remove is false:
	 * 1) If the feature is a containment and the value is an element of the correct kind, the 
	 *    value will be added to the containment in a position according to the priority
	 * 2) If the feature is a reference and the value is an element of the correct kind, the 
	 *    value will be added to the reference in a position according to the priority
	 * 3) If the feature is an EAttribute and the value is of the correct type, the 
	 *    feature will be set to the value
	 *    
	 *    priority can be used to control the relative position of the generated elements  
	 *    1 - must come first
	 *    10 - not important
	 *    ---user entered items---
	 *    0 must come after user entered items
	 *    -10 must come last
	 *    
	 *    editable - this affects read-only status of the generated element and whether or not it will be
	 *    preserved or re-generated in a subsequent generation.
	 *    	false - the element is set as read-only (the user cannot change its attributes nor add/remove children),
	 *    			any existing copy of the element will be deleted and re-generated at each re-generation
	 *    	true  - the element is not read-only and will not be deleted before re-generation. 
	 *    			(N.B It is the responsibility of the client rules to check whether this element already exists and 
	 *    				only only generate a new one if it does not)
	 *    
	 *  If remove is true:
	 * 1) If the feature is a containment and the value is an element of the correct kind, the 
	 *    value will be deleted from the containment
	 * 2) If the feature is a reference and the value is an element of the correct kind, the 
	 *    value will be removed from the reference
	 * 3) If the feature is an EAttribute, the 
	 *    feature will be unset 
	 *  
	 *    
	 * @author cfs
	 *
	 */
public class GenerationDescriptor{
	public EventBElement parent;
	public EStructuralFeature feature;
	public Object value;
	public Integer priority;
	public Boolean editable;
	public Boolean remove;
	
	public GenerationDescriptor(EventBElement parent, EStructuralFeature feature, Object value, Integer priority, Boolean editable){
		this.parent = parent; this.feature = feature; this.value = value; this.priority = priority; this.editable = editable; this.remove = false;
	}
	
	//remove
	public GenerationDescriptor(EventBElement parent, EStructuralFeature feature, Object value, Boolean remove){
		this.parent = parent; this.feature = feature; this.value = value; this.priority = 1; this.editable = false; this.remove = remove;		
	}
	
}
