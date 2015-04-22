package ac.soton.eventb.emf.diagrams.navigator.refactor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.command.UnexecutableCommand;
import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.BasicEMap;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.change.ChangeKind;
import org.eclipse.emf.ecore.change.FeatureChange;
import org.eclipse.emf.ecore.change.ListChange;
import org.eclipse.emf.ecore.change.util.ChangeRecorder;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.emf.core.util.EMFCoreUtil;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBObject;
import org.eventb.emf.persistence.EMFRodinDB;

import ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner;
import ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractExtensionRefiner.RefHandling;
import ac.soton.eventb.emf.core.extension.navigator.refiner.ElementRefinerRegistry;



//import ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractExtensionRefiner;
//import ac.soton.eventb.emf.core.extension.navigator.refiner.IUMLBRefiner;

public class CommitAssistant extends RefactorAssistant {

	protected Resource chRes2;
	protected Resource proxyMapResource2;
	private ChangeDescription forwardChanges;
	private ChangeDescription reverseChanges;
	
	public CommitAssistant(EventBNamedCommentedComponentElement component) {
		super(component);
		if (changes != null){
			reverseChanges= EcoreUtil.copy(changes); //clone reverse change description
			//create forward changes description (changes the original reverse change description)
			CopyReverseCommand command = new CopyReverseCommand(chRes, changes, proxyMap);
			ed.getCommandStack().execute(command);
			forwardChanges = command.getChanges();
			command.dispose();
		}
	}
	
	/**
	 * Apply forward Changes
	 * 
	 */
	//private final static String[] savedFolder = {"iumlb","saved"};
	private final static String[] reportsFolder = {"iumlb","reports"};
	public void applyChangesTo(EventBNamedCommentedComponentElement newComponent){
//		EMap<EObject, EList<FeatureChange>> reverseChanges=getChangeDescription().getObjectChanges();
		
		Resource refinedRes = newComponent.eResource();
		//refinedRes = EMFRodinDB.INSTANCE.loadResource(refinedRes.getURI());   //REMOVE THIS
		String timeStamp = getTimeStamp();
		String report = "\nPropagation report for "+refinedRes.getURI().toPlatformString(true)+" :: "+timeStamp+"\n";
		try {
			if (forwardChanges==null || newComponent.eResource().getResourceSet() != rs) return;
				CompoundCommand cc = new CompoundCommand();
		
				boolean method1 = false;
				if (method1){		//rebase changes and then apply them 
					report = report+ 
							rebaseforwardChanges(this.component, newComponent);
					report = report+
							rebaseProxyMap(this.component, newComponent);
					// saved somewhere else for debugging
//					chRes2 = RefactorPersistence.INSTANCE.getChangesResource(res, savedFolder);
//					proxyMapResource2 = RefactorPersistence.INSTANCE.getProxyMapResource(res, savedFolder);
					//RefactorPersistence.INSTANCE.putChanges(chRes2, forwardChanges, proxyMapResource2, proxyMap);
					cc.append(new ApplyReverseCommand(chRes,forwardChanges));
				
				}else{			//examine abstract changes and attempt to apply corresponding changes
					
					EMap<EObject, EList<FeatureChange>> reverseObjectChanges = reverseChanges.getObjectChanges();
					
					//first do containments so that references will work
					for (Entry<EObject, EList<FeatureChange>> change : forwardChanges.getObjectChanges()){
						EObject abstractObject = change.getKey();
						
						// get the reverse feature changes for this abstract object
						EList<FeatureChange> reverseFeatureChanges = reverseObjectChanges.get(abstractObject);
						if (reverseFeatureChanges==null) continue;
						
						// get a refiner for the epackage containing this abstract object
						String nsURI = abstractObject.eClass().getEPackage().getNsURI();
						AbstractElementRefiner refiner = ElementRefinerRegistry.getRegistry().getRefiner(nsURI);
						if (refiner==null) continue;
						
						// get the equivalent refined object
						EventBObject refinedObject = refiner.getEquivalentObject(newComponent, abstractObject);
						if (refinedObject==null) continue;
						
						// create commands to make corresponding changes to the refined object 
						for (FeatureChange featureChange : change.getValue()){
							FeatureChange reverseFeatureChange = findReverseFeatureChange(reverseFeatureChanges,featureChange);
							if (featureChange.getFeature() instanceof EReference &&
								((EReference)featureChange.getFeature()).isContainment() &&
								canApplyFeatureChange(refinedObject,featureChange, reverseFeatureChange)){
								cc.append(makeFeatureChangeCommand(abstractObject, refinedObject, featureChange, refiner));	
							}
						}
					}
					
					ed.getCommandStack().execute(cc);
					cc.dispose();
					cc = new CompoundCommand();
					
					//now do references etc
					for (Entry<EObject, EList<FeatureChange>> change : forwardChanges.getObjectChanges()){
						EObject abstractObject = change.getKey();
						
						// get the reverse feature changes for this abstract object
						EList<FeatureChange> reverseFeatureChanges = reverseObjectChanges.get(abstractObject);
						if (reverseFeatureChanges==null) continue;
						
						// get a refiner for the epackage containing this abstract object
						String nsURI = abstractObject.eClass().getEPackage().getNsURI();
						AbstractElementRefiner refiner = ElementRefinerRegistry.getRegistry().getRefiner(nsURI);
						if (refiner==null) continue;
						
						// get the equivalent refined object
						EventBObject refinedObject = refiner.getEquivalentObject(newComponent, abstractObject);
						if (refinedObject==null) continue;
						
						// create commands to make corresponding changes to the refined object 
						for (FeatureChange featureChange : change.getValue()){
							FeatureChange reverseFeatureChange = findReverseFeatureChange(reverseFeatureChanges,featureChange);
							if (!(featureChange.getFeature() instanceof EReference && ((EReference)featureChange.getFeature()).isContainment()) &&
								canApplyFeatureChange(refinedObject,featureChange, reverseFeatureChange)){
								cc.append(makeFeatureChangeCommand(abstractObject, refinedObject, featureChange, refiner));	
							}
						}
					}
					
					
					ed.getCommandStack().execute(cc);
					cc.dispose();
				}	
				
//				TransactionalEditingDomain ed2 = EMFRodinDB.INSTANCE.getEditingDomain();
//				Map<String, ?> atts = rs.getURIConverter().getAttributes(refinedRes.getURI(), null);
//				if (ed.isReadOnly(refinedRes)){
//					Map<Resource, Boolean> r2romap = ((AdapterFactoryEditingDomain)ed).getResourceToReadOnlyMap();
//					r2romap.remove(refinedRes);
//				}
	
				
// Better to save later (especially as this turns the component into a proxy)				
//				refinedRes.save(Collections.EMPTY_MAP);
				
				// set things up ready for the next refinement, if any
//				res = refinedRes;
//				component =newComponent;
				
////////////////////////////////////////////////////////				
// this gets the re-based forward Changes (which have just been reversed again) into the change recorder
//				proxyMap=new HashMap<EObject, URI>();
//				if (cr==null) cr = new ChangeRecorder();	//TODO: possibly adapt resumeRecording to do all this
//				cr.setEObjectToProxyURIMap(proxyMap);
//				BeginRecordingCommand brcommand = new BeginRecordingCommand(chRes, cr, forwardChanges , component);
//				ed.getCommandStack().execute(brcommand);
//				brcommand.dispose();
//				saveChanges();			// end recorder and save changes
				
//			}
		} finally{
			
//			forwardChanges=null; 	//reset forward changes so that they will be re-created for the next refinement
			URI commitReportURI = RefactorPersistence.INSTANCE.getRelatedURI(refinedRes, reportsFolder, timeStamp, "report");
			writeTextFile(commitReportURI, report, null);
		}
		return;
	}

	private Command makeFeatureChangeCommand(EObject abstractParent, EventBObject refinedParent, FeatureChange featureChange, AbstractElementRefiner refiner) {
		EventBNamedCommentedComponentElement concreteComponent = (EventBNamedCommentedComponentElement) refinedParent.getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
		EStructuralFeature feature = featureChange.getFeature();
		Object featureValue = featureChange.getValue();
		//FIXME: Temp
		if ("sender".equals(feature.getName())){
			int i=1;
		}
		if (feature.isMany()){
			CompoundCommand cc = new CompoundCommand();
			EList<ListChange> listChanges = featureChange.getListChanges();
			for (ListChange lc : listChanges){
				ChangeKind kind = lc.getKind();
				switch (kind){
				case ADD_LITERAL:
					List<Object> newValues = new ArrayList<Object>();
					if (lc.getDataValues()!=null && lc.getDataValues().size()>0){
						newValues.addAll(lc.getValues());
					}else{
						for (Object v : lc.getValues()){
							if (v instanceof EObject){
								URI uri = null;
								EObject refinedObject;
								if (feature instanceof EReference && ((EReference)feature).isContainment()){
									uri = EcoreUtil.getURI(abstractParent); //((EventBObject)abstractParent).getURI();
									String fragment = EcoreUtil.getID((EObject) v);
									int i = fragment.lastIndexOf("::")+2;
									String tail = fragment.substring(i);
									fragment=fragment.substring(0,i);
									fragment=fragment+uri.fragment().substring(i);
									fragment=fragment+"."+tail;
									uri = uri.appendFragment(fragment); 
									refinedObject = v instanceof EventBObject? refiner.refine(uri, (EventBObject) v, concreteComponent) : null;
								}else{
									refinedObject = refiner.getEquivalentObject(concreteComponent, (EObject) v);
								}
								if (refinedObject!=null){
									newValues.add(refinedObject);
								}
							}
						}
					}
					int index = lc.getIndex();
					int max = ((List<?>)refinedParent.eGet(feature)).size(); 
					cc.append(AddCommand.create(ed, refinedParent, feature, newValues, index>max? max : index ));
					break;
				case MOVE_LITERAL:
					break;
				case REMOVE_LITERAL:
					List<Object> removeValues = new ArrayList<Object>();
					if (lc.getDataValues()!=null){
						removeValues.addAll(lc.getValues());
					}else if (lc.getReferenceValues()!=null){
						for (Object v : lc.getValues()){
							EObject refinedObject = v instanceof EObject? refiner.getEquivalentObject(refinedParent, feature, (EObject)v) : null;
							if (refinedObject!=null){
								removeValues.add(refinedObject);
							}
						}
					}else {
						Object v = ((EList<?>)abstractParent.eGet(feature)).get(lc.getIndex());
						if (v instanceof EObject){
							EObject refinedObject = v instanceof EObject? refiner.getEquivalentObject(refinedParent, feature, (EObject)v) : null;
							if (refinedObject!=null) removeValues.add(refinedObject);
						}else{
							removeValues.add(v);							
						}
					}
					cc.append(RemoveCommand.create(ed, refinedParent, feature, removeValues));
					break;
				default:
					break;
				}
			}
			return cc;
		}else {
			//FIXME: for many features this will create a reference/containment into the abstract machine
			return SetCommand.create(ed, refinedParent, feature, featureChange.getValue());
		}
		//return UnexecutableCommand.INSTANCE;
	}

	private boolean canApplyFeatureChange(EObject refinedObject, FeatureChange featureChange, FeatureChange reverseFeatureChange) {
		EStructuralFeature feature = featureChange.getFeature();
		EClass cl = refinedObject.eClass();
		EList<EStructuralFeature> fs = refinedObject.eClass().getEStructuralFeatures();
		if (refinedObject.eClass().getEAllStructuralFeatures().contains(feature)){
			Object refinedFeatureValue = refinedObject.eGet(feature);
			Object oldAbstractFeatureValue = reverseFeatureChange.getValue();
			return equivalent(feature, refinedFeatureValue, oldAbstractFeatureValue);
		}	
		return false;
	}
	
	private boolean equivalent(EStructuralFeature feature, Object fv1, Object fv2){
		if (feature.getEType().equals(EcorePackage.Literals.ESTRING)){
			return stringEquivalent((String)fv1,(String)fv2);
		}else if (feature.getEType().equals(EcorePackage.Literals.EDATA_TYPE)){
			return fv1.equals(fv2);
		}else  if (feature.getEType().equals(EcorePackage.Literals.EOBJECT)){
			return fv1.equals(fv2);
		}else if (feature.isMany()){
			return true;
		} else {
			return fv1.equals(fv2);
		}
	}

	private boolean stringEquivalent(String s1, String s2) {
		String[] s1a = s1.split("[ \t \r \n \f ]");
		String[] s2a = s2.split("[ \t \r \n \f ]");	
		if (s1a.length!=s2a.length) return false;
		for (int i=0; i<s1a.length; i++){
			if (!s1a[i].equals(s2a[i])) 
				return false;
		}
		return true;
	}

	private FeatureChange findReverseFeatureChange(EList<FeatureChange> reverseFeatureChanges,FeatureChange featureChange) {
		for (FeatureChange fc : reverseFeatureChanges){
			if (fc.getFeatureName().equals(featureChange.getFeatureName())) return fc;
		}
		return null;
	}

	private EObject getEquivalentObject(EObject newParent, EObject abstractObject) {
		return getEquivalentObject(newParent, null, abstractObject);
	}
	private EObject getEquivalentObject(EObject newParent, EStructuralFeature feature, EObject abstractObject) {
		Iterator<?> contents = null;
		
		if (feature != null){
			Object featureValue =	newParent.eGet(feature);
			if (featureValue instanceof EList<?>)
			contents = ((EList<?>) newParent.eGet(feature)).iterator();
		}else{
			contents = newParent.eAllContents();
		}
		
		EClass clazz = abstractObject.eClass();
		EList<EStructuralFeature> features = clazz.getEAllStructuralFeatures();
		
		EStructuralFeature nameFeature = clazz.getEStructuralFeature("name");
		Object name = nameFeature==null? null : abstractObject.eGet(nameFeature);
		EStructuralFeature labelFeature = clazz.getEStructuralFeature("label");
		Object label = labelFeature==null? null : abstractObject.eGet(labelFeature);
		EStructuralFeature refinesFeature = clazz.getEStructuralFeature("refines");

		
		while (contents.hasNext()){
			Object possible = contents.next();
			if (possible instanceof EObject && ((EObject) possible).eClass() == clazz){
				if (nameFeature!=null && name!=null && name.equals(((EObject) possible).eGet(nameFeature))){
					return (EObject) possible;
				}
				if (refinesFeature!=null && !refinesFeature.isMany() && ((EObject) possible).eGet(refinesFeature) == abstractObject){
					return (EObject) possible;
				}
				
			}
		}
		return null;
	}

	////////////method 1/////////
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

//	private EObject getCorrespondingRefinedObject(Resource refinedRes,EObject abstractObject) {
//		TreeIterator<EObject> contents = refinedRes.getAllContents();
//		while (contents.hasNext()){
//			EObject refinedObject = contents.next();
//			EStructuralFeature refinesFeature = refinedObject.eClass().getEStructuralFeature("refines");
//			if (refinesFeature != null){
//				 Object refines = refinedObject.eGet(refinesFeature);
//				 if (refinesFeature.isMany() && ((EList<?>)refines).contains(abstractObject)){
//					 return refinedObject;
//				 }else if (((EObject)refinesFeature) == abstractObject){
//					 return refinedObject;
//				 }
//			}
//		}
//		return null;
//	}
//
//	private Map<EReference,RefHandling> referencemap = new HashMap<EReference,RefHandling>();
//	
//	/*
//	 * This sets up the references in the new refined model according to the 
//	 * settings in the referenceMap
//	 */
//	@SuppressWarnings("unchecked")
//	private void copyReferences(URI abstractResourceURI, String abstractComponentName, URI concreteResourceURI, String concreteComponentName, EventBElement concreteEventBElement) {
//		// Set up references in the new concrete model  (note that copier.copyReferences() does not work for this)
//		//get all the content of the root Element including itself
//		EList<EObject> contents = concreteEventBElement.getAllContained(CorePackage.Literals.EVENT_BELEMENT, true);
//		contents.add(concreteEventBElement);
//		// iterate through the contents looking for references corresponding to those declared in the referencemap
//		// and copy them in the appropriate way according to multiplicity and the refencemap.
//		for (EObject concreteElement : contents){
//			if (concreteElement instanceof EventBElement){
//				EReference referenceFeature;
//				for (Entry<EReference, RefHandling> referenceEntry : referencemap.entrySet()){
//					referenceFeature = referenceEntry.getKey(); 
//					if (referenceFeature.getEContainingClass().isSuperTypeOf(concreteElement.eClass())){
//						EObject abstractElement = getKeyByValue(copier, concreteElement);
//						//NOTE: *** Cannot use the concrete elements to create URIs because their parentage is not complete ***
//						if (referenceFeature.isMany()){
//							for (EObject abstractReferencedElement : (EList<EObject>)(getKeyByValue(copier, concreteElement)).eGet(referenceFeature)){		
//								EObject newValue = getNewReferenceValue(
//										abstractElement,
//										abstractReferencedElement,
//										referenceEntry.getValue());
//								if (newValue!=null){
//									((EList<EObject>)concreteElement.eGet(referenceFeature)).add(newValue);
//								}
//							}
//						}else{
//							if (referenceFeature.getEType() instanceof EClass){
//								EObject newValue = getNewReferenceValue(
//										abstractElement,
//										abstractElement.eGet(referenceFeature,false),
//										referenceEntry.getValue());
//								if (newValue!=null){
//									concreteElement.eSet(referenceFeature, newValue);
//								}
//							}
//						}
//					}
//				}
//			}
//		}
//	}
//
//
//	/**
//	 * @param abstractElement
//	 * @param abstractReferencedElement
//	 * @param handling
//	 * @return
//	 */
//	private EObject getNewReferenceValue(URI abstractResourceURI, String abstractComponentName, EObject abstractElement, EObject abstractReferencedElement, 
//			URI concreteResourceURI, EObject concreteComponent, RefHandling handling) {
//		EClass eclass = null;
//		URI uri = null;
//		switch (handling){
//		case CHAIN:
//			uri = getURI((EObject)abstractElement);
//			uri = uri==null? uri : uri.appendFragment(EcoreUtil.getID((EObject)abstractElement));
//			eclass = abstractElement.eClass();	
//			break;
//		case EQUIV:
//			//abstractReferencedElement = abstractElement.eGet(referenceFeature,false);
//			if (abstractReferencedElement instanceof EObject){
//				EObject target = getEquivalentObject(concreteComponent, abstractReferencedElement);
//				if (target != null){
//					uri = getURI(target);
//					eclass = target.eClass();
//				}
//			}
////				uri = getURI((EObject)abstractReferencedElement);
////				if (uri !=null && uri.path().equals(abstractResourceURI.path())){ //equiv only works for intra-machine refs
////					uri = concreteResourceURI.appendFragment(
////							EcoreUtil.getID((EObject)abstractReferencedElement).replaceAll("::"+abstractComponentName+"\\.", "::"+concreteComponentName+".") );
////					eclass = ((EObject)abstractReferencedElement).eClass();
////					break;
////				}
//			break;
//		case COPY:
//			//abstractReferencedElement = abstractElement.eGet(referenceFeature,false);
//			if (abstractReferencedElement instanceof EObject){
//				uri = getURI((EObject)abstractReferencedElement);
//				uri = uri==null? uri : uri.appendFragment(EcoreUtil.getID((EObject)abstractReferencedElement));
//				eclass = ((EObject)abstractReferencedElement).eClass();
//			}
//			break;
//		case DROP:
//			uri = null;
//			eclass = null;
//		}
//		return (uri==null || eclass==null)? null : EMFCoreUtil.createProxy(eclass, uri);
//	}
//
//	/**
//	 * this returns the URI without loading the eObject
//	 * (copied from EventBObject to support references to non-EventBObjects in refinement)
//	 * @param eObject
//	 * @return
//	 */
//	private static  URI getURI(EObject eObject) {
//		if (eObject==null) return null;
//		if (eObject.eIsProxy()){
//			return ((InternalEObject)eObject).eProxyURI();
//		}else{
//			return  eObject.eResource()==null? null : eObject.eResource().getURI();
//		}
//	}
//	
//	/////////////////////////////////////
//	/**
//	 * This should be made into a class
//	 * 
//	 * 
//	 */

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
