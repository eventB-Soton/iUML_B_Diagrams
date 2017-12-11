package ac.soton.eventb.emf.diagrams.generator.commands;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.machine.Machine;

import ac.soton.emf.translator.UnTranslateCommand;
import ac.soton.emf.translator.configuration.IAdapter;
import ac.soton.emf.translator.eventb.adapter.EventBTranslatorAdapter;

/**
 * Command to delete previously generated elements that have been generated from the given element
 * 
 * Finds the affected resources and sourceExtensionId and then 
 * creates and wraps an unTranslateCommand.
 * 
 * @author cfs
 * @since 4.0
 *
 */
public class DeleteGeneratedCommand extends AbstractEMFOperation {
	
	private UnTranslateCommand untranslateCommand;

	public DeleteGeneratedCommand(TransactionalEditingDomain editingDomain, EObject element) {
		super(editingDomain, "Delete generated elements", null);
		setOptions(Collections.singletonMap(Transaction.OPTION_UNPROTECTED, Boolean.TRUE));

		String sourceExtensionID=null;
		Collection<Resource> resources = null;
		IAdapter adapter = null;
		
		if (element instanceof AbstractExtension){
			AbstractExtension abstractExtension = ( AbstractExtension)element;
			//Obtain the extension ID from the source abstractExtension
			sourceExtensionID = abstractExtension.getExtensionId();
			//get the resources that might have elements generated from this extension
			resources = getAffectedResources(abstractExtension);
			//set up an adapter for the DeTranslate to use
			adapter = new EventBTranslatorAdapter();
			adapter.initialiseAdapter(abstractExtension);
		}
		untranslateCommand = new UnTranslateCommand(editingDomain, resources, sourceExtensionID, adapter);
	}
	
	private Collection<Resource> getAffectedResources(EventBElement eventBElement) {
		Collection<Resource> resources = new HashSet<Resource>();
		EventBNamedCommentedComponentElement component =  
				(EventBNamedCommentedComponentElement) eventBElement.getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);	
		resources.add(component.eResource());
		if (component instanceof Machine){
			for (Context context : ((Machine)component).getSees()){
				resources.addAll(getAffectedResources(context));
			}
		}else if (component instanceof Context){
			for (Context context : ((Context)component).getExtends()){
				resources.addAll(getAffectedResources(context));
			}
		}
		return resources;
	}

	@Override
	public boolean canExecute(){
		return untranslateCommand.canExecute();
	}	
	
	@Override
	public boolean canRedo(){
		return untranslateCommand.canRedo();
	}

	@Override
	public boolean canUndo(){
		return untranslateCommand.canUndo();
	}
	
	@Override
	protected  IStatus doExecute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException {
		return untranslateCommand.execute(monitor, info);
	}
}