package ac.soton.eventb.emf.diagrams.navigator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EObjectEList;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eventb.core.IEventBRoot;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;
import org.eventb.emf.persistence.EMFRodinDB;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRodinFile;
import org.rodinp.core.IRodinProject;
import org.rodinp.core.RodinDBException;

import ac.soton.eventb.emf.diagrams.navigator.provider.IDiagramProvider;

public class DiagramUtil {
	
	private static final Map<String, IDiagramProvider> registry = DiagramsNavigatorExtensionPlugin.getDefault().getDiagramProviderRegistry();
	private static final IProgressMonitor nullProgressMonitor = new NullProgressMonitor();
	
	/**
	 * Finds all the extensions elements in the root and loads them into EMF to call deleteDiagramFile
	 * @param eventBRoot
	 * 
	 */

	public static void deleteDiagramFiles(IEventBRoot eventBRoot) {
		EventBElement eventBElement = EMFRodinDB.INSTANCE.loadEventBComponent(eventBRoot);
		if (eventBElement instanceof EventBNamed){
			for (AbstractExtension absExt : eventBElement.getExtensions()){
				deleteDiagramFile(absExt);
			}
		}
		if (eventBElement!=null && eventBElement.eResource()!= null && eventBElement.eResource().isLoaded()){
			eventBElement.eResource().unload();
		}
	}

//	THIS WAS AN ALTERNATIVE HACK TO TRY TO GET DELETE TO WORK FOR POST CHANGE NOTIFICATION BUT IT DIDN"T WORK
//		try {
//			IProject project = rodinProject.getProject();
//			List<IFile> filesToDelete = new ArrayList<IFile>();
//			for (IResource resource : project.members()){
//				if (resource.exists() &&
//					resource.getType() == IResource.FILE &&
//					getDiagramExtensions().contains(((IFile) resource).getFileExtension()) &&
//					((IFile) resource).getName().startsWith(eventBComponentName+".")
//					){
//					filesToDelete.add((IFile)resource);
//				}
//			}
//			for (IFile file : filesToDelete){
//				file.delete(false,false,new NullProgressMonitor());
//			}
//		} catch (CoreException e) {
//			DiagramsNavigatorExtensionPlugin.getDefault().getLog().log(new Status(Status.ERROR, DiagramsNavigatorExtensionPlugin.PLUGIN_ID, "Failed deleting diagram files", e));
//		}
//	}
	
	/**
	 * deletes the diagram File associated with a particular element in the given machine
	 * 
	 * @param element
	 * @param machineName
	 */
	public static void deleteDiagramFile(EObject element) {
		// find diagram provider
		IDiagramProvider provider = registry.get(element.eClass().getName());
		if (provider == null) return;
		IProject project = WorkspaceSynchronizer.getFile(element.eResource()).getProject();
		IFile diagramFile = project.getFile(provider.getDiagramFileName(element));
		if (diagramFile.exists()) {
			try {
				diagramFile.delete(false,false,nullProgressMonitor);
			} catch (CoreException e) {
				DiagramsNavigatorExtensionPlugin.getDefault().getLog().log(new Status(Status.ERROR, DiagramsNavigatorExtensionPlugin.PLUGIN_ID, "Failed deleting diagram File", e));
			}
		}
	}
	
	/**
	 * Renames all the diagram files associated with elements in the given (new) Event-B root from the old Event-B component name
	 * AND updates any references within all the models in the project
	 * 
	 * @param eventBRoot
	 * @param oldComponentName
	 */
	public static void updateDiagramsForNewComponentName(IEventBRoot eventBRoot, String oldComponentName) {
		String newComponentName = eventBRoot.getElementName();
		String componentFileExtension = eventBRoot.getResource().getFileExtension();
		try {
			EventBElement eventBElement = EMFRodinDB.INSTANCE.loadEventBComponent(eventBRoot);
			if (eventBElement==null) return;
			//for the changed root, update the corresponding diagram files
			for (AbstractExtension abstractExtension : eventBElement.getExtensions()){
				renameDiagramFile(abstractExtension, oldComponentName, newComponentName, componentFileExtension);
			}

			IRodinFile[] rodinFiles = eventBRoot.getRodinProject().getRodinFiles();
			for (IRodinFile rodinFile : rodinFiles){
				IInternalElement root = rodinFile.getRoot();
				eventBElement = EMFRodinDB.INSTANCE.loadEventBComponent(root);
				if (eventBElement!=null) {
					//for all roots update any references				
					boolean dirty = updateModelReferencesForNewComponentName(eventBElement, oldComponentName, newComponentName,componentFileExtension);	
					if (dirty == true) eventBElement.eResource().save(Collections.emptyMap());
				}
			}
		} catch (Exception e) {
			DiagramsNavigatorExtensionPlugin.getDefault().getLog().log(new Status(Status.ERROR, DiagramsNavigatorExtensionPlugin.PLUGIN_ID, "Failed saving updated diagram model", e));
		}
	}

	private static boolean updateModelReferencesForNewComponentName(EObject element, String oldComponentName, String newComponentName, String componentFileExtension) {
		boolean dirty = false;
		for (EReference reference : element.eClass().getEAllReferences()){
			if (!reference.isContainment()){
				Object referenceValue = element.eGet(reference, false);
				if (referenceValue instanceof EObject){
					dirty = updateComponentNameInReference(referenceValue,oldComponentName, newComponentName, componentFileExtension) || dirty;
				}else if (referenceValue instanceof EObjectEList){
					for (Object referenceObject : (EObjectEList<?>)referenceValue){
						dirty = updateComponentNameInReference(referenceObject,oldComponentName, newComponentName, componentFileExtension) || dirty;
					}
				}
			}
		}
		for (EObject child : element.eContents()){
			dirty = updateModelReferencesForNewComponentName(child,oldComponentName, newComponentName, componentFileExtension) || dirty;
		}
		return dirty;
	}

	private static boolean updateComponentNameInReference(Object referenceValue, String oldComponentName, String newComponentName, String componentFileExtension){
		if (referenceValue instanceof EObject && ((EObject) referenceValue).eIsProxy()){
			URI uri = ((InternalEObject)referenceValue).eProxyURI();
			String[] segments = uri.segments();			
			if (segments.length > 2 && 
				"resource".equals(segments[0]) && 
				(oldComponentName+"."+componentFileExtension).equals(segments[2])){
				segments[2] = newComponentName+"."+componentFileExtension;
				uri = uri.trimSegments(uri.segmentCount());
				uri =uri.appendSegments(segments);
				String fragment = uri.fragment();
				fragment = fragment.replaceAll("::"+oldComponentName+"\\.", "::"+newComponentName+".");
				uri = uri.trimFragment().appendFragment(fragment);
				((InternalEObject)referenceValue).eSetProxyURI(uri);
				return !oldComponentName.equals(newComponentName);
			}
		}
		return false;
	}
	
	/**
	 * Renames the diagram file associated with the given element from the old Event-B component name to the new one
	 * 
	 * 
	 * @param element
	 * @param oldMachineName
	 * @param newMachineName
	 * @param componentFileExtension
	 */
	
	public static void renameDiagramFile(EObject element, String oldMachineName, String newMachineName, String componentFileExtension) {
		// find diagram provider
		IDiagramProvider provider = registry.get(element.eClass().getName());
		if (provider == null) return;
		String oldFileName = (provider.getDiagramFileName(element)).replaceFirst(newMachineName, oldMachineName);
		IProject project = WorkspaceSynchronizer.getFile(element.eResource()).getProject();
		IFile diagramFile = project.getFile(oldFileName);
		if (diagramFile.exists()) {
			try {
				//the copier also replaces references to the component inside the diagram file
				copyDiagramForNewRoot(project, oldFileName, oldMachineName, newMachineName, componentFileExtension, null);
				//delete the old diagram
				diagramFile.delete(false,false,nullProgressMonitor);
			} catch (CoreException e) {
				DiagramsNavigatorExtensionPlugin.getDefault().getLog().log(new Status(Status.ERROR, DiagramsNavigatorExtensionPlugin.PLUGIN_ID, "Failed renaming diagram File", e));
			}
		}
	}
	
	
	
	/**
	 * Makes a copy of an ascii file replacing any references to another file. 
	 * Intended for copying diagram layout files that reference an Event-B component (Machine or Context)
	 * The diagram file is assumed to contain the component name in its file name.
	 * 
	 * @param project					The containing project
	 * @param sourceDiagramFileName		The filename of the source diagram file including extension
	 * @param oldRootName				The old component name (without extension)
	 * @param newRootName				The new component name (without extension)
	 * @param fileExtension				The file extension of the Event-B component
	 * @param monitor					A monitor or null
	 * @throws CoreException
	 */
	public static void copyDiagramForNewRoot(IProject project, String sourceDiagramFileName, String oldRootName, String newRootName, String fileExtension, IProgressMonitor monitor) throws CoreException {
		if (monitor!= null) monitor.beginTask("Copying " + sourceDiagramFileName, 1);
		
		final IFile sourceFile = project.getFile(new Path(sourceDiagramFileName));
		String targetDiagramFileName = sourceDiagramFileName.replaceFirst( oldRootName, newRootName);			
		final IFile targetFile = project.getFile(new Path(targetDiagramFileName));
		try {
			InputStream stream = modifiedFileContentStream(sourceFile, oldRootName, newRootName, fileExtension);
			if (targetFile.exists()) {
				targetFile.setContents(stream, true, true, monitor);
			} else {
				targetFile.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		
		if (monitor!= null) monitor.worked(1);
	}

	private static InputStream modifiedFileContentStream(IFile sourceFile, String oldRootName, String newRootName, String fileExtension) throws IOException, CoreException {
		BufferedReader sourceBuf = new BufferedReader(new InputStreamReader(sourceFile.getContents()));
		String line;
		String contents = "";
		while((line = sourceBuf.readLine()) != null){
			line = line.replaceAll("\\b"+oldRootName+"\\."+fileExtension, newRootName+"."+fileExtension);	//resource name
			line = line.replaceAll("::"+oldRootName+"\\.", "::"+newRootName+".");							//component name in ID
			contents = contents + line + "\n";
		}
		sourceBuf.close();
		return new ByteArrayInputStream(contents.toString().getBytes());
	}

	/////////////////////////////////// project rename ////////////////////////////////
	/**
	 * Project renaming
	 * @param project
	 * @param oldProjectName (or null to match any project name)
	 */
	public static void projectRenamed(IRodinProject project, String oldProjectName) {
		try {
			for (IRodinFile rodinFile : project.getRodinFiles()){
				updateDiagramsForNewProjectName((IEventBRoot) rodinFile.getRoot(), project.getElementName(), oldProjectName);
			}
		} catch (RodinDBException e) {
			DiagramsNavigatorExtensionPlugin.getDefault().getLog().log(new Status(Status.ERROR, DiagramsNavigatorExtensionPlugin.PLUGIN_ID, "Failed getting files from renamed project", e));
		}
		
	}
	
	/**
	 * Updates all the references within abstract extensions in the given Event-B root from the old Event-B project name to the new one
	 * 
	 * @param eventBRoot
	 * @param oldComponentName
	 */
	public static void updateDiagramsForNewProjectName(IEventBRoot eventBRoot, String newProjectName, String oldProjectName) {
		try { 
			EventBElement eventBElement = EMFRodinDB.INSTANCE.loadEventBComponent(eventBRoot);
			if (eventBElement==null) return;
			boolean dirty = false;
			for (AbstractExtension abstractExtension : eventBElement.getExtensions()){
				dirty = updateModelReferencesForNewProjectName(abstractExtension, oldProjectName, newProjectName) || dirty;
			}
			Resource resource = eventBElement.eResource();
			boolean deliver = resource.eDeliver();
			resource.eSetDeliver(false);
			if (dirty = true) resource.save(Collections.emptyMap());
			resource.unload();
			resource.eSetDeliver(deliver);
		} catch (IOException e) {
			DiagramsNavigatorExtensionPlugin.getDefault().getLog().log(new Status(Status.ERROR, DiagramsNavigatorExtensionPlugin.PLUGIN_ID, "Failed saving updated diagram model", e));
		}
	}

	private static boolean updateModelReferencesForNewProjectName(EObject element, String oldProjectName, String newProjectName) {
		boolean dirty = false;
		for (EReference reference : element.eClass().getEAllReferences()){
			if (!reference.isContainment()){
				Object referenceValue = element.eGet(reference, false);
				if (referenceValue instanceof EObject){
					dirty = updateProjectNameInReference(referenceValue,oldProjectName, newProjectName) || dirty;
				}else if (referenceValue instanceof EObjectEList){
					for (Object referenceObject : (EObjectEList<?>)referenceValue){
						dirty = updateProjectNameInReference(referenceObject, oldProjectName, newProjectName) || dirty;
					}
				}
			}
		}
		for (EObject child : element.eContents()){
			dirty = updateModelReferencesForNewProjectName(child, oldProjectName, newProjectName) || dirty;
		}
		return dirty;
	}

	private static boolean updateProjectNameInReference(Object referenceValue, String oldProjectName, String newProjectName){
		if (referenceValue instanceof EObject && ((EObject) referenceValue).eIsProxy()){
			URI uri = ((InternalEObject)referenceValue).eProxyURI();
			String[] segments = uri.segments();			
			if (segments.length > 1 && 
				"resource".equals(segments[0]) && 
				(oldProjectName==null || oldProjectName.equals(segments[1]))){
				segments[1] = newProjectName;
				uri = uri.trimSegments(uri.segmentCount());
				uri =uri.appendSegments(segments);
//				String fragment = uri.fragment();					//not required as ID does not include project name
//				fragment = fragment.replaceAll("::"+oldProjectName+"\\.", "::"+newProjectName+".");
//				uri = uri.trimFragment().appendFragment(fragment);
				((InternalEObject)referenceValue).eSetProxyURI(uri);
				return !newProjectName.equals(oldProjectName);
			}
		}
		return false;
	}
	
}
