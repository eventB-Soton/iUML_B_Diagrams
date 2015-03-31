package ac.soton.eventb.emf.diagrams.navigator.refactor;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.change.util.ChangeRecorder;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;

public abstract class RefactorAssistant {

	protected Map<EObject,URI> proxyMap;
	protected Resource res;
	protected ResourceSet rs;
	protected Resource chRes;

	protected EventBNamedCommentedComponentElement component;
	protected TransactionalEditingDomain ed;
	
	public RefactorAssistant(EventBNamedCommentedComponentElement component) {
		super();
		this.component=component;
		res = component.eResource();
		rs = res.getResourceSet();
		ed = TransactionUtil.getEditingDomain(rs);
		
		chRes = RefactorPersistence.INSTANCE.getChangesResource(res);
		proxyMap = RefactorPersistence.INSTANCE.getProxyMap(res);
		
	}
	
	/**
	 * tests whether there are any changes yet
	 * @return true if the changes resource is not empty
	 */
	public boolean hasChanges() {
		return chRes!=null &&  !chRes.getContents().isEmpty();
	}

	/**
	 * disposes the ChangeRecorder
	 */
	public void disposeChangeRecords() {
		if (chRes!=null){
			rs.getResources().remove(chRes);
			rs.getResources().remove(
					RefactorPersistence.INSTANCE.getProxyMapResource(res));
		}
	}
	
	/**
	 * Deletes the Change Records from the file system
	 * 
	 * 
	 */
	public void deleteChangeRecords() {
		try {
			chRes.delete(Collections.EMPTY_MAP);
			RefactorPersistence.INSTANCE.getProxyMapResource(res).delete(Collections.EMPTY_MAP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	////////////////////////////// PROTECTED //////////////////////////////////
	
	/**
	 * 
	 * @return
	 */
	protected ChangeDescription getChangeDescription(){
		chRes = RefactorPersistence.INSTANCE.getChangesResource(res);
		proxyMap = RefactorPersistence.INSTANCE.getProxyMap(res);		
		EList<EObject> contents = chRes.getContents();
		EObject content = contents.size()>0? chRes.getContents().get(0) : null;
		ChangeDescription changes = content instanceof ChangeDescription? (ChangeDescription)content : null;
		return changes;
	}
	
	////////////////////////////// COMMANDS //////////////////////////////////
	
	protected class ApplyReverseCommand extends ChangeCommand {
		ChangeDescription changes;
		ApplyReverseCommand(Resource chRes, ChangeDescription changes){
			super(new ChangeRecorder(chRes).endRecording()); 	//!!! can't find a way to avoid this.. a change recorder to record what the change recorder does!
			this.changes = changes;
		}
		@Override
		public void doExecute(){
			changes.applyAndReverse();
		}
	}
	
	protected class CopyReverseCommand extends ChangeCommand {
		ChangeDescription changes;
		CopyReverseCommand(Resource chRes, ChangeDescription changes){
			super(new ChangeRecorder(chRes).endRecording()); 	//!!! can't find a way to avoid this.. a change recorder to record what the change recorder does!
			this.changes = changes;
		}
		@Override
		public void doExecute(){
			changes.copyAndReverse(proxyMap);
		}
		public ChangeDescription getChanges() { return changes; }
	}

}
