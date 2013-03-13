package org.freeplane.plugin.workspace.features;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOExceptionWithCause;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.osgi.service.url.AbstractURLStreamHandlerService;

public class ProjectURLHandler extends AbstractURLStreamHandlerService {

	public URLConnection openConnection(URL url) throws IOException {
		//WORKSPACE - todo: extend with meaningful exception messages
		AWorkspaceProject project = WorkspaceController.getCurrentModel().getProject(url.getAuthority());		 
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
			throw new IOException("project id is missing");
		}
		
		URL absolutePath = url;
		try {
			absolutePath = new URL(project.getProjectHome().toURL(), url.getFile().startsWith("/") ? url.getFile().substring(1): url.getFile());
		} catch (Exception e) {
			throw new IOExceptionWithCause(e);
		}
		return absolutePath;
	}

}
