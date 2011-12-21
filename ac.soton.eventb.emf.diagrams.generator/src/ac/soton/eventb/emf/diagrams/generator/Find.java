package ac.soton.eventb.emf.diagrams.generator;

import org.eclipse.emf.common.util.EList;
import org.eventb.emf.core.EventBNamed;

public class Find {

	public static EventBNamed named(EList<? extends EventBNamed> collection, String name){
		for (EventBNamed element : collection){
			if (name.equals(element.getName())) return element;
		}
		return null;
	}
}
