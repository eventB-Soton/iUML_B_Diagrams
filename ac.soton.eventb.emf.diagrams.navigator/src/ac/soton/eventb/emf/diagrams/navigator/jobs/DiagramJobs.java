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
package ac.soton.eventb.emf.diagrams.navigator.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eventb.core.IEventBRoot;
import org.rodinp.core.IRodinProject;

public class DiagramJobs {

	private static final String QUALIFIER = "ac.soton.eventb.emf.diagrams.navigator";
	private static final QualifiedName RODIN_PROJECT = new QualifiedName(QUALIFIER, "PROJECT");
	private static final QualifiedName OLD_PROJECT_NAME = new QualifiedName(QUALIFIER, "OLD_PROJECT_NAME");
	private static final QualifiedName RODIN_COMPONENT = new QualifiedName(QUALIFIER, "COMPONENT");
	private static final QualifiedName OLD_COMPONENT_NAME = new QualifiedName(QUALIFIER, "OLD_COMPONENT_NAME");
		   
	public static void ScheduleDiagramUpdateForComponentRename (IRodinProject project, IEventBRoot component, String oldComponentName) {
		Job diagramUpdaterJob = new Job("Updating diagram references for new component name") {
		      public IStatus run(IProgressMonitor monitor) {
		        //System.out.println("Updating diagram references for new project name");
		     	DiagramUtil.updateDiagramsForNewComponentName((IEventBRoot)getProperty(RODIN_COMPONENT), (String)getProperty(OLD_COMPONENT_NAME));
		        return Status.OK_STATUS;
		      }
		   };
		diagramUpdaterJob.setRule(project.getProject()); // the job will need to lock the project
		diagramUpdaterJob.setPriority(Job.LONG);  // low priority
		diagramUpdaterJob.setProperty(RODIN_COMPONENT, component);
		diagramUpdaterJob.setProperty(OLD_COMPONENT_NAME, oldComponentName);				
		diagramUpdaterJob.schedule();
	}	
	
	public static void ScheduleDiagramUpdateForComponentDeletion (IRodinProject project, String oldComponentName){
		Job diagramUpdaterJob = new Job("Updating diagram references for deleted component") {
		      public IStatus run(IProgressMonitor monitor) {
		         //System.out.println("Updating diagram references for deleted component");
		 		 DiagramUtil.deleteDiagramFiles((IEventBRoot)getProperty(RODIN_COMPONENT));
		         return Status.OK_STATUS;
		      }
		   };
		diagramUpdaterJob.setRule(project.getProject()); // the job will need to lock the project
		diagramUpdaterJob.setPriority(Job.LONG);  // low priority
		diagramUpdaterJob.setProperty(RODIN_PROJECT, project);
		diagramUpdaterJob.setProperty(OLD_COMPONENT_NAME, oldComponentName);				
		diagramUpdaterJob.schedule();
	}

	public static void ScheduleDiagramUpdateForProjectRename (IRodinProject renamedProject, String oldProjectName) {
		Job diagramUpdaterJob = new Job("Updating diagram references for new project name") {
		      public IStatus run(IProgressMonitor monitor) {
		         //System.out.println("Updating diagram references for new project name");
		         DiagramUtil.projectRenamed((IRodinProject)getProperty(RODIN_PROJECT), (String)getProperty(OLD_PROJECT_NAME));
		         return Status.OK_STATUS;
		      }
		   };
		diagramUpdaterJob.setRule(renamedProject.getProject()); // the job will need to lock the project
		diagramUpdaterJob.setPriority(Job.LONG);  // low priority
		diagramUpdaterJob.setProperty(RODIN_PROJECT, renamedProject);
		diagramUpdaterJob.setProperty(OLD_PROJECT_NAME, oldProjectName);				
		diagramUpdaterJob.schedule();
	}
		
}
