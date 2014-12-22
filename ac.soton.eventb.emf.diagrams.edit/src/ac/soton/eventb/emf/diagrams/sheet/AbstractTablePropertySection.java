/*******************************************************************************
 * Copyright (c) 2014 University of Southampton and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package ac.soton.eventb.emf.diagrams.sheet;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.AbstractOverrideableCommand;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;

/**
 * An abstract implementation of a section with a table field with add and
 * remove buttons.
 * 
 * @author copied from {@link org.eclipse.ui.examples.views.properties.tabbed.hockeyleague.ui.properties.sections.AbstractTablePropertySection AbstractTablePropertySection}
 */
public abstract class AbstractTablePropertySection
	extends AbstractIumlbPropertySection {

	final int TEXT_MARGIN = 3;
	
	/**
	 * the table control for the section.
	 */
	protected Table table;

	/**
	 * the title columns for the section.
	 */
	protected List<TableColumn> columns;

	/**
	 * the add button for the section.
	 */
	protected Button addButton;

	/**
	 * the remove button for the section.
	 */
	protected Button removeButton;

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
		int labelWidth = getPropertyLabelWidth(composite);
		table = getWidgetFactory().createTable(composite,
			SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		List<String> labels = getColumnLabelText();
		columns = new ArrayList<TableColumn>();
		for (String label : labels) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(label);
			columns.add(column);
		}

		/*
		 * NOTE: MeasureItem, PaintItem and EraseItem are called repeatedly.
		 * Therefore, it is critical for performance that these methods be
		 * as efficient as possible.
		 */
		table.addListener(SWT.MeasureItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem)event.item;
				String text = item.getText(event.index);
				Point size = event.gc.textExtent(text);
				event.width = size.x + 2 * TEXT_MARGIN;
				event.height = Math.max(event.height, size.y + TEXT_MARGIN);
			}
		});
		table.addListener(SWT.EraseItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				event.detail &= ~SWT.FOREGROUND;
			}
		});
		table.addListener(SWT.PaintItem, new Listener() {
			@Override
			public void handleEvent(Event event) {
				TableItem item = (TableItem)event.item;
				String text = item.getText(event.index);
				/* center column 1 vertically */
				int yOffset = 0;
					Point size = event.gc.textExtent(text);
					yOffset = Math.max(0, (event.height - size.y) / 2);
				event.gc.drawText(text, event.x + TEXT_MARGIN, event.y + yOffset, true);
			}
		});
		
		Shell shell = new Shell();
		GC gc = new GC(shell);
		gc.setFont(shell.getFont());
		Point point = gc.textExtent("");//$NON-NLS-1$
		int buttonHeight = point.y + 11;
		gc.dispose();
		shell.dispose();

		addButton = getWidgetFactory().createButton(composite,
		MessageFormat.format("Add {0}",//$NON-NLS-1$
				new Object[] {getButtonLabelText()}), SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(0, labelWidth);
		data.bottom = new FormAttachment(100, 0);
		data.top = new FormAttachment(100, -buttonHeight);
		addButton.setLayoutData(data);
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				addObject(getNewChild());
				refresh();
			}
		});

		removeButton = getWidgetFactory().createButton(composite,
		MessageFormat.format("Delete {0}",//$NON-NLS-1$
				new Object[] {getButtonLabelText()}), SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(addButton, 0);
		data.bottom = new FormAttachment(100, 0);
		data.top = new FormAttachment(100, -buttonHeight);
		removeButton.setLayoutData(data);
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				removeObject(table.getSelection()[0].getData());
				refresh();
			}
		});

		data = new FormData();
		data.left = new FormAttachment(addButton, 0, SWT.LEFT);
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, 0);
		data.bottom = new FormAttachment(addButton, 0);
		data.width = 400;
		table.setLayoutData(data);
		table.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				rowSelected();
			}
		});
		table.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				if (table.getSelection().length > 0) {
					Object object = table.getSelection()[0].getData();
					ISelection selection = getEditorSelection(object);
					if (selection != null) {
						((DiagramEditor) getPart()).getDiagramGraphicalViewer().setSelection(selection);
						((DiagramEditor) getPart()).setFocus();
					}
				}
			}
		});
		CLabel nameLabel = getWidgetFactory().createCLabel(composite, getLabelText());
		data = new FormData();
		data.left = new FormAttachment(0, 0);
		data.right = new FormAttachment(table, -ITabbedPropertyConstants.HSPACE);
		data.top = new FormAttachment(table, 0, SWT.CENTER);
		nameLabel.setLayoutData(data);
	}

	/**
	 * update the feature with the given new value
	 * This can be overridden to add additional behaviour
	 **/
	protected void addObject(Object objectToBeAdded){
		if (objectToBeAdded == null) return;
		EditingDomain editingDomain = ((DiagramEditor) getPart()).getEditingDomain();
		AbstractOverrideableCommand command;
		EStructuralFeature feature = getFeature();
		if (feature.isMany())
			command = (AddCommand) AddCommand.create(
					editingDomain, eObject, feature, objectToBeAdded);
		else
			command = (SetCommand) SetCommand.create(
					editingDomain, eObject, feature, objectToBeAdded);
		editingDomain.getCommandStack().execute(command);
	}
	
	/**
	 * remove object from the elements feature
	 * the action performed when the remove button is used
	 * This can be overridden to add additional behaviour
	 * @param objectToBeRemoved
	 */
	protected void removeObject(Object objectToBeRemoved){
		if (objectToBeRemoved == null) return;
		EditingDomain editingDomain = ((DiagramEditor) getPart()).getEditingDomain();
		//remove the element from the feature
		if (getFeature().isMany()){
			Object values = eObject.eGet(getFeature());
			EList<Object> newValues = new BasicEList<Object>();
			for (Object value : (EList<?>)values) {
				if (!value.equals(objectToBeRemoved)) newValues.add(value);
			}
			editingDomain.getCommandStack().execute(
					SetCommand.create(editingDomain, eObject, getFeature(), newValues));
			//FIXME: RemoveCommand is preferred, but it causes non-containment references to be removed along with !originals!
//					RemoveCommand.create(editingDomain, eObject, getFeature(), object));
		}else{
			editingDomain.getCommandStack().execute(
					SetCommand.create(editingDomain, eObject, getFeature(), SetCommand.UNSET_VALUE));

		}
			
	}
	
	
	protected void rowSelected(){
		removeButton.setEnabled(true);
	}
	
	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#shouldUseExtraSpace()
	 */
	public boolean shouldUseExtraSpace() {
		return true;
	}

	/**
	 * @see org.eclipse.ui.views.properties.tabbed.ISection#refresh()
	 */
	public void refresh() {
		table.removeAll();
		removeButton.setEnabled(false);
		for (Object row : getOwnedRows()) {
			// create the table item
			TableItem item = new TableItem(table, SWT.NONE);
			String[] values = new String[columns.size()];
			List<String> valuesForRow = getValuesForRow(row);
			for (int j = 0; j < columns.size(); j++) {
				values[j] = (String) valuesForRow.get(j);
			}
			item.setText(values);
			item.setData(row);
		}
		for (TableColumn tc : columns) {
			tc.pack();
		}
	}

	/**
	 * Get the text for the labels that will be used for the Add and Remove
	 * buttons.
	 * 
	 * @return the label text.
	 */
	protected abstract String getButtonLabelText();

	/**
	 * Get the row objects for the table.
	 * 
	 * @return the list of the row objects.
	 */
	protected abstract EList<?> getOwnedRows();

	/**
	 * Get the values for the row in the table.
	 * 
	 * @param object
	 *            an object in the row of the table.
	 * @return the list of string values for the row.
	 */
	protected abstract List<String> getValuesForRow(Object object);

	/**
	 * Get the labels for the columns for the table.
	 * 
	 * @return the labels for the columns.
	 */
	protected abstract List<String> getColumnLabelText();

	/**
	 * Get a new child instance for the result of clicking the add button.
	 * 
	 * @return a new child instance.
	 */
	protected abstract Object getNewChild();

	/**
	 * Get editor selection for selected object in table.
	 * 
	 * @param object selected object in the table
	 * @return selection in the editor
	 */
	protected abstract ISelection getEditorSelection(Object object);

}