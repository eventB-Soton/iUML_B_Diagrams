/**
 * Copyright (c) 2020-2020 - University of Southampton.
 * All rights reserved. This program and the accompanying materials  are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this 
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * $Id$
 */
package ac.soton.eventb.emf.diagrams.navigator.wizards;


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.ISetSelectionTarget;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.context.ContextFactory;
import org.eventb.emf.core.context.ContextPackage;
import org.eventb.emf.core.impl.EventBElementImpl;
import org.eventb.emf.core.machine.MachineFactory;
import org.eventb.emf.core.machine.MachinePackage;

import ac.soton.eventb.emf.diagrams.DiagramsFactory;
import ac.soton.eventb.emf.diagrams.DiagramsPackage;
import ac.soton.eventb.emf.diagrams.UMLB;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;
import ac.soton.eventb.emf.diagrams.provider.DiagramsEditPlugin;


/**
 * This is a simple wizard for creating a new umlb model file.
 * (Based on the EMF generated new wizard)
 * 
 * @author cfsnook
 */
public class NewUMLBWizard extends Wizard implements INewWizard {

	/**
	 * The file extension for umlb files.
	 */
	public static final String UMLB_FILE_EXTENSION = "umlb";

	/**
	 * This is the file creation page.
	 */
	protected DiagramsModelWizardNewFileCreationPage newFileCreationPage;

	/**
	 * This is the attribute setting page.
	 */
	protected DiagramsModelWizardElaboratedComponentPage elaboratedComponentPage;

	/**
	 * Remember the selection during initialisation for populating the default container.
	 */
	protected IStructuredSelection selection;

	/**
	 * Remember the workbench during initialisation.
	 */
	protected IWorkbench workbench;

	/**
	 * Caches the names of the Event-B components that can be elaborated.
	 */
	protected List<String> componentNames;
	
	/**
	 * Caches the names of the UMLB models that can be elaborated.
	 */
	protected List<String> umlbNames;

	/**
	 * This just records the information.
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.workbench = workbench;
		this.selection = selection;
		setWindowTitle("New");
		setDefaultPageImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
				DiagramsNavigatorExtensionPlugin.PLUGIN_ID, 
				"icons/UMLB.gif"));
	}

	/**
	 * Returns the names of the components that can be elaborated.
	 * @throws CoreException 
	 */
	protected Collection<String> getComponentNames() throws CoreException {
		if (componentNames == null) {
			componentNames = new ArrayList<String>();
			IProject prj = getModelFile().getProject();
			if (prj!=null && prj.exists()) {
				IResource[] members = prj.members();
				for (IResource member : members) {
					String ext = member.getFileExtension();
					if ("bum".equals(ext) || "buc".equals(ext)) {
						componentNames.add(member.getName());
					}
				}
				
			}
		}
		return componentNames;
	}

	/**
	 * Returns the names of the umlb models that can be refined.
	 * @throws CoreException 
	 */
	protected Collection<String> getUmlbNames() throws CoreException {
		if (umlbNames == null) {
			umlbNames = new ArrayList<String>();
			IProject prj = getModelFile().getProject();
			if (prj!=null && prj.exists()) {
				IResource[] members = prj.members();
				for (IResource member : members) {
					String ext = member.getFileExtension();
					if (UMLB_FILE_EXTENSION.equals(ext)) {
						umlbNames.add(member.getName());
					}
				}
				
			}
		}
		return umlbNames;
	}


	/**
	 * Do the work after everything is specified.
	 */
	@Override
	public boolean performFinish() {
		try {
			
			// get all the selected details from the wizard pages
			//
			final IFile modelFile = getModelFile();
			final String name = modelFile.getName().substring(0, modelFile.getName().lastIndexOf('.'));
			final EventBNamedCommentedComponentElement elaboratedComponent = getElaboratedComponent();
			final UMLB refinedUmlb = getRefinedUmlb();
			
			// Do the work within an operation.
			//
			WorkspaceModifyOperation operation =
				new WorkspaceModifyOperation() {
					@Override
					protected void execute(IProgressMonitor progressMonitor) {
						try {
							// Create a resource set
							//
							ResourceSet resourceSet = new ResourceSetImpl();

							// Get the URI of the model file.
							//
							URI fileURI = URI.createPlatformResourceURI(modelFile.getFullPath().toString(), true);

							// Create a resource for this file.
							//
							Resource resource = resourceSet.createResource(fileURI);

							// Add the initial model object to the contents.
							//
							UMLB umlb = (UMLB) DiagramsFactory.eINSTANCE.create(DiagramsPackage.Literals.UMLB);
							umlb.setName(name);
							umlb.setElaborates(elaboratedComponent);
							umlb.setRefines(refinedUmlb);
							resource.getContents().add(umlb);
							
							// Save the contents of the resource to the file system.
							//
							Map<Object, Object> options = new HashMap<Object, Object>();
							options.put(XMLResource.OPTION_ENCODING, elaboratedComponentPage.getEncoding());
							resource.save(options);
						}
						catch (Exception exception) {
							exception.printStackTrace();
						}
						finally {
							progressMonitor.done();
						}
					}
				};

			getContainer().run(false, false, operation);

			// Select the new file resource in the current view.
			//
			IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
			IWorkbenchPage page = workbenchWindow.getActivePage();
			final IWorkbenchPart activePart = page.getActivePart();
			if (activePart instanceof ISetSelectionTarget) {
				final ISelection targetSelection = new StructuredSelection(modelFile);
				getShell().getDisplay().asyncExec
					(new Runnable() {
						 public void run() {
							 ((ISetSelectionTarget)activePart).selectReveal(targetSelection);
						 }
					 });
			}

// causes exceptions since the new FileEditor has null input.
// we probably do not want to open an editor anyway.
			
//			// Open an editor on the new file.
//			//
//			try {
//				
//				page.openEditor
//					(new FileEditorInput(modelFile),
//					 workbench.getEditorRegistry().getDefaultEditor(modelFile.getFullPath().toString()).getId());
//			}
//			catch (PartInitException exception) {
//				exception.printStackTrace();
//				MessageDialog.openError(workbenchWindow.getShell(), "Open Editor", exception.getMessage());
//				return false;
//			}

			return true;
		}
		catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
	}



	/**
	 * This is the file creation page of the wizard.

	 */
	public class DiagramsModelWizardNewFileCreationPage extends WizardNewFileCreationPage {
		/**
		 * Pass in the selection.
		 */
		public DiagramsModelWizardNewFileCreationPage(String pageId, IStructuredSelection selection) {
			super(pageId, selection);
		}

		/**
		 * The framework calls this to see if the file is correct.
		 */
		@Override
		protected boolean validatePage() {
			if (super.validatePage()) {
				String extension = new Path(getFileName()).getFileExtension();
				if (extension == null || !UMLB_FILE_EXTENSION.equals(extension)) {		
					setErrorMessage("The file name must end in ."+UMLB_FILE_EXTENSION);
					return false;
				}
				return true;
			}
			return false;
		}

		/**
		 * 
		 * Gets the file
		 */
		public IFile getModelFile() {
			return ResourcesPlugin.getWorkspace().getRoot().getFile(getContainerFullPath().append(getFileName()));
		}
		
		
	}

	/**
	 * This is the page where the type of object to create is selected.

	 */
	public class DiagramsModelWizardElaboratedComponentPage extends WizardPage {
		/**
		 */
		protected Combo elaboratedComponentField;
		
		/**
		 */
		protected Combo refinedUmlbField;

		/**
		 */
		protected List<String> encodings;

		/**
		 */
		protected Combo encodingField;

		/**
		 * Pass in the selection.
		 */
		public DiagramsModelWizardElaboratedComponentPage(String pageId) {
			super(pageId);
		}

		/**
		 */
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
			{
				GridLayout layout = new GridLayout();
				layout.numColumns = 1;
				layout.verticalSpacing = 12;
				composite.setLayout(layout);

				GridData data = new GridData();
				data.verticalAlignment = GridData.FILL;
				data.grabExcessVerticalSpace = true;
				data.horizontalAlignment = GridData.FILL;
				composite.setLayoutData(data);
			}

			//Elaborates
			Label elaboratedComponentLabel = new Label(composite, SWT.LEFT);
			{
				elaboratedComponentLabel.setText("&Elaborates");

				GridData data = new GridData();
				data.horizontalAlignment = GridData.FILL;
				elaboratedComponentLabel.setLayoutData(data);
			}

			elaboratedComponentField = new Combo(composite, SWT.BORDER);
			{
				GridData data = new GridData();
				data.horizontalAlignment = GridData.FILL;
				data.grabExcessHorizontalSpace = true;
				elaboratedComponentField.setLayoutData(data);
			}

			elaboratedComponentField.add("");
			try {
				for (String componentName : getComponentNames()) { 
					elaboratedComponentField.add(componentName);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}

			if (elaboratedComponentField.getItemCount() == 1) {
				elaboratedComponentField.select(0);
			}
			elaboratedComponentField.addModifyListener(validator);

			//Refines
			Label refineUmlbLabel = new Label(composite, SWT.LEFT);
			{
				refineUmlbLabel.setText("&Refines");

				GridData data = new GridData();
				data.horizontalAlignment = GridData.FILL;
				refineUmlbLabel.setLayoutData(data);
			}

			refinedUmlbField = new Combo(composite, SWT.BORDER);
			{
				GridData data = new GridData();
				data.horizontalAlignment = GridData.FILL;
				data.grabExcessHorizontalSpace = true;
				refinedUmlbField.setLayoutData(data);
			}

			refinedUmlbField.add("");
			try {
				for (String umlbName : getUmlbNames()) { 
					refinedUmlbField.add(umlbName);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}

			if (refinedUmlbField.getItemCount() == 1) {
				refinedUmlbField.select(0);
			}
			refinedUmlbField.addModifyListener(validator);
			
			//Encoding
			Label encodingLabel = new Label(composite, SWT.LEFT);
			{
				encodingLabel.setText("&XML Encoding");

				GridData data = new GridData();
				data.horizontalAlignment = GridData.FILL;
				encodingLabel.setLayoutData(data);
			}
			encodingField = new Combo(composite, SWT.BORDER);
			{
				GridData data = new GridData();
				data.horizontalAlignment = GridData.FILL;
				data.grabExcessHorizontalSpace = true;
				encodingField.setLayoutData(data);
			}

			for (String encoding : getEncodings()) {
				encodingField.add(encoding);
			}

			encodingField.select(0);
			encodingField.addModifyListener(validator);

			setPageComplete(validatePage());
			setControl(composite);
		}

		/**
		 */
		protected ModifyListener validator =
			new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					setPageComplete(validatePage());
				}
			};

		/**
		 */
		protected boolean validatePage() {
			return 	//getInitialObjectName() != null && 
					getEncodings().contains(encodingField.getText());
		}

		/**
		 */
		@Override
		public void setVisible(boolean visible) {
			super.setVisible(visible);
			if (visible) {
				if (elaboratedComponentField.getItemCount() == 1) {
					elaboratedComponentField.clearSelection();
					encodingField.setFocus();
				}
				else {
					encodingField.clearSelection();
					elaboratedComponentField.setFocus();
				}
			}
		}

		/**
		 */
		public String getEncoding() {
			return encodingField.getText();
		}

		/**
		 * Returns the label for the specified type name.
		 */
		protected String getLabel(String typeName) {
			try {
				return DiagramsEditPlugin.INSTANCE.getString("_UI_" + typeName + "_type");
			}
			catch(MissingResourceException mre) {
				mre.printStackTrace();
				//DiagramsEditorPlugin.INSTANCE.log(mre);
			}
			return typeName;
		}

		/**
		 */
		protected Collection<String> getEncodings() {
			if (encodings == null) {
				encodings = new ArrayList<String>();
				for (StringTokenizer stringTokenizer = new StringTokenizer("UTF-8 ASCII UTF-16 UTF-16BE UTF-16LE ISO-8859-1");
						//DiagramsEditorPlugin.INSTANCE.getString("_UI_XMLEncodingChoices")); 
						stringTokenizer.hasMoreTokens(); ) {
					encodings.add(stringTokenizer.nextToken());
				}
			}
			return encodings;
		}

		/**
		 * Returns a proxy representing the Event-B component to be elaborated
		 * (A proxy is returned to avoid having to load the component)
		 * 
		 * @return
		 * @throws CoreException
		 */
		protected EventBNamedCommentedComponentElement getElaboratedComponent() throws CoreException {
			String componentName = elaboratedComponentField.getText();
			int lio = componentName.lastIndexOf('.');
			if(lio<1) return null;
			String componentFileName = componentName.substring(0,lio);
			String componentExtension = componentName.substring(lio+1);
			EventBNamedCommentedComponentElement component;
			if ("bum".equals(componentExtension)) {
				component= (EventBNamedCommentedComponentElement) MachineFactory.eINSTANCE.create(MachinePackage.Literals.MACHINE);
			}else if("buc".equals(componentExtension)) {
				component= (EventBNamedCommentedComponentElement) ContextFactory.eINSTANCE.create(ContextPackage.Literals.CONTEXT);
			}else return null;

			((InternalEObject)component).eSetProxyURI(
					URI.createPlatformResourceURI(getModelFile().getProject().getName(), true) 	//project name
					  .appendSegment(componentFileName)											//resource name
					  .appendFileExtension(componentExtension)									//file extension
					  .appendFragment(((EventBElementImpl)component).getElementTypePrefix()+"::"+componentFileName));
			  
			return component;
		}
		
		/**
		 * Returns a proxy representing the UMLB model to be refined
		 * (A proxy is returned to avoid having to load the model)
		 * 
		 * @return
		 * @throws CoreException
		 */
		protected UMLB getRefinedUmlb() throws CoreException {
			String refinedUmlbName = refinedUmlbField.getText();
			int lio = refinedUmlbName.lastIndexOf('.');
			if(lio<1) return null;
			String refinedUmlbFileName = refinedUmlbName.substring(0,lio);
			String refinedUmlbExtension = refinedUmlbName.substring(lio+1);
			UMLB umlb;
			if (UMLB_FILE_EXTENSION.equals(refinedUmlbFileName)) {
				umlb= (UMLB) DiagramsFactory.eINSTANCE.create(DiagramsPackage.Literals.UMLB);
			}else return null;

			((InternalEObject)umlb).eSetProxyURI(
					URI.createPlatformResourceURI(getModelFile().getProject().getName(), true) 		//project name
					  .appendSegment(refinedUmlbFileName)											//resource name
					  .appendFileExtension(refinedUmlbExtension)									//file extension
					  .appendFragment(((EventBElementImpl)umlb).getElementTypePrefix()+"::"+refinedUmlbFileName));
			  
			return umlb;
		}
	}
	
	/**
	 * The framework calls this to create the contents of the wizard.

	 */
		@Override
	public void addPages() {
		// Create a page, set the title, and the initial model file name.
		//
		newFileCreationPage = new DiagramsModelWizardNewFileCreationPage("Whatever", selection);
		newFileCreationPage.setTitle("UML-B Diagrams Model"); //DiagramsEditorPlugin.INSTANCE.getString("_UI_DiagramsModelWizard_label"));
		newFileCreationPage.setDescription("Create a new UML-B Diagrams model");  //DiagramsEditorPlugin.INSTANCE.getString("_UI_DiagramsModelWizard_description"));
		newFileCreationPage.setFileName("ChangeMe"  //DiagramsEditorPlugin.INSTANCE.getString("_UI_DiagramsEditorFilenameDefaultBase") 
				+ "." + UMLB_FILE_EXTENSION);
		addPage(newFileCreationPage);

		// Try and get the resource selection to determine a current directory for the file dialog.
		//
		if (selection != null && !selection.isEmpty()) {
			// Get the resource...
			//
			Object selectedElement = selection.iterator().next();
			if (selectedElement instanceof IResource) {
				// Get the resource parent, if its a file.
				//
				IResource selectedResource = (IResource)selectedElement;
				if (selectedResource.getType() == IResource.FILE) {
					selectedResource = selectedResource.getParent();
				}

				// This gives us a directory...
				//
				if (selectedResource instanceof IFolder || selectedResource instanceof IProject) {
					// Set this for the container.
					//
					newFileCreationPage.setContainerFullPath(selectedResource.getFullPath());

					// Make up a unique new name here.
					//
					String defaultModelBaseFilename = "ChangeMe" ; //DiagramsEditorPlugin.INSTANCE.getString("_UI_DiagramsEditorFilenameDefaultBase");
					String defaultModelFilenameExtension = UMLB_FILE_EXTENSION;
					String modelFilename = defaultModelBaseFilename + "." + defaultModelFilenameExtension;
					for (int i = 1; ((IContainer)selectedResource).findMember(modelFilename) != null; ++i) {
						modelFilename = defaultModelBaseFilename + i + "." + defaultModelFilenameExtension;
					}
					newFileCreationPage.setFileName(modelFilename);
				}
			}
		}
		elaboratedComponentPage = new DiagramsModelWizardElaboratedComponentPage("Whatever2");
		elaboratedComponentPage.setTitle("UML-B Diagrams Model"); //DiagramsEditorPlugin.INSTANCE.getString("_UI_DiagramsModelWizard_label"));
		elaboratedComponentPage.setDescription("Select an Event-B component to elaborate"  ); // DiagramsEditorPlugin.INSTANCE.getString("_UI_Wizard_initial_object_description"));
		addPage(elaboratedComponentPage);
	}

	/**
	 * Get the file from the page.

	 */
	public IFile getModelFile() {
		return newFileCreationPage.getModelFile();
	}
	
	private EventBNamedCommentedComponentElement getElaboratedComponent() throws CoreException {
		return elaboratedComponentPage.getElaboratedComponent();
	}
	
	private UMLB getRefinedUmlb() throws CoreException {
		return elaboratedComponentPage.getRefinedUmlb();
	}

}
