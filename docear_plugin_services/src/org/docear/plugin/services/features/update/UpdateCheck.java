package org.docear.plugin.services.features.update;

import java.io.StringReader;

import javax.swing.JOptionPane;
import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.Version;
import org.docear.plugin.services.ADocearServiceFeature;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.docear.plugin.services.features.update.action.DocearUpdateCheckAction;
import org.docear.plugin.services.xml.creators.ApplicationCreator;
import org.docear.plugin.services.xml.creators.BuildNumberCreator;
import org.docear.plugin.services.xml.creators.IXMLNodeProcessor;
import org.docear.plugin.services.xml.creators.MajorVersionCreator;
import org.docear.plugin.services.xml.creators.MiddleVersionCreator;
import org.docear.plugin.services.xml.creators.MinorVersionCreator;
import org.docear.plugin.services.xml.creators.ReleaseDateCreator;
import org.docear.plugin.services.xml.creators.ReleaseNotesCreator;
import org.docear.plugin.services.xml.creators.StatusCreator;
import org.docear.plugin.services.xml.creators.StatusNumberCreator;
import org.docear.plugin.services.xml.creators.VersionCreator;
import org.docear.plugin.services.xml.creators.VersionsCreator;
import org.docear.plugin.services.xml.elements.Application;
import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class UpdateCheck extends ADocearServiceFeature {
	public static final String DOCEAR_UPDATE_CHECKER_DISABLE = "docear.update_checker.disable";
	public static final String DOCEAR_UPDATE_CHECKER_ALL = "docear.update_checker.all";
	public static final String DOCEAR_UPDATE_CHECKER_BETA = "docear.update_checker.beta";
	public static final String DOCEAR_UPDATE_CHECKER_MINOR = "docear.update_checker.minor";
	public static final String DOCEAR_UPDATE_CHECKER_MIDDLE = "docear.update_checker.middle";
	public static final String DOCEAR_UPDATE_CHECKER_MAJOR = "docear.update_checker.major";
			
	final private ReadManager readManager;
	
	private Application application;
	
	ApplicationCreator applicationCreator;
	VersionsCreator versionsCreator;
	VersionCreator versionCreator;
	
	ReleaseDateCreator releaseDateCreator;
	BuildNumberCreator buildNumberCreator;
	MajorVersionCreator majorVersionCreator;
	MiddleVersionCreator middleVersionCreator;
	MinorVersionCreator minorVersionCreator;
	StatusCreator statusCreator;
	StatusNumberCreator statusNumberCreator;
	ReleaseNotesCreator releaseNotesCreator;
	
	
	
	public UpdateCheck() {
		this.readManager = new ReadManager();
		initReadManager();
	}

	public void checkForUpdates() {
		checkForUpdates(false);
	}
	
	public void checkForUpdates(boolean forced) {
		String xml;
		try {
			String choice = DocearController.getPropertiesController().getProperty("docear.update_checker.options");
			if (choice == null || DOCEAR_UPDATE_CHECKER_DISABLE.equals(choice)) {
				return;
			}
			
			String minStatus = null;
			if (choice.equals(DOCEAR_UPDATE_CHECKER_ALL)) {
				minStatus = Version.StatusName.devel.name();
			}
			else if (choice.equals(DOCEAR_UPDATE_CHECKER_BETA)) {
				minStatus = Version.StatusName.beta.name();
			}
			else {
				minStatus = Version.StatusName.stable.name();
			}				
			xml = requestLatestVersionXml(minStatus);
			load(xml);
			
			final Version latestVersion = getLatestAvailableVersion();
			final Version runningVersion = DocearController.getController().getVersion();
			
			if (latestVersion == null || runningVersion == null) {
				return;
			}

			int compCode = latestVersion.compareTo(runningVersion);
			
			String lastLatestVersionString = DocearController.getPropertiesController().getProperty("docer.update_checker.savedLatestVersion", "");
			// don't show Dialog again if latestVersionFromServer was already announced to the user
			if (!latestVersion.toString().equals(lastLatestVersionString) 
					&& showUpdateCheckerDialog(compCode, choice)) {
				DocearUpdateCheckAction.showDialog(runningVersion, latestVersion);
			}
			else {
				if(forced) {
					JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.version.uptodate.text"), TextUtils.getText("docear.version.check.title"), JOptionPane.INFORMATION_MESSAGE);
				}
			}
			
		} catch (Exception e) {
			LogUtils.warn("org.docear.plugin.services.features.UpdateCheck.UpdateCheck(): "+e.getMessage());
		}
	}
	
	private String requestLatestVersionXml(String minStatus) throws Exception {
		if (minStatus == null) {
			return null;
		}
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		queryParams.putSingle("minStatus", minStatus);
		DocearServiceResponse response = ServiceController.getConnectionController().get("/applications/docear/versions/latest", queryParams);
		if(DocearServiceResponse.Status.OK.equals(response.getStatus())) {
			return response.getContentAsString();
		}
		return null;
	}

	private boolean showUpdateCheckerDialog(int compCode, String choice) {
		if (choice.equals(DOCEAR_UPDATE_CHECKER_ALL) && compCode >= Version.CompareCode.DEVEL.code) {
			return true;
		}
		else if (choice.equals(DOCEAR_UPDATE_CHECKER_BETA) && compCode >= Version.CompareCode.BETA.code) {
			return true;
		}
		else if (choice.equals(DOCEAR_UPDATE_CHECKER_MINOR) && compCode >= Version.CompareCode.MINOR.code) {
			return true;
		}
		else if (choice.equals(DOCEAR_UPDATE_CHECKER_MIDDLE) && compCode >= Version.CompareCode.MIDDLE.code) {
			return true;
		}
		else if (choice.equals(DOCEAR_UPDATE_CHECKER_MAJOR) && compCode >= Version.CompareCode.MAJOR.code) {
			return true;
		}
		
		return false;
	}
	
	private void load(final String xml) {		
		final TreeXmlReader reader = new TreeXmlReader(readManager);
		
		try {
			
			reader.load(new StringReader(xml));			
		}
		catch (final Exception e) {
			LogUtils.warn("org.docear.plugin.services.features.UpdateCheck.load: "+e.getMessage());
		}		
	}
	
	private void initReadManager() {
		readManager.addElementHandler("application", getApplicationCreator());
		readManager.addElementHandler("versions", getVersionsCreator());
		readManager.addElementHandler("version", getVersionCreator());
		
		readManager.addElementHandler("release_date", getReleaseDateCreator());
		readManager.addElementHandler("build", getBuildNumberCreator());
		readManager.addElementHandler("major", getMajorVersionCreator());
		readManager.addElementHandler("middle", getMiddleVersionCreator());
		readManager.addElementHandler("minor", getMinorVersionCreator());
		readManager.addElementHandler("status", getStatusCreator());
		readManager.addElementHandler("status_number", getStatusNumberCreator());
		readManager.addElementHandler("release_notes", getReleaseNotesCreator());
	}
	
	public Version getLatestAvailableVersion() {
		return this.application.getVersions().entrySet().iterator().next().getValue();		
	}
	
	private IElementHandler getReleaseNotesCreator() {		
		if (this.releaseNotesCreator == null) {
			this.releaseNotesCreator = new ReleaseNotesCreator();
		}
		
		return this.releaseNotesCreator;
	}
	
	private IElementHandler getStatusNumberCreator() {		
		if (this.statusNumberCreator == null) {
			this.statusNumberCreator = new StatusNumberCreator();
		}
		
		return this.statusNumberCreator;
	}
	
	private IElementHandler getStatusCreator() {		
		if (this.statusCreator == null) {
			this.statusCreator = new StatusCreator();
		}
		
		return this.statusCreator;
	}
	
	private IElementHandler getMinorVersionCreator() {		
		if (this.minorVersionCreator == null) {
			this.minorVersionCreator = new MinorVersionCreator();
		}
		
		return this.minorVersionCreator;
	}
	
	private IElementHandler getMiddleVersionCreator() {		
		if (this.middleVersionCreator == null) {
			this.middleVersionCreator = new MiddleVersionCreator();
		}
		
		return this.middleVersionCreator;
	}
	
	private IElementHandler getMajorVersionCreator() {		
		if (this.majorVersionCreator == null) {
			this.majorVersionCreator = new MajorVersionCreator();
		}
		
		return this.majorVersionCreator;
	}
	
	private IElementHandler getBuildNumberCreator() {		
		if (this.buildNumberCreator == null) {
			this.buildNumberCreator = new BuildNumberCreator();
		}
		
		return this.buildNumberCreator;
	}
	
	private IElementHandler getReleaseDateCreator() {		
		if (this.releaseDateCreator == null) {
			this.releaseDateCreator = new ReleaseDateCreator();
		}
		
		return this.releaseDateCreator;
	}
	
	private IElementHandler getVersionCreator() {		
		if (this.versionCreator == null) {
			this.versionCreator = new VersionCreator();
		}
		
		return this.versionCreator;
	}
	
	private IElementHandler getVersionsCreator() {		
		if (this.versionsCreator == null) {
			this.versionsCreator = new VersionsCreator();
		}
		
		return this.versionsCreator;
	}
	
	private IElementHandler getApplicationCreator() {
		if (this.applicationCreator == null) {
			this.applicationCreator = new ApplicationCreator();
			this.applicationCreator.setResultProcessor(new IXMLNodeProcessor() {
				
				public void process(Object node, Object parent) {
					if(node instanceof Application) {
						setApplication((Application) node);
					}
					
				}
			});
		}
		
		return this.applicationCreator; 
	}
	
	private void setApplication(Application application) {
		this.application = application;
	}

	@Override
	protected void installDefaults(ModeController modeController) {
		WorkspaceController.replaceAction(new DocearUpdateCheckAction());
		new Thread() {
			public void run() {
				checkForUpdates();
			}
		}.start();
	}

	@Override
	public void shutdown() {
	}
}
