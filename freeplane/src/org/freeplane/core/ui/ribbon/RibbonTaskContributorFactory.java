package org.freeplane.core.ui.ribbon;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.IndexedTree.Node;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

public class RibbonTaskContributorFactory implements IRibbonContributorFactory {

	public IRibbonContributor getContributor(final Properties attributes) {
		return new IRibbonContributor() {
			private List<AbstractRibbonBand<?>> bands = new ArrayList<AbstractRibbonBand<?>>();
			
			public String getKey() {
				return attributes.getProperty("name");				
			}
			
			public void contribute(IndexedTree structure, IRibbonContributor parent) {
				bands.clear();
				String pathKey = (String) structure.getKeyByUserObject(this);
				IndexedTree.Node n = (Node) structure.get(pathKey);
				Enumeration<?> children = n.children();
				while(children.hasMoreElements()) {
					IndexedTree.Node node = (IndexedTree.Node) children.nextElement();
					((IRibbonContributor)node.getUserObject()).contribute(structure, this);
				}
				if(!bands.isEmpty()) {
					RibbonTask task = new RibbonTask(TextUtils.getText("ribbon."+getKey()), bands.toArray(new AbstractRibbonBand<?>[0]));
					if(parent != null) {
						parent.addChild(task);
					}
				}
			}

			public void addChild(Object child) {
				if(child instanceof AbstractRibbonBand) {
					bands.add((AbstractRibbonBand<?>) child);
				}
				
			}
		};
	}

}
