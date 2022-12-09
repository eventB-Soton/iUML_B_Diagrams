package ac.soton.eventb.emf.diagrams.navigator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.context.ContextFactory;
import org.eventb.emf.core.context.ContextPackage;
import org.eventb.emf.core.impl.EventBElementImpl;
import org.eventb.emf.core.machine.MachineFactory;
import org.eventb.emf.core.machine.MachinePackage;

import ac.soton.eventb.emf.diagrams.DiagramsFactory;
import ac.soton.eventb.emf.diagrams.DiagramsPackage;
import ac.soton.eventb.emf.diagrams.UMLB;

/**
 * @since 3.3
 */
public class UmlbDiagramUtils {

	public static final String MACHINE_FILE_EXTENSION = "bum"; //The file extension for machine files.
	public static final String CONTEXT_FILE_EXTENSION = "buc"; //The file extension for context files.
	public static final String UMLB_FILE_EXTENSION = "umlb"; //The file extension for umlb files.
	
	/**
	 * Returns a proxy representing the Event-B component (Machine or Context) to be elaborated
	 * (A proxy is returned to avoid having to load the component)
	 * the component file name must have an appropriate extension for a component, otherwise null will be returned
	 * Note that the component does not necessarily exist (existence is not checked)
	 * 
	 * @param the name of the project that contains the component
	 * @param the file name (including extension) of the component
	 * 
	 * @return a proxy constructed from the parameters (or null)
	 * @throws CoreException
	 */
	public static EventBNamedCommentedComponentElement createElaboratedComponentProxy(String projectName, String componentFileName) throws CoreException {
		EObject proxy = UmlbDiagramUtils.createRootElementProxy(
				projectName, 
				componentFileName
				);
		return proxy instanceof EventBNamedCommentedComponentElement? 
			(EventBNamedCommentedComponentElement) proxy : null;
	}
	
	/**
	 * Returns a proxy representing the UMLB model to be refined
	 * (A proxy is returned to avoid having to load the model)
	 * the UMLB file name must have an appropriate extension for a UMLB, otherwise null will be returned
	 * Note that the UMLB does not necessarily exist (existence is not checked)
	 * 
	 * @param the name of the project that contains the UMLB
	 * @param the file name (including extension) of the UMLB
	 * 
	 * @return a proxy constructed from the parameters (or null)
	 * @throws CoreException
	 */
	public static UMLB createRefinedUmlbProxy(String projectName, String umlbFileName) throws CoreException {
		EObject proxy = UmlbDiagramUtils.createRootElementProxy(
				projectName, 
				umlbFileName
				);
		return proxy instanceof UMLB? 
			(UMLB) proxy : null;
	}
	
	
	/**
	 * Returns a proxy representing the root element of a model in a file given the project name and file name.
	 * The root element has the same name as the file name and its type depends on the file extension.
	 * (A proxy is returned to avoid having to load the component)
	 * 
	 * @param  projectName
	 * @param  fileName
	 * 
	 * @return proxy
	 * @throws CoreException
	 */
	public static EObject createRootElementProxy(String projectName, String fileName) throws CoreException {
		int lio = fileName.lastIndexOf('.');
		if(lio<1) return null;
		String elementName = fileName.substring(0,lio);
		String extension = fileName.substring(lio+1);
		return createRootElementProxy(projectName, elementName, extension);
	}
	
	/**
	 * Returns a proxy representing the root element of a model in a file given the project name, element name and extension.
	 * The root element has the same name as the file name and its type depends on the file extension.
	 * (A proxy is returned to avoid having to load the component)
	 * @param  projectName
	 * @param  fileName
	 * 
	 * @return proxy
	 * @throws CoreException
	 */
	public static EObject createRootElementProxy(String projectName, String elementName, String extension) throws CoreException {
		EObject element= createElementFromExtension(extension);
		if (element instanceof InternalEObject){
			((InternalEObject)element).eSetProxyURI(
					URI.createPlatformResourceURI(projectName, true) 	//project name
					  .appendSegment(elementName)						//resource name
					  .appendFileExtension(extension)					//file extension
					  .appendFragment(((EventBElementImpl)element).getElementTypePrefix()+"::"+elementName));
		}
		return element;
	}

	/**
	 * Returns a newly created element corresponding to the given extension (e.g. "bum" --> Machine)
	 * or null if the extension is not recognised as a file type that contains a known root element
	 * 
	 * @param extension
	 * @param element
	 * @return
	 */
	private static EObject createElementFromExtension(String extension) {
		if ("bum".equals(extension)) {
			return MachineFactory.eINSTANCE.create(MachinePackage.Literals.MACHINE);
		}else if("buc".equals(extension)) {
			return ContextFactory.eINSTANCE.create(ContextPackage.Literals.CONTEXT);
		}else if (UMLB_FILE_EXTENSION.equals(extension)) {
			return DiagramsFactory.eINSTANCE.create(DiagramsPackage.Literals.UMLB);
		}else return null;
	}
	
}
