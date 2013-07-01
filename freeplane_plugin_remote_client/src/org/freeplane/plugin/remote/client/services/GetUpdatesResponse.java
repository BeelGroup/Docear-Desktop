package org.freeplane.plugin.remote.client.services;

import java.util.List;

import org.freeplane.plugin.remote.v10.model.updates.MapUpdate;

public class GetUpdatesResponse {
	private final int currentRevision;
	private final List<MapUpdate> orderedUpdates;
	public GetUpdatesResponse(int currentRevision, List<MapUpdate> orderedUpdates) {
		super();
		this.currentRevision = currentRevision;
		this.orderedUpdates = orderedUpdates;
	}
	public int getCurrentRevision() {
		return currentRevision;
	}
	public List<MapUpdate> getOrderedUpdates() {
		return orderedUpdates;
	}
	
	
}
