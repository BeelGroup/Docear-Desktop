package org.freeplane.plugin.workspace.controller;

import java.io.BufferedInputStream;
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
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.config.IConfigurationInfo;
import org.freeplane.plugin.workspace.creator.ActionCreator;
import org.freeplane.plugin.workspace.creator.FolderCreator;
import org.freeplane.plugin.workspace.creator.FolderTypePhysicalCreator;
import org.freeplane.plugin.workspace.creator.FolderTypeVirtualCreator;
import org.freeplane.plugin.workspace.creator.LinkCreator;
import org.freeplane.plugin.workspace.creator.LinkTypeFileCreator;
import org.freeplane.plugin.workspace.creator.WorkspaceRootCreator;
import org.freeplane.plugin.workspace.io.xml.ConfigurationWriter;
import org.freeplane.plugin.workspace.io.xml.WorkspaceNodeWriter;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.nodes.DefaultFileNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;

public class WorkspaceConfiguration {
	final private ReadManager readManager;
	final private WriteManager writeManager;

	public final static int WSNODE_FOLDER = 1;
	public final static int WSNODE_LINK = 2;
	public final static int WSNODE_ACTION = 4;
	
	private final static String DEFAULT_CONFIG_FILE_NAME = "workspace_default.xml";
	private URL DEFAULT_CONFIG_TEMPLATE_URL = WorkspaceConfiguration.class.getResource("/conf/"+DEFAULT_CONFIG_FILE_NAME);
	//private final static String DEFAULT_CONFIG_FILE_NAME_DOCEAR = "workspace_default_docear.xml";
	public final static String CONFIG_FILE_NAME = "workspace.xml";

	private final static String PLACEHOLDER_PROFILENAME = "@@PROFILENAME@@";

	private FolderCreator folderCreator = null;
	private LinkCreator linkCreator = null;
	private ActionCreator actionCreator = null;
	private WorkspaceRootCreator workspaceRootCreator = null;
	private IConfigurationInfo configurationInfo;

	private ConfigurationWriter configWriter;	

	public WorkspaceConfiguration() {
		this.readManager = new ReadManager();
		this.writeManager = new WriteManager();
		this.configWriter = new ConfigurationWriter(writeManager);
		
//		WorkspaceController.getController().getNodeTypeIconManager().addNodeTypeIconHandler(LinkTypeFileNode.class, new LinkTypeFileIconHandler());
//		WorkspaceController.getController().getNodeTypeIconManager().addNodeTypeIconHandler(DefaultFileNode.class, new DefaultFileNodeIconHandler());
		initReadManager();
		initWriteManager();
	}

	public IConfigurationInfo getConfigurationInfo() {
		return this.configurationInfo;
	}
	
	public void setDefaultConfigTemplateUrl(URL templateUrl) {
		if(templateUrl == null) {
			return ;
		}
		this.DEFAULT_CONFIG_TEMPLATE_URL = templateUrl;
	}
	
	private void initReadManager() {
		readManager.addElementHandler("workspace", getWorkspaceRootCreator());
		readManager.addElementHandler("folder", getFolderCreator());
		readManager.addElementHandler("link", getLinkCreator());
		readManager.addElementHandler("action", getActionCreator());

		registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, "virtual", new FolderTypeVirtualCreator());
		registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, "physical", new FolderTypePhysicalCreator());
		registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, "file", new LinkTypeFileCreator());
	}

	private void initWriteManager() {
		WorkspaceNodeWriter writer = new WorkspaceNodeWriter();
		writeManager.addElementWriter("workspace", writer);
		writeManager.addAttributeWriter("workspace", writer);

		writeManager.addElementWriter("folder", writer);
		writeManager.addAttributeWriter("folder", writer);

		writeManager.addElementWriter("link", writer);
		writeManager.addAttributeWriter("link", writer);
		
		writeManager.addElementWriter("action", writer);
		writeManager.addAttributeWriter("action", writer);
	}

	private WorkspaceRootCreator getWorkspaceRootCreator() {
		if (this.workspaceRootCreator == null) {
			this.workspaceRootCreator = new WorkspaceRootCreator(this);
		}
		return this.workspaceRootCreator;
	}

	private FolderCreator getFolderCreator() {
		if (this.folderCreator == null) {
			this.folderCreator = new FolderCreator();
		}
		return this.folderCreator;
	}

	private ActionCreator getActionCreator() {
		if (this.actionCreator == null) {
			this.actionCreator = new ActionCreator();
		}
		return this.actionCreator;
	}
	
	private LinkCreator getLinkCreator() {
		if (this.linkCreator == null) {
			this.linkCreator = new LinkCreator();
		}
		return this.linkCreator;
	}

	public void registerTypeCreator(final int nodeType, final String typeName, final AWorkspaceNodeCreator creator) {
		if (typeName == null || typeName.trim().length() <= 0)
			return;
		switch (nodeType) {
		case WSNODE_FOLDER: {
			getFolderCreator().addTypeCreator(typeName, creator);
			break;
		}
		case WSNODE_LINK: {
			getLinkCreator().addTypeCreator(typeName, creator);
			break;
		}
		case WSNODE_ACTION: {
			getActionCreator().addTypeCreator(typeName, creator);
			break;
		}
		default: {
			throw new IllegalArgumentException(
					"not allowed argument for nodeType. Use only WorkspaceConfiguration.WSNODE_ACTION, WorkspaceConfiguration.WSNODE_FOLDER or WorkspaceConfiguration.WSNODE_LINK.");
		}
		}

	}

	private void load(final URL xmlFile) {
		LogUtils.info("WORKSPACE: load Config from XML: " + xmlFile);
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		try {
			reader.load(new InputStreamReader(new BufferedInputStream(xmlFile.openStream())));
		}
		catch (final IOException e) {
			throw new RuntimeException(e);
		}
		catch (final XMLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @param node
	 */
	public void setConfigurationInfo(IConfigurationInfo info) {
		this.configurationInfo = info;
	}

	public void saveConfiguration(Writer writer) {
//		try {
//			this.configWriter.writeConfigurationAsXml(writer);
//		}
//		catch (final IOException e) {
//			LogUtils.severe(e);
//		}
	}

}
