/**
 * Copyright (c) 2010 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.sheet;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eventb.emf.core.Annotation;
import org.eventb.emf.core.CoreFactory;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBNamedCommentedElement;
import org.eventb.emf.core.EventBObject;
import org.eventb.emf.core.context.CarrierSet;
import org.eventb.emf.core.context.Constant;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.context.ContextFactory;
import org.eventb.emf.core.context.ContextPackage;
import org.eventb.emf.core.machine.Machine;
import org.eventb.emf.core.machine.MachineFactory;
import org.eventb.emf.core.machine.MachinePackage;
import org.eventb.emf.core.machine.Variable;
import org.eventb.emf.persistence.PersistencePlugin;

import ac.soton.eventb.emf.core.extension.coreextension.DataKind;


/**
 * New data creation dialog.
 * 
 * @author cfsnook
 *
 */
public class NewDataDialog extends Dialog {

	private Combo componentCombo;
	private Combo dataKindCombo;
	private String defaultName;
	private Text nameText;
	private Text commentText;
	private DecoratedInputValidator nameValidator;
	private DecoratedInputValidator dataKindValidator;
	private boolean nameValid;
	private boolean dataKindValid;
	private EventBNamedCommentedElement newElement;
	private Set<String> conflictingNames = new HashSet<String>(); //Collections.emptySet();
	private EventBNamedCommentedComponentElement component;
	private List<EventBNamedCommentedComponentElement> componentList;
	private EReference feature;
	private DataKind defaultDataKind;
	protected SelectionListener comboListener;	


	/**
	 * @param parentShell
	 * @param component to check for conflicts with existing data, or null if not required
	 * @param default name proposed for data, or null
	 */
	protected NewDataDialog(Shell parentShell, List<EventBNamedCommentedComponentElement> componentList, DataKind defaultDataKind,
			ILabelProvider dataLabelProvider,  String defaultName) {
		super(parentShell);
		this.defaultName = defaultName;
		this.componentList = componentList;
		this.defaultDataKind = defaultDataKind;
		//for (EventBNamed e : list) conflictingNames.add(dataLabelProvider.getText(e));
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Add New Data Element");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		Group group = new Group(composite, SWT.SHADOW_ETCHED_IN);
		group.setText("Data");
		GridLayout layout = new GridLayout(2, false);
		group.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.widthHint = 300;
		group.setLayoutData(gd);
		
		 comboListener= 
					new SelectionListener() {
						@Override
						public void widgetSelected(SelectionEvent e) {
							validate();
						}
						@Override
						public void widgetDefaultSelected(SelectionEvent e) {
							validate();
						}
		 };
		 
		//component
		Label componentLabel = new Label(group, SWT.NONE);
		componentLabel.setText("Parent:");
		componentCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		componentCombo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		String[] items = new String[componentList.size()];
		int i = 0;
		for (EventBNamedCommentedComponentElement component : componentList)
			items[i++] = component.getName();
		componentCombo.setItems(items);
		componentCombo.setText(items[0]);
		componentCombo.addSelectionListener(comboListener);
		
		Label dataKindLabel = new Label(group, SWT.NONE);
		dataKindLabel.setText("Data kind:");
		dataKindCombo = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY);
		dataKindCombo.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		String[] items1 = new String[DataKind.VALUES.size()];
		int i1 = 0;
		for (DataKind dk : DataKind.VALUES)
			items1[i1++] = dk.getLiteral();
		dataKindCombo.setItems(items1);
		dataKindCombo.setText(defaultDataKind.getLiteral());
		dataKindCombo.addSelectionListener(comboListener);
		
		// name
		Label nameLabel = new Label(group, SWT.NONE);
		nameLabel.setText("Name:");
		nameText = new Text(group, SWT.SINGLE | SWT.BORDER);
		nameText.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL));
		nameText.setFont(PropertySectionUtil.rodinFont);
		nameText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validate();
            }
        });
		
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
				conflictingNames = getConflictingNames(componentList.get(componentCombo.getSelectionIndex()));
				if (conflictingNames != null && conflictingNames.contains(name.trim()))
					return "Already exists!";
				return null;
			}
		};
		
		// validator
		dataKindValidator = new DecoratedInputValidator(PropertySectionUtil.createDecorator(dataKindCombo, "Please select a data kind", FieldDecorationRegistry.DEC_ERROR, false)) {
			@Override
			public String isValidInput(String name) {
				EClass componentType = componentList.get(componentCombo.getSelectionIndex()).eClass();
				DataKind dk = DataKind.get(dataKindCombo.getSelectionIndex());
				if (componentType == MachinePackage.Literals.MACHINE && 
						dk != DataKind.VARIABLE){
					return "Data Kind must be Variable when a Machine is selected";
				}else if (componentType == ContextPackage.Literals.CONTEXT && 
						dk != DataKind.SET && dk != DataKind.CONSTANT){
					return "Data Kind must be Carrier Set or Constant when a Context is selected";
				}else{
					return null;	//no error found
				}
			}
		};
		
		// initial name
		if (defaultName != null)
			nameText.setText(defaultName);
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
	protected void validate() {
		String errorMessage = null;
        if (nameValidator != null) {
            errorMessage = nameValidator.isValid(nameText.getText());
            nameValid = errorMessage == null;
        }
        if (dataKindValidator != null) {
        	errorMessage = dataKindValidator.isValid("");
        	dataKindValid = errorMessage == null;
        }
		update();
	}
	
	private void update() {
		Control button = getButton(IDialogConstants.OK_ID);
		if (button != null) {
			button.setEnabled(nameValid && dataKindValid);
		}
	}
	
	/**
	 * Returns element created by this dialog.
	 * 
	 * @return new event
	 */
	public EventBNamedCommentedElement getElement() {
		return newElement;
	}

	@Override
	protected void okPressed() {
		component = componentList.get(componentCombo.getSelectionIndex());
		DataKind dk = DataKind.get(dataKindCombo.getSelectionIndex());
		feature = 
			component.eClass() == MachinePackage.Literals.MACHINE ? MachinePackage.Literals.MACHINE__VARIABLES 
			:	component.eClass() == ContextPackage.Literals.CONTEXT ?
					dk == DataKind.SET ? ContextPackage.Literals.CONTEXT__SETS 
					:	dk == DataKind.CONSTANT ? ContextPackage.Literals.CONTEXT__CONSTANTS 
						:	null
				: 	null;
		
		if (feature == ContextPackage.Literals.CONTEXT__SETS){
			newElement = ContextFactory.eINSTANCE.createCarrierSet();
		}else if (feature == ContextPackage.Literals.CONTEXT__CONSTANTS){
			newElement = ContextFactory.eINSTANCE.createConstant();
		}else{
			newElement = MachineFactory.eINSTANCE.createVariable(); 
		}
		newElement.setName(nameText.getText().trim());
		newElement.setComment(commentText.getText());
		
		//FIXME: annotating the element here prevents persistence from throwing "Workspace modify operation"
		// when it's adding annotation in AbstractSynchroniser; this is a temporary solution
		annotate(newElement);
		
		super.okPressed();
	}
	
	private void annotate(EventBObject element) {
		Annotation rodinInternals = CoreFactory.eINSTANCE.createAnnotation();
		rodinInternals.setSource(PersistencePlugin.SOURCE_RODIN_INTERNAL_ANNOTATION);
		element.getAnnotations().add(rodinInternals);
	}
	
	private Set<String> getConflictingNames(EventBNamedCommentedComponentElement container) {
		Set<String> list =  new HashSet<String>() ;
		if (container instanceof Machine){
			Machine m = ((Machine)container);
			for (Variable v : m.getVariables()){
				list.add(v.getName());
			}
			for (Context c : m.getSees()){
				list.addAll(getConflictingNames(c));
			}			
		}else if (container instanceof Context){
			Context c = ((Context)container);
			for (CarrierSet s : c.getSets()){
				list.add(s.getName());
			}
			for (Constant s : c.getConstants()){
				list.add(s.getName());
			}
			for (Context x : c.getExtends()){
				list.addAll(getConflictingNames(x));
			}
		}
		return list;
	}

	public Object getComponent() {
		return component;
	}

	public Object getFeature() {
		return feature;
	}

}
