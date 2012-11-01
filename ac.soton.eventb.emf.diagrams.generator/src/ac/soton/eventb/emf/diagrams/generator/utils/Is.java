/**
 * Copyright (c) 2012 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.generator.utils;

import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eventb.emf.core.EventBElement;

import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;

/**
 * Convenience methods for testing things in Generator Rules
 * 
 * @author cfs
 *
 */
public class Is {
	
	/**
	 * Convenience method to check whether an element has been generated already in the list of generationDescriptors
	 * (parent and/or feature may be null if not required to be matched)
	 * 
	 * @param generatedElements
	 * @param parent (or null)
	 * @param feature (or null)
	 * @param identifier
	 * @return
	 */
	public static boolean generated(List<GenerationDescriptor> generatedElements, EventBElement parent, EStructuralFeature feature, String identifier){
		return Find.generatedElement(generatedElements, parent, feature, identifier) != null;
	}
}
