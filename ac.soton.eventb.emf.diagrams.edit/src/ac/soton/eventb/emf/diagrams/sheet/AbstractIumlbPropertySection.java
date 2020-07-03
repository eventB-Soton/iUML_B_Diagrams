/**
 * Copyright (c) 2014-2019 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.sheet;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.AbstractModelerPropertySection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBNamedCommentedComponentElement;
import org.eventb.emf.core.EventBObject;
import org.eventb.emf.core.context.Context;
import org.eventb.emf.core.machine.Machine;

import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

/**
 * Abstract property section for a feature of iUML-B diagrams.
 * 
 *
 */
public abstract class AbstractIumlbPropertySection extends
		AbstractModelerPropertySection {
	
	/**
	 * The currently selected EventBElement or the first object in the selection
	 * when multiple objects are selected.
	 */
	protected EventBElement owner;
	
	
	@Override
	protected EObject unwrap(Object object) {
		return DiagramUtils.unwrap(object);
	}

	/**
	 * Get section label width.
	 * Standard implementation uses 100.
	 * Subclasses may override.
	 * 
	 * @param composite
	 * @return label width
	 */
	
	protected int getPropertyLabelWidth(Composite composite) {
		return 100;
	}

	/**
	 * Get label of a section.
	 * 
	 * @return the label of section
	 */
	protected abstract String getLabelText();

	/**
	 * Get feature of a section.
	 * 
	 * @return the feature of section object
	 */
	protected abstract EStructuralFeature getFeature();

	
	@Override
	public void setInput(final IWorkbenchPart part, final ISelection selection) {
		super.setInput(part, selection);
		if (eObject instanceof EventBElement){
			owner = (EventBElement)eObject;
		}
	}

	/**
	 * This gets the Event-B component to be used as the target for translation.
	 * This component can be used to select Event-B elements to reference.
	 * 
	 * If the 'owner' is contained in a project, the project is returned
	 * Otherwise, If the 'owner' is contained in a Event-B component, the component is returned
	 * Otherwise, If the 'owner' has an "ac.soton.diagrams.translationTarget" annotation that references an EventBObject,
	 * 	that referenced object is returned.
	 * 
	 * @return
	 * @since 3.0
	 */
	protected EventBObject getTranslationTarget() {
		return DiagramUtils.getTranslationTarget(owner);
	}

	/**
	 * This gets all Event-B components that are in scope of the translation target
	 * i.e. the target itself as well as the closure of all seen or extended contexts 
	 * 
	 * @since 3.0
	 */
	protected List<EventBNamedCommentedComponentElement> getComponentsInScope() {
		return getComponentsInScope(getTranslationTarget());
	}
	
	/**
	 * This gets all Event-B components that are in scope of the one passed as input
	 * i.e. the input itself as well as the closure of all seen or extended contexts 
	 * 
	 * @since 3.0
	 */
	protected List<EventBNamedCommentedComponentElement> getComponentsInScope(EventBObject eventBObject) {
		List<EventBNamedCommentedComponentElement> list =  new ArrayList<EventBNamedCommentedComponentElement>() ;
		if (eventBObject instanceof Machine){
			Machine m = ((Machine)eventBObject);
			list.add(m);
			for (Context c : m.getSees()){
				list.addAll(getComponentsInScope(c));
			}			
		}else if (eventBObject instanceof Context){
			Context c = ((Context)eventBObject);
			list.add(c);
			for (Context x : c.getExtends()){
				list.addAll(getComponentsInScope(x));
			}
		}
		return list;
	}
	
}