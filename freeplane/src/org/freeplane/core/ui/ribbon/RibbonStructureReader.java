package org.freeplane.core.ui.ribbon;

import java.io.BufferedInputStream;
import java.io.InputStreamReader;
import java.net.URL;

import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.FileUtils;
import org.freeplane.n3.nanoxml.XMLElement;

public class RibbonStructureReader {
	private final ReadManager readManager;
	private final RibbonBuilder builder;
	
	
	public RibbonStructureReader(RibbonBuilder ribbonBuilder) {
		readManager = new ReadManager();
		readManager.addElementHandler("menu_structure", new StructureCreator());
		readManager.addElementHandler("menu_category", new CategoryCreator());
		readManager.addElementHandler("ribbon_task", new RibbonTaskCreator());
//		readManager.addElementHandler("ribbon_band", new RibbonBandCreator());
		
		this.builder = ribbonBuilder;
	}

	private static class RibbonPath {
		static RibbonPath emptyPath() {
			final RibbonPath menuPath = new RibbonPath("");
			menuPath.key = "";
			return menuPath;
		}

		String parentKey;
		String key;

		RibbonPath(final String key) {
			parentKey = key;
		}

		void setKey(final String name) {
			key = name;
		}

		void setLastKeySection(final String name) {
			key = parentKey + '/' + name;
		}

		@Override
		public String toString() {
			return key;
		}
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
			return RibbonPath.emptyPath();
		}
	}
	
	private final class CategoryCreator implements IElementHandler {
		public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
			if (attributes == null) {
				return null;
			}
			
			final RibbonPath menuPath = new RibbonPath(parent.toString());
			String name = attributes.getAttribute("name", null);
			if("ribbon".equals(name)) {
    			menuPath.setLastKeySection(name);
    			return menuPath;
			}
			return null;
		}
	}
	
	private final class RibbonTaskCreator implements IElementHandler {
		public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
			if (attributes == null) {
				return null;
			}
			
			final RibbonPath menuPath = new RibbonPath(parent.toString());
			menuPath.setLastKeySection(attributes.getAttribute("name", null));
			IRibbonContributorFactory factory = builder.getContributorFactory(tag);
			if(factory != null && !builder.containsKey(menuPath.key)) {
				builder.add(factory.getContributor(attributes.getAttributes()), menuPath.parentKey, IndexedTree.AS_CHILD);
			}
			return menuPath;
		}
	}
	
	
}
