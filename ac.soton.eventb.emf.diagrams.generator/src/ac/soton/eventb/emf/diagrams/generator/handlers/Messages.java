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
package ac.soton.eventb.emf.diagrams.generator.handlers;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.osgi.util.NLS;

/**
 * @since 4.0
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ac.soton.eventb.emf.diagrams.generator.handlers.messages"; //$NON-NLS-1$
	public static String TRANSLATOR_MSG_00;
	public static String TRANSLATOR_MSG_01;
	public static String TRANSLATOR_MSG_01(Object object){
		return bind(TRANSLATOR_MSG_01,object);
	}
	public static String TRANSLATOR_MSG_02;
	public static String TRANSLATOR_MSG_02(Object object){
		return bind(TRANSLATOR_MSG_02,object);
	}
//	public static String TRANSLATOR_MSG_03;
	public static String TRANSLATOR_MSG_04;
	public static String TRANSLATOR_MSG_05;
	public static String TRANSLATOR_MSG_06;
	public static String TRANSLATOR_MSG_07;
	public static String TRANSLATOR_MSG_08;
	public static String TRANSLATOR_MSG_09;
	public static String TRANSLATOR_MSG_10;
	public static String TRANSLATOR_MSG_11;
//	public static String TRANSLATOR_MSG_12;
//	public static String TRANSLATOR_MSG_12(EventBElement element){
//		return bind(TRANSLATOR_MSG_12,
//				element.eClass().getName(),
//				element instanceof EventBNamed ? ((EventBNamed)element).getName() : element.toString()
//				);
//	}
	public static String TRANSLATOR_MSG_13;
	public static String TRANSLATOR_MSG_13(String type){
		return bind(TRANSLATOR_MSG_13, type);
	}
	public static String TRANSLATOR_MSG_14;
	public static String TRANSLATOR_MSG_15;
	public static String TRANSLATOR_MSG_16;
//	public static String TRANSLATOR_MSG_17;
//	public static String TRANSLATOR_MSG_17(EventBElement element){
//		return bind(TRANSLATOR_MSG_17,
//				element.eClass().getName(),
//				element instanceof EventBNamed ? ((EventBNamed)element).getName() : element.toString()
//				);
//	}
	public static String TRANSLATOR_MSG_18;
	public static String TRANSLATOR_MSG_18(Object object){
		return bind(TRANSLATOR_MSG_18,object);
	}
	public static String TRANSLATOR_MSG_19;
//	public static String TRANSLATOR_MSG_20;
	public static String TRANSLATOR_MSG_21;
	public static String TRANSLATOR_MSG_21(Object object, EStructuralFeature feature){
		return bind(TRANSLATOR_MSG_21,
				object,
				feature.getName()
				);
	}
	public static String TRANSLATOR_MSG_22;
	public static String TRANSLATOR_MSG_22(Object parent, EStructuralFeature feature){
		return bind(TRANSLATOR_MSG_22,
				parent.toString(),
				feature == null? "<null>" : feature.getName()
				);
	}
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
