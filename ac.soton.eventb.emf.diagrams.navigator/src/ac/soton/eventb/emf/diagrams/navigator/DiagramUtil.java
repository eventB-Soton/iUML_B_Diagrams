package ac.soton.eventb.emf.diagrams.navigator;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.workspace.util.WorkspaceSynchronizer;
import org.eventb.core.IEventBRoot;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamed;
import org.rodinp.core.IInternalElement;

import ac.soton.eventb.emf.diagrams.navigator.provider.IDiagramProvider;

public class DiagramUtil {
	
	
	private static final Map<String, IDiagramProvider> registry = DiagramsNavigatorExtensionPlugin.getDefault().getDiagramProviderRegistry();
	private static final ResourceSet resourceSet =  new ResourceSetImpl();
	private static List<String> diagramFileExtensions = null;
	
	/**
	 * loads an Event-B component (root) into EMF
	 * @param root
	 * @return
	 */
	public static EventBElement loadIntoEMF(IInternalElement root){
		if (root == null || !root.exists()) return null;
		URI fileURI = URI.createPlatformResourceURI(root.getResource().getFullPath().toString(), true);
		Resource resource = resourceSet.getResource(fileURI, false); //n.b. do not load until notifications disabled
		if (resource == null){
			resource = resourceSet.createResource(fileURI);
		}
		if (!resource.isLoaded()){
			resource.eSetDeliver(false);	// turn off notifications to Transactional Change Recorder while loading
			try {
				resource.load(Collections.emptyMap());
			} catch (IOException e) {
				e.printStackTrace();
			}
			resource.eSetDeliver(true);
		}
		if (resource.isLoaded() && !resource.getContents().isEmpty() && resource.getContents().get(0) instanceof EventBElement) {
			return (EventBElement) resource.getContents().get(0);
		}else{
			return null;
		}

	}
	
	/**
	 * returns a list of all the possible diagram file extensions
	 * 
	 * @return
	 */
	public static List<String> getDiagramExtensions(){
		if (diagramFileExtensions == null){
			diagramFileExtensions = new ArrayList<String>();
			for(IDiagramProvider provider : registry.values()){
				diagramFileExtensions.add(provider.getFileExtension());
			}
		}
		return diagramFileExtensions;
	}
	
	
	/**
	 * THIS DOES NOT WORK BECAUSE WE DON'T GET NOTIFIED UNTIL TOO LATE
	 * STILL DOES NOT WORK BECAUSE RESOURCE TREE IS LOCKED!!!!!! WHY????
	 * Finds all the extensions elements in the root and loads them into EMF to call deleteDiagramFile
	 * @param eventBRoot
	 * 
	 */

	public static void deleteDiagramFiles(IProject project, String eventBComponentName) {
		try {
			List<IFile> filesToDelete = new ArrayList<IFile>();
			for (IResource resource : project.members()){
				if (resource.exists() &&
					resource.getType() == IResource.FILE &&
					getDiagramExtensions().contains(((IFile) resource).getFileExtension()) &&
					((IFile) resource).getName().startsWith(eventBComponentName+".")
					){
					filesToDelete.add((IFile)resource);
				}
			}
			for (IFile file : filesToDelete){
				//FIXME: Why is the resource tree locked?
			//	file.delete(false,false,new NullProgressMonitor());
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
		
//		EventBElement eventBElement = DiagramUtil.loadIntoEMF(eventBRoot);
//		if (eventBElement instanceof EventBNamed){
//			for (AbstractExtension absExt : eventBElement.getExtensions()){
//				deleteDiagramFile(absExt, ((EventBNamed)eventBElement).getName());
//			}
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
				diagramFile.delete(false,false,new NullProgressMonitor());
			} catch (CoreException e) {
				DiagramsNavigatorExtensionPlugin.getDefault().getLog().log(new Status(Status.ERROR, DiagramsNavigatorExtensionPlugin.PLUGIN_ID, "Failed deleting diagram File", e));
			}
		}
	}
	
	/**
	 * Renames all the diagram files associated with elements in the given (new) Event-B root from the old Event-B component name
	 * 
	 * @param eventBRoot
	 * @param oldComponentName
	 */
	public static void renameDiagramFiles(IEventBRoot eventBRoot, String oldComponentName) {
		EventBElement eventBElement = DiagramUtil.loadIntoEMF(eventBRoot);
		if (eventBElement instanceof EventBNamed){
			String newComponentName = ((EventBNamed)eventBElement).getName();
			String componentFileExtension = eventBRoot.getResource().getFileExtension();
			for (AbstractExtension absExt : eventBElement.getExtensions()){
				renameDiagramFile(absExt, oldComponentName, newComponentName, componentFileExtension);
			}
		}
	}

	/**
	 * Renames the diagram file associated with the given element from the old Event-B component name to the new one
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
				diagramFile.delete(false,false,new NullProgressMonitor());
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
			contents = contents + line.replaceAll("\\b"+oldRootName+"\\."+fileExtension, newRootName+"."+fileExtension) + "\n";
		}
		sourceBuf.close();
		return new ByteArrayInputStream(contents.toString().getBytes());
	}
	
}
