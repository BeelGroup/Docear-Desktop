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
package org.freeplane.map.attribute.mindmapnode;

import java.util.ListIterator;
import java.util.NoSuchElementException;

import org.freeplane.controller.filter.util.SortedComboBoxModel;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.map.attribute.Attribute;
import org.freeplane.map.attribute.AttributeController;
import org.freeplane.map.attribute.AttributeRegistry;
import org.freeplane.map.attribute.AttributeRegistryElement;
import org.freeplane.map.attribute.IAttributeController;
import org.freeplane.map.attribute.NodeAttributeTableModel;
import org.freeplane.map.attribute.view.AttributePopupMenu;
import org.freeplane.modes.mindmapmode.MModeController;

public class MAttributeController extends AttributeController implements IAttributeController {
	private class AttributeChanger implements IVisitor {
		final private Object name;
		final private Object newValue;
		final private Object oldValue;

		public AttributeChanger(final Object name, final Object oldValue, final Object newValue) {
			super();
			this.name = name;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freemind.modes.attributes.AttributeRegistry.Visitor#visit(freemind
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		public void visit(final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(name) && model.getValue(i).equals(oldValue)) {
					final int row = i;
					final String newValue = this.newValue.toString();
					final IUndoableActor actor = new SetAttributeValueActor(model, row, newValue);
					getModeController().execute(actor);
				}
			}
		}
	}

	private class AttributeRemover implements IVisitor {
		final private Object name;

		public AttributeRemover(final Object name) {
			super();
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freemind.modes.attributes.AttributeRegistry.Visitor#visit(freemind
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		public void visit(final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(name)) {
					final int row = i;
					final IUndoableActor actor = new RemoveAttributeActor(model, row);
					getModeController().execute(actor);
				}
			}
		}
	}

	private class AttributeRenamer implements IVisitor {
		final private Object newName;
		final private Object oldName;

		public AttributeRenamer(final Object oldName, final Object newName) {
			super();
			this.newName = newName;
			this.oldName = oldName;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freemind.modes.attributes.AttributeRegistry.Visitor#visit(freemind
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		public void visit(final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(oldName)) {
					final int row = i;
					final String name = newName.toString();
					final String oldName = this.oldName.toString();
					final IUndoableActor actor = new SetAttributeNameActor(model, name, oldName,
					    row);
					getModeController().execute(actor);
				}
			}
		}
	}

	private class AttributeValueRemover implements IVisitor {
		final private Object name;
		final private Object value;

		public AttributeValueRemover(final Object name, final Object value) {
			super();
			this.name = name;
			this.value = value;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freemind.modes.attributes.AttributeRegistry.Visitor#visit(freemind
		 * .modes.attributes.ConcreteAttributeTableModel)
		 */
		public void visit(final NodeAttributeTableModel model) {
			for (int i = 0; i < model.getRowCount(); i++) {
				if (model.getName(i).equals(name) && model.getValue(i).equals(value)) {
					final IUndoableActor actor = new RemoveAttributeActor(model, i);
					getModeController().execute(actor);
				}
			}
		}
	}

	private static class InsertAttributeActor implements IUndoableActor {
		private final NodeAttributeTableModel model;
		private final String name;
		private final int row;
		private final String value;

		private InsertAttributeActor(final NodeAttributeTableModel model, final int row,
		                             final String name, final String value) {
			this.row = row;
			this.name = name;
			this.model = model;
			this.value = value;
		}

		public void act() {
			final Attribute newAttribute = new Attribute(name, value);
			model.getAttributes().add(row, newAttribute);
			model.enableStateIcon();
			model.fireTableRowsInserted(row, row);
		}

		public String getDescription() {
			return "InsertAttributeActor";
		}

		public void undo() {
			model.getAttributes().remove(row);
			model.disableStateIcon();
			model.fireTableRowsDeleted(row, row);
		}
	}

	private static class Iterator {
		final private IVisitor visitor;

		Iterator(final IVisitor v) {
			visitor = v;
		}

		/**
		 */
		void iterate(final NodeModel node) {
			visitor.visit(node.getAttributes());
			final ListIterator iterator = node.getModeController().getMapController()
			    .childrenUnfolded(node);
			while (iterator.hasNext()) {
				final NodeModel child = (NodeModel) iterator.next();
				iterate(child);
			}
		}
	}

	private static interface IVisitor {
		void visit(NodeAttributeTableModel model);
	}

	private static class RegistryAttributeActor implements IUndoableActor {
		private final String name;
		private final AttributeRegistry registry;

		private RegistryAttributeActor(final String name, final AttributeRegistry registry) {
			this.name = name;
			this.registry = registry;
		}

		public void act() {
			final AttributeRegistryElement attributeRegistryElement = new AttributeRegistryElement(
			    registry, name);
			final int index = registry.getElements().add(name, attributeRegistryElement);
			registry.getTableModel().fireTableRowsInserted(index, index);
		}

		public String getDescription() {
			return "RegistryAttributeActor";
		}

		public void undo() {
			registry.unregistry(name);
		}
	}

	private static class RegistryAttributeValueActor implements IUndoableActor {
		private final AttributeRegistryElement element;
		private final String newValue;

		private RegistryAttributeValueActor(final AttributeRegistryElement element,
		                                    final String newValue) {
			this.element = element;
			this.newValue = newValue;
		}

		public void act() {
			element.addValue(newValue);
		}

		public String getDescription() {
			return "RegistryAttributeValueActor";
		}

		public void undo() {
			element.removeValue(newValue);
		}
	}

	private static class RemoveAttributeActor implements IUndoableActor {
		final private InsertAttributeActor insertActor;

		private RemoveAttributeActor(final NodeAttributeTableModel model, final int row) {
			final Attribute attribute = model.getAttribute(row);
			final String name = attribute.getName();
			final String value = attribute.getValue();
			insertActor = new InsertAttributeActor(model, row, name, value);
		}

		public void act() {
			insertActor.undo();
		}

		public String getDescription() {
			return "RemoveAttributeActor";
		}

		public void undo() {
			insertActor.act();
		}
	}

	private static class ReplaceAttributeValueActor implements IUndoableActor {
		private final String name;
		private final String newValue;
		private final String oldValue;
		private final AttributeRegistry registry;

		private ReplaceAttributeValueActor(final AttributeRegistry registry, final String name,
		                                   final String oldValue, final String newValue) {
			this.registry = registry;
			this.name = name;
			this.oldValue = oldValue;
			this.newValue = newValue;
		}

		public void act() {
			registry.getElement(name).replaceValue(oldValue, newValue);
		}

		public String getDescription() {
			return "ReplaceAttributeValueActor";
		}

		public void undo() {
			registry.getElement(name).replaceValue(newValue, oldValue);
		}
	}

	private static class SetAttributeColumnWidthActor implements IUndoableActor {
		private final int col;
		private final NodeAttributeTableModel model;
		private final int oldWidth;
		private final int width;

		private SetAttributeColumnWidthActor(final int col, final int oldWidth, final int width,
		                                     final NodeAttributeTableModel model) {
			this.col = col;
			this.oldWidth = oldWidth;
			this.width = width;
			this.model = model;
		}

		public void act() {
			model.getLayout().setColumnWidth(col, width);
		}

		public String getDescription() {
			return "SetAttributeColumnWidthActor";
		}

		public void undo() {
			model.getLayout().setColumnWidth(col, oldWidth);
		}
	}

	private static class SetAttributeNameActor implements IUndoableActor {
		private final NodeAttributeTableModel model;
		private final String name;
		private final String oldName;
		private final int row;

		private SetAttributeNameActor(final NodeAttributeTableModel model, final String name,
		                              final String oldName, final int row) {
			this.model = model;
			this.name = name;
			this.oldName = oldName;
			this.row = row;
		}

		public void act() {
			model.getAttribute(row).setName(name);
			model.fireTableCellUpdated(row, 0);
		}

		public String getDescription() {
			return "setAttributeName";
		}

		public void undo() {
			model.getAttribute(row).setName(oldName);
			model.fireTableCellUpdated(row, 0);
		}
	}

	private static class SetAttributeRestrictedActor implements IUndoableActor {
		private final int index;
		private final boolean isRestricted;
		private final AttributeRegistry registry;

		private SetAttributeRestrictedActor(final AttributeRegistry registry, final int index,
		                                    final boolean isRestricted) {
			this.registry = registry;
			this.index = index;
			this.isRestricted = isRestricted;
		}

		public void act() {
			act(isRestricted);
		}

		public void act(final boolean isRestricted) {
			if (index == AttributeRegistry.GLOBAL) {
				registry.setRestricted(isRestricted);
			}
			else {
				registry.getElement(index).setRestriction(isRestricted);
			}
		}

		public String getDescription() {
			return "SetAttributeRestrictedActor";
		}

		public void undo() {
			act(!isRestricted);
		}
	}

	private static final class SetAttributeValueActor implements IUndoableActor {
		private final NodeAttributeTableModel model;
		private final String newValue;
		private final String oldValue;
		private final int row;

		private SetAttributeValueActor(final NodeAttributeTableModel model, final int row,
		                               final String newValue) {
			this.row = row;
			oldValue = model.getAttribute(row).getValue();
			this.newValue = newValue;
			this.model = model;
		}

		public void act() {
			model.getAttribute(row).setValue(newValue);
			model.fireTableCellUpdated(row, 1);
		}

		public String getDescription() {
			return "SetAttributeValue";
		}

		public void undo() {
			model.getAttribute(row).setValue(oldValue);
			model.fireTableCellUpdated(row, 1);
		}
	}

	private static class SetAttributeVisibleActor implements IUndoableActor {
		private final AttributeRegistry attributeRegistry;
		private final int index;
		private final boolean isVisible;

		private SetAttributeVisibleActor(final AttributeRegistry attributeRegistry,
		                                 final int index, final boolean isVisible) {
			this.attributeRegistry = attributeRegistry;
			this.index = index;
			this.isVisible = isVisible;
		}

		public void act() {
			act(isVisible);
		}

		private void act(final boolean isVisible) {
			attributeRegistry.getElement(index).setVisibility(isVisible);
			attributeRegistry.fireStateChanged();
		}

		public String getDescription() {
			return "SetAttributeVisibleActor";
		}

		public void undo() {
			act(!isVisible);
		}
	}

	private static class UnregistryAttributeActor implements IUndoableActor {
		final private RegistryAttributeActor registryActor;

		private UnregistryAttributeActor(final String name, final AttributeRegistry registry) {
			registryActor = new RegistryAttributeActor(name, registry);
		}

		public void act() {
			registryActor.undo();
		}

		public String getDescription() {
			return "UnregistryAttributeActor";
		}

		public void undo() {
			registryActor.act();
		}
	}

	private static class UnregistryAttributeValueActor implements IUndoableActor {
		final private RegistryAttributeValueActor registryActor;

		private UnregistryAttributeValueActor(final AttributeRegistryElement element,
		                                      final String newValue) {
			registryActor = new RegistryAttributeValueActor(element, newValue);
		}

		public void act() {
			registryActor.undo();
		}

		public String getDescription() {
			return "UnregistryAttributeValueActor";
		}

		public void undo() {
			registryActor.act();
		}
	}

	InsertAttributeActor insertAttributeActor;
	final private MModeController modeController;

	public MAttributeController(final MModeController modeController) {
		super(modeController);
		this.modeController = modeController;
		createActions();
	}

	public int addAttribute(final NodeModel node, final Attribute pAttribute) {
		node.createAttributeTableModel();
		final NodeAttributeTableModel attributes = node.getAttributes();
		final int rowCount = attributes.getRowCount();
		performInsertRow(attributes, rowCount, pAttribute.getName(), pAttribute.getValue());
		return rowCount;
	}

	/**
	 *
	 */
	private void createActions() {
		modeController.addAction("editAttributes", new EditAttributesAction());
		modeController.addAction("assignAttributes", new AssignAttributesAction());
	}

	public int editAttribute(final NodeModel pNode, final String pName, final String pNewValue) {
		pNode.createAttributeTableModel();
		final Attribute newAttribute = new Attribute(pName, pNewValue);
		final NodeAttributeTableModel attributes = pNode.getAttributes();
		for (int i = 0; i < attributes.getRowCount(); i++) {
			if (pName.equals(attributes.getAttribute(i).getName())) {
				if (pNewValue != null) {
					setAttribute(pNode, i, newAttribute);
				}
				else {
					removeAttribute(pNode, i);
				}
				return i;
			}
		}
		if (pNewValue == null) {
			return -1;
		}
		return addAttribute(pNode, newAttribute);
	}

	private AttributeRegistry getAttributeRegistry() {
		return Controller.getController().getMap().getRegistry().getAttributes();
	}

	public AttributePopupMenu getAttributeTablePopupMenu() {
		return new AttributePopupMenu();
	}

	public void performInsertRow(final NodeAttributeTableModel model, final int row,
	                             final String name, String value) {
		final AttributeRegistry attributes = getAttributeRegistry();
		if (name.equals("")) {
			return;
		}
		try {
			final AttributeRegistryElement element = attributes.getElement(name);
			final int index = element.getValues().getIndexOf(value);
			if (index == -1) {
				if (element.isRestricted()) {
					value = element.getValues().firstElement().toString();
				}
				else {
					final IUndoableActor actor = new RegistryAttributeValueActor(element, value);
					getModeController().execute(actor);
				}
			}
		}
		catch (final NoSuchElementException ex) {
			final AttributeRegistry registry = getAttributeRegistry();
			final IUndoableActor nameActor = new RegistryAttributeActor(name, registry);
			getModeController().execute(nameActor);
			final AttributeRegistryElement element = getAttributeRegistry().getElement(name);
			final IUndoableActor valueActor = new RegistryAttributeValueActor(element, value);
			getModeController().execute(valueActor);
		}
		final String newValue = value;
		final IUndoableActor actor = new InsertAttributeActor(model, row, name, newValue);
		getModeController().execute(actor);
	}

	public void performRegistryAttribute(final String name) {
		if (name.equals("")) {
			return;
		}
		final AttributeRegistry attributeRegistry = getAttributeRegistry();
		try {
			attributeRegistry.getElement(name);
		}
		catch (final NoSuchElementException ex) {
			final IUndoableActor actor = new RegistryAttributeActor(name, attributeRegistry);
			getModeController().execute(actor);
			return;
		}
	}

	public void performRegistryAttributeValue(final String name, final String value) {
		if (name.equals("")) {
			return;
		}
		final AttributeRegistry attributeRegistry = getAttributeRegistry();
		try {
			final AttributeRegistryElement element = attributeRegistry.getElement(name);
			if (element.getValues().contains(value)) {
				return;
			}
			final IUndoableActor actor = new RegistryAttributeValueActor(element, value);
			getModeController().execute(actor);
			return;
		}
		catch (final NoSuchElementException ex) {
			final IUndoableActor nameActor = new RegistryAttributeActor(name, attributeRegistry);
			getModeController().execute(nameActor);
			final AttributeRegistryElement element = attributeRegistry.getElement(name);
			final IUndoableActor valueActor = new RegistryAttributeValueActor(element, value);
			getModeController().execute(valueActor);
		}
	}

	public void performRegistrySubtreeAttributes(final NodeModel node) {
		for (int i = 0; i < node.getAttributes().getRowCount(); i++) {
			final String name = node.getAttributes().getValueAt(i, 0).toString();
			final String value = node.getAttributes().getValueAt(i, 1).toString();
			performRegistryAttributeValue(name, value);
		}
		for (final ListIterator e = node.getModeController().getMapController().childrenUnfolded(
		    node); e.hasNext();) {
			final NodeModel child = (NodeModel) e.next();
			performRegistrySubtreeAttributes(child);
		}
	}

	public void performRemoveAttribute(final String name) {
		final IUndoableActor actor = new UnregistryAttributeActor(name, getAttributeRegistry());
		getModeController().execute(actor);
		final IVisitor remover = new AttributeRemover(name);
		final Iterator iterator = new Iterator(remover);
		final NodeModel root = modeController.getMapController().getRootNode();
		iterator.iterate(root);
	}

	public void performRemoveAttributeValue(final String name, final String value) {
		final IUndoableActor unregistryActor = new UnregistryAttributeValueActor(
		    getAttributeRegistry().getElement(name), value);
		getModeController().execute(unregistryActor);
		final IVisitor remover = new AttributeValueRemover(name, value);
		final Iterator iterator = new Iterator(remover);
		final NodeModel root = modeController.getMapController().getRootNode();
		iterator.iterate(root);
	}

	public void performRemoveRow(final NodeAttributeTableModel model, final int row) {
		final IUndoableActor actor = new RemoveAttributeActor(model, row);
		getModeController().execute(actor);
	}

	public void performReplaceAtributeName(final String oldName, final String newName) {
		if (oldName.equals("") || newName.equals("") || oldName.equals(newName)) {
			return;
		}
		final AttributeRegistry registry = getAttributeRegistry();
		final int iOld = registry.getElements().indexOf(oldName);
		final AttributeRegistryElement oldElement = registry.getElement(iOld);
		final SortedComboBoxModel values = oldElement.getValues();
		final IUndoableActor registryActor = new RegistryAttributeActor(newName, registry);
		getModeController().execute(registryActor);
		final AttributeRegistryElement newElement = registry.getElement(newName);
		for (int i = 0; i < values.getSize(); i++) {
			final IUndoableActor registryValueActor = new RegistryAttributeValueActor(newElement,
			    values.getElementAt(i).toString());
			getModeController().execute(registryValueActor);
		}
		final IVisitor replacer = new AttributeRenamer(oldName, newName);
		final Iterator iterator = new Iterator(replacer);
		final NodeModel root = modeController.getMapController().getRootNode();
		iterator.iterate(root);
		final IUndoableActor unregistryActor = new UnregistryAttributeActor(oldName, registry);
		getModeController().execute(unregistryActor);
	}

	public void performReplaceAttributeValue(final String name, final String oldValue,
	                                         final String newValue) {
		final IUndoableActor actor = new ReplaceAttributeValueActor(getAttributeRegistry(), name,
		    oldValue, newValue);
		getModeController().execute(actor);
		final IVisitor replacer = new AttributeChanger(name, oldValue, newValue);
		final Iterator iterator = new Iterator(replacer);
		final NodeModel root = modeController.getMapController().getRootNode();
		iterator.iterate(root);
	}

	public void performSetColumnWidth(final NodeAttributeTableModel model, final int col,
	                                  final int width) {
		final int oldWidth = model.getLayout().getColumnWidth(col);
		if (width == oldWidth) {
			return;
		}
		final IUndoableActor actor = new SetAttributeColumnWidthActor(col, oldWidth, width, model);
		getModeController().execute(actor);
	}

	public void performSetFontSize(final AttributeRegistry registry, final int size) {
		final int oldSize = registry.getFontSize();
		if (size == oldSize) {
			return;
		}
		final IUndoableActor actor = new IUndoableActor() {
			public void act() {
				getAttributeRegistry().setFontSize(size);
			}

			public String getDescription() {
				return "SetAttributeFontSizeActor";
			}

			public void undo() {
				getAttributeRegistry().setFontSize(oldSize);
			}
		};
		getModeController().execute(actor);
	}

	public void performSetRestriction(final int index, final boolean isRestricted) {
		boolean currentValue;
		if (index == AttributeRegistry.GLOBAL) {
			currentValue = getAttributeRegistry().isRestricted();
		}
		else {
			currentValue = getAttributeRegistry().getElement(index).isRestricted();
		}
		if (currentValue == isRestricted) {
			return;
		}
		final IUndoableActor actor = new SetAttributeRestrictedActor(getAttributeRegistry(), index,
		    isRestricted);
		getModeController().execute(actor);
	}

	public void performSetValueAt(final NodeAttributeTableModel model, final Object o,
	                              final int row, final int col) {
		final Attribute attribute = model.getAttribute(row);
		final AttributeRegistry registry = getAttributeRegistry();
		switch (col) {
			case 0: {
				final String name = o.toString().trim();
				final String oldName = attribute.getName();
				if (oldName.equals(name)) {
					return;
				}
				final IUndoableActor nameActor = new SetAttributeNameActor(model, name, oldName,
				    row);
				getModeController().execute(nameActor);
				try {
					final AttributeRegistryElement element = registry.getElement(name);
					final String value = model.getValueAt(row, 1).toString();
					final int index = element.getValues().getIndexOf(value);
					if (index == -1) {
						final IUndoableActor valueActor = new SetAttributeValueActor(model, row,
						    element.getValues().firstElement().toString());
						getModeController().execute(valueActor);
					}
				}
				catch (final NoSuchElementException ex) {
					final IUndoableActor registryActor = new RegistryAttributeActor(name, registry);
					getModeController().execute(registryActor);
				}
				break;
			}
			case 1: {
				final String value = o.toString().trim();
				if (attribute.getValue().equals(value)) {
					return;
				}
				final IUndoableActor actor = new SetAttributeValueActor(model, row, value);
				getModeController().execute(actor);
				final String name = model.getValueAt(row, 0).toString();
				final AttributeRegistryElement element = registry.getElement(name);
				final int index = element.getValues().getIndexOf(value);
				if (index == -1) {
					final IUndoableActor registryActor = new RegistryAttributeValueActor(element,
					    value);
					getModeController().execute(registryActor);
				}
				break;
			}
		}
	}

	public void performSetVisibility(final int index, final boolean isVisible) {
		final AttributeRegistry attributeRegistry = getAttributeRegistry();
		if (attributeRegistry.getElement(index).isVisible() == isVisible) {
			return;
		}
		final IUndoableActor actor = new SetAttributeVisibleActor(attributeRegistry, index,
		    isVisible);
		getModeController().execute(actor);
	}

	public void removeAttribute(final NodeModel pNode, final int pPosition) {
		pNode.createAttributeTableModel();
		performRemoveRow(pNode.getAttributes(), pPosition);
	}

	public void setAttribute(final NodeModel pNode, final int pPosition, final Attribute pAttribute) {
		pNode.createAttributeTableModel();
		pNode.getAttributes().setValueAt(pAttribute.getName(), pPosition, 0);
		pNode.getAttributes().setValueAt(pAttribute.getValue(), pPosition, 1);
	}
}
