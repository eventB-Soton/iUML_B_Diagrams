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
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.edit.command.AbstractOverrideableCommand;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.common.ui.dialogs.PopupDialog;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBObject;
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
public class ElaboratesDataPropertySection extends AbstractEditTableWithReferencedObjectCreationDeletionPropertySection {
	
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
	protected EReference getFeature() {
		return CoreextensionPackage.Literals.EVENT_BDATA_ELABORATION__ELABORATES;
	}
	
	@Override
	protected Object getFeatureForCol(int col) {
		switch (col) {
		case 0 : return EcorePackage.Literals.EREFERENCE__CONTAINER;
		case 1 : return CorePackage.Literals.EVENT_BNAMED__NAME;
		case 2 : return CorePackage.Literals.EVENT_BCOMMENTED__COMMENT;
		default : return null;
		}
	}
	
	@Override
	protected boolean isMulti(final int col){
		return col==2;
	}
	
	@Override
	protected boolean isReadOnly(final int col) {
		return  col==0;
	}

	@Override
	protected String getButtonLabelText() {
		return "Data";
	}
	

	@Override
	protected String getLabelText() {
		return "Elaborates:";
	}
	
	@Override
	protected boolean isReadOnly(){
		return super.isReadOnly(); 
	}

	protected boolean isSingular() {
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	private boolean isRefinement() {
		if (eObject==null) return false;
		EStructuralFeature refinesFeature = eObject.eClass().getEStructuralFeature("refines");
		Object refines = refinesFeature==null? null : eObject.eGet(refinesFeature);
		return 	refines == null? false : 
				refines instanceof EList? !((EList)refines).isEmpty() : true;	
	}
	
	protected EList<?> getPossibleReferences() {
		if (!(getFeature() instanceof EReference)) return null;
		EReference feature = (EReference) getFeature();
		EClass eClass = feature.getEReferenceType();
		EventBObject container = owner.getContaining(CorePackage.Literals.PROJECT);
		if (container == null){
			container = owner.getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
		}
		if (container == null){ return ECollections.EMPTY_ELIST;}
		EList<EObject> possibles = container.getAllContained(eClass, false);
		possibles.removeAll(this.getElements());
		possibles.remove(owner);
		possibles.remove(null);
		return possibles;
	}

	@Override
	protected Object createNewElement(){
		EObject newData = null;
		// create and add new data
		String defaultName = eObject instanceof EventBNamed ? ((EventBNamed)eObject).getName() : null ;
		NewDataDialog dialog = new NewDataDialog(
				getPart().getSite().getShell(), 
				getComponentList((EventBNamedCommentedComponentElement)((EventBElement) eObject).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT)),
				((EventBDataElaboration)eObject).getDataKind(),
				dataLabelProvider, defaultName );
		if (Dialog.OK == dialog.open()) {
			newData = dialog.getElement();
			if (newData != null) {
				EditingDomain editingDomain = ((DiagramEditor) getPart()).getEditingDomain();
				CompoundCommand cc = new CompoundCommand("Add new data for elaborates");
				cc.append(AddCommand.create(editingDomain, dialog.getComponent(), dialog.getFeature(), newData));
				editingDomain.getCommandStack().execute(cc);
				addObject(newData);
			}
		}
		return newData;
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
	
	
	private static ILabelProvider dataLabelProvider = new LabelProvider() {
		@Override
		public String getText(Object element) {
			EventBElement container = (EventBElement) (element instanceof EventBElement? 
					((EventBElement)element).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT) : null);
			String containerName = container instanceof EventBNamed?  ((EventBNamed)container).getName() : "?";
			if (element instanceof EventBNamed){
				return containerName+" : "+((EventBNamed)element).getName();
			} else if (element instanceof EventBLabeled){
				return containerName+" : "+((EventBLabeled)element).getLabel();
			} else {
				return "<unknown>";				
			}
		}
	};

	/**
	 * Get a new data element to elaborate for the result of clicking the add button.
	 * 
	 * @return a data element.
	 */
	@Override
	protected Object getNewValue() {
		EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement)owner.getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
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
	
	@Override
	protected void addObject(Object objectToBeAdded){
		if (objectToBeAdded == null) return;
		super.addObject(objectToBeAdded);
		updateElementAfterElaboratesChanged(objectToBeAdded);
	}

	@Override
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
		if (addButton!=null) addButton.setEnabled(addButton.isEnabled() && !isRefinement());
		if (creAddButton!=null) creAddButton.setEnabled(creAddButton.isEnabled() && !isRefinement());
		if (removeButton!=null) removeButton.setEnabled(removeButton.isEnabled() && !isRefinement());
		if (remDelButton!=null) remDelButton.setEnabled(remDelButton.isEnabled() && !isRefinement());
	}
	
	@Override
	protected void rowSelectionAction(){
		super.rowSelectionAction();
		if (addButton!=null) addButton.setEnabled(addButton.isEnabled() && !isRefinement());
		if (creAddButton!=null) creAddButton.setEnabled(creAddButton.isEnabled() && !isRefinement());
		if (removeButton!=null) removeButton.setEnabled(removeButton.isEnabled() && !isRefinement());
		if (remDelButton!=null) remDelButton.setEnabled(remDelButton.isEnabled() && !isRefinement());
	}
}
