package org.docear.plugin.pdfutilities.pdf;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;

import org.freeplane.plugin.workspace.URIUtils;

public class PdfFileFilter implements FileFilter{
	
	
	public boolean accept(File file) {
        if(file == null) return false;
        
        String path = file.getPath();

        return file.exists() && accept(path);
    }
	
	public static boolean accept(URI uri){
		File file = URIUtils.getAbsoluteFile(uri);
		if(uri == null || file == null || !file.exists()){
			return false;
		}
		else{
			return accept(uri.toString());
		}	
	}

    public static  boolean accept(String path) {
        if(path == null || path.trim().length()==0) return false;
        
        if(path.toLowerCase().endsWith(".pdf")){ 
            return true;
        }        
        else{
            return false;
        }
    }
	

    

}
