package org.docear.plugin.bibtex.jabref;

import java.awt.Color;
import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.BibtexFields;
import net.sf.jabref.Globals;
import net.sf.jabref.JabRef;
import net.sf.jabref.JabRefFrame;
import net.sf.jabref.Util;
import net.sf.jabref.export.DocearReferenceUpdateController;
import net.sf.jabref.export.SaveDatabaseAction;
import net.sf.jabref.export.SaveSession;
import net.sf.jabref.external.FileLinksUpgradeWarning;
import net.sf.jabref.imports.CheckForNewEntryTypesAction;
import net.sf.jabref.imports.OpenDatabaseAction;
import net.sf.jabref.imports.ParserResult;
import net.sf.jabref.imports.PostOpenAction;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.actions.DocearHandleDuplicateWarning;
import org.docear.plugin.bibtex.actions.FilePathValidatorAction;
import org.docear.plugin.bibtex.actions.HandleDuplicateKeys;
import org.docear.plugin.bibtex.actions.IPreOpenAction;
import org.docear.plugin.bibtex.listeners.MapViewListener;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.core.util.WinRegistry;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.OneTouchCollapseResizer;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.ui.IMapViewChangeListener;

public class JabrefWrapper extends JabRef implements IMapViewChangeListener {

	private static final int MAX_TRY_OPEN = 5;

	private static ArrayList<IPreOpenAction> preOpenActions = new ArrayList<IPreOpenAction>();
	private static ArrayList<PostOpenAction> postOpenActions = new ArrayList<PostOpenAction>();
	private static ArrayList<PostOpenAction> postParseActions = new ArrayList<PostOpenAction>();

	static {
		preOpenActions.add(new ZoteroAnnoteFieldRemoverAction());
		
		//escape colons and semicolons (not done by Zotero		
		postParseActions.add(new DocearTransformForeignDatabaseAction());		
		// bibtex files exported by mendeley do not contain leading "/" for
		// absolute paths so we do not know if
		// the file contains relative paths or absolute paths
		postParseActions.add(new FilePathValidatorAction());
		// Add the action for checking for new custom entry types loaded from
		// the bib file:
		postOpenActions.add(new CheckForNewEntryTypesAction());
		// Add the action for the new external file handling system in version
		// 2.3:
		postOpenActions.add(new FileLinksUpgradeWarning());
		// Add the action for warning about and handling duplicate BibTeX keys:
		//postOpenActions.add(new HandleDuplicateWarnings());
		//DOCEAR: don't warn, just resolve --> #464
		if (DocearController.getPropertiesController().getBooleanProperty("docear.reference_manager.resolve_duplicate_keys")) {
		    postOpenActions.add(new HandleDuplicateKeys());
		}
		else {
		    postOpenActions.add(new DocearHandleDuplicateWarning());
		}
		
	}

	private static final MapViewListener mapViewListener = new MapViewListener();
	
	private Map<File, JabRefBaseHandle> baseHandles = new HashMap<File, JabRefBaseHandle>();

	private OneTouchCollapseResizer resizer;

	public JabrefWrapper(JFrame frame) {
		super(frame);		
		registerListeners();
		
		this.jrf.getPreferences().put("generateKeysBeforeSaving", "true");
		this.jrf.getPreferences().put("avoidOverwritingKey", "true");
		this.jrf.addJabRefEventListener(new JabrefChangeEventListener());
		this.jrf.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
	}

	public JabRefFrame getJabrefFrame() {
		return this.jrf;
	}
	
	public JPanel getJabrefFramePanel() {
		return this.jrf;
	}
	
	public String getLocalizedColumnName(String s) {		
		String disName = BibtexFields.getFieldDisplayName(s);
        if (disName != null)
            return disName;
        else
            return Util.nCase(s);
	}

	private void registerListeners() {
		getJabrefFrame().getTabbedPane().addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				BasePanel bp = getJabrefFrame().basePanel();
				if (bp != null) {
					updateWindowsRegistry(bp.getFile());
				}
			}
		});
		
		Controller.getCurrentController().getMapViewManager().addMapViewChangeListener(this);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				synchronized (Controller.getCurrentModeController().getMapController()) {
					
					Controller.getCurrentModeController().getMapController().addNodeSelectionListener(mapViewListener);
				}
			}
		});
	}

	public BasePanel getBasePanel() {
		return (BasePanel) getJabrefFrame().getTabbedPane().getSelectedComponent();
	}

	public BibtexDatabase getDatabase() {
		if (getBasePanel() == null) {
			return null;
		}
		return getBasePanel().getDatabase();
	}

	public BasePanel addNewDatabase(ParserResult pr, File base, boolean raisePanel) {
		File file = base.getAbsoluteFile();
		String fileName = file.getPath();
		BibtexDatabase database = pr.getDatabase();
		
		database.addDatabaseChangeListener(ReferencesController.getJabRefChangeListener());
		
		BasePanel bp;
		// dirty hack, nimbus is sometimes not loaded fast enough
		try {
			bp = new BasePanel(getJabrefFrame(), database, file, pr.getMetaData(), pr.getEncoding());
		}
		catch (Exception e) {
			LogUtils.info("JabrefWrapper.addNewDatabase(): database could not be loaded, trying again in 500 ms");
			try {
				Thread.sleep(800);
			}
			catch (InterruptedException e1) {
			}
			bp = new BasePanel(getJabrefFrame(), database, file, pr.getMetaData(), pr.getEncoding());
		}

		// file is set to null inside the EventDispatcherThread
		// SwingUtilities.invokeLater(new OpenItSwingHelper(bp, file,
		// raisePanel));
		
		getJabrefFrame().addTab(bp, file, raisePanel);

		LogUtils.info(Globals.lang("Opened database") + " '" + fileName + "' " + Globals.lang("with") + " "
				+ database.getEntryCount() + " " + Globals.lang("entries") + ".");

		return bp;
	}

	public JabRefBaseHandle openDatabase(File baseFile, boolean raisePanel) {
		long time = System.currentTimeMillis();
		JabRefBaseHandle handle = null;
		if(baseFile == null) {
			throw new IllegalArgumentException("NULL");
		}
		File file = baseFile.getAbsoluteFile();
		//closeDatabase(file, true);
		if(isOpened(file)) {
			handle = getBaseHandle(file);
			if(handle != null && raisePanel) {
				getJabrefFrame().showBasePanel(handle.getBasePanel());
			}
		}
		else {
			handle = openIt(file, raisePanel);
			if(handle != null) {
				synchronized (baseHandles ) {
					baseHandles.put(file, handle);
				}
			}
		}
		//DOCEAR - todo: how do we deal with multiple files?
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.RM_BIBTEX_FILE_CHANGE, new Object[] {file, this.getDatabase().getEntries().size()});
		LogUtils.info("database "+baseFile+" loaded in: "+(System.currentTimeMillis()-time));
		return handle;
	}
	
	public JabRefBaseHandle getBaseHandle(File baseFile) {
		if(baseFile == null) {
			throw new IllegalArgumentException("NULL");
		}
		File file = baseFile.getAbsoluteFile();
		synchronized (baseHandles) {
			if(baseHandles.containsKey(file)) {
				return baseHandles.get(file); 
			}
		}
		return null;
	}
	
	public void closeDatabase(File baseFile) {
		File file = baseFile.getAbsoluteFile();
		closeDatabase(file, false);
	}
	
	public void closeDatabase(JabRefBaseHandle baseHandle) {
		if(baseHandle == null) {
			return;
		}
		if(!baseHandle.hasMoreConnections()) {
			synchronized (baseHandles) {	
				baseHandles.remove(baseHandle.getFile().getAbsoluteFile());
			}
			closeDatabase(baseHandle.getFile());
		}		
	}
	
	private void closeDatabase(File file, boolean silentClose) {
		for(int i=0; i < getJabrefFrame().baseCount(); i++) {
			BasePanel panel = getJabrefFrame().baseAt(i);
			if(panel.getFile().equals(file)) {
				getJabrefFrame().showBaseAt(i);
				getJabrefFrame().closeCurrentTab();
				if(!silentClose) {
					//firePanelRemoved(panel, i);
				}
			}
		}
	}
	
	public boolean isOpened(File baseFile) {
		if(baseFile == null) {
			throw new IllegalArgumentException("NULL");
		}
		File file = baseFile.getAbsoluteFile();
		for(int i=0; i < getJabrefFrame().baseCount(); i++) {
			BasePanel panel = getJabrefFrame().baseAt(i);
			if(panel.getFile().equals(file)) {
				return true;			
			}
		}
		return false;
	}

	private void updateWindowsRegistry(File file) {
		if(Compat.isWindowsOS()) {
			try {
				WinRegistry.createKey(WinRegistry.HKEY_CURRENT_USER, "SOFTWARE\\Docear4Word");
				WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, "SOFTWARE\\Docear4Word", "BibTexDatabase", file.getAbsolutePath());
				WinRegistry.writeStringValue(WinRegistry.HKEY_CURRENT_USER, "ENVIRONMENT", "docear_bibtex_current", file.getAbsolutePath());
			} 
			catch (Exception e) {
				DocearLogger.warn("org.docear.plugin.bibtex.jabref.JabrefWrapper.updateWindowsRegistry(): "+e.getMessage());
			}
		}
	}

	private JabRefBaseHandle openIt(File file, boolean raisePanel) {
		JabRefBaseHandle handle = null;
		for (IPreOpenAction action : preOpenActions) {
			if (action.isActionNecessary(file)) {
				action.performAction(file);
			}
		}
		if ((file != null) && (file.exists())) {
//			if (!isCompatibleToJabref(file)) {
//				JHyperlink hyperlink = new JHyperlink("http://www.docear.org/faqs/how-to-use-mendeley-together-with-docear/",
//						"http://www.docear.org/faqs/how-to-use-mendeley-together-with-docear/");
//				JPanel panel = new JPanel(new BorderLayout());
//				panel.add(new JLabel(TextUtils.getText("jabref_mendeley_incompatible_1")), BorderLayout.NORTH);
//				panel.add(hyperlink, BorderLayout.CENTER);
//				panel.add(new JLabel(TextUtils.getText("jabref_mendeley_incompatible_2")), BorderLayout.SOUTH);
//
//				int option = JOptionPane.showConfirmDialog(UITools.getFrame(), panel,
//
//				TextUtils.getText("jabref_mendeley_incompatible_title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
//				if (option == JOptionPane.YES_OPTION) {
//					return handle;
//				}
//			}
			File fileToLoad = file;
			LogUtils.info(Globals.lang("Opening References") + ": '" + file.getPath() + "'");

			int tryCounter = 0;
			boolean done = false;
			while (!done && tryCounter++ < MAX_TRY_OPEN) {
				String fileName = file.getPath();
				Globals.prefs.put("workingDirectory", file.getPath());
				// Should this be done _after_ we know it was successfully
				// opened?
				ResourceController resourceController = DocearController.getPropertiesController();
				String encoding = resourceController.getProperty("docear_bibtex_encoding", Globals.prefs.get("defaultEncoding"));

				if (Util.hasLockFile(file)) {
					long modTime = Util.getLockFileTimeStamp(file);
					if ((modTime != -1) && (System.currentTimeMillis() - modTime > SaveSession.LOCKFILE_CRITICAL_AGE)) {
						// The lock file is fairly old, so we can offer to
						// "steal" the file:
						int answer = JOptionPane.showConfirmDialog(
								null,
								"<html>" + Globals.lang("Error opening file") + " '" + fileName + "'. "
										+ Globals.lang("File is locked by another JabRef instance.") + "<p>"
										+ Globals.lang("Do you want to override the file lock?"), Globals.lang("File locked"),
								JOptionPane.YES_NO_OPTION);
						if (answer == JOptionPane.YES_OPTION) {
							Util.deleteLockFile(file);
						}
						else
							return handle;
					}
					else if (!Util.waitForFileLock(file, 10)) {
						JOptionPane.showMessageDialog(null, Globals.lang("Error opening file") + " '" + fileName + "'. "
								+ Globals.lang("File is locked by another JabRef instance."), Globals.lang("Error"),
								JOptionPane.ERROR_MESSAGE);
						return handle;
					}

				}
				ParserResult pr;
				try {
					String source = resourceController.getProperty("docear_bibtex_source", "Jabref");
					pr = OpenDatabaseAction.loadDataBase(fileToLoad, encoding, source);
					for (PostOpenAction action : postParseActions) {
						if (action.isActionNecessary(pr)) {
							long time = System.currentTimeMillis();
							action.performAction(null, pr);
							System.out.println(action.getClass().toString()+" time: "+(System.currentTimeMillis()-time)+"ms");
							System.out.println();
						}						
					}
				}
				catch (Exception ex) {
					//__DOCEAR_
					ex.printStackTrace();
					pr = null;
				}
				if ((pr == null) || (pr == ParserResult.INVALID_FORMAT)) {
					LogUtils.warn("ERROR: Could not load file" + file);
					continue;
				}
				else {
					done = true;
					final BasePanel panel = addNewDatabase(pr, file, raisePanel);
					
					panel.markNonUndoableBaseChanged();

					handle = new JabRefBaseHandle(panel, pr);
					
					// After adding the database, go through our list and see if
					// any post open actions needs to be done. For instance,
					// checking
					// if we found new entry types that can be imported, or
					// checking
					// if the database contents should be modified due to new
					// features
					// in this version of JabRef:
					final ParserResult prf = pr;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							performPostOpenActions(panel, prf, true);
						}
					});
				}
			}

		}
		
		DocearController.getController().getDocearEventLogger().appendToLog(this, DocearLogEvent.RM_BIBTEX_FILE_OPEN, new Object[] {file});
		return handle;
	}

	// JabRef does not use character escaping of "{" and "}"
	// unfortunately all other escapings are not unambiguously or might be set
	// in jabref-preferences too
//	public boolean isCompatibleToJabref(File f) {
//		int escapeCount = 0;
//		int allCount = 0;
//
//		ArrayList<Character> allowedCharsBeforeSlash = new ArrayList<Character>();
//		allowedCharsBeforeSlash.add('\"');
//		allowedCharsBeforeSlash.add('\'');
//		allowedCharsBeforeSlash.add('`');
//		allowedCharsBeforeSlash.add('^');
//		allowedCharsBeforeSlash.add('~');
//
//		RandomAccessFile raf = null;
//		try {
//			//in = new Scanner(new FileReader(f));
//			raf = new RandomAccessFile(f, "r");
//			boolean isWin = Compat.isWindowsOS();
//			String line = null;
//			while ((line = raf.readLine()) != null) {
//				String normalized = line.trim().toLowerCase();
//				if (isWin && normalized.startsWith("file")) {
//					if (normalized.contains("backslash$:")) {
//						return false;
//					}
//				}
//				if (normalized.startsWith("journal") || normalized.startsWith("title") || normalized.startsWith("booktitle")) {
//					int pos = 0;
//					int i = 0;
//
//					String s = normalized.substring(normalized.indexOf("=") + 1).trim();
//					while (s.charAt(pos) == '{') {
//						pos++;
//					}
//					while ((i = s.indexOf("{", pos)) >= 0) {
//						pos = (i + 1);
//						if (allowedCharsBeforeSlash.contains(s.charAt(i - 1))) {
//							continue;
//						}
//						allCount++;
//
//					}
//
//					pos = 0;
//					i = 0;
//					while ((i = s.indexOf("\\{", pos)) >= 0) {
//						escapeCount++;
//						pos = (i + 1);
//					}
//				}
//			}
//		}
//		catch (IOException e) {
//			e.printStackTrace();
//		}
//		finally {
//			try {
//				//in.close();
//				raf.close();
//			}
//			catch (Exception e) {
//				LogUtils.warn(e);
//			}
//		}
//
//		// if no escaped and no unescaped char sequence was found in the whole
//		// file we assume it to be ok for usage in jabref
//		if (allCount / 2 >= escapeCount) {
//			return true;
//		}
//		return false;
//	}

	/**
	 * Go through the list of post open actions, and perform those that need to
	 * be performed.
	 * 
	 * @param panel
	 *            The BasePanel where the database is shown.
	 * @param pr
	 *            The result of the bib file parse operation.
	 */
	public static void performPostOpenActions(BasePanel panel, ParserResult pr, boolean mustRaisePanel) {
		DocearReferenceUpdateController.lock();
		
		try {
    		for (Iterator<PostOpenAction> iterator = postOpenActions.iterator(); iterator.hasNext();) {
    			PostOpenAction action = iterator.next();
    			if (action.isActionNecessary(pr)) {
    				if (mustRaisePanel)
    					panel.frame().getTabbedPane().setSelectedComponent(panel);
    				action.performAction(panel, pr);
    			}
    		}
		}
		finally {
			DocearReferenceUpdateController.unlock();
		}
	}

	public void afterViewChange(Component oldView, Component newView) {
	}

	public void afterViewClose(final Component oldView) {
		oldView.removeMouseListener(mapViewListener);
	}

	public void afterViewCreated(final Component mapView) {
		mapView.addMouseListener(mapViewListener);
	}

	public void beforeViewChange(Component oldView, Component newView) {
	}

	public void shutdown() {
		for (JabRefBaseHandle handle : baseHandles.values()) {
			try {
				BibtexDatabase database = handle.getBasePanel().getDatabase();
				if(database == null) {
					return;
				}
				for (BibtexEntry entry : database.getEntries()) {
					if (entry.getField("docear_add_to_node") != null) {
						entry.setField("docear_add_to_node", null);
					}
				}
				if(ReferencesController.getController().getJabrefWrapper().getBasePanel().isUpdatedExternally()){
					DocearController.getController().addWorkingThreadHandle("ReferenceQuitAction");
					SaveDatabaseAction saveAction = new SaveDatabaseAction(handle.getBasePanel());
					saveAction.runCommand();
					if (saveAction.isCancelled() || !saveAction.isSuccess()) {						
						DocearController.getController().getEventQueue().dispatchEvent(new DocearEvent(this, null, DocearEventType.APPLICATION_CLOSING_ABORTED));
					}
					DocearController.getController().removeWorkingThreadHandle("ReferenceQuitAction");
				}
				else{
					handle.getBasePanel().runCommand("save");
				}
			}
			catch (Throwable t) {
				LogUtils.warn(t);
			}
		}
		
		getJabrefFrame().quit();
	}

	public OneTouchCollapseResizer getResizer() {
		if(this.resizer == null) {
			this.resizer = OneTouchCollapseResizer.findResizerFor(getJabrefFrame());
		}
		return this.resizer;
	}

//	public void addBaseHandleForFile(File baseFile, IJabrefChangeListener listener) {
//		if(listener == null || baseFile == null) {
//			return;
//		}
//		File file = baseFile.getAbsoluteFile();
//		synchronized (baseHandles ) {
//			List<IJabrefChangeListener> list = baseHandles.get(file);
//			if(list == null) {
//				list = new ArrayList<IJabrefChangeListener>();
//				baseHandles.put(file, list);
//			}
//			if(list.contains(listener)) {
//				return;
//			}
//			list.add(listener);
//		}		
//	}
	
//	public void removeBaseHandleForFile(File baseFile, IJabrefChangeListener listener) {
//		if(listener == null || baseFile == null) {
//			return;
//		}
//		File file = baseFile.getAbsoluteFile();
//		synchronized (baseHandles ) {
//			List<IJabrefChangeListener> list = baseHandles.get(file);
//			if(list == null) {
//				return;
//			}
//			list.remove(listener);
//		}	
//	}
}
