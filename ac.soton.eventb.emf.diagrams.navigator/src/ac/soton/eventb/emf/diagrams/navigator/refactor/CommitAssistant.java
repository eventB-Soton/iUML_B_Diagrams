package ac.soton.eventb.emf.diagrams.navigator.refactor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.change.FeatureChange;
import org.eclipse.emf.ecore.change.ListChange;
import org.eclipse.emf.ecore.change.util.ChangeRecorder;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBObject;

//import ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractExtensionRefiner;
//import ac.soton.eventb.emf.core.extension.navigator.refiner.IUMLBRefiner;

public class CommitAssistant extends RefactorAssistant {

	protected Resource chRes2;
	protected Resource proxyMapResource2;
	private ChangeDescription forwardChanges=null;

	public CommitAssistant(EventBNamedCommentedComponentElement component) {
		super(component);
	}
	
	/**
	 * Reverse the Change Records to create a forward change record that can be applied to refinements
	 * The original change recording is deleted in the process so that it cannot be used again or reverted
	 * 
	 */
	private void createForwardChangeRecords() {
		CopyReverseCommand command = new CopyReverseCommand(chRes, getChangeDescription());
		ed.getCommandStack().execute(command);
		forwardChanges = command.getChanges();
		command.dispose();
	}
	
	/**
	 * Apply forward Changes
	 * 
	 */
	private final static String[] savedFolder = {"iumlb","saved"};
	private final static String[] reportsFolder = {"iumlb","reports"};
	public void applyChangesTo(EventBNamedCommentedComponentElement newComponent){
		Resource refinedRes = newComponent.eResource();
		String timeStamp = getTimeStamp();
		String report = "\nPropagation report for "+refinedRes.getURI().toPlatformString(true)+" :: "+timeStamp+"\n";
		try {
			if (newComponent.eResource().getResourceSet() != rs) return;
			if (forwardChanges==null) {
				createForwardChangeRecords();
			}
			if (forwardChanges!=null) {	
				report = report+ 
						rebaseforwardChanges(this.component, newComponent);
				report = report+
						rebaseProxyMap(this.component, newComponent);

				// saved somewhere else for debugging
				chRes2 = RefactorPersistence.INSTANCE.getChangesResource(res, savedFolder);
				proxyMapResource2 = RefactorPersistence.INSTANCE.getProxyMapResource(res, savedFolder);
				RefactorPersistence.INSTANCE.saveChanges(chRes2, forwardChanges, proxyMapResource2, proxyMap);

				ApplyReverseCommand command = new ApplyReverseCommand(chRes,forwardChanges);
				ed.getCommandStack().execute(command);
				refinedRes.save(Collections.EMPTY_MAP);
				// set things up ready for the next refinement, if any
				// this gets the re-based forward Changes (which have just been reversed again) into the change recorder
				res = refinedRes;
				component =newComponent;
				proxyMap=new HashMap<EObject, URI>();
//				if (cr==null) cr = new ChangeRecorder();	//TODO: possibly adapt resumeRecording to do all this
//				cr.setEObjectToProxyURIMap(proxyMap);
//				BeginRecordingCommand brcommand = new BeginRecordingCommand(chRes, cr, forwardChanges , component);
//				ed.getCommandStack().execute(brcommand);
//				brcommand.dispose();
//				saveChanges();			// end recorder and save changes
				forwardChanges=null; 	//reset forward changes so that they will be re-created for the next refinement
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally{
			URI commitReportURI = RefactorPersistence.INSTANCE.getRelatedURI(refinedRes, reportsFolder, timeStamp, "report");
			writeTextFile(commitReportURI, report, null);
		}
		return;
	}

	private String rebaseProxyMap(
			EventBNamedCommentedComponentElement oldBase,
			EventBNamedCommentedComponentElement newBase) {
		String report = "\nREBASE PROXY MAP:\n";
		String oldBaseName = oldBase.getName();
		String newBaseName = newBase.getName();
		Map<EObject,EObject> changeKey = new HashMap<EObject,EObject>();
		List<EObject> delKey = new ArrayList<EObject>();
		
		for (Entry<EObject, URI> entry : proxyMap.entrySet()){
			EObject key = entry.getKey();
			URI proxyURI = EcoreUtil.getURI(entry.getKey());
			report = report + "Key :"+proxyURI;
			proxyURI = rebaseURI(proxyURI, oldBaseName, newBaseName);
			if (proxyURI==null){
				report = report + " >--> UNCHANGED\n";
			}else{
				EFactory factory = key.eClass().getEPackage().getEFactoryInstance();
				EObject newKey = factory.create(key.eClass());
				((InternalEObject) newKey).eSetProxyURI(proxyURI);
				newKey = EcoreUtil.resolve(newKey, rs);
				if (!newKey.eIsProxy()){
					changeKey.put(key, newKey);
					report = report + " >--> "+proxyURI+"\n";
				}else{
					delKey.add(key);
					report = report + " >--> DELETED\n";
					break;
				}
			}
			
			proxyURI = entry.getValue();		
			report = report + "Value:"+proxyURI;
			proxyURI = rebaseURI(proxyURI, oldBaseName, newBaseName);
			if (proxyURI==null){
				report = report + " >--> UNCHANGED\n";
			}else{
				entry.setValue(proxyURI);
				report = report + " >--> "+proxyURI+"\n";
			}
		}
		for (EObject key: delKey)  proxyMap.remove(key);
		for (Entry<EObject,EObject> entry : changeKey.entrySet()) {
			URI value = proxyMap.get(entry.getKey());
			proxyMap.remove(entry.getKey());
			proxyMap.put(entry.getValue(), value);
		}
		return report;
	}

	private URI rebaseURI(URI proxyURI, String oldBaseName, String newBaseName) {
		String filename = proxyURI.trimFileExtension().lastSegment();
		String fileExtension = proxyURI.fileExtension();
		if (filename!= null && filename.equals(oldBaseName)){
			proxyURI = proxyURI.trimSegments(1);
			proxyURI = proxyURI.appendSegment(newBaseName);
			proxyURI = proxyURI.appendFileExtension(fileExtension);
			String fragment = proxyURI.fragment();
			String prefix = "";
			String reference = "";
			if (fragment.contains("::")) {
				int index = fragment.lastIndexOf("::",fragment.length());
				prefix = fragment.substring(0, index);
				reference = fragment.substring(index, fragment.length());
				fragment = prefix+reference.replace("::"+oldBaseName, "::"+newBaseName);
			}
			proxyURI = proxyURI.appendFragment(fragment);
			return proxyURI;
		}
		return null;
	}

	private String rebaseforwardChanges(EventBNamedCommentedComponentElement oldBase, EventBNamedCommentedComponentElement newBase) {
		String report = "REBASE FORWARD CHANGES:\n\n";
		EList<Adapter> adapters = forwardChanges.eAdapters();
		List<Adapter> tbr = new ArrayList<Adapter>();
		for (Adapter adp : adapters){
			if (adp instanceof ChangeRecorder){
				//ChangeRecorder cr = (ChangeRecorder)adp;
				tbr.add(adp);
			}
		}
		adapters.removeAll(tbr);
		//adapters.remove(oldBase);
		//adapters.add((Adapter)newBase);
		
		report = rebase(forwardChanges, oldBase, newBase);	
		return report;
	}

	String report = "";
	private String rebase(Object object, EventBNamedCommentedComponentElement oldBase, EventBNamedCommentedComponentElement newBase) {
		report = "";
		List<EObject> drop = new ArrayList<EObject>();
		List<EObject> add = new ArrayList<EObject>();
		BasicEMap<EObject, EList<FeatureChange>> mapAdd = new BasicEMap<EObject, EList<FeatureChange>>();
		EObject newObject = null;

		if (object instanceof ChangeDescription){
			
			for (EObject eObject : ((ChangeDescription)object).getObjectsToAttach()){
				report = report + "Object to Attach : ";
				if ((newObject=matchingEObject(eObject, oldBase, newBase))!=null){
					add.add(newObject);
				}
			}
			((ChangeDescription)object).getObjectsToAttach().clear();;
			((ChangeDescription)object).getObjectsToAttach().addAll(add);
			add.clear();
			
			for (EObject eObject : ((ChangeDescription)object).getObjectsToDetach()){
				report = report + "Object to Detach : ";
				if ((newObject=matchingEObject(eObject, oldBase, newBase))!=null){
					add.add(newObject);
				}
			}
			((ChangeDescription)object).getObjectsToDetach().clear();
			((ChangeDescription)object).getObjectsToDetach().addAll(add);
			add.clear();

			EMap<EObject, EList<FeatureChange>> objectChanges = ((ChangeDescription)object).getObjectChanges();
			
			for (Entry<EObject, EList<FeatureChange>> objectChange : ((ChangeDescription)object).getObjectChanges()){
				report = report + "Object Change Key: ";
				EObject oldKey = objectChange.getKey();
				EObject newKey=matchingEObject(oldKey, oldBase, newBase);
				if (newKey!=null){
					EList<FeatureChange> featureChanges = objectChange.getValue();
					for (FeatureChange featureChange : featureChanges){
						String dataValue = featureChange.getDataValue();				//?for attributes: string representation of new feature value?
						EStructuralFeature feature = featureChange.getFeature(); 		//the meta feature that changed
						String featureName = featureChange.getFeatureName();			//the name of the feature that changed
						EList<ListChange> listChanges = featureChange.getListChanges(); //for many valued features: a list of list change descriptions.. e.g. kind: ADD, index: 1, moveToIndex =0 , dataValues = null
						EObject referenceValue = featureChange.getReferenceValue(); 	//?for references: eProxy of new feature value?
						Object value = featureChange.getValue();						//the new value of the feature (e.g. a list of elements in a containment)
						int i=0;
						if (false){		//more work here
							drop.add(featureChange);
						}
					}
					featureChanges.removeAll(drop);
					drop.clear();
					
					if (featureChanges.size()>0){
						mapAdd.remove(oldKey);
						mapAdd.put(newKey, featureChanges);
					}
					
				}
				
			}
			((ChangeDescription)object).getObjectChanges().clear();
			//((ChangeDescription)object).getObjectChanges().addAll(mapAdd); //THIS DOESN't WORK for some reason
			for (Entry<EObject, EList<FeatureChange>> entry : mapAdd){
				((ChangeDescription)object).getObjectChanges().put(entry.getKey(), entry.getValue());
				
			}
		}else{
			
		}
		return report;
	}
	
	private EObject matchingEObject(EObject eObject, EventBNamedCommentedComponentElement oldBase, EventBNamedCommentedComponentElement newBase){
		String oldBaseName = oldBase.getName();
		String newBaseName = newBase.getName();
		URI proxyURI = EcoreUtil.getURI((EObject)eObject);		
		report = report + proxyURI;
		proxyURI = rebaseURI(proxyURI, oldBaseName, newBaseName);
		if (proxyURI==null){
			report = report + " >--> UNCHANGED\n";
			return eObject;
		}else{
			EFactory factory = eObject.eClass().getEPackage().getEFactoryInstance();
			EObject newObject = factory.create(eObject.eClass());
			((InternalEObject) newObject).eSetProxyURI(proxyURI);
			newObject = EcoreUtil.resolve(newObject, rs);
			if (!newObject.eIsProxy()){
				report = report + " >--> "+proxyURI+"\n";
				return newObject;
			}else{
				report = report + " >--> DELETED\n";
				return null;
			}
		}
	}

	/**
	 * This writes the given text string out to a text file at the given uri
	 * 
	 * @param uri
	 * @param text
	 * @param monitor
	 */
	private void writeTextFile(URI uri, String text, IProgressMonitor monitor){
		IProject project = WorkspaceSynchronizer.getFile(res).getProject();
		try {
			String folderPath = "";
			for (int i=2; i<uri.segmentCount()-1; i++){
				folderPath = folderPath +"/"+ uri.segment(i);
				IFolder folder = project.getFolder(folderPath);
				if (!folder.exists()){
					folder.create(true, true, monitor);
				}
			}

			IPath filePath = new Path(uri.toPlatformString(true));
			filePath = filePath.makeRelativeTo(project.getFullPath());
			final IFile targetFile = project.getFile(filePath);
			InputStream stream = new ByteArrayInputStream(text.getBytes());
			if (targetFile.exists()) {
				try {
					targetFile.setContents(stream, true, true, monitor);
				} catch (CoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				targetFile.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String getTimeStamp() {
		Calendar cal = Calendar.getInstance();
		String m = ""+(cal.get(Calendar.MONTH)+1);
		m=m.length()<2? "0"+m : m;
		String d = ""+(cal.get(Calendar.DAY_OF_MONTH));
		d=d.length()<2? "0"+d : d;
		String h = ""+(cal.get(Calendar.HOUR_OF_DAY));
		h=h.length()<2? "0"+h : h;
		String n = ""+(cal.get(Calendar.MINUTE));
		n=n.length()<2? "0"+n : n;
		return ""+cal.get(Calendar.YEAR)+m+d+h+n;
	}

	private EObject getCorrespondingRefinedObject(Resource refinedRes,
			EObject abstractObject) {
		TreeIterator<EObject> contents = refinedRes.getAllContents();
		while (contents.hasNext()){
			EObject refinedObject = contents.next();
			EStructuralFeature refinesFeature = refinedObject.eClass().getEStructuralFeature("refines");
			if (refinesFeature != null){
				 Object refines = refinedObject.eGet(refinesFeature);
				 if (refinesFeature.isMany() && ((EList<?>)refines).contains(abstractObject)){
					 return refinedObject;
				 }else if (((EObject)refinesFeature) == abstractObject){
					 return refinedObject;
				 }
			}
		}
		return null;
	}

	/////////////////////////////////////
	/**
	 * This should be made into a class
	 * 
	 * 
	 */

//	private Map<EClass,IUMLBRefiner> refiners= new HashMap<EClass,IUMLBRefiner>();
//	//TODO: populate the IUMLB refiners by looking for contributions to the refinement participant extension (filtering any that don't implement IUMLBRefiner)
//	
//	private IUMLBRefiner getRefinerForObject(EObject eObject) {
//		if (eObject instanceof EventBObject){
//			EventBObject extension = ((EventBObject)eObject).getContaining(CorePackage.Literals.ABSTRACT_EXTENSION);
//			if (extension==null){
//				extension = ((EventBObject)eObject).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);		
//			}
//			IUMLBRefiner refiner = refiners.get(extension);
//			return refiner==null? basicRefiner : refiner;
//		}else{
//			return basicRefiner;
//		}
//	}
//	
//	private IUMLBRefiner basicRefiner = new BasicIUMLBRefiner();
//	/**
//	 * a basic clone refiner that doesn't filter any elements and drops all references
//	 * @author cfs
//	 *
//	 */
//	private class BasicIUMLBRefiner extends AbstractExtensionRefiner  {
//		//TODO: possibly should override the default filter and reference maps to remove the defaults
//		@Override
//		protected String getExtensionID() {
//			return null;
//		}
//	}; 
	
}
