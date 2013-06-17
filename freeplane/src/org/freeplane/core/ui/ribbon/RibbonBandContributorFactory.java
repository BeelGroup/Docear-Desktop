package org.freeplane.core.ui.ribbon;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.freeplane.core.ui.IndexedTree;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;
import org.pushingpixels.flamingo.api.ribbon.resize.CoreRibbonResizePolicies;
import org.pushingpixels.flamingo.api.ribbon.resize.RibbonBandResizePolicy;

public class RibbonBandContributorFactory implements IRibbonContributorFactory {

	public IRibbonContributor getContributor(final Properties attributes) {
		return new IRibbonContributor() {
			
			public String getKey() {
				return attributes.getProperty("name");				
			}
			
			public void contribute(IndexedTree structure, IRibbonContributor parent) {
				if(parent == null) {
					return;
				}
				final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
				try {
    				JRibbonBand band = new JRibbonBand(attributes.getProperty("name"), null);
    				List<RibbonBandResizePolicy> policies = new ArrayList<RibbonBandResizePolicy>();
    				policies.add(new CoreRibbonResizePolicies.Mirror(band.getControlPanel()));
    				policies.add(new CoreRibbonResizePolicies.High2Mid(band.getControlPanel()));
    				band.setResizePolicies(policies);
    				band.addCommandButton(new JCommandButton("test"), RibbonElementPriority.TOP);
    				parent.addChild(band);
				}
				finally {
					Thread.currentThread().setContextClassLoader(contextClassLoader);
				}
				
			}
			
			public void addChild(Object child) {
				// TODO Auto-generated method stub
				
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
