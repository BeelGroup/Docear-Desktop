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
package org.freeplane.controller.resources.ui;

import org.freeplane.core.controller.Controller;

/**
 * @author Dimitry Polivaev
 * 26.12.2008
 */
public class PropertyAdapter {
	private String description;
	private final String label;
	private final String name;

	public PropertyAdapter(final String name) {
		this(name, "OptionPanel." + name, "OptionPanel." + name + ".tooltip");
		if (Controller.getResourceController().getText(description, null) == null) {
			description = null;
		}
	}

	public PropertyAdapter(final String name, final String label, final String description) {
		super();
		assert name != null;
		this.name = name;
		this.label = label;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getLabel() {
		return label;
	}

	public String getName() {
		return name;
	}
}
