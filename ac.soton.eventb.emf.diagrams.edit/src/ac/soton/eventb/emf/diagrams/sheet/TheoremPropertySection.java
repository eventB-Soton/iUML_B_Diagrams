/*******************************************************************************
 * Copyright (c) 2014 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.IFilter;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBDerived;

import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

/**
 * Theorem property section for EventBDerived.
 * 
 *
 */
public class TheoremPropertySection extends AbstractBooleanEnumerationPropertySection {

	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBDerived;
		}
	}

	@Override
	protected String getFeatureAsText() {
		return Boolean.toString(((EventBDerived) eObject).isTheorem());
	}

	@Override
	protected String getLabelText() {
		return "Theorem:";
	}

	@Override
	protected EStructuralFeature getFeature() {
		return CorePackage.Literals.EVENT_BDERIVED__THEOREM;
	}


}
