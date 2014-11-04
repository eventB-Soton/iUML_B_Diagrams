/*
 * Copyright (c) 2013 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.sheet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AbstractOverrideableCommand;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.common.ui.dialogs.PopupDialog;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.context.CarrierSet;
import org.eventb.emf.core.context.Constant;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.machine.Machine;
import org.eventb.emf.core.machine.Variable;

import ac.soton.eventb.emf.core.extension.coreextension.CoreextensionPackage;
import ac.soton.eventb.emf.core.extension.coreextension.DataKind;
import ac.soton.eventb.emf.core.extension.coreextension.EventBDataElaboration;
import ac.soton.eventb.emf.core.extension.coreextension.EventBLabeled;
import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;


/**
 * Elaborates data property section for EventBDataElaboration.
 * 
 * @author cfsnook
 *
 */
public class ElaboratesDataPropertySection extends AbstractTablePropertySection {
	
	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBDataElaboration;
		}
	}
	
	private Button creAddButton;
	private Button remDelButton;
	
	private static ILabelProvider dataLabelProvider = new LabelProvider() {
		@Override
		public String getText(Object element) {
			if (element instanceof EventBNamed){
				return ((EventBNamed)element).getName();
			} else if (element instanceof EventBLabeled){
				return ((EventBLabeled)element).getLabel();
			} else {
				return "<unknown>";				
			}
		}
	};

	@Override
	protected String getButtonLabelText() {
		return "Data";
	}

	/**
	 * data elaboration is singular (unlike event elaboration). 
	 * However, for now this property section will cater for future  
	 *  supporting of multiple data elaboration.
	 */
	@Override
	protected EList<Object> getOwnedRows() {
		EList<Object> list = new BasicEList<Object>();
		list.add(((EventBDataElaboration) eObject).getElaborates());
		return list;
	}

	@Override
	protected EStructuralFeature getFeature() {
		return CoreextensionPackage.Literals.EVENT_BDATA_ELABORATION__ELABORATES;
	}

	@Override
	protected List<String> getValuesForRow(Object object) {
		List<String> values = new ArrayList<String>();
		values.add(dataLabelProvider.getText(object));
		return values;
	}

	@Override
	protected List<String> getColumnLabelText() {
		ArrayList<String> values = new ArrayList<String>();
		values.add("Elaborated Data");		
		return values;
	}

	/**
	 * Get a new data element to elaborate for the result of clicking the add button.
	 * 
	 * @return a data element.
	 */
	@Override
	protected Object getNewChild() {
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)((EventBElement) eObject).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
		PopupDialog dataDialog = new PopupDialog(getPart().getSite().getShell(), getAvailableDataElements(container), dataLabelProvider);
		dataDialog.setTitle( "Data elements in scope of" + container.getName());
		dataDialog.setMessage("Please select data to elaborate");
		//return the chosen one
		if (Dialog.OK == dataDialog.open()) {
			if (dataDialog.getResult().length > 0){
				return dataDialog.getResult()[0]; 
			}
		}
		return null;
	}
	
	private List<EventBNamed> getAvailableDataElements(EventBNamedCommentedComponentElement container) {
		List<EventBNamed> list =  new ArrayList<EventBNamed>() ;
		if (container instanceof Machine){
			Machine m = ((Machine)container);
			list.addAll(m.getVariables());
			for (Context c : m.getSees()){
				list.addAll(getAvailableDataElements(c));
			}			
		}else if (container instanceof Context){
			Context c = ((Context)container);
			list.addAll(c.getSets());
			list.addAll(c.getConstants());
			for (Context x : c.getExtends()){
				list.addAll(getAvailableDataElements(x));
			}
		}
		return list;
	}
	
	private List<EventBNamedCommentedComponentElement> getComponentList(EventBNamedCommentedComponentElement component) {
		List<EventBNamedCommentedComponentElement> list =  new ArrayList<EventBNamedCommentedComponentElement>() ;
		if (component instanceof Machine){
			Machine m = ((Machine)component);
			list.add(m);
			for (Context c : m.getSees()){
				list.addAll(getComponentList(c));
			}			
		}else if (component instanceof Context){
			Context c = ((Context)component);
			list.add(c);
			for (Context x : c.getExtends()){
				list.addAll(getComponentList(x));
			}
		}
		return list;
	}
	

	@Override
	protected String getLabelText() {
		return "Elaborates:";
	}

	@Override
	protected ISelection getEditorSelection(Object object) {
		return null;
	}

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);
		
		Control[] children = parent.getChildren();
		FormData data;
		
		// overriding "Delete" label
		removeButton.setText("Remove Data");
		
		// a new button to create eventB data and add it to elaborates
		creAddButton = getWidgetFactory().createButton((Composite) children[0], "Create && Add", SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(removeButton, 0);
		data.bottom = new FormAttachment(100, 0);
		creAddButton.setLayoutData(data);
		creAddButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// remember selection
				int idx = table.getSelectionIndex();
				// create and add new data
				String defaultName = eObject instanceof EventBNamed ? ((EventBNamed)eObject).getName() : null ;
				NewDataDialog dialog = new NewDataDialog(
						getPart().getSite().getShell(), 
						getComponentList((EventBNamedCommentedComponentElement)((EventBElement) eObject).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT)),
						((EventBDataElaboration)eObject).getDataKind(),
						dataLabelProvider, defaultName );
				if (Dialog.OK == dialog.open()) {
					EObject objectToBeAdded = dialog.getElement();
					if (objectToBeAdded == null) return;
					EditingDomain editingDomain = ((DiagramEditor) getPart()).getEditingDomain();
					CompoundCommand cc = new CompoundCommand("Add new data for elaborates");
					cc.append(AddCommand.create(editingDomain, dialog.getComponent(), dialog.getFeature(), objectToBeAdded));
					editingDomain.getCommandStack().execute(cc);
					addObject(objectToBeAdded);
					refresh();
					// restore selection
					table.select(idx);
					table.notifyListeners(SWT.Selection, new org.eclipse.swt.widgets.Event());
				}
				
			}
		});
		
		// a new button to delete eventB data and remove it from elaborates
		remDelButton = getWidgetFactory().createButton((Composite) children[0], "Remove && Delete", SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(creAddButton, 0);
		data.bottom = new FormAttachment(100, 0);
		remDelButton.setLayoutData(data);
		remDelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				EditingDomain editingDomain = ((DiagramEditor) getPart()).getEditingDomain();
				Object objectToBeRemoved = table.getSelection()[0].getData();
				removeObject(objectToBeRemoved);
				//delete the removed element
				if (objectToBeRemoved instanceof EObject){
					EObject container = EcoreUtil.getRootContainer((EObject)objectToBeRemoved);
					EReference containment = ((EObject)objectToBeRemoved).eContainmentFeature();
					editingDomain.getCommandStack().execute(
						RemoveCommand.create(editingDomain, container, containment , objectToBeRemoved));
				}
				refresh();
			}
		});		
	}
	
	protected void addObject(Object objectToBeAdded){
		if (objectToBeAdded == null) return;
		super.addObject(objectToBeAdded);
		updateElementAfterElaboratesChanged(objectToBeAdded);
	}
	
	protected void removeObject(Object objectToBeRemoved){
		if (objectToBeRemoved == null) return;
		super.removeObject(objectToBeRemoved);
		updateElementAfterElaboratesChanged(null);
	}
	
	private void updateElementAfterElaboratesChanged(Object newElaboratedElement) {
		EditingDomain editingDomain = ((DiagramDocumentEditor) getPart()).getEditingDomain();
		AbstractOverrideableCommand command;
		//set label value
		Object nameLabelFeature = null;
		if (eObject instanceof EventBLabeled){
			nameLabelFeature = CoreextensionPackage.Literals.EVENT_BLABELED__LABEL;
		}else if (eObject instanceof EventBNamed){
			nameLabelFeature = CorePackage.Literals.EVENT_BNAMED__NAME;
		}
		if (nameLabelFeature != null){
			String name = "";
			if (newElaboratedElement instanceof EventBNamed){
				name = ((EventBNamed)newElaboratedElement).getName();
			}
			command = (SetCommand) SetCommand.create(
					editingDomain,
					eObject, 
					nameLabelFeature,
					name);
			editingDomain.getCommandStack().execute(command);
		}		
		//set type to match new elaborated element
		Object dataKind = null;
		if (newElaboratedElement instanceof CarrierSet) dataKind = DataKind.SET;
		else if (newElaboratedElement instanceof Constant) dataKind = DataKind.CONSTANT;
		else if (newElaboratedElement instanceof Variable) dataKind = DataKind.VARIABLE;
		else dataKind = SetCommand.UNSET_VALUE;
		command = (SetCommand) SetCommand.create(
				editingDomain,
				eObject, 
				CoreextensionPackage.Literals.EVENT_BDATA_ELABORATION__DATA_KIND,
				dataKind);;
		editingDomain.getCommandStack().execute(command);
	}

	@Override
	public void refresh() {
		super.refresh();
		remDelButton.setEnabled(false);
	}
	
	@Override
	public void rowSelected(){
		super.rowSelected();
		remDelButton.setEnabled(true);
	}
}
