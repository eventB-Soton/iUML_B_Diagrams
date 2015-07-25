package ac.soton.eventb.emf.diagrams.navigator.refactor.persistence;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.persistence.factory.RodinResource;


public class RefactorPersistence {

	private final static String[] changesFolder = {"iumlb","changes"};
	
//	public final static String proxyMapNsURI = "http://ac.soton.iumlb.refactoring.proxyMap";
//	private EPackage proxyMapPackage;
//	private EClass proxyMapClass;
//	private EReference proxyMapEntriesContainment;
//	private EClass proxyMapEntryClass;
//	private EReference proxyMapEntryProxyReference;
//	private EAttribute proxyMapEntryURIAttribute;
//	
//	public final static String equivalenceMapNsURI = "http://ac.soton.iumlb.refactoring.equivMap";
//	public static EPackage equivMapPackage;
//	public static EClass equivMapClass;
//	public static EReference equivMapContainment;
//	public static EClass equivMapEntryClass;
//	public static EReference equivMapEntryKeyReference;
//	public static EReference equivMapEntryValueReference;

	
	public static RefactorPersistence INSTANCE = new RefactorPersistence();
	
	private RefactorPersistence() {
//		getProxyMapPackage();
//		getEquivalenceMapPackage();
//		//INSTANCE = this;
	}
	
//	private void getProxyMapPackage(){
//		Registry registry = EPackage.Registry.INSTANCE;		
//		if (!registry.containsKey(proxyMapNsURI)){
//			//set up dynamic EMF EClass for storing the proxyMap
//			EcoreFactory ef =  EcoreFactory.eINSTANCE;
//
//			proxyMapEntryProxyReference = ef.createEReference();
//			proxyMapEntryProxyReference.setName("proxyMapEntryProxyReference");
//			proxyMapEntryProxyReference.setEType(EcorePackage.Literals.EOBJECT);
//			proxyMapEntryProxyReference.setContainment(false);
//			proxyMapEntryProxyReference.setUpperBound(1);
//			
//			proxyMapEntryURIAttribute = ef.createEAttribute();
//			proxyMapEntryURIAttribute.setName("proxyMapEntryURIAttribute");
//			proxyMapEntryURIAttribute.setEType(EcorePackage.Literals.ESTRING);
//			proxyMapEntryURIAttribute.setUpperBound(1);
//			
//			proxyMapEntryClass = ef.createEClass();
//			proxyMapEntryClass.setName("proxyMapEntryClass");
//			proxyMapEntryClass.getEStructuralFeatures().add(proxyMapEntryProxyReference);
//			proxyMapEntryClass.getEStructuralFeatures().add(proxyMapEntryURIAttribute);
//			
//			proxyMapEntriesContainment = ef.createEReference();
//			proxyMapEntriesContainment.setName("proxyMapEntriesContainment");
//			proxyMapEntriesContainment.setEType(proxyMapEntryClass);
//			proxyMapEntriesContainment.setContainment(true);
//			proxyMapEntriesContainment.setUpperBound(-1);
//			
//			proxyMapClass = ef.createEClass();
//			proxyMapClass.setName("proxyMapClass");
//			proxyMapClass.getEStructuralFeatures().add(proxyMapEntriesContainment);
//			
//			proxyMapPackage = ef.createEPackage();
//			proxyMapPackage.setName("proxyMapPackage");
//			proxyMapPackage.setNsPrefix("proxyMapPackage");
//			proxyMapPackage.setNsURI(proxyMapNsURI);
//			proxyMapPackage.getEClassifiers().add(proxyMapClass);
//			proxyMapPackage.getEClassifiers().add(proxyMapEntryClass);
//		
//			registry.put(proxyMapNsURI, proxyMapPackage);
//		}
//		proxyMapPackage = registry.getEPackage(proxyMapNsURI);
//		proxyMapClass = (EClass) proxyMapPackage.getEClassifier("proxyMapClass");
//		proxyMapEntriesContainment = (EReference) proxyMapClass.getEStructuralFeature("proxyMapEntriesContainment");
//		proxyMapEntryClass =  (EClass) proxyMapPackage.getEClassifier("proxyMapEntryClass");
//		proxyMapEntryProxyReference = (EReference) proxyMapEntryClass.getEStructuralFeature("proxyMapEntryProxyReference");
//		proxyMapEntryURIAttribute = (EAttribute) proxyMapEntryClass.getEStructuralFeature("proxyMapEntryURIAttribute");
//	}
//
//
//	public EObject convertPMToEMF(Map<EObject, URI> proxyMap) {
//		EFactory proxyMapFactory = proxyMapPackage.getEFactoryInstance();
//		EObject proxyMapObject = proxyMapFactory.create(proxyMapClass);
//		@SuppressWarnings("unchecked")
//		EList<EObject>  entries = (EList<EObject>) proxyMapObject.eGet(proxyMapEntriesContainment);
//		for (Entry<EObject, URI> entry : proxyMap.entrySet()){
//			EObject proxyMapEntryObject = proxyMapFactory.create(proxyMapEntryClass);
//			proxyMapEntryObject.eSet(proxyMapEntryProxyReference, entry.getKey());
//			proxyMapEntryObject.eSet(proxyMapEntryURIAttribute, entry.getValue().toString() );			
//			entries.add(proxyMapEntryObject);
//		}
//		return proxyMapObject;
//	}
//	
//	public Map<EObject, URI> convertPMFromEMF(EObject proxyMapObject) {
//		Map<EObject, URI> map = new HashMap<EObject, URI>();
//		if (proxyMapObject.eClass() == proxyMapClass){
//			@SuppressWarnings("unchecked")
//			EList<EObject> entries = (EList<EObject>) proxyMapObject.eGet(proxyMapEntriesContainment);
//			for (EObject entry : entries){
//				map.put((EObject)entry.eGet(proxyMapEntryProxyReference), URI.createURI((String)entry.eGet(proxyMapEntryURIAttribute)));
//			}
//		}
//		return map;
//	}
//	/**
//	 * inserts the given changes and proxyMap into the given changes and proxyMap resources
//	 * 
//	 * 
//	 */
//	public void putChangesInResources(Resource chRes, ChangeDescription changes, Resource proxyMapResource, Map<EObject,URI> proxyMap) {
//		
//		if (changes!=null && changes.eContents().size()>0){
//			
//			boolean deliver = chRes.eDeliver();
//			boolean deliver2 = changes.eDeliver();
//			chRes.eSetDeliver(false);
//			changes.eSetDeliver(false);
//			EList<Adapter> adpt = changes.eAdapters();
//			changes.eAdapters().clear();
//			chRes.eSetDeliver(false);
//			chRes.getContents().clear();
//			chRes.getContents().add(0, changes);
//			chRes.eSetDeliver(deliver);
//			changes.eAdapters().addAll(adpt);
//			changes.eSetDeliver(deliver2);
//			
//			if (proxyMap!=null){
//				deliver = proxyMapResource.eDeliver();
//				proxyMapResource.eSetDeliver(false);
//				proxyMapResource.getContents().clear();
//				EObject m = convert(proxyMap);
//				proxyMapResource.getContents().add(m);
//				proxyMapResource.eSetDeliver(deliver);			
//			}
//			
//		}
//	}
	
//	/**
//	 * saves (persists)  the given changes and proxyMap in the changes and proxyMap resources
//	 * 
//	 * 
//	 */
//	public void saveChanges(Resource chRes, ChangeDescription changes, Resource proxyMapResource, Map<EObject,URI> proxyMap) {
//		if (changes!=null && changes.eContents().size()>0){
//			try {
//				boolean deliver = chRes.eDeliver();
//				boolean deliver2 = changes.eDeliver();
//				chRes.eSetDeliver(false);
//				changes.eSetDeliver(false);
//				EList<Adapter> adpt = changes.eAdapters();
//				changes.eAdapters().clear();
//				chRes.eSetDeliver(false);
//				chRes.getContents().clear();
//				chRes.getContents().add(0, changes);
//				chRes.save(Collections.EMPTY_MAP);
//				chRes.eSetDeliver(deliver);
//				changes.eAdapters().addAll(adpt);
//				changes.eSetDeliver(deliver2);
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			if (proxyMap!=null){
//				try {
//					boolean deliver = proxyMapResource.eDeliver();
//					proxyMapResource.eSetDeliver(false);
//					proxyMapResource.getContents().clear();
//					EObject m = convert(proxyMap);
//					proxyMapResource.getContents().add(m);
//					proxyMapResource.save(Collections.EMPTY_MAP);
//					proxyMapResource.eSetDeliver(deliver);			
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//		}
//
//	}
//
//	public Map<EObject, URI> getProxyMap(Resource res) throws IOException {
//		Resource proxyMapResource;
//		if ("proxyMap".equals(res.getURI().fileExtension())){
//			proxyMapResource = res;
//		}else{
//			proxyMapResource = getProxyMapResource(res);
//		}
//		if (proxyMapResource.getContents().size()>0){
//			return convertPMFromEMF(proxyMapResource.getContents().get(0));
//		}else{
//			return new HashMap<EObject,URI>();
//		}
//	}
//		
//	public Map<EObject, URI> getProxyMap(ResourceSet rs, URI componentUri) throws IOException {
//		Resource proxyMapResource = getProxyMapResource(rs, componentUri);
//		if (proxyMapResource.getContents().size()>0){
//			return convertPMFromEMF(proxyMapResource.getContents().get(0));
//		}else{
//			return new HashMap<EObject,URI>();
//		}
//	}
//
//	
//	/**
//	 * This dynamically constructs an ecore package for storing the map from objects in the changed model
//	 * to equivalent objects in a copy of the model before change
//	 */
//	private void getEquivalenceMapPackage(){
//		Registry registry = EPackage.Registry.INSTANCE;		
//		if (!registry.containsKey(equivalenceMapNsURI)){
//			//set up dynamic EMF EClass for storing the equivalenceMap
//			EcoreFactory ef =  EcoreFactory.eINSTANCE;
//
//			equivMapEntryKeyReference = ef.createEReference();
//			equivMapEntryKeyReference.setName("equivMapEntryKeyReference");
//			equivMapEntryKeyReference.setEType(EcorePackage.Literals.EOBJECT);
//			equivMapEntryKeyReference.setContainment(false);
//			equivMapEntryKeyReference.setUpperBound(1);
//			
//			equivMapEntryValueReference = ef.createEReference();
//			equivMapEntryValueReference.setName("equivMapEntryValueReference");
//			equivMapEntryValueReference.setEType(EcorePackage.Literals.EOBJECT);
//			equivMapEntryValueReference.setContainment(false);
//			equivMapEntryValueReference.setUpperBound(1);
//			
//			equivMapEntryClass = ef.createEClass();
//			equivMapEntryClass.setName("equivMapEntryClass");
//			equivMapEntryClass.getEStructuralFeatures().add(equivMapEntryKeyReference);
//			equivMapEntryClass.getEStructuralFeatures().add(equivMapEntryValueReference);
//			
//			equivMapContainment = ef.createEReference();
//			equivMapContainment.setName("equivMapContainment");
//			equivMapContainment.setEType(equivMapEntryClass);
//			equivMapContainment.setContainment(true);
//			equivMapContainment.setUpperBound(-1);
//			
//			equivMapClass = ef.createEClass();
//			equivMapClass.setName("equivMapClass");
//			equivMapClass.getEStructuralFeatures().add(equivMapContainment);
//			
//			equivMapPackage = ef.createEPackage();
//			equivMapPackage.setName("equivMapPackage");
//			equivMapPackage.setNsPrefix("equivMapPackage");
//			equivMapPackage.setNsURI(equivalenceMapNsURI);
//			equivMapPackage.getEClassifiers().add(equivMapClass);
//			equivMapPackage.getEClassifiers().add(equivMapEntryClass);
//		
//			registry.put(equivalenceMapNsURI, equivMapPackage);
//		}
//		equivMapPackage = registry.getEPackage(equivalenceMapNsURI);
//		equivMapClass = (EClass) equivMapPackage.getEClassifier("equivMapClass");
//		equivMapContainment = (EReference) equivMapClass.getEStructuralFeature("equivMapContainment");
//		equivMapEntryClass =  (EClass) equivMapPackage.getEClassifier("equivMapEntryClass");
//		equivMapEntryKeyReference = (EReference) equivMapEntryClass.getEStructuralFeature("equivMapEntryKeyReference");
//		equivMapEntryValueReference = (EReference) equivMapEntryClass.getEStructuralFeature("equivMapEntryValueReference");
//	}
//	
//	
//	public EObject convertEQMToEMF(Map<EObject, EObject> equivMap) {
//		EFactory equivMapFactory = equivMapPackage.getEFactoryInstance();
//		EObject equivMapObject = equivMapFactory.create(equivMapClass);
//		@SuppressWarnings("unchecked")
//		EList<EObject>  entries = (EList<EObject>) equivMapObject.eGet(equivMapContainment);
//		for (Entry<EObject, EObject> entry : equivMap.entrySet()){
//			EObject equivMapEntryObject = equivMapFactory.create(equivMapEntryClass);
//			equivMapEntryObject.eSet(equivMapEntryKeyReference, entry.getKey());
//			equivMapEntryObject.eSet(equivMapEntryValueReference, entry.getValue());			
//			entries.add(equivMapEntryObject);
//		}		
//		return equivMapObject;
//	}
//
//	public Map<EObject, EObject> convertEMFToEQM(EObject equivMapObject) {
//		Map<EObject, EObject> map = new HashMap<EObject, EObject>();
//		if (equivMapObject.eClass() == equivMapClass){
//			@SuppressWarnings("unchecked")
//			EList<EObject> entries = (EList<EObject>) equivMapObject.eGet(equivMapContainment);
//			for (EObject entry : entries){
//				map.put((EObject)entry.eGet(equivMapEntryKeyReference), (EObject)entry.eGet(equivMapEntryValueReference));
//			}
//		}
//		return map;
//	}
	
	public Map<EObject, EObject> getEquivalenceMap(Resource res) {
		IProject project = WorkspaceSynchronizer.getFile(res).getProject();
		URI equivMapUri = RefactorPersistence.INSTANCE.getRelatedURI(res, changesFolder, null, "equivmap");
		MapFile equivalenceMapRes = new MapFile(); 
		equivalenceMapRes.load(project, equivMapUri, null);
		Map<EObject, EObject> equivalenceMap = equivalenceMapRes.getMap(res.getResourceSet());  //RefactorPersistence.INSTANCE.getEquivalenceMap(res);
		return equivalenceMap;
		
//		Resource equivMapResource;
//		if ("equivMap".equals(res.getURI().fileExtension())){
//			equivMapResource = res;
//		}else{
//			equivMapResource = getEquivMapResource(res);
//		}
//		if (equivMapResource.getContents().size()>0){
//			return convertEMFToEQM(equivMapResource.getContents().get(0));
//		}else{
//			return new HashMap<EObject,EObject>();
//		}
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

	
//	public Map<EObject, EObject> getEquivalenceMap(ResourceSet rs, URI componentUri) throws IOException {
//		Resource equivMapResource = getEquivMapResource(rs, componentUri);
//		if (equivMapResource.getContents().size()>0){
//			return  convertEMFToEQM(equivMapResource.getContents().get(0));
//		}else{
//			return new HashMap<EObject,EObject>();
//		}
//	}
	
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
//		URI uri = comp_uri.trimFileExtension().appendFileExtension("bum.xmb");
//		return getResource(rs, uri);
		return getResourceInChangesFolder(rs, comp_uri, "xmb");
	}

//	public Resource getProxyMapResource(Resource res) throws IOException{
//		return getProxyMapResource(res.getResourceSet(),res.getURI());
//	}
//	
//	public Resource getProxyMapResource(ResourceSet rs, URI comp_uri) throws IOException{
//		return getResourceInChangesFolder(rs, comp_uri, "proxymap");
//	}
//	
//	public Resource getEquivMapResource(Resource res) throws IOException{
//		return getEquivMapResource(res.getResourceSet(),res.getURI());
//	}
//	
//	public Resource getEquivMapResource(Resource res) throws IOException{
//		return getEquivMapResource(res.getResourceSet(),res.getURI());
//	}
//	
//	public Resource getEquivMapResource(ResourceSet rs, URI comp_uri) throws IOException{
//		return getResourceInChangesFolder(rs, comp_uri, "equivmap");
//	}

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
	 * 
	 */
		
	public boolean hasChangesResource(URI componentUri){
		URI	uri = getRelatedURI(componentUri, changesFolder, null, "changes");
//		String oldFileExtension = component instanceof Machine? "bum" : "buc"; //uri.fileExtension();
//		uri = uri.trimFileExtension();
//		String componentName =  component.getName(); //uri.segments()[uri.segmentCount()-1];
//		uri = uri.trimSegments(1);
//		uri = uri.appendSegment("changes");
//		uri = uri.appendSegment(componentName+"."+oldFileExtension);
//		uri = uri.appendFileExtension("changes");
		Path path = new Path(uri.toPlatformString(true));
		//boolean exists = ResourcesPlugin.getWorkspace().getRoot().exists(path);
		return ResourcesPlugin.getWorkspace().getRoot().exists(path);
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
		//ResourceSet rs = res.getResourceSet();
		
		RodinResource preComponentRes = (RodinResource) RefactorPersistence.INSTANCE.getPreStateResource(res);
		preComponentRes.eSetDeliver(false);		
		EventBNamedCommentedComponentElement preComponent = RefactorPersistence.INSTANCE.getPreComponent(res);
		
//		Resource equivalenceMapRes = RefactorPersistence.INSTANCE.getEquivMapResource(res);
//		equivalenceMapRes.eSetDeliver(false);
//		Map<EObject, EObject> equivalenceMap RefactorPersistence.INSTANCE.getEquivalenceMap(res);
		
//		IProject project = WorkspaceSynchronizer.getFile(res).getProject();
//		URI equivMapUri = RefactorPersistence.INSTANCE.getRelatedURI(res, changesFolder, null, "equivmap");
//
//		MapFile equivalenceMapRes = new MapFile(); 
//		equivalenceMapRes.load(project, equivMapUri, null);
		Map<EObject, EObject> equivalenceMap = getEquivalenceMap(res);  //RefactorPersistence.INSTANCE.getEquivalenceMap(res);
		
		if (preComponent==null || equivalenceMap==null){
			//EcoreUtil.copy(component);
			Copier copier = new Copier();
			preComponent = (EventBNamedCommentedComponentElement) copier.copy(component);
//			equivalenceMapRes.getContents().clear();
			preComponentRes.getContents().add(0, preComponent);
			//preComponentRes.getContents().add(0, preComponent);
			//URIConverter uc = rs.getURIConverter();
			//preComponentRes.save(Collections.EMPTY_MAP);
			copier.copyReferences();
			preComponentRes.save(Collections.EMPTY_MAP);

			saveEquivalenceMap(res, copier);
			
//			equivalenceMapRes.getContents().clear();
//			equivalenceMapRes.getContents().add(0,RefactorPersistence.INSTANCE.convertEQMToEMF(copier));
//			equivalenceMapRes.save(Collections.EMPTY_MAP);
			
		}
		preComponentRes.unload();
//		equivalenceMapRes.unload();
	}

}
