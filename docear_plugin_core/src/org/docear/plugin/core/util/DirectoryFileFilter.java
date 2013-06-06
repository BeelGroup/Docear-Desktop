package org.docear.plugin.core.util;

import java.io.File;
import java.util.List;

import org.docear.plugin.core.DocearController;

public class DirectoryFileFilter extends ADocearFileFilter {

    public boolean accept(File file) {
        if(file.isDirectory()){
            List<String> subfolders = getStringList(DocearController.getPropertiesController().getProperty("docear_subdirs_to_ignore", null));
            for(String subfolder : subfolders){
                if(file.getName().equals(subfolder)){
                    return false;
                }
            }
            return true;
        }
        else{
            return false;
        }
    }
}
