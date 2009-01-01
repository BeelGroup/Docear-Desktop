/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
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
package org.freeplane.map.attribute;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.map.attribute.mindmapnode.AttributeManagerDialog;

class ShowAttributeDialogAction extends AbstractAction {
	private AttributeManagerDialog attributeDialog = null;

	/**
	 *
	 */
	ShowAttributeDialogAction() {
		super(null, new ImageIcon(Controller.getResourceController().getResource(
		    "images/showAttributes.gif")));
		MenuBuilder.setLabelAndMnemonic(this, Controller.getText("attributes_dialog"));
	}

	public void actionPerformed(final ActionEvent e) {
		if (getAttributeDialog().isVisible() == false
		        && Controller.getController().getMapView() != null) {
			getAttributeDialog().pack();
			getAttributeDialog().show();
		}
	}

	private AttributeManagerDialog getAttributeDialog() {
		if (attributeDialog == null) {
			attributeDialog = new AttributeManagerDialog();
		}
		return attributeDialog;
	}
}
