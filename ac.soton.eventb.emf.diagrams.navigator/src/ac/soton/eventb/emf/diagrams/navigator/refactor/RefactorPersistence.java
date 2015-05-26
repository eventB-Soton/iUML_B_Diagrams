package ac.soton.eventb.emf.diagrams.navigator.refactor;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EPackage.Registry;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;


public class RefactorPersistence {

	private final static String[] changesFolder = {"iumlb","changes"};
	
	public final static String proxyMapNsURI = "http://ac.soton.iumlb.refactoring.proxyMap";
	private EPackage proxyMapPackage;
	private EClass proxyMapClass;
	private EReference proxyMapEntriesContainment;
	private EClass proxyMapEntryClass;
	private EReference proxyMapEntryProxyReference;
	private EAttribute proxyMapEntryURIAttribute;
	
	
	public static RefactorPersistence INSTANCE = new RefactorPersistence();
	
	private RefactorPersistence() {
		getProxyMapPackage();
		//INSTANCE = this;
	}
	
	private void getProxyMapPackage(){
		Registry registry = EPackage.Registry.INSTANCE;		
		if (!registry.containsKey(proxyMapNsURI)){
			//set up dynamic EMF EClass for storing the proxyMap
			EcoreFactory ef =  EcoreFactory.eINSTANCE;

			proxyMapEntryProxyReference = ef.createEReference();
			proxyMapEntryProxyReference.setName("proxyMapEntryProxyReference");
			proxyMapEntryProxyReference.setEType(EcorePackage.Literals.EOBJECT);
			proxyMapEntryProxyReference.setContainment(false);
			proxyMapEntryProxyReference.setUpperBound(1);
			
			proxyMapEntryURIAttribute = ef.createEAttribute();
			proxyMapEntryURIAttribute.setName("proxyMapEntryURIAttribute");
			proxyMapEntryURIAttribute.setEType(EcorePackage.Literals.ESTRING);
			proxyMapEntryURIAttribute.setUpperBound(1);
			
			proxyMapEntryClass = ef.createEClass();
			proxyMapEntryClass.setName("proxyMapEntryClass");
			proxyMapEntryClass.getEStructuralFeatures().add(proxyMapEntryProxyReference);
			proxyMapEntryClass.getEStructuralFeatures().add(proxyMapEntryURIAttribute);
			
			proxyMapEntriesContainment = ef.createEReference();
			proxyMapEntriesContainment.setName("proxyMapEntriesContainment");
			proxyMapEntriesContainment.setEType(proxyMapEntryClass);
			proxyMapEntriesContainment.setContainment(true);
			proxyMapEntriesContainment.setUpperBound(-1);
			
			proxyMapClass = ef.createEClass();
			proxyMapClass.setName("proxyMapClass");
			proxyMapClass.getEStructuralFeatures().add(proxyMapEntriesContainment);
			
			proxyMapPackage = ef.createEPackage();
			proxyMapPackage.setName("proxyMapPackage");
			proxyMapPackage.setNsPrefix("proxyMapPackage");
			proxyMapPackage.setNsURI(proxyMapNsURI);
			proxyMapPackage.getEClassifiers().add(proxyMapClass);
			proxyMapPackage.getEClassifiers().add(proxyMapEntryClass);
		
			registry.put(proxyMapNsURI, proxyMapPackage);
		}
		proxyMapPackage = registry.getEPackage(proxyMapNsURI);
		proxyMapClass = (EClass) proxyMapPackage.getEClassifier("proxyMapClass");
		proxyMapEntriesContainment = (EReference) proxyMapClass.getEStructuralFeature("proxyMapEntriesContainment");
		proxyMapEntryClass =  (EClass) proxyMapPackage.getEClassifier("proxyMapEntryClass");
		proxyMapEntryProxyReference = (EReference) proxyMapEntryClass.getEStructuralFeature("proxyMapEntryProxyReference");
		proxyMapEntryURIAttribute = (EAttribute) proxyMapEntryClass.getEStructuralFeature("proxyMapEntryURIAttribute");
	}

	public EObject convert(Map<EObject, URI> proxyMap) {
		EFactory proxyMapFactory = proxyMapPackage.getEFactoryInstance();
		EObject proxyMapObject = proxyMapFactory.create(proxyMapClass);
		@SuppressWarnings("unchecked")
		EList<EObject>  entries = (EList<EObject>) proxyMapObject.eGet(proxyMapEntriesContainment);
		for (Entry<EObject, URI> entry : proxyMap.entrySet()){
			EObject proxyMapEntryObject = proxyMapFactory.create(proxyMapEntryClass);
			proxyMapEntryObject.eSet(proxyMapEntryProxyReference, entry.getKey());
			proxyMapEntryObject.eSet(proxyMapEntryURIAttribute, entry.getValue().toString() );			
			entries.add(proxyMapEntryObject);
		}
		return proxyMapObject;
	}
	
	public Map<EObject, URI> convert(EObject proxyMapObject) {
		Map<EObject, URI> map = new HashMap<EObject, URI>();
		if (proxyMapObject.eClass() == proxyMapClass){
			@SuppressWarnings("unchecked")
			EList<EObject> entries = (EList<EObject>) proxyMapObject.eGet(proxyMapEntriesContainment);
			for (EObject entry : entries){
				map.put((EObject)entry.eGet(proxyMapEntryProxyReference), URI.createURI((String)entry.eGet(proxyMapEntryURIAttribute)));
			}
		}
		return map;
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
		String filename = componentName+"."+oldFileExtension;
		if (timestamp != null && timestamp.length()>0) filename = filename+"."+timestamp;
		uri = uri.appendSegment(filename);
		uri = uri.appendFileExtension(extension);
		return uri;
	}
	
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

	public Map<EObject, URI> getProxyMap(Resource res) {
		Resource proxyMapResource;
		if ("proxyMap".equals(res.getURI().fileExtension())){
			proxyMapResource = res;
		}else{
			proxyMapResource = getProxyMapResource(res);
		}
		if (proxyMapResource.getContents().size()>0){
			return convert(proxyMapResource.getContents().get(0));
		}else{
			return new HashMap<EObject,URI>();
		}
	}
		
	public Map<EObject, URI> getProxyMap(ResourceSet rs, URI componentUri) {
		Resource proxyMapResource = getProxyMapResource(rs, componentUri);
		if (proxyMapResource.getContents().size()>0){
			return convert(proxyMapResource.getContents().get(0));
		}else{
			return new HashMap<EObject,URI>();
		}
	}
	
	/**
	 * this gets a changes resource corresponding to the given Resource and if necessary creates it in the file system
	 * It also loads it.
	 * 
	 */
	public Resource getChangesResource(Resource res){
		return getChangesResource(res.getResourceSet(),res.getURI());
	}
	

	public Resource getChangesResource(ResourceSet rs, URI comp_uri){
		URI	uri = getRelatedURI(comp_uri, changesFolder, null, "changes");
		Resource chRes = rs.getResource(uri, false); //n.b. do not load until notifications disabled
		if (chRes == null) {
			chRes = rs.createResource(uri);
		}


			boolean deliver = chRes.eDeliver();
			chRes.eSetDeliver(false);
			
			if (chRes.isLoaded()){
				chRes.unload();
			}
			
			Path path = new Path(chRes.getURI().toPlatformString(true));
			boolean exists = ResourcesPlugin.getWorkspace().getRoot().exists(path);
			if (exists){
				try {
					chRes.load(Collections.EMPTY_MAP);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				chRes.getContents().clear(); //this makes the new Resource appear loaded
			}
			chRes.eSetDeliver(deliver);

		chRes.setTrackingModification(false);
		return chRes;
	}

	public Resource getProxyMapResource(Resource res){
		return getProxyMapResource(res.getResourceSet(),res.getURI());
	}
	
	public Resource getProxyMapResource(ResourceSet rs, URI comp_uri){
		URI uri = getRelatedURI(comp_uri, changesFolder, null, "proxymap");
		Resource proxyMapResource = rs.getResource(uri, false); //n.b. do not load until notifications disabled
		if (proxyMapResource == null) {
			proxyMapResource = rs.createResource(uri);
		}

		boolean deliver = proxyMapResource.eDeliver();
		proxyMapResource.eSetDeliver(false);
		
		if (proxyMapResource.isLoaded()){
			proxyMapResource.unload();
		}
		
		Path path = new Path(proxyMapResource.getURI().toPlatformString(true));
		boolean exists = ResourcesPlugin.getWorkspace().getRoot().exists(path);
		if (exists){
			try {
				proxyMapResource.load(Collections.EMPTY_MAP);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else{
			proxyMapResource.getContents().clear(); //this makes the new Resource appear loaded
		}
		proxyMapResource.eSetDeliver(deliver);
		proxyMapResource.setTrackingModification(false);
		return proxyMapResource;
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

	
	
}
