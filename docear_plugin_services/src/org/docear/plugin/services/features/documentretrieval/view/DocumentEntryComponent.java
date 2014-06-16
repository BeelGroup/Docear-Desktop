package org.docear.plugin.services.features.documentretrieval.view;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import org.docear.plugin.services.features.documentretrieval.model.DocumentEntry;
import org.docear.plugin.services.features.io.DocearProxyAuthenticator;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.AsynchronousLoadListener;
import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;

import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

public class DocumentEntryComponent extends JPanel {

	private static final long serialVersionUID = 1L;
	public static final int OPEN_RECOMMENDATION = 1;
	public static final int IMPORT_RECOMMENDATION = 2;
	private Set<ActionListener> actionListeners = new HashSet<ActionListener>();
	private Color background;
	private Color selectionBackground;
	private ImageWrapperResizableIcon openIcon;
	private ImageWrapperResizableIcon dlIcon;

	public DocumentEntryComponent(final DocumentEntry recommendation) {
		setLayout(new FormLayout(new ColumnSpec[] {
				ColumnSpec.decode("default:grow"),
				ColumnSpec.decode("50px"),},
			new RowSpec[] {
				new RowSpec(RowSpec.CENTER, Sizes.bounded(Sizes.DEFAULT, Sizes.constant("20px", false), Sizes.constant("50px", false)), 0),}));
		
		final JLabel lblOpenButton = new JLabel();
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		if (recommendation.getPrefix() != null && recommendation.getPrefix().trim().length()>0) {
			sb.append("<b>");
			sb.append(recommendation.getPrefix()).append(" ");			
			sb.append("</b>");
		}		
		sb.append(recommendation.getTitle());
		sb.append("</html>");
		ImageIcon image = new ImageIcon(DocumentEntryComponent.class.getResource("/icons/document-open-remote_32x32.png"));
		openIcon = ImageWrapperResizableIcon.getIcon(image.getImage(), new Dimension(image.getIconWidth(), image.getIconHeight()));
		openIcon.addAsynchronousLoadListener(new AsynchronousLoadListener() {
			public void completed(boolean success) {
				repaint();
			}
		});
		lblOpenButton.setIcon(openIcon);
		background = lblOpenButton.getBackground();
		selectionBackground = new Color(140, 180, 240);		
		lblOpenButton.setText(sb.toString());		
		
		if (recommendation.isHighlighted()) {
			background = new Color(204, 178, 178);
			selectionBackground = new Color(220, 113, 113);
		}		
		setBackground(background);
		
//		final JTextField lblOpenButton = new JTextField(recommendation.getTitle());		
//		lblOpenButton.setBorder( null );
//		lblOpenButton.setOpaque( false );
//		lblOpenButton.setEditable( false );
		
		lblOpenButton.setToolTipText(TextUtils.getText("recommendation.preview.tooltip"));		
		lblOpenButton.setBorder(new BevelBorder(BevelBorder.RAISED, SystemColor.control, null, null, null));		
		lblOpenButton.setMinimumSize(new Dimension(200, 20));
		lblOpenButton.setPreferredSize(new Dimension(200, 50));		
		
		lblOpenButton.addMouseListener(new MouseListener() {
						
			public void mouseReleased(MouseEvent e) {}
			
			public void mousePressed(MouseEvent e) {}
			
			public void mouseExited(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
				setBackground(background);
			}
			
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				setBackground(selectionBackground);
			}
			
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					fireActionEvent(new ActionEvent(lblOpenButton, DocumentEntryComponent.OPEN_RECOMMENDATION, "OPEN_RECOMMENDATION"));
					e.consume();
				}
			}			
		});
		add(lblOpenButton, "1, 1");
		
		JLabel lblImportButton = new JLabel("");
		lblImportButton.setToolTipText(TextUtils.getText("recommendation.import.tooltip"));
		lblImportButton.setHorizontalAlignment(SwingConstants.CENTER);
		image = new ImageIcon(DocumentEntryComponent.class.getResource("/icons/document-import_32x32.png"));
		dlIcon = ImageWrapperResizableIcon.getIcon(image.getImage(), new Dimension(image.getIconWidth(), image.getIconHeight()));
		dlIcon.addAsynchronousLoadListener(new AsynchronousLoadListener() {
			public void completed(boolean success) {
				repaint();
			}
		});
		lblImportButton.setIcon(dlIcon);
		lblImportButton.setBorder(new BevelBorder(BevelBorder.RAISED, SystemColor.control, null, null, null));		
				
		lblImportButton.setMinimumSize(new Dimension(50, 20));
		lblImportButton.setPreferredSize(new Dimension(50, 50));
		MouseListener downloadMouseListener = new MouseListener() {
			
			public void mouseReleased(MouseEvent e) {}
			
			public void mousePressed(MouseEvent e) {}
			
			public void mouseExited(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());	
				setBackground(background);
			}
			
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				setBackground(selectionBackground);
			}
			
			public void mouseClicked(MouseEvent e) {
				if(e.getButton() == MouseEvent.BUTTON1) {
					fireActionEvent(new ActionEvent(recommendation, DocumentEntryComponent.IMPORT_RECOMMENDATION, "IMPORT_RECOMMENDATION"));
					e.consume();
				}
			}			
		};
		if("ftp".equals(recommendation.getLink().getProtocol().toLowerCase())) {
			if(!DocearProxyAuthenticator.useProxyServer()) {		
				lblImportButton.addMouseListener(downloadMouseListener);
			}
			else {
				lblImportButton.setEnabled(false);
			}			
		}
		else {
			lblImportButton.addMouseListener(downloadMouseListener);
		}
		add(lblImportButton, "2, 1");
	}

	public void addActionListener(ActionListener actionListener) {
		actionListeners.add(actionListener);
		
	}
	
	private void fireActionEvent(ActionEvent actionEvent) {
		for(ActionListener listener : actionListeners ) {
			listener.actionPerformed(actionEvent);
		}
		
	}
	
	public void setBounds(int x, int y, int width, int height) {
		Insets insets = getInsets(null);
		int maxHeight = (height-insets.top-insets.bottom);
		//adjust openIcon
		if(openIcon.getIconHeight() > maxHeight) {
			while(openIcon.getIconHeight() > maxHeight) {
				openIcon.setDimension(new Dimension(openIcon.getIconWidth()-8, openIcon.getIconHeight()-8));
			}
			openIcon.paintIcon(this, getGraphics(), 0, 0);
		}
		else if(openIcon.getIconHeight()+8 < maxHeight) {
			while(openIcon.getIconHeight()+8 < maxHeight) {
				openIcon.setDimension(new Dimension(openIcon.getIconWidth()+8, openIcon.getIconHeight()+8));
			}
			openIcon.paintIcon(this, getGraphics(), 0, 0);
		}
		
		//adjust dlIcon
		if(dlIcon.getIconHeight() > maxHeight) {
			while(dlIcon.getIconHeight() > (height-insets.top-insets.bottom)) {
				dlIcon.setDimension(new Dimension(dlIcon.getIconWidth()-8, dlIcon.getIconHeight()-8));
			}
			dlIcon.paintIcon(this, getGraphics(), 0, 0);
		}
		else if(dlIcon.getIconHeight()+8 < maxHeight) {
			while(dlIcon.getIconHeight()+8 < maxHeight) {
				dlIcon.setDimension(new Dimension(dlIcon.getIconWidth()+8, dlIcon.getIconHeight()+8));
			}
			dlIcon.paintIcon(this, getGraphics(), 0, 0);
		}
		
		invalidate();
		super.setBounds(x, y, width, height);
		repaint();
    }

}
