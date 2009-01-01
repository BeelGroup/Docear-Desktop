/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.map.link.filemode;

import org.freeplane.core.map.NodeModel;
import org.freeplane.core.mode.ModeController;
import org.freeplane.map.link.LinkController;
import org.freeplane.modes.filemode.FNodeModel;

/**
 * @author Dimitry Polivaev
 */
public class FLinkController extends LinkController {
	/**
	 * @param modeController
	 */
	public FLinkController(final ModeController modeController) {
		super(modeController);
	}

	@Override
	public String getLink(final NodeModel node) {
		return ((FNodeModel) node).getFile().toString();
	}
}
