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
package org.freeplane.addins;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.freeplane.core.controller.ActionDescriptor;
import org.freeplane.core.controller.FreeplaneAction;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.core.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.core.map.MapController;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.mode.ModeController;
import org.freeplane.core.ui.SelectableAction;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.view.map.NodeView;

public abstract class PersistentNodeHook implements IExtension {
	public class HookAction extends FreeplaneAction {
		public HookAction() {
			super();
		}

		public void actionPerformed(final ActionEvent e) {
			final NodeModel[] nodes = getNodes();
			final boolean activeForSelection = isActiveForSelection();
			for (int i = 0; i < nodes.length; i++) {
				final NodeModel node = nodes[i];
				if (node.containsExtension(getExtensionClass()) != activeForSelection) {
					continue;
				}
				undoableToggleHook(node);
			}
		}
	}

	@SelectableAction(checkOnNodeChange = true)
	private class SelectableHookAction extends HookAction {
		public SelectableHookAction() {
			super();
		}

		@Override
		public void setSelected() {
			setSelected(isActiveForSelection());
		}
	}

	private final class ToggleHookActor implements IUndoableActor {
		IExtension extension;
		private final NodeModel node;

		private ToggleHookActor(final NodeModel node, final IExtension extension) {
			this.node = node;
			this.extension = extension != null ? extension : node.getExtension(getExtensionClass());
		}

		public void act() {
			if (extension != null && node.containsExtension(extension)) {
				remove(node, extension);
			}
			else {
				if (extension == null) {
					extension = createExtension(node);
				}
				add(node, extension);
			}
		}

		public String getDescription() {
			return getHookName();
		}

		public void undo() {
			act();
		}
	}

	protected class XmlReader implements IElementDOMHandler {
		public Object createElement(final Object parent, final String tag,
		                            final IXMLElement attributes) {
			if (attributes == null) {
				return null;
			}
			if (!getHookName().equals(attributes.getAttribute("NAME", null))) {
				return null;
			}
			return parent;
		}

		public void endElement(final Object parent, final String tag, final Object userObject,
		                       final IXMLElement lastBuiltElement) {
			if (getHookAnnotation().onceForMap()) {
				final IXMLElement parentNodeElement = lastBuiltElement.getParent().getParent();
				if (parentNodeElement == null || parentNodeElement.getName().equals("node")) {
					return;
				}
			}
			if (action != null) {
				action.setEnabled(true);
			}
			final NodeModel node = (NodeModel) userObject;
			add(node, createExtension(node, lastBuiltElement));
		}
	}

	protected class XmlWriter implements IExtensionElementWriter {
		public void writeContent(final ITreeWriter writer, final Object object,
		                         final IExtension extension) throws IOException {
			final XMLElement element = new XMLElement("hook");
			saveExtension(extension, element);
			writer.addElement(null, element);
		}
	}

	private final FreeplaneAction action;
	private final ModeController modeController;

	public PersistentNodeHook(final ModeController modeController) {
		super();
		this.modeController = modeController;
		final ActionDescriptor actionAnnotation = getActionAnnotation();
		if (actionAnnotation != null) {
			action = createAction();
			if (action != null) {
				registerAction(action, actionAnnotation);
			}
		}
		else {
			action = null;
		}
		final MapController mapController = modeController.getMapController();
		mapController.getReadManager().addElementHandler("hook", createXmlReader());
		mapController.getWriteManager().addExtensionElementWriter(getExtensionClass(),
		    createXmlWriter());
	}

	protected void add(final NodeModel node, final IExtension extension) {
		assert (getExtensionClass().equals(extension.getClass()));
		node.addExtension(extension);
		getModeController().getMapController().nodeChanged(node);
	}

	protected FreeplaneAction createAction() {
		final SelectableHookAction selectableAction = new SelectableHookAction();
		return selectableAction;
	}

	protected IExtension createExtension(final NodeModel node) {
		return createExtension(node, null);
	}

	protected IExtension createExtension(final NodeModel node, final IXMLElement element) {
		return this;
	}

	protected XmlReader createXmlReader() {
		return new XmlReader();
	}

	protected XmlWriter createXmlWriter() {
		return new XmlWriter();
	}

	protected ActionDescriptor getActionAnnotation() {
		final ActionDescriptor annotation = getClass().getAnnotation(ActionDescriptor.class);
		return annotation;
	}

	protected Class getExtensionClass() {
		return getClass();
	}

	private NodeHookDescriptor getHookAnnotation() {
		final NodeHookDescriptor annotation = getClass().getAnnotation(NodeHookDescriptor.class);
		return annotation;
	}

	protected String getHookName() {
		return getHookAnnotation().hookName();
	}

	protected ModeController getModeController() {
		return modeController;
	}

	protected NodeModel[] getNodes() {
		if (getHookAnnotation().onceForMap()) {
			return getRootNode();
		}
		return getSelectedNodes();
	}

	protected NodeModel[] getRootNode() {
		final NodeModel[] nodes = new NodeModel[1];
		nodes[0] = getModeController().getMapView().getModel().getRootNode();
		return nodes;
	}

	protected NodeModel[] getSelectedNodes() {
		final List<NodeView> selection = getModeController().getMapView().getSelection();
		final int size = selection.size();
		final NodeModel[] nodes = new NodeModel[size];
		final Iterator<NodeView> iterator = selection.iterator();
		int i = 0;
		while (iterator.hasNext()) {
			nodes[i++] = iterator.next().getModel();
		}
		return nodes;
	}

	protected boolean isActiveForSelection() {
		final NodeModel[] nodes = getNodes();
		for (int i = 0; i < nodes.length; i++) {
			if (nodes[i].containsExtension(getExtensionClass())) {
				return true;
			}
		}
		return false;
	}

	protected void registerAction(final FreeplaneAction action) {
		registerAction(action, action.getClass().getAnnotation(ActionDescriptor.class));
	}

	protected void registerAction(final FreeplaneAction action,
	                              final ActionDescriptor actionAnnotation) {
		modeController.addAction(actionAnnotation.name(), action);
		getModeController().getUserInputListenerFactory().getMenuBuilder().addAction(action,
		    actionAnnotation);
	}

	protected void remove(final NodeModel node, final IExtension extension) {
		node.removeExtension(extension);
		getModeController().getMapController().nodeChanged(node);
	}

	protected void saveExtension(final IExtension extension, final IXMLElement element) {
		element.setAttribute("NAME", getHookName());
	}

	public void undoableActivateHook(final NodeModel node, final IExtension extension) {
		undoableToggleHook(node, extension);
	}

	public void undoableToggleHook(final NodeModel node) {
		undoableToggleHook(node, null);
	}

	private void undoableToggleHook(final NodeModel node, final IExtension extension) {
		final IUndoableActor actor = new ToggleHookActor(node, extension);
		getModeController().execute(actor);
	}
}
