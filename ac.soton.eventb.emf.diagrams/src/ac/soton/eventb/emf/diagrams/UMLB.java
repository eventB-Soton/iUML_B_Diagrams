/**
 * Copyright (c) 2012-2020 - University of Southampton.
 * All rights reserved. This program and the accompanying materials  are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * $Id$
 */
package ac.soton.eventb.emf.diagrams;

import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBNamedCommentedElement;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>UMLB</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link ac.soton.eventb.emf.diagrams.UMLB#getElaborates <em>Elaborates</em>}</li>
 *   <li>{@link ac.soton.eventb.emf.diagrams.UMLB#getRefines <em>Refines</em>}</li>
 * </ul>
 * </p>
 *
 * @see ac.soton.eventb.emf.diagrams.DiagramsPackage#getUMLB()
 * @model
 * @generated
 * @since 5.0
 */
public interface UMLB extends EventBNamedCommentedElement, DiagramOwner {
	/**
	 * Returns the value of the '<em><b>Elaborates</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Elaborates</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Elaborates</em>' reference.
	 * @see #setElaborates(EventBNamedCommentedComponentElement)
	 * @see ac.soton.eventb.emf.diagrams.DiagramsPackage#getUMLB_Elaborates()
	 * @model
	 * @generated
	 */
	EventBNamedCommentedComponentElement getElaborates();

	/**
	 * Sets the value of the '{@link ac.soton.eventb.emf.diagrams.UMLB#getElaborates <em>Elaborates</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Elaborates</em>' reference.
	 * @see #getElaborates()
	 * @generated
	 */
	void setElaborates(EventBNamedCommentedComponentElement value);

	/**
	 * Returns the value of the '<em><b>Refines</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Refines</em>' reference isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Refines</em>' reference.
	 * @see #setRefines(UMLB)
	 * @see ac.soton.eventb.emf.diagrams.DiagramsPackage#getUMLB_Refines()
	 * @model
	 * @generated
	 */
	UMLB getRefines();

	/**
	 * Sets the value of the '{@link ac.soton.eventb.emf.diagrams.UMLB#getRefines <em>Refines</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Refines</em>' reference.
	 * @see #getRefines()
	 * @generated
	 */
	void setRefines(UMLB value);

} // UMLB
