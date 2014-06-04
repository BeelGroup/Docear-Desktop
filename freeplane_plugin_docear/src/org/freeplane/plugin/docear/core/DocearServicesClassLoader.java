package org.freeplane.plugin.docear.core;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.freeplane.core.util.LogUtils;

public class DocearServicesClassLoader extends URLClassLoader {

	public DocearServicesClassLoader(ClassLoader parentLoader) {
		super(new URL[0], parentLoader);
	}

	public void addURL(URL url) {
		try {
			URLConnection conn = url.openConnection();
			if(conn instanceof JarURLConnection) {
				JarURLConnection jarConn = (JarURLConnection) conn;
				JarFile jarFile = jarConn.getJarFile();
				try {
					ZipEntry entry = jarFile.getEntry("META-INF/org.docear.services.desc");
					if(entry == null) {
						super.addURL(url);
					}
				}
				finally {
					jarFile.close();
				}
			}
			else {
				super.addURL(url);
			}
		}
		catch (ClassCastException e) {
			LogUtils.warn("a not-jarfile url passed to DocearServicesClassLoader.addUrl()");
		}
		catch (IOException e) {
			LogUtils.warn(e);
		}
		
	}

}