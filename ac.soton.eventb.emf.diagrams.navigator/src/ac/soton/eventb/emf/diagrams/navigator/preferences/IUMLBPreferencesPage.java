package ac.soton.eventb.emf.diagrams.navigator.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;


/**
 * @author cfsnook
 *         The main, top level, preference page for iUML-B.
 *          
 *         At the moment, this page is empty, but sub-pages can use it as their category field
 */

public class IUMLBPreferencesPage extends FieldEditorPreferencePage implements
		IWorkbenchPreferencePage {

	private static final String message = "iUML-B preference settings - see sub-pages for specific features";
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
		// Do nothing at the moment.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		// Do nothing at the moment.
	}

	
}
