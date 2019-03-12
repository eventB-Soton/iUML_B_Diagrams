/**
 * Copyright (c) 2014-2019 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package ac.soton.eventb.emf.diagrams.sheet;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.gmf.runtime.diagram.ui.properties.sections.AbstractModelerPropertySection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPart;
import org.eventb.emf.core.Annotation;
import org.eventb.emf.core.CorePackage;
import org.eventb.emf.core.EventBElement;
import org.eventb.emf.core.EventBObject;

import ac.soton.eventb.emf.diagrams.util.custom.DiagramUtils;

/**
 * Abstract property section for a feature of iUML-B diagrams.
 * 
 *
 */
public abstract class AbstractIumlbPropertySection extends
		AbstractModelerPropertySection {

	private static final String DIAGRAMS_TRANSLATION_TARGET = "ac.soton.diagrams.translationTarget";
	
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
	 * @since 2.4
	 */
	protected EventBObject getTranslationTarget() {
		EventBObject container = owner.getContaining(CorePackage.Literals.PROJECT);
		if (container == null){
			container = owner.getContaining(CorePackage.Literals.EVENT_BNAMED_COMMENTED_COMPONENT_ELEMENT);
		}
		if (container == null) {
			EObject root = EcoreUtil.getRootContainer(owner);
			if (root instanceof EventBElement) {
				//EventBElement root = getRootElement(owner);
				Annotation annotation = ((EventBElement)root).getAnnotation(DIAGRAMS_TRANSLATION_TARGET);
				if (annotation!=null){
					EList<EObject> references = annotation.getReferences();
					if (!references.isEmpty() && references.get(0) instanceof EventBObject) {
						container = (EventBObject) references.get(0);
					}
				}
			}
		}
		return container;
	}

}