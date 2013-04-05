/**
 * Copyright (c) 2011 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
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
