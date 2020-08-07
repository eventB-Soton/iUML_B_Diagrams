/*******************************************************************************
 * Copyright (c) 2014 University of Southampton and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;

/**
 * This provides an abstract basis for an edit table with additional buttons to create and delete objects in the Event-B model
 * and add/remove references to them in the table.
 * 
 * @author cfs
 *
 */
public abstract class AbstractEditTableWithReferencedObjectCreationDeletionPropertySection extends
		AbstractEditTablePropertySection {

	@Override
	protected abstract EReference getFeature();

	/**
	 * method to create a new element to be added to the references
	 * @return
	 */
	protected abstract Object createNewElement();
	
	protected Button creAddButton;
	protected Button remDelButton;
	
	@Override
	protected FormAttachment moreButtons(FormAttachment leftData, FormAttachment topData, FormAttachment bottomData){
		
		//change labels of add/remove buttons to link/un-link which is more appropriate for a reference
		//and will match the creat/delete and link/un-link buttons we are adding here
		if (addButton!=null) addButton.setText("Link "+getButtonLabelText());
		if (removeButton!=null) removeButton.setText("Un-link "+getButtonLabelText());
		
		Control[] children = parent.getChildren();
		FormData data;
		
		// a new button to create eventB element and add a reference to it to
		creAddButton = getWidgetFactory().createButton((Composite) children[0], "Create && Link", SWT.PUSH);
		data = new FormData();
		data.left = leftData;
		data.bottom = bottomData;
		data.top = topData;
		creAddButton.setLayoutData(data);
		creAddButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				// remember selection
				int idx = table.getSelectionIndex();					
				addObject(createNewElement());
				// restore selection
				table.select(idx);
				table.notifyListeners(SWT.Selection, new org.eclipse.swt.widgets.Event());		
			}
		});
		
		// a new button to delete eventB element and remove it to elaborates
		remDelButton = getWidgetFactory().createButton((Composite) children[0], "Un-link && Delete", SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(creAddButton, ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
		data.bottom = bottomData;
		data.top = topData;
		remDelButton.setLayoutData(data);
		remDelButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				Object objectToBeRemoved = table.getSelection()[0].getData();
				removeObject(objectToBeRemoved);
				//delete the element
				EditingDomain editingDomain = ((DiagramEditor) getPart()).getEditingDomain();
				EObject container = getTranslationTarget();
				editingDomain.getCommandStack().execute(
						RemoveCommand.create(editingDomain, container, null, objectToBeRemoved));
			}
		});
		
		return new FormAttachment(remDelButton, ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
	}

	@Override
	public void refresh() {
		super.refresh();
		if (creAddButton!= null) creAddButton.setEnabled(addButton.isEnabled());
		if (remDelButton!=null) remDelButton.setEnabled(removeButton.isEnabled());
	}
	
	@Override
	protected void rowSelectionAction(){
		super.rowSelectionAction();
		if (creAddButton!= null) creAddButton.setEnabled(addButton.isEnabled());
		if (remDelButton!=null) remDelButton.setEnabled(removeButton.isEnabled());
	}
}
