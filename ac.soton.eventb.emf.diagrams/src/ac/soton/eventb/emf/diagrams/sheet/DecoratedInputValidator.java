/**
 * Copyright (c) 2010 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.fieldassist.ControlDecoration;

/**
 * @author vitaly
 *
 */
public abstract class DecoratedInputValidator implements IInputValidator {
	
	private final ControlDecoration controlDecoration;

	/**
	 * @param controlDecoration control decoration if required
	 */
	public DecoratedInputValidator(ControlDecoration controlDecoration) {
		this.controlDecoration = controlDecoration;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IInputValidator#isValid(java.lang.String)
	 */
	@Override
	public String isValid(String newText) {
		String message = isValidInput(newText);
		if (controlDecoration != null) {
			if (message == null) {
				controlDecoration.hide();
			} else {
				controlDecoration.setDescriptionText(message);
				controlDecoration.show();
			}
		}
		return message;
	}

	/**
	 * Returns validation result.
	 * 
	 * @param input validation input string
	 * @return validation error message or null
	 */
	public abstract String isValidInput(String input);

}
