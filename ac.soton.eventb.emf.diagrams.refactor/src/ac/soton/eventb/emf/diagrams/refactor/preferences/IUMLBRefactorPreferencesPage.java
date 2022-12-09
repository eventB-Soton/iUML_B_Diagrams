/*******************************************************************************
 * Copyright (c) 2014, 2018 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.refactor.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ac.soton.eventb.emf.diagrams.refactor.Activator;


/**
 * @author cfsnook
 *         The refactor preference page for iUML-B.
 *          
 *         
 */

public class IUMLBRefactorPreferencesPage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private static final String message = "iUML-B refactoring preference";
	/**
	 * Constructor.
	 */
	public IUMLBRefactorPreferencesPage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription(message);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(Activator.PREFERENCES_REFACTORING_ENABLED,
			        "&Enable Refactoring Support (make sure there are no outstanding changes before changing this setting)", getFieldEditorParent()));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	    setPreferenceStore(Activator.getDefault().getPreferenceStore());
	    setDescription(message);
	}

	
}
