package ac.soton.eventb.emf.diagrams.navigator.refiner;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.core.IMachineRoot;
import org.eventb.core.ISeesContext;
import org.eventb.core.basis.EventBElement;
import org.eventb.core.basis.MachineRoot;
import org.rodinp.core.IAttributeType;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IInternalElementType;
import org.rodinp.core.IRefinementParticipant;
import org.rodinp.core.IRodinElement;
import org.rodinp.core.RodinCore;
import org.rodinp.core.RodinDBException;

/**
 * This refinement participant sets the generated attribute of a target element
 * whenever the corresponding source element has the generated attribute set to true
 * and the source element has a GENERATOR_ID attribute.
 * This is because the normal machine refinement does not preserve the generated attribute
 * and generators that use the GENERATOR_ID attribute are higher level refine-able models. (I.e.
 * if any changes are to be made in a refinement they must be done in the higher-level model,
 *  not in the generated elements). 
 *  
 *  This participant also deletes any sees clauses to Contexts which have our GENERTOR_ID attribute.
 *  This is because the normal refinement copies these into the refined machine and when the generator is next
 *  run it will delete their contents. Rodin will raise a warning about the missing sees clause which serves as a 
 *  reminder that the generator needs to be invoked in the new concrete machine
 * 
 * @author cfs
 *
 */
public class GeneratedCopier implements IRefinementParticipant {
	
	private static final IAttributeType GENERATOR_ID_ATTRIBUTETYPE = RodinCore.getAttributeType("org.eventb.emf.persistence.generator_ID");

	@Override
	public void process(IInternalElement targetRoot,
			IInternalElement sourceRoot, IProgressMonitor monitor)
			throws RodinDBException {	
		//The default Rodin machine refiner copies sees clauses into the concrete machine. This is a problem for generated contexts because
		//the generator deletes all the elements in the seen contexts when it is next invoked. Therefore we remove these Sees relationships 
		// now and put up with the Rodin warning. The Rodin warning means that the generator needs to be run for the new concrete extensions.
		filterGeneratedSees(targetRoot);
		//The default Rodin refiners do not propagate the generated attribute. Therefore we do this for all concrete elements that have our 
		//GeneratedBy Attribute.
		copyGenerated(sourceRoot, targetRoot, monitor);
	}

	/**
	 * Removes seesClauses of generated Contexts which have been copied into the refined Machine by the Rodin refiner.
	 * (Otherwise generator will delete their content on next generation).
	 * 
	 * @param targetRoot
	 * @throws RodinDBException
	 */
	private void filterGeneratedSees(IInternalElement targetRoot) throws RodinDBException {
		if (targetRoot.getElementType() !=  MachineRoot.ELEMENT_TYPE) return;
		ISeesContext[] seesClauses = ((IMachineRoot)targetRoot).getSeesClauses();
		for (ISeesContext seesContext : seesClauses){
			if (seesContext.getSeenContextRoot().hasAttribute(GENERATOR_ID_ATTRIBUTETYPE)){
				seesContext.delete(true, null);
			}
		}	
	}

	private void copyGenerated (IRodinElement sourceElement, IInternalElement targetRoot, IProgressMonitor monitor) throws RodinDBException {
		if (sourceElement instanceof IInternalElement){
			if (sourceElement instanceof EventBElement){
				EventBElement sourceEventBElement = (EventBElement)sourceElement;
				if (sourceEventBElement.isGenerated() && sourceEventBElement.hasAttribute(GENERATOR_ID_ATTRIBUTETYPE)){
					EventBElement targetEventBElement = findCorrespondingTarget(targetRoot, sourceEventBElement);
					if (targetEventBElement != null){
						targetEventBElement.setGenerated(true, monitor);
					}
				}
			}
			IRodinElement[] children = ((IInternalElement)sourceElement).getChildren();
			for (IRodinElement childElement : children){
				copyGenerated (childElement, targetRoot, monitor);
			}
		}
	}

	
	private EventBElement findCorrespondingTarget(IInternalElement targetRoot, EventBElement sourceEventBElement) throws RodinDBException {
		String sourceLabel = sourceEventBElement.hasLabel() ? sourceEventBElement.getLabel() : "";
		String sourceIdentifier = sourceEventBElement.hasIdentifierString() ? sourceEventBElement.getIdentifierString() : "";
		IInternalElementType<? extends IInternalElement> sourceType = sourceEventBElement.getElementType();
		IRodinElement[] targetElements = targetRoot.getChildren();
		for (IRodinElement targetElement : targetElements){
			if (targetElement instanceof EventBElement){
				EventBElement targetEventBElement = (EventBElement)targetElement;
				if (targetEventBElement.getElementType().equals(sourceType) && (
						(targetEventBElement.hasLabel() && targetEventBElement.getLabel().equals(sourceLabel)) ||
						(targetEventBElement.hasIdentifierString() && targetEventBElement.getIdentifierString().equals(sourceIdentifier))
						))
				{
					return targetEventBElement;
				}	
			}
		}
		
		return null;
	}
			
}
