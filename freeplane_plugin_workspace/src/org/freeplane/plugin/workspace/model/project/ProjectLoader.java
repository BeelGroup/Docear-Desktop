package org.freeplane.plugin.workspace.model.project;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.commons.io.IOExceptionWithCause;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.creator.ActionCreator;
import org.freeplane.plugin.workspace.creator.FolderCreator;
import org.freeplane.plugin.workspace.creator.FolderTypePhysicalCreator;
import org.freeplane.plugin.workspace.creator.FolderTypeVirtualCreator;
import org.freeplane.plugin.workspace.creator.LinkCreator;
import org.freeplane.plugin.workspace.creator.LinkTypeFileCreator;
import org.freeplane.plugin.workspace.creator.ProjectRootCreator;
import org.freeplane.plugin.workspace.io.IProjectSettingsIOHandler;
import org.freeplane.plugin.workspace.io.xml.ProjectNodeWriter;
import org.freeplane.plugin.workspace.io.xml.ProjectSettingsWriter;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IResultProcessor;
import org.freeplane.plugin.workspace.nodes.FolderTypeProjectNode;

public class ProjectLoader implements IProjectSettingsIOHandler {
	private final ReadManager readManager;
	private final WriteManager writeManager;

	public final static int WSNODE_FOLDER = 1;
	public final static int WSNODE_LINK = 2;
	public final static int WSNODE_ACTION = 4;

	private FolderCreator folderCreator = null;
	private LinkCreator linkCreator = null;
	private ActionCreator actionCreator = null;
	private ProjectRootCreator projectRootCreator = null;
	
	private ProjectSettingsWriter projectWriter;
		
	//DOCEAR - required for backwards compatibility   
//	private final static String CONFIG_FILE_NAME = "workspace.xml";

	public ProjectLoader() {
		this.readManager = new ReadManager();
		this.writeManager = new WriteManager();
		this.projectWriter = new ProjectSettingsWriter(writeManager);
		
		initReadManager();
		initWriteManager();
	}
	
	private void initReadManager() {
		readManager.addElementHandler("workspace", getProjectRootCreator());
		readManager.addElementHandler("project", getProjectRootCreator());
		readManager.addElementHandler("folder", getFolderCreator());
		readManager.addElementHandler("link", getLinkCreator());
		readManager.addElementHandler("action", getActionCreator());

		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, "virtual", new FolderTypeVirtualCreator());
		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, "physical", new FolderTypePhysicalCreator());
		registerTypeCreator(ProjectLoader.WSNODE_LINK, "file", new LinkTypeFileCreator());
	}

	private void initWriteManager() {
		ProjectNodeWriter writer = new ProjectNodeWriter();
		writeManager.addElementWriter("project", writer);
		writeManager.addAttributeWriter("project", writer);

		writeManager.addElementWriter("folder", writer);
		writeManager.addAttributeWriter("folder", writer);

		writeManager.addElementWriter("link", writer);
		writeManager.addAttributeWriter("link", writer);
		
		writeManager.addElementWriter("action", writer);
		writeManager.addAttributeWriter("action", writer);
	}

	protected ProjectRootCreator getProjectRootCreator() {
		if (this.projectRootCreator == null) {
			this.projectRootCreator = new ProjectRootCreator();
		}
		return this.projectRootCreator;
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
			throw new IllegalArgumentException("not allowed argument for nodeType. Use only WorkspaceConfiguration.WSNODE_ACTION, WorkspaceConfiguration.WSNODE_FOLDER or WorkspaceConfiguration.WSNODE_LINK.");
		}
		}

	}

	private void load(final URI xmlFile, IResultProcessor processor) throws MalformedURLException, XMLException, IOException {
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		reader.load(new InputStreamReader(new BufferedInputStream(xmlFile.toURL().openStream())));
	}

	public synchronized void loadProject(AWorkspaceProject project) throws IOException {
		try {		
			
			File projectSettings = new File(WorkspaceController.resolveFile(project.getProjectDataPath()),"settings.xml");
			if(projectSettings.exists()) {
				this.load(projectSettings.toURI(), new DefaultResultProcessor(project));
			}
			else {
				DefaultProject prj = new DefaultProject();
				project.getModel().setRoot(prj);
				prj.prepare(project);
			}
		}
		catch (Exception e) {
			throw new IOExceptionWithCause(e);
		}
	}
	
	public void storeProject(Writer writer, AWorkspaceProject project) throws IOException {
		this.projectWriter.storeProject(writer, project);		
	}

	public void storeProject(AWorkspaceProject project) throws IOException {
		File outFile = WorkspaceController.resolveFile(project.getProjectDataPath());
		outFile = new File(outFile, "settings.xml");
		if(!outFile.exists()) {
			outFile.getParentFile().mkdirs();
			outFile.createNewFile();
		}
		Writer writer = new FileWriter(outFile);
		storeProject(writer, project);		
	}
	
	private class DefaultResultProcessor implements IResultProcessor {

		private final AWorkspaceProject project;

		public DefaultResultProcessor(AWorkspaceProject project) {
			this.project = project;
		}

		public void process(AWorkspaceTreeNode parent, AWorkspaceTreeNode node) {
			if(node instanceof FolderTypeProjectNode) {
				this.project.getModel().setRoot(node);
			}
			else {
				if (this.project.getModel().containsNode(node.getKey())) {
					this.project.getModel().addNodeTo(node, (AWorkspaceTreeNode) parent);			
				}
			}
		}

	}
}
