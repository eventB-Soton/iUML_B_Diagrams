package ac.soton.eventb.emf.diagrams.navigator.refactor.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;

/**
 * This class provides a convenient and efficient way to persist a map of EObjects as a text file
 * It is based on TextFile which maintains a persistable text field
 * 
 * @author cfs
 *
 */
		
public class MapFile extends TextFile {

	private static final String KEY = "KEY: ";
	private static final String VAL = "VAL: ";
	
	public MapFile() {
		super();
	}
	
	/**
	 * stores the given map of EObjects into the text field as URIs ready to be saved
	 * THIS DOES NOT SAVE TO FILE
	 * @param map
	 */
	public void setMap(Map<EObject,EObject> map){
		for (Entry<EObject, EObject> entry : map.entrySet()){
			addLine(KEY+EcoreUtil.getURI(entry.getKey()).toString());
			addLine(VAL+EcoreUtil.getURI(entry.getValue()).toString());
		}
	}
	
	/**
	 * gets a map of EObjects from the URIs listed in the text field 
	 * (the text field should have been populated with a list of eProxy URIs beforehand)
	 * The EObjects will be resolved against the given resource set if possible
	 * THIS DOES NOT LOAD THE FILE
	 * 
	 * @param resourceSet
	 * @return
	 */
	public Map<EObject,EObject> getMap(ResourceSet resourceSet){
		Map<EObject, EObject> map = new HashMap<EObject,EObject>();
		String[] lines = getLines();
		EObject key = null;
		EObject val = null;
		for (int i=0; i<lines.length; i++) {
			if (lines[i].startsWith(KEY)){
				key = makeProxy(lines[i].substring(KEY.length()));
				if (key!=null) key = EcoreUtil.resolve(key, resourceSet);
			}else if (lines[i].startsWith(VAL) && key!=null){
				val = makeProxy(lines[i].substring(VAL.length()));
				if (val!=null)  val = EcoreUtil.resolve(val, resourceSet);
				if (val!=null){
					map.put(key, val);
				}
				key=null;
				val=null;
			}
		}
		return map;
	}

	private EObject makeProxy(String string) {
		EObject proxy = null;
		URI uri = URI.createURI(string);
		if (uri==null || uri.fragment() ==null)
			return null;
		String[] frags = uri.fragment().split("::");
		if (frags.length<3) 
			return null;
		EPackage ePackage = EPackage.Registry.INSTANCE.getEPackage(frags[0]);
		if (ePackage != null){
			EClassifier eClass = ePackage.getEClassifier(frags[1]);
			if (eClass instanceof EClass ){
				proxy = ePackage.getEFactoryInstance().create((EClass) eClass);
				((InternalEObject)proxy).eSetProxyURI(uri);
			}
		}
		return proxy;
	}

	
}