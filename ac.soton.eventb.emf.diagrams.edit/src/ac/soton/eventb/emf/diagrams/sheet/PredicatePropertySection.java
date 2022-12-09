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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IFilter;
import org.eventb.emf.core.EventBNamedCommentedDerivedPredicateElement;
import org.eventb.emf.core.EventBPredicate;

import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

/**
 * Predicate property section for EventBPredicate.
 * 
 * @author vitaly
 *
 */
public class PredicatePropertySection extends AbstractTextPropertySection {

	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBPredicate;
		}
	}
	
	
	@Override
	protected String getPropertyNameLabel() {
		return "Predicate:";
	}

	@Override
	protected void setPropertyValue(EObject object, Object value) {
		((EventBNamedCommentedDerivedPredicateElement) object).setPredicate((String) value);
	}

	@Override
	protected String getPropertyValueString() {
		return ((EventBNamedCommentedDerivedPredicateElement) getEObject()).getPredicate();
	}

	@Override
	protected String getPropertyChangeCommandName() {
		return "change predicate";
	}

	protected boolean isRodinKeyboard(){
		return true;
	}

	@Override
	protected boolean isMultiLine() {
		return true;
	}
}
