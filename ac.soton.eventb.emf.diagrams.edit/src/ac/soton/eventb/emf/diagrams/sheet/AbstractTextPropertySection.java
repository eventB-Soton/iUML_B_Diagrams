/**
 * Copyright (c) 2014 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.gmf.runtime.common.core.util.StringStatics;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.AbstractBasicTextPropertySection;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.rodinp.keyboard.ui.RodinKeyboardUIPlugin;
import org.rodinp.keyboard.ui.preferences.PreferenceConstants;

/**
 * General text property section, used for string properties of an object.
 * 
 *
 */
public abstract class AbstractTextPropertySection extends
		AbstractBasicTextPropertySection {

	final int TEXT_MARGIN = 3;
	
	@Override
	protected Text createTextWidget(Composite parent) {
		Text text;
		if (isMultiLine()){
			text = getWidgetFactory().createText(parent, StringStatics.BLANK, SWT.MULTI); //$NON-NLS-1$
		}else{
			text = getWidgetFactory().createText(parent, StringStatics.BLANK); //$NON-NLS-1$			
		}
		FormData data = new FormData();
		data.left = new FormAttachment(0, getPropertyLabelWidth(parent));
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		text.setLayoutData(data);
		text.setFont(JFaceResources.getFont(PreferenceConstants.RODIN_MATH_FONT));
		if (isRodinKeyboard()) {
			text.addModifyListener(RodinKeyboardUIPlugin.getDefault().createRodinModifyListener());
		}
		if (isReadOnly())
			text.setEditable(false);
		return text;
	}

	/**
	 * Start listening to the text widget events
	 */
	protected void startTextWidgetEventListener() {
		if (!isReadOnly()) {
			getListener().startListeningTo(getTextWidget());
			if (!isMultiLine()){
				getListener().startListeningForEnter(getTextWidget());
			}
		}
	}
	
	@Override
	public void refresh() {
		super.refresh();
		reSizeTextWidget(getTextWidget().getText());
	}
	
	@Override
	protected Object computeNewPropertyValue() {
		reSizeTextWidget(getTextWidget().getText());
		return getTextWidget().getText();
	}
	
	private void reSizeTextWidget(String text){
		if (!isMultiLine()) return;
		Text textWidget = getTextWidget();
		GC gc = new GC(textWidget);
		Point size = gc.textExtent(text);
		textWidget.setSize(textWidget.getSize().x, size.y + 2*TEXT_MARGIN);
		gc.dispose();
	}
	
	/**
	 * By default the text field is NOT treated as a Rodin Keyboard.
	 * This method should be overridden to return true if the Rodin key combo modification is required.
	 * 
	 * @return whether or not to use a Rodin Keyboard modify listener for this text
	 */
	protected boolean isRodinKeyboard(){
		return false;
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
	protected boolean isMultiLine(){
		return false;
	}
}