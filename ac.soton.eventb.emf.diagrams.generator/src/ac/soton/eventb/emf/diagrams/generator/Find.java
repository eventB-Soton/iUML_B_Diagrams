package ac.soton.eventb.emf.diagrams.generator;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;
import org.eventb.emf.core.Project;

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

	/**
	 * find the containing Project for this 
	 * @param machine
	 * @return
	 * @throws IOException
	 */
	public static Project project(EventBElement eventBelement) throws IOException {
//		URI eventBelementUri = eventBelement.getURI();
//		URI projectUri = eventBelementUri.trimFragment().trimSegments(1);
//		ProjectResource projectResource = new ProjectResource();
//		projectResource.setURI(eventBelement.getURI());
//		projectResource.load(null);
//		for (EObject eObject : projectResource.getContents()){
//			if (eObject instanceof Project){
//				return (Project)eObject;
//			}
//		}
		return null;
	}

}
