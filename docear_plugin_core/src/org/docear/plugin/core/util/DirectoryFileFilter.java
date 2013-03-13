package org.docear.plugin.core.util;

import java.io.File;
import java.util.List;

import org.freeplane.core.resources.ResourceController;

public class DirectoryFileFilter extends ADocearFileFilter {

    public boolean accept(File file) {
        if(file.isDirectory()){
            List<String> subfolders = getStringList(ResourceController.getResourceController().getProperty("docear_subdirs_to_ignore", null));
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
