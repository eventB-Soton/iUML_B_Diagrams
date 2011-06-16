/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package ac.soton.eventb.emf.diagrams;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Diagram Owner</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link ac.soton.eventb.emf.diagrams.DiagramOwner#getDiagrams <em>Diagrams</em>}</li>
 * </ul>
 * </p>
 *
 * @see ac.soton.eventb.emf.diagrams.DiagramsPackage#getDiagramOwner()
 * @model abstract="true"
 * @generated
 */
public interface DiagramOwner extends EObject {
	/**
	 * Returns the value of the '<em><b>Diagrams</b></em>' containment reference list.
	 * The list contents are of type {@link ac.soton.eventb.emf.diagrams.Diagram}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Diagrams</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Diagrams</em>' containment reference list.
	 * @see ac.soton.eventb.emf.diagrams.DiagramsPackage#getDiagramOwner_Diagrams()
	 * @model containment="true" resolveProxies="true"
	 * @generated
	 */
	EList<Diagram> getDiagrams();

} // DiagramOwner
