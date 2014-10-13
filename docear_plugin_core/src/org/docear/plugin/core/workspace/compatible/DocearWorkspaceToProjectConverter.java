package org.docear.plugin.core.workspace.compatible;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;

import org.apache.commons.io.IOExceptionWithCause;
import org.docear.plugin.core.workspace.controller.DocearConversionDescriptor;
import org.docear.plugin.core.workspace.creator.FolderTypeLibraryCreator;
import org.docear.plugin.core.workspace.creator.FolderTypeLiteratureRepositoryCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeIncomingCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeLiteratureAnnotationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeMyPublicationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeReferencesCreator;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.docear.plugin.core.workspace.node.FolderTypeLibraryNode;
import org.docear.plugin.core.workspace.node.FolderTypeLiteratureRepositoryNode;
import org.docear.plugin.core.workspace.node.LinkTypeIncomingNode;
import org.docear.plugin.core.workspace.node.LinkTypeLiteratureAnnotationsNode;
import org.docear.plugin.core.workspace.node.LinkTypeMyPublicationsNode;
import org.docear.plugin.core.workspace.node.LinkTypeReferencesNode;
import org.docear.plugin.core.workspace.node.LiteratureRepositoryPathNode;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.creator.ActionCreator;
import org.freeplane.plugin.workspace.creator.FolderCreator;
import org.freeplane.plugin.workspace.creator.FolderTypePhysicalCreator;
import org.freeplane.plugin.workspace.creator.FolderTypeVirtualCreator;
import org.freeplane.plugin.workspace.creator.LinkCreator;
import org.freeplane.plugin.workspace.creator.LinkTypeFileCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IResultProcessor;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.IWorkspaceProjectExtension;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;
import org.freeplane.plugin.workspace.nodes.ProjectRootNode;

public class DocearWorkspaceToProjectConverter {
	private static Object lock = new Object();

	private final ReadManager readManager;
	
	public final static int WSNODE_FOLDER = 1;
	public final static int WSNODE_LINK = 2;
	public final static int WSNODE_ACTION = 4;

	private FolderCreator folderCreator = null;
	private LinkCreator linkCreator = null;
	private ActionCreator actionCreator = null;
	private AWorkspaceNodeCreator projectRootCreator = null;
	
	private FolderTypeLibraryCreator folderTypeLibraryCreator;
	private AWorkspaceNodeCreator folderTypeLiteratureRepositoryCreator;
	
	private LinkTypeIncomingCreator linkTypeIncomingCreator;
	private LinkTypeLiteratureAnnotationsCreator linkTypeLiteratureAnnotationsCreator;
	private LinkTypeMyPublicationsCreator linkTypeMyPublicationsCreator;
	private LinkTypeReferencesCreator linkTypeReferencesCreator;
	
	private IResultProcessor resultProcessor;
		
	public DocearWorkspaceToProjectConverter() {
		this.readManager = new ReadManager();
		
		initReadManager();
	}
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	private void initReadManager() {
		readManager.addElementHandler("workspace", getProjectRootCreator());
		readManager.addElementHandler("folder", getFolderCreator());
		readManager.addElementHandler("link", getLinkCreator());
		//readManager.addElementHandler("action", getActionCreator());

		registerTypeCreator(WSNODE_FOLDER, "virtual", new FolderTypeVirtualCreator());
		registerTypeCreator(WSNODE_FOLDER, "physical", new FolderTypePhysicalCreator());
		registerTypeCreator(WSNODE_LINK, "file", new LinkTypeFileCreator());
		
		registerTypeCreator(WSNODE_FOLDER, FolderTypeLibraryCreator.FOLDER_TYPE_LIBRARY, getFolderTypeLibraryCreator());
		registerTypeCreator(WSNODE_FOLDER, FolderTypeLiteratureRepositoryCreator.FOLDER_TYPE_LITERATUREREPOSITORY, getFolderTypeLiteratureRepositoryCreator());
		
		registerTypeCreator(WSNODE_LINK, LinkTypeIncomingCreator.LINK_TYPE_INCOMING, getLinkTypeIncomingCreator());
		registerTypeCreator(WSNODE_LINK, LinkTypeLiteratureAnnotationsCreator.LINK_TYPE_LITERATUREANNOTATIONS, getLinkTypeLiteratureAnnotationsCreator());
		registerTypeCreator(WSNODE_LINK, LinkTypeMyPublicationsCreator.LINK_TYPE_MYPUBLICATIONS, getLinkTypeMyPublicationsCreator());
		registerTypeCreator(WSNODE_LINK, LinkTypeReferencesCreator.LINK_TYPE_REFERENCES, getLinkTypeReferencesCreator());
	
	}

	private AWorkspaceNodeCreator getProjectRootCreator() {
		if (this.projectRootCreator == null) {
			this.projectRootCreator = new AWorkspaceNodeCreator() {
				public AWorkspaceTreeNode getNode(XMLElement data) {
					ProjectRootNode node = new ProjectRootNode();
					String name = data.getAttribute("name", "project");
					String id = data.getAttribute("id", null);
					String version = DocearWorkspaceProject.CURRENT_PROJECT_VERSION.getVersionString();
					node.setName(name.replace("(Workspace)", "").trim());
					node.setProjectID(id);
					node.setVersion(version);
					return node;
				}
			};
			this.projectRootCreator.setResultProcessor(getDefaultResultProcessor());
		}
		return this.projectRootCreator;
	}

	private FolderCreator getFolderCreator() {
		if (this.folderCreator == null) {
			this.folderCreator = new FolderCreator();
			this.folderCreator.setResultProcessor(getDefaultResultProcessor());
		}
		return this.folderCreator;
	}

	private ActionCreator getActionCreator() {
		if (this.actionCreator == null) {
			this.actionCreator = new ActionCreator();
			this.actionCreator.setResultProcessor(getDefaultResultProcessor());
		}
		return this.actionCreator;
	}
	
	private LinkCreator getLinkCreator() {
		if (this.linkCreator == null) {
			this.linkCreator = new LinkCreator();
			this.linkCreator.setResultProcessor(getDefaultResultProcessor());
		}
		return this.linkCreator;
	}
	
	private FolderTypeLibraryCreator getFolderTypeLibraryCreator() {
		if (folderTypeLibraryCreator == null) {
			folderTypeLibraryCreator = new FolderTypeLibraryCreator();
		}
		return folderTypeLibraryCreator;
	}

	private AWorkspaceNodeCreator getFolderTypeLiteratureRepositoryCreator() {
		if (folderTypeLiteratureRepositoryCreator == null) {
			folderTypeLiteratureRepositoryCreator = new AWorkspaceNodeCreator() {
				@Override
				public AWorkspaceTreeNode getNode(XMLElement data) {
					String type = data.getAttribute("type", FolderTypeLiteratureRepositoryNode.TYPE);
					FolderTypeLiteratureRepositoryNode node = new FolderTypeLiteratureRepositoryNode(type);

					String path = data.getAttribute("path", null);

					boolean descending = Boolean.parseBoolean(data.getAttribute("orderDescending", "false"));
					node.orderDescending(descending);
					
					if (path != null && path.length() != 0) {
						LiteratureRepositoryPathNode pathNode = new LiteratureRepositoryPathNode();
						pathNode.setPath(URIUtils.createURI(path));
						node.addChildNode(pathNode);
					}

					return node;
				}
				
				public void endElement(final Object parent, final String tag, final Object node, final XMLElement lastBuiltElement) {
					super.endElement(parent, tag, node, lastBuiltElement);
					
					try {
						((AWorkspaceTreeNode) node).refresh();
					}
					catch(Exception e) {
						LogUtils.warn("Exception in org.docear.plugin.core.workspace.compatible.DocearWorkspaceToProjectConverter.getFolderTypeLiteratureRepositoryCreator().new AWorkspaceNodeCreator() {...}.endElement(parent, tag, node, lastBuiltElement): "+e.getMessage());
					}
				}
			};
		}
		return folderTypeLiteratureRepositoryCreator;
	}
	
	

	private LinkTypeIncomingCreator getLinkTypeIncomingCreator() {
		if (linkTypeIncomingCreator == null) {
			linkTypeIncomingCreator = new LinkTypeIncomingCreator();
		}
		return linkTypeIncomingCreator;
	}

	private LinkTypeLiteratureAnnotationsCreator getLinkTypeLiteratureAnnotationsCreator() {
		if (linkTypeLiteratureAnnotationsCreator == null) {
			linkTypeLiteratureAnnotationsCreator = new LinkTypeLiteratureAnnotationsCreator();
		}
		return linkTypeLiteratureAnnotationsCreator;
	}

	private LinkTypeMyPublicationsCreator getLinkTypeMyPublicationsCreator() {
		if (linkTypeMyPublicationsCreator == null) {
			linkTypeMyPublicationsCreator = new LinkTypeMyPublicationsCreator();
		}
		return linkTypeMyPublicationsCreator;
	}

	private LinkTypeReferencesCreator getLinkTypeReferencesCreator() {
		if (linkTypeReferencesCreator == null) {
			linkTypeReferencesCreator = new LinkTypeReferencesCreator();
		}
		return linkTypeReferencesCreator;
	}

	private void registerTypeCreator(final int nodeType, final String typeName, final AWorkspaceNodeCreator creator) {
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
		if(creator.getResultProcessor() == null) {
			creator.setResultProcessor(getDefaultResultProcessor());
		}

	}

	private void load(final URI xmlFile) throws MalformedURLException, XMLException, IOException {
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		InputStream stream = xmlFile.toURL().openStream();
		try {
			reader.load(new InputStreamReader(new BufferedInputStream(stream)));
		}
		finally {
			try {
				stream.close();
			}
			catch (IOException e) {
			}
		}
	}
	
	private IResultProcessor getDefaultResultProcessor() {
		if(this.resultProcessor == null) {
			this.resultProcessor = new ConverterResultProcessor();
		}
		return this.resultProcessor;
	}
	
	public static void convert(DocearConversionDescriptor descriptor) throws IOException {
		synchronized (lock ) {
			try {
				DocearWorkspaceToProjectConverter converter = new DocearWorkspaceToProjectConverter();
				DocearWorkspaceProject project = descriptor.getTargetProject();
				File profileHome = descriptor.getSelectedProfileHome();
				
				File workspaceSettings = new File(profileHome,"workspace.xml");
				if(workspaceSettings.exists()) {
					DocearConversionURLHandler.setTargetProject(project);
					converter.getDefaultResultProcessor().setProject(project);
					converter.load(workspaceSettings.toURI());
				}
				//delete old workspace settings, if selected
				if(descriptor.deleteOldSettings()) {
					if(!workspaceSettings.delete()) {
						LogUtils.info("could not delete: "+workspaceSettings);
					}
				}
			}
			catch (Exception e) {
				throw new IOExceptionWithCause(e);
			}
		}
	}
	
	private void convertNodeLink(AWorkspaceTreeNode node, AWorkspaceProject project) {
		if(node instanceof LinkTypeIncomingNode) {
			((LinkTypeIncomingNode) node).setLinkPath(getReplacementURI(((LinkTypeIncomingNode) node).getLinkURI(), project));
		}
		else if(node instanceof LinkTypeReferencesNode) {
			((LinkTypeReferencesNode) node).setLinkURI(getReplacementURI(((LinkTypeReferencesNode) node).getLinkURI(), project));
		}
		else if(node instanceof LinkTypeMyPublicationsNode) {
			((LinkTypeMyPublicationsNode) node).setLinkPath(getReplacementURI(((LinkTypeMyPublicationsNode) node).getLinkURI(), project));
		}
		else if(node instanceof LinkTypeLiteratureAnnotationsNode) {
			((LinkTypeLiteratureAnnotationsNode) node).setLinkPath(getReplacementURI(((LinkTypeLiteratureAnnotationsNode) node).getLinkURI(), project));
		}
		else if(node instanceof FolderTypeLiteratureRepositoryNode && node.getChildCount() > 0) { 
			((LiteratureRepositoryPathNode) node.getChildAt(0)).setPath(getReplacementURI(((LiteratureRepositoryPathNode) node.getChildAt(0)).getPath(), project));
		}
		else if(node instanceof LinkTypeFileNode) {
			((LinkTypeFileNode) node).setLinkURI(getReplacementURI(((LinkTypeFileNode) node).getLinkURI(), project));
		}
		else if(node instanceof FolderLinkNode) {
			((FolderLinkNode) node).setPath(getReplacementURI(((FolderLinkNode) node).getPath(), project));
		}
	}
	
	private URI getReplacementURI(URI uri, AWorkspaceProject project) {
		URI replacement = project.getRelativeURI(URIUtils.getAbsoluteURI(uri));
		if(replacement == null) {
			replacement = uri;
			LogUtils.info("could not convert URI: "+uri);
		}
		return replacement;
	}
	
	private class ConverterResultProcessor implements IResultProcessor {

		private AWorkspaceProject project;

		public AWorkspaceProject getProject() {
			return project;
		}

		public void setProject(AWorkspaceProject project) {
			this.project = project;
		}

		public void process(AWorkspaceTreeNode parent, AWorkspaceTreeNode node) {
			if(getProject() == null) {
				LogUtils.warn("Missing project container! cannot add node to a model.");
				return;
			}
			if(node instanceof ProjectRootNode) {
				getProject().getModel().setRoot(node);
				if(((ProjectRootNode) node).getProjectID() == null) {
					((ProjectRootNode) node).setProjectID(getProject().getProjectID());
				}
			}
			else {
				convertNodeLink(node, getProject());
				if(parent == null) {
					if (!getProject().getModel().containsNode(node.getKey())) {
						getProject().getModel().addNodeTo(node, parent);
					}
				}
				else {
					if (!parent.getModel().containsNode(node.getKey())) {
						parent.getModel().addNodeTo(node, parent);
					}
				}
				
				//add myFiles after a certain node type
				if(node instanceof LinkTypeReferencesNode) {
					((ProjectRootNode) parent.getModel().getRoot()).initiateMyFile(getProject());
				}
				else if(node instanceof FolderTypeLiteratureRepositoryNode) {
					project.addExtension(FolderTypeLiteratureRepositoryNode.class, (IWorkspaceProjectExtension) node);
					node.getChildAt(0).refresh();
				}
				else if(node instanceof FolderTypeLibraryNode) {
					//WorkspaceController.getCurrentModeExtension().getView().expandPath(node.getTreePath());
				}
			}
		}

	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
