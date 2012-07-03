package ac.soton.eventb.emf.diagrams.navigator.refiner;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eventb.core.basis.EventBElement;
import org.eventb.emf.persistence.factory.RodinResource;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRefinementParticipant;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinDBException;

import ac.soton.eventb.emf.core.extension.persistence.ISerialisedExtension;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;
import ac.soton.eventb.emf.diagrams.navigator.provider.IDiagramProvider;

public class DiagramCopier implements IRefinementParticipant {

	@Override
	public void process(IInternalElement targetRoot,
			IInternalElement sourceRoot, IProgressMonitor monitor)
			throws RodinDBException {
		
		IPath file = targetRoot.getPath();
		//file.

//		RodinResource emfResource = new RodinResource();
//		emfResource.setURI(file.getLocationURI());
//		IRodinElement[] children = getAllChildren(sourceRoot);
//		for (IRodinElement child : children){
//			if (child instanceof ISerialisedExtension){
//				String serialised = ((ISerialisedExtension)child).getSerialised();
//				
//				if (element instanceof IAdaptable) {
//					EObject eobject = (EObject) ((IAdaptable) element).getAdapter(EObject.class);
//					
//					// find diagram provider
//					Map<String, IDiagramProvider> registry = DiagramsNavigatorExtensionPlugin.getDefault().getDiagramProviderRegistry();
//					String type = eobject.eClass().getName();
//					IDiagramProvider provider = registry.get(type);
//					if (provider == null)
//						return;
//			}
//		}
		
		
	}
	


	IRodinElement[] getAllChildren (IInternalElement parent) throws RodinDBException{
		return parent.getChildren();
	}
}
