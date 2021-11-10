/*******************************************************************************
 * Copyright (c) 2014, 2018 University of Southampton.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    University of Southampton - initial API and implementation
 *******************************************************************************/
package ac.soton.eventb.emf.diagrams.refactor.persistence;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;

/**
 * This class provides a convenient text field which can easily be persisted
 *  
 * @author cfs
 *
 */
public class TextFile {

	private String text;


	public TextFile(String text) {
		this.text = text;
	}

	public TextFile() {
		this.text = "";
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public void addText(String text){
		this.text = this.text+text;
	}
	
	public void addLine(String text){
		this.text = this.text+"\n"+text;
	}
	
	public String[] getLines(){
		return text.split("\n");
	}

	/**
	 * This writes the text string out to a text file at the given uri
	 * 
	 * @param project
	 * @param uri
	 * @param monitor
	 */
	public void save(IProject project, URI uri, IProgressMonitor monitor){
		try {
			final IFile targetFile = getFile(project, uri, monitor);
			InputStream stream = new ByteArrayInputStream(text.getBytes());
			if (targetFile.exists()) {
				targetFile.setContents(stream, true, true, monitor);
			} else {
				targetFile.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This reads the text string from a text file at the given uri
	 * 
	 * @param project
	 * @param uri
	 * @param monitor
	 */
	public void load(IProject project, URI uri, IProgressMonitor monitor){
		try {
			text="";
			final IFile targetFile = getFile(project, uri, monitor);
			if (targetFile.exists()) {
				InputStream stream = targetFile.getContents(true);
				byte[] bytes = new byte[1000];
				while (stream.read(bytes)>0){
					String string = new String(bytes);
					text=text+string;
				}
				stream.close();
			}
		} catch (IOException e) {
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This reads the text string from a text file at the given uri
	 * 
	 * @param project
	 * @param uri
	 * @param monitor
	 */
	public void delete(IProject project, URI uri, IProgressMonitor monitor){
		try {
			final IFile targetFile = getFile(project, uri, monitor);
			if (targetFile.exists()) {
				targetFile.delete(true, monitor);
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * @param project
	 * @param uri
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	private IFile getFile(IProject project, URI uri, IProgressMonitor monitor)
			throws CoreException {
		String folderPath = "";
		for (int i=2; i<uri.segmentCount()-1; i++){
			folderPath = folderPath +"/"+ uri.segment(i);
			IFolder folder = project.getFolder(folderPath);
			if (!folder.exists()){
				folder.create(true, true, monitor);
			}
		}
		IPath filePath = new Path(uri.toPlatformString(true));
		filePath = filePath.makeRelativeTo(project.getFullPath());
		final IFile targetFile = project.getFile(filePath);
		return targetFile;
	}
	
}
