package ac.soton.eventb.emf.diagrams.generator;

import org.eclipse.emf.ecore.EStructuralFeature;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Action;
import org.eventb.emf.core.machine.Invariant;
import org.eventb.emf.core.machine.MachineFactory;
import org.eventb.emf.core.machine.Variable;

public class Make {

	public static GenerationDescriptor descriptor(EventBElement parent, EStructuralFeature feature, Object value, int priority){
		return new GenerationDescriptor(parent,feature,value,Integer.valueOf(priority));
	}
	
	  public static Variable variable(String name, String comment) {
		Variable v =  MachineFactory.eINSTANCE.createVariable();
	    v.setName(name);
	    v.setLocalGenerated(true);
	    v.setComment(comment);
	    return v;  
	  }
	  
	public static Invariant invariant(String name, String predicate,String comment) {
		Invariant i =  MachineFactory.eINSTANCE.createInvariant();
	    i.setName(name);
	    i.setPredicate(predicate);
	    i.setLocalGenerated(true);
	    i.setComment(comment);
	    return i;  
	}

	public static Object action(String name, String expression, String comment) {
		Action a =  MachineFactory.eINSTANCE.createAction();
	    a.setName(name);
	    a.setAction(expression);
	    a.setLocalGenerated(true);
	    a.setComment(comment);
	    return a;  
	}

}
