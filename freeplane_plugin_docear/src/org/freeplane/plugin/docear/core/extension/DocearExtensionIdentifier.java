package org.freeplane.plugin.docear.core.extension;

import org.freeplane.plugin.docear.util.Version;

/**
 * @author genzmehr@docear.org
 *
 */
public final class DocearExtensionIdentifier {
	
	private final String name;
	private final Version version;

	public DocearExtensionIdentifier(String name, Version version) {
		if(name == null || version == null || name.trim().length() <= 0) {
			throw new IllegalArgumentException("empty or null argument passed to DocearExtensionIdentifier()");
		}
		this.name = name;
		this.version = version;
	}
	
	public String getName() {
		return name;
	}

	public Version getVersion() {
		return version;
	}
		
	public boolean equals(Object o) {
		if(o instanceof DocearExtensionIdentifier) {
			DocearExtensionIdentifier other = (DocearExtensionIdentifier) o;
			return version.equals(other.getVersion()) && name.equals(other.getName());
		}
		return false;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getName());
		sb.append(":");
		sb.append(getVersion());
		return sb.toString();
	}

	
}
