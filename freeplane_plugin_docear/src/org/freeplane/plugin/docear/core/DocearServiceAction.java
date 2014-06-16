package org.freeplane.plugin.docear.core;

import org.freeplane.plugin.docear.core.spi.DocearService;

/**
 * @author genzmehr@docear.org
 *
 */
public abstract class DocearServiceAction {

	public boolean isTarget(DocearService ds) {
		return true;
	}

	abstract public void execute(DocearService ds);

}