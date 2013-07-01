package org.docear.plugin.core.listeners;


public class WorkspaceChangeListener /*implements IWorkspaceEventListener*/ {
	//WORKSPACE - todo: implement in DocearProjectLoader
//
//	private boolean workspacePrepared = false;	
//
//	public void openWorkspace(AWorkspaceEvent event) {
//		if (DocearController.getController().isLicenseDialogNecessary()) 
//		{
//			DocearController.getController().dispatchDocearEvent(new DocearEvent(DocearController.getController(), DocearEventType.SHOW_LICENSES));
//		}
//	}
//
//	public void closeWorkspace(AWorkspaceEvent event) {}
//
//	public void workspaceChanged(AWorkspaceEvent event) {}
//
//	public void toolBarChanged(AWorkspaceEvent event) {}
//
//	public void workspaceReady(AWorkspaceEvent event) {	
//		setSystemNodes();
//		ResourceController resController = Controller.getCurrentController().getResourceController();
//		if (resController.getProperty("ApplicationName").equals("Docear")) {
//			String mapPath = new File(WorkspaceUtils.getDataDirectory(), "/help/docear-welcome.mm").toURI().getPath();
//			resController.setProperty("first_start_map", mapPath);
//			resController.setProperty("tutorial_map", mapPath);
//		}
//	}
//
//	private void setSystemNodes() {
//		try{
//			File libPath = WorkspaceController.resolveFile(DocearController.getController().getLibraryPath());		
//			URI _tempFile = Compat.fileToUrl(new File(libPath, "temp.mm")).toURI();	
//			URI _trashFile = Compat.fileToUrl(new File(libPath, "trash.mm")).toURI();
//			
//			AWorkspaceTreeNode parent = WorkspaceUtils.getNodeForPath(((WorkspaceRoot) WorkspaceUtils.getModel().getRoot()).getName()+"/Library");
//			for(AWorkspaceTreeNode node : Collections.list(parent.children())){
//				if(node.getType().equals(LinkTypeIncomingCreator.LINK_TYPE_INCOMING) || node.getType().equals(LinkTypeLiteratureAnnotationsCreator.LINK_TYPE_LITERATUREANNOTATIONS) || node.getType().equals(LinkTypeMyPublicationsCreator.LINK_TYPE_MYPUBLICATIONS)){
//					node.setSystem(true);
//				}
//				if(node.getType().equals(ALinkNode.LINK_TYPE_FILE)){
//					URI linkPath = WorkspaceUtils.absoluteURI(((ALinkNode)node).getLinkPath());
//					if(linkPath.equals(_trashFile) || linkPath.equals(_tempFile)){
//						node.setSystem(true);
//					}
//				}
//			}
//		}		
//		catch (MalformedURLException e) {
//			LogUtils.warn(e);
//		}
//		catch (URISyntaxException e) {
//			LogUtils.warn(e);
//		}
//	}
//
//	public void configurationLoaded(AWorkspaceEvent event) {
//		linkWelcomeMindmapAfterWorkspaceCreation();
//		IDocearLibrary lib = DocearController.getController().getLibrary();
//		if(lib != null && lib instanceof FolderTypeLibraryNode) {
//			WorkspaceController.getController().getExpansionStateHandler().addPathKey(((AWorkspaceTreeNode)lib).getKey());			
//			WorkspaceController.getCurrentModel().getRoot().refresh();
//		}
//			
//	}
//
//	public void configurationBeforeLoading(AWorkspaceEvent event) {
//		removeLibraryPaths();
//		prepareWorkspace();
//	}
//	
//	private void removeLibraryPaths() {
//		CoreConfiguration.projectPathObserver.reset();
//		CoreConfiguration.referencePathObserver.reset();
//		CoreConfiguration.repositoryPathObserver.reset();
//	}
//	
//	private void prepareWorkspace() {
//		if(!workspacePrepared) {
//			WorkspaceController controller = WorkspaceController.getController();
//			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, FolderTypeLibraryCreator.FOLDER_TYPE_LIBRARY, new FolderTypeLibraryCreator());
//			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, FolderTypeLiteratureRepositoryCreator.FOLDER_TYPE_LITERATUREREPOSITORY, new FolderTypeLiteratureRepositoryCreator());
//			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_FOLDER, FolderTypeProjectsCreator.FOLDER_TYPE_PROJECTS, new FolderTypeProjectsCreator());
//			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeMyPublicationsCreator.LINK_TYPE_MYPUBLICATIONS , new LinkTypeMyPublicationsCreator());
//			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeReferencesCreator.LINK_TYPE_REFERENCES , new LinkTypeReferencesCreator());
//			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeLiteratureAnnotationsCreator.LINK_TYPE_LITERATUREANNOTATIONS , new LinkTypeLiteratureAnnotationsCreator());
//			controller.getConfiguration().registerTypeCreator(WorkspaceConfiguration.WSNODE_LINK, LinkTypeIncomingCreator.LINK_TYPE_INCOMING , new LinkTypeIncomingCreator());
//			
//			controller.getConfiguration().setDefaultConfigTemplateUrl(getClass().getResource("/conf/workspace_default_docear.xml"));
//			
//			modifyContextMenus();
//		}
//		workspacePrepared  = true;		
//		copyInfoIfNeeded();		
//	}
//	
//	private void copyInfoIfNeeded() {
//		File infoFile = new File(WorkspaceUtils.getProfileBaseFile(), "!!!info.txt");
//		if(!infoFile.exists()) {
//			createAndCopy(infoFile, "/conf/!!!info.txt");
//		}
//		
//		File _dataInfoFile = new File(WorkspaceUtils.getDataDirectory(), "!!!info.txt");
//		if(!_dataInfoFile.exists()) {
//			createAndCopy(_dataInfoFile, "/conf/!!!info.txt");
//		}
//		
//		File _welcomeFile = new File(WorkspaceUtils.getDataDirectory(), "/help/docear-welcome.mm");
//		if(!_welcomeFile.exists()) {
//			createAndCopy(_welcomeFile, "/conf/docear-welcome.mm");			
//		}
//		
//		File _docearLogo = new File(WorkspaceUtils.getDataDirectory(), "/help/docear-logo.png");
//		if(!_docearLogo.exists()) {
//			createAndCopy(_docearLogo, "/images/docear_logo.png");			
//		}
//	}
//
//	/**
//	 * @param file
//	 * @param resourcePath
//	 */
//	private void createAndCopy(File file, String resourcePath) {
//		try {
//			createFile(file);
//			FileUtils.copyInputStreamToFile(CoreConfiguration.class.getResourceAsStream(resourcePath), file);
//		}
//		catch (IOException e) {
//			LogUtils.warn(e);
//		}	
//	}
//
//	/**
//	 * @param file
//	 * @throws IOException
//	 */
//	private void createFile(File file) throws IOException {
//		if(!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
//			return;
//		}
//		file.createNewFile();
//	}
//	
//	private void linkWelcomeMindmapAfterWorkspaceCreation() {		
//		AWorkspaceTreeNode parent = WorkspaceUtils.getNodeForPath(((WorkspaceRoot) WorkspaceController.getCurrentModel().getRoot()).getName()+"/Miscellaneous");
//		if (parent == null) {
//			return;
//		}
//		File _welcomeFile = new File(WorkspaceUtils.getDataDirectory(), "/help/docear-welcome.mm");
//		LinkTypeFileNode node = new LinkTypeFileNode();
//		node.setName(_welcomeFile.getName());
//		node.setLinkPath(WorkspaceUtils.getWorkspaceRelativeURI(_welcomeFile));
//		WorkspaceUtils.getModel().addNodeTo(node, parent, false);
//		parent.refresh();
//	}
//	
//	private void modifyContextMenus() {		
//		AWorkspaceTreeNode root =  (AWorkspaceTreeNode) WorkspaceUtils.getModel().getRoot();
//		WorkspacePopupMenuBuilder.insertAction(root.getContextMenu(), "workspace.action.docear.locations.change", 3);
//	}

}
