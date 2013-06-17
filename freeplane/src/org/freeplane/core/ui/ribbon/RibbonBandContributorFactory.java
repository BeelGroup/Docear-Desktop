package org.freeplane.core.ui.ribbon;

import java.util.Properties;

import org.freeplane.core.ui.IndexedTree;
import org.pushingpixels.flamingo.api.ribbon.JRibbonBand;

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
