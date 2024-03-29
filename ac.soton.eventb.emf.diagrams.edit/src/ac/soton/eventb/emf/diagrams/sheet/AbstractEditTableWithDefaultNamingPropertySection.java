/*******************************************************************************
 * Copyright (c) 2014, 2015 University of Southampton.
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

import org.eclipse.emf.ecore.EReference;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;
import org.eventb.emf.core.util.NameUtils;

import ac.soton.eventb.emf.diagrams.Diagram;
import ac.soton.eventb.emf.diagrams.DiagramsPackage;

public abstract class AbstractEditTableWithDefaultNamingPropertySection extends
		AbstractEditTablePropertySection {

	@Override
	protected abstract EReference getFeature();

	/**
	 * This Overrides the getNewValue method to add a default unique name to the DataPacket
	 */
	@Override
	protected Object getNewValue(){
		Object newVal = super.getNewValue();
		if (newVal instanceof EventBNamed && newVal instanceof EventBElement){
			String newName = NameUtils.getName((Diagram)owner.getContaining(DiagramsPackage.Literals.DIAGRAM))+"_"+getFeaturePrefix();
			((EventBNamed)newVal).setName(NameUtils.getSafeName((EventBElement)newVal, newName, owner, getFeature()));
		}
		return newVal;
	}
	
	/**
	 * returns a name prefix to use for elements of the property sections feature
	 * This default returns the feature name.
	 * Extenders may override this to provide an alternative prefix
	 * 
	 * 
	 * @return
	 */
	protected String getFeaturePrefix() {
		return getFeature().getName();
	}

}
