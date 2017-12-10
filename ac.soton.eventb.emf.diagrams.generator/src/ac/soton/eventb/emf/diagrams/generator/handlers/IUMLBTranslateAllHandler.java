/**
 * Copyright (c) 2012, 2015 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 	University of Southampton - Initial implementation
 *  
 */
package ac.soton.eventb.emf.diagrams.generator.handlers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;
import org.eventb.emf.persistence.EMFRodinDB;
import org.rodinp.core.IInternalElement;

import ac.soton.emf.translator.TranslatorFactory;
import ac.soton.eventb.emf.diagrams.generator.Activator;


/**
 * Handler to translate all iUML-B diagrams in a Machine/Context into Event-B
 * 
 * @author cfs
 * @since 4.0
 *
 */
public class IUMLBTranslateAllHandler extends AbstractHandler {

	final String commandId = "ac.soton.eventb.emf.diagrams.generator.translateToEventB";
	String report = null;
	IStatus status = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public final IStatus execute(final ExecutionEvent event) throws ExecutionException {
		EObject sourceElement;
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof IStructuredSelection && !selection.isEmpty()){
			Object obj = ((IStructuredSelection)selection).getFirstElement();
			sourceElement = getEObject(obj);
		} else sourceElement = null;
		if (!(sourceElement instanceof EventBElement)) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TRANSLATOR_MSG_07);
		}
		
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		final Shell shell = activeWorkbenchWindow.getShell();		
	
		final EObject component = ((EventBElement)sourceElement).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
		if (component==null ){
			Activator.getDefault().getLog().log(new Status(Status.WARNING, Activator.PLUGIN_ID, 
					"Selected element is not a component\n"+
					"Element: "+sourceElement, null));
			report = "Translation ABORTED - Selected element is not a component";
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, report);
		}else{
			report = "Translating all iUML-B diagrams in "+((EventBNamed)component).getName()+"\n";
		}

		try {
			//get the diagram elements to be translated
			final List<EventBElement> generateList = getDiagramRoots(component, null);
			//get the translator factory
			final TranslatorFactory factory = TranslatorFactory.getFactory();
			ProgressMonitorDialog dialog = new ProgressMonitorDialog(shell);
			dialog.run(true, true, new IRunnableWithProgress(){
				
				public void run(IProgressMonitor monitor) throws InvocationTargetException { 
					try {
						SubMonitor submonitor = SubMonitor.convert(monitor, "Translating All", 3*generateList.size());								
						for (EventBElement sourceElement: generateList){
							if (factory != null && factory.canTranslate(commandId, sourceElement.eClass())){
								String sourceElementName = sourceElement instanceof EventBNamed?
										((EventBNamed)sourceElement).getName() :
										sourceElement.getReference();	
								EClass sourceClass = sourceElement.eClass();
								report = report +"\n"+sourceClass.getName()+":"+sourceElementName+" - ";
								submonitor.setTaskName("Validating "+sourceClass.getName()+" : "+sourceElementName);
								status = validate(sourceElement, submonitor.newChild(1));
								if (status.isOK()){
									submonitor.setTaskName("Translating "+sourceClass.getName()+" : "+sourceElementName);
									status = factory.translate(sourceElement, commandId, submonitor.newChild(2));
								}else{
									submonitor.worked(2);
								}
								report = report+status.getMessage();
							}
						
						}
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					}
				}
			});
		} catch (InvocationTargetException e) {
	    	status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TRANSLATOR_MSG_07, e);
			Activator.logError(Messages.TRANSLATOR_MSG_07, e);
			report = report + "\n"+e.toString();
		} catch (InterruptedException e) {
	    	status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TRANSLATOR_MSG_08, e);        	
			Activator.logError(Messages.TRANSLATOR_MSG_08, e);
			report = report + "\n"+e.toString();
		} catch (CoreException e) {
	    	status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, Messages.TRANSLATOR_MSG_07, e);        	
			Activator.logError(Messages.TRANSLATOR_MSG_07, e);
			report = report + "\n"+e.toString();
		}finally{
			MessageDialog.openInformation(shell, Messages.TRANSLATOR_MSG_09, report);
		}
		return status;
	}
	
	protected IStatus validate(EventBElement sourceElement, IProgressMonitor monitor) throws ExecutionException {
		Diagnostic diagnostic = Diagnostician.INSTANCE.validate(sourceElement);
		if (diagnostic.getSeverity() == Diagnostic.ERROR || diagnostic.getSeverity() == Diagnostic.WARNING){
		// didn't validate so show feedback
			String errors = diagnostic.getMessage()+"\n";
		    for (Diagnostic ch : diagnostic.getChildren()){
		    	errors = errors+ch.getMessage()+"\n";
		    }
		    status = new Status(IStatus.INFO, Activator.PLUGIN_ID, ValidationFailedMessage+errors );
		}else{
		    status = Status.OK_STATUS;
		}
		monitor.done();
		return status;
	}
	final static String ValidationFailedMessage = "Translation cancelled because validation failed with the following errors : \n";

	/**
	 * From the selected object, get an EObject that can be translated.
	 * @param obj
	 * @return corresponding EObject to be translated
	 */
	protected EObject getEObject(Object obj) {
		if (obj instanceof IInternalElement){
			return (new EMFRodinDB()).loadEventBComponent((IInternalElement)obj) ;
		}else if (obj instanceof EObject){
			return (EObject)obj;
		}else if (obj instanceof IAdaptable) {
			Object adaptedObj = ((IAdaptable) obj).getAdapter(EObject.class);
			if (adaptedObj instanceof EObject){
				return (EObject) adaptedObj; 
			} else return null;
		}else if (obj instanceof Resource){
			return ((Resource)obj).getContents().get(0);
		}else return null;
	}
	
	/* This recursively traverses the contents tree looking for the root diagram elements that can be generated
	 * Note that generate-able elements may be nested in others and these should not be generated unless 
	 * there is an intermediate generate-able element of another type
	 */
	private List<EventBElement> getDiagramRoots(EObject element, EClass lastType) throws CoreException {
		List<EventBElement> generateList = new ArrayList<EventBElement>();
		if (element instanceof EventBElement && element.eClass()!=lastType && 
				TranslatorFactory.getFactory().canTranslate(commandId, element.eClass())){
			generateList.add((EventBElement)element);
			lastType = element.eClass();
		}
		for (EObject eObject : element.eContents()){
			generateList.addAll(getDiagramRoots(eObject, lastType));
		}
		return generateList;
	}
}
