package org.docear.plugin.core.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.workspace.controller.DocearConversionDescriptor;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.XMLException;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.model.AWorkspaceTreeNode;
import org.freeplane.plugin.workspace.model.IResultProcessor;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;
import org.freeplane.plugin.workspace.model.project.ProjectLoader;
import org.freeplane.plugin.workspace.nodes.ProjectRootNode;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class ImportProjectPagePanel extends AWizardPage {

	private static final SimpleDateFormat format = new SimpleDateFormat("M/d/yy HH:mm");
	
	private static final long serialVersionUID = 1L;
	private JTextField txtImportHome;
	
	private ProjectVersionsModel versionModel;
	private JList lstVersions;

	private WizardSession cachedContext;

	private JLabel lblWarning;

	private JCheckBox chckbxDeleteOldSettings;

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public ImportProjectPagePanel() {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
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
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		JEditorPane dtrpnInfo = new JEditorPane();
		dtrpnInfo.setBackground(Color.WHITE);
		dtrpnInfo.setEnabled(true);
		dtrpnInfo.setEditable(false);
		dtrpnInfo.setText(TextUtils.getText("docear.setup.wizard.import.info"));
		add(dtrpnInfo, "2, 2, 5, 1, fill, fill");
		
		JLabel lblSelectTheHome = new JLabel(TextUtils.getText("docear.setup.wizard.import.home.label"));
		add(lblSelectTheHome, "2, 6, 5, 1");
		
		txtImportHome = new JTextField();
		txtImportHome.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			
			@Override
			public void keyReleased(KeyEvent e) {
				updateProjectVersions();
			}
			
			@Override
			public void keyPressed(KeyEvent e) {}
		});
		add(txtImportHome, "2, 8, fill, default");
		txtImportHome.setColumns(10);
		
		JButton btnBrowse = new JButton(TextUtils.getText("docear.setup.wizard.import.home.browse"));
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				File home = URIUtils.getAbsoluteFile(getImportHome());
				while(home != null && !home.exists()) {
					home = home.getParentFile();
				}
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileHidingEnabled(true);
				if(home != null) {
					chooser.setCurrentDirectory(home);
				}
				int response = chooser.showOpenDialog(ImportProjectPagePanel.this);
				if(response == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					setImportHome(file.getAbsolutePath());
				}
			}
		});
		add(btnBrowse, "4, 8");
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, "2, 10, 4, 1, fill, fill");
		
		lstVersions = new JList();
		lstVersions.setVisibleRowCount(10);
		lstVersions.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		lstVersions.setModel(getModel());
		lstVersions.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				enableControls(cachedContext);
			}
		});
		scrollPane.setViewportView(lstVersions);
		
		lblWarning = new JLabel();
		lblWarning.setVisible(false);
		URL url = WorkspaceController.class.getResource("/images/16x16/dialog-warning-4.png");
		if(url != null) {
			lblWarning.setIcon(new ImageIcon(url));
		}
		
		
		chckbxDeleteOldSettings = new JCheckBox(TextUtils.getText("docear.setup.wizard.import.delete.old"));
		chckbxDeleteOldSettings.setBackground(Color.WHITE);
		chckbxDeleteOldSettings.setEnabled(false);
		add(chckbxDeleteOldSettings, "2, 12, 5, 1");
		add(lblWarning, "2, 14");
		
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/
	private void setImportHome(String path) {
		txtImportHome.setText(path);
		updateProjectVersions();
	}
	
	private URI getImportHome() {
		if(txtImportHome.getText().length()==0) {
			return WorkspaceController.getDefaultProjectHome();
		}
		return new File(txtImportHome.getText()).toURI();
	}

	private void enableControls(WizardSession context) {
		chckbxDeleteOldSettings.setEnabled(false);
		if(context != null) {
			boolean enabled = getModel().getSize() > 0;
			lblWarning.setText(TextUtils.getText("docear.setup.wizard.import.warn1"));
			lblWarning.setVisible(false);
			lstVersions.setEnabled(enabled);
			
			if(lstVersions.getSelectedValue() == null) {
				context.getNextButton().setEnabled(false);
				if(lstVersions.isEnabled()) {
					lblWarning.setText(TextUtils.getText("docear.setup.wizard.import.warn2"));
					lblWarning.setVisible(true);
				}
				else if(!txtImportHome.getText().isEmpty()) {
					lblWarning.setVisible(true);
				}
			}
			else {
				if(WorkspaceController.getCurrentModel().getProject(((VersionItem) lstVersions.getSelectedValue()).getProject().getProjectID()) != null) {
					lblWarning.setText(TextUtils.getText("docear.setup.wizard.import.warn3"));
					lblWarning.setVisible(true);
					context.getNextButton().setEnabled(false);
				}
				else if(getProject().getExtensions(DocearConversionDescriptor.class) != null) {
					lblWarning.setText(TextUtils.getText("docear.setup.wizard.import.warn4"));
					lblWarning.setVisible(true);
					chckbxDeleteOldSettings.setEnabled(true);
					context.getNextButton().setEnabled(true);
				}
				else{
					context.getNextButton().setEnabled(true);
					lblWarning.setVisible(false);
				}
			}
		}
		
	}
	
	private void updateProjectVersions() {
		File home = new File(txtImportHome.getText());
		getModel().clear();
		lstVersions.getSelectionModel().clearSelection();
		
		File _data = new File(home, "_data");
		if(_data.exists()) {
			readVersions(_data);		
		}
		lookForIncompatibles();
		if(getModel().getSize() > 0) {
			lstVersions.setSelectedIndex(0);
		}
		enableControls(cachedContext);
	}
	
	private void readVersions(File home) {
		for(File folder : home.listFiles(new FileFilter() {			
			public boolean accept(File pathname) {
				if(pathname.isDirectory()) {
					return true;
				}
				return false;
			}
			})) {
			
			File settings = new File(folder, "settings.xml");
			if(settings.exists()) {
				AWorkspaceProject project = AWorkspaceProject.create(folder.getName(), home.getParentFile().toURI());
				String item = new TempProjectLoader().getMetaInfo(project);
				if(item == null) {
					continue;
				}
				//see issue #113
				if(WorkspaceController.getCurrentModel().getProject(project.getProjectID()) == null) {
					getModel().addItem(new VersionItem(project, item, new Date(settings.lastModified())));
				}
			}
		}
	}
	
	private ProjectVersionsModel getModel() {
		if(this.versionModel == null) {
			this.versionModel = new ProjectVersionsModel();
		}
		return this.versionModel;
	}
	
	private void lookForIncompatibles() {
		File dir = URIUtils.getFile(getImportHome());
		if(dir != null) {
			dir = DocearConversionDescriptor.getOldProfilesHome(dir);
			if(dir.exists()) {
				for (File profileHome : dir.listFiles(new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						if(pathname.isDirectory()) {
							return true;
						}
						return false;
					}
				})) {
					if(new File(profileHome, "workspace.xml").exists()) {
						String profileName = profileHome.getName();
						DocearWorkspaceProject project = new DocearWorkspaceProject(getImportHome());
						project.addExtension(new DocearConversionDescriptor(project, profileName));
						getModel().addItem(new ConversionItem(project, profileName, new Date(profileHome.lastModified())));
					}
				}
				//enableControlls(cachedContext);
			}
		}
		
	}
	
	public boolean isConversionNecessary() {
		return getProject().getExtensions(DocearConversionDescriptor.class) != null;
	}
	
	public boolean deleteOldSettings() {
		return chckbxDeleteOldSettings.isEnabled() && chckbxDeleteOldSettings.isSelected();
	}
	
	public AWorkspaceProject getProject() {
		VersionItem item = (VersionItem) lstVersions.getSelectedValue();
		if(item == null) {
			return null;
		}
		DocearConversionDescriptor desc = item.getProject().getExtensions(DocearConversionDescriptor.class);
		if(desc != null) {
			desc.setDeleteOldSettings(deleteOldSettings());
		}
		return item.getProject();
	}

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	@Override
	public String getTitle() {
		//TODO Service
		if(DocearController.getController().isServiceAvailable()){
			return TextUtils.getText("docear.setup.wizard.import.title");
		}
		else{
			return TextUtils.getText("docear.setup.wizard.import.title.noservice");
		}
	}

	@Override
	public void preparePage(WizardSession context) {
		this.cachedContext = context;
		context.setWizardTitle(getTitle());
		context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
		enableControls(context);
	}
	
	/***********************************************************************************
	 * NESTED TYPES
	 **********************************************************************************/
	
	class ProjectVersionsModel implements ListModel {
		
		private List<VersionItem> items = new ArrayList<VersionItem>();
		private List<ListDataListener> listeners = new ArrayList<ListDataListener>();

		public int getSize() {
			return items.size();
		}

		public Object getElementAt(int index) {
			return items.get(index);
		}

		public void addListDataListener(ListDataListener l) {
			listeners.add(l);
		}

		public void removeListDataListener(ListDataListener l) {
			listeners.remove(l);
		}
		
		public void clear() {
			int endIdx = items.size();
			this.items.clear();
			fireItemsRemoved(0, endIdx);
		}
		
		public void addItem(VersionItem item) {
			for(VersionItem it : items) {
				if(it.compareTo(item) < 0) {
					insertItem(item, items.indexOf(it));
					return;
				}
			}
			insertItem(item, items.size());
		}
		
		private void insertItem(VersionItem item, int index) {
			if(item == null) {
				return;
			}
			if(index < 0 || index > items.size()) {
				throw new IndexOutOfBoundsException();
			}
			items.add(index, item);
			fireItemsAdded(index, index);
		}
		
		public void removeItem(VersionItem anItem) {
			if(anItem == null) {
				return;
			}
			int idx = items.indexOf(anItem);
			if(idx == -1) {
				return;
			}
			removeItem(idx);
		}
		
		public VersionItem removeItem(int index) {
			if(index < 0 || index >= items.size()) {
				throw new IndexOutOfBoundsException();
			}
			VersionItem obj = items.remove(index);
			fireItemsRemoved(index, index);
			return obj;
		}

		protected void fireItemsAdded(int startIndex, int endIndex) {
			ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_ADDED, startIndex, endIndex);
			for (int i = listeners.size()-1; i >= 0; i--) {
				listeners.get(i).intervalAdded(event);
			}			
		}
		
		protected void fireItemsRemoved(int startIndex, int endIndex) {
			ListDataEvent event = new ListDataEvent(this, ListDataEvent.INTERVAL_REMOVED, startIndex, endIndex);
			for (int i = listeners.size()-1; i >= 0; i--) {
				listeners.get(i).intervalRemoved(event);
			}			
		}
		
		protected void fireItemsChanged(int startIndex, int endIndex) {
			ListDataEvent event = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, startIndex, endIndex);
			for (int i = listeners.size()-1; i >= 0; i--) {
				listeners.get(i).contentsChanged(event);
			}			
		}
		
	}
	
	class TempProjectLoader extends ProjectLoader {
		StringBuilder versionString;
		private final ReadManager readManager;
		
		public TempProjectLoader() {
			this.readManager = new ReadManager();
			readManager.addElementHandler("workspace", getProjectRootCreator());
			readManager.addElementHandler("project", getProjectRootCreator());
		}
		
		protected void load(final URI xmlFile) throws MalformedURLException, XMLException, IOException {
			final TreeXmlReader reader = new TreeXmlReader(readManager);
			reader.load(new InputStreamReader(new BufferedInputStream(xmlFile.toURL().openStream())));
		}
		
		public String getMetaInfo(AWorkspaceProject project) {
			try {
				versionString = new StringBuilder();
				LOAD_RETURN_TYPE retType = this.loadProject(project);
				if(LOAD_RETURN_TYPE.EXISTING_PROJECT.equals(retType)) {
					return versionString.length() == 0 ? null : versionString.toString();
				}
			} catch (IOException e) {
				LogUtils.warn(e);
			}
			return null;
		}

		@Override
		public IResultProcessor getDefaultResultProcessor() {
			return new IResultProcessor() {
				public void process(AWorkspaceTreeNode parent, AWorkspaceTreeNode node) {
					if(node == null) {
						return;
					}
					if(node instanceof ProjectRootNode) {
						versionString.append(node.getName());
					}
					if(parent == null) {
						return;
					}						
					parent.addChildNode(node);
				}

				public void setProject(AWorkspaceProject project) {
				}
			};
		}
		
		
		
	}
	
	class VersionItem implements Comparable<VersionItem> {
		
		private final AWorkspaceProject project;
		private final Date latestUse;
		private final String name;

		/***********************************************************************************
		 * CONSTRUCTORS
		 **********************************************************************************/

		public VersionItem(AWorkspaceProject prj, String name, Date version) {
			if(prj == null || name == null || version == null) {
				throw new IllegalArgumentException("NULLPointer");
			}
			this.project = prj;
			this.latestUse = version;
			this.name = name;
		}
		/***********************************************************************************
		 * METHODS
		 **********************************************************************************/
		
		public AWorkspaceProject getProject() {
			return project;
		}
		
		public String toString() {
			return this.name + " [" +format.format(latestUse) + "]"; 
		}
		
		/***********************************************************************************
		 * REQUIRED METHODS FOR INTERFACES
		 **********************************************************************************/
		
		public int compareTo(VersionItem o) {
			return (int) (latestUse.getTime()-o.latestUse.getTime());
		}
	}
	
	class ConversionItem extends VersionItem {

		private ConversionItem(DocearWorkspaceProject prj, String name, Date version) {
			super(prj, name, version);
		}
		
		public String toString() {
			return /*"[profile] " +/**/ super.toString(); 
		}
	}
}
