package ac.soton.eventb.emf.diagrams.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.EObjectResolvingEList;
import org.eclipse.emf.edit.command.DeleteCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.Attribute;
import org.eventb.emf.core.AttributeType;
import org.eventb.emf.core.CoreFactory;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;


public class Generator {

	private class GeneratorConfig{
		public String generatorID;
		@SuppressWarnings("unused")
		public EPackage sourcePackage;
		public EClass rootSourceClass;
		public Map<EClassifier, List<IRule>> ruleMapping;
		public GeneratorConfig(){
			ruleMapping = new HashMap<EClassifier, List<IRule>>();
		}
		public void addRule(EClassifier sourceClass,IRule rule){
			List<IRule> ruleList = ruleMapping.get(sourceClass);
			if (ruleList == null) ruleList = new ArrayList<IRule>();
			ruleList.add(rule);
			ruleMapping.put(sourceClass, ruleList);
		}
	}
	
	// Key for the generator ID atttribute
	private static final String GENERATOR_ID_KEY = "org.eventb.emf.persistence.generator_ID";

	// ID's for the extension point defining generators
	private static final String EXTPT_ID = "ac.soton.eventb.emf.diagrams.generator.rule";
	
	private static final String EXTPT_ROOTSOURCECLASS ="root_source_class";
	private static final String EXTPT_GENERATORID = "generatorID";
	private static final String EXTPT_SOURCEPACKAGE = "sourcePackage";
	
	private static final String EXTPT_RULE = "rule";
	private static final String EXTPT_RULECLASS = "rule_class";
	private static final String EXTPT_SOURCECLASS = "source_class";
//	private static final String EXTPT_TARGETFEATURE = "target_feature";
//	private static final String EXTPT_TARGETPRIORITY = "target_priority";

	//cached store of generator configurations that have been loaded from extension points so far
	private static Map<EClass,GeneratorConfig> generators = new HashMap<EClass, GeneratorConfig  >();
	
//	configuration data for this instance of a generator
	private GeneratorConfig generatorConfig;

	
	// VARIABLE DATA
	/**
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
 * Construct a generator.
 * This should be called from a command handler action.
 * 	
 * @param rootSourceClass	- the EClass of the root element that this is a generator for
 */
	
	public Generator(EClass rootSourceClass){ 
		
		if (generators.containsKey(rootSourceClass)){
			generatorConfig = generators.get(rootSourceClass);
			
		}else{
			generatorConfig = null;

			// populate generator configuration data from registered extensions
			for (final IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(EXTPT_ID).getExtensions()) {
				for (final IConfigurationElement generatorExtensionElement : extension.getConfigurationElements()) {
					EPackage sourcePackage = EPackage.Registry.INSTANCE.getEPackage(generatorExtensionElement.getAttribute(EXTPT_SOURCEPACKAGE));
					if (sourcePackage!= null &&
							rootSourceClass == sourcePackage.getEClassifier(generatorExtensionElement.getAttribute(EXTPT_ROOTSOURCECLASS))){
						generatorConfig = new GeneratorConfig();
						generatorConfig.sourcePackage = sourcePackage;
						generatorConfig.rootSourceClass = rootSourceClass;
						generatorConfig.generatorID = generatorExtensionElement.getAttribute(EXTPT_GENERATORID);
						for (final IConfigurationElement ruleExtensionElement : generatorExtensionElement.getChildren(EXTPT_RULE)) {
							try {
								final IRule rule = (IRule) ruleExtensionElement.createExecutableExtension(EXTPT_RULECLASS);
								EClassifier sourceClass = sourcePackage.getEClassifier(ruleExtensionElement.getAttribute(EXTPT_SOURCECLASS));
								if (sourceClass != null) generatorConfig.addRule(sourceClass, rule);
								
							} catch (final CoreException e) {
								Activator.logError(e.getMessage(),e);
							}
						}
					}
				}
			}
			//save config data in case another generator instance is needed for this EClass
			if (generatorConfig != null) generators.put(rootSourceClass,generatorConfig);
		}
	}

	
/**
 * generate - this should be called from a command handler action, passing the selected element
 * @param editingDomain 
 * 
 */
	public List<Resource> generate (TransactionalEditingDomain editingDomain, final EventBElement sourceElement){
		String sourceExtensionID;
		List<Resource> modifiedResources = new ArrayList<Resource>();
		try {
			
			//check we have a valid configuration for the generator
			if (generatorConfig==null) {
				Activator.logError("no generator found for "+ sourceElement);
				return null;
			}
			
			//check we have the correct generator configuration for the source element
			if (sourceElement.eClass() != generatorConfig.rootSourceClass){
				Activator.logError("Incorrect generator called for "+ sourceElement);
				return null;
			}
			
			//Obtain the extension ID from the source element
			sourceExtensionID = ((AbstractExtension)sourceElement).getExtensionId();
			assert(sourceExtensionID != null && sourceExtensionID.startsWith(generatorConfig.generatorID));
			
			//do the generation
			doGenerate(sourceElement);
			
			//create new EventB components
			modifiedResources.addAll(
					createNewComponents(editingDomain, sourceElement)
					);
			
			//Remove previously generated elements	
			List<EObject> previouslyGeneratedElements = getGeneratedElements(
					(EventBNamedCommentedComponentElement) sourceElement.getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT),
					sourceExtensionID);
			if (previouslyGeneratedElements.size()>0){
				Command deleteCommand = DeleteCommand.create(editingDomain, previouslyGeneratedElements);
				if (deleteCommand.canExecute()){
					deleteCommand.execute();
				}else{
					Activator.logError("failed to delete previously generated elements");
					return null;
				}
			}
			
		} catch (Exception e) {
			Activator.logError(e.getMessage(),e);
			return null;
		} 

		//place the newly generated elements in their correct parent features
		//(this is done in a separate post-generation phase and only if the deletion of old generated elements succeeds. 
		// This is so that we do not leave the model in an inconsistent state if the generation fails)
		try {
			modifiedResources.addAll(
					placeGenerated(editingDomain, sourceExtensionID)
					);
		} catch (Exception e) {
			Activator.logError("Failed to place generated elements", e);
			return null;
		}
		
		return modifiedResources;
			
	}
	
/**
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
	private Collection<? extends Resource> createNewComponents(TransactionalEditingDomain editingDomain, EventBElement sourceElement) {
		List<Resource> newResources = new ArrayList<Resource>();
		String projectName = sourceElement.getURI().trimFragment().trimSegments(1).lastSegment();
		URI projectUri = URI.createPlatformResourceURI(projectName, true);
		for (GenerationDescriptor generationDescriptor : generatedElements){
			if (generationDescriptor.feature == CorePackage.Literals.PROJECT__COMPONENTS &&
					generationDescriptor.value instanceof EventBNamedCommentedComponentElement){
					String fileName = ((EventBNamedCommentedComponentElement)generationDescriptor.value).getName();
					URI fileUri = projectUri.appendSegment(fileName).appendFileExtension("buc");
					//fileUri = projectUri.
					String fileString = fileUri.toString();
					Resource newResource = editingDomain.createResource(fileString); //projectUri.appendSegment(fileName).toFileString());
					//editingDomain.getResourceSet().
					newResource.getContents().add((EventBNamedCommentedComponentElement)generationDescriptor.value);
					newResources.add(newResource);
			}
		}
		return newResources;
	}


	/**
	 * finds all elements that have been generated with this generators generatorID
	 * @return 
	 */
		public ArrayList<EObject> getGeneratedElements(final EventBNamedCommentedComponentElement component, String sourceExtensionID) {
			EList<EObject> contents = component.getAllContained(CorePackage.eINSTANCE.getEventBElement(),false);
			contents.remove(null);
			contents.add(0,component);
			ArrayList<EObject> remove = new ArrayList<EObject>();
			for(EObject eObject : contents){
				if (wasGeneratedBy(eObject,sourceExtensionID)){
					remove.add(eObject);					
				}else{
					for (EReference referenceFeature : eObject.eClass().getEAllReferences()){
						Object referenceValue = eObject.eGet(referenceFeature, true);
						if (wasGeneratedBy(referenceValue,sourceExtensionID)){
							//FIXME: this may not be right and should be deferred until deletion time
							eObject.eSet(referenceFeature, null);
						}else{
							if (referenceValue instanceof EObjectResolvingEList){
								@SuppressWarnings("unchecked")
								EObjectResolvingEList<EObject> referenceList = (EObjectResolvingEList<EObject>)referenceValue;
								List<EObject> toBeRemoved = new ArrayList<EObject>();
								for (EObject ref : referenceList){
									if (wasGeneratedBy(ref,sourceExtensionID)){
										toBeRemoved.add(ref);
										//if (!remove.contains(ref)) remove.add(ref); can't remove a root of Resource (e.g. ContextRoot)
									}										
								}
								//FIXME: this should be deferred until deletion time
								referenceList.removeAll(toBeRemoved);
							}
						}
					}						
				}
			}
			return remove;
		}

			private static boolean wasGeneratedBy(Object object, String id){
				if (object instanceof EventBElement){
					Attribute generatedBy = ((EventBElement)object).getAttributes().get(GENERATOR_ID_KEY);
					if (generatedBy!= null && id.equals(generatedBy.getValue()) ){
						return true;
					}
				}
				return false;
			}
		
/**
 * puts the generated elements into the model
 * @param editingDomain 
 * @return 
 */
	private Collection<? extends Resource> placeGenerated(EditingDomain editingDomain, String generatedByID) throws Exception {
		prepare();
		List<Resource> modifiedResources = new ArrayList<Resource>();
		for (int pri=10; pri>=-10; pri--){
			if (priorities.containsKey(pri))
			for (GenerationDescriptor generationDescriptor : priorities.get(pri)){
				Resource resource = null;
				if (generationDescriptor.value instanceof EventBElement){
					EventBElement newChild = ((EventBElement)generationDescriptor.value);
					
					// set the generated property
					newChild.setLocalGenerated(true);
					
					// add an attribute with this generators ID
					Attribute genID =   CoreFactory.eINSTANCE.createAttribute();
					genID.setValue(generatedByID);
					genID.setType(AttributeType.STRING);
					newChild.getAttributes().put(GENERATOR_ID_KEY,genID);
					
					if (generationDescriptor.parent != null){
						Object featureValue = generationDescriptor.parent.eGet(generationDescriptor.feature);
						if (featureValue instanceof EObjectContainmentEList){	//containment collection
							@SuppressWarnings("unchecked")
							EObjectContainmentEList<EObject> list = (EObjectContainmentEList<EObject>) featureValue; 
							list.add(newChild);
						}else if (featureValue instanceof EObjectResolvingEList){	//list of references
							@SuppressWarnings("unchecked")
							EObjectResolvingEList<EObject> list = (EObjectResolvingEList<EObject>) featureValue; 
							list.add(newChild);
						}else {
							generationDescriptor.parent.eSet(generationDescriptor.feature, newChild);
						}
						//add to list of modifiedResources if not already there
						resource = generationDescriptor.parent.eResource();

					}else{
						//currently not supported
					}
				}
				if (resource!= null && !modifiedResources.contains(resource)){
					modifiedResources.add(resource);
				}
			}
		}
		return modifiedResources;
	}

	/**
	 * prepares the generated elements.
	 * This sets the local generated property and adds an attribute with this generators ID
	 * The mapping's priority, parent and feature are populated
	 */
	private void prepare() {
		for (GenerationDescriptor generationDescriptor : generatedElements){
				Integer pri = generationDescriptor.priority;
				List<GenerationDescriptor> objects = priorities.get(pri);
				if (objects ==null) objects = new ArrayList<GenerationDescriptor>();
				objects.add(generationDescriptor); //newChild);
				priorities.put(pri,objects);
		}
	}

	private Map<EventBElement, List<IRule>> deferredRules = new HashMap<EventBElement,List<IRule>>();
	private void defer(EventBElement sourceElement, IRule rule){
		List<IRule> rules = deferredRules.get(sourceElement);
		if (rules == null) rules = new ArrayList<IRule>();
		rules.add(rule);
		deferredRules.put(sourceElement, rules);
	}
	
	/**
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
						if (rule.dependenciesOK(sourceElement, generatedElements)){
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
			if (progress == false) throw new Exception("Generator Rules contain circlar dependencies");
		} 
	}
		
	private void traverseModel(final EventBElement sourceElement) throws Exception {
		
		//this ensures that we do not generate from our own generated elements
		if (generatorConfig.generatorID.equals(sourceElement.getAttributes().get(GENERATOR_ID_KEY))) return;
		
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
				if (rule.dependenciesOK(sourceElement, generatedElements)){
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
