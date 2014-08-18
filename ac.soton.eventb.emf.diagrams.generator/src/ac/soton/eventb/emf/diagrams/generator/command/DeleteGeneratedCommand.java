package ac.soton.eventb.emf.diagrams.generator.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.WrappedException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.Transaction;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.machine.Machine;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

import ac.soton.eventb.emf.diagrams.generator.Activator;
import ac.soton.eventb.emf.diagrams.generator.impl.Messages;
import ac.soton.eventb.emf.diagrams.generator.utils.Is;


public class DeleteGeneratedCommand extends AbstractEMFOperation {

	private AbstractExtension abstractExtension;
	private String sourceExtensionID;

	public DeleteGeneratedCommand(TransactionalEditingDomain editingDomain, EObject element) {
			super(editingDomain, "Delete generated elements", null); //Messages.GENERATOR_MSG_11, null);
			setOptions(Collections.singletonMap(Transaction.OPTION_UNPROTECTED, Boolean.TRUE));
		if (element instanceof AbstractExtension){
			abstractExtension = ( AbstractExtension)element;
			//Obtain the extension ID from the source abstractExtension
			sourceExtensionID = abstractExtension.getExtensionId();
		}else{
			abstractExtension = null;
			sourceExtensionID = null;
		}
	}
	
	@Override
	public boolean canExecute(){
		return abstractExtension != null && sourceExtensionID!=null ;
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
		try {
			RodinCore.run(new IWorkspaceRunnable() {
				public void run(final IProgressMonitor monitor) throws CoreException{
					monitor.setTaskName("Deleting generated elements for "+ sourceExtensionID);	
					//Remove previously generated elements	
					List<EObject> previouslyGeneratedElements = getPreviouslyGeneratedElements(
							(EventBNamedCommentedComponentElement) abstractExtension.getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT),
							sourceExtensionID);
					for (EObject eObject : previouslyGeneratedElements){
						EcoreUtil.delete(eObject, true);	//this deletes the object from its containment and removes all references to it and its content
					}
				monitor.done();
				}
			},monitor);
			
			return Status.OK_STATUS;

		} catch (RodinDBException e) {
			Activator.logError(Messages.GENERATOR_MSG_19, e);
			return new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.GENERATOR_MSG_19, e);
		} catch (WrappedException e) {
			Activator.logError(Messages.GENERATOR_MSG_20, e);
			return new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.GENERATOR_MSG_20, e);
		} finally {
			monitor.done();
		}
	}
	
	/*
	 * finds all elements that have previously been generated with this generators generatorID
	 * @return List of elements
	 */
	private ArrayList<EObject> getPreviouslyGeneratedElements(final EventBNamedCommentedComponentElement component, String sourceExtensionID) {
		EList<EObject> contents = component.getAllContained(CorePackage.eINSTANCE.getEventBElement(),false);
		contents.remove(null);
		contents.add(0,component);
		ArrayList<EObject> remove = new ArrayList<EObject>();
		for(EObject eObject : contents){
			if(eObject instanceof Machine){
				for(Context ctx : ((Machine)eObject).getSees()){
					for(EObject ieObject : ctx.eContents()){
						if(Is.generatedBy(ieObject, sourceExtensionID))
							remove.add(ieObject);
					}
				}
				
				
				
			}
			if (Is.generatedBy(eObject,sourceExtensionID)){
				remove.add(eObject);						
			}
			
			
		}
		return remove;
	}

}