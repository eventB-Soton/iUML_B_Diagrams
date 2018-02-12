/*******************************************************************************
 *  (c) Crown owned copyright 2011, 2017 (UK Ministry of Defence)
 *  
 *  All rights reserved. This program and the accompanying materials  are 
 *  made available under the terms of the Eclipse Public License v1.0 which
 *  accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *  
 *  This is to identify the UK Ministry of Defence as owners along with the
 *   license rights provided.
 *  
 *  Contributors:
 *  			University of Southampton - Initial implementation
 *******************************************************************************/
package ac.soton.eventb.emf.diagrams.refactor.impl;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.util.TransactionUtil;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;

import ac.soton.eventb.emf.diagrams.refactor.persistence.RefactorPersistence;

public class RefactorAssistant {

	protected ChangeDescription changes;
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
		try {
			chRes = RefactorPersistence.INSTANCE.getChangesResource(res);
			EObject content = chRes.getContents().size()>0? chRes.getContents().get(0) : null;
			changes = content instanceof ChangeDescription? (ChangeDescription)content : null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This constructor can be used when the EventB component does not exist
	 * 
	 * @param componentUri
	 */
	public RefactorAssistant(URI componentUri, TransactionalEditingDomain ed) {
		super();
		this.component=null;
		res = null;
		rs = ed.getResourceSet();
		this.ed = ed;
		try {
			chRes = RefactorPersistence.INSTANCE.getChangesResource(rs, componentUri);
			EObject content = chRes.getContents().size()>0? chRes.getContents().get(0) : null;
			changes = content instanceof ChangeDescription? (ChangeDescription)content : null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	/**
	 * tests whether there are any changes yet
	 * @return true if the changes resource is not empty
	 */
	public boolean hasChanges() {
		return changes!=null;
	}

	/**
	 * disposes the ChangeRecorder
	 */
	public void disposeChangeRecords() {
		if (chRes!=null){
			rs.getResources().remove(chRes);
			try {
				rs.getResources().remove(
						RefactorPersistence.INSTANCE.getPreStateResource(res));			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Deletes the Change Records from the file system
	 * 
	 * 
	 */
	public void deleteChangeRecords() {
		try {
			disposeChangeRecords();
			chRes.eSetDeliver(false);
			chRes.delete(Collections.EMPTY_MAP);
			
			Resource pre = RefactorPersistence.INSTANCE.getPreStateResource(res);
			pre.eSetDeliver(false);
			pre.delete(Collections.EMPTY_MAP);
			
			RefactorPersistence.INSTANCE.deleteEquivalenceMap(res);
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
