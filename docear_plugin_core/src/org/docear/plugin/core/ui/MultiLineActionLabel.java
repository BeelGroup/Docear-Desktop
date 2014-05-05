package org.docear.plugin.core.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicHTML;
import javax.swing.text.View;

import sun.swing.SwingUtilities2;

public class MultiLineActionLabel extends JPanel implements SwingConstants, Accessible {

	private static final long serialVersionUID = 1L;

	private String text = "";

	private int verticalAlignment = CENTER;
	private int horizontalAlignment = LEADING;
	private int verticalTextPosition = CENTER;
	private int horizontalTextPosition = TRAILING;
	
	private Rectangle paintTextR = new Rectangle();
	private Rectangle paintIconR = new Rectangle();
	
	private List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	private List<ActionLabelItem> actionItems = new ArrayList<ActionLabelItem>();
	private List<TextToken> textTokens = new ArrayList<TextToken>();
	
	private ActionLabelItem mouseHotspot = null;
	
	private final MouseAdapter mouseAdapter = new MouseAdapter();

	private Color actionColor = Color.blue.darker().darker();

	private boolean underline = true;
	
	public MultiLineActionLabel() {
		this(null);
	}
	
	public MultiLineActionLabel(String text) {
		setText(text);
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}
	public Insets getInsets() {
		return getInsets(null);
	}
	
	public Insets getInsets(Insets insets) {
		Insets ins = super.getInsets(insets);
		Insets margin = UIManager.getInsets("EditorPane.margin");
		if(margin == null) {
			margin = UIManager.getInsets("EditorPane.contentMargins");
		}
		if(margin == null) {
			margin = new Insets(3, 3, 3, 3);
		}
		//ins.top += margin.top;
		//ins.bottom += margin.bottom;
		ins.left += margin.left;
		ins.right += margin.right;
		return ins;
	}
	
	@Override
	public void setEnabled(boolean enabled) {
		boolean oldEnabled = isEnabled();
		super.setEnabled(enabled);
		if (enabled != oldEnabled) {
			if(enabled) {
				setForeground(UIManager.getColor("Label.foreground"));
			}
			else {
				Color col = UIManager.getColor("Label.disabledForeground");
				if(col == null) {
					col = UIManager.getColor("Label.disabledText");
				}
				if(col == null) {
					col = Color.BLACK;
				}
				this.setForeground(col);
			}
			
			BasicHTML.updateRenderer(this, getText());
			
			repaint();
		}
		
	}

	public void setText(String text) {
		if(text == null) {
			text = "";
		}
		String oldAccessibleName = null;
		if (accessibleContext != null) {
			oldAccessibleName = accessibleContext.getAccessibleName();
		}

		String oldValue = this.text;
		
		this.text = text;
		
		parsedString(text);
		
		BasicHTML.updateRenderer(this, getText());

		firePropertyChange("text", oldValue, getText());

		if ((accessibleContext != null) && (accessibleContext.getAccessibleName() != oldAccessibleName)) {
			accessibleContext.firePropertyChange(AccessibleContext.ACCESSIBLE_VISIBLE_DATA_PROPERTY, oldAccessibleName, accessibleContext.getAccessibleName());
		}
		if (getText() == null || oldValue == null || !getText().equals(oldValue)) {
			revalidate();
			repaint();
		}
	}
	
	public void setActionColor(Color col) {
		if(col == null) {
			actionColor = Color.blue;
		}
		this.actionColor = col;
	}
	
	public Color getActionColor() {
		return this.actionColor;
	}
	
	public String getActionColorHex() {
		String r,g,b;
		if(isEnabled()) {
			r = Integer.toHexString(actionColor.getRed());
			g = Integer.toHexString(actionColor.getGreen());
			b = Integer.toHexString(actionColor.getBlue());
		}
		else {
			Color disabledColor = getForeground();
			r = Integer.toHexString(disabledColor.getRed());
			g = Integer.toHexString(disabledColor.getGreen());
			b = Integer.toHexString(disabledColor.getBlue());
		}
		
		StringBuffer buffer = new StringBuffer();
		if(r.length() == 1) {
			buffer.append("0");
		}
		buffer.append(r);
		if(g.length() == 1) {
			buffer.append("0");
		}
		buffer.append(g);
		if(b.length() == 1) {
			buffer.append("0");
		}
		buffer.append(b);
		return buffer.toString();
	}
	
	public void setActionUnderline(boolean underline) {
		this.underline = underline;
	}
	
	public boolean isActionUnderline() {
		return this.underline;
	}

	private void parsedString(final String str) {
		int currentPos = -1;
		int lastPos = 0;
		String text = str.replaceAll("[\n]", "<br/>");
		actionItems.clear();
		textTokens.clear();
		while ((currentPos = text.indexOf("<action cmd=\"", (currentPos + 1))) > -1) {
			textTokens.add(new TextToken(text.substring(lastPos, currentPos)));
			
			lastPos = currentPos+"<action cmd=\"".length();
			String actionCommand = text.substring(lastPos, text.indexOf("\"", lastPos));			
			lastPos = text.indexOf(">", lastPos)+1;
			currentPos = text.indexOf("</action>", (lastPos + 1));			
			ActionLabelItem item = new ActionLabelItem(text.substring(lastPos, currentPos));
			item.setActionCommand(actionCommand);
			textTokens.add(item);
			
			lastPos = currentPos+"</action>".length();
			actionItems.add(item);
		}
		if(lastPos < text.length()) {
			textTokens.add(new TextToken(text.substring(lastPos)));
		}
	}

	public String getText() {
		StringBuilder builder = new StringBuilder();
		if (!BasicHTML.isHTMLString(text)) {
			builder.append("<html><body>");
		}
		for (TextToken token : textTokens) {
			builder.append(token.toString());
		}
		if (!BasicHTML.isHTMLString(text)) {
			builder.append("</body></html>");
		}
		return builder.toString();
	}
	
	public int getVerticalAlignment() {
		return verticalAlignment;
	}

	public void setVerticalAlignment(int verticalAlignment) {
		this.verticalAlignment = verticalAlignment;
	}

	public int getHorizontalAlignment() {
		return horizontalAlignment;
	}

	public void setHorizontalAlignment(int horizontalAlignment) {
		this.horizontalAlignment = horizontalAlignment;
	}

	public int getVerticalTextPosition() {
		return verticalTextPosition;
	}

	public void setVerticalTextPosition(int verticalTextPosition) {
		this.verticalTextPosition = verticalTextPosition;
	}

	public int getHorizontalTextPosition() {
		return horizontalTextPosition;
	}

	public void setHorizontalTextPosition(int horizontalTextPosition) {
		this.horizontalTextPosition = horizontalTextPosition;
	}

	public void paint(Graphics g) {
		super.paint(g);
		String text = getText();

		if (text == null) {
			return;
		}
		
		FontMetrics fm = SwingUtilities2.getFontMetrics(this, g);
		layout(fm, getWidth(), getHeight());
		FontMetrics am = SwingUtilities2.getFontMetrics(this, g, fm.getFont().deriveFont(Font.BOLD));
		
		View view = (View) getClientProperty(BasicHTML.propertyKey);
		if (view != null) {
			try {
				String clippedText = view.getDocument().getText(1, view.getDocument().getLength());
				identifyActionAreas(clippedText, fm, am);
				if(isEnabled()) {
					g.setColor(getForeground().brighter().brighter().brighter().brighter());
				}
				view.paint(g, paintTextR);
			} catch (Exception e) {
				e.printStackTrace();
			}				
		} 

		if(isActionUnderline() && mouseHotspot != null) {
			g.setColor(getActionColor());
			mouseHotspot.drawUnderline(g);
		}
	}

	private void identifyActionAreas(final String text, FontMetrics fmDefault, FontMetrics fmAction) {
		int lastPos = 0;
		for(ActionLabelItem item : actionItems) {
			item.clearBounds();
			Rectangle rect = item.getNewBox();
			int textPos = text.indexOf(item.getText(), lastPos);
			if(textPos > -1) {
				// text before action label
				String sub = text.substring(0, textPos);
				// starting pos for action label
				rect.x = paintTextR.x + fmDefault.stringWidth(sub);
				rect.y = paintTextR.y;
				// max available width 
				int maxWidth = getWidth()-getInsets().left-getInsets().right;
				// add new line as long as the label does not fit into the available width
				while(rect.x > maxWidth) {
					sub = findLastSpaceBeforeRightEnd(sub, fmDefault);
					rect.x = paintTextR.x + fmDefault.stringWidth(sub);
					rect.y += fmDefault.getHeight();
				}				
				rect.width = fmDefault.stringWidth(item.getText());
				rect.height = fmDefault.getHeight();
				
				// check whether the action label fits into one line
				sub = item.getText();
				String lastSub;
				while((rect.x+rect.width) > maxWidth) {
					lastSub = sub;
					sub = fitIntoWidth(sub, rect, fmDefault, maxWidth);					
					if(sub != null) {
						int lastY = rect.y;
						rect = item.getNewBox();
						rect.x = paintTextR.x;
						rect.y = lastY + fmDefault.getHeight();
						rect.width = fmDefault.stringWidth(sub);
						rect.height = fmDefault.getHeight();
					}
					if (lastSub != null && lastSub.equals(sub)) {
						break;
					}
				}
				
				lastPos = textPos+item.getText().length();
			}
		}
	}
	

	private String fitIntoWidth(String sub, Rectangle rect, FontMetrics fmDefault, int maxWidth) {
		String partSub = sub.substring(0);
		int index = partSub.lastIndexOf(" ");		
		while((rect.x + fmDefault.stringWidth(partSub)) > maxWidth) {
			index = partSub.lastIndexOf(" ");
			if(index == -1) {
				break;
			}
			
			partSub = sub.substring(0, index);
		}
		rect.width = fmDefault.stringWidth(partSub);
		partSub = sub.substring(index+1);
		if(partSub != null && partSub.trim().length()>0) {
			return partSub;
		}
		
		return null;
	}

	private String findLastSpaceBeforeRightEnd(final String sub, FontMetrics fmDefault) {
		String partSub = sub.substring(0);
		int index = partSub.lastIndexOf(" ");
		int width = getWidth()-getInsets().left-getInsets().right;
		while(fmDefault.stringWidth(partSub) > width) {
			index = partSub.lastIndexOf(" ");
			partSub = sub.substring(0, index);			
		}
		return sub.substring(index+1);
	}

	private String layout(FontMetrics fm, int width, int height) {
		Insets insets = getInsets(null);
		String text = getText();
		Rectangle paintViewR = new Rectangle();
		paintViewR.x = insets.left;
		paintViewR.y = insets.top;
		paintViewR.width = width - (insets.left + insets.right);
		paintViewR.height = height - (insets.top + insets.bottom);
		paintIconR.x = paintIconR.y = paintIconR.width = paintIconR.height = 0;
		paintTextR.x = paintTextR.y = paintTextR.width = paintTextR.height = 0;
		return layoutCL(fm, text, null, paintViewR, paintIconR, paintTextR);
	}

	protected String layoutCL(FontMetrics fontMetrics, String text, Icon icon, Rectangle viewR, Rectangle iconR, Rectangle textR) {
		return SwingUtilities.layoutCompoundLabel(this, fontMetrics, text, icon, verticalAlignment, horizontalAlignment, verticalTextPosition,
				horizontalTextPosition, viewR, iconR, textR, 4);
	}

	public Dimension getPreferredSize() {
		String text = getText();

		Insets insets = getInsets(null);
		Font font = getFont();

		int dx = insets.left + insets.right;
		int dy = insets.top + insets.bottom;

		if (((text == null) || ((text != null) && (font == null)))) {
			return new Dimension(dx, dy);
		} else {
			FontMetrics fm = getFontMetrics(font);

			Rectangle iconR = new Rectangle();
			Rectangle textR = new Rectangle();
			Rectangle viewR = new Rectangle();
			iconR.x = iconR.y = iconR.width = iconR.height = 0;
			textR.x = textR.y = textR.width = textR.height = 0;
			viewR.x = dx;
			viewR.y = dy;
			viewR.width = viewR.height = Short.MAX_VALUE;

			layoutCL(fm, text, null, viewR, iconR, textR);
			int x1 = Math.min(iconR.x, textR.x);
			int x2 = Math.max(iconR.x + iconR.width, textR.x + textR.width);
			int y1 = Math.min(iconR.y, textR.y);
			int y2 = Math.max(iconR.y + iconR.height, textR.y + textR.height);
			Dimension rv = new Dimension(x2 - x1, y2 - y1);

			rv.width += dx;
			rv.height += dy;
			return rv;
		}
	}

	/**
	 * @return getPreferredSize()
	 */
	public Dimension getMinimumSize() {
		Dimension d = getPreferredSize();
		View view = (View) getClientProperty(BasicHTML.propertyKey);
		if (view != null) {
			d.width -= view.getPreferredSpan(View.X_AXIS) - view.getMinimumSpan(View.X_AXIS);
		}
		return d;
	}

	/**
	 * @return getPreferredSize()
	 */
	public Dimension getMaximumSize() {
		Dimension d = getPreferredSize();
		View view = (View) getClientProperty(BasicHTML.propertyKey);
		if (view != null) {
			d.width += view.getMaximumSpan(View.X_AXIS) - view.getPreferredSpan(View.X_AXIS);
		}
		return d;
	}

	public int getBaseline(int width, int height) {
		super.getBaseline(width, height);
		String text = getText();
		if (text == null || "".equals(text) || getFont() == null) {
			return -1;
		}
		FontMetrics fm = getFontMetrics(getFont());
		layout(fm, width, height);
		return getHTMLBaseline(paintTextR.y, fm.getAscent(), paintTextR.width, paintTextR.height);
	}
	
	public void addActionListener(ActionListener listener) {
		if(!actionListeners.contains(listener)) {
			this.actionListeners.add(listener);
		}
	}
	
	public void removeActionListener(ActionListener listener) {
		this.actionListeners.remove(listener);
	}

	private int getHTMLBaseline(int y, int ascent, int width, int height) {
		View view = (View) getClientProperty(BasicHTML.propertyKey);
		if (view != null) {
			int baseline = BasicHTML.getHTMLBaseline(view, width, height);
			if (baseline < 0) {
				return baseline;
			}
			return y + baseline;
		}
		return y + ascent;

	}
	
	protected void fireActionEvent(String actionCommand) {
		ActionEvent event = new ActionEvent(this, 0, actionCommand);
		for(ActionListener listener : actionListeners) {
			listener.actionPerformed(event);
		}
		
	}
	
	protected ActionLabelItem getIntersectingItem(Point point) {
		for(ActionLabelItem item : actionItems) {
			if(item.contains(point)) {
				return item;
			}
		}
		return null;
	}
	
	protected ActionLabelItem getActionItem(Rectangle rect) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ActionLabelItem getActionByLocation(Point point) {
		for (ActionLabelItem item : actionItems) {
			if(item.contains(point)) {
				return item;
			}
		}
		return null;
	}
	
	class MouseAdapter implements MouseListener, MouseMotionListener {
	
		public void mouseMoved(MouseEvent e) {
			if(isEnabled()) {
				ActionLabelItem item = getIntersectingItem(e.getPoint());
				if(item != null) {
					MultiLineActionLabel.this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
					mouseHotspot = item;
				} else {
					MultiLineActionLabel.this.setCursor(Cursor.getDefaultCursor());
					mouseHotspot = null;
				}
				repaint();
			}
		}

		public void mouseClicked(MouseEvent e) {
			ActionLabelItem item = getIntersectingItem(e.getPoint());
			if(item != null) {
				fireActionEvent(item.getActionCommand());
			}
		}

		public void mouseDragged(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
		public void mouseEntered(MouseEvent e) {
			mouseHotspot = null;
			repaint();
		}
		public void mouseExited(MouseEvent e) {
			mouseHotspot = null;
			repaint();
		}
	}
	
	class TextToken {
		private final String text;
		public TextToken(String text) {
			this.text = text;
		}
		
		public String getText() {
			return text;
		}
		
		public String toString() {
			return getText();
		}

	}

	class ActionLabelItem extends TextToken {
		private List<Rectangle> boxes = new ArrayList<Rectangle>();
		private String actionCommand;
		
		
		public ActionLabelItem(String text) {
			super(text);
		}

		public Rectangle getNewBox() {
			Rectangle rect = new Rectangle();
			boxes.add(rect);
			return rect;
		}

		public void clearBounds() {
			boxes.clear();
		}

		public void setActionCommand(String actionCommand) {
			this.actionCommand = actionCommand;			
		}

		public String getActionCommand() {
			return actionCommand;
		}

		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("<span style=\"color: #"+getActionColorHex()+";\">");
			builder.append(super.getText());
			builder.append("</span>");
			return builder.toString();
		}
		
		public boolean contains(Point point) {
			for(Rectangle box : boxes) {
				if(box.contains(point)) {
					return true;
				}
			}
			return false;
		}
		
		public void drawUnderline(Graphics g) {
			for(Rectangle box : boxes) {
				int y = box.y+box.height - (int)(g.getFontMetrics().getDescent()*0.75);
				g.drawLine(box.x, y, box.x+box.width, y);
			}
		}
		
	}
}
