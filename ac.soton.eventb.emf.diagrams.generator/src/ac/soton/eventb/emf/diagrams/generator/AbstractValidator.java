/*******************************************************************************
 * Copyright (c) 2014, 2021 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.generator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.gmf.runtime.diagram.ui.resources.editor.parts.DiagramDocumentEditor;
import org.eclipse.jface.action.Action;


/**
 * <p>
 * 
 * </p>
 * 
 * @author cfs
 * @version
 * @see
 * @since
 * @date
 */
public abstract class AbstractValidator implements IValidator {

	protected abstract String getMarkerType();
	
	protected abstract Action getValidateAction(DiagramEditor diagramDocumentEditor);
	
	
	@Override
	public boolean validate(DiagramDocumentEditor diagramDocumentEditor) throws Exception {
		// run validation
		Action action = getValidateAction(diagramDocumentEditor);
		action.run();
		return hasMarkers(diagramDocumentEditor)==false || 
				"".equals(getValidationErrors(diagramDocumentEditor));
	}
		
	/**
	 * Returns a string of errors from validation markers for file.
	 * 
	 * @param diagramDocumentEditor
	 * @return string of errors
	 * 
	 */
	@Override
	public String getValidationErrors(DiagramDocumentEditor diagramDocumentEditor) {
		IMarker[] markers;
		try {
			markers = getFile(diagramDocumentEditor).findMarkers(
					getMarkerType(),
					true,
					IResource.DEPTH_ZERO);
		} catch (CoreException e) {
			return "ERRORS UNKNOWN: COULD NOT RETRIEVE VALIDATION ERROR MARKERS FROM FILE";
		}
		StringBuilder errors = new StringBuilder();
		for (IMarker marker : markers) {
			//int severity = marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
			if (true) //severity == IMarker.SEVERITY_ERROR)
				errors.append("\n"
						+ marker.getAttribute(IMarker.MESSAGE, "unknown error"));
		}
		return errors.toString();
	}

	private boolean hasMarkers(DiagramDocumentEditor diagramDocumentEditor) throws CoreException{
		return getFile(diagramDocumentEditor).findMarkers(getMarkerType(),true,IResource.DEPTH_ZERO).length>0;
	}
	
	private IFile getFile(DiagramDocumentEditor diagramDocumentEditor){
		return WorkspaceSynchronizer.getFile(diagramDocumentEditor.getDiagram().eResource());
	}

}
