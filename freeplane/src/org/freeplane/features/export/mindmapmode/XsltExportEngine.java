package org.freeplane.features.export.mindmapmode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.MapWriter.Mode;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;

public class XsltExportEngine implements IExportEngine {
	public XsltExportEngine(File xsltFile) {
	    super();
	    this.xsltFile = xsltFile;
    }
	final private File xsltFile;

	public void export(MapModel map, File toFile) {
		final Source xsltSource = new StreamSource(xsltFile);
		final Source xmlSource = getMapXml(map);
		FileOutputStream outputStream = null;
        try {
        	outputStream = new FileOutputStream(toFile);
        	final Result result = new StreamResult(outputStream);
        	final TransformerFactory transFact = TransformerFactory.newInstance();
        	final Transformer trans = transFact.newTransformer(xsltSource);
        	trans.transform(xmlSource, result);
        }
        catch (final Exception e) {
        	UITools.errorMessage(TextUtils.getText("export_failed"));
        	LogUtils.warn(e);
        }
        finally {
        	try {
        		if (outputStream != null) {
        			outputStream.close();
        		}
        	}
        	catch (final IOException e) {
        		LogUtils.severe(e);
        	}
        }
	}
	/**
	 * @param mode 
	 * @throws IOException
	 */
	private StreamSource getMapXml(final MapModel map) {
		final StringWriter writer = new StringWriter();
		final ModeController modeController = Controller.getCurrentModeController();
		try {
			modeController.getMapController().getFilteredXml(map, writer, Mode.EXPORT, true);
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
		
		String string = clean(writer.getBuffer().toString());		
		
		final StringReader stringReader = new StringReader(string);
		return new StreamSource(stringReader);
	}
	
	private String clean(String string) {
		StringBuilder sb = new StringBuilder();
		int startingPos = 0;
		
		while (string.indexOf("&#", startingPos) >= 0) {			
			int specialCharPos1 = string.indexOf("&#", startingPos);
				sb.append(string.substring(startingPos, specialCharPos1));
				int specialCharPos2 = string.indexOf(";", specialCharPos1);
				
				String specialChar = string.substring(specialCharPos1+2, specialCharPos2);
				
				Integer specialCharValue = null;
				if (specialChar.startsWith("x")) {
					specialCharValue = Integer.decode("0"+specialChar);
				}
				else {
					specialCharValue = Integer.parseInt(specialChar);
				}
				
				if ((specialCharValue == 0x9) ||
		                (specialCharValue == 0xA) ||
		                (specialCharValue == 0xD) ||
		                ((specialCharValue >= 0x20) && (specialCharValue <= 0xD7FF)) ||
		                ((specialCharValue >= 0xE000) && (specialCharValue <= 0xFFFD)) ||
		                ((specialCharValue >= 0x10000) && (specialCharValue <= 0x10FFFF))) {
					sb.append(string.substring(specialCharPos1, specialCharPos2+1));
				}
				else {
					LogUtils.info("filtered specialChar: &#"+specialChar);					
				}
			startingPos = specialCharPos2+1;			
		}		
		sb.append(string.substring(startingPos));
		
		return sb.toString();
	}
}
