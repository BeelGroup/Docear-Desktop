package org.freeplane.plugin.docear.core.spi;

import org.freeplane.plugin.docear.core.DocearServiceContext;

/**
 * @author genzmehr@docear.org
 *
 */
public interface DocearService {
	
	/**
	 * 
	 * @param context
	 */
	public void start(DocearServiceContext context);
	
	/**
	 * 
	 * @param context
	 */
	public void stop(DocearServiceContext context);

}
