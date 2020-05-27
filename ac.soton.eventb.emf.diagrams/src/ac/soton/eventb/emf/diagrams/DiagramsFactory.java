/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package ac.soton.eventb.emf.diagrams;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see ac.soton.eventb.emf.diagrams.DiagramsPackage
 * @generated
 */
public interface DiagramsFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DiagramsFactory eINSTANCE = ac.soton.eventb.emf.diagrams.impl.DiagramsFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>UMLB</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>UMLB</em>'.
	 * @generated
	 * @since 5.0
	 */
	UMLB createUMLB();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	DiagramsPackage getDiagramsPackage();

} //DiagramsFactory
