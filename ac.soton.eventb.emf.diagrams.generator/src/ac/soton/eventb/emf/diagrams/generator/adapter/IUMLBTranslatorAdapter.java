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
package ac.soton.eventb.emf.diagrams.generator.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBObject;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.machine.Machine;

import ac.soton.emf.translator.configuration.IAdapter;
import ac.soton.emf.translator.eventb.adapter.EventBTranslatorAdapter;
import ac.soton.emf.translator.eventb.utils.Utils;
import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;


/**
 * This implementation of IAdapter extends the EventBTranslatorAdapter to 
 * make it more suitable for iUML-B translation needs
 * 
 * @author cfs
 * @since 4.0
 *
 */

public class IUMLBTranslatorAdapter extends EventBTranslatorAdapter implements IAdapter {

	
	/**
	 * @see ac.soton.emf.translator.configuration.EventBTranslatorAdapter#getAffectedResources(org.eclipse.emf.transaction.TransactionalEditingDomain, org.eclipse.emf.ecore.EObject)
	 * 
	 * This implementation restricts the affected resources to those that are in scope of the resource containing the target element.
	 * I.e. the target element's containing machine or context and any seen or extended contexts 
	 * 
	 * @param editingDomain
	 * @param sourceElement
	 * @return list of affected Resources
	 */
	public Collection<Resource> getAffectedResources(TransactionalEditingDomain editingDomain, EObject sourceElement) throws IOException {
		List<Resource> affectedResources = new ArrayList<Resource>();
		if (sourceElement instanceof EventBElement){
			EventBNamedCommentedComponentElement targetComponent = Utils.getTranslationTarget();			
			List<EventBNamedCommentedComponentElement> components = getComponentList(targetComponent);
			for (EventBNamedCommentedComponentElement component : components){
				if (component.eIsProxy()) {
					component = (EventBNamedCommentedComponentElement) EcoreUtil.resolve(component, editingDomain.getResourceSet());
				}
				affectedResources.add(component.eResource());
			}
		}
		return affectedResources;
	}
	

	private List<EventBNamedCommentedComponentElement> getComponentList(EventBNamedCommentedComponentElement component) {
		List<EventBNamedCommentedComponentElement> list =  new ArrayList<EventBNamedCommentedComponentElement>() ;
		if (component instanceof Machine){
			Machine m = ((Machine)component);
			list.add(m);
			for (Context c : m.getSees()){
				list.addAll(getComponentList(c));
			}			
		}else if (component instanceof Context){
			Context c = ((Context)component);
			list.add(c);
			for (Context x : c.getExtends()){
				list.addAll(getComponentList(x));
			}
		}
		return list;
	}
	
	/* (non-Javadoc)
	 * @see ac.soton.emf.translator.eventb.adapter.EventBTranslatorAdapter#getTargetComponent(java.lang.Object)
	 */
	@Override
	public Object getTargetComponent(Object sourceElement) {
		Object container = null;
		if (sourceElement instanceof EventBObject) {
			container = DiagramUtils.getTranslationTarget((EventBObject)sourceElement);
		}
		return container == null? 
			super.getTargetComponent(sourceElement) :
			container;
	}
	
}
