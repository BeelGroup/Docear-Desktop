package org.docear.plugin.core.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.ui.components.ComponentGroup;
import org.docear.plugin.core.ui.wizard.AWizardPage;
import org.docear.plugin.core.ui.wizard.WizardSession;
import org.docear.plugin.core.workspace.actions.DocearProjectSettings;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.dialog.NewProjectDialogPanel;
import org.freeplane.plugin.workspace.model.project.AWorkspaceProject;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class CreateProjectPagePanel extends AWizardPage {

	private static final long serialVersionUID = 1L;
	private JLabel lblWarning;
	private JTextField txtProjectName;
	private JTextField txtProjectHome;
	private JTextField txtProjectName_1;
	private JTextField txtProjectHome_1;
	private JTextField txtBibFile;
	private WizardSession cachedContext;
	private JRadioButton rdbtnStartFromScratch;
	private JRadioButton rdbtnUseExisting;
	private JRadioButton rdbtnImportMendeley;
	private ComponentGroup optionGroup1 = new ComponentGroup();
	private ComponentGroup optionGroup2 = new ComponentGroup();
	private ComponentGroup option3 = new ComponentGroup();
	
	private boolean byHandPath = false;
	private boolean byHandBib = false;
	private boolean isDirty = true;
	private boolean useDefaultRepository = true;
	
	private RepositoryListModel repoModel = new RepositoryListModel();
	
	private List<RepositoryListItem> repositoryItems = new ArrayList<RepositoryListItem>();
	private List<URI> parsedList;
	private JCheckBox chckbxIncludeDemoFiles;
	private File defaultPath;
	private MultiLineActionLabel lblInfoWhenCreateAProject;

	/***********************************************************************************
	 * CONSTRUCTORS
	 * @param settings 
	 **********************************************************************************/

	public CreateProjectPagePanel() {
		setBackground(Color.WHITE);
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
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
				FormFactory.DEFAULT_ROWSPEC,
				RowSpec.decode("fill:default"),
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
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		
		
		lblInfoWhenCreateAProject = new MultiLineActionLabel(TextUtils.getText("docear.setup.wizard.create.info"));
		Font f = lblInfoWhenCreateAProject.getFont().deriveFont(Font.BOLD);
		lblInfoWhenCreateAProject.setFont(f);
		lblInfoWhenCreateAProject.setForeground(Color.RED);
		lblInfoWhenCreateAProject.setBackground(Color.WHITE);
		lblInfoWhenCreateAProject.setVisible(!WorkspaceController.getCurrentModel().getProjects().isEmpty());
		lblInfoWhenCreateAProject.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if("project_read_more".equals(e.getActionCommand())) {
					try {
						Controller.getCurrentController().getViewController().openDocument(URI.create("http://www.docear.org/faqs/what-is-a-project/"));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		add(lblInfoWhenCreateAProject, "4, 2, 7, 1");
		
		/***********
		 * option 1
		 */
		
		
		
		lblWarning = new JLabel(TextUtils.getText("docear.setup.wizard.create.warn1"));
		URL url = WorkspaceController.class.getResource("/images/16x16/dialog-warning-4.png");
		if(url != null) {
			lblWarning.setIcon(new ImageIcon(url));
		}
		lblWarning.setVisible(false);
		add(lblWarning, "2, 4, 9, 1");
		
		rdbtnStartFromScratch = new JRadioButton(TextUtils.getText("docear.setup.wizard.create.option.title.1"));
		rdbtnStartFromScratch.setSelected(true);
		rdbtnStartFromScratch.setBackground(Color.WHITE);
		rdbtnStartFromScratch.setFont(rdbtnStartFromScratch.getFont().deriveFont(Font.BOLD, 11));
		rdbtnStartFromScratch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateFields();
			}
		});
		
		add(rdbtnStartFromScratch, "2, 6, 9, 1");
		
		JLabel lblProjectName = new JLabel(TextUtils.getText("docear.setup.wizard.create.name.label"));
		add(lblProjectName, "4, 8, right, default");
		
		computeDefaultProjectPath(null);
		txtProjectName = new JTextField();
		txtProjectName.setText(defaultPath.getName());
		txtProjectName.setColumns(10);
		txtProjectName.addKeyListener(getProjectNameListener());
		add(txtProjectName, "6, 8, fill, default");
		
		
		JLabel lblProjectHome = new JLabel(TextUtils.getText("docear.setup.wizard.create.home.label"));
		add(lblProjectHome, "4, 10, right, default");
		
		txtProjectHome = new JTextField();
		txtProjectHome.setText(defaultPath.getAbsolutePath());
		txtProjectHome.setColumns(10);
		txtProjectHome.addKeyListener(getProjectHomeListener());
		add(txtProjectHome, "6, 10, fill, default");
		
		
		JButton btnBrowse = new JButton(TextUtils.getText("docear.setup.wizard.create.browse.label"));
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showProjectHomeChooser();
			}
		});
		add(btnBrowse, "8, 10");
		
		optionGroup1.add(lblProjectHome);
		optionGroup1.add(lblProjectName);
		optionGroup1.add(txtProjectHome);
		optionGroup1.add(txtProjectName);
		optionGroup1.add(btnBrowse);
		
		/***********
		 * option 2
		 */
		
		rdbtnUseExisting = new JRadioButton(TextUtils.getText("docear.setup.wizard.create.option.title.2"));
		rdbtnUseExisting.setBackground(Color.WHITE);
		rdbtnUseExisting.setFont(rdbtnUseExisting.getFont().deriveFont(Font.BOLD, 11));
		rdbtnUseExisting.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				updateFields();
			}
		});
		add(rdbtnUseExisting, "2, 12, 9, 1");
		
		final JEditorPane dtrpnInfoText = new JEditorPane() {
			private static final long serialVersionUID = 1L;

			@Override
			public void setEnabled(boolean enabled) {
				if(enabled) {
					setForeground(UIManager.getColor("EditorPane.foreground"));
				}
				else {
					Color col = UIManager.getColor("EditorPane.disabledText");
					if(col == null) {
						col = UIManager.getColor("EditorPane.inactiveForeground");
					}
					setForeground(col);
				}
			}
		}/**/;
		dtrpnInfoText.setEditable(false);
		dtrpnInfoText.setBackground(Color.WHITE);
		dtrpnInfoText.setText(TextUtils.getText("docear.setup.wizard.create.option.info"));
		add(dtrpnInfoText, "4, 13, 7, 1, fill, fill");
		
		final MultiLineActionLabel malReadInfo = new MultiLineActionLabel(TextUtils.getText("docear.setup.wizard.create.option.read"));
		malReadInfo.setBorder(new EmptyBorder(0, 2, 0, 0));
		malReadInfo.setBackground(Color.WHITE);
		malReadInfo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					if("mendeley".equals(e.getActionCommand())) {
							Controller.getCurrentController().getViewController().openDocument(URI.create("http://www.docear.org/faqs/how-to-use-zotero-and-docear-at-the-same-time/"));
					}
					else if("zotero".equals(e.getActionCommand())) {
						Controller.getCurrentController().getViewController().openDocument(URI.create("http://www.docear.org/faqs/how-to-use-zotero-and-docear-at-the-same-time/"));
					}
				} catch (IOException ex) {
					LogUtils.warn("org.docear.plugin.core.ui.CreateProjectPagePanel.CreateProjectPagePanel()$ActionListener.actionPerformed(ActionEvent): "+ ex.getMessage());
				}
				
			}
		});
		add(malReadInfo, "4, 14, 7, 1, fill, top");
		
		final JLabel lblProjectName_1 = new JLabel(TextUtils.getText("docear.setup.wizard.create.name.label"));
		add(lblProjectName_1, "4, 16, right, top");
		
		computeDefaultProjectPath(null);
		
		txtProjectName_1 = new JTextField();
		txtProjectName_1.setText(defaultPath.getName());
		txtProjectName_1.setColumns(10);
		txtProjectName_1.addKeyListener(getProjectNameListener());
		add(txtProjectName_1, "6, 16, fill, default");
				
		final JLabel lblProjectHome_1 = new JLabel(TextUtils.getText("docear.setup.wizard.create.home.label"));
		add(lblProjectHome_1, "4, 18, right, top");
		
		txtProjectHome_1 = new JTextField();
		txtProjectHome_1.setText(defaultPath.getAbsolutePath());
		txtProjectHome_1.setColumns(10);
		txtProjectHome_1.addKeyListener(getProjectHomeListener());
		add(txtProjectHome_1, "6, 18, fill, default");
		
		
		final JButton btnBrowse_2 = new JButton(TextUtils.getText("docear.setup.wizard.create.browse.label"));
		btnBrowse_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showProjectHomeChooser();
			}
		});
		add(btnBrowse_2, "8, 18");
		
		final JLabel lblBibtexFile = new JLabel(TextUtils.getText("docear.setup.wizard.create.bibtex.label"));
		add(lblBibtexFile, "4, 20, right, top");
		
		txtBibFile = new JTextField();
		txtBibFile.setText(getProjectName()+".bib");
		txtBibFile.setColumns(10);
		txtBibFile.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {
				if(NewProjectDialogPanel.isBlackListed(e.getKeyChar())) {
					e.consume();
				}
				else {
					checkBibTeXWarning();
				}
			}
			
			@Override
			public void keyReleased(KeyEvent e) {
				if(NewProjectDialogPanel.isBlackListed(e.getKeyChar())) {
					e.consume();
				}
				else {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							setBibTeXPath(txtBibFile.getText());
							checkBibTeXWarning();
						}
					});
				}
				byHandBib = true;
			}
			
			@Override
			public void keyPressed(KeyEvent e) {
				if(NewProjectDialogPanel.isBlackListed(e.getKeyChar())) {
					e.consume();
				}
				else {
					checkBibTeXWarning();
				}
			}
		});
		add(txtBibFile, "6, 20, fill, default");
		
		final JButton btnBrowseBib = new JButton(TextUtils.getText("docear.setup.wizard.create.browse.label"));
		btnBrowseBib.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File home = URIUtils.getAbsoluteFile(getProjectHome());
				while(home != null && !home.exists()) {
					home = home.getParentFile();
				}
				computeDefaultProjectPath(getProjectName());				
				JFileChooser chooser = new JFileChooser(home == null ? defaultPath.getAbsolutePath() : home.getAbsolutePath());
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileHidingEnabled(true);
				chooser.addChoosableFileFilter(new FileFilter() {
					public String getDescription() {
						return "*.bib (" + TextUtils.getText("locationdialog.filefilter.bib") + ")";
					}

					public boolean accept(File f) {
						return (f.isDirectory() || f.getName().endsWith(".bib"));
					}
				});
				
				int response = chooser.showOpenDialog(getRootPane());
				if(response == JFileChooser.APPROVE_OPTION) {
					byHandBib = true;
					File file = chooser.getSelectedFile();
					setBibTeXPath(file.getAbsolutePath());
					checkBibTeXWarning();
				}
			}
		});
		add(btnBrowseBib, "8, 20");
		
		JLabel lblPdfsLiterature = new JLabel(TextUtils.getText("docear.setup.wizard.create.literature.label"));
		add(lblPdfsLiterature, "4, 22, right, top");
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, "6, 22, 1, 5, fill, fill");
		
		final JList repositoryPathList = new JList();
		repositoryPathList.setModel(repoModel);
		repoModel.addItem(new DefaultRepositoryListItem());
		scrollPane.setViewportView(repositoryPathList);
		
		final JButton btnAdd = new JButton(TextUtils.getText("docear.setup.wizard.create.add.label"));
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File home = URIUtils.getAbsoluteFile(getProjectHome());
				while(home != null && !home.exists()) {
					home = home.getParentFile();
				}
				computeDefaultProjectPath(getProjectName());
				JFileChooser chooser = new JFileChooser(home == null ? defaultPath.getAbsolutePath() : home.getAbsolutePath());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileHidingEnabled(true);
				chooser.setFileFilter(new FileFilter() {					
					@Override
					public boolean accept(File file) {
						for(RepositoryListItem item : getRepositoryItems()) {
							if(file.toURI().getPath().startsWith(item.getPathURI().getPath())) {
								return false;
							}
						}
						if(file.isDirectory()) {
							return true;
						}
						return false;
					}

					@Override
					public String getDescription() {
						return "";
					}
				});
				int response = chooser.showOpenDialog(getRootPane());
				if(response == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					repoModel.addItem(new RepositoryListItem(file.getAbsolutePath()));
				}
			}
		});
		add(btnAdd, "8, 22");
		
		final JButton btnRemove = new JButton(TextUtils.getText("docear.setup.wizard.create.remove.label"));
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int[] indices = repositoryPathList.getSelectedIndices();
				//DOCEAR - todo: question dialog
				if(true) {
					for (int i=indices.length-1; i >= 0 ; i--) {
						repoModel.removeItem(indices[i]);
					}
				}
			}
		});
		add(btnRemove, "8, 24");
		
		optionGroup2.add(lblProjectHome_1);
		optionGroup2.add(lblProjectName_1);
		optionGroup2.add(lblBibtexFile);
		optionGroup2.add(lblPdfsLiterature);
		optionGroup2.add(txtBibFile);
		optionGroup2.add(repositoryPathList);
		optionGroup2.add(scrollPane);
		optionGroup2.add(btnBrowseBib);
		optionGroup2.add(btnBrowse_2);
		optionGroup2.add(btnAdd);
		optionGroup2.add(btnRemove);
		optionGroup2.add(txtProjectHome_1);
		optionGroup2.add(txtProjectName_1);
		optionGroup2.add(malReadInfo);
		optionGroup2.add(dtrpnInfoText);
		
		/***********
		 * option 3
		 */
		
		rdbtnImportMendeley = new JRadioButton(TextUtils.getText("docear.setup.wizard.create.option.title.3"));
		rdbtnImportMendeley.setFont(rdbtnImportMendeley.getFont().deriveFont(Font.BOLD, 11));
		rdbtnImportMendeley.setEnabled(false);
		rdbtnImportMendeley.setBackground(Color.WHITE);
		rdbtnImportMendeley.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateFields();
			}
		});
		//add(rdbtnImportMendeley, "2, 26, 9, 1");
		
		chckbxIncludeDemoFiles = new JCheckBox(TextUtils.getText("docear.setup.wizard.create.demo.label"));
		chckbxIncludeDemoFiles.setSelected(true);
		chckbxIncludeDemoFiles.setBackground(Color.WHITE);
		add(chckbxIncludeDemoFiles, "2, 30, 9, 1");
		
		ButtonGroup group = new ButtonGroup();
		group.add(rdbtnImportMendeley);
		group.add(rdbtnStartFromScratch);
		group.add(rdbtnUseExisting);
		
		rdbtnStartFromScratch.setSelected(true);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	private void computeDefaultProjectPath(String projectName) {
		File base = URIUtils.getAbsoluteFile(WorkspaceController.getDefaultProjectHome());
		if(projectName == null) {
			projectName = TextUtils.getText("docear.setup.wizard.create.name.default");
		}
		File path = new File(base, projectName.trim());		
		int counter = 1;
		while(path.exists() && projectName.trim().length() > 0) {
			path = new File(base, projectName.trim()+" "+(counter++));
		}		
		this.defaultPath = path;
	}
	
	
	
	private void updateFields() {
		if(isStartFromScratch()) {
			optionGroup1.setEnabled(true);
			optionGroup2.setEnabled(false);
			option3.setEnabled(false);
		}
		else if(isUseExistingParts()) {
			optionGroup1.setEnabled(false);
			optionGroup2.setEnabled(true);
			option3.setEnabled(false);
		}
		else if(isImportMendeley()) {
			optionGroup1.setEnabled(false);
			optionGroup2.setEnabled(false);
			option3.setEnabled(true);
		}
	}
	
	private void enableControls(WizardSession context) {
		if(context != null) {
			if(nameExistsInWorkspace(getProjectName())) {
				lblWarning.setText(TextUtils.getText("docear.setup.wizard.create.warn1"));
				lblWarning.setVisible(true);
				context.getNextButton().setEnabled(false);					
			}
			else if(pathExistsInWorkspace(getProjectHomePath())) {
				lblWarning.setText(TextUtils.getText("docear.setup.wizard.create.warn2"));
				lblWarning.setVisible(true);
				context.getNextButton().setEnabled(false);
			}
			else {
				context.getNextButton().setEnabled(true);
				lblWarning.setVisible(false);
			}	
		}
	}
	
	private KeyListener getProjectNameListener() {
		return new KeyListener() {			
			public void keyTyped(KeyEvent evt) {
				if(NewProjectDialogPanel.isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
				enableControls(cachedContext);
			}
			
			public void keyReleased(KeyEvent evt) {
				if(NewProjectDialogPanel.isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
				else {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							setProjectName(getProjectName());
						}
					});
					if(!byHandPath) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								computeDefaultProjectPath(getProjectName());
								setProjectName(defaultPath.getName());
								setProjectHome(defaultPath.getAbsolutePath());
							}
						});
					}
					if(!byHandBib) {
						SwingUtilities.invokeLater(new Runnable() {
							public void run() {
								setBibTeXPath(getProjectName()+".bib");
							}
						});
					}
				}
				enableControls(cachedContext);
			}
			
			public void keyPressed(KeyEvent evt) {
				if(NewProjectDialogPanel.isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
				enableControls(cachedContext);
			}
		};
	}
	
	private KeyListener getProjectHomeListener() {
		return new KeyListener() {			
			public void keyTyped(KeyEvent evt) {
				if(NewProjectDialogPanel.isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
				else {
					byHandPath = true;
				}
				enableControls(cachedContext);
			}
			
			public void keyReleased(KeyEvent evt) {
				if(NewProjectDialogPanel.isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
				else {
					byHandPath = true;
				}
				enableControls(cachedContext);
			}
			
			public void keyPressed(KeyEvent evt) {
				if(NewProjectDialogPanel.isBlackListed(evt.getKeyChar())) {
					evt.consume();
				}
				else {
					byHandPath = true;
				}
				enableControls(cachedContext);
			}
		};
	}
	
	private void showProjectHomeChooser() {
		File home = URIUtils.getAbsoluteFile(getProjectHome());
		while(home != null && !home.exists()) {
			home = home.getParentFile();
		}
		computeDefaultProjectPath(getProjectName());
		JFileChooser chooser = new JFileChooser(home == null ? defaultPath.getAbsolutePath() : home.getAbsolutePath());
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileHidingEnabled(true);
		int response = chooser.showOpenDialog(getRootPane());
		if(response == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			setProjectHome(file.getAbsolutePath());
			byHandPath = true;
		}
	}
	
	private void setProjectName(String name) {
		if(isUseExistingParts()) {
			txtProjectName.setText(name);
		}
		else if(isStartFromScratch()) {
			txtProjectName_1.setText(name);
		}
	}
	
	private void setProjectHome(String path) {
		txtProjectHome.setText(path);
		txtProjectHome_1.setText(path);
	}
	
	private void setBibTeXPath(String path) {
		isDirty = true;
		txtBibFile.setText(path);
	}
	
	private void checkBibTeXWarning() {
		enableControls(cachedContext);
		if(getBibTeXPath() != null) {
			File base = URIUtils.getFile(getProjectHome());
			File bib = URIUtils.getFile(getBibTeXPath());
			URI relativeURI = org.freeplane.features.link.LinkController.toRelativeURI(base, bib, LinkController.LINK_RELATIVE_TO_MINDMAP);
			if(relativeURI != null && relativeURI.getPath().startsWith("..")) {
				lblWarning.setText(TextUtils.getText("docear.setup.wizard.create.warn3"));
				lblWarning.setVisible(true);
			}
		}
	}
	
	private boolean nameExistsInWorkspace(String name) {
		if(name == null) {
			return true;
		}
		for(AWorkspaceProject project : WorkspaceController.getCurrentModel().getProjects()) {
			try {
				if(project.getProjectName().equals(name)) {
					return true;
				}
			} 
			catch (Exception e) {
				LogUtils.info(""+e.getMessage());
			}
		}
		return false;
	}
	
	private boolean pathExistsInWorkspace(String path) {
		if(path == null) {
			return true;
		}
		for(AWorkspaceProject project : WorkspaceController.getCurrentModel().getProjects()) {
			try {
				if(URIUtils.getFile(project.getProjectHome()).getAbsolutePath().equals(new File(path).getAbsolutePath())) {
					return true;
				}
			} 
			catch (Exception e) {
				LogUtils.info(""+e.getMessage());
			}
		}
		return false;
	}
	
	private List<RepositoryListItem> getRepositoryItems() {
		return repositoryItems;
	}
	
	private List<URI> parseRepositoryItemList() {
		if(isDirty) {
			useDefaultRepository = false;		
			parsedList = new ArrayList<URI>();
			for (RepositoryListItem item : repositoryItems) {
				if(item instanceof DefaultRepositoryListItem) {
					useDefaultRepository = true;
					continue;
				}
				parsedList.add(item.getPathURI());
			}
		}
		return parsedList;
	}
	
	public List<URI> getRepositoryPathList() {
		parseRepositoryItemList();
		return parsedList;
	}
	
	public boolean useDefaultRepositoryPath() {
		parseRepositoryItemList();
		return useDefaultRepository ;
	}
	
	public boolean isStartFromScratch() {
		return rdbtnStartFromScratch.isSelected();
	}
	
	public boolean isUseExistingParts() {
		return rdbtnUseExisting.isSelected();
	}
	
	public boolean isImportMendeley() {
		return rdbtnImportMendeley.isSelected();
	}

	public String getProjectName() {
		if(isStartFromScratch()) {
			return txtProjectName.getText().trim();
		}
		else if(isUseExistingParts()) {
			return txtProjectName_1.getText().trim();
		}
		
		return null;
		
	}
	
	public URI getProjectHome() {
		return new File(getProjectHomePath()).toURI();
	}
	
	public String getProjectHomePath() {
		if(isStartFromScratch()) {
			return txtProjectHome.getText().trim();
		}
		else if(isUseExistingParts()) {
			return txtProjectHome_1.getText().trim();
		}
		
		return null;
	}
	
	public URI getBibTeXPath() {
		if(txtBibFile.getText().trim().startsWith(getProjectName())) {
			return null;
		}
		try {
			File file = new File(txtBibFile.getText());
			if(!file.isAbsolute()) {
				file = new File(getProjectHomePath(), txtBibFile.getText());
			}
			return file.toURI();
		}
		catch (Exception e) {
		}
		return null;
	}
	
	public boolean includeDemoFiles() {
		return chckbxIncludeDemoFiles.isSelected();
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public String getTitle() {
		//TODO Service
		if(DocearController.getController().isServiceAvailable()){
			return TextUtils.getText("docear.setup.wizard.create.title");
		}
		else{
			return TextUtils.getText("docear.setup.wizard.create.title.noservice");
		}
	}

	@Override
	public void preparePage(WizardSession context) {
		this.cachedContext = context;
		context.setWizardTitle(getTitle());
		context.getNextButton().setText(TextUtils.getText("docear.setup.wizard.controls.finish"));
		updateFields();
		enableControls(context);
	}

	
	/***********************************************************************************
	 * NESTED TYPES
	 **********************************************************************************/
	
	class RepositoryListModel extends AbstractListModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getSize() {
			return getRepositoryItems().size();
		}

		public void removeItem(int index) {
			getRepositoryItems().remove(index);
			fireIntervalRemoved(this, index, index);
			isDirty = true;
		}
		
		public void addItem(RepositoryListItem item) {
			int index = getRepositoryItems().size();
			getRepositoryItems().add(item);
			fireIntervalAdded(this, index, index);
			isDirty = true;
		}
		
		public void insertItem(RepositoryListItem item, int index) {
			getRepositoryItems().add(index, item);
			fireIntervalAdded(this, index, index);
			isDirty = true;
		}

		@Override
		public Object getElementAt(int index) {
			return getRepositoryItems().get(index);
		}
		
	}

	public class RepositoryListItem {
		protected File file;
		
		public RepositoryListItem(String filePath) {
			if(filePath == null) {
				throw new IllegalArgumentException("NULL");
			}			
			file = new File(filePath);
		}

		public String toString() {
			return this.file.getPath(); 
		}
		
		public URI getPathURI() {
			if(file == null) {
				return null;
			}
			return file.toURI();
		}
	}
	
	private class DefaultRepositoryListItem extends RepositoryListItem {

		public DefaultRepositoryListItem() {
			super("");
		}
		
		public String toString() {
			return /*"[default repository] "+*/new File(URIUtils.getFile(getProjectHome()), "literature_repository"+File.separator).getPath();			
		}
		
	}

	public DocearWorkspaceProject getProject() {
		AWorkspaceProject project = AWorkspaceProject.create(null, getProjectHome());
		DocearProjectSettings settings = new DocearProjectSettings();
		settings.includeDemoFiles(includeDemoFiles());
		settings.setProjectName(getProjectName());
		if(isUseExistingParts()) {
			settings.setBibTeXLibraryPath(getBibTeXPath());
			settings.setUseDefaultRepositoryPath(useDefaultRepositoryPath());
			for(URI uri : getRepositoryPathList()) {
				settings.addRepositoryPathURI(uri);
			}
		}
		project.addExtension(settings);
		return (DocearWorkspaceProject) project;
	}
}
