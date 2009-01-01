/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is modified by Dimitry Polivaev in 2008.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.map.cloud;

import java.awt.Color;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.map.NodeModel;

public class CloudModel implements IExtension {
	private Color color;
	private int iterativeLevel;
	private String style;
	private int width;

	public CloudModel() {
		iterativeLevel = -1;
	}

	/**
	 * calculates the cloud iterative level which is importent for the cloud
	 * size
	 */
	private void calcIterativeLevel(final NodeModel target) {
		iterativeLevel = 0;
		if (target != null) {
			for (NodeModel parentNode = target.getParentNode(); parentNode != null; parentNode = parentNode
			    .getParentNode()) {
				final CloudModel cloud = parentNode.getCloud();
				if (cloud != null) {
					iterativeLevel = cloud.getIterativeLevel(target) + 1;
					break;
				}
			}
		}
	}

	/** changes the iterative level. */
	public void changeIterativeLevel(final int deltaLevel) {
		if (iterativeLevel != -1) {
			iterativeLevel = iterativeLevel + deltaLevel;
		}
	}

	public Color getColor() {
		return color;
	}

	/** gets iterative level which is required for painting and layout. */
	public int getIterativeLevel(final NodeModel target) {
		if (iterativeLevel == -1) {
			calcIterativeLevel(target);
		}
		return iterativeLevel;
	}

	public String getStyle() {
		return style;
	}

	public int getWidth() {
		return width;
	}

	public void setColor(final Color color) {
		this.color = color;
	}

	public void setStyle(final String style) {
		this.style = style;
	}

	public void setWidth(final int width) {
		this.width = width;
	}
}
