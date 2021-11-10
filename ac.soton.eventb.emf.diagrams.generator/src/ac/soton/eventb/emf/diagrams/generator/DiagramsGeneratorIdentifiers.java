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
package ac.soton.eventb.emf.diagrams.generator;

import org.eclipse.osgi.util.NLS;

/**
 * Provides the command id for the diagrams generator
 * 
 * @since 4.0
 */
public class DiagramsGeneratorIdentifiers extends NLS {
	private static final String BUNDLE_NAME = "ac.soton.eventb.emf.diagrams.generator.diagramsGeneratorIdentifiers"; //$NON-NLS-1$
	
	public static String COMMAND_ID;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, DiagramsGeneratorIdentifiers.class);
	}

	private DiagramsGeneratorIdentifiers() {
	}
}
