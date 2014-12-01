/*
 * Copyright (c) 2014 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eventb.emf.core.EventBNamed;

import ac.soton.eventb.emf.core.extension.coreextension.EventBDataElaboration;
import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

/**
 * Name property section for EventBNamed.
 * 
 *
 */
public class NamePropertySection extends AbstractTextPropertySection {

	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBNamed;
		}
	}
	
	@Override
	protected String getPropertyNameLabel() {
		return "Name:";
	}

	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		doCreateControls(parent, aTabbedPropertySheetPage);
		getTextWidget().addModifyListener(new ModifyListener(){
			@Override
			public void modifyText(ModifyEvent e) {
				EventBNamed ebn = (EventBNamed) eObject;
				if (ebn instanceof EventBDataElaboration){
					EventBDataElaboration ee = (EventBDataElaboration)ebn;
					if (ee.getElaborates() != null){
						getTextWidget().setEditable(false);
					} else {
						getTextWidget().setEditable(true);
					}
				}
			}
			
		});
	}
	
	@Override
	protected void setPropertyValue(EObject object, Object value) {
		assert object instanceof EventBNamed;
		((EventBNamed) object).setName((String) value);
	}

	@Override
	protected String getPropertyValueString() {
		return ((EventBNamed) getEObject()).getName();
	}

	@Override
	protected String getPropertyChangeCommandName() {
		return "change name";
	}
	
	@Override
	public void refresh() {
		super.refresh();
		if (eObject instanceof EventBDataElaboration && ((EventBDataElaboration) eObject).getElaborates() != null){
			getTextWidget().setForeground(org.eclipse.draw2d.ColorConstants.gray);
			getTextWidget().setEnabled(false);
		} else {
			getTextWidget().setForeground(org.eclipse.draw2d.ColorConstants.black);
			getTextWidget().setEnabled(true);
		}
	}
}
