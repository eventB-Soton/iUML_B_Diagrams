/**
 * 
 */
package ac.soton.eventb.emf.diagrams.generator.handlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ac.soton.emf.translator.Activator;
import ac.soton.emf.translator.eventb.handler.EventBTranslateHandler;
import ac.soton.eventb.emf.diagrams.generator.impl.ValidatorRegistry;


/**
 * <p>
 * 
 * </p>
 * 
 * @author cfs
 * @version
 * @see
 * @since 4.0
 */
public class IUMLBTranslateHandler extends EventBTranslateHandler {	
	
	/**
	 * This is overridden to add some validation of the diagram before the translation
	 * 
	 * @param ExecutionEvent
	 * @param monitor
	 * @return status = OK to continue translation, INFO to report validation errors and cancel translation
	 * @throws ExecutionException 
	 */
	protected IStatus validate(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {

		IEditorPart editor = HandlerUtil.getActiveEditorChecked(event);
		IStatus status = Status.CANCEL_STATUS;

		if (editor instanceof DiagramDocumentEditor) {
			final DiagramDocumentEditor diagramDocumentEditor = (DiagramDocumentEditor)editor;
				
				// save before transformation
				if (editor.isDirty())
					editor.doSave(new NullProgressMonitor());
				
				if (ValidatorRegistry.validate(diagramDocumentEditor)){
					status = Status.OK_STATUS;
				}else{
					// didn't validate so show feedback
					String errors = ValidatorRegistry.getValidationErrors(diagramDocumentEditor);
					status = new Status(IStatus.INFO, Activator.PLUGIN_ID, ValidationFailedMessage+errors );
				}
		}
        monitor.done();
        return status;
	}
				
	final static String ValidationFailedMessage = "Translation cancelled because validation failed with the following errors : \n";
}
