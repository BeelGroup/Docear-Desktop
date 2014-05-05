package org.docear.plugin.bibtex.actions;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;


import org.docear.plugin.bibtex.ReferencesController;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;

@EnabledAction(checkOnNodeChange=true)
public class CopyBibtexToClipboard extends AFreeplaneAction{
	public static final String KEY = "CopyBibtexKeyToClipboardAction";
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CopyBibtexToClipboard() {
		super(KEY);
	}

	public void actionPerformed(ActionEvent e) {
		Collection<NodeModel> nodes = Controller.getCurrentModeController().getMapController().getSelectedNodes();
		Collection<String> keySet = getKeySet(nodes);
		final String bibtexKeys = serializeStringSet(keySet);
		Transferable content = new Transferable() {
			public boolean isDataFlavorSupported(DataFlavor flavor) {
				return DataFlavor.stringFlavor.equals(flavor);
			}
			
			public DataFlavor[] getTransferDataFlavors() {
				return new DataFlavor[]{DataFlavor.stringFlavor};
			}
			
			public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
				if(DataFlavor.stringFlavor.equals(flavor)) {
					return bibtexKeys;
				}
				throw new UnsupportedFlavorException(flavor);
			}
		};
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(content, null);
		
	}

	public static Collection<String> getKeySet(Collection<NodeModel> nodes) {
		Set<String> keys = new HashSet<String>();
		for(NodeModel node : nodes) {
			String bibKey = ReferencesController.getController().getJabRefAttributes().getBibtexKey(node);
			if(bibKey != null) {
				keys.add(bibKey);
			}
		}
		return keys;
	}
	
	public static String serializeStringSet(Collection<String> strSet) {
		StringBuffer strBuffer = new StringBuffer();
		for (String string : strSet) {
			if(strBuffer.length() > 0) {
				strBuffer.append(",");
			}
			strBuffer.append(string);
		}
		return strBuffer.toString();
	}
	
	public void setEnabled() {
		setEnabled(true);
	}
	
	public static boolean hasReferences(Collection<NodeModel> nodes) {
		if (nodes != null) {
			for(NodeModel node : nodes) {		
				final String bibtexKey = ReferencesController.getController().getJabRefAttributes().getBibtexKey(node);
				if (bibtexKey != null && bibtexKey.length()>0) {
					return true;
				}
			}
		}
		return false;
	}
}
