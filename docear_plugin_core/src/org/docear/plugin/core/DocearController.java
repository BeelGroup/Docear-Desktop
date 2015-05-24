package org.docear.plugin.core;

import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

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
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.features.WorkspaceMapModelExtension;
import org.freeplane.plugin.workspace.model.WorkspaceModel;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

/**
 * 
 */
public class DocearController implements IDocearEventListener {
	static final String DOCEAR_FIRST_RUN_PROPERTY = "docear.already_initialized";
	static final String DOCEAR_SERVICE_NOT_AVAILABLE_PROPERTY = "docear.service_not_available";
	
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
		checkServiceAvailability();
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
	
	public boolean hasOutdatedConfigFiles() {
		try {
    		File file = new File(Compat.getApplicationUserDirectory());
    		for (File dir : file.listFiles()) {
    			if (dir.getName().equals("ribbons")) {
    				return false;
    			}
    		}    		
		}
		catch (Exception e) {
			DocearLogger.warn(e);
		}
		
		return true;
	}
	
	public boolean isLicenseDialogNecessary() {		
		int storedBuildNumber = Integer.parseInt(DocearController.getPropertiesController().getProperty(DOCEAR_VERSION_NUMBER, "0"));
		if (storedBuildNumber == 0 || hasOutdatedConfigFiles()) {
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
	
	//TODO Service
	public boolean isServiceAvailable(){		
		return !DocearController.getPropertiesController().getBooleanProperty(DOCEAR_SERVICE_NOT_AVAILABLE_PROPERTY);
	}
	
	//TODO Service
	private void checkServiceAvailability(){
		Map<String, String> serviceProperties = getServiceProperties();	
		if(serviceProperties.get("webservice_status") == null) return;
		if(serviceProperties.get("webservice_status").equalsIgnoreCase("0")){			
			if(isServiceAvailable()){
				DocearController.getPropertiesController().setProperty(DOCEAR_SERVICE_NOT_AVAILABLE_PROPERTY, true);
				if(!isDocearFirstStart()){
					showServiceMessage(serviceProperties.get("webservice_message"), serviceProperties.get("webservice_details_url"));
				}
			}
		}
		if(serviceProperties.get("webservice_status").equalsIgnoreCase("1")){
			if(!isServiceAvailable()){
				DocearController.getPropertiesController().setProperty(DOCEAR_SERVICE_NOT_AVAILABLE_PROPERTY, false);
				if(!isDocearFirstStart()){
					showServiceMessage(serviceProperties.get("webservice_message"), serviceProperties.get("webservice_details_url"));
				}
			}
		}
	}
	
	//TODO Service
	private void showServiceMessage(String message, String detailURL){
		
	    JLabel label = new JLabel();
	    Font font = label.getFont();
	    
	    StringBuffer style = new StringBuffer("font-family:" + font.getFamily() + ";");
	    style.append("font-weight:" + (font.isBold() ? "bold" : "normal") + ";");
	    style.append("font-size:" + font.getSize() + "pt;");

	    
	    JEditorPane ep = new JEditorPane("text/html", "<html><body style=\"" + style + "\" >" //
	            + message +"<br><br>For further detail follow this link:  <a href=\""+detailURL+"\">"+detailURL+"</a>" //
	            + "</body></html>");

	    // handle link events
	    ep.addHyperlinkListener(new HyperlinkListener()
	    {
	        @Override
	        public void hyperlinkUpdate(HyperlinkEvent evt)
	        {
	            if (evt.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED))
					try {
						Controller.getCurrentController().getViewController().openDocument(evt.getURL());
					} catch (Exception e) {
						LogUtils.warn(e);
					}	                
	        }
	    });
	    ep.setEditable(false);
	    ep.setBackground(new Color(214,217,223,0));
	    
	    JOptionPane.showMessageDialog(null, ep);
	}
	
	//TODO Service
	private Map<String, String> getServiceProperties() {
		URI statusURI = URI.create("http://www.docear.org/services/status.php");	        
        Map<String, String> properties = new HashMap<String, String>();
        boolean webService = false;
        boolean version = false;
        HttpURLConnection statusConnection = null;        
		try {
			statusConnection = (HttpURLConnection)statusURI.toURL().openConnection();
			statusConnection.setRequestMethod("GET");
			statusConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
			statusConnection.connect();
			BufferedReader in = new BufferedReader(new InputStreamReader(statusConnection.getInputStream()));
			String inputLine;
	        while ((inputLine = in.readLine()) != null){
	        	if(inputLine.contains("[Web services]")){
	        		webService = true;
	        		version = false;
	        		continue;
	        	}
	        	if(inputLine.contains("[Version]")){
	        		webService = false;
	        		version = true;
	        		continue;
	        	}
	        	inputLine = inputLine.replaceAll("\t/.*", "");
	        	inputLine = inputLine.replaceAll("\"", "");
	        	inputLine = inputLine.replaceAll("\t", "");
        		String[] keyValuePair = inputLine.split("=");
        		if(keyValuePair.length == 2){
        			if(webService){
    	        		properties.put("webservice_" + keyValuePair[0].trim(), keyValuePair[1].trim());
    	        	}
        			if(version){
        				properties.put("version_" + keyValuePair[0].trim(), keyValuePair[1].trim());
        			}
        		}        	
	        }
	        in.close();
		} catch (MalformedURLException e) {
			LogUtils.warn(e);
		} catch (IOException e) {
			LogUtils.warn(e);
		}
		finally{
			if(statusConnection != null){
				statusConnection.disconnect();
			}
		}	
		return properties;
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
		if(map == null) {
			return false;
		}
		DocearMapModelExtension dmme = map.getExtension(DocearMapModelExtension.class);
		if (dmme == null) {
			return false;
		}
		AWorkspaceProject project = WorkspaceController.getMapProject(map);
		if(DocearWorkspaceProject.isCompatible(project)) {
			((DocearWorkspaceProject)project).getLibraryMaps();
			for (URI uri : ((DocearWorkspaceProject)project).getLibraryMaps()) { 
				if (uri != null) {
					String path = map.getFile().getAbsolutePath(); 
					File f = URIUtils.getAbsoluteFile(uri);
					
					if (f != null && f.getAbsolutePath().equals(path)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
}
