/**
 * Copyright (c) 2011 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.navigator.action;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.ui.URIEditorInput;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.emf.workspace.AbstractEMFOperation;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eclipse.gmf.runtime.diagram.core.preferences.PreferencesHint;
import org.eclipse.gmf.runtime.diagram.core.services.ViewService;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.persistence.EMFRodinDB;

import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;
import ac.soton.eventb.emf.diagrams.navigator.provider.IDiagramProvider;

/**
 * Open diagram action.
 * Opens a diagram from its domain element using a registered diagram provider.
 * If diagram file does not exist, creates and initialises it.
 * 
 * @author vitaly
 *
 */
public class OpenDiagramAction extends Action implements ISelectionChangedListener {
	
	private ISelection selection;

	/**
	 * @param aSite site
	 * @param label action label
	 */
	public OpenDiagramAction(ICommonActionExtensionSite aSite, String label) {
		super(label);
		selection = aSite.getStructuredViewer().getSelection();
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		selection = event.getSelection();
	}

	@Override
	public void run() {
		Object element = ((IStructuredSelection) selection).getFirstElement();
		if (element instanceof IAdaptable) {
			EObject eobject = (EObject) ((IAdaptable) element).getAdapter(EObject.class);
			
			// find diagram provider
			Map<String, IDiagramProvider> registry = DiagramsNavigatorExtensionPlugin.getDefault().getDiagramProviderRegistry();
			String type = eobject.eClass().getName();
			IDiagramProvider provider = registry.get(type);
			if (provider == null)
				return;
			
			try {
				OpenDiagramCommand command = new OpenDiagramCommand(eobject, provider);
				if (command.canExecute())
					command.execute(new NullProgressMonitor(), null);
			} catch (ExecutionException e) {
				DiagramsNavigatorExtensionPlugin.getDefault().getLog().log(new Status(Status.ERROR, DiagramsNavigatorExtensionPlugin.PLUGIN_ID, "Failed opening an editor", e));
			}
		}
	}

	/**
	 * A command to open a diagram.
	 * 
	 * @author vitaly
	 *
	 */
	public class OpenDiagramCommand extends AbstractEMFOperation {

		private EObject element;
		private IDiagramProvider provider;

		/**
		 * Command constructor.
		 * 
		 * @param element diagram domain element
		 * @param provider diagram provider
		 */
		public OpenDiagramCommand(EObject element, IDiagramProvider provider) {
			super(TransactionalEditingDomain.Factory.INSTANCE.createEditingDomain(), "Open Diagram Command");
			this.element = element;
			this.provider = provider;
		}

		@Override
		protected IStatus doExecute(IProgressMonitor monitor, IAdaptable info)
				throws ExecutionException {
			try {			
				// get diagram filename
				String filename = provider.getDiagramFileName(element);
				
				// dealing with files
				if (element.eResource()==null) {
					ResourcesPlugin.getWorkspace().getRoot().refreshLocal(IResource.DEPTH_INFINITE, null);
					if (element.eIsProxy()){
						element = EMFRodinDB.INSTANCE.loadElement(((InternalEObject)element).eProxyURI());
					}
					if (element.eResource()==null)
						throw new ExecutionException("Can't open diagram - try refreshing workspace", null);
				}
				
				IFile domainFile = WorkspaceSynchronizer.getFile(element.eResource());
				IProject project = domainFile.getProject();
				IFile diagramFile = project.getFile(filename);
				URI diagramURI = URI.createPlatformResourceURI(diagramFile.getFullPath().toOSString(), true);
			
				// create and initialise a file, if it doesn't exist
				if (diagramFile.exists() == false) {
					Resource resource = getEditingDomain().getResourceSet().createResource(diagramURI);
					//TODO: initialise the diagram file not with selected element, but the root element
					initializeNewDiagram(resource);
				}
			
				Resource diagramRes = getEditingDomain().getResourceSet().getResource(diagramURI, true);
				Diagram diagram = getDiagramToOpen(diagramRes);
				if (diagram == null) {
					diagram = initializeNewDiagram(diagramRes);
				}
				URI uri = EcoreUtil.getURI(diagram);
				String editorName = uri.lastSegment() + '#'
						+ diagram.eResource().getContents().indexOf(diagram);
				IEditorInput editorInput = new URIEditorInput(uri, editorName);
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				page.openEditor(editorInput, getEditorID());
				return Status.OK_STATUS;
			} catch (Exception ex) {
				throw new ExecutionException("Can't open diagram", ex);
			}
		}
		

		/**
		 * Returns a diagram element to open.
		 * 
		 * @param resource a diagram resource
		 * @return
		 */
		private Diagram getDiagramToOpen(Resource resource) {
			Diagram diagram = null;
			EObject de1 = getDiagramDomainElement();
			String ref1;
			if (de1 instanceof EventBElement && (ref1 = ((EventBElement)de1).getReference())!=null ){
				for (EObject e : resource.getContents()){
					EObject de2 = ((Diagram) e).getElement();			
					if (e instanceof Diagram && de2 instanceof EventBElement &&
							(ref1.equals(((EventBElement)de2).getReference()))){
						diagram = (Diagram) e;
						break;
					}
				}				
			}
			return diagram;
		}

		/**
		 * Initialises new diagram.
		 * 
		 * @param resource a resource in which a diagram should be contained
		 * @return new diagram element
		 * @throws ExecutionException
		 */
		protected Diagram initializeNewDiagram(final Resource resource) throws ExecutionException {
			Diagram d = ViewService.createDiagram(getDiagramDomainElement(), getDiagramKind(), getPreferencesHint());
			if (d == null) {
				throw new ExecutionException("Can't create diagram of '" + getDiagramKind() + "' kind");
			}
			resource.getContents().add(d);
			try {
				new WorkspaceModifyOperation() {
					protected void execute(IProgressMonitor monitor)
							throws CoreException, InvocationTargetException,
							InterruptedException {
						try {
							if (resource.isLoaded() && !getEditingDomain().isReadOnly(resource)) {
								resource.save(getSaveOptions());
							}
						} catch (IOException ex) {
							throw new InvocationTargetException(ex, "Save operation failed");
						}
					}
				}.run(null);
			} catch (InvocationTargetException e) {
				throw new ExecutionException("Can't create diagram of '" + getDiagramKind() + "' kind", e);
			} catch (InterruptedException e) {
				throw new ExecutionException("Can't create diagram of '" + getDiagramKind() + "' kind", e);
			}
			return d;
		}

		/**
		 * Returns diagram domain element.
		 * 
		 * @return domain element
		 */
		protected EObject getDiagramDomainElement() {
			return element;
		}

		/**
		 * Returns preferences hint for diagram preferences.
		 * 
		 * @return hint
		 */
		protected PreferencesHint getPreferencesHint() {
			return provider.getPreferencesHint();
		}

		/**
		 * Returns diagram kind.
		 * 
		 * @return diagram kind
		 */
		protected String getDiagramKind() {
			return provider.getDiagramKind();
		}
		
		/**
		 * Returns editor id.
		 * @return id
		 */
		protected String getEditorID() {
			return provider.getEditorId();
		}
		
		protected Map<?, ?> getSaveOptions() {
			HashMap<String, Object> saveOptions = new HashMap<String, Object>();
			saveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8");
			saveOptions.put(Resource.OPTION_SAVE_ONLY_IF_CHANGED,
					Resource.OPTION_SAVE_ONLY_IF_CHANGED_MEMORY_BUFFER);
			return saveOptions;
		}

	}
}
