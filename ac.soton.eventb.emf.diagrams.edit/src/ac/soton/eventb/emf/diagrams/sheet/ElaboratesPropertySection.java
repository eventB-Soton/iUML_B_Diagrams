/*
 * Copyright (c) 2013 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.sheet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.common.ui.dialogs.PopupDialog;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Machine;
import org.eventb.emf.core.machine.MachinePackage;

import ac.soton.eventb.emf.core.extension.coreextension.CoreextensionPackage;
import ac.soton.eventb.emf.core.extension.coreextension.EventBEventGroup;
import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;


/**
 * Elaborates property section for EventBEventGroup.
 * 
 * @author cfsnook
 *
 */
public class ElaboratesPropertySection extends AbstractTablePropertySection {
	
	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBEventGroup;
		}
	}
	
	
	private Button creAddButton;
	private Button remDelButton;
	private Button addRefinesButton;
	private Button remRefinesButton;
	
	private static ILabelProvider eventLabelProvider = new LabelProvider() {

		@Override
		public String getText(Object element) {
			return ((Event) element).getName();
		}};

	@Override
	protected String getButtonLabelText() {
		return "Event";
	}

	@Override
	protected EList<Event> getOwnedRows() {
		return ((EventBEventGroup) eObject).getElaborates();
	}

	@Override
	protected EStructuralFeature getFeature() {
		return CoreextensionPackage.Literals.EVENT_BEVENT_GROUP__ELABORATES;
	}

	@Override
	protected List<String> getValuesForRow(Object object) {
		List<String> values = new ArrayList<String>();
		values.add(((Event) object).getName());
		values.add(((Event) object).getRefinesNames().toString().substring(1).replace("]",""));		
		return values;
	}

	@Override
	protected List<String> getColumnLabelText() {
		List<String> values = new ArrayList<String>();
		values.add("Event");
		values.add("Refines");		
		return values;
	}

	@Override
	protected Object getNewChild() {
		EObject container = EcoreUtil.getRootContainer(eObject);
		Machine machine = (Machine) container;
		PopupDialog eventsDialog = new PopupDialog(getPart().getSite().getShell(), machine.getEvents(), eventLabelProvider);
		eventsDialog.setTitle(machine.getName() + " Events");
		eventsDialog.setMessage("Please select events to elaborate");
		if (Dialog.OK == eventsDialog.open()) {
			Object[] result = eventsDialog.getResult();
			if (result.length > 0) {
				List<Event> events = new ArrayList<Event>();
				for (Object obj : result)
					events.add((Event) obj);
				return events;
			}
		}
		return null;
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
		removeButton.setText("Remove Event");
		
		// a new button to create eventB event and add it to elaborates
		creAddButton = getWidgetFactory().createButton((Composite) children[0], "Create && Add", SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(removeButton, 0);
		data.bottom = new FormAttachment(100, 0);
		creAddButton.setLayoutData(data);
		creAddButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				// remember selection
				int idx = table.getSelectionIndex();
				
				// create and add new event
				EObject container = EcoreUtil.getRootContainer(eObject);
				Machine machine = (Machine) container;
				NewEventDialog dialog = new NewEventDialog(getPart().getSite().getShell(), machine, null);
				if (Dialog.OK == dialog.open()) {
					EObject newChild = dialog.getEvent();
					if (newChild == null)
						return;
					EditingDomain editingDomain = ((DiagramEditor) getPart())
						.getEditingDomain();
					CompoundCommand cc = new CompoundCommand("Add new event for elaborates");
					// new event
					cc.append(AddCommand.create(editingDomain, machine, MachinePackage.Literals.MACHINE__EVENTS, newChild));
					// elaborate
					cc.append(AddCommand.create(editingDomain, eObject, getFeature(), newChild));
					editingDomain.getCommandStack().execute(cc);
					
					refresh();
					
					// restore selection
					table.select(idx);
					table.notifyListeners(SWT.Selection, new org.eclipse.swt.widgets.Event());
				}
				
			}
		});
		
		// a new button to create eventB event and add it to elaborates
		remDelButton = getWidgetFactory().createButton((Composite) children[0], "Remove && Delete", SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(creAddButton, 0);
		data.bottom = new FormAttachment(100, 0);
		remDelButton.setLayoutData(data);
		remDelButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				EditingDomain editingDomain = ((DiagramEditor) getPart()).getEditingDomain();
				Object object = table.getSelection()[0].getData();
				EList<EObject> newValues = new BasicEList<EObject>();
				Iterator<EObject> it = ((EList) eObject.eGet(getFeature())).iterator();
				for (; it.hasNext(); ) {
					EObject value = it.next();
					if (!value.equals(object))
						newValues.add(value);
				}
				editingDomain.getCommandStack().execute(
						SetCommand.create(editingDomain, eObject, getFeature(), newValues));
				//FIXME: RemoveCommand is preferred, but it causes non-containment references to be removed along with !originals!
//					RemoveCommand.create(editingDomain, eObject, getFeature(), object));
				refresh();

				//delete the event from the machine
				EObject container = EcoreUtil.getRootContainer(eObject);
				Machine machine = (Machine) container;
				if (object instanceof Event && machine instanceof Machine){
					editingDomain.getCommandStack().execute(
						RemoveCommand.create(editingDomain, machine, MachinePackage.Literals.MACHINE__EVENTS , object));
					refresh();
				}
			}
		});

		
		// button to add an event refines reference to an eventB event
		addRefinesButton = getWidgetFactory().createButton((Composite) children[0], "Add Refines", SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(remDelButton, 0);
		data.bottom = new FormAttachment(100, 0);
		addRefinesButton.setLayoutData(data);
		addRefinesButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				// remember selection
				int idx = table.getSelectionIndex();
				
				Object object = table.getSelection()[0].getData();
				if (!(object instanceof Event)) return;
				Event refinedEvent = (Event)object;
				if (((Machine) EcoreUtil.getRootContainer(eObject)).getRefines().isEmpty()){
					MessageDialog.openError(getPart().getSite().getShell(),
							"Modelling Error",
							"There are no events to refine because this is the most abstract machine");					
					return;
				}
				// add refines to event
				List<Event> eventList = new ArrayList<Event>();
				eventList.addAll( ((Machine) EcoreUtil.getRootContainer(eObject)).getRefines().get(0).getEvents());
				eventList.removeAll(refinedEvent.getRefines());
				Event newRefinedEvent = selectEvent(eventList, "Events of Abstract Machine", "Select new event to refine");
				if (newRefinedEvent == null) return;
				AddCommand addCommand = (AddCommand) AddCommand.create(getEditingDomain(), refinedEvent, MachinePackage.Literals.EVENT__REFINES, newRefinedEvent);
				getEditingDomain().getCommandStack().execute(addCommand);
				refresh();	
				
				// restore selection
				table.select(idx);
				table.notifyListeners(SWT.Selection, new org.eclipse.swt.widgets.Event());
			}
		});

		// button to remove an event refines reference to an eventB event
		remRefinesButton = getWidgetFactory().createButton((Composite) children[0], "Remove Refines", SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(addRefinesButton, 0);
		data.bottom = new FormAttachment(100, 0);
		remRefinesButton.setLayoutData(data);
		remRefinesButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent event) {
				// remember selection
				int idx = table.getSelectionIndex();
				
				Object object = table.getSelection()[0].getData();
				if (!(object instanceof Event)) return;
				Event refinedEvent = (Event)object;
				List<Event> eventList = new ArrayList<Event>();
				eventList.addAll(refinedEvent.getRefines());
				Event remRefinedEvent = selectEvent(eventList, "Refined Events", "Select refined event to remove");
				if (remRefinedEvent == null) return;
				eventList.remove(remRefinedEvent);
				getEditingDomain().getCommandStack().execute(
						SetCommand.create(getEditingDomain(), refinedEvent, MachinePackage.Literals.EVENT__REFINES, eventList));
				refresh();	
				
				// restore selection
				table.select(idx);
				table.notifyListeners(SWT.Selection, new org.eclipse.swt.widgets.Event());
			}
		});
		
	}

/*
 * Select event to be refined
 */
	private Event selectEvent(List<Event> eventList, String title, String instruction) {
		PopupDialog eventsDialog = new PopupDialog(getPart().getSite().getShell(), eventList, eventLabelProvider);
		eventsDialog.setTitle(title);
		eventsDialog.setMessage(instruction);
		if (Dialog.OK == eventsDialog.open()) {
			Object[] result = eventsDialog.getResult();
			return (Event)result[0];	//currently PopupDialog only allows a single event to be selected at a time
//			if (result.length > 0) {
//				List<Event> events = new ArrayList<Event>();
//				for (Object obj : result)
//					events.add((Event) obj);
//				return events;
//			}
		}
		return null;
	}
	
	@Override
	public void refresh() {
		super.refresh();
		remDelButton.setEnabled(false);
		addRefinesButton.setEnabled(false);
		remRefinesButton.setEnabled(false);
	}
	
	@Override
	public void rowSelected(){
		super.rowSelected();
		remDelButton.setEnabled(true);
		addRefinesButton.setEnabled(true);
		remRefinesButton.setEnabled(true);
	}
}
