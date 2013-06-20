package org.freeplane.core.ui.ribbon;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.ribbon.RibbonBuilder.RibbonPath;
import org.freeplane.core.util.FileUtils;
import org.freeplane.n3.nanoxml.XMLElement;

public class RibbonStructureReader {
	private final ReadManager readManager;
	private final RibbonBuilder builder;
	
	
	public RibbonStructureReader(RibbonBuilder ribbonBuilder) {
		readManager = new ReadManager();
		readManager.addElementHandler("separator", new DefaultCreator());
		readManager.addElementHandler("menu_structure", new StructureCreator());
		readManager.addElementHandler("menu_category", new CategoryCreator());
		readManager.addElementHandler("ribbon_taskbar", new DefaultCreator());
		readManager.addElementHandler("ribbon_menu", new DefaultCreator());
		readManager.addElementHandler("primary_entry", new DefaultCreator());
		readManager.addElementHandler("entry_group", new DefaultCreator());
		readManager.addElementHandler("footer_entry", new DefaultCreator());
		readManager.addElementHandler("ribbon_menu", new DefaultCreator());
		readManager.addElementHandler("ribbon_task", new DefaultCreator());
		readManager.addElementHandler("ribbon_band", new DefaultCreator());
		readManager.addElementHandler("ribbon_action", new DefaultCreator());
		readManager.addElementHandler("ribbon_contributor", new RibbonContributorCreator());
		
		this.builder = ribbonBuilder;
	}
	
	public void loadStructure(final URL xmlSource) {
		InputStreamReader streamReader = null;
		try {
			streamReader = new InputStreamReader(new BufferedInputStream(xmlSource.openStream()));
			final TreeXmlReader reader = new TreeXmlReader(readManager);
			reader.load(streamReader);
		}
		catch (final Exception e) {
			throw new RuntimeException(e);
		}
        finally {
        	FileUtils.silentlyClose(streamReader);
        }
	}
	
	/******************************************************************
	 * NESTED TYPES
	 ******************************************************************/
	
	private final class StructureCreator implements IElementHandler {
		public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
			if (attributes == null) {
				return null;
			}
			return RibbonPath.emptyPath();
		}
	}
	
	private final class CategoryCreator implements IElementHandler {
		public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
			if (attributes == null) {
				return null;
			}
			RibbonPath path = new RibbonPath((RibbonPath) parent);
			String name = attributes.getAttribute("name", null);
			if("ribbon".equals(name)) {
				path.setName(name);
				return path;
			}
			return null;
		}
	}
	
	private final class DefaultCreator implements IElementHandler {
		public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
			if (attributes == null) {
				return null;
			}
			
			final RibbonPath menuPath = new RibbonPath((RibbonPath) parent);
			IRibbonContributorFactory factory = builder.getContributorFactory(tag);
			if(factory != null) {
				IRibbonContributor contributor = factory.getContributor(attributes.getAttributes());
				menuPath.setName(contributor.getKey());
				if(!builder.containsKey(menuPath.getKey())) {
					builder.add(contributor, menuPath.getParent(), IndexedTree.AS_CHILD);
				}
				return menuPath;
			}
			return null;
		}
	}
	
	private final class RibbonContributorCreator implements IElementHandler {
		public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
			if (attributes == null) {
				return null;
			}
			
			final RibbonPath menuPath = new RibbonPath((RibbonPath) parent);
			String name = attributes.getAttribute("name", null);
			if(name != null) {
				IRibbonContributorFactory factory = builder.getContributorFactory(name);
				if(factory != null) {
					IRibbonContributor contributor = factory.getContributor(attributes.getAttributes());
					menuPath.setName(contributor.getKey());
					if(!builder.containsKey(menuPath.getKey())) {
						builder.add(contributor, menuPath.getParent(), IndexedTree.AS_CHILD);
					}
					return menuPath;
				}
			}
			return null;
		}
	}
}
