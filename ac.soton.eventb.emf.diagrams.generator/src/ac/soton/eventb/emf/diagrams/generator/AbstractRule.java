package ac.soton.eventb.emf.diagrams.generator;

import java.util.Collections;
import java.util.List;

import org.eclipse.emf.ecore.EReference;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.machine.MachinePackage;


/**
 * a simple abstract version of a rule that is always enabled and adds generated elements to 
 * the target Component. Clients may extend this rule to adopt this default behaviour.
 * 
 * @author cfs
 *
 */
public abstract class AbstractRule implements IRule {
	
	protected static final EReference variables = MachinePackage.Literals.MACHINE__VARIABLES;
	protected static final EReference invariants = MachinePackage.Literals.MACHINE__INVARIANTS;
	protected static final EReference events = MachinePackage.Literals.MACHINE__EVENTS;
	protected static final EReference parameters = MachinePackage.Literals.EVENT__PARAMETERS;
	protected static final EReference guards = MachinePackage.Literals.EVENT__GUARDS;
	protected static final EReference actions = MachinePackage.Literals.EVENT__ACTIONS;
	
	@Override
	public boolean enabled(EventBElement sourceElement) throws Exception  {
		return true;
	}

	@Override
	public List<GenerationDescriptor> fire(EventBElement sourceElement, List<GenerationDescriptor> generatedElements) throws Exception {
		assert(enabled(sourceElement));
		return Collections.emptyList();
	}
	
	@Override
	public boolean dependenciesOK(EventBElement sourceElement, final List<GenerationDescriptor> generatedElements) throws Exception  {
		return true;
	}

		
	@Override
	public EventBNamedCommentedComponentElement getTargetEventBComponent(EventBNamedCommentedComponentElement sourceComponent, EventBElement sourceElement) throws Exception  {
		return sourceComponent;
	}

}
