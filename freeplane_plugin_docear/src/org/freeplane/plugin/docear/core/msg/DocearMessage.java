package org.freeplane.plugin.docear.core.msg;

import org.freeplane.plugin.docear.core.extension.DocearExtensionIdentifier;

/**
 * @author genzmehr@docear.org
 *
 */
public abstract class DocearMessage {
	private volatile int flags = 0;
	
	public static final int FLAG_CONSUMED = 0x1;
	public static final int FLAG_RUN_ASYNC = 0x2;
	
	private final DocearExtensionIdentifier source;
	
	public DocearMessage(DocearExtensionIdentifier src) {
		this.source = src;
	}
	
	public DocearExtensionIdentifier getSenderID() {
		return this.source;
	}

	public boolean isConsumed() {
		return (flags & FLAG_CONSUMED) > 0;
	}
	
	public void consume() {
		flags |= FLAG_CONSUMED;
	}
	
	public void setFlags(int additionalFlags) {
		flags |= (additionalFlags & 0xFE);
	}
	
	public int getFlags() {
		return flags & 0xFF;
	}
	
	public boolean flagsSet(int flag) {
		return (flags & flag & 0xFF) > 0;
	}
}
