/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package ac.soton.eventb.emf.diagrams;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see ac.soton.eventb.emf.diagrams.DiagramsFactory
 * @model kind="package"
 * @generated
 */
public interface DiagramsPackage extends EPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String copyright = "Copyright (c) 2012-14 - University of Southampton.\rAll rights reserved. This program and the accompanying materials  are made\ravailable under the terms of the Eclipse Public License v1.0 which accompanies this \rdistribution, and is available at http://www.eclipse.org/legal/epl-v10.html";

	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "diagrams";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://soton.ac.uk/models/eventb/diagrams";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "diagrams";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DiagramsPackage eINSTANCE = ac.soton.eventb.emf.diagrams.impl.DiagramsPackageImpl.init();

	/**
	 * The meta object id for the '{@link ac.soton.eventb.emf.diagrams.impl.DiagramImpl <em>Diagram</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ac.soton.eventb.emf.diagrams.impl.DiagramImpl
	 * @see ac.soton.eventb.emf.diagrams.impl.DiagramsPackageImpl#getDiagram()
	 * @generated
	 */
	int DIAGRAM = 0;

	/**
	 * The number of structural features of the '<em>Diagram</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGRAM_FEATURE_COUNT = 0;

	/**
	 * The meta object id for the '{@link ac.soton.eventb.emf.diagrams.impl.DiagramOwnerImpl <em>Diagram Owner</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ac.soton.eventb.emf.diagrams.impl.DiagramOwnerImpl
	 * @see ac.soton.eventb.emf.diagrams.impl.DiagramsPackageImpl#getDiagramOwner()
	 * @generated
	 */
	int DIAGRAM_OWNER = 1;

	/**
	 * The feature id for the '<em><b>Diagrams</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGRAM_OWNER__DIAGRAMS = 0;

	/**
	 * The number of structural features of the '<em>Diagram Owner</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DIAGRAM_OWNER_FEATURE_COUNT = 1;


	/**
	 * Returns the meta object for class '{@link ac.soton.eventb.emf.diagrams.Diagram <em>Diagram</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Diagram</em>'.
	 * @see ac.soton.eventb.emf.diagrams.Diagram
	 * @generated
	 */
	EClass getDiagram();

	/**
	 * Returns the meta object for class '{@link ac.soton.eventb.emf.diagrams.DiagramOwner <em>Diagram Owner</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Diagram Owner</em>'.
	 * @see ac.soton.eventb.emf.diagrams.DiagramOwner
	 * @generated
	 */
	EClass getDiagramOwner();

	/**
	 * Returns the meta object for the containment reference list '{@link ac.soton.eventb.emf.diagrams.DiagramOwner#getDiagrams <em>Diagrams</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Diagrams</em>'.
	 * @see ac.soton.eventb.emf.diagrams.DiagramOwner#getDiagrams()
	 * @see #getDiagramOwner()
	 * @generated
	 */
	EReference getDiagramOwner_Diagrams();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	DiagramsFactory getDiagramsFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link ac.soton.eventb.emf.diagrams.impl.DiagramImpl <em>Diagram</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ac.soton.eventb.emf.diagrams.impl.DiagramImpl
		 * @see ac.soton.eventb.emf.diagrams.impl.DiagramsPackageImpl#getDiagram()
		 * @generated
		 */
		EClass DIAGRAM = eINSTANCE.getDiagram();

		/**
		 * The meta object literal for the '{@link ac.soton.eventb.emf.diagrams.impl.DiagramOwnerImpl <em>Diagram Owner</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ac.soton.eventb.emf.diagrams.impl.DiagramOwnerImpl
		 * @see ac.soton.eventb.emf.diagrams.impl.DiagramsPackageImpl#getDiagramOwner()
		 * @generated
		 */
		EClass DIAGRAM_OWNER = eINSTANCE.getDiagramOwner();

		/**
		 * The meta object literal for the '<em><b>Diagrams</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference DIAGRAM_OWNER__DIAGRAMS = eINSTANCE.getDiagramOwner_Diagrams();

	}

} //DiagramsPackage
