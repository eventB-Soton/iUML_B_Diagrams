package ac.soton.eventb.emf.diagrams.navigator.refactor.persistence;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.persistence.factory.RodinResource;


public class RefactorPersistence {

	private final static String[] changesFolder = {"iumlb","changes"};
	
	public static RefactorPersistence INSTANCE = new RefactorPersistence();
	
	private RefactorPersistence() {
	}
	
	public Map<EObject, EObject> getEquivalenceMap(Resource res) {
		IProject project = WorkspaceSynchronizer.getFile(res).getProject();
		URI equivMapUri = RefactorPersistence.INSTANCE.getRelatedURI(res, changesFolder, null, "equivmap");
		MapFile equivalenceMapRes = new MapFile(); 
		equivalenceMapRes.load(project, equivMapUri, null);
		Map<EObject, EObject> equivalenceMap = equivalenceMapRes.getMap(res.getResourceSet());  //RefactorPersistence.INSTANCE.getEquivalenceMap(res);
		return equivalenceMap;
	}
	
	public void saveEquivalenceMap(Resource res, Map<EObject,EObject> map) {
		IProject project = WorkspaceSynchronizer.getFile(res).getProject();
		URI equivMapUri = RefactorPersistence.INSTANCE.getRelatedURI(res, changesFolder, null, "equivmap");
		MapFile equivalenceMapRes = new MapFile(); 
		equivalenceMapRes.setMap(map);
		equivalenceMapRes.save(project, equivMapUri, null);
	}
	
	public void deleteEquivalenceMap(Resource res) {
		IProject project = WorkspaceSynchronizer.getFile(res).getProject();
		URI equivMapUri = RefactorPersistence.INSTANCE.getRelatedURI(res, changesFolder, null, "equivmap");
		MapFile equivalenceMapRes = new MapFile(); 
		equivalenceMapRes.delete(project, equivMapUri, null);
	}


	public EventBNamedCommentedComponentElement getPreComponent(Resource res) throws IOException {
		Resource preRes = getPreStateResource(res);
		if (preRes.getContents().size()>0 && preRes.getContents().get(0) instanceof EventBNamedCommentedComponentElement){
			return (EventBNamedCommentedComponentElement) preRes.getContents().get(0);
		}else{
			return null;
		}
	}

	/**
	 * this gets a changes resource corresponding to the given Resource and if necessary creates it in the file system
	 * It also loads it.
	 * @throws IOException 
	 * 
	 */
	public Resource getChangesResource(Resource res) throws IOException{
		return getChangesResource(res.getResourceSet(),res.getURI());
	}

	public Resource getChangesResource(ResourceSet rs, URI comp_uri) throws IOException{
		return getResourceInChangesFolder(rs, comp_uri, "changes");
	}
	
	public Resource getPreStateResource(Resource res) throws IOException {
		return getPreStateResource(res.getResourceSet(),res.getURI());
	}
	
	public Resource getPreStateResource(ResourceSet rs, URI comp_uri) throws IOException{
		return getResourceInChangesFolder(rs, comp_uri, "xmb");
	}


	/**
	 * gets the resource using the uri comp_uri but with the alternative extension and
	 * in the changes folder of the same project as the uri
	 *  
	 * @param rs
	 * @param comp_uri
	 * @return
	 * @throws IOException 
	 */
	private Resource getResourceInChangesFolder(ResourceSet rs, URI comp_uri, String extension) throws IOException {
		URI uri = getRelatedURI(comp_uri, changesFolder, null, extension);
		return getResource(rs, uri);
	}

	/**
	 * @param rs
	 * @param uri
	 * @return
	 * @throws IOException
	 */
	private Resource getResource(ResourceSet rs, URI uri) throws IOException {
		Resource resource = rs.getResource(uri, false); //n.b. do not load until notifications disabled
		if (resource == null) {
			resource = rs.createResource(uri);
		}

		boolean deliver = resource.eDeliver();
		resource.eSetDeliver(false);
		
		if (resource.isLoaded()){
			resource.unload();
		}
		
		Path path = new Path(resource.getURI().toPlatformString(true));
		boolean exists = ResourcesPlugin.getWorkspace().getRoot().exists(path);
		if (exists){
			resource.load(Collections.EMPTY_MAP);
		}else{
			resource.getContents().clear(); //this makes the new Resource appear loaded
		}
		resource.eSetDeliver(deliver);
		resource.setTrackingModification(false);
		return resource;
	}
	
	/**
	 * This checks whether a changes file exists for an arbitrary component
	 * and has some contents (other than an empty change description)
	 * 
	 */
	public boolean hasChanges(URI componentUri, ResourceSet rs){
		URI	uri = getRelatedURI(componentUri, changesFolder, null, "changes");
		Path path = new Path(uri.toPlatformString(true));
		if (!ResourcesPlugin.getWorkspace().getRoot().exists(path)){
			return false;
		}else{
			try {
				Resource chRes = getResource(rs, uri);
				if (chRes==null) return false;
				EList<EObject> c = chRes.getContents();
				if (c==null || c.isEmpty()) return false;
				for (EObject eo : c){
					if (eo instanceof ChangeDescription){
						ChangeDescription cd = (ChangeDescription)eo;
						if (	cd.getObjectChanges()!=null && !cd.getObjectChanges().isEmpty() ||
								cd.getObjectsToAttach()!=null && !cd.getObjectsToAttach().isEmpty() ||
								cd.getObjectsToDetach()!=null && !cd.getObjectsToDetach().isEmpty() ||
								cd.getResourceChanges()!=null && !cd.getResourceChanges().isEmpty()
								){
							return true;
						}
					}
				}
				return false;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
		}
	}
	
	
	/**
	 * constructs a uri based on the given resources uri
	 * 
	 * @param originalResource
	 * @param subFolder or null
	 * @param timestamp or null
	 * @param extension
	 * @return
	 */
	public URI getRelatedURI(Resource originalResource, String[] subFolders, String timestamp, String extension){
		URI uri = originalResource.getURI();
		return getRelatedURI(uri, subFolders, timestamp, extension);
	}
		
	public URI getRelatedURI(URI uri, String[] subFolders, String timestamp, String extension){	
		String oldFileExtension = uri.fileExtension();
		uri = uri.trimFileExtension();
		String componentName = uri.segments()[uri.segmentCount()-1];
		uri = uri.trimSegments(1);
		for (String subFolder : subFolders){
			if (subFolder != null && subFolder.length()>0) uri = uri.appendSegment(subFolder);			
		}
		String filename = componentName+"_"+oldFileExtension;
		if (timestamp != null && timestamp.length()>0) filename = filename+"."+timestamp;
		uri = uri.appendSegment(filename);
		uri = uri.appendFileExtension(extension);
		return uri;
	}

	/**
	 * checks whether the state of the component before changes has been saved already and if not saves a copy of the component
	 * as well as the copier map to persist the mapping of future changed elements to the saved current elements
	 * 
	 * @param component
	 * @throws IOException
	 */
	public void checkPreState(EventBNamedCommentedComponentElement component) throws IOException {
		Resource res = component.eResource();
		RodinResource preComponentRes = (RodinResource) RefactorPersistence.INSTANCE.getPreStateResource(res);
		preComponentRes.eSetDeliver(false);		
		EventBNamedCommentedComponentElement preComponent = RefactorPersistence.INSTANCE.getPreComponent(res);
		Map<EObject, EObject> equivalenceMap = getEquivalenceMap(res);  //RefactorPersistence.INSTANCE.getEquivalenceMap(res);
		
		if (preComponent==null || equivalenceMap==null){
			Copier copier = new Copier();
			preComponent = (EventBNamedCommentedComponentElement) copier.copy(component);
			preComponentRes.getContents().add(0, preComponent);
			copier.copyReferences();
			preComponentRes.save(Collections.EMPTY_MAP);
			saveEquivalenceMap(res, copier);		
		}
		preComponentRes.unload();
	}

}
