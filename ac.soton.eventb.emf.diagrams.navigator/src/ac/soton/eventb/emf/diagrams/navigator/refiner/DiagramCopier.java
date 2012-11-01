/**
 * Copyright (c) 2012 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.navigator.refiner;

import java.io.BufferedReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.machine.Machine;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRefinementParticipant;
import org.rodinp.core.RodinDBException;

import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;
import ac.soton.eventb.emf.diagrams.navigator.provider.IDiagramProvider;

/**
 * This refinement participant preserves the layout of diagrams in machines.
 * For each diagram model in the machine, the corresponding diagram layout file (if any)
 * is copied and model references are updated to the refined version of the diagram model.
 * 
 * @author Dong Wang
 *
 */
public class DiagramCopier implements IRefinementParticipant {

	ResourceSet resourceSet =  new ResourceSetImpl();
	
	@Override
	public void process(IInternalElement targetRoot,
			IInternalElement sourceRoot, IProgressMonitor monitor)
			throws RodinDBException {

		URI fileURI = URI.createPlatformResourceURI(sourceRoot.getResource().getFullPath().toString(), true);
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
		if (resource.isLoaded() && resource.getContents().isEmpty() == false && resource.getContents().get(0) instanceof Machine) {
			Machine machine = (Machine) resource.getContents().get(0);

			for (AbstractExtension ext : machine.getExtensions()){
				// find diagram provider
				Map<String, IDiagramProvider> registry = DiagramsNavigatorExtensionPlugin.getDefault().getDiagramProviderRegistry();
				String type = ext.eClass().getName();
				IDiagramProvider provider = registry.get(type);
				if (provider!= null){
					// get diagram filename
					String sourceDiagramFilename = provider.getDiagramFileName(ext);
					String projectFullPath = sourceRoot.getResource().getProject().getFullPath().toString();
					String sourceMachineName = fileNameProcess(sourceRoot.getResource().getName());//It is the machine name, not the machine file name.
					String targetMachineName = fileNameProcess(targetRoot.getResource().getName());
					String targetDiagramFilename = sourceDiagramFilename.replaceFirst( sourceMachineName, targetMachineName);
					try {
						copyDiagram(projectFullPath, sourceDiagramFilename, targetDiagramFilename, monitor);
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}	
		}		
	}
	
	private void copyDiagram(
			String projectFullPath,
			String sourceFileName,
			String targetFileName,
			IProgressMonitor monitor)
			throws CoreException {
			// copy the file
			monitor.beginTask("Copying " + sourceFileName, 1);
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource resource = root.findMember(new Path(projectFullPath));
			IContainer container = (IContainer) resource;
			final IFile sourceFile = container.getFile(new Path(sourceFileName));
			final IFile targetFile = container.getFile(new Path(targetFileName));
			try {
				InputStream stream = fileContentStream(sourceFile, targetFileName);
				if (targetFile.exists()) {
					targetFile.setContents(stream, true, true, monitor);
				} else {
					targetFile.create(stream, true, monitor);
				}
				stream.close();
			} catch (IOException e) {
			}
			monitor.worked(1);
		}

		private InputStream fileContentStream(IFile sourceFile, String targetFile) throws IOException, CoreException {
			BufferedReader sourceBuf = new BufferedReader(new InputStreamReader(sourceFile.getContents()));
			String line;
			String contents = "";
			String source = sourceFile.getName();
			while((line = sourceBuf.readLine()) != null){
				contents = contents + contentProcess(line, fileNameProcess(source), fileNameProcess(targetFile)) + "\n";
			}
			sourceBuf.close();
			
			return new ByteArrayInputStream(contents.toString().getBytes());
		}
		
		private String fileNameProcess(String fileName){
			int i = 0;
			while(!fileName.substring(i,i+1).equals(".")){
				i++;
			}
			return fileName.substring(0, i).trim();

		}
		
		private String contentProcess(String content, String source, String target){
			String regex;
			String replacement;
			regex = source + ".bum";
			replacement = target + ".bum";
			return content.replaceAll(regex, replacement);

		}
}
