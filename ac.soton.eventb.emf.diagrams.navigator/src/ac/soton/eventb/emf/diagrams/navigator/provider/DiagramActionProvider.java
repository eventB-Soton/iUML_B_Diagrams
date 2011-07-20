/**
 * Copyright (c) 2011 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.navigator.provider;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.CommonActionProvider;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.eclipse.ui.navigator.ICommonMenuConstants;

import ac.soton.eventb.emf.diagrams.navigator.action.OpenDiagramAction;

/**
 * Diagram action provider for the navigator.
 * 
 * @author vitaly
 *
 */
public class DiagramActionProvider extends CommonActionProvider {

	private Action dblClickAction;

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		dblClickAction = new OpenDiagramAction(aSite, "&Open");
		aSite.getStructuredViewer().addSelectionChangedListener((ISelectionChangedListener) dblClickAction);
	}

	@Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, dblClickAction);
		actionBars.updateActionBars();
	}

	@Override
	public void fillContextMenu(IMenuManager menu) {
		super.fillContextMenu(menu);
		menu.appendToGroup(ICommonMenuConstants.GROUP_OPEN, dblClickAction);
	}

}
