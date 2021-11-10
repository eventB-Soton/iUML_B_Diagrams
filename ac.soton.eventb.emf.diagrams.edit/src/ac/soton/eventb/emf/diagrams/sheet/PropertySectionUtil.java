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

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Control;
import org.rodinp.keyboard.ui.RodinKeyboardUIPlugin;
import org.rodinp.keyboard.ui.preferences.PreferenceConstants;

/**
 * Utility class for property sections.
 * 
 * @author vitaly
 *
 */
public final class PropertySectionUtil {
	
	/**
	 * Event-B keyboard input modify listener.
	 * Converts ASCII to Event-B mathematical symbols.
	 */
	public static final ModifyListener eventBListener = RodinKeyboardUIPlugin.getDefault().createRodinModifyListener();
	
	/**
	 * Rodin font.
	 */
	public static final Font rodinFont = JFaceResources.getFont(PreferenceConstants.RODIN_MATH_FONT);
	
	/**
	 * Returns new control decorator initialised with message.
	 * 
	 * @param control control widget
	 * @param message initial message or null if none
	 * @param type decorator type constant from FieldDecorationRegistry
	 * @param visible is it visible when created
	 * @return control decorator
	 */
	public static ControlDecoration createDecorator(Control control, String message, String type, boolean visible) {
		ControlDecoration controlDecoration = new ControlDecoration(control, SWT.LEFT | SWT.TOP);
		controlDecoration.setDescriptionText(message);
		FieldDecoration fieldDecoration = FieldDecorationRegistry.getDefault().getFieldDecoration(type);
		controlDecoration.setImage(fieldDecoration.getImage());
		if (!visible)
			controlDecoration.hide();
		return controlDecoration;
	}
}
