package org.freeplane.plugin.docear.core.spi;

import org.freeplane.plugin.docear.core.DocearServiceContext;

public interface DocearService {
	
	public void start(DocearServiceContext context);
	
	public void stop(DocearServiceContext context);

}
