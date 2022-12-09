/*******************************************************************************
 * Copyright (c) 2014, 2015 University of Southampton.
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

package ac.soton.eventb.emf.diagrams.navigator.handler;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.wizards.datatransfer.ArchiveFileExportOperation;

import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;


/**
 * Archive project handler.
 * This handler makes an archive of the selected project at the destination specified in the iUML-B preferences.
 * This is equivalent to the eclipse export wizard but reduces user interaction
 * 
 * @author cfs 
 *
 */
@SuppressWarnings("restriction")
public class ArchiveProjectHandler extends AbstractHandler {
	
	private static IContainer archiveContainer = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {	
		
		IProgressMonitor monitor = new NullProgressMonitor(); // for now no progress shown

		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IProject) {
				archiveProject((IProject)element, monitor);	
			}
		}
		return null;
	}

	/**
	 * @param monitor
	 * @param timeStamp
	 * @param project
	 */
	public static void archiveProject(IProject project, IProgressMonitor monitor) {
		ArchiveFileExportOperation archive;
		try {
			archive = new ArchiveFileExportOperation(project, getArchiveDestination(project.getName()+"_"+getTimeStamp()));
		    archive.setCreateLeadupStructure(true);
		    archive.setUseCompression(true);  
		    archive.setUseTarFormat(true);
			archive.run(monitor);
			if (archiveContainer!=null) archiveContainer.refreshLocal(IResource.DEPTH_INFINITE, monitor);
		} catch (CoreException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	//gets the path to the location to store archives
	//if this is in the workspace the projects and folders are created if necessary and the project is opened
	private static IPath getArchivePath() throws CoreException{
		String pathString = DiagramsNavigatorExtensionPlugin.getDefault().getPreferenceStore().getString(DiagramsNavigatorExtensionPlugin.PREFERENCES_ARCHIVE_PATH);
		int sep = pathString.indexOf(':');
		if (sep > -1) pathString = pathString.substring(0, sep);
		IPath path = Path.fromOSString(pathString);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IPath workspacePath = root.getLocation();
		if (workspacePath.isPrefixOf(path)){
			IPath relativePath = path.makeRelativeTo(workspacePath);
			IContainer container = root;
			boolean proj = true;
			for (String seg : relativePath.segments()){
				container = proj? root.getProject(seg) : root.getFolder(relativePath);
				if (!container.exists()){
					if (proj) {
						((IProject)container).create(null);
					} else {
						((IFolder)container).create(true, true, null);
					}
				}
				if (proj && !((IProject)container).isOpen()){
					((IProject)container).open(null);
				}
				proj = false;
			}
			archiveContainer = container;
		}else{
			archiveContainer = null;			
		}
		return path;
	}
	
	//gets a location string path to the archive destination file
	private static String getArchiveDestination(String archiveFileName) throws CoreException {
		IPath path = getArchivePath();
		path = path.append(archiveFileName);
		path = path.addFileExtension("tar.gz");
		return path.toOSString();
	}
	
	/**
	 * generates a string from the current time
	 * @return
	 */
	private static String getTimeStamp() {
		Calendar cal = Calendar.getInstance();
		String m = ""+(cal.get(Calendar.MONTH)+1);
		m=m.length()<2? "0"+m : m;
		String d = ""+(cal.get(Calendar.DAY_OF_MONTH));
		d=d.length()<2? "0"+d : d;
		String h = ""+(cal.get(Calendar.HOUR_OF_DAY));
		h=h.length()<2? "0"+h : h;
		String n = ""+(cal.get(Calendar.MINUTE));
		n=n.length()<2? "0"+n : n;
		return ""+cal.get(Calendar.YEAR)+m+d+h+n;
	}
}
