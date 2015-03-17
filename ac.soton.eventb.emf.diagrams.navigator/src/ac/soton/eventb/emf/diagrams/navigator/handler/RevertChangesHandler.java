/**
 * Copyright (c) 2012 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.navigator.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eventb.core.IMachineRoot;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.machine.Machine;
import org.eventb.emf.persistence.EMFRodinDB;

import ac.soton.eventb.emf.diagrams.navigator.refactor.RevertAssistant;

/**
 * Commit changes handler.
 * This handler commits the recorded changes made to the selected machine.
 * This consists of:
 * a) propagating the changes made in the current selected component to each lower refinement (TBD)
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
public class RevertChangesHandler extends AbstractHandler {
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {	
		Shell shell = HandlerUtil.getActiveShell(event);
		ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		if (selection instanceof IStructuredSelection) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IMachineRoot) {
				EventBElement machine = EMFRodinDB.INSTANCE.loadEventBComponent((IMachineRoot)element);

				RevertAssistant assistant = new RevertAssistant((Machine)machine);		
				
				// Check that there is a change record for this refinement level
				if (assistant.hasChanges()){
				
					boolean ok = MessageDialog.openConfirm(shell,
				    		  "Revert Changes", 
				    		  "WARNING!: This will roll back all the changes accumulated for this refinement level and delete the change record.\n"
				    			  );
					if (ok){
						 //roll back the accumulated changes
						assistant.revertChangeRecords();
						
						 //delete the change record for the current selected component
						assistant.deleteChangeRecords();
						
						//tidyup
						assistant.disposeChangeRecords();
					}
				}else
					MessageDialog.openError(shell,
				    		  "Revert Changes", 
				    		  "There are no changes to revert on this refinement.\n"
				    			  );
			}
		}
		return null;
	}
}
