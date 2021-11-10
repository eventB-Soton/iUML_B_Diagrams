/*******************************************************************************
 * Copyright (c) 2014, 2018 University of Southampton.
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

import java.util.Collections;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;

import ac.soton.emf.translator.TranslatorFactory;
import ac.soton.eventb.emf.diagrams.generator.Activator;
import ac.soton.eventb.emf.diagrams.generator.DiagramsGeneratorIdentifiers;

/**
 * Command to delete previously generated elements that have been generated from the given element
 * 
 * Finds the relevant translator and runs its 'untranslate' method
 * 
 * @author cfs
 * @since 4.0
 *
 */
public class DeleteGeneratedCommand extends AbstractEMFOperation {
	

	TranslatorFactory factory = null;	
	protected EventBElement sourceElement;
	
	public DeleteGeneratedCommand(TransactionalEditingDomain editingDomain, EventBElement sourceElement) {
		super(editingDomain, "Delete generated elements", null);
		setOptions(Collections.singletonMap(Transaction.OPTION_UNPROTECTED, Boolean.TRUE));
		try {
			factory = TranslatorFactory.getFactory();
		} catch (CoreException e) {
			e.printStackTrace();
		}
		this.sourceElement = sourceElement;
	}

	@Override
	public boolean canExecute(){
		return factory!=null && factory.canTranslate(DiagramsGeneratorIdentifiers.COMMAND_ID, sourceElement.eClass());
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
		IStatus status = null;
		
		SubMonitor submonitor = SubMonitor.convert(monitor, "Delete generated elements", 3);								

		String sourceElementName = sourceElement instanceof EventBNamed?
				((EventBNamed)sourceElement).getName() :
				sourceElement.getReference();	
		EClass sourceClass = sourceElement.eClass();
		
		if (factory != null && factory.canTranslate(DiagramsGeneratorIdentifiers.COMMAND_ID, sourceElement.eClass())){
			submonitor.setTaskName("Un-translating "+sourceClass.getName()+" : "+sourceElementName);
			status = factory.untranslate(getEditingDomain(), sourceElement, DiagramsGeneratorIdentifiers.COMMAND_ID, monitor);
			submonitor.worked(2);

		}else{
			String CannotUnTranslateMessage = "cannot untranslate";
			status = new Status(IStatus.CANCEL, Activator.PLUGIN_ID, CannotUnTranslateMessage  );
		}

		return status;
	}
}