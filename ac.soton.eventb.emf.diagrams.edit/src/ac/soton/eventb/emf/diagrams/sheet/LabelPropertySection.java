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
