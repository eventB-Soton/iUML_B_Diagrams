package ac.soton.eventb.emf.diagrams.navigator.refactor;

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


public class Recorder {
	
	protected ChangeRecorder cr;
	protected Map<EObject,URI> proxyMap;
	protected Resource res;
	protected ResourceSet rs;
	protected Resource chRes;
	
	protected EventBNamedCommentedComponentElement component;
	private TransactionalEditingDomain ed;
	
	public Recorder(EventBNamedCommentedComponentElement component) {
		super();
		this.component=component;
		res = component.eResource();
		rs = res.getResourceSet();
		ed = TransactionUtil.getEditingDomain(rs);
		
		chRes = RefactorPersistence.INSTANCE.getChangesResource(res);
		proxyMap = RefactorPersistence.INSTANCE.getProxyMap(res);
	}
	
//	/**
//	 * tests whether there are any changes yet
//	 * @return true if the changes resource is not empty
//	 */
//	public boolean hasChanges() {
//		return chRes!=null &&  !chRes.getContents().isEmpty();
//	}
	
	/**
	 * Resume recording for the given component.
	 * If no previous recording is found a new recording is started. 
	 * 
	 * @param component to record changes for
	 */
	public void resumeRecording(){
		if (ed==null) {
			System.out.println("cannot use change recorder without editing domain");
			//TODO: log an error
			return;
		}
		ChangeDescription changes = getChangeDescription();
		proxyMap = RefactorPersistence.INSTANCE.getProxyMap(res);
		if (cr==null) 		
			cr = new ChangeRecorder();
		cr.setEObjectToProxyURIMap(proxyMap);
		BeginRecordingCommand command = new BeginRecordingCommand(chRes, cr, changes , component);
		ed.getCommandStack().execute(command);
		command.dispose();
	}

	/**
	 * disposes the ChangeRecorder
	 */
	public void disposeChangeRecorder() {
		if (cr!=null) cr.dispose();
		if (chRes!=null){
			rs.getResources().remove(chRes);
			rs.getResources().remove(
					RefactorPersistence.INSTANCE.getProxyMapResource(res));
		}
	}
	
//	/**
//	 * Deletes the Change Records from the file system
//	 * 
//	 * 
//	 */
//	public void deleteChangeRecords() {
//		try {
//			chRes.delete(Collections.EMPTY_MAP);
//			RefactorPersistence.INSTANCE.getProxyMapResource(res).delete(Collections.EMPTY_MAP);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * saves (persists)  the changes in the current recorder and proxyMap
	 * 
	 */
	public void saveChanges() {
		EndRecordingCommand erc = new EndRecordingCommand(chRes, cr);
		ed.getCommandStack().execute(erc);
		ChangeDescription changes = erc.getChanges();
		erc.dispose();
		Resource proxyMapResource = RefactorPersistence.INSTANCE.getProxyMapResource(res);
		RefactorPersistence.INSTANCE.saveChanges(chRes,changes,proxyMapResource,proxyMap);
	}

	
	/**
	 * 
	 * @return
	 */
	private ChangeDescription getChangeDescription(){
		chRes = RefactorPersistence.INSTANCE.getChangesResource(res);
		proxyMap = RefactorPersistence.INSTANCE.getProxyMap(res);		
		EList<EObject> contents = chRes.getContents();
		EObject content = contents.size()>0? chRes.getContents().get(0) : null;
		ChangeDescription changes = content instanceof ChangeDescription? (ChangeDescription)content : null;
		return changes;
	}
	
	////////////////////////////// COMMANDS //////////////////////////////////
	
	protected class BeginRecordingCommand extends ChangeCommand {
		ChangeRecorder cr;
		ChangeDescription changes;
		EventBNamedCommentedComponentElement component;
		 BeginRecordingCommand(Resource chRes, ChangeRecorder cr, ChangeDescription changes, EventBNamedCommentedComponentElement component){
			super(new ChangeRecorder(chRes).endRecording());
			this.cr = cr;
			this.changes = changes;
			this.component = component;
		}
		@Override
		public void doExecute(){			
			cr.beginRecording(changes, Collections.singleton(component));
		}
	}
	
	protected class EndRecordingCommand extends ChangeCommand {
		ChangeRecorder cr;
		ChangeDescription changes;
		 EndRecordingCommand(Resource chRes, ChangeRecorder cr){
			super(new ChangeRecorder(chRes).endRecording());
			this.cr = cr;
		}
		@Override
		public void doExecute(){
			changes = cr.endRecording();
		}
		public ChangeDescription getChanges() { return changes; }
	}
}
