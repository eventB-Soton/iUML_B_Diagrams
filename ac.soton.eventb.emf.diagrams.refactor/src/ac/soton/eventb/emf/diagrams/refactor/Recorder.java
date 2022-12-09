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
package ac.soton.eventb.emf.diagrams.refactor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
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
import org.eventb.emf.core.EventBNamedCommentedComponentElement;

import ac.soton.eventb.emf.diagrams.refactor.persistence.RefactorPersistence;


public class Recorder {
	
	/**
	 * @since 1.0
	 */
	protected ChangeRecorder cr;
	/**
	 * @since 1.0
	 */
	protected Resource res;
	/**
	 * @since 1.0
	 */
	protected ResourceSet rs;
	/**
	 * @since 1.0
	 */
	protected Resource chRes;
	/**
	 * @since 1.0
	 */
	protected boolean recordingInProgress;
		
	/**
	 * @since 1.0
	 */
	protected EventBNamedCommentedComponentElement component;
	private TransactionalEditingDomain ed;
	
	protected Recorder(EventBNamedCommentedComponentElement component) throws IOException {
		super();
		this.component=component;
		res = component.eResource();
		rs = res.getResourceSet();
		ed = TransactionUtil.getEditingDomain(rs);
		recordingInProgress = false;
		chRes = RefactorPersistence.INSTANCE.getChangesResource(res);
		RefactorPersistence.INSTANCE.checkPreState(component);
	}
	
	/** 
	 * if re-factoring is enabled 
	 * 	gets a new change recorder 
	 * otherwise returns null
	 * 
	 */
	public static Recorder getNewRecorder(EventBNamedCommentedComponentElement component){
		if (Activator.getDefault().getPreferenceStore().getBoolean(Activator.PREFERENCES_REFACTORING_ENABLED)){
			try {
				return new Recorder(component);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		}else{
			return null;
		}
	}
	
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
		if (cr==null) 		
			cr = new ChangeRecorder();
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
		}
	}	
	
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
		try {
			chRes = RefactorPersistence.INSTANCE.getChangesResource(res);
			EList<EObject> contents = chRes.getContents();
			EObject content = contents.size()>0? chRes.getContents().get(0) : null;
			ChangeDescription changes = content instanceof ChangeDescription? (ChangeDescription)content : null;
			return changes;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	////////////////////////////// COMMANDS //////////////////////////////////
	
	/**
	 * @since 1.0
	 */
	protected class BeginRecordingCommand extends ChangeCommand {
		ChangeDescription changes;
		boolean resetChanges;
		 BeginRecordingCommand(ChangeDescription changes){
			super(new ChangeRecorder(chRes).endRecording());
			this.changes = changes;
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
	
	/**
	 * @since 1.0
	 */
	protected class EndRecordingCommand extends ChangeCommand {
		ChangeDescription changes;
		
		EndRecordingCommand(){
			super(new ChangeRecorder(chRes).endRecording());
		}
		
		@Override
		public void doExecute(){
			changes = cr.endRecording();
			if (changes!=null){
				fixRefs();
				chRes.getContents().clear();
				chRes.getContents().add(0, changes);
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
							InternalEObject proxy = (InternalEObject) ref.eClass().getEPackage().getEFactoryInstance().create(ref.eClass());
							proxy.eSetProxyURI(fixURI(ref, uri));
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
			return uri;
		}
		
		public ChangeDescription getChanges() { return changes; }
	}

}
