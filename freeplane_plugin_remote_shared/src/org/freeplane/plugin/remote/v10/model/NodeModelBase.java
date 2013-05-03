package org.freeplane.plugin.remote.v10.model;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.link.NodeLinks;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.note.mindmapmode.MNoteController;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@JsonInclude(Include.NON_NULL)
abstract public class NodeModelBase implements Serializable {
	private static final long serialVersionUID = 1L;

	public String id;
	public String nodeText;
	public Boolean isHtml;
	public Boolean folded;
	public String[] icons;
	public ImageModel image;
	public String link;
	public String locked;
	public String note;
	
	public List<String> childrenIds;
	
	@JsonIgnore
	private final NodeModel freeplaneNode;

	/**
	 * necessary for JAX-B
	 */
	protected NodeModelBase() {
		this.freeplaneNode = null;
//		id = null;
//		nodeText = null;
//		isHtml = false;
//		folded = false;
//		icons = null;
//		image = null;
//		link = null;
//		locked = null;
		//freeplaneNode = null;
	}

//	public NodeModelBase(NodeModel freeplaneNode) {
//		this(freeplaneNode, null, false);
//	}
	
	public NodeModelBase(NodeModel freeplaneNode, boolean autoloadChildren) {

		this.freeplaneNode = freeplaneNode;
		this.id = freeplaneNode.getID();
		this.nodeText = freeplaneNode.getText();
		this.isHtml = freeplaneNode.getXmlText() != null;
		this.folded = freeplaneNode.isFolded();
		this.icons = getIconArray(freeplaneNode);
		this.image = getImage(freeplaneNode);
		this.note = getNote(freeplaneNode);

		URI uri = NodeLinks.getValidLink(freeplaneNode);
		this.link = uri != null ? uri.toString() : null;

		LockModel lm = freeplaneNode.getExtension(LockModel.class);
		this.locked = lm != null ? lm.getUsername() : null;

		saveChildrenIds(freeplaneNode);

		if(autoloadChildren) { //load children models
			loadChildren(true);
		}
	}
	
	private String getNote(org.freeplane.features.map.NodeModel freeplaneNode) {
		NoteController noteController = MNoteController.getController();
		final String noteText = noteController.getNoteText(freeplaneNode);
		//if no note, 'noteText' is null
		return noteText;
	}

	private String[] getIconArray(org.freeplane.features.map.NodeModel freeplaneNode) {
		String[] iconNames = new String[freeplaneNode.getIcons().size()];
		int count = 0;
		for(MindIcon mi : freeplaneNode.getIcons()) {
			iconNames[count++] = mi.getName();
		}
		return iconNames;
	}

	private ImageModel getImage(org.freeplane.features.map.NodeModel freeplaneNode) {
		// TODO: implement; Where is the Image hidden? (JS) 
		return null;
	}

	/**
	 * stores nodeIds
	 * @param freeplaneNode
	 */
	protected void saveChildrenIds(NodeModel freeplaneNode) {
		childrenIds = new ArrayList<String>();

		for(NodeModel node : freeplaneNode.getChildren()) {
			childrenIds.add(node.getID());
		}
	}

	/**
	 * loads the children into the model
	 * @return number of children that have been added
	 */
	public abstract int loadChildren(boolean autoloadChildren);

	@JsonIgnore
	public abstract List<NodeModelDefault> getAllChildren();

	
	public String toJsonString() {
		try {
		final ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this);
		} catch (Exception e) {
			return "";
		}
	}
	
	protected NodeModel getFreeplaneNode() {
		return freeplaneNode;
	}
	
//	@JsonIgnore
//	protected String getJsonStringParts() {
//		String childrenList = "";
//		if(childrenIds != null) {
//			for(String cId : childrenIds) {
//				childrenList += ",\""+cId+"\"";
//			}
//			childrenList = childrenList.substring(1);
//		}
//
//		return  "\"id\":\""+id+"\"," +
//		"\"nodeText\":\""+new String(JsonStringEncoder.getInstance().quoteAsString(nodeText))+"\"," +
//		"\"isHtml\":"+isHtml.toString()+"," +
//		"\"link\":\""+(link != null ? new String(JsonStringEncoder.getInstance().quoteAsString(link)) : "")+"\"," +
//		"\"folded\":"+folded+"," +
//		"\"locked\":\""+(locked != null ? new String(JsonStringEncoder.getInstance().quoteAsString(locked)) : "")+"\"," +
//		(childrenIds != null && childrenIds.size() > 0 ? "\"childrenIds\":["+childrenList+"]," : "") +
//		"\"image\":\""+"NOT IMPLEMENTED"+"\"," +
//		"\"icons\":\""+"NOT IMPLEMENTED"+"\"";
//	}
}
