package org.docear.plugin.services.features.documentretrieval.documentsearch.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.io.InputStreamReader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.model.DocumentsModel;
import org.docear.plugin.services.features.documentretrieval.view.DocumentView;
import org.docear.plugin.services.features.io.DocearConnectionProvider;
import org.docear.plugin.services.features.io.DocearServiceResponse;
import org.docear.plugin.services.features.user.DocearUser;
import org.docear.plugin.services.xml.DocearXmlBuilder;
import org.docear.plugin.services.xml.DocearXmlElement;
import org.docear.plugin.services.xml.DocearXmlRootElement;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.IXMLParser;
import org.freeplane.n3.nanoxml.IXMLReader;
import org.freeplane.n3.nanoxml.StdXMLReader;
import org.freeplane.n3.nanoxml.XMLParserFactory;

public class DocumentSearchView extends DocumentView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private DocumentSearchPanel documentSearchPanel;
	
	public DocumentSearchView(DocumentsModel model) {
		super(model);
	}
	
	public DocumentSearchView() {
		super();
	}
	
	@Override
	protected Container getNewRecommandationContainerComponent(String title) {
//		JPanel containerPanel = new JPanel();
//		containerPanel.setLayout(new BorderLayout());
//		containerPanel.setBackground(Color.WHITE);
//		containerPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
//		JPanel panel = new JPanel();
//				
//		JLabel containerTitle = new JLabel("<html><b>"+title+"</b></html>");
//		containerTitle.setFont(containerTitle.getFont().deriveFont(Font.BOLD, 18));
//		
//		containerPanel.add(containerTitle, BorderLayout.NORTH);
//		
//		panel.setBackground(Color.WHITE);
//		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
//		panel.setLayout(new ListLayoutManager());		
//		containerPanel.add(panel, BorderLayout.CENTER);
//		
//		this.add(getNewButtonBar(), BorderLayout.NORTH);
//		this.add(containerPanel, BorderLayout.CENTER);
//		this.add(getStarBar(), BorderLayout.SOUTH);
		
//		return panel
		return new JPanel();
	}
	
	@Override
	protected Container getNewEmptyContainerComponent() {
		JPanel containerPanel = new JPanel();
		containerPanel.setLayout(new BorderLayout());
		containerPanel.setBackground(Color.WHITE);
		containerPanel.setBorder(new EmptyBorder(0, 10, 0, 0));
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBorder(new LineBorder(Color.LIGHT_GRAY, 1, true));
		panel.setLayout(new ListLayoutManager());		
		containerPanel.add(panel, BorderLayout.CENTER);
		
		this.add(getSearchPanel(), BorderLayout.NORTH);
		this.add(containerPanel, BorderLayout.CENTER);
		this.add(getStarBar(), BorderLayout.SOUTH);
		
		return panel;
	}

	public Component getSearchPanel() {
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(getNewButtonBar(), BorderLayout.NORTH);
		documentSearchPanel = new DocumentSearchPanel(getSearchModel());
		panel.add(documentSearchPanel, BorderLayout.CENTER);
		return panel;
	}
	
	private String[] getSearchModel() {
		final DocearUser user = ServiceController.getCurrentUser();		
		if (user == null) {
			return null;
		}
		
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> task = executor.submit(new Callable<String>() {
			public String call() throws Exception {
				try {
    				DocearServiceResponse response = ServiceController.getConnectionController().get("/user/"+user.getUsername()+"/searchmodel/");
    				
    				DocearXmlBuilder xmlBuilder = new DocearXmlBuilder();
    				IXMLReader reader = new StdXMLReader(new InputStreamReader(response.getContent(), "UTF8"));
    				IXMLParser parser = XMLParserFactory.createDefaultXMLParser();
    				parser.setBuilder(xmlBuilder);
    				parser.setReader(reader);
    				parser.parse();
    				DocearXmlRootElement result = (DocearXmlRootElement) xmlBuilder.getRoot();
    				
    				DocearXmlElement element = result.find("searchmodel");
					return element.getContent().trim();
				}
				catch(NullPointerException ignore) {}
				return null;
			}
		});
		try {
			String model = task.get(DocearConnectionProvider.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
			if (model == null) {
				return null;
			}
			return model.split(" ");
		}
		catch(Exception e) {
			LogUtils.warn(e);
		}
		
		return null;
	}
	
	public String getQueryText() {
		if (this.documentSearchPanel == null) {
			return "";
		}
		return this.documentSearchPanel.getQueryText();
	}

}
