package ac.soton.eventb.emf.diagrams.navigator.handler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.persistence.EMFRodinDB;
import org.eventb.emf.persistence.SaveResourcesCommand;

import ac.soton.eventb.emf.core.extension.navigator.refiner.AbstractElementRefiner;
import ac.soton.eventb.emf.core.extension.navigator.refiner.ElementRefinerRegistry;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;

/**
 * @since 3.2
 */
public class RefineDiagramElementHandler extends AbstractHandler implements IHandler {

	private final IProgressMonitor monitor = new NullProgressMonitor();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			final EventBElement eventBElement = getDiagramElement(element);
			//final Resource resource = getRefinedResource(eventBElement.eResource(), eventBElement.eResource().getURI().lastSegment()+"0");
			URI concreteResourceUri = getRefinedResource(eventBElement.eResource(), eventBElement.eResource().getURI().lastSegment()+"0");
			TransactionalEditingDomain editingDomain = TransactionalEditingDomain.Factory.INSTANCE.createEditingDomain(); //TransactionUtil.getEditingDomain(resource);
			//final Resource resource = editingDomain.getResourceSet().createResource(uri);
			final Resource resource = editingDomain.createResource(concreteResourceUri.toString());
//			try {
//				resource.save(Collections.emptyMap());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			//editingDomain.isReadOnly(resource)

			AbstractElementRefiner refiner = ElementRefinerRegistry.getRegistry().getRefiner(eventBElement);
			if (refiner!=null) {
				Map<EObject,EObject> copier = refiner.refine(eventBElement, concreteResourceUri);
						//cloneAsAlternativeRefinement(eventBElement, null, preComponentRes.getURI());
				EObject refinedElement = copier.get(eventBElement);

//			//resource.getContents().add(refinedElement);
//			if (editingDomain != null) {
//				//editingDomain.createCommand(commandClass, commandParameter)
//				// command to delete the diagram element from the model
//				Command refineDiagramCommand =  new RecordingCommand(editingDomain, "Refine Diagram Command") {
//					protected void doExecute() {
						resource.eSetDeliver(false);
						resource.getContents().add(0,refinedElement);
						resource.eSetDeliver(true);
//					}
//				};
//				if (refineDiagramCommand.canExecute()){
//					//refine the diagram model element
//					refineDiagramCommand.execute();
					resource.setModified(true); 
					// save all resources that have been modified
					SaveResourcesCommand saveCommand = new SaveResourcesCommand(editingDomain);
					if (saveCommand.canExecute()){
							saveCommand.execute(monitor, null);
					}
				}else{
					DiagramsNavigatorExtensionPlugin.logError("Failed to refine diagram - aborted refine of : "+eventBElement);
				}
			}
//		}
		return null;
	}

	/**
	 * if element is adaptable, adapts the element to an EObject and then if necessary loads it
	 * and returns it as an EventBElement
	 * @param element
	 * @return EventBElement (loaded)
	 */
		private EventBElement getDiagramElement(Object element) {
			if (element instanceof IAdaptable) {
				EObject eobject = (EObject) ((IAdaptable) element).getAdapter(EObject.class);
				if (eobject.eIsProxy()) {
					EMFRodinDB emfrdb = new EMFRodinDB();
					eobject = emfrdb.loadElement(((InternalEObject)eobject).eProxyURI());
				}
				if (eobject instanceof EventBElement) {
					return (EventBElement)eobject;
				}
			}
			return null;
		}
		
		private URI getRefinedResource(Resource resource, String newName) {
			URI newUri = resource.getURI();
			String fileExtension = newUri.fileExtension();
			newUri = newUri.trimSegments(1).appendSegment(newName).appendFileExtension(fileExtension);
			return newUri; //resource.getResourceSet().createResource(newUri);
		}
}
