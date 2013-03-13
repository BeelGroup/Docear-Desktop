package org.docear.plugin.pdfutilities.util;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;

import org.freeplane.plugin.workspace.URIUtils;

public class CustomFileFilter implements FileFilter {
	
	String regex;
	
	public CustomFileFilter(String regex){
		this.regex = regex;
	}

	public boolean accept(File file) {
        if(file == null) return false;
        
        String path = file.getPath();

        return file.exists() && accept(path);
    }
	
	public boolean accept(URI uri){
		File file = URIUtils.getFile(uri);
		if(uri == null || file == null || !file.exists()){
			return false;
		}
		else{
			return this.accept(uri.toString());
		}	
	}

    public boolean accept(String path) {
        if(path == null || path.trim().length()==0) return false;
        
        if(path.matches(regex)){
            return true;
        }        
        else{
            return false;
        }
    }

}
