/*******************************************************************************
 * Copyright (c) 2014, 2017 University of Southampton.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    University of Southampton - initial API and implementation
 *******************************************************************************/

package ac.soton.eventb.emf.diagrams.generator.utils;


import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.Attribute;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBObject;
import ac.soton.emf.translator.configuration.AttributeIdentifiers;



/**
 * Convenience methods for testing things in Generator Rules
 * 
 * @author cfs
 *
 */
public class Is {
	
	/**
	 * @since 4.0
	 */
	public static String generatedById(AbstractExtension sourceElement){
		return sourceElement.getExtensionId();
	}

	public static boolean generatedBy(Object object, Object sourceElement){
		if (sourceElement instanceof EventBObject){
			AbstractExtension ae = (AbstractExtension) ((EventBObject) sourceElement).getContaining(CorePackage.Literals.ABSTRACT_EXTENSION);
			if (ae instanceof AbstractExtension){
				return generatedBy(object, generatedById(ae));
			}
		}
		return false;
	}
	
	public static boolean generatedBy(Object object, String id){
		if (object instanceof EventBElement){
			Attribute generatedBy = ((EventBElement)object).getAttributes().get(AttributeIdentifiers.TRANSLATOR__TRANSLATION_ID_KEY);
			if (generatedBy!= null && id.equals(generatedBy.getValue()) ){
				return true;
			}
		}
		return false;
	}
	
	public static boolean generated(Object object){
		if (object instanceof EventBElement){
			Attribute generatedBy = ((EventBElement)object).getAttributes().get(AttributeIdentifiers.TRANSLATOR__TRANSLATION_ID_KEY);
			if (generatedBy!= null){
				return true;
			}
		}
		return false;
	}
	
	public static boolean readOnly(Object object){
		if (object instanceof EventBElement){
			return ((EventBElement)object).isLocalGenerated();
		}
		return false;
	}
	
}
