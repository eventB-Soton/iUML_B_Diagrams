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
import org.eventb.emf.core.CorePackage;

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
 * @since 5.0
 */
public interface DiagramsPackage extends EPackage {
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
	String eNS_URI = "http://soton.ac.uk/models/eventb/diagrams/2020";

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
	 * The meta object id for the '{@link ac.soton.eventb.emf.diagrams.impl.UMLBImpl <em>UMLB</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see ac.soton.eventb.emf.diagrams.impl.UMLBImpl
	 * @see ac.soton.eventb.emf.diagrams.impl.DiagramsPackageImpl#getUMLB()
	 * @generated
	 * @since 5.0
	 */
	int UMLB = 2;

	/**
	 * The feature id for the '<em><b>Annotations</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__ANNOTATIONS = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT__ANNOTATIONS;

	/**
	 * The feature id for the '<em><b>Extensions</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__EXTENSIONS = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT__EXTENSIONS;

	/**
	 * The feature id for the '<em><b>Attributes</b></em>' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__ATTRIBUTES = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT__ATTRIBUTES;

	/**
	 * The feature id for the '<em><b>Reference</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__REFERENCE = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT__REFERENCE;

	/**
	 * The feature id for the '<em><b>Generated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__GENERATED = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT__GENERATED;

	/**
	 * The feature id for the '<em><b>Local Generated</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__LOCAL_GENERATED = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT__LOCAL_GENERATED;

	/**
	 * The feature id for the '<em><b>Internal Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__INTERNAL_ID = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT__INTERNAL_ID;

	/**
	 * The feature id for the '<em><b>Comment</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__COMMENT = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT__COMMENT;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__NAME = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT__NAME;

	/**
	 * The feature id for the '<em><b>Diagrams</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__DIAGRAMS = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Elaborates</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__ELABORATES = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Refines</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB__REFINES = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT_FEATURE_COUNT + 2;

	/**
	 * The number of structural features of the '<em>UMLB</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 * @since 5.0
	 */
	int UMLB_FEATURE_COUNT = CorePackage.EVENT_BNAMED_COMMENTED_ELEMENT_FEATURE_COUNT + 3;


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
	 * Returns the meta object for class '{@link ac.soton.eventb.emf.diagrams.UMLB <em>UMLB</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>UMLB</em>'.
	 * @see ac.soton.eventb.emf.diagrams.UMLB
	 * @generated
	 * @since 5.0
	 */
	EClass getUMLB();

	/**
	 * Returns the meta object for the reference '{@link ac.soton.eventb.emf.diagrams.UMLB#getElaborates <em>Elaborates</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Elaborates</em>'.
	 * @see ac.soton.eventb.emf.diagrams.UMLB#getElaborates()
	 * @see #getUMLB()
	 * @generated
	 * @since 5.0
	 */
	EReference getUMLB_Elaborates();

	/**
	 * Returns the meta object for the reference '{@link ac.soton.eventb.emf.diagrams.UMLB#getRefines <em>Refines</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the reference '<em>Refines</em>'.
	 * @see ac.soton.eventb.emf.diagrams.UMLB#getRefines()
	 * @see #getUMLB()
	 * @generated
	 * @since 5.0
	 */
	EReference getUMLB_Refines();

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

		/**
		 * The meta object literal for the '{@link ac.soton.eventb.emf.diagrams.impl.UMLBImpl <em>UMLB</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see ac.soton.eventb.emf.diagrams.impl.UMLBImpl
		 * @see ac.soton.eventb.emf.diagrams.impl.DiagramsPackageImpl#getUMLB()
		 * @generated
		 * @since 5.0
		 */
		EClass UMLB = eINSTANCE.getUMLB();

		/**
		 * The meta object literal for the '<em><b>Elaborates</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 * @since 5.0
		 */
		EReference UMLB__ELABORATES = eINSTANCE.getUMLB_Elaborates();

		/**
		 * The meta object literal for the '<em><b>Refines</b></em>' reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 * @since 5.0
		 */
		EReference UMLB__REFINES = eINSTANCE.getUMLB_Refines();

	}

} //DiagramsPackage
