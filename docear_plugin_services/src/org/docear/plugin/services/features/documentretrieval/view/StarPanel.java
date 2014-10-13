package org.docear.plugin.services.features.documentretrieval.view;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.lang.ref.SoftReference;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.docear.plugin.services.features.io.DocearServiceResponse.Status;

import com.sun.jersey.core.util.MultivaluedMapImpl;

public class StarPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private final int setId;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	public StarPanel(String label, int id) {
		setId = id;
		setLayout(new CenterLayout(CenterLayout.CENTER_HORIZONTAL));
		
		Component starOverlay = new StarBar();
		starOverlay.setPreferredSize(new Dimension(150, 30));
		setBackground(Color.white);
		JLabel lblQuestion = new JLabel(label);
		lblQuestion.setFont(lblQuestion.getFont().deriveFont(Font.BOLD, 20));
		lblQuestion.setAlignmentY(CENTER_ALIGNMENT);
		lblQuestion.setForeground(Color.RED);
		add(lblQuestion);
		add(starOverlay);
		
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public class StarBar extends Component {
		private static final long serialVersionUID = 1L;
		SoftReference<Image> cache;
		private boolean isValueSet = false;
		public StarBar() {
			MouseAdapter adapter = new MouseAdapter() {
				
				public void mouseMoved(MouseEvent e) {
					updateStars(getMark(e.getPoint().x, (e.getComponent().getSize().width/5)), true);
					repaint();
				}

				public void mouseDragged(MouseEvent e) {
					updateStars(getMark(e.getPoint().x, (e.getComponent().getSize().width/5)), true);
					repaint();
				}
				
				public void mouseReleased(MouseEvent e) {
					if(e.getComponent().equals(StarBar.this)) {
						final int mark = getMark(e.getPoint().x, (e.getComponent().getSize().width/5));
						updateStars(mark, true);
						repaint();
						if(!isValueSet) {
							isValueSet = true;
							DocearController.getController().getEventQueue().invoke(new Runnable() {
								public void run() {
									MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
									formParams.add("rating", String.valueOf(mark));
									//send rating for recommendations
									DocearServiceResponse resp = ServiceController.getConnectionController().put("user/"+ServiceController.getCurrentUser().getName()+"/recommendations/"+ String.valueOf(setId)+"/", formParams);
									if(resp.getStatus() != Status.OK) {
										DocearLogger.info(resp.getContentAsString());
									}
								}
							});
						}
					}
					
				}
				
				public void mouseExited(MouseEvent e) {
					updateStars(0, true);
					repaint();
				}
			};
			this.addMouseMotionListener(adapter);
			this.addMouseListener(adapter);
			cache = new SoftReference<Image>(null);
		}
		
		private int getMark(int x, int framesize) {
			int mark = x%framesize > 0 ? 1 : 0;
			return x/framesize + mark;
		}
		
		private Image updateStars(int mark, boolean force) {
			Image img = cache.get();
			if(!isValueSet && (force || img == null)) {
				img = new BufferedImage(150, 30, BufferedImage.TYPE_4BYTE_ABGR);
				paintStars(img.getGraphics(), mark);
				if(cache != null) {
					cache.clear();
				}
				cache = new SoftReference<Image>(img);
			}
			return img;
		}

		private void paintStars(Graphics graphics, int mark) {
			Polygon p = new Polygon();
			//						1
			//
			//		9			10		2			3
			//
			//				8				4
			//						6
			//			7						5
			
			
			p.addPoint(15, 3); 	/*1*/
			p.addPoint(19, 10); /*2*/
			p.addPoint(26, 10); /*3*/
			p.addPoint(21, 16); /*4*/
			p.addPoint(22, 24); /*5*/
			p.addPoint(15, 19); /*6*/
			p.addPoint(7, 24); /*7*/
			p.addPoint(9, 16); 	/*8*/
			p.addPoint(4, 10); /*9*/
			p.addPoint(11, 10); /*10*/
			Graphics2D g2 = (Graphics2D) graphics;
			g2.setStroke(new BasicStroke(2));
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			for(int i = 0; i < 5; i++) {
				g2.setColor(Color.ORANGE);
				g2.setPaintMode();
				if(mark > i) {
					g2.fillPolygon(p);
				}
//				g2.setColor(Color.BLACK);
//				g2.setPaintMode();
				g2.drawPolygon(p);
				
				p.translate(30, 0);
			}
		}

		public void paint(Graphics g) {	
			Image img = updateStars(0, false);
			g.drawImage(img, 0, 0, getWidth(), getHeight(), 0, 0, img.getWidth(null), img.getHeight(null), null);
		}
	}

}
