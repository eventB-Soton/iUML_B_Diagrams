/*
 * Copyright (c) 2014 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
