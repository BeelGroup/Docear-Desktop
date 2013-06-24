package org.freeplane.core.ui.ribbon;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.ribbon.AbstractRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonTask;

public class RibbonTaskContributorFactory implements IRibbonContributorFactory {

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {
			private List<AbstractRibbonBand<?>> bands = new ArrayList<AbstractRibbonBand<?>>();
			
			public String getKey() {
				return attributes.getProperty("name");				
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				bands.clear();
				Enumeration<?> children = context.getStructureNode(this).children();
				while(children.hasMoreElements()) {
					IndexedTree.Node node = (IndexedTree.Node) children.nextElement();
					((ARibbonContributor)node.getUserObject()).contribute(context, this);
				}
				if(!bands.isEmpty()) {
					RibbonTask task = new RibbonTask(TextUtils.getText("ribbon."+getKey()), bands.toArray(new AbstractRibbonBand<?>[0]));
					if(parent != null) {
						parent.addChild(task, null);
					}
				}
			}

			public void addChild(Object child, Object properties) {
				if(child instanceof AbstractRibbonBand) {
					bands.add((AbstractRibbonBand<?>) child);
				}
				
			}
		};
	}

}
