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

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.UnexecutableCommand;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EEnumLiteral;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.change.util.ChangeRecorder;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.edit.command.MoveCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gmf.runtime.diagram.ui.properties.views.TextChangeHelper;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBCommented;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.Project;
import org.eventb.emf.core.util.NameUtils;
import org.rodinp.keyboard.ui.RodinKeyboardUIPlugin;
import org.rodinp.keyboard.ui.preferences.PreferenceConstants;



/**
 * Abstract property section displaying items as a table where the table entries can be edited directly
 *
 *
 */

public abstract class AbstractEditTablePropertySection extends AbstractIumlbPropertySection { 

	protected Composite parent;
	protected boolean singular;
	protected boolean createControlsCompleted;
	final int TEXT_MARGIN = 3;

	protected abstract EStructuralFeature getFeature();

	protected abstract Object getFeatureForCol(int col);

	protected String getTableLabel(){ return null;}

	protected String getNewChildrenDialogTitle(){
		String elementKind = getFeature().getEType().getName();
		String featureName = getFeature().getName();
		return "Select new "+elementKind+"s to be added to "+featureName;
	}

	protected String getButtonLabelText(){
		String label = "<unknown element>";
		label= getFeature().getName();
		if (label.endsWith("s")) label=label.substring(0,label.length()-1);
		label=label.substring(0,1).toUpperCase()+label.substring(1);
		return label;
	}

	protected List<String> getValuesForRow(final Object object) {
		ArrayList<String> ret = new ArrayList<String>();
		int col=0;
		Object feature=null;
		if (object instanceof EObject){
			while ((feature=getFeatureForCol(col++)) != null){
				String value = "<unknown feature>";
				if (feature instanceof EStructuralFeature){
					Object featureValue = ((EObject)object).eGet((EStructuralFeature)feature);
					if (featureValue instanceof String){
						value=(String)featureValue;
					}else if (featureValue instanceof List){
						value = "";
						for (Object element : (List<?>)featureValue) {
							if (element instanceof EventBNamed) {
								if (value!="") value=value+",";
								value = value+((EventBNamed)element).getName();
							}
						}
					}else if (featureValue instanceof EventBNamed){
						value = ((EventBNamed)featureValue).getName();
					}else if (featureValue instanceof Boolean){
						value = ((Boolean)featureValue).toString();
					}else if (featureValue instanceof Enumerator){
						value = ((Enumerator)featureValue).getLiteral();
					}else if (featureValue instanceof Integer){
						value = ((Integer)featureValue).toString();
					}else if (featureValue == null){
						value = "<null>";
					}

				}
				ret.add(value);
			}
		}else if (object instanceof String){
			ret.add((String)object);
		}else if (object instanceof Boolean){
//			EDataType eDataType = null; //((EDataType) object).eClass();
//			String instanceValue = EcoreFactory.eINSTANCE.convertToString(eDataType, object);
//			ret.add(instanceValue);
		}
		return ret;
	}

    /**
  	 * By default all text fields are treated as NOT a Rodin Keyboard.
	 * This method should be overridden to return true for the columns
	 *  for which the Rodin key combo modification is required.
     * 
     * @param col
     * @return whether this column is a text field that should be treated as a Rodin Keyboard 
     *
     */
    protected boolean isRodinKeyboard(final int col){
    	return false;
    }
    
	protected List<String> getColumnLabelText() {
		ArrayList<String> ret = new ArrayList<String>();
		int col=0;
		Object feature;
		while ((feature=getFeatureForCol(col++)) != null){
			String label = "<unknown feature>";
			if (feature instanceof EStructuralFeature){
				label= ((EStructuralFeature)feature).getName();
				label=label.substring(0,1).toUpperCase()+label.substring(1);
			}
			ret.add(label);
		}
		return ret;
	}

	protected Object getNewValue(){
		if (getFeature() instanceof EReference){
			if (((EReference)getFeature()).isContainment()){
				EClass eClass = ((EReference)getFeature()).getEReferenceType();
				return eClass.getEPackage().getEFactoryInstance().create(eClass);
			}else{
				return getNewReferences();
			}
		}else if (getFeature() instanceof EAttribute){
			EDataType eDataType = ((EAttribute)getFeature()).getEAttributeType();
			String literalValue = getNewDataValue();
			return EcoreFactory.eINSTANCE.createFromString(eDataType, literalValue);
		}else
			return null;
	}

	protected Object[] getNewReferences(){
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(shell,new BLabelProvider());
		dialog.setFilter(null);
		dialog.setElements(getPossibleChildren().toArray());
		dialog.setTitle(getNewChildrenDialogTitle());
		dialog.create();
		dialog.open();
		if (dialog.getReturnCode() == Window.CANCEL) return null;
		return dialog.getResult();
	}

	protected String getNewDataValue() {
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		InputDialog inputDialog = new InputDialog(shell, "Add new "+getFeature().getName(), "Input new value for "+getFeature().getName(), "", null);
		inputDialog.create();
		inputDialog.open();
		return inputDialog.getValue();
	}

	protected EList<EObject> getPossibleChildren() {
		if (!(getFeature() instanceof EReference)) return null;
		EReference feature = (EReference) getFeature();
		EClass eClass = feature.getEReferenceType();
		Project project = (Project) owner.getContaining(CorePackage.eINSTANCE.getProject());
		EList<EObject> possibles = project.getAllContained(eClass, false);
		possibles.removeAll(this.getElements());
		return possibles;
	}

	protected List<?> getPossibleValues(final int col){
		Object feature = getFeatureForCol(col);
		if (feature instanceof EReference){
			EClass eClass = ((EReference)feature).getEReferenceType();
			EventBNamedCommentedComponentElement container = (EventBNamedCommentedComponentElement) owner.getContaining(CorePackage.eINSTANCE.getEventBNamedCommentedComponentElement());
			return container.getAllContained(eClass, true);
		}else if (feature instanceof EAttribute) {
			EDataType dataType = ((EAttribute)feature).getEAttributeType();
			if (dataType instanceof EEnum){
				return ((EEnum)dataType).getELiterals();
			}else if (dataType.getName().equals("EBoolean")){
				List<Object> l = new ArrayList<Object>(); l.add(true); l.add(false); return l;
			}else return null;
		}else if (feature instanceof EDataType){
			EDataType dataType = (EDataType) feature;
			if (dataType instanceof EEnum){
				return ((EEnum)dataType).getELiterals();
			}else if (dataType.getName().equals("EBoolean")){
				List<Object> l = new ArrayList<Object>(); l.add(true); l.add(false); return l;
			}else return null;
		}else{
			return null;
		}
	}

	protected Object getNewValue(final int col, final int index) {
		Object newValue = null;
		List<?> possVals = getPossibleValues(col);
		if (possVals!=null) newValue=possVals.get(index);
		if (newValue instanceof EEnumLiteral) newValue = ((EEnumLiteral)newValue).getInstance();
		return newValue;
	}

	protected List<? extends Object> getElements() {
		ArrayList<Object> ret= new ArrayList<Object>();
		Object featureValue = owner.eGet(getFeature());
		if (featureValue instanceof List)
			for (Object element : (List<?>)featureValue) {
				ret.add(element);
			}
		else if (featureValue instanceof Object){
			ret.add(featureValue);
		}
		return ret;
	}

	//the default column widths may be overridden in extensions
	protected int columnWidth(final int col){
		return 150;
	}

	//a column can be made read-only
	protected boolean isReadOnly(final int col){
		return false;
	}

	//a row can be made read-only
	protected boolean isReadOnly(final Object object){
		return false;
	}

	private class BLabelProvider extends LabelProvider{
		@Override
		public String getText(final Object element){
			if (element==null) return "<null>";
			else if (element instanceof EventBNamed) return ((EventBNamed)element).getName();
			return "<unknown element>";
		}
		@Override
		public org.eclipse.swt.graphics.Image getImage(final Object element){
			if (element==null) return null;
			else if (element instanceof EventBElement) {
				//List adapters = ((EventBElement)element).eAdapters();
				return null;
			}
			else return null;
		}
	}

	protected void addButtonAction() {
		EditingDomain editingDomain = ((DiagramDocumentEditor) getPart()).getEditingDomain();
		Command command;
		Object newValue = getNewValue();
		if (newValue == null) {
			command = UnexecutableCommand.INSTANCE;
		}else{
			if (singular){
				command = SetCommand.create(editingDomain, owner, getFeature(), newValue);
			}else{
				command = AddCommand.create(editingDomain, owner, getFeature(), newValue);
			}
			if (newValue instanceof EventBCommented){
				((EventBCommented)newValue).setComment("");
			}
		}
		editingDomain.getCommandStack().execute(command);
		refresh();
	}

	protected void removeButtonAction() {
		EditingDomain editingDomain = ((DiagramDocumentEditor) getPart()).getEditingDomain();
		Object object = table.getSelection()[0].getData();
		if (singular){
			editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, owner, getFeature(), SetCommand.UNSET_VALUE));
		}else{
			editingDomain.getCommandStack().execute(RemoveCommand.create(editingDomain, owner, getFeature(), object));
		}
		refresh();
	}

	protected void upButtonAction() {move(-1);}

	protected void downButtonAction() {move(1);}

	private void move(final int movement){
		EditingDomain editingDomain = ((DiagramDocumentEditor) getPart()).getEditingDomain();
		Object object = table.getSelection()[0].getData();
		Object featureValue = owner.eGet(getFeature());
		if (featureValue instanceof EList){
			int pos = ((EList<?>)featureValue).indexOf(object)+movement;
			if (pos<0 || pos>((EList<?>)featureValue).size()-1) return;
			editingDomain.getCommandStack().execute(MoveCommand.create(editingDomain, owner, getFeature(), object, pos));
			refresh();					//redraw the new table
			table.select(pos);			//reselect the same object ready for another move
			rowSelectionAction();		//do the actions needed for a selection event
		}
	}


	protected Table table;
	protected List<TableColumn> columns;
	protected Button addButton;
	protected Button removeButton;
	protected Button upButton;
	protected Button downButton;
	protected Button seeErrorsButton;

	private Combo combo=null;
	private Text text= null;
	private final ModifyListener rodinKbdListener = RodinKeyboardUIPlugin.getDefault().createRodinModifyListener();
	private final Font font = JFaceResources.getFont(PreferenceConstants.RODIN_MATH_FONT);
	private TableEditor editor;


	private List<String> getValuesForRow(final int row) {
		return getValuesForRow(getElements().get(row));
	}

	@Override
	public void createControls(final Composite parent, final TabbedPropertySheetPage tabbedPropertySheetPage) {
		super.createControls(parent, tabbedPropertySheetPage);
		this.parent = parent;
		createControlsCompleted=false;
	}

	/**
	 * Some tables need to know the owner when creating the table. Since the selection is not always set (in the supertype)
	 * when createControls is called, the main creation of controls is delayed until refresh is invoked.
	 *
	 **/

	private void doCreateControls(){
		singular = getFeature().getUpperBound()==1;
		Composite composite = getWidgetFactory().createFlatFormComposite(parent);
		FormData data;
		table = getWidgetFactory().createTable(composite,SWT.FILL |SWT.FULL_SELECTION);
		createTable();
		table.setFont(JFaceResources.getFont(PreferenceConstants.RODIN_MATH_FONT));
		editor= new TableEditor (table);
	    editor.grabHorizontal = true;
	    
		Shell shell = new Shell();
		GC gc = new GC(shell);
		gc.setFont(shell.getFont());
		Point point = gc.textExtent("");
		int buttonHeight = point.y + 11;
		gc.dispose();
		shell.dispose();

		FormAttachment leftData = null;

		//label
		if (getTableLabel()!=null){
			CLabel nameLabel = getWidgetFactory().createCLabel(composite,
				getTableLabel());
			data = new FormData();
			data.left = new FormAttachment(0, 0);
			data.right = new FormAttachment(table, -ITabbedPropertyConstants.HSPACE);
			data.top = new FormAttachment(table, 0, SWT.CENTER);
			nameLabel.setLayoutData(data);
			leftData = new FormAttachment(0, getStandardLabelWidth(composite, new String[] {getTableLabel()}));
		}else{
			leftData = new FormAttachment(0, 0);
		}

		FormAttachment buttonTopData = null;
		FormAttachment buttonBottomData = null;
		if (singular){
			buttonTopData = new FormAttachment(table,0);
			buttonBottomData = new FormAttachment(table, buttonHeight+6, SWT.BOTTOM);
		}else{
			buttonTopData = new FormAttachment(100, -buttonHeight);
			buttonBottomData = new FormAttachment(100, 0);
		}

		//add button
		addButton = getWidgetFactory().createButton(composite,
			MessageFormat.format("Add {0}", new Object[] {getButtonLabelText()}), SWT.PUSH);
		data = new FormData();
		data.left = leftData;
		data.bottom = buttonBottomData;
		data.top = buttonTopData;
		addButton.setLayoutData(data);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {addButtonAction();}
		});

		//delete button
		removeButton = getWidgetFactory().createButton(composite,
			MessageFormat.format("Delete {0}", new Object[] {getButtonLabelText()}), SWT.PUSH);
		data = new FormData();
		data.left = new FormAttachment(addButton, ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
		data.bottom = buttonBottomData;
		data.top = buttonTopData;
		removeButton.setLayoutData(data);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {removeButtonAction();}
		});
		Button lastButton=removeButton;

		//move buttons
		if (!singular){
			//up button
			upButton = getWidgetFactory().createButton(composite,
					MessageFormat.format("Move Up", new Object[] {}), SWT.PUSH);
			data = new FormData();
			data.left = new FormAttachment(lastButton, ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
			data.bottom = buttonBottomData;
			data.top = buttonTopData;
			upButton.setLayoutData(data);
			upButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent event) {upButtonAction();}
			});
			//down button
			downButton = getWidgetFactory().createButton(composite,
					MessageFormat.format("Move Down", new Object[] {}), SWT.PUSH);
			data = new FormData();
			data.left = new FormAttachment(upButton, ITabbedPropertyConstants.VSPACE, SWT.BOTTOM);
			data.bottom = buttonBottomData;
			data.top = buttonTopData;
			downButton.setLayoutData(data);
			downButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent event) {downButtonAction();}
			});
			lastButton=downButton;
		}

		//main table layout and row selection
		data = new FormData();
		data.left=leftData;
		data.right = new FormAttachment(100, 0);
		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
		if (!singular){
			data.bottom = new FormAttachment(addButton,-ITabbedPropertyConstants.VSPACE);
		}
		data.width = 400;
		table.setLayoutData(data);
		table.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent event) {rowSelectionAction();}
		});

	}

	private void rowSelectionAction() {
		removeButton.setEnabled(true);
		if (upButton!=null) upButton.setEnabled(true);
		if (downButton!=null) downButton.setEnabled(true);
	}

	@Override
	public boolean shouldUseExtraSpace() {
		return true;
	}

	private void createTable() {
		  table.setHeaderVisible(true);
		  table.setLinesVisible(true);
		  createColumns();
		  table.addListener(SWT.MouseUp, tableMouseListener);
		  table.addDisposeListener(disposeListener);
		  
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

	}

	private void createColumns(){
		List<String> labels = getColumnLabelText();
		columns = new ArrayList<TableColumn>();
		int col=0;
		for (String label : labels) {
			TableColumn column = new TableColumn(table, SWT.NONE);
			column.setText(label);
			columns.add(column);
			column.setWidth(columnWidth(col));
			col++;
		}
	}

	@Override
	public void refresh() {
		if (createControlsCompleted==false) doCreateControls();
		createControlsCompleted=true;
		if (combo!=null) combo.dispose();
		if (text!=null) text.dispose();
		table.removeAll();
		table.redraw();
		if (getFeature().getUpperBound() <0 ||
			getElements().size()<getFeature().getUpperBound()){
			addButton.setEnabled(true);
		}else{
			addButton.setEnabled(false);
		}
		removeButton.setEnabled(false);
		if (upButton!=null) upButton.setEnabled(false);
		if (downButton!=null) downButton.setEnabled(false);
		if (seeErrorsButton!=null) seeErrorsButton.setEnabled(false);
		for (Object next :getElements()) {
			// create the table item
			TableItem item = new TableItem(table, SWT.NONE);
			String[] values = new String[columns.size()];
			List<String> valuesForRow = getValuesForRow(next);
			for (int j = 0; j < columns.size(); j++)
				if (j<valuesForRow.size()) {
					values[j] = valuesForRow.get(j);
				}
				else
					values[j]="";
			item.setText(values);
			item.setData(next);
			if (isReadOnly(next)) item.setGrayed(true);
		}

		if (owner instanceof EventBElement && ((EventBElement)owner).isGenerated()){
			table.setEnabled(false);
			addButton.setEnabled(false);
		}else{
			table.setEnabled(true);
			addButton.setEnabled(true);
		}
		
		table.redraw();
	}

private final Listener tableMouseListener = new Listener() {
	TableItem tableItem = null;
	int row = 0;
	int column = 0;


    public void handleEvent(final Event event) {
		if (combo!=null) combo.dispose();
		textChangeListener.stopListeningTo(text);
		if (text!=null) text.dispose();
		text=null;
		row = table.getTopIndex();
		boolean found=false;
		while (row < table.getItemCount()) {
			tableItem = table.getItem(row);
			column=0;
		    found=false;
		    while (column<table.getColumnCount()){
			    Rectangle rect = tableItem.getBounds(column);
			    if (rect.contains(new Point(event.x, event.y))) {
			    	if (isReadOnly(column) || isReadOnly(tableItem.getData())){
			    		found=false; break;
			    	}else{
			    		found=true; break;
			    	}
			    }else if (!rect.intersects(table.getClientArea())) {
			    	found=false; break;
			    }
			    column++;
		    }
		    if (found==true) break;
		    else row++;
		}
		if (found==false) return;

		tableItem.setFont(font);
		if (getPossibleValues(column)==null) {	//if the list of names of possible values is null must be a text field
			int style;
			if (isMulti(column)) {
				style= SWT.MULTI;
			}else{
				style= SWT.NONE;
			}
			text = new Text(table, style);
			if (isRodinKeyboard(column)) {
				text.addModifyListener(rodinKbdListener);
			}
			text.setFont(font);
			textChangeListener.startListeningTo(text);
			if (!isMulti(column)){
				textChangeListener.startListeningForEnter(text);
			}
			editor.setEditor(text, tableItem, column);
			text.setText(tableItem.getText(column));
			text.selectAll();
			text.setFocus();
		}else{
			combo = new Combo(table, SWT.READ_ONLY);
			combo.setFont(font);
			combo.setItems(NameUtils.getNames(getPossibleValues(column)).toArray(new String[0]));
			combo.addSelectionListener(comboSelectionListener);
			editor.setEditor(combo, tableItem, column);
			combo.setText(tableItem.getText(column));
		}
    }
   

    private final TextChangeHelper textChangeListener = new TextChangeHelper() {
    	@Override
    	public void textChanged(final Control control) {
    		if (text.isDisposed()) return;
       		String newText = text.getText();
    		String oldText = getValuesForRow(row).get(column);
    		if (oldText==null || !oldText.equals(newText)){
    			if (getFeatureForCol(column).equals(CorePackage.eINSTANCE.getEventBNamed_Name())) {
    			}
    			tableItem.setText(column, newText);
    			handleTextChanged(column, row, newText);
    		}
    	}
    };

	SelectionListener comboSelectionListener = new SelectionListener() {
		public void widgetDefaultSelected(SelectionEvent arg0) {
			return;
		}
		public void widgetSelected(SelectionEvent e) {
			EditingDomain editingDomain = ((DiagramDocumentEditor) getPart()).getEditingDomain();
			Object object = getElements().get(row);
			if (combo.getSelectionIndex()!=-1){
				Object newValue = getNewValue(column, combo.getSelectionIndex());
				if (getFeatureForCol(column) !=null)
					editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, object, getFeatureForCol(column), newValue));
				String setText = newValue instanceof String ? (String) newValue :
						NameUtils.getNames(getPossibleValues(column)).get(combo.getSelectionIndex());
				tableItem.setText(column, setText );
			}
        	combo.dispose();
		}
	};

};

protected void handleTextChanged(int column, int row, String newText){
	EditingDomain editingDomain = ((DiagramDocumentEditor) getPart()).getEditingDomain();
	Object feature = getFeatureForCol(column);
	if (feature instanceof EStructuralFeature){
		Object object = getElements().get(row);
		Object newVal = newText;
		if (feature instanceof EAttribute && ((EAttribute) feature).getEType().getName().equals("EInt")){
			try {
				newVal = Integer.parseInt(newText);
			}catch (NumberFormatException e){
				MessageBox mb = new MessageBox(this.getPart().getSite().getShell());
				mb.setMessage("number format error");
				mb.open();
			}
		}
		editingDomain.getCommandStack().execute(SetCommand.create(editingDomain, object, feature, newVal));	
	}else if (getFeature() instanceof EAttribute && feature instanceof EDataType){
		editingDomain.getCommandStack().execute(new SetTextInListCommand(owner, getFeature(), column, newText));
	}
}

protected class SetTextInListCommand extends ChangeCommand {
	final EventBElement owner;
	final EStructuralFeature feature;
	final String  newText;
	final int index;
	SetTextInListCommand(EventBElement owner, EStructuralFeature feature, int index, String newText){
		super(new ChangeRecorder(owner).endRecording());
		this.owner = owner;
		this.feature = feature;
		this.newText = newText;
		this.index = index;
	}
	@SuppressWarnings("unchecked")
	@Override
	public void doExecute(){
		if (((EDataType)feature).getName().equals("EString")){
			((EList<String>)owner.eGet(feature)).set(index, newText);
		}else if (((EDataType)feature).getName().equals("EInt")){
			//Object val = owner.eGet(feature);
			//((EList<Int>)owner.eGet(feature)).set(index, newText);
		}
	}
}


private final DisposeListener disposeListener = new DisposeListener() {
	public void widgetDisposed(DisposeEvent e) {
		refresh();
	}
};

/** override to return true for the columns that should support multiple lines
 * the default is for all columns to be single line of text only
 * @param column
 * @return
 */
protected boolean isMulti(int column) {
	return false;
}

}