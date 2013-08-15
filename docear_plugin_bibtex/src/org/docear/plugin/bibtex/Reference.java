package org.docear.plugin.bibtex;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.GUIGlobals;
import net.sf.jabref.gui.FileListTableModel;

import org.docear.plugin.bibtex.jabref.JabRefAttributes;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.plugin.workspace.URIUtils;

public class Reference {
	public class Item {
		private String name;
		private String value;

		public Item(String name, String value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public String getValue() {
			return value;
		}
	}

	private final Item key;
	private final ArrayList<Item> attributes;
	private Set<URI> uris = new HashSet<URI>();
	private URL url = null;

	public Reference(BasePanel basePanel, BibtexEntry entry) {
		JabRefAttributes jabRefAttributes = ReferencesController.getController().getJabRefAttributes();

		attributes = new ArrayList<Reference.Item>();

		if (entry.getCiteKey() == null || entry.getCiteKey().trim().length() == 0) {
			jabRefAttributes.generateBibtexEntry(entry);
		}

		this.key = new Item(jabRefAttributes.getKeyAttribute(), entry.getCiteKey());
		for (Entry<String, String> valueAttributes : jabRefAttributes.getValueAttributes().entrySet()) {
			attributes.add(new Item(valueAttributes.getKey(), entry.getField(valueAttributes.getValue())));
		}

		String fileField = entry.getField(GUIGlobals.FILE_FIELD);
		if (fileField != null) {
			FileListTableModel model = new FileListTableModel();
			model.setContent(fileField);
			for (int i = 0; i < model.getRowCount(); i++) {
				String link = model.getEntry(i).getLink();
				File f = new File(link);
				//DOCEAR - todo: check if the path is absolute
				if(!f.isAbsolute()) {
					f = new File(basePanel.getFile().getParentFile(), link);
				}
				try {
					f = f.getCanonicalFile();
				} catch (IOException e) {
					LogUtils.warn("Reference.Reference() exception for file " + f.toString() + ": " + e.getMessage());
				}
				uris.add(f.toURI());
			}
		}

		try {
			String url = entry.getField("url");
			if (url != null && url.trim().length() > 0) {
				this.url = new URL(url);
			}
		} catch (MalformedURLException e) {
			// LogUtils.info("org.docear.plugin.bibtex.Reference(): "+e.getMessage());
		}
	}

	public Item getKey() {
		return key;
	}

	public ArrayList<Item> getAttributes() {
		return attributes;
	}

	public Set<URI> getUris() {
		return uris;
	}

	public void addUri(URI uri) {
		this.uris.add(uri);
	}

	public URL getUrl() {
		return this.url;
	}

	public boolean containsLink(URI nodeLink) {
		File file = URIUtils.getAbsoluteFile(nodeLink);

		if (file != null) {
			String name = file.getName();
			for (URI uri : getUris()) {
				if (name.equalsIgnoreCase(new File(uri).getName())) {
					return true;
				}
			}
		} else {
			try {
				if (this.url.toExternalForm().equals(nodeLink.toURL().toExternalForm())) {
					return true;
				}
			} catch (MalformedURLException e) {
				LogUtils.info(e.getMessage());
			}
		}

		return false;
	}

	public boolean containsFile(File file) {
		if (file != null) {
			String name = file.getName();
			for (URI uri : getUris()) {
				if (name.equals(new File(uri).getName())) {
					return true;
				}
			}
		}
		return false;
	}
	
	public static URI getBibTeXRelativeURI(URI uri, DocearWorkspaceProject project) {
		File bibPath = URIUtils.getAbsoluteFile(project.getBibtexDatabase());
		URI relativeBibURI = LinkController.toLinkTypeDependantURI(bibPath, URIUtils.getAbsoluteFile(uri), LinkController.LINK_RELATIVE_TO_MINDMAP);
//		if(Compat.isWindowsOS() && relativeBibURI.getPath().startsWith("//")) {
//			new File(relativeBibURI).getPath().replace(File.separator, File.separator+File.separator)/*+File.separator+File.separator+"Example PDFs"*/));
//		}
//		else {
//			replaceMapping.put("@LITERATURE_BIB_DEMO@", cutLastSlash(relativeBibURI.getPath().replace(":", "\\:")/*+"/Example PDFs"*/));
//		}
		return relativeBibURI;
	}
}
