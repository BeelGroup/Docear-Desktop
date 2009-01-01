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
package org.freeplane.map.edge.view;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.CubicCurve2D;

import org.freeplane.view.map.VerticalRootNodeViewLayout;

/**
 * This class represents a single Edge of a MindMap.
 */
public class BezierEdgeView extends EdgeView {
	private static final int CHILD_XCTRL = 20;
	private static final int XCTRL = 12;
	CubicCurve2D.Float graph = new CubicCurve2D.Float();

	public BezierEdgeView() {
		super();
	}

	@Override
	protected void paint(final Graphics2D g) {
		update();
		final Color color = getColor();
		g.setColor(color);
		final Stroke stroke = getStroke();
		g.setStroke(stroke);
		g.draw(graph);
		if (isTargetEclipsed()) {
			g.setColor(g.getBackground());
			g.setStroke(EdgeView.getEclipsedStroke());
			g.draw(graph);
			g.setStroke(stroke);
			g.setColor(color);
		}
	}

	private void update() {
		final int sign = (getTarget().isLeft()) ? -1 : 1;
		int sourceSign = 1;
		if (getSource().isRoot() && !VerticalRootNodeViewLayout.USE_COMMON_OUT_POINT_FOR_ROOT_NODE) {
			sourceSign = 0;
		}
		final int xctrl = getMap().getZoomed(sourceSign * sign * BezierEdgeView.XCTRL);
		final int childXctrl = getMap().getZoomed(-1 * sign * BezierEdgeView.CHILD_XCTRL);
		graph.setCurve(start.x, start.y, start.x + xctrl, start.y, end.x + childXctrl, end.y,
		    end.x, end.y);
	}
}
