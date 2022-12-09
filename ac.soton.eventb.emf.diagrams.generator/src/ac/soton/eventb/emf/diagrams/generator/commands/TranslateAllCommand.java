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
package ac.soton.eventb.emf.diagrams.generator.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;

import ac.soton.emf.translator.TranslatorFactory;
import ac.soton.eventb.emf.diagrams.generator.Activator;
import ac.soton.eventb.emf.diagrams.generator.DiagramsGeneratorIdentifiers;

/**
 * This command searches the given sourceElement for iUML-B diagrams that can be translated.
 * If any they are validated using the registered validator  and if ok, 
 * translated using the registered translator
 * 
 * following execution, a report can be retrieved using getReport()
 * 
 * @author cfs
 * @since 4.0
 *
 */
public class TranslateAllCommand extends AbstractEMFOperation {
		
	final static String commandTitle = "Translate All iUML-B Diagrams";
	final static String ValidationFailedMessage = "Translation cancelled because validation failed with the following errors : \n";
	final static String CannotTranslateMessage = "Translation cancelled because there is no translator\n";

	List<EventBElement> generateList;
	TranslatorFactory factory;
	String report = null;

	public TranslateAllCommand(TransactionalEditingDomain editingDomain, EventBElement sourceElement) {
		super(editingDomain, commandTitle, null);
		setOptions(Collections.singletonMap(Transaction.OPTION_UNPROTECTED, Boolean.TRUE));
		try {
			//get the translator factory
			factory = TranslatorFactory.getFactory();
			//get the diagram elements to be translated (supports any root, not just Event-B components) 
			final EObject root = EcoreUtil.getRootContainer(sourceElement);
			generateList = getDiagramRoots(root, null);
			report = commandTitle+" in "+(root instanceof EventBNamed? ((EventBNamed)root).getName() : root.toString());
		} catch (CoreException e) {
			factory = null;
			generateList = null;
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean canExecute(){
		return factory != null && generateList != null && generateList.size()>0;
	}	
	
	@Override
	public boolean canRedo(){
		return false;
	}

	@Override
	public boolean canUndo(){
		return false;
	}
	
	@Override
	protected  IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		IStatus[] statusArray = new IStatus[generateList.size()];
		int i = 0;
		SubMonitor submonitor = SubMonitor.convert(monitor, "Translating All", 3*generateList.size());								
		for (EventBElement sourceElement: generateList){
			String sourceElementName = sourceElement instanceof EventBNamed?
					((EventBNamed)sourceElement).getName() :
					sourceElement.getReference();	
			EClass sourceClass = sourceElement.eClass();
			IStatus status;
			report = report +"\n"+sourceClass.getName()+":"+sourceElementName+" - ";
			
			if (factory != null && factory.canTranslate(DiagramsGeneratorIdentifiers.COMMAND_ID, sourceElement.eClass())){
				submonitor.setTaskName("Validating "+sourceClass.getName()+" : "+sourceElementName);
				status = validate(sourceElement, submonitor.newChild(1));
				if (status.isOK()){
					submonitor.setTaskName("Translating "+sourceClass.getName()+" : "+sourceElementName);
					status = factory.translate(getEditingDomain(), sourceElement, DiagramsGeneratorIdentifiers.COMMAND_ID, submonitor.newChild(2));
				}
				submonitor.worked(2);

			}else{
				status = new Status(IStatus.CANCEL, Activator.PLUGIN_ID, CannotTranslateMessage );
			}
			report = report+status.getMessage();
			statusArray[i++]=status;
		}
		return new MultiStatus(Activator.PLUGIN_ID, 
				statusArray.length==0 ? IStatus.OK : IStatus.ERROR,
				statusArray, report, null) ;
	}

	/**
	 * validation of a particular diagram.
	 * called before translation
	 * 
	 * @param sourceElement
	 * @param monitor
	 * @return
	 * @throws ExecutionException
	 */
	protected IStatus validate(EventBElement sourceElement, IProgressMonitor monitor) throws ExecutionException {
		IStatus status;
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

	
	/** This recursively traverses the contents tree looking for the root diagram elements that can be generated
	 * Note that generate-able elements may be nested in others and these should not be generated unless 
	 * there is an intermediate generate-able element of another type
	 */
	private List<EventBElement> getDiagramRoots(EObject element, EClass lastType) throws CoreException {
		List<EventBElement> generateList = new ArrayList<EventBElement>();
		if (element instanceof EventBElement && element.eClass()!=lastType && 
				TranslatorFactory.getFactory().canTranslate(DiagramsGeneratorIdentifiers.COMMAND_ID, element.eClass())){
			generateList.add((EventBElement)element);
			lastType = element.eClass();
		}
		for (EObject eObject : element.eContents()){
			generateList.addAll(getDiagramRoots(eObject, lastType));
		}
		// diagrams may also be contained by reference via abstract extensions
		if (element instanceof AbstractExtension) {
			for (EReference r : element.eClass().getEReferences()) {
				Object referencedObject = element.eGet(r);
				if (referencedObject instanceof EObject) {
					generateList.addAll(getDiagramRoots((EObject) referencedObject, lastType));
				}
			}
		}
		return generateList;
	}

}