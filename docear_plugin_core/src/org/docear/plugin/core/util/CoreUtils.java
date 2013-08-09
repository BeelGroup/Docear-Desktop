package org.docear.plugin.core.util;

import java.awt.Component;
import java.awt.Container;
import java.io.File;
import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;

import org.freeplane.core.ui.components.OneTouchCollapseResizer;
import org.freeplane.plugin.workspace.URIUtils;

public class CoreUtils {
	public static File resolveURI(final URI uri) {
		return URIUtils.getAbsoluteFile(uri);
	}
	
	public static String createRandomString(int length) {
		SecureRandom random = new SecureRandom();
		String s = new BigInteger(length*8, random).toString(Character.MAX_RADIX);
		return s;
	}

	public static boolean isEmpty(Object o) {
		return o==null || o.toString().length()==0;		
	}
	
	
	public static OneTouchCollapseResizer findResizerFor(Component component) {
		if(component != null) {
			Component parent = component.getParent();
			if(parent != null) {
				if(parent instanceof Container) {
					Component[] children = ((Container) parent).getComponents();
					for (Component child : children) {
						if(child instanceof OneTouchCollapseResizer) {
							return (OneTouchCollapseResizer) child;
						}
					}
				}
				return findResizerFor(parent);
			}
		}
		return null;
	}
}
