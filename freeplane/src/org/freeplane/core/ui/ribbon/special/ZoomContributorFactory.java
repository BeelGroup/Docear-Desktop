package org.freeplane.core.ui.ribbon.special;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JComboBox;
import javax.swing.event.TreeSelectionEvent;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.CurrentState;
import org.freeplane.core.ui.ribbon.IChangeObserver;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory.ActionAcceleratorChangeListener;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory.ActionChangeListener;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.ui.ribbon.RibbonBuilder;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.view.swing.map.MapViewController;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.ribbon.JFlowRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.IconRibbonBandResizePolicy;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

public class ZoomContributorFactory implements IRibbonContributorFactory {
	
	private ActionAcceleratorChangeListener changeListener;

	public ZoomContributorFactory(RibbonBuilder builder) {
		builder.getAcceleratorManager().addAcceleratorChangeListener(getAccelChangeListener());
	}
	

	protected ActionAcceleratorChangeListener getAccelChangeListener() {
		if(changeListener == null) {
			changeListener = new ActionAcceleratorChangeListener();
		}
		return changeListener;
	}

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {

			public String getKey() {
				return attributes.getProperty("name");
			}

			public void contribute(final RibbonBuildContext context, ARibbonContributor parent) {
				if (parent == null) {
					return;
				}				
				JFlowRibbonBand band = new JFlowRibbonBand(TextUtils.getText("ribbon.band.zoom"), null, null);
				
				JComboBox zoomBox = ((MapViewController) Controller.getCurrentController().getMapViewManager()).createZoomBox();
				addDefaultToggleHandler(context,zoomBox);
				band.addFlowComponent(zoomBox);
				
				AFreeplaneAction action = context.getBuilder().getMode().getAction("FitToPage");				
				JCommandButton button = RibbonActionContributorFactory.createCommandButton(action);				
				button.setDisplayState(CommandButtonDisplayState.MEDIUM);
				getAccelChangeListener().addAction(action.getKey(), button);
				addDefaultToggleHandler(context, action, button);
				band.addFlowComponent(button);
				
				List<RibbonBandResizePolicy> policies = new ArrayList<RibbonBandResizePolicy>();				
				policies.add(new CoreRibbonResizePolicies.FlowThreeRows(band.getControlPanel()));
				policies.add(new IconRibbonBandResizePolicy(band.getControlPanel()));
				band.setResizePolicies(policies);			
				
				parent.addChild(band, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));		    	
			}

			public void addChild(Object child, ChildProperties properties) {
			}
		};
	}
	
	private void addDefaultToggleHandler(final RibbonBuildContext context, final Component component) {
		context.getBuilder().getMapChangeAdapter().addListener(new IChangeObserver() {
			public void updateState(CurrentState state) {
				if(state.isNodeChangeEvent()) {					
				}
				else if(state.allMapsClosed()) {					
					component.setEnabled(false);
				}
				else if (state.get(TreeSelectionEvent.class) == null) {
					component.setEnabled(true);
				}
			}
		});
	}
	
	private void addDefaultToggleHandler(final RibbonBuildContext context, final AFreeplaneAction action, final AbstractCommandButton button) {		
		context.getBuilder().getMapChangeAdapter().addListener(new ActionChangeListener(action, button));
	}	
}
