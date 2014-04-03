package org.docear.plugin.bibtex.jabref;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.swing.JOptionPane;

import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.GUIGlobals;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.dialogs.DuplicateLinkDialogPanel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

public class DuplicateResolver {
	
	public final static DuplicateResolver duplicateResolver = new DuplicateResolver();
	
	File file = null;
	URL url = null;	
	
	public static DuplicateResolver getDuplicateResolver() {
		return duplicateResolver;
	}
	
	public BibtexEntry resolveDuplicateLinks(Object link) throws ResolveDuplicateEntryAbortedException, IllegalArgumentException {
		if (link instanceof File) {
			file = (File) link;
			url = null;
		}
		else if (link instanceof URL) {
			url = (URL) link;
			file = null;
		}
		else {
			throw new IllegalArgumentException("link has to be either of type java.io.File or java.net.URL!");
		}
		
		List<BibtexEntry> entries = new ArrayList<BibtexEntry>();

		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();

		if (file != null) {
			//handle duplicate file links
    		for (BibtexEntry entry : database.getEntries()) {
    			for (String jabrefPath : retrieveFileLinksFromEntry(entry)) {
    				File jabrefFile = new File(jabrefPath);
    
    				if (jabrefFile != null && jabrefFile.getName().equals(file.getName())) {
    					entries.add(entry);
    					break;					
    				}
    			}
    		}
		}
		else {			
			//handle duplicate url links 
    		for (BibtexEntry entry : database.getEntries()) {
    			URL entryUrl = null;
    			String urlString = entry.getField("url");
    			try {
    				if (urlString != null) {
    					entryUrl = new URL(urlString);
    				}
    			}
    			catch (MalformedURLException e) {
    				LogUtils.info(urlString + ": " + e.getMessage());
    			}
    			if (url.equals(entryUrl)) {
    				entries.add(entry);
    			}
    		}
		}

		Boolean ignoreAlways = ResourceController.getResourceController().getBooleanProperty("docear.reference.duplicate_always_ignore");
		if (entries.size() == 1 || (ignoreAlways && entries.size()>0)) {
			return entries.get(0);
		}
		else if (entries.size() == 0) {
			return null;
		}
				
		DuplicateLinkDialogPanel panel = new DuplicateLinkDialogPanel(entries, link);		
		int answer = getDuplicateLinkDialogAnswer(panel);
		
		BibtexEntry entry = panel.getSelectedEntry();		
		if (answer == JOptionPane.OK_OPTION) {
			removeDuplicateLinks(file, entry);
			ReferencesController.getController().getJabrefWrapper().getBasePanel().runCommand("save");
			ReferencesController.getController().getJabRefAttributes().setNodeDirty(true);
			return entry;
		}
		else {			
			throw new ResolveDuplicateEntryAbortedException(file);
		}
	}	
		
	private int getDuplicateLinkDialogAnswer(DuplicateLinkDialogPanel panel) {		
		String ok = TextUtils.getText("ok");	
		String ignore = TextUtils.getText("docear.reference.duplicate.ignore");
		
		String[] options = {ok, ignore};
			
		int answer = JOptionPane.showOptionDialog(UITools.getFrame(), panel, TextUtils.getText("docear.reference.duplicate_url.title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		
		return answer;
	}
	
	public List<String> retrieveFileLinksFromEntry(BibtexEntry entry) {
		String jabrefFiles = entry.getField(GUIGlobals.FILE_FIELD);
		if (jabrefFiles != null) {
			// path linked in jabref
			return JabRefAttributes.parsePathNames(entry, jabrefFiles);
		}
		return Collections.emptyList();
	}
	
	private void removeDuplicateLinks(Object link, BibtexEntry entry) {
		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();

		Iterator<BibtexEntry> iter = database.getEntries().iterator();
		while (iter.hasNext()) {
			BibtexEntry item = iter.next();
			if (item != entry) {
				if (url != null) {
					ReferencesController.getController().getJabRefAttributes().removeUrlFromBibtexEntry(url, item);
				}
				else {
					ReferencesController.getController().getJabRefAttributes().removeFileromBibtexEntry(file, item);
				}
			}
		}
	}
	
}
