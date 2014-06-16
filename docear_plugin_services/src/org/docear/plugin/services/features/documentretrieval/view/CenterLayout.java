package org.docear.plugin.services.features.documentretrieval.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.Rectangle;

public class CenterLayout implements LayoutManager {
	
	public static final int CENTER_HORIZONTAL = 0x01;
	public static final int CENTER_VERTICAL = 0x02;
	
	private int layoutOption;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public CenterLayout(int center) {
		layoutOption = center;
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void addLayoutComponent(String name, Component comp) {
		// TODO Auto-generated method stub

	}

	public void removeLayoutComponent(Component comp) {
		// TODO Auto-generated method stub

	}

	public Dimension preferredLayoutSize(Container parent) {
		int w = 0;
		for(Component comp : parent.getComponents()) {
			w += comp.getPreferredSize().width;
			w += 5; //5pix gap
		}
		return new Dimension(w, 30);
	}

	public Dimension minimumLayoutSize(Container parent) {
		int w = 0;
		for(Component comp : parent.getComponents()) {
			w += comp.getMinimumSize().width;
			w += 5; //5pix gap
		}
		return new Dimension(w, 30);
	}

	public void layoutContainer(Container parent) {
		Dimension rootDim = parent.getSize();
		int midX = rootDim.width / 2;
		int midY = rootDim.height / 2;
		Rectangle bounds;
		int sumWidth = 0;
		int sumHeight = 0;
		for(Component comp : parent.getComponents()) {
			Dimension dim = comp.getPreferredSize();
			if((layoutOption & CENTER_HORIZONTAL) > 0) {
				sumWidth += dim.width + 5;
			}
			if((layoutOption & CENTER_VERTICAL) > 0) {
				sumHeight += dim.height + 3;
			}
		}
		int startX = midX - (sumWidth/2);
		int startY = midY - (sumHeight/2);
		for(Component comp : parent.getComponents()) {
			Dimension dim = comp.getPreferredSize();
			bounds = new Rectangle(dim);
			int x = 0;
			int y = 0;
			if((layoutOption & CENTER_HORIZONTAL) > 0) {
				x = startX;
				startX += dim.width + 5;
				
				//x = midX - (dim.width / 2);
			}
			if((layoutOption & CENTER_VERTICAL) > 0) {
				y = startY;
				startY += dim.height + 3;
				//y = midY - (dim.height / 2);
			}
			bounds.setLocation(x, y);
			comp.setBounds(bounds);
		}

	}
}
