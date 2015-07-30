package ac.soton.eventb.emf.diagrams.navigator.refactor;

import java.io.IOException;
import java.util.Collections;

import org.eclipse.emf.ecore.change.ChangeDescription;
import org.eclipse.emf.ecore.change.util.ChangeRecorder;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.command.ChangeCommand;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;

public class RevertAssistant extends RefactorAssistant {

	public RevertAssistant(EventBNamedCommentedComponentElement component) {
		super(component);
	}
		
	/**
	 * Reverts the Change Records from the file system by applying them in reverse to the recorded resource
	 * Then saves the resource and deletes the change records so that they cannot be used again
	 * 
	 * 
	 */
	public void revertChangeRecords() {
		if (!hasChanges()) return;
		ApplyReverseCommand command = new ApplyReverseCommand(chRes, changes);
		ed.getCommandStack().execute(command);
		command.dispose();
		try {
			res.save(Collections.EMPTY_MAP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	////////////////////////////// COMMANDS //////////////////////////////////
	
	protected class ApplyReverseCommand extends ChangeCommand {
		ChangeDescription changes;
		ApplyReverseCommand(Resource chRes, ChangeDescription changes){
			super(new ChangeRecorder(chRes).endRecording()); 	//a change recorder to record what the change recorder does!
			this.changes = changes;
		}
		@Override
		public void doExecute(){
			changes.applyAndReverse();
		}
	}
	
}
