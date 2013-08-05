package org.docear.plugin.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventQueue;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.features.DocearLifeCycleObserver;
import org.docear.plugin.core.features.DocearMapModelExtension;
import org.docear.plugin.core.features.DocearProgressObserver;
import org.docear.plugin.core.io.IOTools;
import org.docear.plugin.core.listeners.MapWithoutProjectHandler;
import org.docear.plugin.core.logger.DocearEventLogger;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;
import org.freeplane.plugin.workspace.model.WorkspaceModel;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

/**
 * 
 */
public class DocearController implements IDocearEventListener {
	static final String DOCEAR_FIRST_RUN_PROPERTY = "docear.already_initialized";
	
	private final static String DOCEAR_VERSION_NUMBER = "docear.version.number";
	
	private String applicationName;
	private String applicationVersion;
	private String applicationStatus;
	private String applicationStatusVersion;
	private int applicationBuildNumber;
	private String applicationBuildDate;
	
	private final SemaphoreController semaphoreController = new SemaphoreController();
	
	private final DocearEventLogger docearEventLogger = new DocearEventLogger();
	private final static DocearController docearController = new DocearController();
	private final DocearEventQueue eventQueue = new DocearEventQueue();
	private final Set<String> workingThreads = new HashSet<String>();
	private final boolean firstRun;
	private boolean applicationShutdownAborted = false;
	private Map<Class<?>, Set<DocearProgressObserver>> progressObservers = new TreeMap<Class<?>, Set<DocearProgressObserver>>(new Comparator<Class<?>>() {
		public int compare(Class<?> c1, Class<?> c2) {
			if(c1.equals(c2)) {
				return 0;
			}
			return c1.getName().compareTo(c2.getName());
		}
	});

	private DocearLifeCycleObserver lifeCycleObserver;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	protected DocearController() {
		firstRun = !DocearController.getPropertiesController().getBooleanProperty(DOCEAR_FIRST_RUN_PROPERTY);
		setApplicationIdentifiers();
		eventQueue.addEventListener(this);
	}
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	public boolean isDocearFirstStart() {
		return firstRun;
	}
	
	public DocearEventQueue getEventQueue() {
		return eventQueue;
	}
	
	public boolean isLicenseDialogNecessary() {		
		int storedBuildNumber = Integer.parseInt(DocearController.getPropertiesController().getProperty(DOCEAR_VERSION_NUMBER, "0"));
		if (storedBuildNumber == 0) {
			DocearController.getPropertiesController().setProperty(DOCEAR_VERSION_NUMBER, ""+this.applicationBuildNumber);
			return true;
		}
		else {
			return false;
		}
	}
	
	public static DocearController getController() {
		return docearController;
	}
	
	public void addWorkingThreadHandle(String handleId) {
		if(handleId == null) {
			return;
		}
		synchronized (workingThreads) {
			workingThreads.add(handleId);	
		}
	}
	
	public void removeWorkingThreadHandle(String handleId) {
		if(handleId == null) {
			return;
		}
		synchronized (workingThreads) {
			workingThreads.remove(handleId);
		}
	}
	

	
	public void setApplicationIdentifiers() {
		final Properties versionProperties = new Properties();
		InputStream in = null;
		try {
			in = this.getClass().getResource("/version.properties").openStream();
			versionProperties.load(in);
		}
		catch (final IOException e) {
			
		}
		
		final Properties buildProperties = new Properties();
		in = null;
		try {
			in = this.getClass().getResource("/build.number").openStream();
			buildProperties.load(in);
		}
		catch (final IOException e) {
			
		}
		
		setApplicationName("Docear");
		setApplicationVersion(versionProperties.getProperty("docear_version"));
		setApplicationStatus(versionProperties.getProperty("docear_version_status"));		
		setApplicationStatusVersion(versionProperties.getProperty("docear_version_status_number"));
		setApplicationBuildDate(versionProperties.getProperty("build_date"));
		setApplicationBuildNumber(Integer.parseInt(buildProperties.getProperty("build.number")) -1);
	}
	
	public Version getVersion() {
		try {
			DocearController docearController = DocearController.getController();
			Version version = new Version();		
			String[] versionStrings = docearController.getApplicationVersion().split("\\.");
			version.setMajorVersion(Integer.parseInt(versionStrings[0]));
			version.setMiddleVersion(Integer.parseInt(versionStrings[1]));
			version.setMinorVersion(Integer.parseInt(versionStrings[2]));
			
			version.setStatus(docearController.getApplicationStatus());
			version.setStatusNumber(Integer.parseInt(docearController.getApplicationStatusVersion()));
			version.setBuildNumber(docearController.getApplicationBuildNumber());
			
			return version;
		}
		catch(Exception e) {
			LogUtils.warn(e);
			return null;
		}
	}
		
	
		
	public DocearEventLogger getDocearEventLogger() {
		return this.docearEventLogger;
	}
	
	public String getApplicationName() {
		return this.applicationName;
	}
	
	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}
	
	public String getApplicationVersion() {
		return applicationVersion;
	}
	private void setApplicationVersion(String applicationVersion) {
		this.applicationVersion = applicationVersion;
	}
	public String getApplicationStatus() {
		return applicationStatus;
	}
	private void setApplicationStatus(String applicationStatus) {
		this.applicationStatus = applicationStatus;
	}
	public String getApplicationStatusVersion() {
		return applicationStatusVersion;
	}
	private void setApplicationStatusVersion(String applicationStatusVersion) {
		this.applicationStatusVersion = applicationStatusVersion;
	}
	public int getApplicationBuildNumber() {
		return applicationBuildNumber;
	}
	private void setApplicationBuildNumber(int i) {
		this.applicationBuildNumber = i;
	}
	public String getApplicationBuildDate() {
		return applicationBuildDate;
	}
	private void setApplicationBuildDate(String applicationBuildDate) {
		this.applicationBuildDate = applicationBuildDate;
	}
	
	private boolean hasWorkingThreads() {
		synchronized (workingThreads) {
			return !workingThreads.isEmpty();
		}
		
	}
	
	public boolean shutdown() {	
		getEventQueue().dispatchEvent(new DocearEvent(this, null, DocearEventType.APPLICATION_CLOSING));
		
		Controller.getCurrentController().getViewController().saveProperties();
		DocearController.getPropertiesController().saveProperties();
		ResourceController.getResourceController().saveProperties();		
		if(!waitThreadsReady()){
			return false;
		}
		if(Controller.getCurrentController().getViewController().quit()) {
			getEventQueue().dispatchEvent(new DocearEvent(this, null, DocearEventType.FINISH_THREADS));
			if(!waitThreadsReady()){
				return false;
			}
		}
		else {
			return false;
		}		
		return true;
	}
	
	public void addProgressObserver(Class<?> clazz, DocearProgressObserver observer) {
		Set<DocearProgressObserver> observers = progressObservers.get(clazz);
		if(observers == null) {
			observers = new HashSet<DocearProgressObserver>();
			progressObservers.put(clazz, observers);
		}
		observers.add(observer);
	}
	
	public Collection<DocearProgressObserver> getProgressObservers(Class<?> clazz) {
		Collection<DocearProgressObserver> observers = progressObservers.get(clazz);
		if(observers == null) {
			observers = Collections.emptySet();
		}
		return observers;
	}
	
	private boolean waitThreadsReady() {
		while(hasWorkingThreads()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
			}				
		}	
		if(this.applicationShutdownAborted){
			this.applicationShutdownAborted = false;
			return false;
		}
		return true;
	}
	
	
	public String getGPLv2Terms() {
		try {
			return IOTools.getStringFromStream(DocearController.class.getResourceAsStream("/gplv2.html"),"UTF-8");
		}
		catch (IOException e) {
			LogUtils.warn(e);
			return "GPLv2";
		}
	}
	
	public String getDataProcessingTerms() {
		try {
			return IOTools.getStringFromStream(DocearController.class.getResourceAsStream("/Docear_data_processing.txt"),"UTF-8");
		}
		catch (IOException e) {
			LogUtils.warn(e);
			return "Data Processing";
		}
	}
	
	public String getDataPrivacyTerms() {
		try {
			return IOTools.getStringFromStream(DocearController.class.getResourceAsStream("/Docear_data_privacy.txt"), "UTF-8");
		}
		catch (IOException e) {
			LogUtils.warn(e);
			return "Data Privacy";
		}
	}
	
	public String getTermsOfService() {
		try {
			return IOTools.getStringFromStream(DocearController.class.getResourceAsStream("/Docear_terms_of_use.txt"),"UTF-8");
		}
		catch (IOException e) {
			LogUtils.warn(e);
			return "Terms of Use";
		}
	}
	
	protected void setLifeCycleObserver(DocearLifeCycleObserver observer) {
		if(lifeCycleObserver != null) {
			throw new RuntimeException("observer already set");
		}
		this.lifeCycleObserver = observer;
	}
	
	public DocearLifeCycleObserver getLifeCycleObserver() {
		return this.lifeCycleObserver;
	}
	
	public static ResourceController getPropertiesController() {
		return ResourceController.getResourceController();
	}
	
	public static AWorkspaceProject findProject(MapModel map) {
		AWorkspaceProject project = null; 
		WorkspaceMapModelExtension mapExt = WorkspaceController.getMapModelExtension(map, false);
		if(mapExt != null) {
			project = mapExt.getProject();
		}
		if(project == null) {
			WorkspaceModel model = WorkspaceController.getCurrentModel();
			for (AWorkspaceProject prj : model.getProjects()) {
				URI relativeUri = prj.getRelativeURI(map.getFile().toURI());
				//DOCEAR - validate: check whether a path is within the projects home
				if(relativeUri != null && !relativeUri.getRawPath().startsWith("/..") && !"file".equals(relativeUri.getScheme())) {
					project = prj;
					WorkspaceMapModelExtension ext = WorkspaceController.getMapModelExtension(map);
					ext.setProject(project);
					break;
				}
			}
		}
		
		if (project == null) {
			project = MapWithoutProjectHandler.showProjectSelectionWizard(map, false);
		}
		return project;
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/

	public void handleEvent(DocearEvent event) {		
		if(event.getType() == DocearEventType.APPLICATION_CLOSING_ABORTED){
			this.applicationShutdownAborted = true;
		}			
	}
	
	public SemaphoreController getSemaphoreController() {
		return semaphoreController;
	}
	public boolean isLibraryMap(MapModel map) {
		DocearMapModelExtension dmme = map.getExtension(DocearMapModelExtension.class);
		if (dmme == null) {
			return false;
		}
		//WORKSPACE - DOCEAR todo: implement 
//		for (URI uri : DocearController.getController().getLibrary().getMindmaps()) { 
//			if (uri != null && map != null) {
//				String path = map.getFile().getAbsolutePath(); 
//				File f = URIUtils.getAbsoluteFile(uri);
//				
//				if (f != null && f.getAbsolutePath().equals(path)) {
//					return true;
//				}
//			}
//		}
		return false;
	}
	
}
