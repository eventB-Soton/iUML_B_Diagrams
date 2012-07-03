package ac.soton.eventb.emf.diagrams.navigator.refiner;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.machine.Machine;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRefinementParticipant;
import org.rodinp.core.RodinDBException;

import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;
import ac.soton.eventb.emf.diagrams.navigator.provider.IDiagramProvider;

public class DiagramCopier implements IRefinementParticipant {

	ResourceSet resourceSet =  new ResourceSetImpl();
	
	@Override
	public void process(IInternalElement targetRoot,
			IInternalElement sourceRoot, IProgressMonitor monitor)
			throws RodinDBException {

		URI fileURI = URI.createPlatformResourceURI(sourceRoot.getResource().getFullPath().toString(), true);
		Resource resource = resourceSet.getResource(fileURI, false); //n.b. do not load until notifications disabled
		if (resource == null){
			resource = resourceSet.createResource(fileURI);
		}
		if (!resource.isLoaded()){
			resource.eSetDeliver(false);	// turn off notifications to Transactional Change Recorder while loading
			try {
				resource.load(Collections.emptyMap());
			} catch (IOException e) {
				e.printStackTrace();
			}
			resource.eSetDeliver(true);
		}
		if (resource.isLoaded() && resource.getContents().isEmpty() == false && resource.getContents().get(0) instanceof Machine) {
			Machine machine = (Machine) resource.getContents().get(0);

			for (AbstractExtension ext : machine.getExtensions()){
				// find diagram provider
				Map<String, IDiagramProvider> registry = DiagramsNavigatorExtensionPlugin.getDefault().getDiagramProviderRegistry();
				String type = ext.eClass().getName();
				IDiagramProvider provider = registry.get(type);
				// get diagram filename
				String filename = provider ==null? null : provider.getDiagramFileName(ext);

				if (filename!= null){
					// we now have the filename of the diagram 
					// NOTE: it is RELATIVE to the containing project
					// Also note that this code is not specific to statemachines in any way,
					// it should work for any diagrams that use our extensions framework and 
					// contribute a Diagram provider (like the statemachines plugin does).
					System.out.println("diagram file = "+ filename);
					
					//TODO: WANG TO TAKE IT FROM HERE!!!
					
				}
			}	
		}		
	}
}
