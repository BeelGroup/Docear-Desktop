package org.freeplane.plugin.remote.client.listeners;

public interface ProjectsListener {
	
	//files
	void conflictedFile(String projectId, String fileName);
	
	//quota
	void quotaLimitAlmostReached(String projectId, double percentLeft);
	void quotaLimitReached(String projectId);
	
	void unknownError(Throwable t);
}
