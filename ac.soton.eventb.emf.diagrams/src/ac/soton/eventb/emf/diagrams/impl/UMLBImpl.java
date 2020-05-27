/**
 * Copyright (c) 2012-2020 - University of Southampton.
 * All rights reserved. This program and the accompanying materials  are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * $Id$
 */
package ac.soton.eventb.emf.diagrams.impl;

import ac.soton.eventb.emf.diagrams.Diagram;
import ac.soton.eventb.emf.diagrams.DiagramOwner;
import ac.soton.eventb.emf.diagrams.DiagramsPackage;
import ac.soton.eventb.emf.diagrams.UMLB;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eventb.emf.core.EventBNamedCommentedComponentElement;

import org.eventb.emf.core.impl.EventBNamedCommentedElementImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>UMLB</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * <ul>
 *   <li>{@link ac.soton.eventb.emf.diagrams.impl.UMLBImpl#getDiagrams <em>Diagrams</em>}</li>
 *   <li>{@link ac.soton.eventb.emf.diagrams.impl.UMLBImpl#getElaborates <em>Elaborates</em>}</li>
 *   <li>{@link ac.soton.eventb.emf.diagrams.impl.UMLBImpl#getRefines <em>Refines</em>}</li>
 * </ul>
 * </p>
 *
 * @generated
 * @since 5.0
 */
public class UMLBImpl extends EventBNamedCommentedElementImpl implements UMLB {
	/**
	 * The cached value of the '{@link #getDiagrams() <em>Diagrams</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDiagrams()
	 * @generated
	 * @ordered
	 */
	protected EList<Diagram> diagrams;

	/**
	 * The cached value of the '{@link #getElaborates() <em>Elaborates</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getElaborates()
	 * @generated
	 * @ordered
	 */
	protected EventBNamedCommentedComponentElement elaborates;

	/**
	 * The cached value of the '{@link #getRefines() <em>Refines</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRefines()
	 * @generated
	 * @ordered
	 */
	protected UMLB refines;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected UMLBImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DiagramsPackage.Literals.UMLB;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EList<Diagram> getDiagrams() {
		if (diagrams == null) {
			diagrams = new EObjectContainmentEList.Resolving<Diagram>(Diagram.class, this, DiagramsPackage.UMLB__DIAGRAMS);
		}
		return diagrams;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EventBNamedCommentedComponentElement getElaborates() {
		if (elaborates != null && elaborates.eIsProxy()) {
			InternalEObject oldElaborates = (InternalEObject)elaborates;
			elaborates = (EventBNamedCommentedComponentElement)eResolveProxy(oldElaborates);
			if (elaborates != oldElaborates) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, DiagramsPackage.UMLB__ELABORATES, oldElaborates, elaborates));
			}
		}
		return elaborates;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public EventBNamedCommentedComponentElement basicGetElaborates() {
		return elaborates;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setElaborates(EventBNamedCommentedComponentElement newElaborates) {
		EventBNamedCommentedComponentElement oldElaborates = elaborates;
		elaborates = newElaborates;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramsPackage.UMLB__ELABORATES, oldElaborates, elaborates));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UMLB getRefines() {
		if (refines != null && refines.eIsProxy()) {
			InternalEObject oldRefines = (InternalEObject)refines;
			refines = (UMLB)eResolveProxy(oldRefines);
			if (refines != oldRefines) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, DiagramsPackage.UMLB__REFINES, oldRefines, refines));
			}
		}
		return refines;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public UMLB basicGetRefines() {
		return refines;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void setRefines(UMLB newRefines) {
		UMLB oldRefines = refines;
		refines = newRefines;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DiagramsPackage.UMLB__REFINES, oldRefines, refines));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case DiagramsPackage.UMLB__DIAGRAMS:
				return ((InternalEList<?>)getDiagrams()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DiagramsPackage.UMLB__DIAGRAMS:
				return getDiagrams();
			case DiagramsPackage.UMLB__ELABORATES:
				if (resolve) return getElaborates();
				return basicGetElaborates();
			case DiagramsPackage.UMLB__REFINES:
				if (resolve) return getRefines();
				return basicGetRefines();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case DiagramsPackage.UMLB__DIAGRAMS:
				getDiagrams().clear();
				getDiagrams().addAll((Collection<? extends Diagram>)newValue);
				return;
			case DiagramsPackage.UMLB__ELABORATES:
				setElaborates((EventBNamedCommentedComponentElement)newValue);
				return;
			case DiagramsPackage.UMLB__REFINES:
				setRefines((UMLB)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case DiagramsPackage.UMLB__DIAGRAMS:
				getDiagrams().clear();
				return;
			case DiagramsPackage.UMLB__ELABORATES:
				setElaborates((EventBNamedCommentedComponentElement)null);
				return;
			case DiagramsPackage.UMLB__REFINES:
				setRefines((UMLB)null);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case DiagramsPackage.UMLB__DIAGRAMS:
				return diagrams != null && !diagrams.isEmpty();
			case DiagramsPackage.UMLB__ELABORATES:
				return elaborates != null;
			case DiagramsPackage.UMLB__REFINES:
				return refines != null;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eBaseStructuralFeatureID(int derivedFeatureID, Class<?> baseClass) {
		if (baseClass == DiagramOwner.class) {
			switch (derivedFeatureID) {
				case DiagramsPackage.UMLB__DIAGRAMS: return DiagramsPackage.DIAGRAM_OWNER__DIAGRAMS;
				default: return -1;
			}
		}
		return super.eBaseStructuralFeatureID(derivedFeatureID, baseClass);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int eDerivedStructuralFeatureID(int baseFeatureID, Class<?> baseClass) {
		if (baseClass == DiagramOwner.class) {
			switch (baseFeatureID) {
				case DiagramsPackage.DIAGRAM_OWNER__DIAGRAMS: return DiagramsPackage.UMLB__DIAGRAMS;
				default: return -1;
			}
		}
		return super.eDerivedStructuralFeatureID(baseFeatureID, baseClass);
	}

} //UMLBImpl
