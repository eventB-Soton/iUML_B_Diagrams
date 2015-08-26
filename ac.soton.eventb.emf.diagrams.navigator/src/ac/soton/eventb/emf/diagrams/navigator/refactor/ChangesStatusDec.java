/*******************************************************************************
 * Copyright (c) 2011 Systerel and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License  v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Systerel - initial API and implementation
 *******************************************************************************/
package ac.soton.eventb.emf.diagrams.navigator.refactor;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eventb.core.IContextRoot;
import org.eventb.core.IEventBRoot;
import org.eventb.core.IMachineRoot;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.persistence.EMFRodinDB;
import org.rodinp.core.IElementType;
import org.rodinp.core.IRodinElement;

import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;
import ac.soton.eventb.emf.diagrams.navigator.refactor.persistence.RefactorPersistence;
import fr.systerel.explorer.IElementNode;

/**
 * @author cfs
 * 
 */
public class ChangesStatusDec implements ILightweightLabelDecorator {

	/**
	 * Create an EMFRodinDB for loading extensions into EMF
	 */
	private final static EMFRodinDB emfRodinDB = new EMFRodinDB();
	private final static String CHANGES_ICON_PATH = "icons/ChangesDecorator.gif";

	
	@Override
	public void addListener(ILabelProviderListener listener) {
		// do nothing
	}

	@Override
	public void dispose() {
		// do nothing
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// do nothing
	}

	private static String getOverlayIcon(Object obj) {
		final IElementType<?> type = getElementType(obj);
		if (type == IMachineRoot.ELEMENT_TYPE || type == IContextRoot.ELEMENT_TYPE){
			EventBElement eventBElement = emfRodinDB.loadEventBComponent((IEventBRoot)obj);
		    if (eventBElement  instanceof EventBNamedCommentedComponentElement){
		    	URI uri = EcoreUtil.getURI(eventBElement);
		    	if (RefactorPersistence.INSTANCE.hasChanges(uri, emfRodinDB.getResourceSet())){
		    		return CHANGES_ICON_PATH;		    		
		    	};
		    }
		}
	    return null;
	}

	private static IElementType<?> getElementType(Object obj) {
		if (obj instanceof IRodinElement) {
			return ((IRodinElement) obj).getElementType();
		}
		if (obj instanceof IElementNode) {
			return ((IElementNode) obj).getChildrenType();
		}
		return null;
	}

	@Override
	public void decorate(Object element, IDecoration decoration) {
		final String ovrName = getOverlayIcon(element);
		if (ovrName == null)
			return;
		final ImageDescriptor overlay = AbstractUIPlugin.imageDescriptorFromPlugin(DiagramsNavigatorExtensionPlugin.PLUGIN_ID, ovrName);
		decoration.addOverlay(overlay, IDecoration.TOP_LEFT);
	}

}
