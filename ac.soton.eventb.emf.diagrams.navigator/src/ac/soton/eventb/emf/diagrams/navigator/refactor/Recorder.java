package ac.soton.eventb.emf.diagrams.navigator.refactor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.change.FeatureChange;
import org.eclipse.emf.ecore.change.ListChange;
import org.eclipse.emf.ecore.change.util.ChangeRecorder;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;

import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;


public class Recorder {
	
	protected ChangeRecorder cr;
	protected Map<EObject,URI> proxyMap;
	protected Resource res;
	protected ResourceSet rs;
	protected Resource chRes;
	protected Resource pmRes;
	protected boolean recordingInProgress;
	
	protected static Boolean refactoringEnabled =  DiagramsNavigatorExtensionPlugin.getDefault().getPreferenceStore().getBoolean("RefactoringEnabled");
	
	protected EventBNamedCommentedComponentElement component;
	private TransactionalEditingDomain ed;
	
	protected Recorder(EventBNamedCommentedComponentElement component) {
		super();
		this.component=component;
		res = component.eResource();
		rs = res.getResourceSet();
		ed = TransactionUtil.getEditingDomain(rs);
		recordingInProgress = false;
		
		chRes = RefactorPersistence.INSTANCE.getChangesResource(res);
		pmRes = RefactorPersistence.INSTANCE.getProxyMapResource(res);
		proxyMap = RefactorPersistence.INSTANCE.getProxyMap(res);
	}
	
	/** 
	 * if re-factoring is enabled 
	 * 	gets a new change recorder 
	 * otherwise returns null
	 * 
	 */
	public static Recorder getNewRecorder(EventBNamedCommentedComponentElement component){
		if (refactoringEnabled){
			return new Recorder(component);
		}else{
			return null;
		}
	}
	
//	/**
//	 * tests whether there are any changes yet
//	 * @return true if the changes resource is not empty
//	 */
//	public boolean hasChanges() {
//		return chRes!=null &&  !chRes.getContents().isEmpty();
//	}
	
	/**
	 * Resume recording.
	 * If no previous recording is found a new recording is started. 
	 * 
	 * @param component to record changes for
	 * @return codes: 	0 resumed, 
	 * 					1 changes out of sync, restarted changes,
	 * 					2 ignored, recording already in progress, 
	 * 					3 ignored no editing domain
	 */
	public int resumeRecording(){
		if (ed==null) {
			System.out.println("cannot use change recorder without editing domain");
			//TODO: log an error
			return 3;
		}
		if (recordingInProgress) return 2;
		ChangeDescription changes = getChangeDescription();
		proxyMap = RefactorPersistence.INSTANCE.getProxyMap(res);
		if (cr==null) 		
			cr = new ChangeRecorder();
		cr.setEObjectToProxyURIMap(proxyMap);
		BeginRecordingCommand command = new BeginRecordingCommand(changes);
		ed.getCommandStack().execute(command);
		boolean reset = command.resetChanges;
		command.dispose();
		recordingInProgress = true;
		return reset? 1 : 0; 
	}
	
	/**
	 * End the recording.
	 * saves (persists)  the changes in the current recorder and proxyMap
	 * 
	 */
	public void endRecording() {
		if (!recordingInProgress) return;
		EndRecordingCommand erc = new EndRecordingCommand(); //chRes, cr);
		ed.getCommandStack().execute(erc); 
		erc.dispose();
		recordingInProgress = false;
	}

	/**
	 * disposes the ChangeRecorder
	 */
	public void disposeChangeRecorder() {
		if (cr!=null) cr.dispose();
		if (chRes!=null){
			rs.getResources().remove(chRes);
			rs.getResources().remove(pmRes);
					//RefactorPersistence.INSTANCE.getProxyMapResource(res));
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
//	
	
	/**
	 * Convenience method that saves (persists)  the changes in the current recorder and proxyMap
	 * if recording, endRecording is called first, then the changes resource and proxyMap resources are saved
	 * 
	 * N.B. Editors may automatically save all resources in the same resource set when editor save is performed.
	 * Calling this extra save may cause synchronisation problems.
	 * 
	 */
	public void saveChanges() {
		if (recordingInProgress){
			endRecording();
		}
		try {
			chRes.save(Collections.EMPTY_MAP);
			pmRes.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	
	/**
	 * 
	 * @return
	 */
	private ChangeDescription getChangeDescription(){
		if (recordingInProgress){
			endRecording();
		}
		chRes = RefactorPersistence.INSTANCE.getChangesResource(res);
		proxyMap = RefactorPersistence.INSTANCE.getProxyMap(res);		
		EList<EObject> contents = chRes.getContents();
		EObject content = contents.size()>0? chRes.getContents().get(0) : null;
		ChangeDescription changes = content instanceof ChangeDescription? (ChangeDescription)content : null;
		return changes;
	}
	
	////////////////////////////// COMMANDS //////////////////////////////////
	
	protected class BeginRecordingCommand extends ChangeCommand {
//		ChangeRecorder cr;
		ChangeDescription changes;
		boolean resetChanges;
//		EventBNamedCommentedComponentElement component;
		 BeginRecordingCommand(ChangeDescription changes){
			super(new ChangeRecorder(chRes).endRecording());
//			this.cr = cr;
			this.changes = changes;
//			this.component = component;
			resetChanges = false;
		}
		@Override
		public void doExecute(){
			try{
				resetChanges = false;
				cr.beginRecording(changes, Collections.singleton(component));
			}catch (Exception e){
				cr.setEObjectToProxyURIMap(new HashMap<EObject,URI>());
				cr.beginRecording(null, Collections.singleton(component));
				resetChanges = true;
			}
		}
	}
	
	protected class EndRecordingCommand extends ChangeCommand {
		ChangeDescription changes;
//		ChangeRecorder cr;
//		Resource chRes;
		 EndRecordingCommand(){
			super(new ChangeRecorder(chRes).endRecording());
//			this.cr = cr;
//			this.chRes = chRes;
		}
		@Override
		public void doExecute(){
			changes = cr.endRecording();
			if (changes!=null){
				fixRefs();
				chRes.getContents().clear();
				chRes.getContents().add(0, changes);
				pmRes.getContents().clear();
				EObject m = RefactorPersistence.INSTANCE.convert(proxyMap);
				if (m!=null){
					pmRes.getContents().add(m);
				}
			}else{
				int i=0;
			}

		}
		
		private void fixRefs() {
			for (Entry<EObject, EList<FeatureChange>> change : changes.getObjectChanges()){
				EList<FeatureChange> abstractFeatureChanges = change.getValue();
				for (FeatureChange reverseFeatureChange : abstractFeatureChanges){
					EList<ListChange> listChanges = reverseFeatureChange.getListChanges();
					for (ListChange lc : listChanges){
						List<EObject> newList = new ArrayList<EObject>();
						for (EObject ref : lc.getReferenceValues()){
							URI uri = EcoreUtil.getURI(ref);
							EObject proxy = EMFCoreUtil.createProxy(ref.eClass(), fixURI(ref, uri));
							newList.add(proxy);
						}
						lc.getReferenceValues().clear();
						lc.getReferenceValues().addAll(newList);
					}
				}
			}
		}
		
		/**
		 * The change description persistence uses the default offset referencing scheme
		 * whereas the references created by our model elements will be our intrinsic ids
		 * Therefore we need to find all the references in the change description and convert
		 * the fragment part to containment offsets.
		 * 
		 * @param object
		 * @param uri
		 * @return
		 */
		private URI fixURI(EObject object, URI uri){
			EList<EObject> attach = changes.getObjectsToAttach();
			for (EObject o : attach){
				URI ouri = EcoreUtil.getURI(o);
				if (ouri.equals(uri)){
					URI fixedURI = uri.trimFragment().appendFragment("//@objectsToAttach."+attach.indexOf(o));
					return fixedURI;
				}
			}
//		NOT SURE IF THIS IS ALSO NEEDED - THESE ARE REFERENCES NOT CONTAINMENTS AND SEEM TO BE OK
//		EList<EObject> detach = changes.getObjectsToDetach();
//			for (EObject o : detach){
//				URI ouri = EcoreUtil.getURI(o);
//				if (ouri.equals(uri)){
//					URI fixedURI = URI.createURI("//@objectsToDetach."+detach.indexOf(o));
//					return fixedURI;
//				}
//			}
			return uri;
		}
		
		public ChangeDescription getChanges() { return changes; }
	}
	

}
