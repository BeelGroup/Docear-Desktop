package org.freeplane.plugin.docear.core.extension;

import org.freeplane.plugin.docear.core.msg.DocearMessage;

public interface DocearExtension {

	public DocearExtensionIdentifier getID();
	
	public void processMessage(DocearMessage message);
	
}
