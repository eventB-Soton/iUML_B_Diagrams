/*******************************************************************************
 * Copyright (c) 2014, 2017 University of Southampton.
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
package ac.soton.eventb.emf.diagrams.navigator.refiner;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eventb.emf.core.AbstractExtension;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.persistence.EMFRodinDB;
import org.rodinp.core.IInternalElement;
import org.rodinp.core.IRefinementParticipant;
import org.rodinp.core.RodinDBException;

import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;
import ac.soton.eventb.emf.diagrams.navigator.jobs.DiagramUtil;
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

	/**
	 * Create an EMFRodinDB for loading extensions into EMF
	 */
	private final static EMFRodinDB emfRodinDB = new EMFRodinDB();
	
	@Override
	public void process(IInternalElement targetRoot, IInternalElement sourceRoot, IProgressMonitor monitor) throws RodinDBException {

		String oldRootName = sourceRoot.getElementName(); 
		String newRootName = targetRoot.getElementName();
		String fileExtension = sourceRoot.getResource().getFileExtension();		

		EventBElement eventBElement = emfRodinDB.loadEventBComponent(sourceRoot);
		if (eventBElement != null){
			//get the diagram provider registry
			Map<String, IDiagramProvider> registry = DiagramsNavigatorExtensionPlugin.getDefault().getDiagramProviderRegistry();
			//find any extension elements likely to have diagrams
			for (AbstractExtension extension : eventBElement.getExtensions()){
				// try to find a diagram provider
				IDiagramProvider provider = registry.get(extension.eClass().getName());
				if (provider!= null){
					//try to make a copy of the diagram
					try {
						DiagramUtil.copyDiagramForNewRoot(sourceRoot.getResource().getProject(), provider.getDiagramFileName(extension), oldRootName, newRootName, fileExtension, monitor);
					} catch (CoreException e) {

						e.printStackTrace();
					}
				}
			}
		}		
	}

}
