/*******************************************************************************
 * Copyright (c) 2014 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.navigator.provider;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;

/**
 * Diagram provider class.
 * 
 * @author vitaly
 *
 */
public interface IDiagramProvider {
	
	/**
	 * Returns diagram file name.
	 * 
	 * @param element domain element
	 * @return diagram file name
	 */
	public String getDiagramFileName(EObject element);

	/**
	 * Returns diagram preference hint.
	 * 
	 * @return diagram preference hint for finding appropriate preference store
	 */
	public PreferencesHint getPreferencesHint();

	/**
	 * Returns diagram kind.
	 * 
	 * @return diagram kind
	 */
	public String getDiagramKind();

	/**
	 * Returns diagram editor ID.
	 * 
	 * @return id
	 */
	public String getEditorId();
	
	/**
	 * Returns the diagram file extension
	 * 
	 * @return fileExtension
	 */
	public String getFileExtension();
}
