package ac.soton.eventb.emf.diagrams.navigator.refactor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.command.CompoundCommand;
import org.eclipse.emf.common.command.UnexecutableCommand;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.change.ChangeKind;
import org.eclipse.emf.ecore.change.FeatureChange;
import org.eclipse.emf.ecore.change.ListChange;
import org.eclipse.emf.ecore.util.EContentsEList;
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
import ac.soton.eventb.emf.diagrams.navigator.refactor.persistence.RefactorPersistence;
import ac.soton.eventb.emf.diagrams.navigator.refactor.persistence.TextFile;


public class CommitAssistant extends RefactorAssistant {
	
	private final static String[] reportsFolder = {"iumlb","reports"};
	
	private Map<EObject,URI> oldNameMap = new HashMap<EObject,URI>();
	private TextFile report = new TextFile();
	EObject oldComponent;
	Map<EObject, EObject> equivMap;
	Map<Object,Object> newObjects = new HashMap<Object,Object>();
	
	public CommitAssistant(EventBNamedCommentedComponentElement component) {
		super(component);
	}
	
	/**
	 * Apply the recorded changes to the given (refined) component where appropriate
	 * 
	 * Note that the recorded changes are in reverse (i.e. undo) format and therefore need to be reversed as they are applied.
	 * 
	 * Changes are only applied where an equivalent element is still present in the refinement 
	 * and the corresponding feature has an equivalent value in the refinement.
	 * 
	 */
	public void applyChangesTo(EventBNamedCommentedComponentElement newComponent){
		
		String timeStamp = getTimeStamp();
		oldNameMap.clear();
		oldComponent = null;
		equivMap = null;
		newObjects.clear();
		report.setText("");
		report.addLine("Propagation report for "+newComponent.eResource().getURI().toPlatformString(true)+" :: "+timeStamp+"\n");
		
		try {
				if (changes==null || newComponent.eResource().getResourceSet() != rs) return;
				CompoundCommand cc = new CompoundCommand();

				preApply();
				
				//first check for name changes which will break vertical references as well as the equivalence map used later
				for (Entry<EObject, EList<FeatureChange>> change : changes.getObjectChanges()){
					EObject abstractObject = change.getKey();
					if (abstractObject==null) continue;
					EList<FeatureChange> abstractFeatureChanges = change.getValue();
					// check for name changes 
					for (FeatureChange reverseFeatureChange : abstractFeatureChanges){
						EStructuralFeature feature = reverseFeatureChange.getFeature();
						if (feature instanceof EAttribute && ((EAttribute)feature).getName().equals("name")){
							Object v = reverseFeatureChange.getValue();
							if (v instanceof String){
								URI uri = EcoreUtil.getURI(abstractObject);
								String fragment = uri.fragment();
								fragment=fragment.substring(0,fragment.lastIndexOf((String)abstractObject.eGet(feature)));
								fragment=fragment+v;
								uri = uri.trimFragment().appendFragment(fragment);
								oldNameMap.put(abstractObject, uri);
							}
						}
					}		
				}
				
				//fix vertical references to account for name changes
				updateVerticalReferences(cc, newComponent);
				cc = executeAndResetCommand(cc);
				
				//now do containments so that references to new elements will work
				for (Entry<EObject, EList<FeatureChange>> change : changes.getObjectChanges()){

					EObject abstractObject = change.getKey();
					if (abstractObject==null) continue;
					EList<FeatureChange> abstractFeatureChanges = change.getValue();
					
					// get a refiner for the ePackage containing this abstract object
					String nsURI = abstractObject.eClass().getEPackage().getNsURI();
					AbstractElementRefiner refiner = ElementRefinerRegistry.getRegistry().getRefiner(nsURI);
					if (refiner==null) continue;
					
					// get the equivalent refined object
					EObject refinedObject = getEquivalentObjectInRefinement(newComponent, abstractObject, refiner);
					if (refinedObject==null) continue;
					
					// create commands to make corresponding changes to the refined object 
					for (FeatureChange reverseFeatureChange : abstractFeatureChanges){
						EStructuralFeature feature = reverseFeatureChange.getFeature();
						//FeatureChange reverseFeatureChange = findReverseFeatureChange(reverseFeatureChanges,featureChange);
						if (feature instanceof EReference && ((EReference)feature).isContainment() &&
							canApplyFeatureChange(newComponent, refinedObject, reverseFeatureChange, refiner)){
							makeFeatureChangeCommand(cc, abstractObject, refinedObject, reverseFeatureChange, refiner);	
						}
					}
					cc = executeAndResetCommand(cc);
				}
				
				//now do references and attributes etc
				for (Entry<EObject, EList<FeatureChange>> change : changes.getObjectChanges()){
					
					EObject abstractObject = change.getKey();
					if (abstractObject==null) continue;

					// get a refiner for the ePackage containing this abstract object
					String nsURI = abstractObject.eClass().getEPackage().getNsURI();
					AbstractElementRefiner refiner = ElementRefinerRegistry.getRegistry().getRefiner(nsURI);
					if (refiner==null) continue;

					EObject refinedObject = getEquivalentObjectInRefinement(newComponent, abstractObject, refiner);
					if (refinedObject==null) {
						continue;
					}
					// create commands to make corresponding changes to the refined object 
					for (FeatureChange reverseFeatureChange : change.getValue()){
						//FeatureChange reverseFeatureChange = findReverseFeatureChange(reverseFeatureChanges,featureChange);
						EStructuralFeature feature = (EStructuralFeature)reverseFeatureChange.getFeature();
						if (!(feature instanceof EReference && ((EReference)feature).isContainment()) &&
								!(feature instanceof EReference && ((EReference)feature).getEOpposite()!=null && !((EReference)feature).isMany()) &&
							canApplyFeatureChange(newComponent, refinedObject, reverseFeatureChange, refiner)){
							makeFeatureChangeCommand(cc, abstractObject, refinedObject, reverseFeatureChange, refiner);	
						}
					}
				}
				cc = executeAndResetCommand(cc);
	
		} finally{
			IProject project = WorkspaceSynchronizer.getFile(res).getProject();
			URI commitReportURI = RefactorPersistence.INSTANCE.getRelatedURI(newComponent.eResource(), reportsFolder, timeStamp, "report");
			report.save(project, commitReportURI, null);
		}
		return;
	}

	/**
	 * @param cc
	 * @return
	 */
	private CompoundCommand executeAndResetCommand(CompoundCommand cc) {
		if (!cc.isEmpty()){
			if (cc.canExecute()){
				report.addLine(">>> SUCCESS >>> The command for the above items was executable ");
				ed.getCommandStack().execute(cc);
			}else{
				report.addLine(">>> FAILURE >>> The command for the above items was not executable ");					
			}
		}
		cc.dispose();
		cc = new CompoundCommand();
		return cc;
	}

	private void updateVerticalReferences(CompoundCommand cc, EObject parent) {
		for (@SuppressWarnings("rawtypes")
		EContentsEList.FeatureIterator featureIterator =  (EContentsEList.FeatureIterator)parent.eCrossReferences().iterator(); featureIterator.hasNext(); )
		{
			EObject target = (EObject)featureIterator.next();
			EReference feature = (EReference)featureIterator.feature();
			if (target.eIsProxy() &&
				oldNameMap.containsValue(((InternalEObject)target).eProxyURI())) {
				//command to set feature to the renamed abstract object corresponding to the old reference URI
				Object newValue = getKeyByValue(oldNameMap, ((InternalEObject)target).eProxyURI());
				if (newValue!=null){
					report.addLine("\nSet "+feature.getName()+" of "+EcoreUtil.getURI(parent).fragment()+" to \""+newValue.toString()+"\"");
					cc.append(SetCommand.create(ed, parent, feature, newValue));
				}
			}
		}
		for (EObject child : parent.eContents()){
			updateVerticalReferences(cc, child);
		}
	}

	private static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (value.equals(entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}
	
	/**
	 * @param newComponent
	 * @param abstractObject
	 * @param refiner
	 * @return
	 */
	private EObject getEquivalentObjectInRefinement(
			EventBNamedCommentedComponentElement newComponent,
			EObject abstractObject, AbstractElementRefiner refiner) {
		// get the equivalent refined object
		if (newObjects.containsKey(abstractObject)){
			return (EObject)newObjects.get(abstractObject);
		}
		EObject refinedObject = refiner.getEquivalentObject(newComponent, abstractObject);
		if (refinedObject==null) {
			EObject oldObject = getOldObject(abstractObject);
			refinedObject = oldObject instanceof EObject? refiner.getEquivalentObject(newComponent, oldObject) : null;
		}
		return refinedObject;
	}
	
	/**
	 * 	list changes of type REMOVE (i.e. for items that have been added) refer to the element to be
	 *  removed by index into the original list rather than by caching the element in the lc. 
	 *  Unfortunately the index assumes and previous removal lc on the same feature have been applied.
	 *  Hence we need to calculate the real index into the current abstract list by counting how many 
	 *  lc remove from the same feature.
	 * 
	 */
	 protected Map<ListChange, Integer> listChangeIndex = new HashMap<ListChange,Integer>();
	/**
	 *	populate list change index map 
	 *	
	 */
	private void preApply() {
		Map<EObject, Map<EStructuralFeature,Integer>> indexMap = new HashMap<EObject, Map<EStructuralFeature,Integer>>();
		for (Entry<EObject, EList<FeatureChange>> change : changes.getObjectChanges()){
			EObject eObject = change.getKey();
			for (FeatureChange fc : change.getValue()){
				EStructuralFeature feature = fc.getFeature();
				EList<ListChange> listChanges = fc.getListChanges();
				for (ListChange lc : listChanges){
					if (lc.getKind()==ChangeKind.REMOVE_LITERAL){
						int indexOffset=0;
						if (indexMap.containsKey(eObject) && 
								indexMap.get(eObject).containsKey(feature)) {
							 indexOffset = indexMap.get(eObject).get(feature);
							 int newIndex = lc.getIndex()+indexOffset;
							 listChangeIndex.put(lc,newIndex);
						}
						if (!indexMap.containsKey(eObject)){
							indexMap.put(eObject,new HashMap<EStructuralFeature,Integer>());
						}
						indexMap.get(eObject).put(feature, new Integer(indexOffset+1));
					}
				}
			}
		}
	}
	
	/**
	 * Generates a command to apply a (reversed) feature change to the refined parent
	 * 
	 * Note that the feature change is in reversed (i.e. undo) format. Hence the value it contains is the original old value. 
	 * The current (new) valaue must be obtained by reading the feature in the abstract Parent. 
	 * The given refiner is used to generate new refined elements for additions aas well as to find corresponding elements 
	 * to be changed.
	 * @param cc2 
	 * 
	 * @param abstractParent
	 * @param refinedParent
	 * @param reverseFeatureChange
	 * @param refiner
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private Command makeFeatureChangeCommand(CompoundCommand cc, EObject abstractParent, EObject refinedParent, FeatureChange reverseFeatureChange, AbstractElementRefiner refiner) {
		if (!(refinedParent instanceof EventBObject)){
			return UnexecutableCommand.INSTANCE;
		}
		EventBNamedCommentedComponentElement concreteComponent = (EventBNamedCommentedComponentElement) ((EventBObject)refinedParent).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
		EStructuralFeature feature = reverseFeatureChange.getFeature();
		Object oldAbstractFeatureValue = reverseFeatureChange.getValue();
		if (feature.isMany()){
			if (oldAbstractFeatureValue instanceof EList<?>){
				//in some cases undo is specified as a resulting value even for a list feature
				// in this case we need to add the equivalent of everything in the current model except 
				// the items in this final value.

				EList<Object> newAbstractValuesList =  (EList<Object>)abstractParent.eGet(feature);	//get the referenced target/value from the current model
				List<Object> newRefinedValuesList = new ArrayList<Object>();
				for (Object newAbstractValue : newAbstractValuesList){
					if (!((EList<?>)oldAbstractFeatureValue).contains(newAbstractValue)){
						Object newRefinedValue = null;
						if (feature instanceof EReference && newAbstractValue instanceof EObject){
							if (((EReference)feature).isContainment()){
								newRefinedValue = makeRefinedElement(
									abstractParent, refiner,
									concreteComponent, (EObject)newAbstractValue);
								newObjects.put(newAbstractValue, newRefinedValue);
							}else if (isIn(component,(EObject)newAbstractValue)){	//for now we only support intra-component references here
								newRefinedValue = getEquivalentObjectInRefinement(concreteComponent, (EObject)newAbstractValue, refiner);
							}else{
								newRefinedValue = newAbstractValue;
							}
						}else{
							newRefinedValue = newAbstractValue;
						}
						if (!((EList<Object>)refinedParent.eGet(feature)).contains(newRefinedValue)){
							newRefinedValuesList.add(newRefinedValue);
						}else{
							report.addLine("    (n.b. "+feature.getName()+" of "+EcoreUtil.getURI(refinedParent).fragment()+" already contains \""+newRefinedValue+"\")");
						}
					}
				}
				if (!newRefinedValuesList.isEmpty()) {
					report.addLine("Add "+getURIsAsCSL(newRefinedValuesList)+" to "+feature.getName()+" of "+EcoreUtil.getURI(refinedParent).fragment());
					cc.append(AddCommand.create(ed, refinedParent, feature, newRefinedValuesList));
				}
				List<Object> refinedValuesToRemove = new ArrayList<Object>();
				for (Object oldAbstractValue : (EList<?>)oldAbstractFeatureValue){
					Object refinedValueToRemove;
					if (!newAbstractValuesList.contains(oldAbstractValue)){
						if (feature instanceof EReference && oldAbstractValue instanceof EObject && isIn(component,(EObject)oldAbstractValue)){
							refinedValueToRemove = getEquivalentObjectInRefinement(concreteComponent, (EObject)oldAbstractValue, refiner);
						}else{
							refinedValueToRemove = oldAbstractValue;
						}
						if (((EList<Object>)refinedParent.eGet(feature)).contains(refinedValueToRemove)){
							refinedValuesToRemove.add(refinedValueToRemove);
						}else{
							report.addLine("    (n.b. "+feature.getName()+" of "+EcoreUtil.getURI(refinedParent).fragment()+" already doesn't contain \""+refinedValueToRemove+"\")");
						}
					}
				}
				if (!refinedValuesToRemove.isEmpty()) {
					report.addLine("Remove "+getURIsAsCSL(refinedValuesToRemove)+" from "+feature.getName()+" of "+EcoreUtil.getURI(refinedParent).fragment());
					cc.append(RemoveCommand.create(ed, refinedParent, feature, refinedValuesToRemove));
				}
				
			}else{
				//FIXME: LOOKS LIKE THIS BRANCH IS NEVER TAKEN!!
				//when undo is specified as a collection of list changes we need to iterate through the changes 
				// and reverse each individual change
				EList<ListChange> listChanges = reverseFeatureChange.getListChanges();
				for (ListChange lc : listChanges){
					ChangeKind kind = lc.getKind();
					switch (kind){
					case REMOVE_LITERAL:	//I.E. ADD values to the list in the refinement
						List<Object> newRefinedValuesList = new ArrayList<Object>();
						List<Object> newAbstractValuesList = new ArrayList<Object>();
						if (lc.getValues()!=null && lc.getValues().size()>0){
							newAbstractValuesList.addAll(lc.getValues());
						} else if (lc.getReferenceValues()!=null && lc.getReferenceValues().size()>0) {
							newAbstractValuesList.addAll(lc.getReferenceValues());
						} else {
							//Single Value to be added - get it from current model using index
							// (note lc index assumes that other changes to this feature have been applied
							int i = listChangeIndex.containsKey(lc) ? listChangeIndex.get(lc) : lc.getIndex();
							Object v = ((EList<?>)abstractParent.eGet(feature)).get(i);
							if (v!=null){
								newAbstractValuesList.add(v);							
							}
						}
						for (Object newAbstractValue : newAbstractValuesList){
							Object newRefinedValue;
							if (newAbstractValue instanceof EObject){
								if (feature instanceof EReference && ((EReference)feature).isContainment()){
									newRefinedValue = makeRefinedElement(
											abstractParent, refiner,
											concreteComponent, (EObject)newAbstractValue);
									newObjects.put(newAbstractValue, newRefinedValue);
								}else if(isIn(component,(EObject)newAbstractValue)){
									newRefinedValue = getEquivalentObjectInRefinement(concreteComponent, (EventBObject) newAbstractValue, refiner);
								}else{
									newRefinedValue = newAbstractValue;
								}
							}else{
								newRefinedValue = newAbstractValue;
							}
							if (!((EList<Object>)refinedParent.eGet(feature)).contains(newRefinedValue)){
								newRefinedValuesList.add(newRefinedValue);
							}else{
								report.addLine("    (n.b. "+feature.getName()+" of "+EcoreUtil.getURI(refinedParent).fragment()+" already contains \""+newRefinedValue+"\")");
							}
						}
//						int index = lc.getIndex();
//						int max = ((List<?>)refinedParent.eGet(feature)).size(); 
						if (!newRefinedValuesList.isEmpty()) {
							report.addLine("Add "+getURIsAsCSL(newRefinedValuesList)+" to "+feature.getName()+" of "+EcoreUtil.getURI(refinedParent).fragment());
							cc.append(AddCommand.create(ed, refinedParent, feature, newRefinedValuesList)); //, index>max? max : index ));
						}
						break;
					case MOVE_LITERAL:
						break;
					case ADD_LITERAL:	//I.E. REMOVE Values from the list in the refinement
						List<Object> refinedValuesToRemove = new ArrayList<Object>();
						if (lc.getDataValues()!=null && lc.getDataValues().size()>0){
							//FIXME: should this include the check that the value is there to be removed?
							refinedValuesToRemove.addAll(lc.getValues());
						}else if (lc.getReferenceValues()!=null && lc.getReferenceValues().size()>0){
							for (Object v : lc.getValues()){
								Object refinedValueToRemove = getEquivalentObjectInRefinement(concreteComponent, (EventBObject) v, refiner);
								if (((EList<Object>)refinedParent.eGet(feature)).contains(refinedValueToRemove)){
									refinedValuesToRemove.add(refinedValueToRemove);
								}else{
									report.addLine("    (n.b. "+feature.getName()+" of "+EcoreUtil.getURI(refinedParent).fragment()+" already doesn't contain \""+refinedValueToRemove+"\")");
								}
							}
						}else {
							@SuppressWarnings("unused")
							int i =0;
							//EList<Object> v = lc.getValues();
							//SHOULD NOT GET HERE AS ADD ALWAYS SETS VALUES 
						}
						if (refinedValuesToRemove.size()>0){
							report.addLine("Remove "+getURIsAsCSL(refinedValuesToRemove)+" from "+feature.getName()+" of "+EcoreUtil.getURI(refinedParent).fragment());
							cc.append(RemoveCommand.create(ed, refinedParent, feature, refinedValuesToRemove));
						}
						break;
					default:
						break;
					}
				}

			}
		}else {
			Object newAbstractValue =  abstractParent.eGet(feature);	//get the referenced target/value from the current model
			Object newRefinedValue = null;
			if (feature instanceof EReference){
				if (((EReference)feature).isContainment()){
					newRefinedValue = makeRefinedElement(
							abstractParent, refiner,
							concreteComponent, (EObject)newAbstractValue);
					newObjects.put(newAbstractValue, newRefinedValue);
				}else if (isIn(component,(EObject)newAbstractValue)){
					newRefinedValue = getEquivalentObjectInRefinement(concreteComponent, (EventBObject)newAbstractValue, refiner);
				}else{
					newRefinedValue = newAbstractValue;
				}
			}else{
				newRefinedValue = newAbstractValue;				
			}
			if (newRefinedValue == null && refinedParent.eGet(feature)!= null) newRefinedValue = SetCommand.UNSET_VALUE;
			if (newRefinedValue!= null && newRefinedValue==refinedParent.eGet(feature,true)){
				report.addLine("    (n.b. "+feature.getName()+" of "+EcoreUtil.getURI(refinedParent).fragment()+" was already set to \""+newRefinedValue+"\")");
			}else{
				report.addLine("Set "+feature.getName()+" of "+EcoreUtil.getURI(refinedParent).fragment()+" to \""+newRefinedValue.toString()+"\"");
				cc.append(SetCommand.create(ed, refinedParent, feature, newRefinedValue));
			}
		}
		return cc;
	}

	/**
	 * @param abstractParent
	 * @param refiner
	 * @param concreteComponent
	 * @param v
	 * @return
	 */
	private EObject makeRefinedElement(EObject abstractParent,
			AbstractElementRefiner refiner,
			EventBNamedCommentedComponentElement concreteComponent, EObject abstractElement) {
		EObject refinedObject;
		if (abstractElement instanceof EventBObject) {
			URI uri = EcoreUtil.getURI(abstractElement);
			//FIXME: eOpposite references may be a problem: the reference in the refined element is  populated but its eOpposite elsewhere may not be.
			//FIXME: e.g. for a new transition, source and target are set but the referenced states do not have incoming/outgoing set to the new transition 
			refinedObject =  refiner.refine(uri, (EventBObject)abstractElement, concreteComponent);
		} else {
			// for non-Event-B elements the best we can do is to return a copy
			refinedObject = EcoreUtil.copy(abstractElement);
		}
		return refinedObject;
	}
	
	/**
	 * gets the old (pre-changes) eObject that is equivalent to the given one.
	 * however, where names have been changed this would fail because the proxy URI is changed by the new name,
	 * therefore it checks the prepared name changes map and updates the equivalence map with a similar old but renamed eObject
	 * before retrieving it. Once the equivalence map has been updated for the name change the name change entry is removed from the 
	 * name changes map so that next time this key object is retrieved, the equivalence map is used directly.
	 **/
	private EObject getOldObject(EObject v)  {
		try {
			if (oldComponent == null){
					oldComponent = RefactorPersistence.INSTANCE.getPreComponent(res);
			}
			if (equivMap == null){
				equivMap = RefactorPersistence.INSTANCE.getEquivalenceMap(res);
			}
			if (oldNameMap.containsKey(v)){
				EObject value = null;
				for (Entry<EObject, EObject> en : equivMap.entrySet()){
					EObject k = en.getKey();
					String uri1 = EcoreUtil.getURI(k).toString();
					String uri2 = oldNameMap.get(v).toString();
					if (uri1.equals(uri2)){
						value = (EObject)en.getValue();
						continue;
					}
				}
				if (value != null) {
					equivMap.put(v, value);
					oldNameMap.remove(v);
				}
			}
			return (EObject) equivMap.get(v);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Returns true if the child EObject is directly or indirectly contained in the container EObject
	 * @param container
	 * @param child
	 * @return
	 */
	private boolean isIn(EObject container,EObject child) {
		if (child==null || container==null) return false;
		if (container==child) return true;
		return isIn(container,child.eContainer());
	}
	
	/**
	 * Converts a list of objects into a comma separated list of identifiers
	 * For EventBObjects the identifiers come from their uri fragments
	 * 
	 * @param collection
	 * @return
	 */
	private String getURIsAsCSL(Collection<Object> collection) {
		String ret = "";
		for (Object o : collection){
			if (ret.length()>0) ret=ret+", /n/t";
			if (o instanceof EventBObject){
				ret = ret + EcoreUtil.getURI((EventBObject)o).fragment();
			}else{
				ret = ret + o.toString();
			}
		}
		return ret.length()>0? ret : "FAILED";
	}
	
	/**
	 * tests whether the (reverse) FeatureChange can be applied to the EObject
	 * this is true if 
	 * a) the refined object is new OR
	 * b) the feature is many OR
	 * c) the corresponding feature value in the target object is
	 * equivalent to the old value from the feature change
	 * 
	 *
	 * @param refinedObject
	 * @param reverseFeatureChange
	 * @return
	 */
	private boolean canApplyFeatureChange(EventBNamedCommentedComponentElement concreteComponent, EObject refinedObject, FeatureChange reverseFeatureChange, AbstractElementRefiner refiner) {
		if (newObjects.values().contains(refinedObject)) return true;
		EStructuralFeature feature = reverseFeatureChange.getFeature();
		if (feature.isMany()) return true; //for now, all lists can apply changes
		if (refinedObject.eClass().getEAllStructuralFeatures().contains(feature)){
			Object refinedFeatureValue = refinedObject.eGet(feature);
			Object oldAbstractFeatureValue = reverseFeatureChange.getValue();
			return isEquivalent(concreteComponent, feature, refinedFeatureValue, oldAbstractFeatureValue, refiner);
		}	
		return false;
	}
	
	/**
	 * returns true if the two value instances, fv1 and fv2, of feature, are equivalent
	 * (where equivalence depends on type)
	 * 
	 * for now lists always return true
	 * 
	 * @param feature
	 * @param fv1	refined value
	 * @param fv2	abstract value
	 * @return
	 */
	private boolean isEquivalent(EventBNamedCommentedComponentElement concreteComponent, EStructuralFeature feature, Object fv1, Object fv2, AbstractElementRefiner refiner){
		if (fv1==fv2) return true;
		if (feature.getEType().equals(EcorePackage.Literals.ESTRING)){
			return isStringEquivalent((String)fv1,(String)fv2);
		}else if (feature.getEType().equals(EcorePackage.Literals.EDATA_TYPE)){
			return fv1.equals(fv2);
//		}else  if (feature.getEType().equals(EcorePackage.Literals.EOBJECT)){
//			return fv1.equals(fv2);
		}else if (feature.isMany()){
			return true;
		}else if (fv1==null){
			return fv2==null;
		}else if (fv1 instanceof EObject && fv2 instanceof EObject){
			return fv1.equals(getEquivalentObjectInRefinement(
					concreteComponent, 
					(EObject)fv2, refiner));
		}else return false;
	}

	/**
	 * returns true iff s1 and s2 are equal after removal of whitespace or both null
	 * @param s1
	 * @param s2
	 * @return
	 */
	private boolean isStringEquivalent(String s1, String s2) {
		if (s1==null) return s2==null;
		if (s2==null) return false;
		String s1r = s1.replaceAll("\\s", "");
		String s2r = s2.replaceAll("\\s", "");
		return s1r.equals(s2r);
	}
	


	/**
	 * generates a string from the current time
	 * @return
	 */
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
