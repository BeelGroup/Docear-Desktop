package org.docear.plugin.core.ui.components;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicTextFieldUI;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.text.Caret;
import javax.swing.text.JTextComponent;

class LabeledTextFieldUI extends BasicTextFieldUI {
	/**
	 * 
	 */
	public LabeledTextFieldUI() {
		super();
	}

	@Override
	protected void installDefaults() {
		super.installDefaults();
		updateStyle(getComponent(), getPropertyPrefix());
	}

	protected void paintBackground(Graphics g) {
		SynthContext context = getContext();
		if(context != null) {
			SynthStyle style = context.getStyle();
			SynthPainter painter = style.getPainter(context);
			if (painter != null) {
				painter.paintTextFieldBackground(context, g, -1, -1, getComponent().getWidth()+2, getComponent().getHeight()+2);
			}
		}
		else {
			super.paintBackground(g);
		}
		
		paintLabel(g);
	}

	protected void paintLabel(Graphics g) {
		if (isLabelVisible()) {
			Graphics2D g2 = (Graphics2D) g;
			g2.setColor(new Color(0x50FFFFFF & getComponent().getForeground().getRGB(), true));

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			Font oldFont = g.getFont();
			Font font = oldFont.deriveFont(Font.BOLD);
			g2.setFont(font);

			int x = 10;
			int y = getBaseline(getComponent(), getComponent().getWidth(), getComponent().getHeight());

			g2.drawString(((ILabeledComponent)getComponent()).getLabelText(), x, y);
			g.setFont(oldFont);
		}
	}

	protected boolean isLabelVisible() {
		return !getComponent().isFocusOwner() && "".equals(getComponent().getText().trim());
	}
	
	SynthContext getContext() {
		return this.getContext(getComponentState(getComponent()));
	}
	
	private SynthContext getContext(int state) {
		try {
			return new SynthContext(getComponent(), getComponentRegion(getComponent()), getComponentStyle(getComponent()), state);
		}
		catch (Exception e) {
			return null;
		}
	}

	private int getComponentState(JTextComponent comp) {
		if (comp.isEnabled()) {
			if (comp.isFocusOwner()) {
				return SynthConstants.ENABLED | SynthConstants.FOCUSED;
			}
			return SynthConstants.ENABLED;
		}
		return SynthConstants.DISABLED;
	}

	private Region getComponentRegion(JTextComponent comp) {
		return SynthLookAndFeel.getRegion(comp);
	}

	private SynthStyle getComponentStyle(JTextComponent comp) {
		return SynthLookAndFeel.getStyle(comp, getComponentRegion(comp));
	}

	void updateStyle(JTextComponent comp, String prefix) {
		updateStyle(comp, getContext(), prefix);
	}

	private void updateStyle(JTextComponent comp, SynthContext context, String prefix) {
		if(context == null) {
			return;
		}
		SynthStyle style = context.getStyle();

		Color color = comp.getCaretColor();
		if (color == null || color instanceof UIResource) {
			comp.setCaretColor((Color) style.get(context, prefix + ".caretForeground"));
		}

		Color fg = comp.getForeground();
		if (fg == null || fg instanceof UIResource) {
			fg = style.getColor(context, ColorType.TEXT_FOREGROUND);
			if (fg != null) {
				comp.setForeground(fg);
			}
		}

		Object ar = style.get(context, prefix + ".caretAspectRatio");
		if (ar instanceof Number) {
			comp.putClientProperty("caretAspectRatio", ar);
		}

		//context.setComponentState(SynthConstants.SELECTED | SynthConstants.FOCUSED);
		SynthContext ctx = getContext(SynthConstants.SELECTED | SynthConstants.FOCUSED); 
		Color s = comp.getSelectionColor();
		if (s == null || s instanceof UIResource) {
			comp.setSelectionColor(style.getColor(ctx, ColorType.TEXT_BACKGROUND));
		}

		Color sfg = comp.getSelectedTextColor();
		if (sfg == null || sfg instanceof UIResource) {
			comp.setSelectedTextColor(style.getColor(ctx, ColorType.TEXT_FOREGROUND));
		}

		ctx = getContext(SynthConstants.DISABLED);

		Color dfg = comp.getDisabledTextColor();
		if (dfg == null || dfg instanceof UIResource) {
			comp.setDisabledTextColor(style.getColor(ctx, ColorType.TEXT_FOREGROUND));
		}

		Insets margin = comp.getMargin();
		if (margin == null || margin instanceof UIResource) {
			margin = (Insets) style.get(ctx, prefix + ".margin");

			if (margin == null) {
				// Some places assume margins are non-null.
				margin = new InsetsUIResource(0, 0, 0, 0);//SynthLookAndFeel.EMPTY_UIRESOURCE_INSETS;
			}
			comp.setMargin(margin);
		}

		Caret caret = comp.getCaret();
		if (caret instanceof UIResource) {
			Object o = style.get(ctx, prefix + ".caretBlinkRate");
			if (o != null && o instanceof Integer) {
				Integer rate = (Integer) o;
				caret.setBlinkRate(rate.intValue());
			}
		}
	}
}