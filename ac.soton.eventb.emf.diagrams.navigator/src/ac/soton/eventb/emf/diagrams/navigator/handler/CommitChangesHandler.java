/**
 * Copyright (c) 2012 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.navigator.handler;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.diagram.ui.parts.DiagramEditor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.core.IMachineRoot;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.machine.Machine;
import org.eventb.emf.persistence.EMFRodinDB;

import ac.soton.eventb.emf.diagrams.generator.actions.GenerateAllHandler;
import ac.soton.eventb.emf.diagrams.navigator.DiagramsNavigatorExtensionPlugin;
import ac.soton.eventb.emf.diagrams.navigator.refactor.CommitAssistant;
import ac.soton.eventb.emf.diagrams.navigator.refactor.Recorder;
import ac.soton.eventb.emf.diagrams.navigator.refactor.persistence.RefactorPersistence;


/**
 * Commit changes handler.
 * This handler commits the recorded changes made to the selected machine.
 * This consists of:
 * a) propagating the changes made in the current selected component to each lower refinement
 * b) delete the change record for the current selected component
 * c) generate all diagrams in the current selected component and all lower level refinements
 * 
 * Committing is NOT enabled if 
 * There is no change record for this refinement level OR
 * ANY of the lower refinement levels have outstanding change records.
 * 
 * @author cfs 
 *
 */
public class CommitChangesHandler extends AbstractHandler {
	
	/**
	 * Create an EMFRodinDB for loading extensions into EMF
	 */
	private final static EMFRodinDB emfRodinDB = new EMFRodinDB();
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {	
		Shell shell = HandlerUtil.getActiveShell(event);
		
		//check refactoring preference is enabled
		Boolean refactoringEnabled =  DiagramsNavigatorExtensionPlugin.getDefault().getPreferenceStore().getBoolean("RefactoringEnabled");
		if (!refactoringEnabled) {
			MessageDialog.open(MessageDialog.INFORMATION, shell,
		    		  "Refactoring is switched off", 
		    		  "Changes are not being recorded because refactoring is disabled in preferences. "+
		    		  " Therefore there are no changes to commit. "+
		    		  " To switch refactoring on, go to Rodin Platform - Preferences and select iUML-B.", SWT.NONE 
		    			);
			return null;
		}

		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IMachineRoot) {
				EventBNamedCommentedComponentElement component = (EventBNamedCommentedComponentElement) emfRodinDB.loadEventBComponent((IMachineRoot)element);
				String projectName = emfRodinDB.getProjectName(component); 
				List<EventBNamedCommentedComponentElement> components = emfRodinDB.loadAllComponents(projectName);
		
				// Check that there is a change record for this refinement level
				if (RefactorPersistence.INSTANCE.hasChangesResource(EcoreUtil.getURI(component))){
					
					if (!hasLowerLevelChanges(component,components)){
					
						boolean ok = MessageDialog.openConfirm(shell,
					    		  "Confirm Commit", 
					    		  "The Commit operation will propagate the changes accumulated for this refinement level.\n"+
					    		  "This consists of:\n"+
					    			  "* propagating the changes made in the current selected component to each lower refinement\n"+
					    			  "* delete the change record for the current selected component\n"+
					    			  "* generate all diagrams in the current selected component and all lower level refinements.\n"+
					    			  "(all editors will be closed automatically before commit begins)"
					    			);
						if (ok){
							
							//save/close all diagrams
							for(IWorkbenchPage pg : HandlerUtil.getActiveWorkbenchWindow(event).getPages()){
								//pg.closeAllEditors(true);
						    	for(IEditorReference editorRef: pg.getEditorReferences()){
						    		IEditorPart editor = editorRef.getEditor(true);
						    		if (editor instanceof DiagramEditor) {
						    			pg.closeEditor(editor, true);
//						    			DiagramEditor diagramEditor = (DiagramEditor) editor;
//						    			EObject diagramElement = diagramEditor.getDiagram().getElement();
//						    			Resource resource = diagramElement.eResource();
//						    			if (diagramElement instanceof EventBObject){
//					    					EventBObject c = ((EventBObject)diagramElement).getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
//					    					if (c!= null && components.contains(c)){
//					    						if (diagramEditor.isDirty()){
//					    							diagramEditor.doSave(new NullProgressMonitor());
//					    						}
//					    						editorRef.getPage().closeAllEditors(true);
//					    						diagramEditor.dispose();
//					    					}
//						    			}
					    	    	}
						    	}
							}

							GenerateAllHandler genAll = new GenerateAllHandler();
							
							//propagate the changes made in the current selected component to each lower refinement (TBD)
							//ecr.createForwardChangeRecords();
							EventBNamedCommentedComponentElement cp = component;
							EventBNamedCommentedComponentElement rcp = null;
							do {														
								CommitAssistant commitAssistant = new CommitAssistant(cp);			
								if ((rcp=getRefinement(cp, components))!=null 
									&& commitAssistant.hasChanges()){
									Recorder ecr = Recorder.getNewRecorder(rcp);
									ecr.resumeRecording();
									commitAssistant.applyChangesTo(rcp);
									ecr.endRecording();
									ecr.saveChanges();
									ecr.disposeChangeRecorder();
								}
								//delete the change record for the current selected component
								commitAssistant.deleteChangeRecords();
								
								genAll.generateAllDiagrams(cp, shell, emfRodinDB.getEditingDomain(), null);

								try {
									if (!cp.eIsProxy()){
										cp.eResource().save(Collections.EMPTY_MAP);
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}while ((cp=rcp)!=null);
							
//							 //propagate the changes made in the current selected component to each lower refinement (TBD)
//							//ecr.createForwardChangeRecords();
//							EventBNamedCommentedComponentElement cp = component;
//							while ((cp=getRefinement(cp, components))!=null){
//								commitAssistant.applyChangesTo(cp);
//							}
//							
//							//delete the change record for the current selected component
//							commitAssistant.deleteChangeRecords();
//							
//							//generate all diagrams in the current selected component and all lower level refinements
//							GenerateAllHandler genAll = new GenerateAllHandler();
//							cp = component;
//							do {
//								genAll.generateAllDiagrams(cp, shell, EMFRodinDB.INSTANCE.getEditingDomain(), null);
//							}while ((cp=getRefinement(cp, components))!=null);
//							
//							//tidyup
//							commitAssistant.disposeChangeRecords();
						}
					}else{
						MessageDialog.openError(shell,
					    		  "Commit Changes", 
					    		  "There are lower level refinements with outstanding changes.\n"
					    		  + "Please commit or revert lower levels first"
					    			  );
					}
				}else
					MessageDialog.openError(shell,
				    		  "Commit Changes", 
				    		  "There are no changes to commit on this refinement.\n"
				    			  );
			}
		}
		return null;
	}



	private EventBNamedCommentedComponentElement getRefinement(EventBNamedCommentedComponentElement abs, List<EventBNamedCommentedComponentElement> components){
		for (EventBNamedCommentedComponentElement cp : components){
			if (isDirectRefinementOf(abs,cp)) {
				if (cp.eIsProxy()){		 //FIXME: needed because commit assistant unloads components in 'components'
					cp = (EventBNamedCommentedComponentElement) emfRodinDB.loadElement(cp.getURI());
				}
				return cp;
			}
		}
		return null;
	}
	
	private boolean hasLowerLevelChanges(EventBNamedCommentedComponentElement abs, List<EventBNamedCommentedComponentElement> components){	
		for (EventBNamedCommentedComponentElement cp : components){
			if (isRefinementOf(abs,cp) && RefactorPersistence.INSTANCE.hasChangesResource(EcoreUtil.getURI(cp))) {
				return true;
			}
		}
		return false;
	}
	
	private boolean isRefinementOf(EventBNamedCommentedComponentElement abs, EventBNamedCommentedComponentElement ref){
		if (abs.getReference().equals(ref.getReference())) return false;
		if (abs instanceof Machine && ref instanceof Machine && !((Machine)ref).getRefines().isEmpty()){
			if (isDirectRefinementOf(abs,ref)) return true;
			else return isRefinementOf(abs,((Machine)ref).getRefines().get(0));
		}else return false;
	}
	
	private boolean isDirectRefinementOf(EventBNamedCommentedComponentElement abs, EventBNamedCommentedComponentElement ref){
		if (abs instanceof Machine && ref instanceof Machine 
				&& ((Machine)ref).getRefines().size()>0){
			Machine refined = ((Machine)ref).getRefines().get(0);
			if (abs.getReference().equals(refined.getReference())) return true;
		}
		return false;
	}
	
}
