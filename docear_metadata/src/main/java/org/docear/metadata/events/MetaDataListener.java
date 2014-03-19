package org.docear.metadata.events;


public interface MetaDataListener {
	
	public void onFinishedRequest(MetaDataEvent event);	
	
	public void onCaptchaRequested(MetaDataEvent event);

}
