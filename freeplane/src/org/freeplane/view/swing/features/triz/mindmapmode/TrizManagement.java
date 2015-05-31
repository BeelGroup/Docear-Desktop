package org.freeplane.view.swing.features.triz.mindmapmode;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextPane;

import org.freeplane.core.ui.components.calendar.JCalendar;
import org.freeplane.core.ui.components.calendar.JTripleCalendar;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.IMapSelectionListener;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.MapChangeEvent;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.script.IScriptEditorStarter;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.view.swing.map.MapViewController;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.FormLayout;

public class TrizManagement implements PropertyChangeListener, IMapSelectionListener{

	class JTrizPanel extends JPanel {
	        private static final long serialVersionUID = 1L;
			private JButton applyButton;
			private JButton addToMap = new JButton(TextUtils.getText("plugins/TrizManagement.xml_AddtoMap"));
			JComboBox worseningBox = new JComboBox();
			JComboBox improvingBox = new JComboBox();
			JTextPane Principles;

			public JTrizPanel() {
		        super();
		        loadTrizViewInfo();
		        
		        init();
		        final NodeModel selected = trizHook.getModeController().getMapController().getSelectedNode();
		        update(selected);
	        }
			
			Map<Integer,String> FactorViewInfos = new HashMap<Integer,String>();
			private void loadTrizViewInfo(){
				Map<Integer,String> factors = trizHook.getBaseTRIZ().FactorDefs();
				//query localization definition
				String factor;
				for(Object key:factors.keySet()){
					factor = TextUtils.getSafeRawText("plugins/TrizManagement.xml_Factor"+key.toString());
					if(factor == null){
					    FactorViewInfos.put(Integer.parseInt(key.toString()), factors.get(key));
					}else{
						FactorViewInfos.put(Integer.parseInt(key.toString()), factor);
					}
				}
			}
			
			private String findRuleKey(String display){
				for(Object key: FactorViewInfos.keySet()){
					if(display.equalsIgnoreCase(FactorViewInfos.get(key).toString())){
						return key.toString();
					}
				}
				return "";
			}
			
			public void update(NodeModel node){
				if(node == null)
					return;
				final TrizExtension triz = TrizExtension.getExtension(node);
				final boolean trizIsSet = triz != null;
				if(trizIsSet){//triz is set
                    String worsen_feature_disaply = FactorViewInfos.get(triz.getWorseingFeatures());
					String improve_feature_display = FactorViewInfos.get(triz.getImprovingFeatures());
					
					worseningBox.setSelectedItem(worsen_feature_disaply);
					improvingBox.setSelectedItem(improve_feature_display);
					
                    ArrayList<Integer> contradictions = trizHook.getBaseTRIZ().getContradictionByKey(
                    		triz.getImprovingFeatures()+"."+triz.getWorseingFeatures());
					
					StringBuffer sb = new StringBuffer();
					for(Integer c: contradictions){
						sb.append(c);
						sb.append(": ");
						sb.append(trizHook.getBaseTRIZ().getRuleByKey(c));
						sb.append("\n");
					}
					Principles.setText(sb.toString());
					
				}else{
					worseningBox.setSelectedItem("");
					improvingBox.setSelectedItem("");
					Principles.setText("");
				}
			}

			
			private void init() {
				DefaultFormBuilder btnBuilder = new DefaultFormBuilder(new FormLayout(FormFactory.GROWING_BUTTON_COLSPEC.toString(), ""));
				setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
				add(Box.createHorizontalGlue());
				
				applyButton = new JButton(TextUtils.getText("plugins/TrizManagement.xml_ApplyButtion"));
				applyButton.setToolTipText(TextUtils.getText("plugins/TrizManagement.xml_ApplyButtion_tip"));
				applyButton.addMouseListener(new MouseAdapter() {
					@Override
	                public void mouseClicked(MouseEvent e) {
						addTRIZ();
					}
				});
				
				JLabel worseningLabel = new JLabel(TextUtils.getText("plugins/TrizManagement.xml_WorsenKey"));
				JLabel improvingLabel = new JLabel(TextUtils.getText("plugins/TrizManagement.xml_ImproveKey"));
				//worseningBox.setEditable(true);
				//improvingBox.setEditable(true);
				Iterator<?> iterator = FactorViewInfos.values().iterator();
				String item;
				worseningBox.addItem("");
				improvingBox.addItem("");
				while(iterator.hasNext()) {
					item = (String) iterator.next();
					worseningBox.addItem(item);
					improvingBox.addItem(item);
		        }		
				
				btnBuilder.append(improvingLabel);
				btnBuilder.append(improvingBox);
				
				btnBuilder.append(worseningLabel);
				btnBuilder.append(worseningBox);
				
				
				btnBuilder.append(applyButton);
				
				JLabel PrinciplesLabel = new JLabel("Principles");
				btnBuilder.append(PrinciplesLabel);
				setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
				Principles = new JTextPane();
				btnBuilder.append(Principles);
				
				addToMap.addMouseListener(new MouseAdapter() {
					@Override
	                public void mouseClicked(MouseEvent e) {
						final ModeController modeController = Controller.getCurrentModeController();
				        final MMapController mapController = (MMapController) modeController.getMapController();
				        final NodeModel targetNode = mapController.getSelectedNode();					
											
						if(contradictions != null){
							for(Integer c: contradictions){
								NodeModel newNode = mapController.addNewNode(MMapController.NEW_CHILD);
								newNode.setText(trizHook.getBaseTRIZ().getRuleByKey(c));
								mapController.select(targetNode);
							}
						}
						final MapViewController mapViewManager = (MapViewController) Controller.getCurrentModeController().getController().getMapViewManager();
						mapViewManager.zoomOut();
						mapViewManager.zoomIn();
						
						final MapModel map = Controller.getCurrentController().getMap();
						mapController.setSaved(map, false);			
						addToMap.setEnabled(false);
					}
				});
				
				addToMap.setEnabled(false);
				
				btnBuilder.append(addToMap);
				
				
				final JPanel btnPanel = btnBuilder.getPanel();
				btnPanel.setAlignmentX(CENTER_ALIGNMENT);
				add(btnPanel);
			}

			ArrayList<Integer> contradictions;
			private void addTRIZ() {
				Controller controller = Controller.getCurrentController();
				for (final NodeModel node : controller.getModeController().getMapController().getSelectedNodes()) {
					final TrizExtension alreadyPresentHook = TrizExtension.getExtension(node);
					
					if(worseningBox.getSelectedItem().toString().isEmpty()){
						return;
					}
					
					if(improvingBox.getSelectedItem().toString().isEmpty()){
						return;
					}
					
					String worsen_feature = findRuleKey(worseningBox.getSelectedItem().toString());
					String improve_feature = findRuleKey(improvingBox.getSelectedItem().toString());
					
					if (alreadyPresentHook != null) {
						alreadyPresentHook.setWorseingFeatures(Integer.parseInt(worsen_feature));
						alreadyPresentHook.setImprovingFeatures(Integer.parseInt(improve_feature));
						//trizHook.undoableToggleHook(node);
					}else{					
						final TrizExtension trizExtension = new TrizExtension(node);
						//set triz properties to trizExtension
						trizExtension.setWorseingFeatures(Integer.parseInt(worsen_feature));
						trizExtension.setImprovingFeatures(Integer.parseInt(improve_feature));
						
						trizHook.undoableActivateHook(node, trizExtension);
					}
					contradictions = trizHook.getBaseTRIZ().getContradictionByKey(improve_feature+"."+worsen_feature);
					
					StringBuffer sb = new StringBuffer();
					for(Integer c: contradictions){
						sb.append(c);
						sb.append(": ");
						sb.append(trizHook.getBaseTRIZ().getRuleByKey(c));
						sb.append("\n");
					}
					Principles.setText(sb.toString());
					addToMap.setEnabled(true);
				}
			}

			private void removeTRIZ() {
		        for (final NodeModel node : getMindMapController().getMapController().getSelectedNodes()) {
					final TrizExtension alreadyPresentHook = TrizExtension.getExtension(node);
					if (alreadyPresentHook != null) {
						trizHook.undoableToggleHook(node);
					}
				}
		    }
		}
	
	private JDialog dialog;
	private INodeChangeListener nodeChangeListener;
	private INodeSelectionListener nodeSelectionListener;
	private final TrizHook trizHook;
	public TrizManagement( final TrizHook trizHook) {
		this.trizHook = trizHook;
		Controller.getCurrentController().getMapViewManager().addMapSelectionListener(this);
	}
	
	public void afterMapChange(MapModel oldMap, MapModel newMap) {
		// TODO Auto-generated method stub
		
	}

	public void beforeMapChange(MapModel oldMap, MapModel newMap) {
		disposeDialog();
		
	}

	public void propertyChange(PropertyChangeEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	public JTrizPanel createTrizPanel(final Dialog dialog) {
		JTrizPanel contentPane = new JTrizPanel();
		return contentPane;
	}
	
	private void disposeDialog() {
		if (dialog == null) {
			return;
		}
		getMindMapController().getMapController().removeNodeSelectionListener(nodeSelectionListener);
		nodeSelectionListener = null;
		getMindMapController().getMapController().removeNodeChangeListener(nodeChangeListener);
		nodeChangeListener = null;
		dialog.setVisible(false);
		dialog.dispose();
		dialog = null;
	}
	
	private ModeController getMindMapController() {
		return Controller.getCurrentModeController();
	}
}
