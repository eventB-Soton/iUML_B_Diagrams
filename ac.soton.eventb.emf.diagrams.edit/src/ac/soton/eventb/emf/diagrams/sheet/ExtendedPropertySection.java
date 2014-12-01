/*******************************************************************************
 * Copyright (c) 2014 University of Southampton and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.IFilter;

import ac.soton.eventb.emf.core.extension.coreextension.CoreextensionPackage;
import ac.soton.eventb.emf.core.extension.coreextension.EventBEventGroup;
import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

public class ExtendedPropertySection extends AbstractBooleanEnumerationPropertySection {

	/**
	 * Element Filter for this property section.
	 * @since 2.0
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBEventGroup;
		}
	}

	@Override
	protected String getFeatureAsText() {
		return Boolean.toString(((EventBEventGroup) eObject).isExtended());
	}

	@Override
	protected String getLabelText() {
		return "Extended:";
	}

	@Override
	protected EStructuralFeature getFeature() {
		return CoreextensionPackage.Literals.EVENT_BEVENT_GROUP__EXTENDED;
	}

}
