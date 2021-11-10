/*******************************************************************************
 * Copyright (c) 2014, 2020 University of Southampton.
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

package ac.soton.eventb.emf.diagrams.navigator.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.ResourceTransfer;

import ac.soton.eventb.emf.core.extension.navigator.provider.ExtensionNavigatorItem;

/**
 * Handler to copy UMLB componentNames to the clipboard
 * 
 * @author cfsnook
 */
public class CopyUMLBHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final ISelection currentSelection = HandlerUtil
				.getCurrentSelectionChecked(event);
		if (!(currentSelection instanceof IStructuredSelection)) {
			return null;
		}
		final IStructuredSelection selection = (IStructuredSelection) currentSelection;

		final IWorkbench workbench = PlatformUI.getWorkbench();
		final Clipboard clipboard = new Clipboard(workbench.getDisplay());

		final Iterator<?> iterator = selection.iterator();
		final Collection<IResource> resources = new ArrayList<IResource>();

		while (iterator.hasNext()) {
			final Object selected = iterator.next();
			IResource file = getWorkspaceResource(selected);
			if (file!=null) resources.add(file);
		}

		clipboard.setContents(new Object[] { resources
				.toArray(new IResource[resources.size()]), },
				new Transfer[] { ResourceTransfer.getInstance(), });

		return null;
	}

	/**
	 * @param selected
	 */
	private IResource getWorkspaceResource(final Object selected) {
		IResource resource = null;
		if (selected instanceof ExtensionNavigatorItem) {
			EObject eObject = ((ExtensionNavigatorItem)selected).getEObject();
			if (eObject!=null) {
				String path = eObject.eResource().getURI().toPlatformString(true);
				resource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
			}
		}
		return resource;
	}

}
