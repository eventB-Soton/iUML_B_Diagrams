/*******************************************************************************
 * Copyright (c) 2014, 2019 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.generator.handlers;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.persistence.EMFRodinDB;
import org.eventb.emf.persistence.SaveResourcesCommand;
import org.rodinp.core.IInternalElement;

import ac.soton.eventb.emf.diagrams.generator.Activator;
import ac.soton.eventb.emf.diagrams.generator.commands.TranslateAllCommand;


/**
 * Handler to translate all iUML-B diagrams in a Machine/Context into Event-B
 * 
 * @author cfs
 * @since 4.0
 *
 */
public class IUMLBTranslateAllHandler extends AbstractHandler {

	String report = "";
	IStatus status = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public final IStatus execute(final ExecutionEvent event) throws ExecutionException {

		ISelection selection = HandlerUtil.getCurrentSelection(event);
		
		final EObject root;
		if (selection instanceof IStructuredSelection && !selection.isEmpty()){
			Object obj = ((IStructuredSelection)selection).getFirstElement();
			root = getEObject(obj);
		}else {
			root=null;
		}

		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		final Shell shell = activeWorkbenchWindow.getShell();
		try {
			if (!(root instanceof EventBElement)){
				status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TRANSLATOR_MSG_07);
				Activator.getDefault().getLog().log(new Status(Status.WARNING, Activator.PLUGIN_ID,
						"Selected element is not suitable for translation\n"+
						"Root of selected element: "+root, null));
				report = "Translation ABORTED - Selected element cannot be translated";
			}else{
				ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
				dialog.run(true, true, new IRunnableWithProgress(){
					public void run(IProgressMonitor monitor) throws InvocationTargetException {
						try {
							TransactionalEditingDomain editingDomain = TransactionalEditingDomain.Factory.INSTANCE.getEditingDomain(root.eResource().getResourceSet());
							TranslateAllCommand translateAllCmd = new TranslateAllCommand(editingDomain, (EventBElement) root);
							if (translateAllCmd.canExecute()){
								status = translateAllCmd.execute(null, null);
								report = status.getMessage();
								// save all resources that have been modified
								SaveResourcesCommand saveCommand = new SaveResourcesCommand(editingDomain);
								if (saveCommand.canExecute()){
										saveCommand.execute(monitor, null);
								}
							}
						} catch (Exception e) {
							throw new InvocationTargetException(e);
						}
					}
				});
			}
		} catch (InvocationTargetException e) {
			e.printStackTrace();
	    	status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TRANSLATOR_MSG_07, e);
			Activator.logError(Messages.TRANSLATOR_MSG_07, e);
			report = report + "\n"+e.toString();
		} catch (InterruptedException e) {
	    	status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TRANSLATOR_MSG_08, e);        	
			Activator.logError(Messages.TRANSLATOR_MSG_08, e);
			report = report + "\n"+e.toString();
		}finally{
			MessageDialog.openInformation(shell, Messages.TRANSLATOR_MSG_09, report);
		}
		return status;
	}
	
	/**
	 * From the selected object, get an EObject that can be translated.
	 * @param obj
	 * @return corresponding EObject to be translated
	 */
	protected EObject getEObject(Object obj) {
		if (obj instanceof IInternalElement){
			return (new EMFRodinDB()).loadEventBComponent((IInternalElement)obj) ;
		}else if (obj instanceof EObject){
			return EcoreUtil.getRootContainer((EObject)obj);
		}else if (obj instanceof IAdaptable) {
			Object adaptedObj = ((IAdaptable) obj).getAdapter(EObject.class);
			if (adaptedObj instanceof EObject){
				return EcoreUtil.getRootContainer((EObject)adaptedObj); 
			} else return null;
		}else if (obj instanceof Resource){
			return ((Resource)obj).getContents().get(0);
		}else return null;
	}
	
}
