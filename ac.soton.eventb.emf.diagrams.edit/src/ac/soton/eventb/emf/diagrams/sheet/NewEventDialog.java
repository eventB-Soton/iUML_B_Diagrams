/**
 * Copyright (c) 2010 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.sheet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.core.machine.Machine;
import org.eventb.emf.core.machine.MachineFactory;


/**
 * New event creation dialog.
 * 
 * @author vitaly
 *
 */
public class NewEventDialog extends Dialog {

	private Text nameText;
	private Text commentText;
	private Combo refinesCombo;
	private DecoratedInputValidator nameValidator;
	private boolean nameValid;
	private Event event;
	private String initialName = null;
	private Set<String> conflictingNames = null;
	private Map<String, Event> refinesNames = null;

	/**
	 * @param parentShell
	 */
	protected NewEventDialog(Shell parentShell) {
		super(parentShell);
	}
	
	/**
	 * @param parentShell
	 * @param machine to check for conflicts with existing events, or null if not required
	 * @param name proposed initial name for event, or null
	 */
	protected NewEventDialog(Shell parentShell, Machine machine, String name) {
		super(parentShell);
		
		// event's proposed name
		if (name != null)
			initialName = name;
		
		// build up a set of existing names
		if (machine != null) {
			conflictingNames = new HashSet<String>(machine.getEvents().size());
			for (Event e : machine.getEvents())
				conflictingNames.add(e.getName());
			if (!machine.getRefines().isEmpty()) {
				refinesNames = new HashMap<String, Event>();
				for (Event event : machine.getRefines().get(0).getEvents())
					refinesNames.put(event.getName(), event);
			}
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add New Event");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setText("Event");
		GridLayout layout = new GridLayout(2, false);
		group.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 300;
		group.setLayoutData(gd);
		
		// name
		Label nameLabel = new Label(group, SWT.NONE);
		nameLabel.setText("Name:");
		nameText = new Text(group, SWT.SINGLE | SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		nameText.setFont(PropertySectionUtil.rodinFont);
		nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validateName();
            }
        });
		
		// refines
		if (refinesNames != null) {
			Label refinesLabel = new Label(group, SWT.NONE);
			refinesLabel.setText("Refines:");
			refinesCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
			refinesCombo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
			String[] items = new String[refinesNames.size() + 1];
			int i = 0;
			items[i++] = ""; // empty item for non-refinement
			for (String name : refinesNames.keySet())
				items[i++] = name;
			Arrays.sort(items);
			refinesCombo.setItems(items);
		}
		
		// comment
		Label commentLabel = new Label(group, SWT.NONE);
		commentLabel.setText("Comment:");
		commentText = new Text(group, SWT.SINGLE | SWT.BORDER);
		commentText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		commentText.setFont(PropertySectionUtil.rodinFont);
		
		// validator
		nameValidator = new DecoratedInputValidator(PropertySectionUtil.createDecorator(nameText, "Please enter name", FieldDecorationRegistry.DEC_ERROR, false)) {
			
			@Override
			public String isValidInput(String name) {
				if (name == null || name.trim().isEmpty())
					return "Name cannot be empty";
				if (conflictingNames != null && conflictingNames.contains(name.trim()))
					return "Event with such name already exists";
				return null;
			}
		};
		
		// initial name
		if (initialName != null)
			nameText.setText(initialName);
		
		return composite;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		// only here the buttons are available, so that's where OK can be disabled for initial state
		update();
		return contents;
	}

	/**
	 * Validates name.
	 */
	protected void validateName() {
		String errorMessage = null;
        if (nameValidator != null) {
            errorMessage = nameValidator.isValid(nameText.getText());
            nameValid = errorMessage == null;
        }
		update();
	}

	private void update() {
		Control button = getButton(IDialogConstants.OK_ID);
		if (button != null) {
			button.setEnabled(nameValid);
		}
	}
	
	/**
	 * Returns event created by this dialog.
	 * 
	 * @return new event
	 */
	public Event getEvent() {
		return event;
	}

	@Override
	protected void okPressed() {
		event = MachineFactory.eINSTANCE.createEvent();
		event.setName(nameText.getText().trim());
		event.setComment(commentText.getText());
		if (refinesNames != null && refinesCombo.getSelectionIndex() > 0)
			event.getRefines().add(refinesNames.get(refinesCombo.getItem(refinesCombo.getSelectionIndex())));
		
		//FIXME: annotating the event here prevents persistence from throwing "Workspace modify operation"
		// when it's adding annotation in AbstractSynchroniser; this is a temporary solution
		//UPDATE Nov'16 - this doesn't seem to be needed anymore.. remove dependency on persistence plugin
		//annotate(event);
		
		super.okPressed();
	}
	
//	private void annotate(EventBObject element) {
//		Annotation rodinInternals = CoreFactory.eINSTANCE.createAnnotation();
//		rodinInternals.setSource("http:///org/eventb/core/RodinInternalAnnotations"); //avoid dependency on persistence.. PersistencePlugin.SOURCE_RODIN_INTERNAL_ANNOTATION);
//		element.getAnnotations().add(rodinInternals);
//	}

}
