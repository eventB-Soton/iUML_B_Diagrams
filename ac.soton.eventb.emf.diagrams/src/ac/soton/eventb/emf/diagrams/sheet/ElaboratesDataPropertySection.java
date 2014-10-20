/*
 * Copyright (c) 2010 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.sheet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.edit.command.AbstractOverrideableCommand;

import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.common.ui.dialogs.PopupDialog;

import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
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
import ac.soton.eventb.emf.diagrams.Diagram;
import ac.soton.eventb.emf.diagrams.DiagramsPackage;
import ac.soton.eventb.emf.diagrams.generator.utils.Is;
import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

/**
 * Elaborates property section for Classdiagrams.
 * 
 * @author colin/gin
 * 
 */
public class ElaboratesDataPropertySection extends AbstractLOVPropertySection {

	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBDataElaboration;
		}
	}
	
	private static ILabelProvider variableLabelProvider = new LabelProvider() {

		@Override
		public String getText(Object element) {
			if (element instanceof EventBNamed){
				return ((EventBNamed)element).getName();
			} else {
				return "<unknown>";				
			}
		}
	};

	/**
	 * Get a new child instance for the result of clicking the add button.
	 * 
	 * @return a new child instance.
	 */
	protected Object getNewChild() {
		//get the container machine or context
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)((EventBElement)eObject).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
		// find all data elements in scope
		List<EventBNamed> valuesList = getAvailableDataElements(container);
		valuesList.removeAll(getElaboratedElements(container));
		// ask user to choose
		PopupDialog variablesDialog = new PopupDialog(getPart().getSite()
				.getShell(), valuesList, variableLabelProvider);
		variablesDialog.setTitle("Avaliable data elements in scope from "+((EventBNamed)container).getName());
		variablesDialog.setMessage("Please select an element to elaborate");
		//return the chosen one
		if (Dialog.OK == variablesDialog.open()) {
			if (variablesDialog.getResult().length > 0){
				return variablesDialog.getResult()[0]; 
			}
		}
		return null;
	}
	
private Collection<?> getElaboratedElements(EventBNamedCommentedComponentElement container) {
		List<EventBNamed> list =  new ArrayList<EventBNamed>();
		List<EventBNamed> filteredList =  new ArrayList<EventBNamed>();
		if (container instanceof Machine){
			Machine m = ((Machine)container);
			list.addAll(m.getVariables());	
		}else if (container instanceof Context){
			Context c = ((Context)container);
			list.addAll(c.getSets());
			list.addAll(c.getConstants());
		}
		for (EventBNamed ne : list){
			if (Is.generatedBy(ne, eObject)){
				filteredList.add(ne);
			}
		}
		Diagram diagram = (Diagram)((EventBElement)eObject).getContaining(DiagramsPackage.Literals.DIAGRAM);
		if (diagram instanceof EventBElement){
			EList<EObject> elaborators = ((EventBElement)diagram).getAllContained(CoreextensionPackage.Literals.EVENT_BDATA_ELABORATION, true);
			for (EObject elaborator : elaborators){
				if (elaborator instanceof EventBDataElaboration){
					EventBNamed elaborated = ((EventBDataElaboration)elaborator).getElaborates();
					if (list.contains(elaborated)){
						filteredList.add(elaborated);
					}
				}
			}
		}
		return filteredList;
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

	@Override
	public void createControls(Composite parent,
			TabbedPropertySheetPage aTabbedPropertySheetPage) {
		super.createControls(parent, aTabbedPropertySheetPage);

		getTextWidget().addModifyListener(new ModifyListener(){

			@Override
			public void modifyText(ModifyEvent e) {
				if (eObject != null && ((EventBDataElaboration)eObject).getElaborates() != null){
					addButton.setEnabled(false);
					clearButton.setEnabled(true);
				} else {
					addButton.setEnabled(true);
					clearButton.setEnabled(false);
				}
				
				//if refined or elaborated, disable text editing
				if (eObject != null && 
						
						//(((Class)eObject).getRefines() != null || 
						((EventBDataElaboration)eObject).getElaborates() != null){
						lovText.setEnabled(false);
//						lovText.setForeground(ColorConstants.gray);
				} else {
						lovText.setEnabled(true);
//						lovText.setForeground(ColorConstants.black);
				}
			}
			
		});
		
		refresh();
	}

	@Override
	protected void clearElement() {
		//if the elaborated element has been generated by this element
		// we need to delete it so that it doesn't become orphaned
		EditingDomain editingDomain = ((DiagramDocumentEditor) getPart()).getEditingDomain();
		AbstractOverrideableCommand command;
		EventBNamed elaborated = ((EventBDataElaboration)eObject).getElaborates();
		if (elaborated instanceof EObject && Is.generatedBy(elaborated, eObject)){
			command = (RemoveCommand) RemoveCommand.create(editingDomain, ((EObject)elaborated).eContainer(), ((EObject)elaborated).eContainingFeature(), elaborated);
			editingDomain.getCommandStack().execute(command);
		}
		//super will clear the elaborates field for us
		super.clearElement();
	}
	
	
	protected void modifyElement(Object pNewChild){
		super.modifyElement(pNewChild);
		
		EditingDomain editingDomain = ((DiagramDocumentEditor) getPart()).getEditingDomain();
		
		AbstractOverrideableCommand command;
		
		//set label value
		Object eref = null;
		if (eObject instanceof EventBLabeled){
			eref = CoreextensionPackage.Literals.EVENT_BLABELED__LABEL;
		}else if (eObject instanceof EventBNamed){
			eref = CorePackage.Literals.EVENT_BNAMED__NAME;
		}
		if (eref != null){
			if (getFeature().isMany() == true){
					command = null;
	//					(AddCommand) AddCommand.create(
	//						editingDomain,
	//						eObject, 
	//						ClassdiagramsPackage.eINSTANCE.getName(), 
	//						((EventBNamed)pNewChild).getName());
			} else {
				command = (SetCommand) SetCommand.create(
						editingDomain,
						eObject, 
						eref,
						((EventBNamed)pNewChild).getName());
			}
			if (command != null){
				editingDomain.getCommandStack().execute(command);
			}
		}
		//AbstractOverrideableCommand command2;
		
		//set type to match new elaborated element
		DataKind dataKind = null;
		EventBElement elaboratedElement = ((EventBElement)pNewChild);
		if (elaboratedElement instanceof CarrierSet) dataKind = DataKind.SET;
		else if (elaboratedElement instanceof Constant) dataKind = DataKind.CONSTANT;
		else if (elaboratedElement instanceof Variable) dataKind = DataKind.VARIABLE;
		command = (SetCommand) SetCommand.create(
				editingDomain,
				eObject, 
				CoreextensionPackage.Literals.EVENT_BDATA_ELABORATION__DATA_KIND,
				dataKind);;
		editingDomain.getCommandStack().execute(command);
		
	}

	@Override
	public String getLOVValue() {
		if (eObject != null && ((EventBDataElaboration) eObject).getElaborates() != null) { 
			return ((EventBDataElaboration) eObject).getElaborates().getName();
		} else {
			return "<not set>";
		}
	}

	@Override
	protected EStructuralFeature getFeature() {
		return CoreextensionPackage.Literals.EVENT_BDATA_ELABORATION__ELABORATES;
	}

	@Override
	protected String getLabelText() {
		return "Elaborates:";
	}

	@Override
	protected String getClearButtonLabel() {
		return "Disconnect / Delete generated element";
	}

	@Override
	protected String getPickValueButtonLabel() {
		return "Connect to existing data element";
	}

	
}
