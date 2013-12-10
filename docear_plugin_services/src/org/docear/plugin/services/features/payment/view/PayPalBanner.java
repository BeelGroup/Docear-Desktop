package org.docear.plugin.services.features.payment.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.TreeView;
import org.freeplane.plugin.workspace.features.AWorkspaceModeExtension;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
import org.pushingpixels.flamingo.api.common.icon.ResizableIcon;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class PayPalBanner extends JPanel {
	public static final String BANNER_PROPERTY_NAME = "docear.banner.paypal.show";

	private static final long serialVersionUID = 1L;

	public PayPalBanner() {
		setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 0, (Color) new Color(160, 160, 160)), new CompoundBorder(new EmptyBorder(0, 2, 2, 2), new LineBorder(new Color(160, 160, 160)))));
		JPanel banner = new JPanel();
		banner.setBackground(Color.white);
		banner.setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("4dlu:grow"),
				ColumnSpec.decode("right:default"),
				ColumnSpec.decode("left:default"),
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				RowSpec.decode("22px"),}));
		
		JLabel lblText = new JLabel(TextUtils.getText("docear.banner.supportus") + " ");
		Font font = lblText.getFont().deriveFont(Font.BOLD);
		lblText.setFont(font);
		banner.add(lblText, "2, 1");
		
		JLabel lblIcon = new JLabel("");
		ResizableIcon icon = ImageWrapperResizableIcon.getIcon(PayPalBanner.class.getResource("/icons/paypal.png"), new Dimension(151, 46));
		icon.setDimension(new Dimension(48, 16));
		lblIcon.setIcon(icon);
		banner.add(lblIcon, "3, 1");
		
		setMaximumSize(new Dimension(99999, 28));
		banner.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		banner.addMouseListener(new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {
				if(!e.isConsumed() && e.isPopupTrigger()) {
					e.consume();
					showContextMenu(e);
				}
			}
			
			public void mousePressed(MouseEvent e) {
				if(!e.isConsumed() && e.isPopupTrigger()) {
					e.consume();
					showContextMenu(e);
				}
			}
			
			public void mouseExited(MouseEvent e) {}
			
			public void mouseEntered(MouseEvent e) {}
			
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					try {
						Controller.getCurrentController().getViewController().openDocument(URI.create("https://www.docear.org/give-back/donate/"));
					} catch (IOException ex) {
						LogUtils.warn("org.docear.plugin.services.features.payment.view.PayPalBanner.PayPalBanner(): "+ ex.getMessage());
					}
				}
			}
		});
		setLayout(new BorderLayout(0, 0));
		add(banner);
	}
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	private void showContextMenu(MouseEvent e) {
		JPopupMenu context = new JPopupMenu();
		JMenuItem item = new JMenuItem();
		item.setAction(new AbstractAction() {
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				ResourceController.getResourceController().setProperty(BANNER_PROPERTY_NAME, "false");
			}
		});
		item.setText(TextUtils.getText("menu.context.banner.paypal.disable"));
		context.add(item);
		context.show(e.getComponent(), e.getX(), e.getY());
	}
	
	public void init(final ModeController modeController) {
		ResourceController.getResourceController().addPropertyChangeListener(new IFreeplanePropertyListener() {
			public void propertyChanged(String propertyName, String newValue, String oldValue) {
				if(BANNER_PROPERTY_NAME.equals(propertyName)) {
					showBanner(modeController, Boolean.parseBoolean(newValue));
				}
			}
		});
		showBanner(modeController, Boolean.parseBoolean(ResourceController.getResourceController().getProperty(BANNER_PROPERTY_NAME, "true")));
	}
	
	public void showBanner(ModeController modeController, boolean enabled) {
		AWorkspaceModeExtension modeExt = WorkspaceController.getModeExtension(modeController);
		if(modeExt != null) {
			if(modeExt.getView() instanceof TreeView) {
				TreeView view = (TreeView) modeExt.getView();
				
				if(enabled) {
					view.addBottomBanner(this);
				}
				else {
					view.removeBottomBanner(this);
				}
			}
		}
	}
	

}
