package ac.soton.eventb.emf.diagrams.refactor;

import java.io.IOException;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;


public class Recorder {
	
	protected Recorder(EventBNamedCommentedComponentElement component) throws IOException {
		super();
	}
	
	/** 
	 * This version is a dummy with refactoring disabled
	 * it always returns null
	 * 
	 */
	public static Recorder getNewRecorder(EventBNamedCommentedComponentElement component){
			return null;
	}
	
	/**
	 * This version is a dummy with refactoring disabled
	 * Does nothing
	 */
	public int resumeRecording(){
		return 3;
	}
	
	/**
	 * This version is a dummy with refactoring disabled
	 * Does nothing
	 */
	public void endRecording() {
		return;
	}

	/**
	 * This version is a dummy with refactoring disabled
	 * Does nothing
	 */
	public void disposeChangeRecorder() {
		return;
	}	
	
	/**
	 * This version is a dummy with refactoring disabled
	 * Does nothing
	 */
	public void saveChanges() {
		return;

	}


}
