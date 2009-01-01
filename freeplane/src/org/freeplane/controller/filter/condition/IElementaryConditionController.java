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
package org.freeplane.controller.filter.condition;

import javax.swing.ComboBoxEditor;
import javax.swing.ComboBoxModel;

import org.freeplane.controller.filter.util.IListModel;
import org.freeplane.controller.resources.NamedObject;
import org.freeplane.core.io.xml.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 * 21.12.2008
 */
public interface IElementaryConditionController {
	boolean canEditValues(final Object property, final NamedObject simpleCond);

	boolean canHandle(final Object selectedItem);

	boolean canSelectValues(final Object property, final NamedObject simpleCond);

	public ICondition createCondition(final Object selectedItem, final NamedObject simpleCond,
	                                  final Object value, final boolean ignoreCase);

	ComboBoxModel getConditionsForProperty(final Object property);

	IListModel getFilteredProperties();

	ComboBoxEditor getValueEditor();

	ComboBoxModel getValuesForProperty(final Object property);

	boolean isCaseDependent(final Object property, final NamedObject simpleCond);

	ICondition loadCondition(final XMLElement element);
}
