package ac.soton.eventb.emf.diagrams.generator;

import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;

import ac.soton.eventb.emf.core.extension.coreextension.EventBLabeled;

public class Find {
	
	

	public static EventBNamed named(EList<? extends EventBNamed> collection, String name){
		for (EventBNamed element : collection){
			if (name.equals(element.getName())) return element;
		}
		return null;
	}

	public static Object generatedElement(List<GenerationDescriptor> generatedElements, EventBElement parent, EStructuralFeature feature, String identifier) {
		for (GenerationDescriptor generatedElement : generatedElements){
			if (generatedElement.parent == parent && generatedElement.feature== feature){
				if ((generatedElement.value instanceof EventBNamed && ((EventBNamed)generatedElement.value).getName().equals(identifier))
				|| (generatedElement.value instanceof EventBLabeled && ((EventBLabeled)generatedElement.value).getLabel().equals(identifier))) 
					return generatedElement.value;
			}
		}
		return null;
	}
}
