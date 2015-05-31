package org.freeplane.view.swing.features.triz.mindmapmode;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.INodeSelectionListener;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.NodeHookDescriptor;
import org.freeplane.features.mode.PersistentNodeHook;
import org.freeplane.features.mode.PersistentNodeHook.HookAction;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.view.swing.features.time.mindmapmode.ReminderExtension;
import org.freeplane.view.swing.features.triz.mindmapmode.TrizManagement.JTrizPanel;
import org.freeplane.view.swing.map.attribute.AttributePanelManager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

@NodeHookDescriptor(hookName = "plugins/TRIZ.xml", onceForMap = false)
public class TrizHook extends PersistentNodeHook implements IExtension{
	
	class TrizBase{		
		private String url;
		public TrizBase(String URL){
			this.url = URL;
		}	
		
		private JSONObject trizJSON;
		private Map<Integer,String> FactorDef = new HashMap<Integer,String>();
		private Map<Integer,String> RulesDef = new HashMap<Integer,String>();
		private Map<String,ArrayList<Integer>> ContradictionDef = new HashMap<String,ArrayList<Integer>>();
		
		public String getFactorbyKey(int key){
			return FactorDef.get(key);
		}
		
		public Map<Integer,String> FactorDefs()
		{
			return FactorDef;
		}
		
		public String getRuleByKey(int key){
			String rule = TextUtils.getSafeRawText("plugins/TrizManagement.xml_rule"+String.valueOf(key));
			if(rule != null)
			{
				return rule;
			}
			return RulesDef.get(key);
		}
		
		public ArrayList<Integer> getContradictionByKey(String key){
			return ContradictionDef.get(key);
		}
		
		public void parse(){
			final URL trizURL = ResourceController.getResourceController().getResource(this.url);
			InputStreamReader streamReader = null;
			try {
				streamReader = new InputStreamReader(new BufferedInputStream(trizURL.openStream()));
				BufferedReader br = new BufferedReader(streamReader);

                String valueString;
                StringBuilder buffer = new StringBuilder();
                try {
                    while ((valueString = br.readLine()) != null) {
                        buffer.append(valueString);
                    }
                } catch (Exception ex) {
                }
                trizJSON = (JSONObject) JSON.parse(buffer.toString());
                
                //create definition
                JSONArray definitions = (JSONArray) trizJSON.get("Definitions");
                if(definitions != null){
                    for(Object fact: definitions){
                    	Map<String, Object> m = (Map<String, Object>) fact;
                    	FactorDef.put(Integer.parseInt(m.get("id").toString()), m.get("detail").toString());
                    }
                }
                
                //create Principles
                JSONArray principles = (JSONArray) trizJSON.get("Principles");
                if(principles != null){
                    for(Object rule: principles){
                    	Map<String, Object> m = (Map<String, Object>) rule;
                    	RulesDef.put(Integer.parseInt(m.get("id").toString()), m.get("detail").toString());
                    }
                }
                
                //load Contradiction
                JSONArray contradictions = (JSONArray) trizJSON.get("Contradiction");
                JSONArray rules;
                JSONArray innerrules;
                if(contradictions != null){
                    for(Object contradiction: contradictions){
                    	Map<String, Object> m = (Map<String, Object>) contradiction;
                    	String improve_id = m.get("improve_id").toString();
                    	rules = (JSONArray) m.get("rules");                    	
                    	for(Object r: rules){
                    		Map<String, Object> mm = (Map<String, Object>) r;
                    		String worsen_id = mm.get("worsen_id").toString();
                    		innerrules = (JSONArray) mm.get("rules");
                    		ArrayList<Integer> prins = new ArrayList<Integer>();
                    		for(Object inner: innerrules){
                    			prins.add(Integer.parseInt(inner.toString()));
                    		}
                    		
                    		ContradictionDef.put(improve_id+"."+worsen_id, prins);
                    	}
                        
                    }
                }
			}
			catch (final Exception e) {
			}
	        finally {
	        	FileUtils.silentlyClose(streamReader);
	        }			 			
		}
	}
	
	@EnabledAction(checkOnNodeChange = true)
	private class TrizHookAction extends HookAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * 
		 */
		public TrizHookAction() {
			super("TrizHookAction");
		}

		@Override
		public void setEnabled() {
			setEnabled(isActiveForSelection());
		}
	}
	
	private ModeController modeController;
	private TrizBase       baseTRIZ;
	public TrizHook(ModeController modeController){
		super();
		this.modeController = modeController;
		this.setBaseTRIZ(new TrizBase("/json/triz.json"));
		this.getBaseTRIZ().parse();
		
		modeController.addMenuContributor(new IMenuContributor() {
			public void updateMenus(ModeController modeController, MenuBuilder builder) {
				createTrizPanel();
			}
		});
	}
	
	ModeController getModeController() {
    	return modeController;
    }
	
	private void createTrizPanel() {
		final int axis = BoxLayout.Y_AXIS;
		final TrizManagement trizManagement = new TrizManagement(this);
		final JTrizPanel trizPanel = trizManagement.createTrizPanel(null);
		modeController.getMapController().addNodeSelectionListener(new INodeSelectionListener() {
			public void onSelect(NodeModel node) {
				trizPanel.update(node);
			}
			
			public void onDeselect(NodeModel node) {
			}
		});
		
		modeController.getMapController().addNodeChangeListener(new INodeChangeListener() {
			public void nodeChanged(NodeChangeEvent event) {
				final NodeModel node = event.getNode();
				if(event.getProperty().equals(getExtensionClass()) && node.equals(modeController.getMapController().getSelectedNode()))
						trizPanel.update(node);
			}
		});
		
		//TODO: 
		//trizPanel.setBorder(BorderFactory.createTitledBorder(TextUtils.getText("calendar_panel")));
		trizPanel.setBorder(BorderFactory.createTitledBorder(TextUtils.getText("plugins/TrizManagement.xml_GroupTitle")));
		
		final JPanel tablePanel = new AttributePanelManager(modeController).getTablePanel();
		tablePanel.setBorder(BorderFactory.createTitledBorder(TextUtils.getText("attributes_attribute")));
		final Box panel = new Box(axis);
		panel.add(trizPanel);
		//panel.add(tablePanel);
		final JTabbedPane tabs = (JTabbedPane) modeController.getUserInputListenerFactory().getToolBar("/format").getComponent(1);
		final JScrollPane trizScrollPane = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		UITools.setScrollbarIncrement(trizScrollPane);
		//tabs.add(TextUtils.getText("calendar_attributes_panel"), timeScrollPane);
		tabs.add(TextUtils.getText("plugins/TrizManagement.xml_TabTitle"), trizScrollPane);
    }
		
	@Override
	protected HookAction createHookAction() {
		return new TrizHookAction();
	}

	@Override
	protected Class<? extends IExtension> getExtensionClass() {
		return TrizExtension.class;
	}

	@Override
	public void remove(final NodeModel node, final IExtension extension) {
		final TrizExtension trizExtension = (TrizExtension) extension;
		trizExtension.displayState(null, trizExtension.getNode(), true);
		modeController.getMapController().removeMapChangeListener(trizExtension);
		super.remove(node, extension);
	}
	
	@Override
	public void add(final NodeModel node, final IExtension extension) {
		final TrizExtension trizExtension = (TrizExtension) extension;
		modeController.getMapController().addMapChangeListener(trizExtension);
		super.add(node, extension);
	}

	static final String IMPROVEMENT = "improving";
	static final String WORSEN = "worsening";
	@Override
	protected void saveExtension(final IExtension extension, final XMLElement element) {
		super.saveExtension(extension, element);
		final TrizExtension trizExtension = (TrizExtension) extension;
		final XMLElement parameters = element.createElement("TrizParas");
		parameters.setAttribute(IMPROVEMENT,String.valueOf(trizExtension.getImprovingFeatures()));
		parameters.setAttribute(WORSEN, String.valueOf(trizExtension.getWorseingFeatures()));		
		element.addChild(parameters);
	}
	
	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		final TrizExtension trizExtension = new TrizExtension(node);
		final XMLElement parameters = element.getFirstChildNamed("TrizParas");
		final String improve = parameters.getAttribute(IMPROVEMENT, "");
		final String worsen = parameters.getAttribute(WORSEN, "");
		trizExtension.setImprovingFeatures(Integer.parseInt(improve));
		trizExtension.setWorseingFeatures(Integer.parseInt(worsen));;
		return trizExtension;
	}

	public TrizBase getBaseTRIZ() {
		return baseTRIZ;
	}

	public void setBaseTRIZ(TrizBase baseTRIZ) {
		this.baseTRIZ = baseTRIZ;
	}
}
