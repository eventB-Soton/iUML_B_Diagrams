/**
 * Copyright (c) 2014 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.common.core.util.StringStatics;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.AbstractBasicTextPropertySection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

/**
 * General text property section, used for string properties of an object.
 * 
 *
 */
public abstract class AbstractTextPropertySection extends
		AbstractBasicTextPropertySection {

	@Override
	protected EObject unwrap(Object object) {
		return DiagramUtils.unwrap(object);
	}
	
	@Override
	protected Text createTextWidget(Composite parent) {
		Text text;
		if (numberOfRows()>1){
			text = getWidgetFactory().createText(parent, StringStatics.BLANK, SWT.MULTI); //$NON-NLS-1$
		}else{
			text = getWidgetFactory().createText(parent, StringStatics.BLANK); //$NON-NLS-1$			
		}
		FormData data = new FormData();
		data.left = new FormAttachment(0, getPropertyLabelWidth(parent));
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		GC gc = new GC(text);
		int height = gc.getFontMetrics().getHeight();
		data.height = numberOfRows()*height;
		text.setLayoutData(data);
		if (isReadOnly())
			text.setEditable(false);
		gc.dispose();
		return text;
	}

	
	/**
	 * Returns width of property label.
	 * Standard implementation uses label text width to calculate the width.
	 * Subclasses may overrride.
	 * 
	 * @param parent
	 * @return
	 */
	private int getPropertyLabelWidth(Composite parent) {
		return 100;
	}

	/**
	 * Override to return more than 1 for a multi line text widget
	 * @return
	 */
	protected int numberOfRows(){
		return 1;
	}
}