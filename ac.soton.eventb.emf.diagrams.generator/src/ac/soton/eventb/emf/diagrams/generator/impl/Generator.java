/**
 * Copyright (c) 2012 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.generator.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.Attribute;
import org.eventb.emf.core.AttributeType;
import org.eventb.emf.core.CoreFactory;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBNamedCommentedElement;
import org.eventb.emf.core.EventBNamedCommentedPredicateElement;
import org.eventb.emf.core.EventBObject;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.machine.Event;
import org.eventb.emf.persistence.AttributeIdentifiers;

import ac.soton.eventb.emf.diagrams.generator.Activator;
import ac.soton.eventb.emf.diagrams.generator.GenerationDescriptor;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.utils.Make;

/**
 * A generic Generator which is configured from rule classes which have been declared in an extension point.
 * The rules are fired (if enabled) and return a collection of descriptors for adding elements to the model
 * or adding references within the model.
 * Rules may be deferred until later if they depend on other rules which have not yet fired.
 * Descriptors may be prioritised to influence the order of elements in collections. 
 * 
 * @author cfs
 *
 */
public class Generator {
	
	//	configuration data for this instance of a generator
	private GeneratorConfig generatorConfig;

	private Map<String, Integer> extensionOrder = new HashMap<String,Integer>();
	
	// VARIABLE DATA
	/*
	 * These mappings are populated from the generation descriptors prior to updating the model
	 * priorities maps from a range of priority numbers (10 down to -10) to a list of new child elements for each priority
	 * parents maps each new element to the parent element that should own it
	 * features maps each new element to the feature of the parent in which it should be contained
	 * 
	 */
	private Map<Integer,List<GenerationDescriptor>> priorities = new HashMap<Integer,List<GenerationDescriptor>>();
	//private Map<EventBObject, EventBObject> trace = new HashMap<EventBObject, EventBObject>();	
	private List<GenerationDescriptor> generatedElements = new ArrayList<GenerationDescriptor>();
	
	
/**
 * Construct a generator. This should only be called from the GeneratorFactory.
 * 	
 * @param generatorConfig	- the configuration for the generator
 */
	
	public Generator(GeneratorConfig generatorConfig){ 	
			this.generatorConfig = generatorConfig;
	}

	
/**
 * generate - this should be called from a command handler action, passing the selected element
 * @param editingDomain 
 * @param sourceElement 
 */
	public List<Resource> generate (TransactionalEditingDomain editingDomain, final EventBElement sourceElement){
		String generatedByID;
		List<Resource> modifiedResources = new ArrayList<Resource>();
		try {
			
			//check we have a valid configuration for the generator
			if (generatorConfig==null) {
				Activator.logError(Messages.GENERATOR_MSG_01(sourceElement));
				return null;
			}
			
			//check we have the correct generator configuration for the source element
			if (sourceElement.eClass() != generatorConfig.rootSourceClass){
				Activator.logError(Messages.GENERATOR_MSG_02(sourceElement));
				return null;
			}
			
			//Obtain the extension ID from the source element
			generatedByID = Make.generatedById(((AbstractExtension)sourceElement));
			assert(generatedByID != null && generatedByID.startsWith(generatorConfig.generatorID)) : "source elements generator ID and generator config do not match";
			
			//set up map of extensions ids and their positions
			EventBObject component = sourceElement.getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
			int i=0;
			for (EObject ae : component.getAllContained(CorePackage.Literals.ABSTRACT_EXTENSION, true)){
				if (ae !=null) {
					String id = ((AbstractExtension)ae).getExtensionId();
					extensionOrder.put(id, i++);
				}
			}
			
			//do the generation
			doGenerate(sourceElement);
			
			//verifyDescriptors;
			for (GenerationDescriptor generationDescriptor : generatedElements){
				if (generationDescriptor.feature!=null &&
						!generationDescriptor.feature.getEType().isInstance(generationDescriptor.value)){
					Activator.logError(Messages.GENERATOR_MSG_21(generationDescriptor.value,generationDescriptor.feature));
					return null;
				}
				if (generationDescriptor.parent!=null &&
					!generationDescriptor.parent.eClass().getEAllStructuralFeatures().contains(generationDescriptor.feature)){
					Activator.logError(Messages.GENERATOR_MSG_22(generationDescriptor.parent, generationDescriptor.feature));
					return null;
				}
			}

			//create new EventB components
			modifiedResources.addAll(createNewComponents(editingDomain, sourceElement, generatedByID));

//			DeleteGeneratedCommand deleteGeneratedCommand = new DeleteGeneratedCommand(editingDomain, sourceElement);
//			if (deleteGeneratedCommand.canExecute()){
//				deleteGeneratedCommand.execute(null, null);
//			}else{
//				Activator.logError(Messages.GENERATOR_MSG_03);
//				return null;
//			}
			GeneratedRemover genRemover = new GeneratedRemover(editingDomain, sourceElement, generatedByID);
			modifiedResources.addAll(genRemover.removeGenerated());
			
		} catch (Exception e) {
			Activator.logError(e.getMessage(),e);
			return null;
		} 

		//place the newly generated elements in their correct parent features
		//(this is done in a separate post-generation phase and only if the deletion of old generated elements succeeds. 
		// This is so that we do not leave the model in an inconsistent state if the generation fails)
		try {
			modifiedResources.addAll(
					placeGenerated(editingDomain, generatedByID)
					);
			removeComponents(editingDomain, sourceElement);
		} catch (Exception e) {
			Activator.logError(Messages.GENERATOR_MSG_04, e);
			return null;
		}
		
		modifiedResources.add(sourceElement.eContainer().eResource());
		return modifiedResources;	
	}
	
	/**
	 * Removes Event-B components according to the descriptors
	 * 
	 * 
	 * @param editingDomain
	 * @param sourceElement
	 * @throws IOException 
	 */
	private void removeComponents(TransactionalEditingDomain editingDomain, EventBElement sourceElement) throws IOException{
		String projectName = EcoreUtil.getURI(sourceElement).trimFragment().trimSegments(1).lastSegment();
		URI projectUri = URI.createPlatformResourceURI(projectName, true);
		for (GenerationDescriptor generationDescriptor : generatedElements){
			if (generationDescriptor.feature == CorePackage.Literals.PROJECT__COMPONENTS &&
					generationDescriptor.value instanceof EventBNamedCommentedComponentElement){
				String fileName = ((EventBNamedCommentedComponentElement)generationDescriptor.value).getName();
				String ext = generationDescriptor.value instanceof Context? "buc" :  "bum";
				URI fileUri = projectUri.appendSegment(fileName).appendFileExtension(ext); //$NON-NLS-1$
				if(generationDescriptor.remove == true){	
					Resource oldResource = editingDomain.getResourceSet().getResource(fileUri, false);
					if(oldResource != null) {
						oldResource.delete(Collections.EMPTY_MAP);
					}	
				}
			}
		}
	}

/*
 * If any generated elements are a new EventB component (e.g. machine, context) this creates a new resource
 * for them in the editing domains resource set and attaches the new element as the content of the resource.
 * Note that we do not save the resource yet in case the generation process does not complete. 
 * 
 * N.B. CURRENTLY ALL RESOURCES ARE ASSUMED TO BE WITHIN THE SAME PROJECT AS THE SOURCE ELEMENT. 
 * (I.E. CURRENTLY WE DO NOT USE generationDecriptor.parent WHICH CAN BE LEFT NULL AS LONG AS 
 * generationDecriptor.feature IS SET TO CorePackage.Literals.PROJECT__COMPONENTS)
 * 
 * @param editingDomain
 * @param sourceElement
 * @return list of new Resources
 */
	private Collection<? extends Resource> createNewComponents(TransactionalEditingDomain editingDomain, EventBElement sourceElement, String generatedByID) throws IOException {
		List<Resource> newResources = new ArrayList<Resource>();
		String projectName = EcoreUtil.getURI(sourceElement).segment(1);
		URI projectUri = URI.createPlatformResourceURI(projectName, true);
		for (GenerationDescriptor generationDescriptor : generatedElements){
			if (generationDescriptor.remove == false && 
				generationDescriptor.feature == CorePackage.Literals.PROJECT__COMPONENTS &&
				generationDescriptor.value instanceof EventBNamedCommentedComponentElement){
					String fileName = ((EventBNamed)generationDescriptor.value).getName();
					String ext = generationDescriptor.value instanceof Context? "buc" :  "bum";
					URI fileUri = projectUri.appendSegment(fileName).appendFileExtension(ext); //$NON-NLS-1$
					setGeneratedBy(generatedByID, 0, generationDescriptor.editable, (EventBElement)generationDescriptor.value);
					Resource newResource = editingDomain.createResource(fileUri.toString());
					newResource.getContents().add((EObject)generationDescriptor.value);
					newResources.add(newResource);
			}
		}
		return newResources;
	}

		
/*
 * puts the generated elements into the model
 * @param editingDomain 
 * @return modified resources
 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<? extends Resource> placeGenerated(EditingDomain editingDomain, String generatedByID) throws Exception {
		//arrange the generation descriptors into priority order
		for (GenerationDescriptor generationDescriptor : generatedElements){
			Integer pri = generationDescriptor.priority;
			List<GenerationDescriptor> objects = priorities.get(pri);
			if (objects ==null) objects = new ArrayList<GenerationDescriptor>();
			objects.add(generationDescriptor); //newChild);
			priorities.put(pri,objects);
		}
		//process the prioritised mappings of generation descriptors
		List<Resource> modifiedResources = new ArrayList<Resource>();
		Map<EList,Integer> positions = new HashMap<EList,Integer>();
		for (int pri=10; pri>=-10; pri--){
			positions.clear();
			if (priorities.containsKey(pri)){
				for (GenerationDescriptor generationDescriptor : priorities.get(pri)){
					if (generationDescriptor.remove == false && filter(generationDescriptor)) continue;								
					Resource resource = null;
					Object value = generationDescriptor.value;
					if (generationDescriptor.parent != null && 
						generationDescriptor.parent.eClass().getEAllStructuralFeatures().contains(generationDescriptor.feature) &&
						generationDescriptor.feature.getEType().isInstance(value)){
						
						Object featureValue = generationDescriptor.parent.eGet(generationDescriptor.feature);
	
						if (featureValue instanceof EList){	
							if(generationDescriptor.remove == false){
								if (generationDescriptor.value instanceof EventBElement){						
									EventBElement newChild = ((EventBElement)generationDescriptor.value);					
									setGeneratedBy(generatedByID, pri, generationDescriptor.editable, newChild);
								}
								//add the new value to the list at the correct index - i.e. after any higher priority elements and
								//after stuff generated by earlier extensions which has the same priority
								int pos = 0;
								for (int i=0; i<((EList)featureValue).size(); i++){
									Object v = ((EList)featureValue).get(i);
									if(v instanceof EventBElement){
										Attribute at = ((EventBElement)v).getAttributes().get(AttributeIdentifiers.GENERATOR_ID_KEY);
										String gb = (String) (at==null? null : at.getValue());
										Integer od = extensionOrder.get(gb);
										if (od==null) od = extensionOrder.size(); // not an extension => user entered stuff comes last
										at = ((EventBElement)v).getAttributes().get(AttributeIdentifiers.PRIORITY_KEY);
										Integer pr = (Integer) (at==null? null : at.getValue());
										if (pr==null) pr = 0; // no priority => user stuff at priority 0
										//priority order = highest 1..10,0,-1..-10
										if ((pr>0 && pr < pri) || (pr < 1 && pr > pri) || (pr==pri && od<=extensionOrder.get(generatedByID))){
											pos = i+1;
										};
										
									}
								}
								((EList)featureValue).add(pos, generationDescriptor.value);
								
								
							}
							else{
								ArrayList<Object> toRemove = new ArrayList<Object>();
								for(Object obj : (EList)featureValue){
									if(match(obj, generationDescriptor.value))
										toRemove.add(obj);
								}
								((EList)featureValue).removeAll(toRemove);
							}
						}else {
							if(generationDescriptor.remove == false){
								//FIXME: this should be analysed more
								generationDescriptor.parent.eSet(generationDescriptor.feature, generationDescriptor.value);
							}
							else
								if  (generationDescriptor.feature.isUnsettable())
									generationDescriptor.parent.eUnset(generationDescriptor.feature);
								else
									generationDescriptor.parent.eSet(generationDescriptor.feature, generationDescriptor.feature.getDefaultValue());
						}
						
						//add to list of modifiedResources if not already there
						resource = generationDescriptor.parent.eResource();
					}else{
						//Error messages are generated elsewhere - should not get here.
					}
					if (resource!= null && !modifiedResources.contains(resource)){
						modifiedResources.add(resource);
					}
				}
			}
		}
		return modifiedResources;
	}


/*
 * adds attributes to record:
 * a) the id of the extension that generated this element
 * b) the priority of this element for ordering
 * c) the fact that this element is generated
 * 
 * @param generatedByID
 * @param priority
 * @param element
 */
private void setGeneratedBy(String generatedByID, Integer priority, boolean editable, EventBElement element) {
	// set the generated property 
	if (editable)
		element.setLocalGenerated(false);
	else
		element.setLocalGenerated(true);
	// add an attribute with this generators ID
	Attribute genID =   CoreFactory.eINSTANCE.createAttribute();
	genID.setValue(generatedByID);
	genID.setType(AttributeType.STRING);
	element.getAttributes().put(AttributeIdentifiers.GENERATOR_ID_KEY,genID);
	// add an attribute with the priority for ordering
	Attribute pri =   CoreFactory.eINSTANCE.createAttribute();
	pri.setValue(priority);
	pri.setType(AttributeType.INTEGER);
	element.getAttributes().put(AttributeIdentifiers.PRIORITY_KEY,pri);
}

	/*
	 * Filters out any generationDescriptors that should not be acted upon
	 * This may be because a child is already visible via extension of the refined parent
	 *
	 */
	private boolean filter(GenerationDescriptor generationDescriptor) {
		
		//filter any new elements that are already there 	
		if (generationDescriptor.parent==null) return false;
		Object featureValue = generationDescriptor.parent.eGet(generationDescriptor.feature);
		if (featureValue instanceof EList){
			EList<?> list = (EList<?>)featureValue;
			for (Object el : list){
				if (match(el,generationDescriptor.value)) return true;
			}
		}
		
		// filter any new values which are already present by event extension
		if (generationDescriptor.parent instanceof Event){
			for (Object el : getExtendedValues((Event)generationDescriptor.parent,generationDescriptor.feature)){
				if (match(el,generationDescriptor.value)) return true;
			}
		}

		return false;
	}


	/*
	 * transitively get all the elements which are present by event extension
	 */
	@SuppressWarnings("unchecked")
	private List<Object> getExtendedValues(Event event, EStructuralFeature feature) {
		if (!(event.isExtended()) || event.getRefines().isEmpty()){
			return new ArrayList<Object>();
		}else{
			Event refinedEvent = event.getRefines().get(0);
			List<Object> extended = getExtendedValues(refinedEvent,feature);

			Object refinedFeatureValue = refinedEvent.eGet(feature);
			if (refinedFeatureValue instanceof List){
				extended.addAll((List<Object>)refinedEvent.eGet(feature));
			}
			return extended;
		}
	}

	/*
	 * test whether two elements should be considered to be the same in event B terms
	 */
	private boolean match(Object el1, Object el2) {
		if (el1.getClass()!=el2.getClass()) return false;
		if (el1 instanceof EventBNamedCommentedPredicateElement){	
			return stringEquivalent(
					((EventBNamedCommentedPredicateElement)el1).getPredicate(),
					((EventBNamedCommentedPredicateElement)el2).getPredicate()
					);
		} else if (el1 instanceof EventBNamedCommentedElement){
			String s1 = ((EventBNamedCommentedElement)el1).getName();
			String s2 = ((EventBNamedCommentedElement)el2).getName();
			return (s1 != null && s1.equals(s2));
		} else if(el1 instanceof String && el2 instanceof String) {
			return (el1 != null && el1.equals(el2));
		} else return false;
	}

	private boolean stringEquivalent(String s1, String s2) {
		if (s1==null) return s2==null;
		if (s2==null) return false;
		String s1r = s1.replaceAll("\\s", "");
		String s2r = s2.replaceAll("\\s", "");
		return s1r.equals(s2r);
	}

	/*
	 * a record of rules that have been deferred
	 */
	private Map<EventBElement, List<IRule>> deferredRules = new HashMap<EventBElement,List<IRule>>();
	private void defer(EventBElement sourceElement, IRule rule){
		List<IRule> rules = deferredRules.get(sourceElement);
		if (rules == null) rules = new ArrayList<IRule>();
		rules.add(rule);
		deferredRules.put(sourceElement, rules);
	}
	
	/*
	 * The generation is done in two stage:
	 * 1) traverse the model firing any appropriate rules that are enabled. This may result in deferred
	 *    rules that are enabled but cannot be fired due to dependencies on other rules.
	 * 2) repeatedly iterate through the deferred rules firing any that can fire until none are left or
	 *    if no progress is made raise an exception.
	 * 
	 * @param sourceElement
	 * @throws Exception - for any exception, abort
	 */
	private void doGenerate(final EventBElement rootSourceElement) throws Exception {
		boolean late = false;
		// stage 1 - traverse the model firing appropriate enabled rules
		traverseModel(rootSourceElement);
		
		// stage 2 - deal with any deferred rules
		while (deferredRules.size() >0 ){
			boolean progress = false;
			List<EventBElement> empties = new ArrayList<EventBElement>();
			for (EventBElement sourceElement : deferredRules.keySet()){
				List<IRule> firedRules = new ArrayList<IRule>();
				for (IRule rule : deferredRules.get(sourceElement)){
					if (rule != null && rule.enabled(sourceElement)) {
						if (late==rule.fireLate() && rule.dependenciesOK(sourceElement, generatedElements)){
							generatedElements.addAll(rule.fire(sourceElement, generatedElements));
							firedRules.add(rule);
						}
					}
				}
				if (firedRules.size()>0) {
					progress = true;
					deferredRules.get(sourceElement).removeAll(firedRules);
				}
				if (deferredRules.get(sourceElement).size()==0){
					empties.add(sourceElement);
				}
			}
			
			deferredRules.keySet().removeAll(empties);
			if (progress == false) {
				if (late){ //o-oh, no progress when already doing the late rules 
					throw new Exception(Messages.GENERATOR_MSG_00);
				}
				late = true; //enable the late rules
			}
		} 
	}
	
	/*
	 * recursive routine to traverse model firing appropriate rules
	 */
	private void traverseModel(final EventBElement sourceElement) throws Exception {
		
		//this ensures that we do not generate from our own generated elements
		if (generatorConfig.generatorID.equals(sourceElement.getAttributes().get(AttributeIdentifiers.GENERATOR_ID_KEY))) return;
		
		//try to fire all the rules listed for this kind of source element
		List<IRule> rules = new ArrayList<IRule>();
		List<EClass> types = new ArrayList<EClass>();
		types.addAll(sourceElement.eClass().getEAllSuperTypes());
		types.add(sourceElement.eClass());
		for (EClass type : types ){
			if (generatorConfig.ruleMapping.get(type)!=null)
				rules.addAll(generatorConfig.ruleMapping.get(type));
		}
		for (final IRule rule : rules){
			if (rule != null && rule.enabled(sourceElement)) {
				if (!rule.fireLate() && rule.dependenciesOK(sourceElement, generatedElements)){
					generatedElements.addAll(rule.fire(sourceElement, generatedElements));
				}else{
					defer(sourceElement,rule);
				}
			}
		}

		for (final EObject child : sourceElement.eContents()) {
			if (child instanceof EventBElement)
				traverseModel((EventBElement) child);
		}
	}
	
	

}
