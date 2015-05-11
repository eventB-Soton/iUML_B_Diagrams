/**
 * Copyright (c) 2011 University of Southampton.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package ac.soton.eventb.emf.diagrams.navigator;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.rodinp.core.IElementChangedListener;
import org.rodinp.core.RodinCore;

import ac.soton.eventb.emf.diagrams.navigator.provider.IDiagramProvider;
import ac.soton.eventb.emf.diagrams.navigator.refactor.ChangesUpdaterListener;

/**
 * The activator class controls the plug-in life cycle
 */
public class DiagramsNavigatorExtensionPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ac.soton.eventb.emf.diagrams.navigator"; //$NON-NLS-1$

	// The shared instance
	private static DiagramsNavigatorExtensionPlugin plugin;

	// diagram providers extension point ID
	private static final String DIAGRAM_PROVIDERS_EXTENSION_ID = "ac.soton.eventb.emf.diagrams.navigator.diagramProviders";
	
	// diagram provider registry
	private static final Map<String, IDiagramProvider> diagramProviderRegistry = new HashMap<String, IDiagramProvider>();
	
	private static final IElementChangedListener diagramUpdater = new DiagramUpdaterListener();
	private static final IElementChangedListener changesUpdater = new ChangesUpdaterListener();
	
	/**
	 * The constructor
	 */
	public DiagramsNavigatorExtensionPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		registerDiagramProviders();
		//this listener will update diagrams when components in Rodin database are renamed or deleted
		RodinCore.addElementChangedListener(diagramUpdater);
		RodinCore.addElementChangedListener(changesUpdater);
	}

	/**
	 * Registers diagram providers from all client extensions.
	 */
	private void registerDiagramProviders() {
		IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(DIAGRAM_PROVIDERS_EXTENSION_ID);
		for (IConfigurationElement element : config) {
			try {
				String type = element.getAttribute("type");
				Object extension = element.createExecutableExtension("class");
				if (extension instanceof IDiagramProvider) {
					diagramProviderRegistry.put(type, (IDiagramProvider) extension);
				}
			} catch (CoreException e) {
				getLog().log(new Status(Status.ERROR, PLUGIN_ID, "Failed to create executable extension of " + element.getAttribute("class"), e));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		RodinCore.removeElementChangedListener(diagramUpdater);
		RodinCore.removeElementChangedListener(changesUpdater);
		diagramProviderRegistry.clear();
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DiagramsNavigatorExtensionPlugin getDefault() {
		return plugin;
	}

	/**
	 * @return diagram provider registry
	 */
	public Map<String, IDiagramProvider> getDiagramProviderRegistry() {
		return diagramProviderRegistry;
	}

}
