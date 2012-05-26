package ac.soton.eventb.emf.diagrams.generator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;

import ac.soton.eventb.emf.diagrams.generator.IRule;

public class GeneratorConfig{
	public String generatorID;
	public EPackage rootSourcePackage;
	public EClassifier rootSourceClass;
	public Map<EClassifier, List<IRule>> ruleMapping;
	
	public GeneratorConfig(String generatorID, EPackage rootSourcePackage, EClassifier rootSourceClass){
		this.generatorID = generatorID;
		this.rootSourcePackage = rootSourcePackage;
		this.rootSourceClass = rootSourceClass;
		ruleMapping = new HashMap<EClassifier, List<IRule>>();
	}
	
	public void addRule(EClassifier sourceClass,IRule rule){
		List<IRule> ruleList = ruleMapping.get(sourceClass);
		if (ruleList == null) ruleList = new ArrayList<IRule>();
		ruleList.add(rule);
		ruleMapping.put(sourceClass, ruleList);
	}
	
}
