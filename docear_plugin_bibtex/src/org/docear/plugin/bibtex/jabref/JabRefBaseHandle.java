package org.docear.plugin.bibtex.jabref;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;

import net.sf.jabref.BasePanel;
import net.sf.jabref.imports.ParserResult;

public class JabRefBaseHandle {
	private final ParserResult parserResult;
	private final BasePanel basePanel;
	private final Set<DocearWorkspaceProject> connectedProjects = new LinkedHashSet<DocearWorkspaceProject>();
	
	public JabRefBaseHandle(BasePanel basePanel, ParserResult parserResult) {
		this.parserResult = parserResult;
		this.basePanel = basePanel;
	}
	
	public BasePanel getBasePanel() {
		return this.basePanel;
	}
	
	public ParserResult getParserResult() {
		return this.parserResult;
	}

	public HashMap<String, String> getMeta() {
		return this.getParserResult().getMetaData();
	}

	public String getEncoding() {
		return this.getParserResult().getEncoding();
	}
	
	public File getFile() {
		return this.getBasePanel().getFile();
	}

	public void addProjectConnection(DocearWorkspaceProject project) {
		if(project == null) {
			return;
		}
		synchronized (connectedProjects) {
			connectedProjects.add(project);
		}		
	}
	
	public void removeProjectConnection(DocearWorkspaceProject project) {
		if(project == null) {
			return;
		}
		synchronized (connectedProjects) {
			connectedProjects.remove(project);
		}
	}
	
	public boolean hasMoreConnections() {
		return connectedProjects.size() > 0;
	}
	
	public void showBasePanel() {
		ReferencesController.getController().getJabrefWrapper().getJabrefFrame().showBasePanel(getBasePanel());		
	}
}
