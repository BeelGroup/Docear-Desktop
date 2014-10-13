package org.docear.plugin.core.workspace.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOExceptionWithCause;
import org.docear.plugin.core.io.ReplacingInputStream;
import org.docear.plugin.core.workspace.actions.DocearProjectSettings;
import org.docear.plugin.core.workspace.creator.FolderTypeLibraryCreator;
import org.docear.plugin.core.workspace.creator.FolderTypeLiteratureRepositoryCreator;
import org.docear.plugin.core.workspace.creator.FolderTypeLiteratureRepositoryPathCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeIncomingCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeLiteratureAnnotationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeMyPublicationsCreator;
import org.docear.plugin.core.workspace.creator.LinkTypeReferencesCreator;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.docear.plugin.core.workspace.node.FolderTypeLibraryNode;
import org.docear.plugin.core.workspace.node.FolderTypeLiteratureRepositoryNode;
import org.docear.plugin.core.workspace.node.LinkTypeIncomingNode;
import org.docear.plugin.core.workspace.node.LinkTypeLiteratureAnnotationsNode;
import org.docear.plugin.core.workspace.node.LinkTypeReferencesNode;
import org.docear.plugin.core.workspace.node.LiteratureRepositoryPathNode;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.model.AWorkspaceNodeCreator;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IResultProcessor;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.IWorkspaceProjectExtension;
import org.freeplane.plugin.workspace.model.project.ProjectLoader;
import org.freeplane.plugin.workspace.nodes.FolderLinkNode;
import org.freeplane.plugin.workspace.nodes.FolderTypeMyFilesNode;
import org.freeplane.plugin.workspace.nodes.LinkTypeFileNode;
import org.freeplane.plugin.workspace.nodes.ProjectRootNode;

public class DocearProjectLoader extends ProjectLoader {	
	private FolderTypeLibraryCreator folderTypeLibraryCreator;
	private FolderTypeLiteratureRepositoryCreator folderTypeLiteratureRepositoryCreator;
	private FolderTypeLiteratureRepositoryPathCreator folderTypeLiteratureRepositoryPathCreator;
	
	private LinkTypeIncomingCreator linkTypeIncomingCreator;
	private LinkTypeLiteratureAnnotationsCreator linkTypeLiteratureAnnotationsCreator;
	private LinkTypeMyPublicationsCreator linkTypeMyPublicationsCreator;
	private LinkTypeReferencesCreator linkTypeReferencesCreator;
	private IResultProcessor resultProcessor;
	

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DocearProjectLoader() {
		super();	
		initDocearReadManager();	
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	private void initDocearReadManager() {		
		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, FolderTypeLibraryCreator.FOLDER_TYPE_LIBRARY, getFolderTypeLibraryCreator());
		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, FolderTypeLiteratureRepositoryCreator.FOLDER_TYPE_LITERATUREREPOSITORY, getFolderTypeLiteratureRepositoryCreator());
		registerTypeCreator(ProjectLoader.WSNODE_FOLDER, FolderTypeLiteratureRepositoryPathCreator.REPOSITORY_PATH_TYPE, getFolderTypeLiteratureRepositoryPathCreator());
		
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeIncomingCreator.LINK_TYPE_INCOMING, getLinkTypeIncomingCreator());
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeLiteratureAnnotationsCreator.LINK_TYPE_LITERATUREANNOTATIONS, getLinkTypeLiteratureAnnotationsCreator());
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeMyPublicationsCreator.LINK_TYPE_MYPUBLICATIONS, getLinkTypeMyPublicationsCreator());
		registerTypeCreator(ProjectLoader.WSNODE_LINK, LinkTypeReferencesCreator.LINK_TYPE_REFERENCES, getLinkTypeReferencesCreator());
	}
	
	private AWorkspaceNodeCreator getFolderTypeLiteratureRepositoryPathCreator() {
		if (folderTypeLiteratureRepositoryPathCreator == null) {
			folderTypeLiteratureRepositoryPathCreator = new FolderTypeLiteratureRepositoryPathCreator();
		}
		return folderTypeLiteratureRepositoryPathCreator;
	}
	
	private FolderTypeLibraryCreator getFolderTypeLibraryCreator() {
		if (folderTypeLibraryCreator == null) {
			folderTypeLibraryCreator = new FolderTypeLibraryCreator();
		}
		return folderTypeLibraryCreator;
	}

	private FolderTypeLiteratureRepositoryCreator getFolderTypeLiteratureRepositoryCreator() {
		if (folderTypeLiteratureRepositoryCreator == null) {
			folderTypeLiteratureRepositoryCreator = new FolderTypeLiteratureRepositoryCreator();
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
	
	public synchronized LOAD_RETURN_TYPE loadProject(AWorkspaceProject project) throws IOException {
		long time = System.currentTimeMillis();
		try {
			File projectSettings = new File(URIUtils.getAbsoluteFile(project.getProjectDataPath()),"settings.xml");
			if(projectSettings.exists()) {
				getDefaultResultProcessor().setProject(project);
				load(projectSettings.toURI());
				project.setLoaded();
				return LOAD_RETURN_TYPE.EXISTING_PROJECT;
			}
			else {
				createDefaultProject((DocearWorkspaceProject)project);
				((ProjectRootNode)project.getModel().getRoot()).setVersion(DocearWorkspaceProject.CURRENT_PROJECT_VERSION.getVersionString());
				project.setLoaded();
				return LOAD_RETURN_TYPE.NEW_PROJECT;
			}
		}
		catch (Exception e) {
			throw new IOExceptionWithCause(e);
		}
		finally {
			LogUtils.info("project "+project.getProjectName()+" loaded in: "+(System.currentTimeMillis()-time));
		}
	}
	
	public IResultProcessor getDefaultResultProcessor() {
		if(this.resultProcessor == null) {
			this.resultProcessor = new DocearResultProcessor();
		}
		return this.resultProcessor;
	}
	
	private void createDefaultProject(DocearWorkspaceProject project) {
		File home = URIUtils.getFile(project.getProjectHome());
		if(!home.exists()) {
			home.mkdirs();
		}
		DocearProjectSettings settings = (DocearProjectSettings) project.getExtensions(DocearProjectSettings.class);
		ProjectRootNode root = new ProjectRootNode();
		root.setProjectID(project.getProjectID());				
		root.setModel(project.getModel());
		root.setName(home.getName());
		
		project.getModel().setRoot(root);
		
		
		File _dataInfoFile = new File(URIUtils.getFile(project.getProjectDataPath()).getParentFile(), "!!!info.txt");
		if(!_dataInfoFile.exists()) {
			createAndCopy(_dataInfoFile, "/conf/!!!info.txt");
		}
		
		// create and load all default nodes
		FolderTypeLibraryNode libNode = new FolderTypeLibraryNode();
		libNode.setName(TextUtils.getText(libNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
		project.getModel().addNodeTo(libNode, root);
		
		FolderLinkNode draftsNode = new FolderLinkNode();
		File draftsFile = new File(URIUtils.getAbsoluteFile(project.getProjectLibraryPath()).getParentFile(), "My Drafts");
		draftsFile.mkdirs();
		URI draftsPath = project.getRelativeURI(draftsFile.toURI());
		draftsNode.setPath(draftsPath);
		draftsNode.setName(TextUtils.getText(draftsNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".drafts.label" ));
		project.getModel().addNodeTo(draftsNode, root);
		
		URI libPath = project.getRelativeURI(project.getProjectLibraryPath());
		
//		LinkTypeIncomingNode incomNode = new LinkTypeIncomingNode();
//		incomNode.setLinkPath(URIUtils.createURI(libPath.toString()+"/incoming.mm"));
//		incomNode.setName(TextUtils.getText(incomNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
//		project.getModel().addNodeTo(incomNode, libNode);
		
		LinkTypeLiteratureAnnotationsNode litNode = new LinkTypeLiteratureAnnotationsNode();
		litNode.setLinkPath(URIUtils.createURI(libPath.toString()+"/literature_and_annotations.mm"));
		litNode.setName(TextUtils.getRawText(litNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
		project.getModel().addNodeTo(litNode, libNode);
		
//		LinkTypeMyPublicationsNode pubNode = new LinkTypeMyPublicationsNode();
//		pubNode.setLinkPath(URIUtils.createURI(libPath.toString()+"/my_publications.mm"));
//		pubNode.setName(TextUtils.getText(pubNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".label" ));
//		project.getModel().addNodeTo(pubNode, libNode);
		
//		if(settings != null && settings.includeDemoFiles()) {
//			LinkTypeFileNode newPaperNode = new LinkTypeFileNode();
//			newPaperNode.setLinkURI(URIUtils.createURI(libPath.toString()+"/My%20New%20Paper.mm"));
//			newPaperNode.setName(TextUtils.getText(newPaperNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".newpaper.label" ));
//			project.getModel().addNodeTo(newPaperNode, libNode);
//		}
		
		LinkTypeFileNode tempNode = new LinkTypeFileNode();
		tempNode.setLinkURI(URIUtils.createURI(libPath.toString()+"/temp.mm"));
		tempNode.setName(TextUtils.getText(tempNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".temp.label" ));
		tempNode.setSystem(true);
		project.getModel().addNodeTo(tempNode, libNode);
		
		LinkTypeFileNode trashNode = new LinkTypeFileNode();
		trashNode.setLinkURI(URIUtils.createURI(libPath.toString()+"/trash.mm"));
		trashNode.setName(TextUtils.getText(trashNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".trash.label" ));
		trashNode.setSystem(true);
		project.getModel().addNodeTo(trashNode, libNode);
		
		// expand library node by default
		//WorkspaceController.getCurrentModeExtension().getView().expandPath(libNode.getTreePath());
		
		FolderTypeLiteratureRepositoryNode litRepoNode = new FolderTypeLiteratureRepositoryNode();
		litRepoNode.setSystem(true);		
		project.getModel().addNodeTo(litRepoNode, root);
		
		if(settings != null) { 
			if(settings.useDefaultRepositoryPath()) {
				LiteratureRepositoryPathNode defaultPathNode = new LiteratureRepositoryPathNode();
				defaultPathNode.setPath(URIUtils.createURI(project.getProjectHome().toString()+"/literature_repository"));
				defaultPathNode.setName(TextUtils.getText(defaultPathNode.getClass().getName().toLowerCase(Locale.ENGLISH)+".default.label" ));
				defaultPathNode.setSystem(true);
				project.getModel().addNodeTo(defaultPathNode, litRepoNode);
			}
			for (URI uri : settings.getRepositoryPathURIs()) {
				LiteratureRepositoryPathNode pathNode = new LiteratureRepositoryPathNode();
				File file = URIUtils.getFile(uri);
				pathNode.setPath(project.getRelativeURI(uri));
				pathNode.setName(file.getName());
				pathNode.setSystem(true);
				project.getModel().addNodeTo(pathNode, litRepoNode);
			}
		}		
		project.addExtension(FolderTypeLiteratureRepositoryNode.class, litRepoNode);
		
		LinkTypeReferencesNode defaultRef = new LinkTypeReferencesNode();
		//use default bib file
		if(settings != null && settings.getBibTeXLibraryPath() != null) {
			File file = URIUtils.getFile(settings.getBibTeXLibraryPath());
			defaultRef.setName(file.getName());
			defaultRef.setLinkURI(project.getRelativeURI(settings.getBibTeXLibraryPath()));
		}
		else {
			defaultRef.setName(TextUtils.getText(FolderTypeMyFilesNode.class.getPackage().getName().toLowerCase(Locale.ENGLISH)+".refnode.name"));
			String bibName = "default";
			if(settings != null && settings.getProjectName() != null) {
				bibName = settings.getProjectName();
			}
			defaultRef.setLinkURI(URIUtils.createURI(libPath.toString()+"/"+bibName+".bib"));
		}
		defaultRef.setSystem(true);
		
		if(settings != null && settings.includeDemoFiles()) {
			LogUtils.info("copy docear tutorial files");
			copyDemoFiles(project, URIUtils.getAbsoluteFile(defaultRef.getLinkURI()), (settings.getBibTeXLibraryPath() == null));
		}
		
		project.getModel().addNodeTo(defaultRef, root);
		
		root.initiateMyFile(project);
		
		//misc -> help.mm
		root.refresh();
		
		
	}
	
	private void copyDemoFiles(DocearWorkspaceProject project, File bibPath, boolean setBib) {
		//prepare paths
		File defaultFilesPath = URIUtils.getFile(project.getProjectLibraryPath());
		defaultFilesPath.mkdirs();
		File repoPath = new File(URIUtils.getFile(project.getProjectHome()), "literature_repository/Example PDFs");
		repoPath.mkdirs();
		URI relativeRepoPath = project.getRelativeURI(repoPath.toURI());
		
		//prepare replace map
		Map<String, String> replaceMapping = new HashMap<String, String>();
		replaceMapping.put("@PROJECT_ID@", project.getProjectID());
		replaceMapping.put("@PROJECT_HOME@", project.getProjectHome().toString());
		
		replaceMapping.put("@LITERATURE_REPO_DEMO@", cutLastSlash(relativeRepoPath.toString()));
		
		URI relativeBibURI = LinkController.toLinkTypeDependantURI(bibPath, repoPath, LinkController.LINK_RELATIVE_TO_MINDMAP);
		if(Compat.isWindowsOS() && relativeBibURI.getPath().startsWith("//")) {
			replaceMapping.put("@LITERATURE_BIB_DEMO@", (new File(relativeBibURI).getPath().replace(File.separator, File.separator+File.separator)/*+File.separator+File.separator+"Example PDFs"*/));
		}
		else {
			replaceMapping.put("@LITERATURE_BIB_DEMO@", cutLastSlash(relativeBibURI.getPath().replace(":", "\\:")/*+"/Example PDFs"*/));
		}
		
		boolean created = 
			/*createAndCopy(new File(defaultFilesPath,"incoming.mm"), "/demo/template_incoming.mm", replaceMapping);//*/
		createAndCopy(new File(defaultFilesPath,"literature_and_annotations.mm"), "/demo/template_litandan.mm", replaceMapping);
		//createAndCopy(new File(defaultFilesPath,"my_publications.mm"), "/demo/template_mypubs.mm", replaceMapping);
		createAndCopy(new File(defaultFilesPath,"temp.mm"), "/demo/template_temp.mm", created, replaceMapping);
		createAndCopy(new File(defaultFilesPath,"trash.mm"), "/demo/template_trash.mm", created, replaceMapping);
		if(setBib) {
			createAndCopy(bibPath, "/demo/docear_example.bib", true, replaceMapping);
		}
		
		File draftsFile = new File(URIUtils.getAbsoluteFile(project.getProjectLibraryPath()).getParentFile(), "My Drafts");
		createAndCopy(new File(draftsFile, "My New Paper.mm"), "/demo/docear_example_project/My New Paper.mm", replaceMapping);
		
		createAndCopy(new File(repoPath, "Academic Search Engine Optimization (ASEO) -- Optimizing Scholarly Literature for Google Scholar and Co.pdf"), "/demo/docear_example_pdfs/Academic Search Engine Optimization (ASEO) -- Optimizing Scholarly Literature for Google Scholar and Co.pdf");
		createAndCopy(new File(repoPath, "Academic search engine spam and Google Scholars resilience against it.pdf"), "/demo/docear_example_pdfs/Academic search engine spam and Google Scholars resilience against it.pdf");
		createAndCopy(new File(repoPath, "An Exploratory Analysis of Mind Maps.pdf"), "/demo/docear_example_pdfs/An Exploratory Analysis of Mind Maps.pdf");
		createAndCopy(new File(repoPath, "Docear -- An Academic Literature Suite.pdf"), "/demo/docear_example_pdfs/Docear -- An Academic Literature Suite.pdf");
		createAndCopy(new File(repoPath, "Google Scholar's Ranking Algorithm -- An Introductory Overview.pdf"), "/demo/docear_example_pdfs/Google Scholar's Ranking Algorithm -- An Introductory Overview.pdf");
		createAndCopy(new File(repoPath, "Google Scholar's Ranking Algorithm -- The Impact of Citation Counts.pdf"), "/demo/docear_example_pdfs/Google Scholar's Ranking Algorithm -- The Impact of Citation Counts.pdf");
		createAndCopy(new File(repoPath, "Information Retrieval on Mind Maps -- What could it be good for.pdf"), "/demo/docear_example_pdfs/Information Retrieval on Mind Maps -- What could it be good for.pdf");
		createAndCopy(new File(repoPath, "Mr. DLib -- A Machine Readable Digital Library.pdf"), "/demo/docear_example_pdfs/Mr. DLib -- A Machine Readable Digital Library.pdf");
	}

	private String cutLastSlash(String path) {
		while(path.endsWith("/")) {
			path = path.substring(0, path.length()-1);
		}
		return path;
	}
	
	private boolean createAndCopy(File file, String resourcePath) {
		return createAndCopy(file, resourcePath, false, null);
	}
	
	private boolean createAndCopy(File file, String resourcePath,final Map<String, String> replaceMap) {
		return createAndCopy(file, resourcePath, false, replaceMap);
	}
	
	private boolean createAndCopy(File file, String resourcePath, boolean force,final Map<String, String> replaceMap) {
		try {
			if(!file.exists() || force) {
				createFile(file);
				InputStream is = this.getClass().getResourceAsStream(resourcePath);
				if(replaceMap == null) {
					FileUtils.copyInputStreamToFile(is, file);
				}
				else {
					FileUtils.copyInputStreamToFile(new ReplacingInputStream(replaceMap, is), file);
				}
				return true;
			}			
		}
		catch (Exception e) {
			LogUtils.warn(e);
		}	
		return false;
	}
	
	/**
	 * @param file
	 * @throws IOException
	 */
	private void createFile(File file) throws IOException {
		if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
			return;
		}
		file.createNewFile();
	}
	
	/***********************************************************************************
	 * NESTED CLASSES
	 **********************************************************************************/
	
	private class DocearResultProcessor implements IResultProcessor {

		private AWorkspaceProject project;

		public AWorkspaceProject getProject() {
			return project;
		}

		public void setProject(AWorkspaceProject project) {
			this.project = project;
		}

		public void process(final AWorkspaceTreeNode parent, final AWorkspaceTreeNode node) {
			_process(parent, node);
		}

		private void _process(AWorkspaceTreeNode parent, AWorkspaceTreeNode node) {
			if(getProject() == null) {
				LogUtils.warn("Missing project container! cannot add node to a model.");
				return;
			}
			if(node instanceof ProjectRootNode) {
				getProject().getModel().setRoot(node);
				if(((ProjectRootNode) node).getProjectID() == null) {
					((ProjectRootNode) node).setProjectID(getProject().getProjectID());
					if(!DocearWorkspaceProject.CURRENT_PROJECT_VERSION.equals(getProject().getVersion())) {
						((ProjectRootNode) node).initiateMyFile(getProject());
					}
				}				
			}
			else {
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
					if(DocearWorkspaceProject.CURRENT_PROJECT_VERSION.equals(getProject().getVersion())) {
						((ProjectRootNode) parent.getModel().getRoot()).initiateMyFile(getProject());
					}
				}
				else if(node instanceof FolderTypeLiteratureRepositoryNode) {
					project.addExtension(FolderTypeLiteratureRepositoryNode.class, (IWorkspaceProjectExtension) node);
				}
				else if(node instanceof LinkTypeIncomingNode) {
					//WorkspaceController.getCurrentModeExtension().getView().expandPath(node.getParent().getTreePath());
				}
			}
		}

	}
}
