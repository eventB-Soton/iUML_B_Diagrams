/*******************************************************************************
 * Copyright (c) 2014, 2020 University of Southampton.
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
import org.eventb.emf.core.EventBNamed;

import ac.soton.eventb.emf.core.extension.coreextension.EventBDataElaboration;
import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

/**
 * Name property section for EventBNamed.
 * 
 *
 */
public class NamePropertySection extends AbstractTextPropertySection {

	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBNamed;
		}
	}
	
	@Override
	protected String getPropertyNameLabel() {
		return "Name:";
	}
	
	@Override
	protected void setPropertyValue(EObject object, Object value) {
		assert object instanceof EventBNamed;
		((EventBNamed) object).setName((String) value);
	}

	@Override
	protected String getPropertyValueString() {
		if (!(getEObject() instanceof EventBNamed)) {
			return getEObject().eClass().toString();
		}
		return ((EventBNamed) getEObject()).getName();
	}

	@Override
	protected String getPropertyChangeCommandName() {
		return "change name";
	}
	
	@Override
	public boolean isReadOnly() {
		return super.isReadOnly() || 
			(eObject instanceof EventBDataElaboration && ((EventBDataElaboration) eObject).getElaborates() != null);
	}
}
