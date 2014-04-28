package org.docear.plugin.bibtex.jabref;

import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import javax.ws.rs.core.MultivaluedMap;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.BibtexEntryType;
import net.sf.jabref.EntryTypeDialog;
import net.sf.jabref.FocusRequester;
import net.sf.jabref.Globals;
import net.sf.jabref.JabRefFrame;
import net.sf.jabref.KeyCollisionException;
import net.sf.jabref.SearchManager2;
import net.sf.jabref.Util;
import net.sf.jabref.export.DocearReferenceUpdateController;
import net.sf.jabref.external.DroppedFileHandler;
import net.sf.jabref.gui.MainTable;
import net.sf.jabref.labelPattern.LabelPatternUtil;
import net.sf.jabref.undo.UndoableInsertEntry;
import net.sf.jabref.util.XMPUtil;

import org.docear.plugin.bibtex.Reference;
import org.docear.plugin.bibtex.ReferenceUpdater;
import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.actions.MetaDataAction;
import org.docear.plugin.bibtex.actions.MetaDataAction.MetaDataActionObject;
import org.docear.plugin.bibtex.actions.MetaDataAction.MetaDataActionResult;
import org.docear.plugin.bibtex.dialogs.PdfMetadataListDialog;
import org.docear.plugin.bibtex.dialogs.PdfTitleQuestionDialog;
import org.docear.plugin.core.mindmap.MindmapUpdateController;
import org.docear.plugin.core.util.CoreUtils;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.io.StringOutputStream;

import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;

public abstract class JabRefCommons {
	
	static class MetadataRequestTask implements Callable<MetadataCallableResult> {
		private MetadataCallableResult result;
		private Runnable task;

		private MetadataRequestTask(Runnable task, MetadataCallableResult result) {
			this.task = task;
			this.result = result;
		}

		public static Callable<MetadataCallableResult> create(Runnable task, MetadataCallableResult result) {
			return new MetadataRequestTask(task, result);
		}

		public MetadataCallableResult call() throws Exception {
			if (task == null) {
				return this.result;
			}
			task.run();
			return this.result;
		}

	}

	public static class MetadataCallableResult {
		private String result;
		private String errorText;
		DocearServiceResponse.Status status;

		public static MetadataCallableResult newInstance() {
			return new MetadataCallableResult();
		}

		public void setResult(String string) {
			if (string == null || string.trim().length() <= 0) {
				this.result = null;
			} else {
				this.result = string;
			}
		}

		public String getResult() {
			return this.result;
		}

		public void setError(String text) {
			this.errorText = text;
		}
		
		public DocearServiceResponse.Status getStatus() {
			return this.status;
		}

		public void setStatus(DocearServiceResponse.Status status) {
			this.status = status;
		}

		public boolean hasError() {
			return this.errorText != null;
		}

		public String getError() {
			return errorText;
		}

		public String toString() {
			return getResult();
		}

	}

	private static void updateEntryInDatabase(File file, BibtexEntry selected, BibtexEntry oldEntry) {
		if (selected == null) {
			return;
		}
		BibtexEntryType type = selected.getType();
		if (type != null) {
			oldEntry.setType(type);
		}

		addMissingFields(oldEntry, selected);
//		insertFields(oldEntry.getRequiredFields(), oldEntry, selected);
//		insertFields(oldEntry.getGeneralFields(), oldEntry, selected);
//		insertFields(oldEntry.getOptionalFields(), oldEntry, selected);

		JabrefWrapper wrapper = ReferencesController.getController().getJabrefWrapper();
		if(file != null) {
			new JabRefAttributes().removeFileromBibtexEntry(file, oldEntry);
			DroppedFileHandler dfh = new DroppedFileHandler(wrapper.getJabrefFrame(), wrapper.getBasePanel());
			// DOCEAR - change file path to relative to bib-library path?
			dfh.linkPdfToEntry(file.getPath(), oldEntry);
		}
		else {
			runCurrentMapUpdate();
		}
		showInReferenceManager(oldEntry, false);
	}
	
	private static void runCurrentMapUpdate() {
		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				try {
					if (DocearReferenceUpdateController.isLocked()) {
						return;
					}
					DocearReferenceUpdateController.lock();
					
					MapModel currentMap = Controller.getCurrentController().getMap();
					if (currentMap == null) {
						return;
					}

					MindmapUpdateController mindmapUpdateController = new MindmapUpdateController(false);
					mindmapUpdateController.addMindmapUpdater(new ReferenceUpdater(TextUtils.getText("update_references_current_mindmap")));
					mindmapUpdateController.updateCurrentMindmap(true);
				}catch(Exception e){
					LogUtils.warn(e.getMessage());
				}				
				finally {
					DocearReferenceUpdateController.unlock();
				}
			}

		});
	}

	private static void addMissingFields(BibtexEntry oldEntry, BibtexEntry newData) {
		DocearReferenceUpdateController.lock();		
		for(String field : oldEntry.getAllFields()){
			oldEntry.clearField(field);
		}
		for (String field : newData.getAllFields()) {
			/*if (oldEntry.getField(field) == null) {
				oldEntry.setField(field, newData.getField(field));
			}*/
			oldEntry.setField(field, newData.getField(field));
		}
		DocearReferenceUpdateController.unlock();
	}
	
	

	
	
	public static List<String> addOrUpdateRefenceEntry(String[] fileNames, int dropRow, JabRefFrame jabRefFrame, BasePanel basePanel, MainTable entryTable, boolean chooseFirst) {
		
		MetaDataActionObject result =  new MetaDataAction.MetaDataActionObject();
		if(fileNames == null) return new ArrayList<String>();
		BibtexDatabase database = ReferencesController.getController().getJabrefWrapper().getDatabase();		
		BibtexEntry dropEntry = null;
		if(dropRow >= 0){
			dropEntry = entryTable.getEntryAt(dropRow);
		}
		for(String fileName : fileNames){
			if (fileName != null && fileName.toLowerCase().endsWith(".pdf")) {
				BibtexEntry existingEntry = null;
				URI fileUri = new File(fileName).toURI();
				for (BibtexEntry entry : database.getEntries()) {
					URL entryUrl = null;
					String urlString = entry.getField("url");
					try {
						if (urlString != null) {
							entryUrl = new URL(urlString);
						}
						if (fileUri.toURL().equals(entryUrl)) {
							existingEntry = entry;
						}
					}
					catch (MalformedURLException e) {
						LogUtils.info(urlString + ": " + e.getMessage());
					}					
					for (String jabrefPath : DuplicateResolver.getDuplicateResolver().retrieveFileLinksFromEntry(entry)) {
						File jabrefFile = new File(jabrefPath);
						if (jabrefFile != null && jabrefFile.getName().equals(new File(fileName).getName())) {
							existingEntry = entry;
							break;
						}
					}
					if(existingEntry != null) break;
				}
				MetaDataActionResult fileResult = new MetaDataAction.MetaDataActionResult();
				if(dropEntry != null && existingEntry != null){
					fileResult.setDuplicatePdf(true);
					fileResult.setShowattachOnlyOption(true);
					fileResult.setEntryToUpdate(dropEntry);
				}
				else if(dropEntry == null && existingEntry != null){
					fileResult.setEntryToUpdate(existingEntry);
				}
				else if(dropEntry != null && existingEntry == null){
					fileResult.setEntryToUpdate(dropEntry);
					fileResult.setShowattachOnlyOption(true);
				}
				result.getResult().put(fileUri, fileResult);
			}
			else{
				result.getUnhandledFiles().add(fileName);
			}
		}
		
		MetaDataAction.showDialog(result);
		
		for(URI file :  result.getResult().keySet()){
			MetaDataActionResult fileResult = result.getResult().get(file);
			if(fileResult.isSelectedCancel()) continue;
			if(fileResult.isAttachOnly()){
				DroppedFileHandler dfh = new DroppedFileHandler(jabRefFrame, basePanel);
				dfh.linkPdfToEntry(CoreUtils.resolveURI(file).getAbsolutePath(), fileResult.getEntryToUpdate());
			}
			if(fileResult.isSelectedBlank()){
				addOrUpdateEntryToDatabase(CoreUtils.resolveURI(file), new BibtexEntry());
							
			}
			else if((fileResult.isSelectedFetched() || fileResult.isSelectedXmp()) && fileResult.getResultEntry() != null){
				addOrUpdateEntryToDatabase(CoreUtils.resolveURI(file), fileResult.getResultEntry());					
			}
			
		}
		
		return result.getUnhandledFiles();
		
//		List<String> unhandledFileNames = new ArrayList<String>();
//		if (fileNames != null && fileNames.length > 0) {			
//			for (String fileName : fileNames) {
//
//				// create document hash and try to extract the title for
//				// each file that is of type pdf
//				if (fileName.toLowerCase().endsWith(".pdf")) {
//					URI fileUri = new File(fileName).toURI();
//					
//					ImportDialog importDialog = new ImportDialog(dropRow, fileName, (chooseFirst ? (dropRow < 0) : null));
//					Tools.centerRelativeToWindow(importDialog, UITools.getFrame());
//					
//					String hash = AnnotationController.getDocumentHash(fileUri);
//					if(hash == null) {
//						importDialog.getRadioButtonMrDlib().setEnabled(false);
//						importDialog.getRadioButtonUpdateEmptyFields().setEnabled(false);
//						importDialog.getRadioButtonMrDlib().setSelected(false);
//						importDialog.getRadioButtonNoMeta().setSelected(true);
//					}
//					
//					List<BibtexEntry> xmpEntriesInFile = readXmpEntries(fileName);
//					if ((xmpEntriesInFile == null) || (xmpEntriesInFile.size() == 0)) {
//						importDialog.getRadioButtonXmp().setEnabled(false);
//					}
//					
//					if(chooseFirst) {
//						importDialog.showDialog();						
//					}
//					else if (dropRow == -1 && hash != null) { // dropped on a new area
//						// create new entry (with metadata? empty entry?
//						try {
//							showMetadataDialog(fileUri);
//						} catch (Exception e) {
//							LogUtils.warn("exception in JabRefCommons.addOrUpdateRefenceEntry() 1 : " + e.getMessage());
//						}
//						continue;						
//					} 
//					
//					// dropped on an existing entry
//					if(!chooseFirst) {
//						importDialog.showDialog();						
//					}
//					if (importDialog.getResult() == JOptionPane.OK_OPTION) {
//						// xmp metadata was selected
//						if (importDialog.getRadioButtonXmp().isSelected()) {
//							PrintStream old_err = System.err;
//							try {
//								System.setErr(new PrintStream(new StringOutputStream(), false));
//								ImportMenuItem importer = new ImportMenuItem(jabRefFrame, false);
//								importer.automatedImport(new String[] { fileName });
//							} 
//							finally {
//								System.setErr(old_err);
//							}
//						}
//						// docear services was selected
//						else if (importDialog.getRadioButtonMrDlib().isSelected()) {
//							try {
//								showMetadataDialog(fileUri);
//							} catch (Exception e) {
//								LogUtils.warn("exception in JabRefCommons.addOrUpdateRefenceEntry() 2 : " + e.getMessage());
//							}
//						} else {
//							
//							if (importDialog.getRadioButtonNoMeta().isSelected()) {
//								BibtexEntry newEntry = JabRefCommons.createNewEntry(jabRefFrame, basePanel);
//								if (newEntry != null) {
//									DroppedFileHandler dfh = new DroppedFileHandler(jabRefFrame, basePanel);
//									dfh.linkPdfToEntry(fileName, newEntry);
//								}
//							}
//							// update was selected
//							else if (importDialog.getRadioButtonUpdateEmptyFields().isSelected()) {
//								try {
//									showMetadataUpdateDialog(fileUri, entryTable.getEntryAt(dropRow));
//								} catch (Exception e) {
//									LogUtils.warn("exception in JabRefCommons.addOrUpdateRefenceEntry() 3: " + e.getMessage());
//								}
//							}
//							// attach file only was selected
//							else if (importDialog.getRadioButtononlyAttachPDF().isSelected()) {
//								DroppedFileHandler dfh = new DroppedFileHandler(jabRefFrame, basePanel);
//								dfh.linkPdfToEntry(fileName, entryTable.getEntryAt(dropRow));
//							}
//						}
//					}
//				} else {
//					// add filename to fallback list
//					unhandledFileNames.add(fileName);
//				}
//			}	
//		}
//		return unhandledFileNames;
	}
	
	public static List<BibtexEntry> readXmpEntries(String fileName) {
		List<BibtexEntry> xmpEntriesInFile = new ArrayList<BibtexEntry>();
		PrintStream err = System.err;
		System.setErr(new PrintStream(new StringOutputStream()));
		try {
			xmpEntriesInFile = XMPUtil.readXMP(fileName);
		} catch (Throwable e) {
			LogUtils.warn("exception in JabRefCommons.readXmpEntries(): " + e.getMessage());
		}
		finally {
			System.setErr(err);
		}
		return xmpEntriesInFile;
	}
	
	public static void showMetadataDialog(URI uri) throws InterruptedException, ExecutionException, IOException {
		String userName = ServiceController.getCurrentUser().getUsername();
//		if (userName == null) {
//			JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.metadata.import.requirement_failed"));
//			return;
//		}

		final String hash = AnnotationController.getDocumentHash(uri);
		if (hash == null) {
			JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.metadata.import.no_hash"));
			return;
		}

		// ask for title dialog
		String title = searchForTitle(AnnotationController.getDocumentTitle(uri), uri);
		if (title == null) {
			return;
		}

		File file = new File(uri);
		final MultivaluedMap<String, String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
		if (userName != null) {
			params.add("username", userName);
		}
		if (title != null) {
			params.add("title", title);
		}

		
		PdfMetadataListDialog metadata = new PdfMetadataListDialog();
		metadata.runServiceRequest(hash, params);
		int response = JOptionPane.showConfirmDialog(UITools.getFrame(), metadata, TextUtils.getText("docear.metadata.import.title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if(metadata.wasSuccessful()) //does not work anymore with errors shown right away
		{
			if (response == JOptionPane.OK_OPTION) {
				Util.setAutomaticFields(metadata.getEntries(), true, true, false);
				BibtexEntry selected = metadata.getSelectedEntry();
				selected.setField("dcr_pdf_hash", hash);
				addOrUpdateEntryToDatabase(file, selected);
				if (metadata.hasRemoteBib()) {
					commit(selected.getField("dcr_bibtex_id"), hash, userName);
				}
			} else {
				if (metadata.hasRemoteBib()) {
					rejectAll(hash, userName);
				}
			}
		}
	}
	
	public static void showMetadataUpdateDialog(URI uri, BibtexEntry oldEntry) throws InterruptedException, ExecutionException, IOException {
		String userName = ServiceController.getCurrentUser().getUsername();
//		if (userName == null) {
//			JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.metadata.import.requirement_failed"));
//			return;
//		}

		final String hash = AnnotationController.getDocumentHash(uri);
		if (hash == null) {
			JOptionPane.showMessageDialog(UITools.getFrame(), TextUtils.getText("docear.metadata.import.no_hash"));
			return;
		}

		// ask for title dialog
		String title = searchForTitle(AnnotationController.getDocumentTitle(uri), uri);
		if (title == null) {
			return;
		}

		File file = new File(uri);
		final MultivaluedMap<String, String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
		params.add("username", userName);
		if (title != null) {
			params.add("title", title);
		}
		
		PdfMetadataListDialog metadata = new PdfMetadataListDialog();
		metadata.runServiceRequest(hash, params);
		int response = JOptionPane.showConfirmDialog(UITools.getFrame(), metadata, TextUtils.getText("docear.metadata.import.title"),
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if(metadata.wasSuccessful()) //does not work anymore with errors shown right away
		{
			if (response == JOptionPane.OK_OPTION) {
				BibtexEntry selected = metadata.getSelectedEntry();
				selected.setField("dcr_pdf_hash", hash);
				updateEntryInDatabase(file, selected, oldEntry);
				if (metadata.hasRemoteBib()) {
					commit(selected.getField("dcr_bibtex_id"), hash, userName);
				}
			} else {
				if (metadata.hasRemoteBib()) {
					rejectAll(hash, userName);
				}
			}
		}
			
	}

	public static String searchForTitle(String title, URI fileUri) {
		PdfTitleQuestionDialog titleDialog = new PdfTitleQuestionDialog(title == null ? "" : title, fileUri);
		int searchGo = JOptionPane.showConfirmDialog(UITools.getFrame(), titleDialog, TextUtils.getText("docear.metadata.title.title"),
				JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (searchGo == JOptionPane.YES_OPTION) {
			return titleDialog.getTitle();
		}
		return null;
	}

	private static void commit(final String bibtexID, final String hash, final String userName) {
		final MetadataCallableResult result = MetadataCallableResult.newInstance();
		Runnable task = new Runnable() {
			public void run() {
				try {
					MultivaluedMap<String, String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
					params.add("username", userName);
					params.add("commit", "true");
					params.add("id", bibtexID);
					DocearServiceResponse serviceResponse = ServiceController.getConnectionController().put("/internal/documents/" + hash + "/metadata", params);
					if (serviceResponse.getStatus() != DocearServiceResponse.Status.OK) {
						LogUtils.info("JabRefCommons.commit(...).new Runnable() {...}.run(): " + serviceResponse.getContentAsString());
					}
				} catch (Throwable e) {
					// JOptionPane.showMessageDialog(UITools.getFrame(),
					// e.getLocalizedMessage(),
					// TextUtils.getText("docear.metadata.import.error"),
					// JOptionPane.ERROR_MESSAGE);
					result.setError(e.getLocalizedMessage());
				}
			}
		};
		try {
			executeTask(MetadataRequestTask.create(task, result));
		} catch (Exception e) {
			LogUtils.info("JabRefCommons.commit(...).new Runnable() {...}.run(): " + e.getLocalizedMessage());
			LogUtils.warn(e);
		}
	}

	private static void rejectAll(final String hash, final String userName) {
		final MetadataCallableResult result = MetadataCallableResult.newInstance();
		Runnable task = new Runnable() {
			public void run() {
				try {
					MultivaluedMap<String, String> params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
					params.add("username", userName);
					params.add("commit", "false");
					DocearServiceResponse serviceResponse = ServiceController.getConnectionController().put("/internal/documents/" + hash + "/metadata", params);
					if (serviceResponse.getStatus() != DocearServiceResponse.Status.OK) {
						if(serviceResponse.getStatus() == DocearServiceResponse.Status.UNAUTHORIZED) {
							//DOCEAR - TODO: show wizard with registration button
						}
						else {							
							LogUtils.info("JabRefCommons.rejectAll(...).new Runnable() {...}.run(): " + serviceResponse.getContentAsString());
						}
					}
				} catch (Throwable e) {
					// JOptionPane.showMessageDialog(UITools.getFrame(),
					// e.getLocalizedMessage(),
					// TextUtils.getText("docear.metadata.import.error"),
					// JOptionPane.ERROR_MESSAGE);
					result.setError(e.getLocalizedMessage());
				}
			}
		};
		try {
			executeTask(MetadataRequestTask.create(task, result));
		} catch (Exception e) {			
			LogUtils.info("exception in JabRefCommons.rejectAll(): " + e.getLocalizedMessage());
			LogUtils.warn(e);
		}

	}

	public static MetadataCallableResult requestBibTeX(final String hash, final MultivaluedMap<String, String> params) throws InterruptedException, ExecutionException, IOException {
		final MetadataCallableResult result = MetadataCallableResult.newInstance();
		Runnable task = new Runnable() {
			public void run() {
				try {
					StringBuilder sb = new StringBuilder();
					DocearServiceResponse serviceResponse = ServiceController.getConnectionController().get("/internal/documents/" + hash + "/metadata", params);
					result.setStatus(serviceResponse.getStatus());
					if (serviceResponse.getStatus() == DocearServiceResponse.Status.FAILURE || serviceResponse.getStatus() == DocearServiceResponse.Status.UNAUTHORIZED) {
						result.setError(serviceResponse.getContentAsString());
						return;
					}
					if (serviceResponse.getStatus() == DocearServiceResponse.Status.NO_CONTENT) {
						result.setError(TextUtils.getText("docear.metadata.import.infotext"));
						return;
					}

					InputStream is = serviceResponse.getContent();// this.getClass().getResourceAsStream("/bibtex-test.bib");
					Reader reader = new InputStreamReader(is);
					int c = -1;
					while ((c = reader.read()) > -1) {
						sb.append((char) c);
					}
					is.close();
					result.setResult(sb.toString());
				} catch (Throwable e) {
					JOptionPane.showMessageDialog(UITools.getFrame(), e.getLocalizedMessage(), TextUtils.getText("docear.metadata.import.error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		executeTask(MetadataRequestTask.create(task, result));
		return result;
	}

	private static MetadataCallableResult executeTask(Callable<MetadataCallableResult> task) throws InterruptedException, ExecutionException {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		MetadataCallableResult taskResult = null;
		try {
			Future<MetadataCallableResult> future = executor.submit(task);
			taskResult = future.get(5, TimeUnit.SECONDS);
			future.cancel(true);
		} catch (TimeoutException tex) {
		}
		executor.shutdown();
		return taskResult;
	}

	private static void addOrUpdateEntryToDatabase(File file, BibtexEntry selected) {
		if (selected == null) {
			return;
		}
		JabrefWrapper wrapper = ReferencesController.getController().getJabrefWrapper();
		BibtexEntry oldEntry = null;
		if(file != null) {
			for(BibtexEntry entry : wrapper.getDatabase().getEntries()) {
				Reference ref = new Reference(wrapper.getBasePanel(), entry);
				if(ref.containsFile(file)) {
					oldEntry = entry;
					break;
				}
			}
		}
		if(oldEntry == null) {
			selected.setId(Util.createNeutralId());
			wrapper.getBasePanel().getDatabase().insertEntry(selected);
			showInReferenceManager(selected, false);
			DroppedFileHandler dfh = new DroppedFileHandler(wrapper.getJabrefFrame(), wrapper.getBasePanel());
			
			if(file != null) {
				// DOCEAR - change file path to relative to bib-library path?
				dfh.linkPdfToEntry(file.getPath(), selected);
				LabelPatternUtil.makeLabel(Globals.prefs.getKeyPattern(), wrapper.getDatabase(), selected);
			}
		}
		else {
			JabRefCommons.updateEntryInDatabase(file, selected, oldEntry);
			showInReferenceManager(oldEntry, false);
		}
		
		
	}
	
	public static void showInReferenceManager(String bibtexKey) {
			showInReferenceManager(bibtexKey, false);
	}
	
	public static BibtexEntry showInReferenceManager(String bibtexKey, boolean keepSelected) {
		if (bibtexKey != null && bibtexKey.length()>0) {
			JabrefWrapper wrapper = ReferencesController.getController().getJabrefWrapper();
			BibtexEntry referenceEntry = wrapper.getDatabase().getEntryByKey(bibtexKey);
			return showInReferenceManager(referenceEntry, keepSelected);
		}
		return null;
	}
	
	public static void clearSearchFilter() {
		SearchManager2 searcher = (SearchManager2) ReferencesController.getController().getJabrefWrapper().getJabrefFrame().sidePaneManager.getComponent("search");
		searcher.clearSearch();
	}
	
	public static BibtexEntry showInReferenceManager(BibtexEntry referenceEntry, boolean keepSelected) {
		if(referenceEntry == null) {
			return null;
		}
		
		MainTable table = ReferencesController.getController().getJabrefWrapper().getBasePanel().getMainTable();
		List<BibtexEntry> list = table.getTableRows();
		int viewHeight = table.getPane().getHeight()-table.getTableHeader().getHeight();
		Rectangle viewRect = new Rectangle(0,((JViewport)table.getParent()).getViewPosition().y, 4, viewHeight);
		int pos = 0;
		Rectangle rowArea = new Rectangle(); 
		for(BibtexEntry row : list) {
			if(row.equals(referenceEntry)) {
				rowArea.setBounds(0, (table.getRowHeight()*pos), 2, table.getRowHeight());
				if(!keepSelected) {
					table.clearSelection();
				}
				table.addRowSelectionInterval(pos,pos);
				if(isRowOutsideViewArea(viewRect, rowArea)) {
					((JViewport)table.getParent()).setViewPosition(rowArea.getLocation());
				}
				return row;
			}
			pos++;
		}
		return null;
	}
	
	private static boolean isRowOutsideViewArea(final Rectangle viewArea, final Rectangle row) {
		if(viewArea.contains(row)) {
			return false;
		}
		return true;
	}

	public static void addNewRefenceEntry(String[] fileNames, JabRefFrame jabRefFrame, BasePanel basePanel) {
		addOrUpdateRefenceEntry(fileNames, -1, jabRefFrame, basePanel, null, true);
		
	}

	public static BibtexEntry createNewEntry(JabRefFrame frame, BasePanel panel) {		
	    // Find out what type is wanted.
	    EntryTypeDialog etd = new EntryTypeDialog(frame);
	    // We want to center the dialog, to make it look nicer.
	    Util.placeDialog(etd, UITools.getFrame());
	    etd.setVisible(true);
	    BibtexEntryType type = etd.getChoice();
	
	    if (type != null) { // Only if the dialog was not cancelled.
	        String id = Util.createNeutralId();
	        final BibtexEntry be = new BibtexEntry(id, type);
	        if(insertEntry(panel, be)){
	        	return be;
	        }
	        else{
	        	return null;
	        }
	    }
	    return null;
	}

	private static boolean insertEntry(BasePanel panel, final BibtexEntry be) {
		try {
		    panel.database().insertEntry(be);

		    // Set owner/timestamp if options are enabled:
		    ArrayList<BibtexEntry> list = new ArrayList<BibtexEntry>();
		    list.add(be);
		    Util.setAutomaticFields(list, true, true, false);

		    // Create an UndoableInsertEntry object.
		    panel.undoManager.addEdit(new UndoableInsertEntry(panel.database(), be, panel));
		    panel.output(Globals.lang("Added new")+" '"+be.getType().getName().toLowerCase()+"' "
		           +Globals.lang("entry")+".");

		    // We are going to select the new entry. Before that, make sure that we are in
		    // show-entry mode. If we aren't already in that mode, enter the WILL_SHOW_EDITOR
		    // mode which makes sure the selection will trigger display of the entry editor
		    // and adjustment of the splitter.
		    if (panel.getMode() != BasePanel.SHOWING_EDITOR) {
		    	panel.setMode(BasePanel.WILL_SHOW_EDITOR);
		    }

		    panel.showEntry(be);
		    panel.markBaseChanged(); // The database just changed.
		    new FocusRequester(panel.getEntryEditor(be));
		    return true;
		} 
		catch (KeyCollisionException ex) {
			LogUtils.warn("exception in JabRefCommons.insertEntry(): " + ex.getMessage());	            
		}
		return false;
	}

//	private static void insertFields(String[] fields, BibtexEntry entry, BibtexEntry newData) {
//		for (String field : fields) {
//			if (entry.getField(field) == null) {
//				entry.setField(field, newData.getField(field));
//			}
//		}
//	}
}
