package ac.soton.eventb.emf.diagrams.generator.impl;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

import ac.soton.eventb.emf.diagrams.generator.Activator;
import ac.soton.eventb.emf.diagrams.generator.IRule;
import ac.soton.eventb.emf.diagrams.generator.impl.GeneratorConfig;


public class GeneratorFactory {

	// The shared instance
	private static GeneratorFactory factory = null;
	
	
	//cached store of generator configurations that have been loaded from extension points
	private Map<EClassifier,GeneratorConfig> generatorConfigRegistry = new HashMap<EClassifier, GeneratorConfig  >();
	
	
	/*
	 * The constructor for the shared instance of factory,
	 * populates the registry of generator configurations from extensions point
	 */
	private GeneratorFactory() {
		// populate generator configuration data from registered extensions
		for (final IExtension extension : Platform.getExtensionRegistry().getExtensionPoint(Identifiers.EXTPT_RULE_ID).getExtensions()) {
			for (final IConfigurationElement generatorExtensionElement : extension.getConfigurationElements()) {
				EPackage rootSourcePackage = EPackage.Registry.INSTANCE.getEPackage(generatorExtensionElement.getAttribute(Identifiers.EXTPT_RULE_SOURCEPACKAGE));
				EClassifier rootSourceClass = rootSourcePackage.getEClassifier(generatorExtensionElement.getAttribute(Identifiers.EXTPT_RULE_ROOTSOURCECLASS));
				String generatorID = generatorExtensionElement.getAttribute(Identifiers.EXTPT_RULE_GENERATORID);
				if (rootSourcePackage!= null) {
					GeneratorConfig generatorConfig = new GeneratorConfig(generatorID, rootSourcePackage, rootSourceClass);
					for (final IConfigurationElement ruleExtensionElement : generatorExtensionElement.getChildren(Identifiers.EXTPT_RULE_RULE)) {
						try {
							EClassifier sourceClass = null;
							//see if a EPackage has been explicitly defined
							EPackage sourcePackage = EPackage.Registry.INSTANCE.getEPackage(ruleExtensionElement.getAttribute(Identifiers.EXTPT_RULE_SOURCEPACKAGE));
							if (sourcePackage != null) {
								sourceClass = sourcePackage.getEClassifier(ruleExtensionElement.getAttribute(Identifiers.EXTPT_RULE_SOURCECLASS));
							}else{
								//no explicit EPackage so try the rootSourcePackage of the generator
								sourcePackage = rootSourcePackage;
								sourceClass = sourcePackage.getEClassifier(ruleExtensionElement.getAttribute(Identifiers.EXTPT_RULE_SOURCECLASS));
								//if not in rootSourcePackage, try its subPackages
								if(sourceClass == null){
									for (EPackage subPackage  : sourcePackage.getESubpackages()){
										sourceClass = subPackage.getEClassifier(ruleExtensionElement.getAttribute(Identifiers.EXTPT_RULE_SOURCECLASS));
										if (sourceClass != null) break;
									}
								}
							}
							
							// if we found the class, add the rule
							if (sourceClass != null) {
								final IRule rule = (IRule) ruleExtensionElement.createExecutableExtension(Identifiers.EXTPT_RULE_RULECLASS);									
								generatorConfig.addRule(sourceClass, rule);
							}
							
						} catch (final CoreException e) {
							Activator.logError(e.getMessage(),e);
							generatorConfig = null;
							break;
						}
					}
					//save config data in case another generator instance is needed for this EClass
					if (generatorConfig != null) generatorConfigRegistry.put(rootSourceClass,generatorConfig);
				}
			}
		}
	}
	


	public static GeneratorFactory getFactory(){
		if (factory == null){
			factory = new GeneratorFactory();
		}
		return factory;
	}



	public boolean canGenerate(EClassifier rootSourceClass){
		return generatorConfigRegistry.containsKey(rootSourceClass);
	}
	
	/**
	 * Construct a generator.
	 * This should be called from a command handler action.
	 * 	
	 * @param rootSourceClass	- the EClass of the root element that this is a generator for
	 */
		
	public Generator createGenerator(EClass rootSourceClass){ 	
		if (generatorConfigRegistry.containsKey(rootSourceClass)){
			return new Generator(generatorConfigRegistry.get(rootSourceClass));
		}else{
			return null;
		}
	}
	
	
}
