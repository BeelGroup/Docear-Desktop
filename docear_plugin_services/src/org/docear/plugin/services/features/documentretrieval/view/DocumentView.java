package org.docear.plugin.services.features.documentretrieval.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.NoSuchElementException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.io.IOTools;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.documentretrieval.DocumentRetrievalController;
import org.docear.plugin.services.features.documentretrieval.model.DocumentEntry;
import org.docear.plugin.services.features.documentretrieval.model.DocumentModelNode;
import org.docear.plugin.services.features.documentretrieval.model.DocumentsModel;
import org.docear.plugin.services.features.documentretrieval.recommendations.RecommendationsController;
import org.docear.plugin.services.features.user.action.DocearUserServicesAction;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.WorkspaceController;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public abstract class DocumentView extends JPanel {
	private static final long serialVersionUID = 1L;
	protected static JTabbedPane tabPane;	
	private String evaluationLabel = "";
	private int recSetId = 0;
	
	public DocumentView(final DocumentsModel model) {
		this();
		setModel(model);
	}

	protected DocumentView() {
		Container cont = Controller.getCurrentController().getViewController().getContentPane();
		this.setLayout(new BorderLayout());
		updateTitle();		
		this.setBackground(Color.WHITE);
		this.setBorder(new LineBorder(Color.LIGHT_GRAY, 1));
		
		if(tabPane == null) {
			tabPane = findTabbedPane(cont);			
		}
		cont.remove(tabPane);
		cont.add(this, BorderLayout.CENTER, 0);
	}
	
	protected abstract Container getNewRecommandationContainerComponent(String title);
	protected abstract Container getNewEmptyContainerComponent();
	protected abstract void addComponendAfterDocumentList(Container documentList);
	
	public void close() throws NoSuchElementException {
		Container cont = Controller.getCurrentController().getViewController().getContentPane();
		if (DocumentRetrievalController.getView() == null) {
			return;
		}

		cont.remove(DocumentRetrievalController.getView());
		cont.add(tabPane, BorderLayout.CENTER, 0);
		DocumentRetrievalController.destroyView();
		((JComponent) cont).revalidate();
		UITools.getFrame().repaint();	
	}
//	public abstract void close();
	
	protected static JTabbedPane findTabbedPane(Container cont) throws NoSuchElementException {
		JTabbedPane tabPane = null;
		for(Component comp : cont.getComponents()) {
			if(comp instanceof JTabbedPane) {
				tabPane = (JTabbedPane) comp;
				break;
			}
		}
		if(tabPane == null) {
			LogUtils.warn("Exception in "+RecommendationsController.class+".getOrCreateRecommendationView(): could not find tabbed pane");
			throw new NoSuchElementException("could not find tabbed pane"); 
		}
		return tabPane;
	}

	public void setModel(DocumentsModel model) {
		removeAll();
		layoutModel(model);
		updateTitle();
		fireModelChanged(null, model);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				revalidate();
			}
		});
		
	}

	public void updateTitle() {
		String label = ServiceController.getCurrentUser().getName();
		if(label != null && label.trim().length() > 0) {
			setName(TextUtils.format("recommendations.map.label.forUser", label));
		} 
		else {
			setName(TextUtils.getText("recommendations.map.label.anonymous"));
		}
	}

	private void layoutModel(DocumentsModel model) {
		evaluationLabel = model.getEvaluationLabel();
		recSetId = model.getSetId();
		DocumentModelNode node = model.getRootNode();
		layoutModel(node, null);
		

	}

	private void layoutModel(DocumentModelNode node, Container parent) {
		if (node != null) {
			Object obj = node.getUserObject();
			Container container = parent;
			
			if(node instanceof DocumentModelNode.RecommendationContainerNode) {
				container = getNewRecommandationContainerComponent(obj.toString());
				if(parent == null) {
					//this.add(container);
				}
				else {
					parent.add(container);
				}
			}
			else if(node instanceof DocumentModelNode.UntitledRecommendationContainerNode) {
				container = getNewEmptyContainerComponent();
				if(parent == null) {
					this.add(container);
				}
				else {
					parent.add(container);
				}
			}
			else {
				
					if(container == null) {
						container = getNewEmptyContainerComponent();
						//this.add(container);
					}
				
				if (node instanceof DocumentModelNode.RecommendationEntryNode) {
					JComponent comp = getRecommendationComponent((DocumentEntry) obj);
					container.add(comp);
				} 
				else if(node instanceof DocumentModelNode.NoRecommendationsNode) {
						container.add(getNoRecommendationsComponent((String) obj));
				}
				else if(node instanceof DocumentModelNode.NoServiceNode) {
					container.add(getNoServiceComponent());
					}
					else {
						container.add(new JLabel(obj.toString()));
					}					
				}
				

			if (node.getChildCount() > 0) {
				for (DocumentModelNode child : node.getChildren()) {
					layoutModel(child, container);
				}
				
				addComponendAfterDocumentList(container);
			}			
		}
	}

	private JComponent getRecommendationComponent(final DocumentEntry recommendation) {
		DocumentEntryComponent comp = new DocumentEntryComponent(recommendation);
		comp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				URL page = recommendation.getClickUrl();
				try {
					page = redirectRecommendationLink(recommendation, page);
				} 
				catch (Exception ex) {
					//click didn't work
					LogUtils.info(ex.getMessage());
				}
				
				if(e.getID() == DocumentEntryComponent.OPEN_RECOMMENDATION) {					
					try {
						Controller.getCurrentController().getViewController().openDocument(page);
					} 
					catch (Exception ex) {
						LogUtils.warn("could not open link to (" + recommendation.getLink() + ")", ex);
					}
				}
				else if(e.getID() == DocumentEntryComponent.IMPORT_RECOMMENDATION) {
					//WORKSPACE - DOCEAR todo: change EventType -> recommendations are project independent, so there is no specific library
					DocearController.getController().getEventQueue().dispatchEvent(new DocearEvent(page, null, DocearEventType.IMPORT_TO_LIBRARY, recommendation.getTitle()));
				}
			}

			private URL redirectRecommendationLink(final DocumentEntry recommendation, URL page) throws IOException, MalformedURLException {
				URLConnection connection;
				connection = recommendation.getClickUrl().openConnection();
				if(connection instanceof HttpURLConnection) {
					HttpURLConnection hconn = (HttpURLConnection) connection;							
				    hconn.setInstanceFollowRedirects(false);
				    String accessToken = ServiceController.getCurrentUser().getAccessToken();
				    hconn.addRequestProperty("accessToken", accessToken);
				    
				    int response = hconn.getResponseCode();
				    boolean redirect = (response >= 300 && response <= 399);
				    

				    /*
				     * In the case of a redirect, we want to actually change the URL
				     * that was input to the new, redirected URL
				     */
				    if (redirect) {
						String loc = connection.getHeaderField("Location");
						if (loc.startsWith("http", 0)) {
						    page = new URL(loc);
						} else {
						    page = new URL(page, loc);
						}
					} 
				    else {
				    	if(response == 200) {
					    	String content = IOTools.getStringFromStream(connection.getInputStream(), "UTF-8");
					    	String searchPattern = "<meta http-equiv=\"REFRESH\" content=\"0;url=";
							int pos = content.indexOf(searchPattern);
					    	if(pos >= 0) {
					    		String loc = content.substring(pos+searchPattern.length());
					    		loc = loc.substring(0,loc.indexOf("\""));
					    		if (loc.startsWith("http", 0)) {
								    page = new URL(loc);
								} else {
								    page = new URL(page, loc);
								}
					    	}
					    }
				    }
				}
				return page;
			}
		});
		return comp;
	}

	
	
	protected Component getStarBar() {
		return new StarPanel(evaluationLabel, recSetId);
	}

	protected Component getNewButtonBar(boolean showRefreshButton) {
		JPanel panel = new JPanel(new LayoutManager() {
			
			public void removeLayoutComponent(Component comp) {				
			}
			
			public Dimension preferredLayoutSize(Container parent) {
				int count = parent.getComponentCount();
				for(Component comp : parent.getComponents()) {
					Dimension comDim = comp.getPreferredSize();
					if(comDim != null) {
						return new Dimension((parent.getInsets().left+parent.getInsets().right+comDim.width)*count, parent.getInsets().top+parent.getInsets().bottom+comDim.height);
					}
				}
				return null;
			}
			
			public Dimension minimumLayoutSize(Container parent) {
				for(Component comp : parent.getComponents()) {
					int count = parent.getComponentCount();
					Dimension comDim = comp.getMinimumSize();
					if(comDim != null) {
						return new Dimension((parent.getInsets().left+parent.getInsets().right+comDim.width)*count, parent.getInsets().top+parent.getInsets().bottom+comDim.height);
					}
				}
				return null;
			}
			
			public void layoutContainer(Container parent) {
				int right = parent.getWidth()-parent.getInsets().right;
				int top = parent.getInsets().top;
				int count = parent.getComponentCount();
				for(Component comp : parent.getComponents()) {
					comp.setSize(comp.getPreferredSize());
					int x = right-comp.getWidth()*count;
					comp.setLocation(x,top);
					count--;
				}
			}
			
			public void addLayoutComponent(String name, Component comp) {
				
			}
		});
		panel.setBackground(Color.white);
		panel.setBorder(new EmptyBorder(5, 5, 0, 5));
		
		if (showRefreshButton) {
    		JButton refreshButton = new JButton(new ImageIcon(DocumentEntryComponent.class.getResource("/icons/view-refresh-7_32x32.png")));
    		refreshButton.setMinimumSize(new Dimension(50, 50));
    		refreshButton.setPreferredSize(new Dimension(50, 50));
    		refreshButton.addActionListener(new ActionListener() {			
    			public void actionPerformed(ActionEvent e) {
    				DocumentRetrievalController.getController().refreshDocuments();
    			}
    		});
    		refreshButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    		refreshButton.setToolTipText(TextUtils.getText("recommendations.refresh.title"));
    		panel.add(refreshButton);
		}
		
		JButton closeButton = new JButton(new ImageIcon(DocumentEntryComponent.class.getResource("/icons/window-close-2_32x32.png")));
		closeButton.setMinimumSize(new Dimension(50, 50));
		closeButton.setPreferredSize(new Dimension(50, 50));
		closeButton.addActionListener(new ActionListener() {			
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new  Runnable() {
					public void run() {
						DocumentRetrievalController.getController().closeDocumentView();
					}
				});
			}
		});
		closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		closeButton.setToolTipText(TextUtils.getText("recommendations.close.title"));
		panel.add(closeButton);
		return panel;
	}
		
	private Component getNoRecommendationsComponent(String message) {
		return new NoRecommendations(message);
		}
	
	private Component getNoServiceComponent() {
		return new NoService();
	}
	
//	private Component getProgressBarNode(int min, int max) {
//		return new ProgressBarComponent(min, max);
//	}
	
	protected class NoRecommendations extends JLabel implements NodeModelItem {
		
		private static final long serialVersionUID = 1L;
		
		private final String text;

		public NoRecommendations(String message) {
			this.text = message;
			this.setText(this.getText());
			this.setHorizontalAlignment(CENTER);
			this.setVerticalAlignment(CENTER);
			this.setBorder(new EmptyBorder(30, 10, 30, 10));
		}
		
		public String getText() {
			return this.text;
		}
		
		public String toString() {
			return getText();
		}
		
	}
	
	protected class ProgressBarComponent extends JPanel implements NodeModelItem {
		private static final long serialVersionUID = 1L;

		public ProgressBarComponent(int min, int max) {
			this.setLayout(new BorderLayout());
			JProgressBar bar = new JProgressBar();
			if(max < min) {
				bar.setIndeterminate(true);
			}
			else {
				bar.setMinimum(min);
				bar.setMaximum(max);
			}
			this.add(bar, BorderLayout.CENTER);
		}

		public String getText() {
			return "waiting ...";
		}
		
	}
	
	protected class NoService extends JPanel implements NodeModelItem {
		private static final long serialVersionUID = 1L;
		private final String text;

		public NoService() {
			this.text = TextUtils.getText("recommendations.error.no_service");
			setLayout(new FormLayout(new ColumnSpec[] {
					FormFactory.RELATED_GAP_COLSPEC,
					FormFactory.DEFAULT_COLSPEC,
					FormFactory.RELATED_GAP_COLSPEC,
					ColumnSpec.decode("default:grow"),},
				new RowSpec[] {
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("fill:default"),
					FormFactory.RELATED_GAP_ROWSPEC,
					FormFactory.DEFAULT_ROWSPEC,
					FormFactory.RELATED_GAP_ROWSPEC,
					RowSpec.decode("default:grow"),}));
			
			JLabel lblText = new JLabel(text);
			add(lblText, "2, 2, 3, 1");
			
			JButton btnNewButton = new JButton(TextUtils.getText("recommendations.error.no_service.button"));
			add(btnNewButton, "2, 4");
			btnNewButton.addActionListener(new ActionListener() {				
				public void actionPerformed(ActionEvent e) {
					WorkspaceController.getAction(DocearUserServicesAction.KEY).actionPerformed(null);
				}
			});
		}
		
		public String getText() {
			return this.text;
		}
		
		public String toString() {
			return getText();
		}
		
	}
	
	interface NodeModelItem {
		public String getText();
	}
	
	
	
	protected final class ListLayoutManager implements LayoutManager {
		public ListLayoutManager() {
		}

		public void removeLayoutComponent(Component comp) {				
		}

		public Dimension preferredLayoutSize(Container parent) {
			if(parent.getComponentCount() > 0) {
				Dimension compPref = parent.getComponent(0).getPreferredSize();
//				for(Component comp : parent.getComponents()) {
//					
//				}
				Insets insets = new Insets(0, 0, 0, 0); 
				if(parent instanceof JComponent) {
					insets = ((JComponent) parent).getInsets();
				}
				return new Dimension(compPref.width+insets.left+insets.right, compPref.height*parent.getComponentCount()+insets.top+insets.bottom);
			}
			return new Dimension();
		}

		public Dimension minimumLayoutSize(Container parent) {
			return parent.getMinimumSize();
		}
			
		public void layoutContainer(Container parent) {
			int i = parent.getComponentCount()-1;
			if(i < 0) {
				return;
			}
			Insets insets = new Insets(0, 0, parent.getHeight(), parent.getWidth());
			if(parent instanceof JComponent) {
				insets = ((JComponent) parent).getInsets();
			}
			int width = parent.getWidth()-insets.left-insets.right;
			int height = parent.getComponent(0).getPreferredSize().height;
			int x = insets.left;
			if(parent.getHeight() < ((height*(i+1)) + insets.top)) {
				height = (parent.getHeight()-insets.top-insets.bottom)/(i+1);
			}
			if(height < 20) {
				height = 20;
			}
			for(; i >= 0; i-- ) {
				Component comp = parent.getComponent(i);	
				int y = i*height + insets.top;
				setPreferredSize(new Dimension(width, height));
				comp.setBounds(x, y, width, height);
			}
		}
	
		public void addLayoutComponent(String name, Component comp) {
							
		}
	}

	private void fireModelChanged(DocumentsModel oldModel, DocumentsModel newModel) {
		DocumentViewChangedEvent event = new DocumentViewChangedEvent(this, DocumentViewChangedEvent.MODEL_CHANGED_TYPE, oldModel, newModel);
		synchronized (listenerList) {
			DocumentViewListener[] listeners = listenerList.getListeners(DocumentViewListener.class);
			for (DocumentViewListener listener : listeners) {
				listener.viewChanged(event);
			}
		}
	}
	
	public void addViewChangeListener(DocumentViewListener listener) {
		synchronized (listenerList) {
			listenerList.add(DocumentViewListener.class, listener);
		}		
	}
}
