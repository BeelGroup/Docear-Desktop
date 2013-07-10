/**
 * author: Marcel Genzmehr
 * 18.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.tree.DefaultTreeCellRenderer;

import org.docear.plugin.core.IBibtexDatabase;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenu;
import org.freeplane.plugin.workspace.components.menu.WorkspacePopupMenuBuilder;
import org.freeplane.plugin.workspace.event.WorkspaceActionEvent;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

/**
 * 
 */
public class LinkTypeReferencesNode extends LinkTypeFileNode implements IBibtexDatabase {
	public static final String TYPE = "references";
	private static final String DEFAULT_REFERENCE_TEMPLATE = "/conf/reference_db.bib";
	private static final Icon DEFAULT_ICON = new ImageIcon(LinkTypeReferencesNode.class.getResource("/images/text-x-bibtex.png"));

	private static final long serialVersionUID = 1L;
	
	private WorkspacePopupMenu popupMenu = null;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public LinkTypeReferencesNode() {
		super(TYPE);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public boolean setIcons(DefaultTreeCellRenderer renderer) {
		renderer.setOpenIcon(DEFAULT_ICON);
		renderer.setClosedIcon(DEFAULT_ICON);
		renderer.setLeafIcon(DEFAULT_ICON);
		return true;
	}
	
	public void handleAction(WorkspaceActionEvent event) {
		if (event.getType() == WorkspaceActionEvent.MOUSE_RIGHT_CLICK) {			
			showPopup((Component) event.getBaggage(), event.getX(), event.getY());
		} 
		else if(event.getType() == WorkspaceActionEvent.MOUSE_LEFT_DBLCLICK) {
			//do nth.
		}
		else {
			super.handleAction(event);
		}
	}

	public AWorkspaceTreeNode clone() {
		LinkTypeReferencesNode node = new LinkTypeReferencesNode();
		return clone(node);
	}
	
	public void setLinkURI(URI uri) {
		super.setLinkURI(uri);
		if (uri != null) {
			createIfNeeded(uri);
		}
	}
	
	public void setName(String name) {
		super.setName("References");
	}
	
	public void initializePopup() {
		if (popupMenu == null) {
						
			popupMenu = new WorkspacePopupMenu();
			WorkspacePopupMenuBuilder.addActions(popupMenu, new String[] {
					"ChangeBibtexDatabaseAction",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.cut",
					"workspace.action.node.copy",
					"workspace.action.node.paste",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.rename",
					"workspace.action.node.remove",
					"workspace.action.file.delete",
					WorkspacePopupMenuBuilder.SEPARATOR,
					"workspace.action.node.refresh"	
			});
		}
		
	}	
	
	public WorkspacePopupMenu getContextMenu() {
		if (popupMenu == null) {
			initializePopup();
		}
		return popupMenu;
	}
	
	public void refresh() {
		//maybe do sth
	}
		
	private void createIfNeeded(URI uri) {
		try {
			File file = URIUtils.getAbsoluteFile(uri);
			if(file != null) {
				if (!file.getParentFile().exists()) {
					if(!file.getParentFile().mkdirs()) {
						return;
					}
				}
				if(!file.exists()) {
					if(!file.createNewFile()) {
						return;
					} else {
						copyDefaultsTo(file);
					}
				}
				this.setName(file.getName());
			}
		}
		catch (IOException e) {
			return;
		}
	}
	
	private void copyDefaultsTo(File config) throws FileNotFoundException, IOException {
		String referenceContent;
		referenceContent = getFileContent(DEFAULT_REFERENCE_TEMPLATE);
		
		DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(config)));
		out.write(referenceContent.getBytes());
		out.close();
	}
	
	private String getFileContent(String filename) throws IOException {
		InputStream in = getClass().getResourceAsStream(filename);
		Writer writer = new StringWriter();
		char[] buffer = new char[1024];

		try {
			Reader reader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			int n;

			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}

		}
		finally {
			in.close();
		}

		return writer.toString();
	}

	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	public URI getUri() {
		return this.getLinkURI();
	}

	@Override
	public void setUri(URI path) {
		setLinkURI(path);		
	}

}
