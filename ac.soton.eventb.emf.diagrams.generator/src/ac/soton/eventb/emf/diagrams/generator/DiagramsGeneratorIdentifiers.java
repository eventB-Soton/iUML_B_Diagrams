/*******************************************************************************
 *  Copyright (c) 2012-2017 University of Southampton.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *   
 *  Contributors:
 *  University of Southampton - Initial implementation
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
