package org.docear.plugin.core.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

public class OverlayLayoutManager implements LayoutManager {
	public static String WRAPPED_LAYOUT = "overlay_orig_layout|";
	public static String ALIGN_TOP = "overlay_align_top|";
	public static String ALIGN_CENTER = "overlay_align_center|";
	public static String ALIGN_BOTTOM = "overlay_align_bottom|";
	public static String FLOAT_LEFT = "overlay_float_left|";
	public static String FLOAT_RIGHT = "overlay_float_right|";
	public static String FLOAT_MIDDLE = "overlay_float_middle|";
	
	
	private final LayoutManager wrappedLayout;
	private final List<Component> overlayComponents = new ArrayList<Component>();

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public OverlayLayoutManager(LayoutManager layout) {
		wrappedLayout = layout;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	public static boolean instanceOf(LayoutManager layout) {
		if(layout instanceof OverlayLayoutManager) {
			return true;
		}
		return false;
	}
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	@Override
	public void addLayoutComponent(String name, Component comp) {
		if(name != null) {
			if(name.contains("overlay_")) {
				addLayoutComponent(name.split("[|]"), comp);
			}
			else {
				if(wrappedLayout != null) {
					wrappedLayout.addLayoutComponent(name, comp);
				}
			}
		}
	}
	
	public void addLayoutComponent(String[] positionConstraints, Component comp) {
		overlayComponents.add(comp);
	}

	@Override
	public void removeLayoutComponent(Component comp) {
		if(!overlayComponents.remove(comp)) {
			if(wrappedLayout != null) {
				wrappedLayout.removeLayoutComponent(comp);
			}
		}
	}

	@Override
	public Dimension preferredLayoutSize(Container parent) {
		if(wrappedLayout == null) {
			return parent.getPreferredSize();
		}
		return wrappedLayout.preferredLayoutSize(parent);
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		if(wrappedLayout == null) {
			return parent.getMinimumSize();
		}
		return wrappedLayout.minimumLayoutSize(parent);
	}

	@Override
	public void layoutContainer(Container parent) {
		if(wrappedLayout != null) {
			wrappedLayout.layoutContainer(parent);
			Insets insets = parent.getInsets();
			if(insets == null) {
				insets = new Insets(5, 5, 5, 5);
			}
			int tr = parent.getWidth()-parent.getInsets().right;
//			int tl = parent.getInsets().left;
//			int cr = parent.getWidth()-parent.getInsets().right;
//			int cl = parent.getInsets().left;
//			int br = parent.getWidth()-parent.getInsets().right;
//			int bl = parent.getInsets().left;
			for (Component overlayComp : overlayComponents) {
				if(overlayComp.isVisible()) {
					Dimension prefSize = overlayComp.getSize();
					tr -= prefSize.width - 5; 
					overlayComp.setBounds(tr, parent.getInsets().top+5, 100, 100);
				}
			}
		}
	}
}
