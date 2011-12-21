package ac.soton.eventb.emf.diagrams.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.edit.command.DeleteCommand;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.transaction.impl.EditingDomainManager;
import org.eventb.emf.core.Attribute;
import org.eventb.emf.core.AttributeType;
import org.eventb.emf.core.CoreFactory;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBObject;


public class Generator {

	private class GeneratorConfig{
		public String generatorID;
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
	private Map<EventBObject, EventBObject> trace = new HashMap<EventBObject, EventBObject>();	
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
	public Map<EventBObject,EventBObject> generate (TransactionalEditingDomain editingDomain, final EventBElement sourceElement){
		try {
			if (generatorConfig==null) {
				Activator.logError("no generator found for "+ sourceElement);
				return null;
			}
			if (sourceElement.eClass() != generatorConfig.rootSourceClass){
				Activator.logError("Incorrect generator called for "+ sourceElement);
				return null;
			}
			doGenerate(sourceElement);
		} catch (Exception e) {
			Activator.logError(e.getMessage(),e);
			return null;
		} 

		//Remove previously generated elements	
		Command deleteCommand = DeleteCommand.create(editingDomain, getGeneratedElements(sourceElement));
		if (deleteCommand.canExecute()){
			deleteCommand.execute();

			//place the newly generated elements in their correct parent features
			//(this is done in a separate post-generation phase and only if the deletion of old generated elements succeeds. 
			// This is so that we do not leave the model in an inconsistent state if the generation fails)
			placeGenerated();
			
			return trace;
			
		}else{
			Activator.logError("failed to delete previously generated elements");
			return null;
		}
	}

	/**
	 * finds all elements that have been generated with this generators generatorID
	 * @return 
	 */
		public ArrayList<EObject> getGeneratedElements(final EventBElement sourceElement) {
			EventBNamedCommentedComponentElement component= (EventBNamedCommentedComponentElement) sourceElement.getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
			EList<EObject> contents = component.getAllContained(CorePackage.eINSTANCE.getEventBElement(),false);
			ArrayList<EObject> remove = new ArrayList<EObject>();
			for(EObject eObject : contents){
				if (eObject instanceof EventBElement){
					Attribute generatedBy = ((EventBElement)eObject).getAttributes().get(GENERATOR_ID_KEY);
					if (generatedBy!= null && generatorConfig.generatorID.equals(generatedBy.getValue()) ){
						remove.add(eObject);
					}
				}
			}
			return remove;
		}

		
/**
 * puts the generated elements into the model
 */
	private void placeGenerated() {
		prepare();
		for (int pri=10; pri>=-10; pri--){
			if (priorities.containsKey(pri))
			for (GenerationDescriptor generationDescriptor : priorities.get(pri)){
				if (generationDescriptor.value instanceof EventBElement){
					EventBElement newChild = ((EventBElement)generationDescriptor.value);
					
					// set the generated property
					newChild.setLocalGenerated(true);
					// add an attribute with this generators ID
					Attribute genID =   CoreFactory.eINSTANCE.createAttribute();
					genID.setValue(generatorConfig.generatorID);
					genID.setType(AttributeType.STRING);
					newChild.getAttributes().put(GENERATOR_ID_KEY,genID);
					if (generationDescriptor.parent != null){
						Object featureValue = generationDescriptor.parent.eGet(generationDescriptor.feature);
						if (featureValue instanceof EObjectContainmentEList){
							try{
								@SuppressWarnings("unchecked")
								EObjectContainmentEList<EObject> list = (EObjectContainmentEList<EObject>) featureValue; 
								list.add(newChild);
							}catch (Exception e){
								Activator.logError("Failed to add generated element to feature", e);
							}
						}else{
							generationDescriptor.parent.eSet(generationDescriptor.feature, newChild);
						}
					}else{
						//currently not supported
					}
				}
			}
		}
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

	/**
	 * recursively traverse the model firing any appropriate rules that are enabled
	 * 
	 * @param sourceElement
	 * @throws CoreException
	 */
	private void doGenerate(final EventBElement sourceElement) throws CoreException {
		//this ensures that we do not generate from our own generated elements
		if (generatorConfig.generatorID.equals(sourceElement.getAttributes().get(GENERATOR_ID_KEY))) return;
		
		//try to fire all the rules listed for this kind of source element
		List<IRule> rules = generatorConfig.ruleMapping.get(sourceElement.eClass());
		if (rules == null) return;
		for (final IRule rule : rules){
			if (rule != null && rule.enabled(sourceElement, generatedElements)) {
				generatedElements.addAll(rule.fire(sourceElement));
			}
		}

		for (final EObject child : sourceElement.eContents()) {
			if (child instanceof EventBElement)
				doGenerate((EventBElement) child);
		}
	}

}
