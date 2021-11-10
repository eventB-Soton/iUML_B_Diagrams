/*******************************************************************************
 * Copyright (c) 2014, 2020 University of Southampton.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.common.ui.dialogs.PopupDialog;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBObject;
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
public class ElaboratesPropertySection extends AbstractEditTableWithReferencedObjectCreationDeletionPropertySection {
	
	/**
	 * Element Filter for this property section.
	 */
	public static final class Filter implements IFilter {
		@Override
		public boolean select(Object toTest) {
			return DiagramUtils.unwrap(toTest) instanceof EventBEventGroup;
		}
	}

	@Override
	protected EReference getFeature() {
		return CoreextensionPackage.Literals.EVENT_BEVENT_GROUP__ELABORATES;
	}

	@Override
	protected Object getFeatureForCol(int col) {
		switch (col) {
		case 0 : return CorePackage.eINSTANCE.getEventBNamed_Name();
		case 1 : return MachinePackage.eINSTANCE.getEvent_Extended();
		case 2 : return MachinePackage.eINSTANCE.getEvent_Convergence();
		case 3 : return MachinePackage.eINSTANCE.getEvent_Refines();
		case 4 : return CorePackage.eINSTANCE.getEventBCommented_Comment();
		default : return null;
		}
	}
	
	@Override
	protected boolean isMulti(final int col){
		return col==4;
	}
	
	@Override
	protected boolean isReadOnly(final int col) {
		return  col==3;		//use buttons for refines
	}

	@Override
	protected String getButtonLabelText() {
		return "Event";
	}

	@Override
	protected String getLabelText() {
		return "Elaborates:";
	}
	
	@Override
	protected Object createNewElement(){
		EObject newEvent = null;
		// create and add new element
		EventBObject target = getTranslationTarget();
		Machine machine = target instanceof Machine? (Machine) target : null;
		NewEventDialog dialog = new NewEventDialog(getPart().getSite().getShell(), machine, null);
		if (Dialog.OK == dialog.open()) {
			newEvent = dialog.getEvent();
			if (newEvent != null) {
				EditingDomain editingDomain = ((DiagramEditor) getPart()).getEditingDomain();
				CompoundCommand cc = new CompoundCommand("Add new event for elaborates");
				cc.append(AddCommand.create(editingDomain, machine, MachinePackage.Literals.MACHINE__EVENTS, newEvent));
				editingDomain.getCommandStack().execute(cc);
			}
		}
		return newEvent;
	}

	private Button addRefinesButton;
	private Button remRefinesButton;
	
	@Override
	protected FormAttachment moreButtons(FormAttachment buttonLeftData, FormAttachment buttonTopData, FormAttachment buttonBottomData){
		
		Control[] children = parent.getChildren();
		FormAttachment leftData = super.moreButtons(buttonLeftData, buttonTopData, buttonBottomData);
				
		// button to add an event refines reference to an eventB event
		addRefinesButton = getWidgetFactory().createButton((Composite) children[0], "Add Refines", SWT.PUSH);
		FormData data = new FormData();
		data.left = leftData;
		data.bottom = buttonBottomData;
		data.top = buttonTopData;
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
		data.bottom = buttonBottomData;
		data.top = buttonTopData;
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
		return new FormAttachment(remRefinesButton, ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
	}

	private static ILabelProvider eventLabelProvider = new LabelProvider() {
		@Override
		public String getText(Object element) {
			return ((Event) element).getName();
		}};
		
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
		addRefinesButton.setEnabled(false);
		remRefinesButton.setEnabled(false);
	}
	
	@Override
	protected void rowSelectionAction(){
		super.rowSelectionAction();
		addRefinesButton.setEnabled(true);
		remRefinesButton.setEnabled(true);
	}

}
