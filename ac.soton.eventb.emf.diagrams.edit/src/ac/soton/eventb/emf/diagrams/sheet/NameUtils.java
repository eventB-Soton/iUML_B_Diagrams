package ac.soton.eventb.emf.diagrams.sheet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;
import org.eclipse.emf.ecore.EObject;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;

import ac.soton.eventb.emf.core.extension.coreextension.EventBLabeled;

/**
 * @since 2.3
 */
public class NameUtils {

	/**
	 * Takes an List of model elements and returns a list of their names
	 *
	 * @param list of elements
	 * @return an array of names
	 */
	public static List<String> getNames(final List<?> list){
		return getNames(list,false,false,false);
	}


	/**
	 * Takes an List of elements and returns a list of their names or other identifier
	 * Optionally the meta-class name can be included
	 * and the owning construct and/or component can be included
	 *
	 * @param list of elements
	 * @param incClassNames - if true, include meta-class name (i.e. class name) as a prefix to the element name
	 * @param incOwners - if true, include direct owning element name as a prefix to the element name
	 * @param incComponent - if true, include owning component name as a prefix to the element name
	 * @return an array of names
	 */
	public static List<String> getNames(final List<?> list, final boolean incClassNames, final boolean incOwners, final boolean incComponent){
		ArrayList<String> names = new ArrayList<String>();
		if (list==null) return names;
		for (Object o: list){
			String prefix = "";
			if (incComponent && o instanceof EventBElement) {
				prefix= ((EventBNamed)((EventBElement)o).getContaining(CorePackage.eINSTANCE.getEventBNamedCommentedComponentElement())).getName()+" :: ";
			}
			if (incOwners && o instanceof EventBElement) {
				String ownerId = getIdentifier(((EventBElement)o).eContainer());
				if (ownerId!=null) {
					prefix= ownerId+" :: ";
				}
			}
			if (incClassNames && o instanceof EObject) {
				prefix=	prefix+ ((EObject) o).eClass().getName()+" - ";
			}
			String oid = getIdentifier(o);
			if (oid!=null) {
				names.add(prefix+oid);
			}else{
				names.add("<WARNING: unsupported item>");
			}
		}
		return names;
	}

	public static String getIdentifier(Object o) {
		if (o==null) return "<null>";
		else if (o instanceof Enumerator ) return ((Enumerator)o).getName();
		else if (o instanceof EventBNamed) return ((EventBNamed)o).getName();
		else if (o instanceof EventBLabeled) return ((EventBLabeled)o).getLabel();
		else if (o instanceof Boolean) return ((Boolean)o).toString();
		else return null;
	}
}
