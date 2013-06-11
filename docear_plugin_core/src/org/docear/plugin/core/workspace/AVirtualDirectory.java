package org.docear.plugin.core.workspace;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;

public abstract class AVirtualDirectory extends File {

	private static final long serialVersionUID = 1L;
	
	public AVirtualDirectory(String pathname) {
		super(pathname);
	}

	public boolean isDirectory() {
		return true;
	}
	
	public boolean isFile() {
		return false;
	}
	
	public boolean exists() {
		return true;
	}
	
	public boolean canExecute() {
		return false;
	}
	
	public boolean canRead() {
		return true;
	}
	
	public boolean canWrite() {
		return false;
	}
	
	public boolean delete() {
		return true;
	}
	
	public void deleteOnExit() {
	}
	
	public int compareTo(File pathname) {
		if(pathname instanceof AVirtualDirectory) {
			super.compareTo(pathname);
		}
		return 1;
	}
	
	public boolean equals(Object obj) {
		if(obj instanceof AVirtualDirectory) {
			return super.equals(obj);
		}
		return false;
	}
	
	public long getFreeSpace() {
		return 0;
	}
	
	public long getUsableSpace() {
		return 0;
	}
	
	public long getTotalSpace() {
		return 0;
	}
	
	public String getParent() {
		return null;
	}
	
	public File getParentFile() {
		return null;
	}
	
	public boolean isAbsolute() {
		return true;
	}
	
	public boolean createNewFile() {
		return false;
	}
	
	public boolean setReadOnly() {
		return true;
	}
	
	public boolean setWritable(boolean writable) {
		return false;
	}
	
	public boolean setWritable(boolean writable, boolean ownerOnly) {
		return false;
	}
	
	public boolean setReadable(boolean readable) {
		return true;
	}
	
	public boolean setReadable(boolean readable, boolean ownerOnly) {
		return true;
	}
	
	public boolean setExecutable(boolean executable) {
		return false;
	}
	
	public boolean setExecutable(boolean executable, boolean ownerOnly) {
		return false;
	}
	
	public boolean renameTo(File dest) {
		return false;
	}
	
	public boolean mkdir() {
		return false;
	}
	
	public boolean mkdirs() {
		return false;
	}
	
	public String[] list() {
		return list(null);
	}
	
	public File[] listFiles() {
		return listFiles((FileFilter)null);
	}
	
	public URI[] listURIs() {
		return listURIs(null);
	}

	public String[] list(FilenameFilter filter) {
		if(filter == null) {
			filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return true;
				}
			};
		}
		Collection<File> children = getChildren();
		if(children == null) {
			return new String[]{};
		}
		ArrayList<String> files = new ArrayList<String>();
		for (File child : children) {
			if(child.exists() && filter.accept(child.getParentFile(), child.getName())) {
				files.add(child.toString());
			}
		}
		return files.toArray(new String[]{});
	}
	
	public File[] listFiles(FileFilter filter) {
		if(filter == null) {
			filter = new FileFilter() {
				public boolean accept(File pathname) {
					return true;
				}
			};
		}
		Collection<File> children = getChildren();
		if(children == null) {
			return new File[]{};
		}
		ArrayList<File> files = new ArrayList<File>();
		for (File child : children) {
			if(child.exists() && filter.accept(child)) {
				files.add(child);
			}
		}
		return files.toArray(new File[]{});
	}
	
	public File[] listFiles(FilenameFilter filter) {
		if(filter == null) {
			filter = new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return true;
				}
			};
		}
		Collection<File> children = getChildren();
		if(children == null) {
			return new File[]{};
		}
		ArrayList<File> files = new ArrayList<File>();
		for (File child : children) {
			if(child.exists() && filter.accept(child.getParentFile(), child.getName())) {
				files.add(child);
			}
		}
		return files.toArray(new File[]{});
	}
	
	public URI[] listURIs(URIFilter filter) {
		if(filter == null) {
			filter = new URIFilter() {
				public boolean accept(URI uri) {
					return true;
				}
			};
		}
		Collection<File> children = getChildren();
		if(children == null) {
			return new URI[]{};
		}
		ArrayList<URI> files = new ArrayList<URI>();
		for (File child : children) {
			if(child.exists() && filter.accept(child.toURI())) {
				files.add(child.toURI());
			}
		}
		return files.toArray(new URI[]{});
	}
	
	protected abstract Collection<File> getChildren();
	
}