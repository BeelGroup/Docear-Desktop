package org.docear.plugin.core.workspace.compatible;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.osgi.service.url.AbstractURLStreamHandlerService;

public class DocearConversionURLHandler extends AbstractURLStreamHandlerService {
	
	private static AWorkspaceProject targetProject;
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public static AWorkspaceProject getTargetProject() {
//		if(targetProject == null) {
//			return WorkspaceController.getCurrentProject();
//		}
		return targetProject;
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	public URLConnection openConnection(URL url) throws IOException {
		if(getTargetProject() == null) {
			throw new IOException("no project assignment");
		}
		String path = targetProject.getProjectHome().getRawPath();
		URL ret = new URL("file", null, path + url.getPath());
		try {
			URI uri = ret.toURI();
			if (uri.getPath().startsWith("//")) {
				uri = uri.normalize();
				uri = new URI(uri.getScheme(), null, "///" + uri.getPath(), null);
			} else {
				uri = uri.normalize();
			}
			ret = uri.toURL();
		} catch (URISyntaxException e) {
			throw new IOException(e.getMessage());
		}
		return ret.openConnection();
	}

	public static void setTargetProject(AWorkspaceProject project) {
		targetProject = project;
	}
}
