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
