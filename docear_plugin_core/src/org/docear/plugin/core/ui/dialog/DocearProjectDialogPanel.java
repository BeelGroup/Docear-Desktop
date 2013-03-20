package org.docear.plugin.core.ui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.MatteBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.dialog.NewProjectDialogPanel;
import org.swingplus.JHyperlink;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class DocearProjectDialogPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private JTextField txtProjectName;
	private JTextField txtProjectPath;
	protected boolean manualChoice = false;
	private JTextField txtBibfile;
	private JList repositoryPathList;
	private boolean useDefaults = true;
	private JScrollPane reposiScrollPane;
	private JButton btnAddPath;
	private JButton btnRemovePath;
	private JButton btnBrowseBib;
	private JLabel lblBibPath;
	private JLabel lblLiteratureRepository;
	private List<RepositoryListItem> repositoryItems = new ArrayList<RepositoryListItem>();
	private RepositoryListModel repoModel = new RepositoryListModel();
	private boolean useDefaultRepository = true;
	private List<URI> parsedList;
	private boolean isDirty = true;
	private JCheckBox chckbxIncludeDemoFiles;	
	
	public DocearProjectDialogPanel() {
		setPreferredSize(new Dimension(480, 400));
		setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("max(100dlu;min):grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("top:max(40dlu;pref)"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default"),
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("top:default:grow"),
				FormFactory.RELATED_GAP_ROWSPEC,}));
		
		JPanel panel = new JPanel();
		panel.setBorder(new MatteBorder(0, 0, 1, 0, (Color) new Color(0, 0, 0)));
		panel.setBackground(Color.WHITE);
		add(panel, "1, 1, 8, 2, fill, fill");
		panel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				RowSpec.decode("fill:default:grow"),}));
		
		JLabel lblNewLabel = new JLabel(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".help"));
		lblNewLabel.setVerticalAlignment(SwingConstants.TOP);
		panel.add(lblNewLabel, "2, 2");
		
		JLabel lblProjectName = new JLabel(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".name.label"));
		lblProjectName.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblProjectName, "2, 4, right, default");
		
		txtProjectName = new JTextField();
		txtProjectName.setText(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".name.default"));
		add(txtProjectName, "4, 4, fill, default");
		txtProjectName.setColumns(10);
		txtProjectName.addKeyListener(new KeyListener() {			
			public void keyTyped(KeyEvent arg0) {
			}
			
			public void keyReleased(KeyEvent arg0) {
				if(!manualChoice) {
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							setProjectPath(getDefaultProjectPath(getProjectName()));
						}
					});
				}
			}
			
			public void keyPressed(KeyEvent arg0) {	
			}
		});
		
		JLabel lblProjectPath = new JLabel(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".path.label"));
		lblProjectPath.setHorizontalAlignment(SwingConstants.RIGHT);
		add(lblProjectPath, "2, 6, right, default");
		
		txtProjectPath = new JTextField(getDefaultProjectPath(txtProjectName.getText()));
		setProjectPath(getDefaultProjectPath(getProjectName()));
		add(txtProjectPath, "4, 6, fill, default");
		txtProjectPath.setColumns(10);
		
		JButton btnBrowse = new JButton(TextUtils.getText("browse"));
		btnBrowse.setToolTipText(TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".button.tip"));
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File home = URIUtils.getAbsoluteFile(getProjectPath());
				while(home != null && !home.exists()) {
					home = home.getParentFile();
				}
				JFileChooser chooser = new JFileChooser(home == null ? getDefaultProjectPath(getProjectName()) : home.getAbsolutePath());
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setMultiSelectionEnabled(false);
				chooser.setFileHidingEnabled(true);
				int response = chooser.showOpenDialog(DocearProjectDialogPanel.this);
				if(response == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					setProjectPath(file.getAbsolutePath());
					manualChoice = true;
				}
			}
		});
		add(btnBrowse, "6, 6");
		
		JSeparator separator = new JSeparator();
		add(separator, "2, 8, 6, 1");
		
		chckbxIncludeDemoFiles = new JCheckBox("include demo files");
		add(chckbxIncludeDemoFiles, "2, 10, 5, 1");
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "  "+TextUtils.getText("advanced_project_settings_title")+"  ", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		add(panel_1, "1, 11, 8, 2, fill, fill");
		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				ColumnSpec.decode("default:grow"),
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
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
				RowSpec.decode("top:default:grow"),}));
		
		final JCheckBox chckbxUseDefaults = new JCheckBox(TextUtils.getText("library_path_use_defaults"), useDefaults());
		chckbxUseDefaults.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (chckbxUseDefaults.isSelected()) {
					setUseDefaults(true);
				} else {
					setUseDefaults(false);
				}
			}
		});
		panel_1.add(chckbxUseDefaults, "1, 2");
		
		JLabel lblAdvancedInfo = new JHyperlink(TextUtils.getText("library_paths_help"), TextUtils.getText("library_paths_help_uri"));
		panel_1.add(lblAdvancedInfo, "3, 2, 3, 1");
		
		lblBibPath = new JLabel("BibTeX Library:");
		panel_1.add(lblBibPath, "1, 4, right, default");
		
		txtBibfile = new JTextField();
		txtBibfile.setText("default.bib");
		panel_1.add(txtBibfile, "3, 4, fill, default");
		txtBibfile.setColumns(10);
		
		btnBrowseBib = new JButton(TextUtils.getText("browse"));
		btnBrowseBib.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File home = URIUtils.getAbsoluteFile(getProjectPath());
				while(home != null && !home.exists()) {
					home = home.getParentFile();
				}
				JFileChooser chooser = new JFileChooser(home == null ? getDefaultProjectPath(getProjectName()) : home.getAbsolutePath());
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
				int response = chooser.showOpenDialog(DocearProjectDialogPanel.this);
				if(response == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					setBibTeXPath(file.getAbsolutePath());
				}
			}
		});
		panel_1.add(btnBrowseBib, "5, 4");
		
		JLabel lblMendeleyInfo = new JHyperlink(TextUtils.getText("bibtex_mendeley_help"), TextUtils.getText("bibtex_mendeley_help_uri"));
		panel_1.add(lblMendeleyInfo, "3, 6, 3, 1");
		
		lblLiteratureRepository = new JLabel("Literature Repository");
		panel_1.add(lblLiteratureRepository, "1, 8, 5, 1");
		
		reposiScrollPane = new JScrollPane();
		panel_1.add(reposiScrollPane, "1, 10, 3, 3, fill, fill");
		
		repositoryPathList = new JList();
		reposiScrollPane.setViewportView(repositoryPathList);
		repositoryPathList.setModel(repoModel);
		repoModel.addItem(new DefaultRepositoryListItem());
		
		btnAddPath = new JButton("Add Path");
		btnAddPath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File home = URIUtils.getAbsoluteFile(getProjectPath());
				while(home != null && !home.exists()) {
					home = home.getParentFile();
				}
				JFileChooser chooser = new JFileChooser(home == null ? getDefaultProjectPath(getProjectName()) : home.getAbsolutePath());
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
				int response = chooser.showOpenDialog(DocearProjectDialogPanel.this);
				if(response == JFileChooser.APPROVE_OPTION) {
					File file = chooser.getSelectedFile();
					repoModel.addItem(new RepositoryListItem(file.getAbsolutePath()));
				}
			}
		});
		panel_1.add(btnAddPath, "5, 10");
		
		btnRemovePath = new JButton("Remove Path");
		btnRemovePath.addActionListener(new ActionListener() {
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
		panel_1.add(btnRemovePath, "5, 12");
		
		setUseDefaults(true);
	}
	
	private void setControlsEnabled(boolean b) {
		txtBibfile.setEnabled(b);
		reposiScrollPane.setEnabled(b);
		repositoryPathList.setEnabled(b);
		btnAddPath.setEnabled(b);
		btnRemovePath.setEnabled(b);
		btnBrowseBib.setEnabled(b);
		lblBibPath.setEnabled(b);
		lblLiteratureRepository.setEnabled(b);
	}
	
	public boolean includeDemoFiles() {
		return chckbxIncludeDemoFiles.isSelected();
	}
	
	private void setBibTeXPath(String path) {
		isDirty = true;
		txtBibfile.setText(path);
	}
	
	public URI getBibTeXPath() {
		if("default.bib".equals(txtBibfile.getText())) {
			return null;
		}
		try {			
			return new File(txtBibfile.getText()).toURI();
		}
		catch (Exception e) {
		}
		return null;
	}
	
	public boolean useDefaults() {
		return useDefaults;
	}

	public void setUseDefaults(boolean enabled) {
		useDefaults = enabled;
		setControlsEnabled(!useDefaults);
	}
	
	private void setProjectPath(String path) {
		txtProjectPath.setText(path);
	}

	private String getDefaultProjectPath(String projectName) {
		File base = URIUtils.getAbsoluteFile(WorkspaceController.getDefaultProjectHome());
		if(projectName == null) {
			projectName = TextUtils.getText(NewProjectDialogPanel.class.getSimpleName().toLowerCase(Locale.ENGLISH)+".name.default");
		}
		File path = new File(base, projectName.trim());		
		int counter = 1;
		while(path.exists() && projectName.trim().length() > 0) {
			path = new File(base, projectName.trim()+" "+(counter++));
		}		
		return path.getAbsolutePath();
	}

	public String getProjectName() {
		return txtProjectName.getText().trim();
	}
	
	public URI getProjectPath() {
		return new File(txtProjectPath.getText()).toURI();
	}
	
	private List<RepositoryListItem> getRepositoryItems() {
		return repositoryItems;
	}
	
	public List<URI> getRepositoryPathList() {
		parseRepositoryItemList();
		return parsedList;
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
	
	public boolean useDefaultRepositoryPath() {
		parseRepositoryItemList();
		return useDefaultRepository ;
	}
	
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
			return "[project default repository]";			
		}
		
	}
}
