/*******************************************************************************
 * Copyright (c) 2014, 2020 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.navigator.refiner;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
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
	 * and to fix the references which, in our case, are messed up by AbstractElementRefiner
	 * 
	 * (non-Javadoc)
	 * @see ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner#copyReferences(org.eclipse.emf.ecore.EObject, org.eclipse.emf.ecore.util.EcoreUtil.Copier, org.eclipse.emf.common.util.URI, org.eclipse.emf.common.util.URI, org.eventb.emf.core.EventBNamedCommentedComponentElement, java.lang.String, ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner.Mode)
	 */
	@Override
	protected void copyReferences(
			EObject concreteElement, 
			Copier copier, 
			URI abstractUri, 
			URI concreteResourceURI, 
			EventBNamedCommentedComponentElement concreteComponent, 
			String concreteComponentName, 
			Mode mode) {
		
		if (concreteElement instanceof UMLB) {
			((UMLB)concreteElement).setName(concreteComponentName==null? concreteResourceURI.trimFileExtension().lastSegment() : concreteComponentName);
		}
		
		super.copyReferences(concreteElement, copier, abstractUri, concreteResourceURI, concreteComponent, concreteComponentName, mode);
		
		//the AbstractElementRefiner gets the fragment part of the reference URI wrong because 
		// it assumes the refined element will be added to a Machine or Context
		// therefore we find each such reference and fix it
		String abstractFileName = abstractUri.trimFileExtension().lastSegment();
		if (concreteElement instanceof UMLB) {
			TreeIterator<EObject> contents = ((UMLB)concreteElement).eAllContents();
			while (contents.hasNext()) {
				EObject element = contents.next();
				for (EReference feature : element.eClass().getEAllReferences()) {
					if (!feature.isContainment()) {
						if (feature.isMany()) {
							@SuppressWarnings("unchecked")
							EList<InternalEObject> refList = (EList<InternalEObject>) element.eGet(feature, false);
							for (InternalEObject refValue : refList) {
								fix_fragment(concreteComponentName, abstractFileName, refValue);								
							}
						}else {
							InternalEObject refValue = (InternalEObject) element.eGet(feature, false);
							fix_fragment(concreteComponentName, abstractFileName, refValue);
						}
					}
				}
			}
		}
	}

	/**
	 * @param concreteComponentName
	 * @param abstractFileName
	 * @param refValue
	 */
	private void fix_fragment(String concreteComponentName, String abstractFileName, InternalEObject refValue) {
		if (refValue!=null && refValue.eIsProxy()) {
			URI uri = refValue.eProxyURI();
			String fileName = uri.trimFileExtension().lastSegment();
			if (fileName.equals(concreteComponentName)) {
				String fragment = uri.fragment();
				int idStart = fragment.lastIndexOf("::")+2;
				String id = fragment.substring(idStart);
				if (id.startsWith(concreteComponentName+"."+abstractFileName+".")) {
					id = id.replace(concreteComponentName+"."+abstractFileName, concreteComponentName);
					fragment = fragment.substring(0, idStart)+id;
					uri = uri.trimFragment().appendFragment(fragment);
					refValue.eSetProxyURI(uri);
				}
			}		
		}
	}
	
}
