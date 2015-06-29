package ac.soton.eventb.emf.diagrams.navigator.refactor;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.change.ChangeKind;
import org.eclipse.emf.ecore.change.FeatureChange;
import org.eclipse.emf.ecore.change.ListChange;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.command.AddCommand;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBObject;

import ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner;
import ac.soton.eventb.emf.core.extension.navigator.refiner.ElementRefinerRegistry;


public class CommitAssistant extends RefactorAssistant {
	
	public CommitAssistant(EventBNamedCommentedComponentElement component) {
		super(component);
	}
	
	/**
	 * Apply the recorded changes to the given (refined) component where appropriate
	 * 
	 * Note that the recorded changes are in reverse (i.e. undo) and therefore need to be reversed as they are applied
	 * Changes are only applied where an equivalent element is still present in the refinement 
	 * and the corresponding feature has an equivalent value in the refinement.
	 * 
	 */
	public void applyChangesTo(EventBNamedCommentedComponentElement newComponent){
		
		String timeStamp = getTimeStamp();
		report = "\nPropagation report for "+newComponent.eResource().getURI().toPlatformString(true)+" :: "+timeStamp+"\n";
		
		try {
				if (changes==null || newComponent.eResource().getResourceSet() != rs) return;
				CompoundCommand cc = new CompoundCommand();

				//first do containments so that references will work
				for (Entry<EObject, EList<FeatureChange>> change : changes.getObjectChanges()){

					EObject abstractObject = change.getKey();
					if (abstractObject==null) continue;
					
					// get a refiner for the ePackage containing this abstract object
//					EClass ec = abstractObject.eClass();
//					EPackage ep = ec==null ? null : ec.getEPackage();
//					if (ec==null||ep==null){
//						int i = 0;
//					}
					
					String nsURI = abstractObject.eClass().getEPackage().getNsURI();
					AbstractElementRefiner refiner = ElementRefinerRegistry.getRegistry().getRefiner(nsURI);
					if (refiner==null) continue;
					
					// get the equivalent refined object
					EventBObject refinedObject = refiner.getEquivalentObject(newComponent, abstractObject);
					if (refinedObject==null) continue;
					
					// create commands to make corresponding changes to the refined object 
					for (FeatureChange reverseFeatureChange : change.getValue()){
						//FeatureChange reverseFeatureChange = findReverseFeatureChange(reverseFeatureChanges,featureChange);
						if (reverseFeatureChange.getFeature() instanceof EReference &&
							((EReference)reverseFeatureChange.getFeature()).isContainment() &&
							canApplyFeatureChange(refinedObject, reverseFeatureChange)){
							cc.append(makeFeatureChangeCommand(abstractObject, refinedObject, reverseFeatureChange, refiner));	
						}
					}
					
					ed.getCommandStack().execute(cc);
					cc.dispose();
					cc = new CompoundCommand();
				}
				
				//now do references etc
				for (Entry<EObject, EList<FeatureChange>> change : changes.getObjectChanges()){
					
					EObject abstractObject = change.getKey();
					if (abstractObject==null) continue;

					// get a refiner for the ePackage containing this abstract object
					String nsURI = abstractObject.eClass().getEPackage().getNsURI();
					AbstractElementRefiner refiner = ElementRefinerRegistry.getRegistry().getRefiner(nsURI);
					if (refiner==null) continue;

					// get the equivalent refined object
					EventBObject refinedObject = refiner.getEquivalentObject(newComponent, abstractObject);
					if (refinedObject==null) continue;

					// create commands to make corresponding changes to the refined object 
					for (FeatureChange reverseFeatureChange : change.getValue()){
						//FeatureChange reverseFeatureChange = findReverseFeatureChange(reverseFeatureChanges,featureChange);
						if (!(reverseFeatureChange.getFeature() instanceof EReference && ((EReference)reverseFeatureChange.getFeature()).isContainment()) &&
							canApplyFeatureChange(refinedObject, reverseFeatureChange)){
							cc.append(makeFeatureChangeCommand(abstractObject, refinedObject, reverseFeatureChange, refiner));	
						}
					}
				}

				ed.getCommandStack().execute(cc);
				cc.dispose();

//				TransactionalEditingDomain ed2 = EMFRodinDB.INSTANCE.getEditingDomain();
//				Map<String, ?> atts = rs.getURIConverter().getAttributes(refinedRes.getURI(), null);
//				if (ed.isReadOnly(refinedRes)){
//					Map<Resource, Boolean> r2romap = ((AdapterFactoryEditingDomain)ed).getResourceToReadOnlyMap();
//					r2romap.remove(refinedRes);
//				}
			
		} finally{
			URI commitReportURI = RefactorPersistence.INSTANCE.getRelatedURI(newComponent.eResource(), reportsFolder, timeStamp, "report");
			writeTextFile(commitReportURI, report, null);
		}
		return;
	}

	private Command makeFeatureChangeCommand(EObject abstractParent, EventBObject refinedParent, FeatureChange reverseFeatureChange, AbstractElementRefiner refiner) {
		EventBNamedCommentedComponentElement concreteComponent = (EventBNamedCommentedComponentElement) refinedParent.getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
		EStructuralFeature feature = reverseFeatureChange.getFeature();
		if (feature.isMany()){
			CompoundCommand cc = new CompoundCommand();
			EList<ListChange> listChanges = reverseFeatureChange.getListChanges();
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
					report = report+"\nAdd "+getURIsAsCSL(newValues)+" to "+feature.getName()+" of "+refinedParent.getURI().fragment();
					cc.append(AddCommand.create(ed, refinedParent, feature, newValues, index>max? max : index ));
					break;
				case MOVE_LITERAL:
					break;
				case REMOVE_LITERAL:
					List<Object> removeValues = new ArrayList<Object>();
					if (lc.getDataValues()!=null && lc.getDataValues().size()>0){
						removeValues.addAll(lc.getValues());
					}else if (lc.getReferenceValues()!=null && lc.getReferenceValues().size()>0){
						for (Object v : lc.getValues()){
							EObject refinedObject = v instanceof EObject? refiner.getEquivalentObject(refinedParent, feature, (EObject)v) : null;
							if (refinedObject!=null){
								removeValues.add(refinedObject);
							}
						}
					}else {
						//FIXME: where do we get the abstract removed object from?
						Object v = null; //((EList<?>)abstractParent.eGet(feature)).get(lc.getIndex());
						if (v instanceof EObject){
							EObject refinedObject = v instanceof EObject? refiner.getEquivalentObject(refinedParent, feature, (EObject)v) : null;
							if (refinedObject!=null) removeValues.add(refinedObject);
						}else if (v!=null){
							removeValues.add(v);							
						}
					}
					report = report+"\nRemove "+getURIsAsCSL(removeValues)+" from "+feature.getName()+" of "+refinedParent.getURI().fragment();
					cc.append(RemoveCommand.create(ed, refinedParent, feature, removeValues));
					break;
				default:
					break;
				}
			}
			return cc;
		}else {
			Object newValue =  abstractParent.eGet(feature);	//get the referenced target/value from the current model
			if (feature instanceof EReference && isIn(component,(EObject)newValue)){
				newValue = refiner.getEquivalentObject(concreteComponent, (EObject) newValue);
			}
			if (newValue == null) newValue = SetCommand.UNSET_VALUE;
			report = report+"\nSet "+feature.getName()+" of "+EcoreUtil.getURI(refinedParent).fragment()+" to \""+newValue.toString()+"\"";
			return SetCommand.create(ed, refinedParent, feature, newValue);
		}
	}

	private boolean isIn(EObject container,EObject child) {
		if (child==null || container==null) return false;
		if (container==child) return true;
		return isIn(container,child.eContainer());
	}
	
	private String getURIsAsCSL(List<Object> newValues) {
		String ret = "";
		for (Object o : newValues){
			if (ret.length()>0) ret=ret+",";
			if (o instanceof EventBObject){
				ret = ret + EcoreUtil.getURI((EventBObject)o).fragment();
			}else{
				ret = ret + o.toString();
			}
		}
		return ret.length()>0? ret : "FAILED";
	}
	
	private boolean canApplyFeatureChange(EObject refinedObject, FeatureChange reverseFeatureChange) {
		EStructuralFeature feature = reverseFeatureChange.getFeature();
		if (refinedObject.eClass().getEAllStructuralFeatures().contains(feature)){
			Object refinedFeatureValue = refinedObject.eGet(feature);
			Object oldAbstractFeatureValue = reverseFeatureChange.getValue();
			return isEquivalent(feature, refinedFeatureValue, oldAbstractFeatureValue);
		}	
		return false;
	}
	
	private boolean isEquivalent(EStructuralFeature feature, Object fv1, Object fv2){
		if (feature.getEType().equals(EcorePackage.Literals.ESTRING)){
			return isStringEquivalent((String)fv1,(String)fv2);
		}else if (feature.getEType().equals(EcorePackage.Literals.EDATA_TYPE)){
			return fv1.equals(fv2);
		}else  if (feature.getEType().equals(EcorePackage.Literals.EOBJECT)){
			return fv1.equals(fv2);
		}else if (feature.isMany()){
			return true;
		} else {
			return fv1 == null? fv2==null : fv1.equals(fv2);
		}
	}

	private boolean isStringEquivalent(String s1, String s2) {
		if (s1==null) return s2==null;
		if (s2==null) return false;
		String s1r = s1.replaceAll("\\s", "");
		String s2r = s2.replaceAll("\\s", "");
		return s1r.equals(s2r);
	}
	
	//////////////////////// Text reporting /////////////////////////////////
	private final static String[] reportsFolder = {"iumlb","reports"};
	protected String report = "";
	
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
}
