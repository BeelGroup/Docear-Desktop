package org.docear.plugin.bibtex.jabref;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.regex.Pattern;

import org.docear.plugin.bibtex.actions.IPreOpenAction;
import org.docear.plugin.core.logging.DocearLogger;

public class ZoteroAnnoteFieldRemoverAction implements IPreOpenAction {
	
	public final static Pattern FIELD_END_1 = Pattern.compile("\\},$");
	public final static Pattern FIELD_END_2 = Pattern.compile("\\}$");
	
	@Override
	public boolean isActionNecessary(File file) {
		return true;
	}

	@Override
	public void performAction(File file) {
		StringBuffer sb = new StringBuffer();
				
		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;
			while ((line = br.readLine()) != null) {
				if (line.trim().startsWith("annote = {")) {
					while (!line.endsWith("},") && !line.endsWith("}")) {
						line = br.readLine();
					}
				}
				else {
					sb.append(line);
					sb.append("\r\n");
				}
			}
			
			br.close();
			
			FileWriter fw = new FileWriter(file);
			fw.write(sb.toString());
			fw.close();
		}
		catch (IOException e) {			
			DocearLogger.error(e);
		}
		
	}
	
}
