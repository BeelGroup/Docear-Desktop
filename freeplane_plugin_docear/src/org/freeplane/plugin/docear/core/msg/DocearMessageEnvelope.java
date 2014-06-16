package org.freeplane.plugin.docear.core.msg;

/**
 * @author genzmehr@docear.org
 *
 */
final class DocearMessageEnvelope {
	private final long queueStamp;
	private long lastCycle;
	private final DocearMessage message;
	private boolean delivered = false;
	
	public DocearMessageEnvelope(DocearMessage message) {
		this.queueStamp = System.nanoTime();
		this.message = message;
	}
	
	public long getTimestamp() {
		return queueStamp;
	}
	
	public DocearMessage getMessage() {
		return message;
	}
	
	public void delivered() {
		this.delivered  = true;
	}
	
	public boolean isDelivered() {
		return this.delivered;
	}
	
	public void setLastCycle(long cycle) {
		lastCycle = cycle;		
	}
	
	public long getLastCycle() {
		return lastCycle;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DocearMessageEnvelope[");
		sb.append(getTimestamp());
		sb.append(";");
		sb.append(getLastCycle());
		sb.append(";");
		sb.append(isDelivered());
		sb.append(";");
		sb.append(getMessage());
		sb.append("]");
		return sb.toString();
	}	
}
