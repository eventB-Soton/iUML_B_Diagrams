/*******************************************************************************
 * Copyright (c) 2014, 2018 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.generator.impl;

import org.eclipse.osgi.util.NLS;

public class ValidatorExtPtIdentifiers extends NLS {
	private static final String BUNDLE_NAME = "ac.soton.eventb.emf.diagrams.generator.impl.validatorExtPtIdentifiers"; //$NON-NLS-1$
	public static String EXTPT_VALIDATOR_ID;
	public static String EXTPT_VALIDATOR_EDITORCLASS;
	public static String EXTPT_VALIDATOR_VALIDATORCLASS;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, ValidatorExtPtIdentifiers.class);
	}

	private ValidatorExtPtIdentifiers() {
	}
}
