package org.docear.plugin.bibtex.actions;

import java.awt.Color;
import java.awt.Dimension;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import net.sf.jabref.BibtexEntry;

import org.docear.plugin.bibtex.actions.MetaDataAction.MetaDataActionObject;
import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.Wizard;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.ui.wizard.WizardPageDescriptor;
import org.docear.plugin.core.util.CoreUtils;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.TextUtils;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class MetaDataDuplicatePage extends AWizardPage {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private URI pdfFile;
	private Object pdfFileName;
	private MultiLineActionLabel labelMessage;

	public MetaDataDuplicatePage() {
		setSize(new Dimension(400, 200));
		setMaximumSize(new Dimension(400, 200));
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("50dlu"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("max(50dlu;min):grow"),
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		JLabel labelIcon = new JLabel();
		labelIcon.setVerticalAlignment(SwingConstants.TOP);
		labelIcon.setHorizontalAlignment(SwingConstants.CENTER);
		labelIcon.setIcon(UIManager.getIcon("OptionPane.warningIcon"));
		add(labelIcon, "2, 2");
		
		labelMessage = new MultiLineActionLabel();
		labelMessage.setBackground(Color.WHITE);
		add(labelMessage, "4, 2, fill, fill");
	}

	@Override
	public String getTitle() {		
		return TextUtils.getText("docear.metadata.extraction.dublicate.title");
	}

	@Override
	public void preparePage(WizardSession session) {
		session.getBackButton().setVisible(false);
		session.getNextButton().setText(TextUtils.getText("ok"));
		
		MetaDataActionObject data = session.get(MetaDataActionObject.class);
		this.pdfFile = data.getCurrentPDF();
		this.pdfFileName = CoreUtils.resolveURI(pdfFile).getName();
		BibtexEntry duplicateEntry = data.getResult().get(data.getCurrentPDF()).getEntryToUpdate();
		session.setWizardTitle(getTitle());
		labelMessage.setText(TextUtils.getText("docear.metadata.extraction.dublicate.message.1")+ this.pdfFileName +TextUtils.getText("docear.metadata.extraction.dublicate.message.2")+ duplicateEntry.getCiteKey() +TextUtils.getText("docear.metadata.extraction.dublicate.message.3"));
	}
	
	public static void showDuplicateMessage(WizardSession context){
		MetaDataActionObject data = context.get(MetaDataActionObject.class);
		final Wizard wiz = new Wizard(UITools.getFrame());
		wiz.setResizable(false);
		wiz.getSession().set(data.getClass(), data);
		
		WizardPageDescriptor duplicatePdfDescriptor = new WizardPageDescriptor("duplicate", new MetaDataDuplicatePage()) {
			
			@Override
			public WizardPageDescriptor getNextPageDescriptor(WizardSession context) {
				return Wizard.FINISH_PAGE;
			}

			@Override
			public WizardPageDescriptor getBackPageDescriptor(WizardSession context) {			
				wiz.cancel();
				return Wizard.FINISH_PAGE;
			}
		};	
		
		wiz.registerWizardPanel(duplicatePdfDescriptor);
		wiz.setStartPage(duplicatePdfDescriptor.getIdentifier());
		wiz.show();
	}
	
	
	

}
