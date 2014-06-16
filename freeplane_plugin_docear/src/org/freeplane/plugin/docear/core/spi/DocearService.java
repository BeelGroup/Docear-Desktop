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
	public void install(DocearServiceContext context);
	
	/**
	 * 
	 * @param context
	 */
	public void uninstall(DocearServiceContext context);

}
