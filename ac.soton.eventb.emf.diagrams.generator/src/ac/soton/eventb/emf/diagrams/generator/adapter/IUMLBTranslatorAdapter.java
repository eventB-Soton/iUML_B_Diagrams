/*******************************************************************************
 *  Copyright (c) 2017 University of Southampton.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *   
 *  Contributors:
 *  University of Southampton - Initial implementation
 *******************************************************************************/
package ac.soton.eventb.emf.diagrams.generator.adapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.machine.Machine;

import ac.soton.emf.translator.configuration.IAdapter;
import ac.soton.emf.translator.eventb.adapter.EventBTranslatorAdapter;


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
	 * This implementation restricts the affected resources to those that are in scope of the resource containing the source element.
	 * I.e. the source element's containing machine or context and any seen or extended contexts 
	 * 
	 * @param editingDomain
	 * @param sourceElement
	 * @return list of affected Resources
	 */
	public Collection<Resource> getAffectedResources(TransactionalEditingDomain editingDomain, EObject sourceElement) throws IOException {
		List<Resource> affectedResources = new ArrayList<Resource>();
		if (sourceElement instanceof EventBElement){
			EventBNamedCommentedComponentElement sourceComponent = (EventBNamedCommentedComponentElement) ((EventBElement)sourceElement).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
			List<EventBNamedCommentedComponentElement> components = getComponentList(sourceComponent);
			for (EventBNamedCommentedComponentElement component : components){
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
	
	
}
