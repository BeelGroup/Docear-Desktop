package org.docear.plugin.core.ui.dialog;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.JButton;

@Deprecated 
/***
 * @author mag
 *
 * don't use! early state
 */
public abstract class ADocearOverlayDialog extends Window {
	private static final long serialVersionUID = 1L;
	private final Window self;
	private JPanel captionArea;
	private JPanel buttonArea;

	public ADocearOverlayDialog(Frame parent, Component content) {
		super(parent);
		self = this;
		this.addWindowListener(new WindowListener() {
			public void windowOpened(WindowEvent e) {
			}
			
			public void windowIconified(WindowEvent e) {
			}
			
			public void windowDeiconified(WindowEvent e) {
			}
			
			public void windowDeactivated(WindowEvent e) {
			}
			
			public void windowClosing(WindowEvent e) {
			}
			
			public void windowClosed(WindowEvent e) {
				closeDialog();
			}
			
			public void windowActivated(WindowEvent e) {
				
			}
		});
		this.setAlwaysOnTop(true);
		this.setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
		
		this.setLayout(new BorderLayout());
		JPanel contentPain = new JPanel();
		contentPain.setBorder(new CompoundBorder(new EmptyBorder(2, 2, 2, 2), new LineBorder(new Color(0, 0, 0))));
		contentPain.setBackground(Color.WHITE);
		this.add(contentPain, BorderLayout.CENTER);
		contentPain.setLayout(new BorderLayout(0, 0));
		
		captionArea = new JPanel();
		contentPain.add(captionArea, BorderLayout.NORTH);
		
		buttonArea = new JPanel();
		contentPain.add(buttonArea, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("OK");
		buttonArea.add(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDialog();				
			}
		});
		
		JButton btnCancel = new JButton("Cancel");
		buttonArea.add(btnCancel);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeDialog();				
			}
		});
		this.setVisible(true);		
	}

	protected void closeDialog() {
		self.dispose();		
	}

}
