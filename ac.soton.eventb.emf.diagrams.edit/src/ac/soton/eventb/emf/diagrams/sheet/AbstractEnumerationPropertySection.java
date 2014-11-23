/*******************************************************************************
 * Copyright (c) 2014 University of Southampton and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;


/**
 * An abstract implementation of a section with a enumeration field using a
 * combo box (pulldown).
 * 
 * @author copied from {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractEnumerationPropertySection AbstractEnumerationPropertySection}
 */
public abstract class AbstractEnumerationPropertySection
	extends AbstractIumlbPropertySection {

	/**
	 * the combo box control for the section.
	 */
	protected CCombo combo;

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage)
	 */
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		Composite composite = getWidgetFactory()
			.createFlatFormComposite(parent);
		FormData data;

		combo = getWidgetFactory().createCCombo(composite); 
		data = new FormData();
		data.left = new FormAttachment(0, getPropertyLabelWidth(composite));
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		combo.setLayoutData(data);

		CLabel nameLabel = getWidgetFactory().createCLabel(composite,
			getLabelText());
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(combo, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(combo, 0, SWT.CENTER);
		nameLabel.setLayoutData(data);

		combo.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				handleComboModified();
			}
		});
	}

	/**
	 * Handle the combo modified event.
	 */
	protected void handleComboModified() {
		EditingDomain editingDomain = ((DiagramDocumentEditor) getPart()).getEditingDomain();
		int index = combo.getSelectionIndex();
		boolean equals = isEqual(combo.getItem(index));
		if (!equals) {
				Object value = getFeatureValue(combo.getItem(index));
			/* apply the property change to single selected object */
			if (getFeature().isUnsettable() && value==null){
				value = org.eclipse.emf.edit.command.SetCommand.UNSET_VALUE;				
			}
			editingDomain.getCommandStack().execute(
				SetCommand.create(editingDomain, eObject, getFeature(),
					getFeatureByValue(value)));
		}
	}

	protected abstract Object getFeatureByValue(Object value);

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
	 */
	public void refresh() {
		combo.setItems(getEnumerationFeatureValues());
		combo.setText(getFeatureAsText());
	}

	/**
	 * Determine if the provided selection of the enumeration is equal to the
	 * current setting of the enumeration property.
	 * 
	 * @param index
	 *            the new selection in the enumeration.
	 * @return <code>true</code> if the new index value is equal to the
	 *         current property setting.
	 */
	protected abstract boolean isEqual(String selection);

	/**
	 * Get the enumeration values of the feature for the combo field for the
	 * section.
	 * 
	 * @return the list of values of the feature as text.
	 */
	protected abstract String[] getEnumerationFeatureValues();

	/**
	 * Get the value of the feature as text for the combo field for the section.
	 * 
	 * @return the value of the feature as text.
	 */
	protected abstract String getFeatureAsText();

	/**
	 * Get the new value of the feature for the the selection in the enumeration.
	 * 
	 * @param index
	 *            the new selection in the enumeration.
	 * @return the new value of the feature.
	 */
	protected abstract Object getFeatureValue(String selection);
}