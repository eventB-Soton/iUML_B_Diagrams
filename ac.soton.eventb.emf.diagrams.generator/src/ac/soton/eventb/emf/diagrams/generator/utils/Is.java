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
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.Attribute;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBObject;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.impl.Identifiers;

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
	

	public static boolean generatedBy(Object object, Object sourceElement){
		if (sourceElement instanceof EventBObject){
			AbstractExtension ae = (AbstractExtension) ((EventBObject) sourceElement).getContaining(CorePackage.Literals.ABSTRACT_EXTENSION);
			if (ae instanceof AbstractExtension){
				return generatedBy(object, Make.generatedById(ae));
			}
		}
		return false;
	}
	
	public static boolean generatedBy(Object object, String id){
		if (object instanceof EventBElement){
			Attribute generatedBy = ((EventBElement)object).getAttributes().get(Identifiers.GENERATOR_ID_KEY);
			if (generatedBy!= null && id.equals(generatedBy.getValue()) ){
				return true;
			}
		}
		return false;
	}
}
