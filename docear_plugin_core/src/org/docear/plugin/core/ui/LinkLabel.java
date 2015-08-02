package org.docear.plugin.core.ui;

import java.awt.Color;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.swing.JEditorPane;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

public class LinkLabel extends JEditorPane {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public LinkLabel(String html){
		super();
		this.setContentType("text/html");
		this.setText(html);
		this.setEditable(false);
		this.setOpaque(true);
		this.setBackgroundColor();	
		this.getInsets().set(0, 0, 0, 0);
		this.addHyperlinkListener(new HyperlinkListener() {
			
			@Override
			public void hyperlinkUpdate(HyperlinkEvent evt) {
				if(HyperlinkEvent.EventType.ACTIVATED.equals(evt.getEventType())){
					try {
						Controller.getCurrentController().getViewController().openDocument(evt.getURL().toURI());
					} catch (IOException e) {
						LogUtils.warn(e);
					} catch (URISyntaxException e) {
						LogUtils.warn(e);
					}
				}				
			}
		});
	}
	
	private void setBackgroundColor(){
		final Color col = UIManager.getColor("Label.background");
		if(col != null){
			if(UIManager.getLookAndFeel().getName().contentEquals("Nimbus")){
				UIDefaults defaults = new UIDefaults();
				defaults.put("EditorPane[Enabled].backgroundPainter", col);
				this.putClientProperty("Nimbus.Overrides", defaults);
				this.putClientProperty("Nimbus.Overrides.InheritDefaults", true);
				this.setBackground(col);			
			}
			else{
				this.setBackground(col);
			}
		}
	}	

	@Override
	public void setEnabled(boolean enabled) {
		if(enabled){
			Color col = UIManager.getColor("Label.foreground");	
			if(col == null) {
				col = Color.BLACK;
			}
			this.setForeground(col);
		}
		else{
			Color col = UIManager.getColor("Label.disabledForeground");
			if(col == null) {
				col = UIManager.getColor("Label.disabledText");
			}
			if(col == null) {
				col = Color.GRAY;
			}
			this.setForeground(col);
		}		
	}
	
	


}
