package org.docear.plugin.bibtex.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.freeplane.core.util.LogUtils;

public class DocearTransformZoteroFile implements IPreOpenAction {
	
	private boolean changes = false;
	
	@Override
	public boolean isActionNecessary(File file) {		
		return false;
	}
	
	@Override
	public void performAction(File f) {
		try {
			String convertedContent = readFile(f);
    		
    		if (changes) {
    			writeFile(f, convertedContent);
    		}
    	}
    	catch (FileNotFoundException e) {			
    		LogUtils.severe("DocearTransformZoteroFile.performAction(): " + e.getMessage());
    	}	
    	catch (IOException e) {
    		LogUtils.severe(e);
    	}
	}
	
	
	private String readFile(File f) throws IOException {	
		BufferedReader br = new BufferedReader(new FileReader(f));
		StringBuffer convertedContent = new StringBuffer();
		
		String outside;
		while ((outside = getOutside(br)) != null) {
			convertedContent.append(outside);
			
			String entry = getEntry(br);
			if (entry != null) {
				convertedContent.append(entry);
			}
		}
					
		br.close();
		
		return convertedContent.toString();
	}
	
	
	private void writeFile(File f, String convertedContent) throws IOException {
		FileUtils.writeStringToFile(f, convertedContent);
	}
	
	
	private String getOutside(final BufferedReader br) throws IOException {
		StringBuffer sb = new StringBuffer();
		String line;
		while ((line = br.readLine()) != null && !line.trim().startsWith("@")) {
			sb.append(line);
		}
		
		return sb.toString();
	}
	
	private String getEntry(final BufferedReader br) throws IOException {
		StringBuffer sb = new StringBuffer();
		String line;
		
		int openbrackets = 0;
		do {
			line = br.readLine();
			if (line == null) {
				throw new IOException("brackets are not matching");
			}
			
			String convertedLine = line.replaceAll("\\{", ""); 
			convertedLine = line.replaceAll("\\}", "");
			if (!line.equals(convertedLine)) {
				changes = true;
			}
			
			openbrackets += StringUtils.countMatches(convertedLine, "{");
			openbrackets -= StringUtils.countMatches(convertedLine, "}");
			
			sb.append(convertedLine);
		}
		while (openbrackets > 0);
		
		return sb.toString();		
	}


	

}
