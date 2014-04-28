package org.docear.plugin.services.features.setup.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.MultiLineActionLabel;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.services.DocearServiceException;
import org.docear.plugin.services.features.setup.DocearServiceTestTask;
import org.docear.plugin.services.features.user.DocearUser;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;


public class VerifyServicePagePanel extends AWizardPage {
	private static final long serialVersionUID = 1L;
	private MultiLineActionLabel lblMessage;
	private final DocearServiceTestTask test;
	private final String title;
	private boolean skipOnSuccess;
	private boolean showSocialLinks = true;
	private JPanel socialPanel;

	/***********************************************************************************
	 * CONSTRUCTORS
	 * @param settings 
	 * @wbp.parser.constructor
	 **********************************************************************************/
	public VerifyServicePagePanel(String title, DocearServiceTestTask task) {
		this(title, task, false);
	}
	
	public VerifyServicePagePanel(String title, DocearServiceTestTask task, boolean skipOnSuccess) {
		this.title = title;
		this.test = task;
		this.setSkipOnSuccess(skipOnSuccess);
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				RowSpec.decode("default:grow"),}));
		
		lblMessage = new MultiLineActionLabel();
		lblMessage.setBackground(Color.WHITE);
		lblMessage.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if("contact".equals(e.getActionCommand())) {
					try {
						Controller.getCurrentController().getViewController().openDocument(URI.create("http://www.docear.org/docear/contact/"));
					} catch (IOException e1) {
						LogUtils.warn("Exception in org.docear.plugin.services.features.setup.view.VerifyServicePagePanel.VerifyServicePagePanel(...).new ActionListener() {...}.actionPerformed(e): "+e1.getMessage());
					}
				}
			}
		});
		add(lblMessage, "2, 2, fill, fill");
		
		socialPanel = new JPanel();
		socialPanel.setBackground(Color.WHITE);
		add(socialPanel, "2, 4, center, fill");
		socialPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
		JLabel lblTwitter = new JLabel(TextUtils.getText("docear.setup.wizard.follow.twitter"));
		lblTwitter.setFont(adjustFont(lblTwitter.getFont()));
		lblTwitter.setIcon(new ImageIcon(DocearController.class.getResource("/images/twitter.png")));
		lblTwitter.setCursor(cursor);
		lblTwitter.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Controller.getCurrentController().getViewController().openDocument(URI.create("https://twitter.com/Docear_org"));
				} catch (IOException e1) {
					LogUtils.warn("Exception in org.docear.plugin.services.features.setup.view.VerifyServicePagePanel.VerifyServicePagePanel(title, task, skipOnSuccess)$lblTwitter.mouseClicked(MouseEvent): "+e1.getMessage());
				}
			}
		});
		socialPanel.add(lblTwitter, "2, 2");
		
		JLabel lblFacebook = new JLabel(TextUtils.getText("docear.setup.wizard.follow.facebook"));
		lblFacebook.setFont(adjustFont(lblFacebook.getFont()));
		lblFacebook.setIcon(new ImageIcon(DocearController.class.getResource("/images/facebook.png")));
		lblFacebook.setCursor(cursor);
		lblFacebook.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Controller.getCurrentController().getViewController().openDocument(URI.create("https://www.facebook.com/pages/Docear/137985949605902"));
				} catch (IOException e1) {
					LogUtils.warn("Exception in org.docear.plugin.services.features.setup.view.VerifyServicePagePanel.VerifyServicePagePanel(title, task, skipOnSuccess)$lblFacebook.mouseClicked(MouseEvent): "+e1.getMessage());
				}
			}
		});
		socialPanel.add(lblFacebook, "2, 4");
		
		JLabel lblGoogle = new JLabel(TextUtils.getText("docear.setup.wizard.follow.google"));
		lblGoogle.setFont(adjustFont(lblGoogle.getFont()));
		lblGoogle.setIcon(new ImageIcon(DocearController.class.getResource("/images/google+.png")));
		lblGoogle.setCursor(cursor);
		lblGoogle.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					Controller.getCurrentController().getViewController().openDocument(URI.create("https://plus.google.com/109967308877260625203/posts"));
				} catch (IOException e1) {
					LogUtils.warn("Exception in org.docear.plugin.services.features.setup.view.VerifyServicePagePanel.VerifyServicePagePanel(title, task, skipOnSuccess)$lblGoogle.mouseClicked(MouseEvent): "+e1.getMessage());
				}
			}
		});
		socialPanel.add(lblGoogle, "2, 6");
	}	
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	private Font adjustFont(Font font) {
		Font f = font.deriveFont(Font.BOLD, 16);
		@SuppressWarnings("rawtypes")
		Map attributes = f.getAttributes();
		attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
		return f.deriveFont(attributes);
	}
	
	@Override
	public String getTitle() {
		return TextUtils.format(("docear.setup.wizard.verification."+title).toLowerCase(Locale.ENGLISH), TextUtils.getText(("docear.setup.wizard.verification."+title+".result."+test.isSuccessful()).toLowerCase(Locale.ENGLISH)));
	}

	@Override
	public void preparePage(WizardSession context) {
		this.setPageDisplayable(true);
		DocearUser settings = context.get(DocearUser.class);
		socialPanel.setVisible(showSocialLinks);
		try {
			test.run(settings);
			context.getNextButton().setEnabled(true);
			getRootPane().setDefaultButton((JButton) context.getNextButton());
			lblMessage.setText(TextUtils.getText(("docear.setup.wizard.verification."+title+".msg").toLowerCase(Locale.ENGLISH)));
			if(skipOnSuccess) {
				this.setPageDisplayable(false);
			}
		} catch (DocearServiceException e) {
			LogUtils.warn("Exception in org.docear.plugin.services.features.setup.view.VerifyServicePagePanel.preparePage(context): "+e.getMessage());
			context.getNextButton().setEnabled(false);
			getRootPane().setDefaultButton((JButton) context.getBackButton());
			lblMessage.setText(e.getMessage());
			socialPanel.setVisible(false);
		}
		context.setWizardTitle(getTitle());
	}

	public boolean isSkipOnSuccess() {
		return skipOnSuccess;
	}

	public void setSkipOnSuccess(boolean skipOnSuccess) {
		this.skipOnSuccess = skipOnSuccess;
	}
	
	public void setSocialLinksVisible(boolean b) {
		this.showSocialLinks = b;
	}
}
