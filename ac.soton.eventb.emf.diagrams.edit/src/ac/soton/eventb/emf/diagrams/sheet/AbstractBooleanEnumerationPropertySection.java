/*******************************************************************************
 * Copyright (c) 2014 University of Southampton and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package ac.soton.eventb.emf.diagrams.sheet;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractBooleanEnumerationPropertySection extends AbstractEnumerationPropertySection {

	
	@Override
	protected String[] getEnumerationFeatureValues() {
		return boolsStrings;
	}

	@Override
	protected Object getFeatureValue(String selection) {
		return Boolean.valueOf(selection);
	}

	@Override
	protected List<Object> getAvailableDataElements() {
		return bools;
	}

	private static final List<Object> bools =  new ArrayList<Object>();
	private static final String[] boolsStrings = new String[]{"true", "false"};
	static{
		bools.add(Boolean.TRUE);
		bools.add(Boolean.FALSE);
	}
}
