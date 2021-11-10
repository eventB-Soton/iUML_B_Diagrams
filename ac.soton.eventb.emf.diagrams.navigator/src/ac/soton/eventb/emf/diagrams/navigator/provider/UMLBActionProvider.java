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

package ac.soton.eventb.emf.diagrams.navigator.provider;

import static org.eclipse.ui.IWorkbenchCommandConstants.EDIT_DELETE;
import static org.eclipse.ui.actions.ActionFactory.NEW_WIZARD_DROP_DOWN;
import static org.eclipse.ui.navigator.ICommonActionConstants.OPEN;
import static org.eclipse.ui.navigator.ICommonMenuConstants.GROUP_NEW;
import static org.eclipse.ui.navigator.ICommonMenuConstants.GROUP_OPEN;
import static org.eclipse.ui.navigator.ICommonMenuConstants.GROUP_OPEN_WITH;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.OpenWithMenu;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eventb.emf.core.EventBObject;

import ac.soton.eventb.emf.core.extension.navigator.provider.ExtensionNavigatorItem;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;



/**
 * The action provider for <code>UMLB</code>s.
 * @since 3.2
 */
public class UMLBActionProvider extends CommonActionProvider {
	
	public static final String GROUP_MODELLING = "modelling";
	
	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		final ICommonActionExtensionSite site = getActionSite();
		// forward doubleClick to doubleClickAction
		actionBars.setGlobalActionHandler(OPEN, getOpenAction(site));
		// forwards pressing the delete key to deleteAction
		actionBars.setGlobalActionHandler(EDIT_DELETE, getDeleteAction(site));
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		final ICommonActionExtensionSite site = getActionSite();
		menu.add(new Separator(GROUP_NEW));
		menu.appendToGroup(GROUP_NEW, getNewAction());
		menu.appendToGroup(GROUP_OPEN, getOpenAction(site));
		menu.appendToGroup(GROUP_OPEN_WITH, buildOpenWithMenu());
		menu.add(new Separator(GROUP_MODELLING));
		menu.appendToGroup(GROUP_MODELLING, getDeleteAction(site));
	}

	/**
	 * Builds a New menu action with drop down specific to the current IDE.
	 *
	 * @return the built menu
	 */
	private static IWorkbenchAction getNewAction() {
		final IWorkbenchAction newAction = NEW_WIZARD_DROP_DOWN
				.create(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		newAction.setText(newAction.getToolTipText());
		return newAction;
	}

	/**
	 * Builds an Open With menu.
	 * 
	 * @return the built menu
	 */
	MenuManager buildOpenWithMenu() {
		MenuManager menu = new MenuManager("Open With",
				ICommonMenuConstants.GROUP_OPEN_WITH);
		final StructuredViewer viewer = getActionSite().getStructuredViewer();
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		menu.add(new OpenWithMenu(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(),
				getFile(obj)));
		return menu;
	}
	
	/////////////////////////////// ACTION collection ///
	
	/**
	 * Provides an open action for UMLB elements 
	 * @param site
	 * @return An open action
	 */
	public static Action getOpenAction(final ICommonActionExtensionSite site) {
		Action doubleClickAction = new Action("Open") {
			@Override
			public void run() {
				ISelection selection = site.getStructuredViewer().getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();

				linkToPreferredEditor(((ExtensionNavigatorItem)obj).getEObject());				
			}
		};

		return doubleClickAction;
		
	}
	

	
	
	/**
	 * 
	 * @param site
	 * @return An action for deleting rodin project or rodin files
	 */
	public static Action getDeleteAction(final ICommonActionExtensionSite site) {
		Action deleteAction = new Action() {
			@Override
			public void run() {
				if (!(site.getStructuredViewer().getSelection().isEmpty())) {

					// Convert the selection into a set of Resources
					Set<Resource> set = new HashSet<Resource>();
					// Convert the selection into a set of Resources
					Set<IFile> setF = new HashSet<IFile>();
					
					IStructuredSelection ssel = (IStructuredSelection) site.getStructuredViewer().getSelection();
		
					for (Iterator<?> it = ssel.iterator(); it.hasNext();) {
						final EObject eObj = ((ExtensionNavigatorItem)it.next()).getEObject();
						if (eObj instanceof EventBObject) {
							if (((EventBObject) eObj).eResource() instanceof Resource) {
								set.add(((EventBObject) eObj).eResource());
							}
							IFile file = getFile(eObj);
							if (
									MessageDialog.openConfirm(site.getViewSite().getShell(), "Confirm File Delete", 
									"Are you sure you want to delete file '"
											+ file.getName()
											+ "' ?")){
								setF.add(file);								
							}
						}
					}
					for (IFile file : setF) {
						try {
							file.delete(false, new NullProgressMonitor());
						} catch (CoreException e) {
							MessageDialog.openError(null, "Error", "Could not delete file");
							e.printStackTrace();
						}
					}
				}
			}
		};
		deleteAction.setText("&Delete");
		deleteAction.setToolTipText("Delete these elements");
		deleteAction.setImageDescriptor(
				AbstractUIPlugin.imageDescriptorFromPlugin(
						DiagramsNavigatorExtensionPlugin.PLUGIN_ID, 
						"icons/Delete.png"));
		return deleteAction;
	}

	/////////////////// Utils ////////////
	
	/**
	 * if the passed Object is an EObject or ExtensionNavigatorItem,
	 * returns the corresponding IFile
	 * @param object
	 */
	private static IFile getFile(final Object object) {
		IResource resource = null;
		EObject eObject = null;
		if (object instanceof ExtensionNavigatorItem) {
			eObject = ((ExtensionNavigatorItem)object).getEObject();
		}else if (object instanceof EObject) {
			eObject  = (EObject) object;
		}
		if (eObject!=null) {
			String path = eObject.eResource().getURI().toPlatformString(true);
			resource = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(path));
		}
		return resource instanceof IFile? (IFile)resource : null;
	}
	
	
	/**
	 * Link the current object to the preferred editor.
	 * <p>
	 * 
	 * @param eObj
	 */
	private static void linkToPreferredEditor(EObject eObj) {
		IFile file = getFile(eObj);
		IEditorDescriptor desc = IDE.getDefaultEditor(file);
		linkToEditor(file, desc);
	}
	
	
	/**
	 * Link the current object to the specified editor.
	 * <p>
	 * 
	 * @param desc
	 *            the editor descriptor
	 */
	private static void linkToEditor(IFile file, IEditorDescriptor desc) {
		try {
			IWorkbenchPage  page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			@SuppressWarnings("unused")
			IEditorPart editor = page.openEditor(new FileEditorInput(file), desc.getId());
		} catch (PartInitException e) {
			e.printStackTrace();
			String errorMsg = "Error opening Editor"+desc.getId();
			MessageDialog.openError(null, null, errorMsg);
		}
	}
	
	
}
