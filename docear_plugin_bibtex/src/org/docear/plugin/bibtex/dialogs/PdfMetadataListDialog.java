package org.docear.plugin.bibtex.dialogs;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.SoftBevelBorder;
import javax.ws.rs.core.MultivaluedMap;

import net.sf.jabref.BibtexEntry;
import net.sf.jabref.Globals;
import net.sf.jabref.export.ExportFormats;
import net.sf.jabref.export.layout.LayoutHelper;
import net.sf.jabref.imports.BibtexParser;

import org.docear.plugin.bibtex.ReferencesController;
import org.docear.plugin.bibtex.jabref.JabRefCommons;
import org.docear.plugin.bibtex.jabref.JabRefCommons.MetadataCallableResult;
import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.io.DocearServiceResponse.Status;
import org.docear.plugin.services.features.user.DocearUserController;
import org.docear.plugin.services.features.user.action.DocearUserRegistrationAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class PdfMetadataListDialog extends JPanel {
private static final long serialVersionUID = -627410651667772600L;
	
	private JList list;

	private BibtexEntryListModel listModel;
	private Collection<BibtexEntry> entries;

	private boolean requesting;

	private boolean remoteBib;

	private boolean hasError;

	private JScrollPane scrollPane;

	private boolean bibResults;

	private JLabel lblNewLabel;
	
	public PdfMetadataListDialog() {
		setPreferredSize(new Dimension(640, 200));
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("434px:grow"),},
			new RowSpec[] {
				RowSpec.decode("max(30dlu;default)"),
				RowSpec.decode("fill:151px:grow"),}));
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		add(panel, "1, 1, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				RowSpec.decode("max(20dlu;default)"),
				RowSpec.decode("default:grow"),}));
		
		lblNewLabel = new JLabel(TextUtils.getText("docear.metadata.import.help"));
		panel.add(lblNewLabel, "2, 1");
		
		Calendar cal = Calendar.getInstance(Locale.GERMANY);
		cal.clear();
		cal.set(2014, 2, 15);
		if(cal.after(Calendar.getInstance(Locale.GERMANY))) {
			MultiLineActionLabel message = new MultiLineActionLabel("<b style=\"color: red;\">Do you want better PDF metadata extraction and automatic PDF file renaming? </b><action cmd=\"donate\">Read here...</action>");
			message.setPreferredSize(new Dimension(0, 25));
			message.setFont(message.getFont().deriveFont(Font.BOLD));
			message.setBackground(panel.getBackground());
			message.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					try {
						Controller.getCurrentController().getViewController().openDocument(URI.create("http://www.docear.org/2014/01/23/call-for-donation-automatic-pdf-metadata-extraction-and-renaming/"));
					} catch (IOException ignore) {
					}
				}
			});
			panel.add(message, "1, 2, 3, 1, fill, fill");
		}
				
		scrollPane = new JScrollPane();
		scrollPane.setViewportBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null));
		add(scrollPane, "1, 2, fill, fill");
		
		JLabel lblWaiting = new JLabel(new ImageIcon(this.getClass().getResource("/ajax-loader.gif")), JLabel.CENTER);
		lblWaiting.setBackground(Color.WHITE);
		
		scrollPane.setViewportView(lblWaiting);
		
	}
	
	public BibtexEntry getSelectedEntry() {
		if(list.getSelectedIndex() > -1) {
			return listModel.getEntry(list.getSelectedIndex());
		}
		return null;
	}
	
	private void updateResults() {
		if(entries != null && !bibResults) {
			bibResults = true;
			listModel = new BibtexEntryListModel();		
			list = new JList(listModel);
			list.setSelectedIndex(0);
			list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);			
		
			list.setCellRenderer(new DefaultListCellRenderer() {
				
				private static final long serialVersionUID = 4805846114365117400L;

				public Component getListCellRendererComponent(JList table, Object value, int index, final boolean isSelected, boolean hasFocus) {
					StringBuilder sb = new StringBuilder();
					sb.append("<html><body>");
					sb.append(value);
					sb.append("</body></html>");
					final JLabel label = (JLabel) super.getListCellRendererComponent(table, sb, index, isSelected, hasFocus);
					if(index > 0) {
						label.setBorder(new CompoundBorder(new MatteBorder(2, 0, 0, 0, (Color) new Color(0, 0, 0)), new EmptyBorder(10, 8, 10, 8)));
					}
					else {
						label.setBorder(new EmptyBorder(10, 8, 10, 8));
					}							
					return label;
				}
			});
			scrollPane.setViewportView(list);
			listModel.fireDataChanged();
			if(!hasRemoteBib()) {
				lblNewLabel.setText(TextUtils.getText("docear.metadata.import.help.fallback"));
			}
		}
	}

	public void runServiceRequest(final String hash, final MultivaluedMap<String, String> params) {
		if(!requesting) {
			requesting = true;
			new Thread() {
				public void run() {
					remoteBib = true;
					
					try {
						MetadataCallableResult ret = JabRefCommons.requestBibTeX(hash, params);					

						if (ret.hasError()) {
							hasError = true;
							//Replace with local presentation
							if(ServiceController.getCurrentUser() == DocearUserController.LOCAL_USER) {
								showErrorMessage(ret.getError(), ret.getStatus() == Status.UNAUTHORIZED);
							}
						}					
					
						String bib = ret.getResult();
						// if no bibtex found -> create misc entry with title only
						if (bib == null) {
							remoteBib = false;
							StringBuilder sb = new StringBuilder();
							sb.append("@ARTICLE{,\n  title = {");
							sb.append(params.get("title").get(0));
							sb.append("}\n}");
							bib = sb.toString();
						}

						entries = BibtexParser.fromString(bib);
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								updateResults();
							}
						});
					} catch (Exception e) {						
						hasError = true;
					}
				}
			}.start();
		}
	}
	
	private void showErrorMessage(String message, boolean showRegistration) {
		Window window = SwingUtilities.getWindowAncestor(PdfMetadataListDialog.this);
		if(window != null && window != UITools.getFrame()) {
			PdfMetadataListDialog.this.dispatchEvent(new WindowEvent(window, WindowEvent.WINDOW_CLOSING));
			window.setVisible(false);
		}
		final Wizard wizard = new Wizard(UITools.getFrame());
		
		final Object regId = DocearUserRegistrationAction.addRegistrationPages(wizard);
		MetadataErrorPage page = new MetadataErrorPage(message, showRegistration);
		//JOptionPane.showMessageDialog(UITools.getFrame(), message , , JOptionPane.ERROR_MESSAGE);
		
		WizardPageDescriptor desc = new WizardPageDescriptor("error_message", page) {

			@Override
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				wizard.cancel();
				return null;
			}

			@Override
			public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {
				return context.getModel().getPage(regId);
			}
			
		};
		wizard.registerWizardPanel(desc);
		wizard.setStartPage(desc.getIdentifier());
		
		new Thread(new Runnable() {
			public void run() {
				int ret = wizard.show();
				if(ret == Wizard.OK_OPTION) {
					DocearUserRegistrationAction.useRegisteredUser(wizard);
				}
			}
		}).start();
	}
	
	class BibtexEntryListModel extends DefaultListModel {
		
		private static final long serialVersionUID = 2610007578887026651L;
		
		public BibtexEntryListModel() {
					
		}
	
		public void fireDataChanged() {
			fireContentsChanged(this, 0, entries.size()-1);			
		}

		public BibtexEntry getEntry(int rowIndex) {
			if(rowIndex < 0 || rowIndex >= entries.size()) {
				throw new IndexOutOfBoundsException();
			}
			
			int i=0;
			for (Iterator<BibtexEntry> iterator = entries.iterator(); iterator.hasNext(); i++) {
				if(i==rowIndex) {
					return iterator.next();
				}
				iterator.next();
			}
			return null;
		}

		public boolean isCellEditable(int row, int column) {
            return false;
        }
		
		public int getSize() {
			if(entries == null) {
				return 0;
			}
			return entries.size();
		}
		
		
		
		public Object getElementAt(int index) {
			BibtexEntry entry = getEntry(index);
			if(entry == null) {
				return null;
			}
			
			StringBuffer sb = new StringBuffer();
			StringReader sr = new StringReader(Globals.prefs.get("preview0").replaceAll("__NEWLINE__", "\n"));
	        ExportFormats.entryNumber = 1; // Set entry number in case that is included in the preview layout.
			try {
				sb.append(new LayoutHelper(sr).getLayoutFromText(Globals.FORMATTER_PACKAGE).doLayout(entry,ReferencesController.getController().getJabrefWrapper().getDatabase()));
			} 
			catch (Exception e) {
			}			
			return sb.toString();
		}
	}

	public Collection<BibtexEntry> getEntries() {
		return this.entries;
	}

	public boolean hasRemoteBib() {
		return remoteBib;
	}

	public boolean wasSuccessful() {
		return !hasError;
	}
}
