package org.freeplane.core.ui.ribbon;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

public class RibbonBandContributorFactory implements IRibbonContributorFactory {

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {
			
			private JRibbonBand band;
			private boolean valid = false;
			public String getKey() {
				return attributes.getProperty("name");
			}
			
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				if(parent == null) {
					return;
				}
				final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				try {
    				band = new JRibbonBand(TextUtils.getText("ribbon.band."+attributes.getProperty("name")), null);
    				//read policies and sub-contributions
    				context.processChildren(context.getCurrentPath(), this);
    				setResizePolicies(attributes.getProperty("resize_policies"));
    				if(valid) {
    					parent.addChild(band, null);
    				}
				}
				finally {
					Thread.currentThread().setContextClassLoader(contextClassLoader);
				}
				
			}
			
			public void addChild(Object child, Object properties) {
				if(child instanceof AbstractCommandButton) {
					RibbonElementPriority priority = RibbonElementPriority.TOP;
					if(properties instanceof RibbonElementPriority) {
						priority = (RibbonElementPriority) properties;
					}
					band.addCommandButton((AbstractCommandButton) child, priority);
					valid = true;
				}
				
			}
			
			private void setResizePolicies(String policiesString) {
				if(policiesString != null) {
					String[] tokens = policiesString.split(",");
					List<RibbonBandResizePolicy> policyList = new ArrayList<RibbonBandResizePolicy>();
					for (String policyStr : tokens) {
						if("none".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.None(band.getControlPanel()));
						}
						else if("mirror".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.Mirror(band.getControlPanel()));
						}
						else if("high2low".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.High2Low(band.getControlPanel()));
						}
						else if("high2mid".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.High2Mid(band.getControlPanel()));
						}
						else if("mid2low".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.Mid2Low(band.getControlPanel()));
						}
						else if("mid2mid".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.Mid2Mid(band.getControlPanel()));
						}
						else if("low2mid".equals(policyStr.toLowerCase().trim())) {
							policyList.add(new CoreRibbonResizePolicies.Low2Mid(band.getControlPanel()));
						}
					}
					band.setResizePolicies(policyList);
				}
					
			}
		};
	}
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
