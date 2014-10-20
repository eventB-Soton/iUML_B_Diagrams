/*
 * Copyright (c) 2010 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IFilter;

import ac.soton.eventb.emf.core.extension.coreextension.EventBLabeled;
import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

/**
 * Label property section for EventBLabeled.
 * 
 * @author vitaly
 *
 */
public class LabelPropertySection extends AbstractTextPropertySection {

	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBLabeled;
		}
	}
	
	@Override
	protected String getPropertyNameLabel() {
		return "Label:";
	}

	@Override
	protected void setPropertyValue(EObject object, Object value) {
		// nothing to set
	}

	@Override
	protected String getPropertyValueString() {
		return ((EventBLabeled) getEObject()).getLabel();
	}

	@Override
	protected String getPropertyChangeCommandName() {
		return "change nothing";
	}

	@Override
	protected boolean isReadOnly() {
		return true;
	}

}
