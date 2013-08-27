package org.docear.plugin.pdfutilities.features;

import java.io.IOException;
import java.util.Map.Entry;

import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.IAttributeHandler;
import org.freeplane.core.io.IElementDOMHandler;
import org.freeplane.core.io.IExtensionAttributeWriter;
import org.freeplane.core.io.IExtensionElementWriter;
import org.freeplane.core.io.ITreeWriter;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.WriteManager;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public class DocearNodeMonitoringExtensionXmlBuilder implements IElementDOMHandler, IExtensionElementWriter, IExtensionAttributeWriter {	
	
	public static final String INCOMING_ATTRIBUTE_NAME = "INCOMING";
	private static final String DOCEAR_NODE_EXTENSION_KEY_XML_TAG = "key";
	private static final String DOCEAR_NODE_EXTENSION_VALUE_XML_TAG = "value";
	private static final String DOCEAR_NODE_EXTENSION_OBJECT_XML_TAG = "object";
	private static final String DOCEAR_NODE_EXTENSION_XML_TAG = "docear_node_extension";
	private static final String DOCEAR_NODE_EXTENSIONS_XML_TAG = "docear_node_extensions";
	
	public void registerBy(final ReadManager reader, final WriteManager writer) {
		reader.addElementHandler(DOCEAR_NODE_EXTENSION_XML_TAG, this);
		reader.addElementHandler(DOCEAR_NODE_EXTENSIONS_XML_TAG, this);
		reader.addAttributeHandler("node", INCOMING_ATTRIBUTE_NAME, new IAttributeHandler() {
			public void setAttribute(Object node, String value) {
				if(node instanceof NodeModel) {
					MonitoringUtils.markAsIncomingNode((NodeModel)node, false);
				}
			}
		});
		writer.addExtensionElementWriter(DocearNodeMonitoringExtension.class, this);
		writer.addExtensionAttributeWriter(DocearNodeMonitoringExtension.class, this);
		writer.addExtensionAttributeWriter(IcomingNodeExtension.class, new IExtensionAttributeWriter() {
			
			public void writeAttributes(ITreeWriter writer, Object userObject, IExtension extension) {
				if(userObject instanceof NodeModel && MonitoringUtils.isIncomingNode((NodeModel)userObject)) {
					writer.addAttribute(INCOMING_ATTRIBUTE_NAME, "true");
				}
			}
		});
		registerAttributeHandlers(reader);
	}
	
	private void registerAttributeHandlers(ReadManager reader) {
		reader.addAttributeHandler(DOCEAR_NODE_EXTENSION_XML_TAG, DOCEAR_NODE_EXTENSION_KEY_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {				
				final DocearNodeMonitoringExtension extension = (DocearNodeMonitoringExtension) node;
				extension.setXmlBuilderKey(value);
				extension.putEntry(value, null);			
			}
			
		});
		
		reader.addAttributeHandler(DOCEAR_NODE_EXTENSION_XML_TAG, DOCEAR_NODE_EXTENSION_VALUE_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {				
				final DocearNodeMonitoringExtension extension = (DocearNodeMonitoringExtension) node;
				if(extension.getXmlBuilderKey() != null && extension.getXmlBuilderKey().length() > 0){
					extension.putEntry(extension.getXmlBuilderKey(), value);
				}
				extension.setXmlBuilderKey(null);
			}
			
		});	
		
		reader.addAttributeHandler(DOCEAR_NODE_EXTENSION_XML_TAG, DOCEAR_NODE_EXTENSION_OBJECT_XML_TAG, new IAttributeHandler() {
			
			public void setAttribute(Object node, String value) {
				final DocearNodeMonitoringExtension extension = (DocearNodeMonitoringExtension) node;
				if(extension.getXmlBuilderKey() != null && extension.getXmlBuilderKey().length() > 0){
					extension.putEntry(extension.getXmlBuilderKey(), value);
				}
				extension.setXmlBuilderKey(null);
			}
			
		});	
	}

	public Object createElement(Object parent, String tag, XMLElement attributes) {
		if (tag.equals(DOCEAR_NODE_EXTENSIONS_XML_TAG)) {
			final DocearNodeMonitoringExtension oldDocearNodeModel = DocearNodeMonitoringExtensionController.getModel((NodeModel) parent);
			if(oldDocearNodeModel != null){
				return oldDocearNodeModel;
			}
			else{				
				return new DocearNodeMonitoringExtension();				
			}
		}
		if (tag.equals(DOCEAR_NODE_EXTENSION_XML_TAG)) {
			return parent;			
		}
		return null;
	}
	
	public void endElement(final Object parent, final String tag, final Object userObject, final XMLElement dom) {
		if (parent instanceof NodeModel) {
			final NodeModel node = (NodeModel) parent;
			if (userObject instanceof DocearNodeMonitoringExtension) {
				final DocearNodeMonitoringExtension docearNodeModel = (DocearNodeMonitoringExtension) userObject;
				DocearNodeMonitoringExtensionController.setModel(node, docearNodeModel);
			}
		}
		if (parent instanceof DocearNodeMonitoringExtension) {
			final DocearNodeMonitoringExtension docearNodeModel = (DocearNodeMonitoringExtension) parent;
			if (userObject instanceof Entry<?, ?>) {
				@SuppressWarnings("unchecked")
				final Entry<String, Object> entry = (Entry<String, Object>) userObject;
				docearNodeModel.putEntry(entry);				
			}
		}
	}
	
	public void writeContent(ITreeWriter writer, Object element, IExtension extension) throws IOException {
		writeContentImpl(writer, null, extension);
	}

	public void writeContentImpl(final ITreeWriter writer, final NodeModel node, final IExtension extension) throws IOException {
		
		final DocearNodeMonitoringExtension docearNodeModel = extension != null ? (DocearNodeMonitoringExtension) extension : DocearNodeMonitoringExtensionController.getModel(node);
		if (docearNodeModel == null || docearNodeModel.getMap().size() < 1) {
			return;
		}
		XMLElement docearNodeModelXmlElement = new XMLElement();
		docearNodeModelXmlElement.setName(DOCEAR_NODE_EXTENSIONS_XML_TAG);
		for(Entry<String, Object> entry : docearNodeModel.getAllEntries()){
			XMLElement entryXmlElement = new XMLElement();
			entryXmlElement.setName(DOCEAR_NODE_EXTENSION_XML_TAG);
			if(entry.getKey() != null && entry.getKey().length() > 0){
				entryXmlElement.setAttribute(DOCEAR_NODE_EXTENSION_KEY_XML_TAG, entry.getKey());
				if(entry.getValue() != null){
					if(entry.getValue() instanceof String){
						entryXmlElement.setAttribute(DOCEAR_NODE_EXTENSION_VALUE_XML_TAG, entry.getValue().toString());
					}
					else{
						entryXmlElement.setAttribute(DOCEAR_NODE_EXTENSION_OBJECT_XML_TAG, entry.getValue().toString());
					}
				}
			}
			docearNodeModelXmlElement.addChild(entryXmlElement);
		}			
		writer.addElement(docearNodeModel, docearNodeModelXmlElement);
		
	}

	public void writeAttributes(ITreeWriter writer, Object userObject, IExtension extension) {
		
	}
}
