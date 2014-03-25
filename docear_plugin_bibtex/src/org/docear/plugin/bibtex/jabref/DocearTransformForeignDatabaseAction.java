package org.docear.plugin.bibtex.jabref;

import java.io.IOException;
import java.util.regex.Pattern;

import org.freeplane.core.util.LogUtils;

import net.sf.jabref.BasePanel;
import net.sf.jabref.BibtexDatabase;
import net.sf.jabref.BibtexEntry;
import net.sf.jabref.imports.ParserResult;
import net.sf.jabref.imports.PostOpenAction;

public class DocearTransformForeignDatabaseAction implements PostOpenAction {

	private final static Pattern COLON_PATTERN = Pattern.compile("(?<!\\\\):", Pattern.MULTILINE);
	private final static Pattern SEMICOLON_PATTERN = Pattern.compile("(?<!\\\\);", Pattern.MULTILINE);
	private final static Pattern BACKSLASH_PATTERN = Pattern.compile("(?<!\\\\)\\\\(?![\\\\:])");
	
	@Override
	public boolean isActionNecessary(ParserResult pr) {
		return true;
	}

	@Override
	public void performAction(BasePanel panel, ParserResult pr) {
		BibtexDatabase db = pr.getDatabase();
		
		for (BibtexEntry entry : db.getEntries()) {
			String fileFieldContent = entry.getField("file");
			if (fileFieldContent != null && fileFieldContent.length()>0) {
				try {
					entry.setField("file", convertFileField(fileFieldContent));
				}
				catch(Exception e) {
					LogUtils.severe("file-field content not well formed: "+fileFieldContent);
				}
			}
		}
	}
	
	private String convertFileField(String fileFieldContent) throws ArrayIndexOutOfBoundsException {
		StringBuffer sb = new StringBuffer();	
		
		String[] fileContents = SEMICOLON_PATTERN.split(fileFieldContent);
		
		for (int i=0; i<fileContents.length; i++) {
			if (sb.length() > 0) {
				sb.append(";");
			}
			try {
				sb.append(convertSingleFileContent(fileContents[i]));
			}
 			catch(IOException e) {
				fileContents[i+1] = fileContents[i]+"\\\\;"+fileContents[i+1];
			}
		}
		
		return sb.toString();
	}
	
	private String convertSingleFileContent(String content) throws IOException {
		StringBuffer sb = new StringBuffer();		
		int typePointer = content.lastIndexOf(":");		
		
		if (typePointer < 0) {
			// file content not complete, maybe due to unescaped semicolons (Zotero style) --> try to add the next field
			throw new IOException("file content not complete!");
		}
		
		int pathPointer = content.substring(0, typePointer).lastIndexOf(":");
		if (typePointer-pathPointer <= 2) {
			// file content not complete (MS Windows Drive letter colon is no content separator
			throw new IOException("file content not complete!");
		}
		
		//test if this pos is part of a windows path (after a drive letter) and if it is: move further backwards
		int tmpPointer = content.substring(0, pathPointer).lastIndexOf(':');
		if (tmpPointer >= 0) {
			String possibleDriverLetter = content.substring(tmpPointer, pathPointer);
		
			possibleDriverLetter = possibleDriverLetter.replaceAll("\\\\", "").trim();
			if (possibleDriverLetter.length()==2) {
				pathPointer = tmpPointer;
			}
		}
		
		String type = content.substring(typePointer+1);
		String path = content.substring(pathPointer+1, typePointer);		
		String description = content.substring(0, pathPointer);
		
		sb.append(convertDescription(description)).append(":");
		sb.append(convertPath(path)).append(":");
		sb.append(type);
		
		return sb.toString();
	}
	
	private String convertDescription(String description) {		
		description = COLON_PATTERN.matcher(description).replaceAll("\\\\:");
		description = BACKSLASH_PATTERN.matcher(description).replaceAll("\\\\\\\\");
		return description;
	}

	private String convertPath(String path) {		
		path = COLON_PATTERN.matcher(path).replaceAll("\\\\:");
		path = BACKSLASH_PATTERN.matcher(path).replaceAll("\\\\\\\\");		
		return path;
	}
	
	

}
