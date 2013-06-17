package org.freeplane.core.ui.ribbon;

import java.net.URL;
import java.util.HashMap;

import javax.swing.JOptionPane;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.ModeController;
import org.pushingpixels.flamingo.api.ribbon.JRibbon;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuButton;


public class RibbonBuilder {
	private final HashMap<String, IRibbonContributorFactory> contributorFactories = new HashMap<String, IRibbonContributorFactory>();
	
	private final IndexedTree structure;
	private final RootContributor rootContributor;
	private final RibbonStructureReader reader;
	
	public RibbonBuilder(ModeController mode, JRibbon ribbon) {
		final JRibbonApplicationMenuButton applicationMenu = new JRibbonApplicationMenuButton(ribbon);
		structure = new IndexedTree(applicationMenu);
		this.rootContributor = new RootContributor(ribbon);
		reader = new RibbonStructureReader(this);
		registerContributorFactory("ribbon_task", new RibbonTaskContributorFactory());
	}
	
	public void add(IRibbonContributor contributor, String path, int position) {
		if(contributor == null || path == null) {
			throw new IllegalArgumentException("NULL");
		}
		synchronized (structure) {
			structure.addElement(path, contributor, position);
		}
	}
	
	public void registerContributorFactory(String key, IRibbonContributorFactory factory) {
		synchronized (contributorFactories) {
			this.contributorFactories.put(key, factory);
		}

	}
	
	public IRibbonContributorFactory getContributorFactory(String key) {
		return this.contributorFactories.get(key);
	}
	
	public void buildRibbon() {
		synchronized (structure) {
			rootContributor.contribute(structure, null);			
		}
	}
	
	public void updateRibbon(String xmlResource) {
		final URL xmlSource = ResourceController.getResourceController().getResource(xmlResource);
		if (xmlSource != null) {
			final boolean isUserDefined = xmlSource.getProtocol().equalsIgnoreCase("file");
			try{
			reader.loadStructure(xmlSource);
			}
			catch (RuntimeException e){
				if(isUserDefined){
					LogUtils.warn(e);
					String myMessage = TextUtils.format("ribbon_error", xmlSource.getPath(), e.getMessage());
					UITools.backOtherWindows();
					JOptionPane.showMessageDialog(UITools.getFrame(), myMessage, "Freeplane", JOptionPane.ERROR_MESSAGE);
					System.exit(-1);
				}
				throw e;
			}
		}
	}

	public boolean containsKey(String key) {
		synchronized (structure) {
			return structure.contains(key);
		}		
	}

	

}
