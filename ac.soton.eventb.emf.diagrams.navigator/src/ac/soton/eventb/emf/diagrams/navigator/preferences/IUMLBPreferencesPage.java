package ac.soton.eventb.emf.diagrams.navigator.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;


/**
 * @author cfsnook
 *         The main, top level, preference page for iUML-B.
 *          
 *         sub-pages can use this as their category field
 */

public class IUMLBPreferencesPage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private static final String message = "iUML-B preference settings - see sub-pages for specific diagram features";
	/**
	 * Constructor.
	 */
	public IUMLBPreferencesPage() {
		super(GRID);
		setPreferenceStore(DiagramsNavigatorExtensionPlugin.getDefault().getPreferenceStore());
		setDescription(message);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(DiagramsNavigatorExtensionPlugin.PREFERENCES_REFACTORING_ENABLED,
			        "&Enable Refactoring Support (make sure there are no outstanding changes before changing this setting)", getFieldEditorParent()));
//		addField(new BooleanFieldEditor("LockingEnabled",
//		        "&Refinement Locking (one refinement level can be edited at a time - needs refactoring to be enabled)", getFieldEditorParent()));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	    setPreferenceStore(DiagramsNavigatorExtensionPlugin.getDefault().getPreferenceStore());
	    setDescription(message);
	}

	
}
