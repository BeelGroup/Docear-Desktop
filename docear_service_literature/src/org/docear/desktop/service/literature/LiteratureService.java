package org.docear.desktop.service.literature;

import java.io.File;

import org.docear.pdf.PdfDataExtractor;
import org.freeplane.plugin.docear.core.DocearServiceContext;
import org.freeplane.plugin.docear.core.spi.DocearService;
import org.freeplane.plugin.docear.util.DocearLogger;

public class LiteratureService implements DocearService {

	public LiteratureService() {
	}
	
	public void install(DocearServiceContext context) {
		DocearLogger.info("installing Literature Service components...");
		try {
			PdfDataExtractor extractor = new PdfDataExtractor(new File("C:\\Users\\mag\\Documents\\literature\\memory-efficient-java-tutorial.pdf"));
			System.out.println("title: " + extractor.extractTitle());
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	public void uninstall(DocearServiceContext context) {
		// TODO Auto-generated method stub
		
	}

}
