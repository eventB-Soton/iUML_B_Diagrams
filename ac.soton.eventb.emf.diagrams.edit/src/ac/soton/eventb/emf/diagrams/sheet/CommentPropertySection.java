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
import org.eventb.emf.core.EventBCommented;
import org.eventb.emf.core.EventBCommentedElement;

import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

/**
 * Comment property section for EventBCommented.
 * 
 * @author vitaly
 *
 */
public class CommentPropertySection extends AbstractTextPropertySection {

	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBCommentedElement;
		}
	}
	
	@Override
	protected String getPropertyNameLabel() {
		return "Comment:";
	}

	@Override
	protected void setPropertyValue(EObject object, Object value) {
		assert object instanceof EventBCommented;
		((EventBCommented) object).setComment((String) value);
	}

	@Override
	protected String getPropertyValueString() {
		String comment = ((EventBCommented) getEObject()).getComment();
		return comment == null ? "" : comment;
	}

	@Override
	protected String getPropertyChangeCommandName() {
		return "change comment";
	}

	@Override
	protected boolean isMultiLine() { return true;}
}
