/*
 * Copyright (c) 2011 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.sheet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.jface.viewers.IFilter;

import ac.soton.eventb.emf.core.extension.coreextension.CoreextensionPackage;
import ac.soton.eventb.emf.core.extension.coreextension.DataKind;
import ac.soton.eventb.emf.core.extension.coreextension.EventBDataElaboration;
import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

/**
 * property section for Data elaboration, DataKinds.
 * 
 * @author colin
 *
 */
public class DataKindPropertySection extends AbstractEnumerationPropertySection {
	
	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBDataElaboration;
		}
	}

	@Override
	protected String[] getEnumerationFeatureValues() {
		List<String> dataKinds = new ArrayList<String>();
		for (DataKind dk : DataKind.VALUES){
			dataKinds.add(dk.getLiteral());
		}
		dataKinds = filterDataKinds(dataKinds);
		return dataKinds.toArray(new String[dataKinds.size()]);
	}

	/**
	 * extenders can override this to filter out invalid data kinds
	 * so that they are not available
	 * 
	 * @param values
	 * @return values with invalid data kinds removed
	 */
	protected List<String> filterDataKinds(List<String> values) {
		return values;
	}

	@Override
	protected String getFeatureAsText() {
		return ((EventBDataElaboration)eObject).getDataKind().getLiteral();
	}

	@Override
	protected Object getFeatureValue(String selection) {
		return DataKind.get(selection).getValue();
	}

	@Override
	protected String getLabelText() {
		return "Data Kind:";
	}

	@Override
	protected EStructuralFeature getFeature() {
		return CoreextensionPackage.Literals.EVENT_BDATA_ELABORATION__DATA_KIND;
	}
	
	@Override
	public void refresh() {
		combo.setItems(getEnumerationFeatureValues());
		combo.setText(getFeatureAsText());
		combo.setEnabled(((EventBDataElaboration)eObject).getElaborates() == null);
	}

	@Override
	protected Object getFeatureByValue(Object value) {
		Integer ftValue = (Integer)value; 
		return DataKind.get(ftValue);
	}

	@Override
	protected List<DataKind> getAvailableDataElements() {
		return DataKind.VALUES;
	}

}
