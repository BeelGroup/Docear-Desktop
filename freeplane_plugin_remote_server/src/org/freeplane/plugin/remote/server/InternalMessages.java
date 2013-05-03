package org.freeplane.plugin.remote.server;

import java.io.Serializable;

public class InternalMessages {

	@SuppressWarnings("serial")
	public static class ReleaseTimedOutLocks implements Serializable {
		private final Long millisecondsSinceRequest;

		public ReleaseTimedOutLocks(Long millisecondsSinceRequest) {
			super();
			this.millisecondsSinceRequest = millisecondsSinceRequest;
		}

		public Long getMillisecondsSinceRequest() {
			return millisecondsSinceRequest;
		}

	}
}
