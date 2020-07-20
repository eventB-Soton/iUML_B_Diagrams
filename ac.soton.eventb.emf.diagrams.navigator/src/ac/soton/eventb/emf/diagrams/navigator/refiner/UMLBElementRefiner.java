/*******************************************************************************
 * Copyright (c) 2020-2020 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package ac.soton.eventb.emf.diagrams.navigator.refiner;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;

import ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner;
import ac.soton.eventb.emf.diagrams.DiagramsPackage;
import ac.soton.eventb.emf.diagrams.UMLB;

/**
 * UMLB Element Refiner 
 * 
 * This is called by the refinement processes when a UMLB element is found.
 * 
 * @author cfsnook
 *
 */

public class UMLBElementRefiner extends AbstractElementRefiner {

//	public UMLBElementRefiner() {
//		// TODO Auto-generated constructor stub
//	}


	@Override
	protected void populateFilterByTypeList(List<EClass> filterList) {
		//do nothing - nothing needs filtering
	}

	/**
	 * refines should be handled by extending the chain of refines references - i.e. point to the UMLB being refined
	 * elaborates should point to the machine that refines the machine elaborated by the UMLB being refined.
	 */
	@Override
	protected void populateReferenceMap(Map<EReference, RefHandling> referencemap) {
		referencemap.put(DiagramsPackage.Literals.UMLB__REFINES, RefHandling.CHAIN);
		referencemap.put(DiagramsPackage.Literals.UMLB__ELABORATES, RefHandling.EQUIV);
	}

	/**
	 * Overridden to change the name of the UMLB
	 * 
	 * (non-Javadoc)
	 * @see ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner#copyReferences(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.util.EcoreUtil.Copier, org.eclipse.emf.common.util.URI, org.eclipse.emf.common.util.URI, org.eventb.emf.core.EventBNamedCommentedComponentElement, java.lang.String, ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner.Mode)
	 */
	@Override
	protected void copyReferences(EObject concreteElement, Copier copier, URI abstractUri, URI concreteResourceURI, String concreteComponentName, Mode mode) {
		
		if (concreteElement instanceof UMLB) {
			((UMLB)concreteElement).setName(concreteComponentName==null? concreteResourceURI.trimFileExtension().lastSegment() : concreteComponentName);
		}
		
		super.copyReferences(concreteElement, copier, abstractUri, concreteResourceURI, concreteComponentName, mode);
	}
	
//	/* (non-Javadoc)
//	 * @see ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner#getEquivalentObject(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EObject)
//	 */
//	@Override
//	public EventBObject getEquivalentObject(EObject concreteParent, EObject abstractObject) {
//		// TODO Auto-generated method stub
//		return super.getEquivalentObject(concreteParent, abstractObject);
//	}
//
//	/* (non-Javadoc)
//	 * @see ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner#getEquivalentObject(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.EStructuralFeature, org.eclipse.emf.ecore.EObject)
//	 */
//	@Override
//	public EventBObject getEquivalentObject(EObject concreteParent, EStructuralFeature feature, EObject abstractObject) {
//		// TODO Auto-generated method stub
//		return super.getEquivalentObject(concreteParent, feature, abstractObject);
//	}

}
