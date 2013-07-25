package org.freeplane.plugin.workspace.features;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOExceptionWithCause;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.osgi.service.url.AbstractURLStreamHandlerService;

public class ProjectURLHandler extends AbstractURLStreamHandlerService {
	
	public URLConnection openConnection(URL url) throws IOException {
		String projectID = url.getAuthority();
		AWorkspaceProject project = WorkspaceController.getCachedProjectByID(projectID);
		
		if(project == null) {
			throw new IOException("project with id="+projectID+" is missing");
		}
		
		URL absolutePath = resolve(project, url);
		return absolutePath.openConnection();
	}

	/**
	 * resolve a project-relative path
	 * @param project
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public static URL resolve(AWorkspaceProject project, URL url) throws IOException {
		if(project == null) {
			throw new IOException("project is NULL");
		}
		
		URL absolutePath = url;
		try {
			String projectUrl = project.getProjectHome().toURL().toExternalForm();
			absolutePath = createAbsoluteURL(projectUrl, url);
		} catch (Exception e) {
			throw new IOExceptionWithCause(e);
		}
		return absolutePath;
	}

	private static URL createAbsoluteURL(String projectBase, URL projectRelativeURL) throws MalformedURLException {
		URL absolutePath;
		String urlFile = projectRelativeURL.getFile();
		urlFile = urlFile.startsWith("/") ? urlFile.substring(1): urlFile;
		projectBase = projectBase.endsWith("/") ? projectBase.substring(0, projectBase.length()-1): projectBase;
		absolutePath = new URL(projectBase+"/"+urlFile);
		return absolutePath;
	}
}
