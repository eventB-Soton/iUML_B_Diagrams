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
