package org.freeplane.plugin.workspace.mindmapmode;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Properties;

import javax.swing.KeyStroke;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonAcceleratorManager;
import org.freeplane.core.ui.ribbon.RibbonActionContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.actions.WorkspaceProjectOpenLocationAction;
import org.freeplane.plugin.workspace.model.project.IProjectSelectionListener;
import org.freeplane.plugin.workspace.model.project.ProjectSelectionEvent;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandMenuButton;
import org.pushingpixels.flamingo.api.common.popup.JCommandPopupMenu;
import org.pushingpixels.flamingo.api.common.popup.JPopupPanel;
import org.pushingpixels.flamingo.api.common.popup.PopupPanelCallback;
import org.pushingpixels.flamingo.api.ribbon.RibbonElementPriority;

public class WorkspaceProjectBandContributorFactory implements IRibbonContributorFactory {
	
	private JCommandButton removeButton;
	private JCommandButton newButton;
	private JCommandButton openButton;
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public WorkspaceProjectBandContributorFactory(MModeWorkspaceController modeWorkspaceController) {
		modeWorkspaceController.getView().addProjectSelectionListener(new IProjectSelectionListener() {
			public void selectionChanged(ProjectSelectionEvent event) {
				boolean enabled = (event.getSelectedProject() != null);
				if(newButton != null) {
					newButton.setEnabled(enabled);
				}
				if(removeButton != null) {
					removeButton.setEnabled(enabled);
				}
				if(removeButton != null) {
					openButton.setEnabled(enabled);
				}
			}
		});
		
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {

			@Override
			public String getKey() {
				return attributes.getProperty("name");
			}
			
			@Override
			public void contribute(final RibbonBuildContext context, ARibbonContributor parent) {
				boolean enabled = (WorkspaceController.getSelectedProject() != null);
				removeButton = RibbonActionContributorFactory.createCommandButton(WorkspaceController.getAction("workspace.action.project.remove"));
				removeButton.setEnabled(enabled);
				
				ChildProperties childProperties = new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", "")));
				childProperties.set(RibbonElementPriority.class, RibbonElementPriority.MEDIUM);
				parent.addChild(removeButton, childProperties);
				
				final WorkspaceProjectOpenLocationAction openLocAction = new WorkspaceProjectOpenLocationAction();
				openButton = new JCommandButton(RibbonActionContributorFactory.getActionTitle(openLocAction), RibbonActionContributorFactory.getActionIcon(openLocAction));
				KeyStroke ks = RibbonAcceleratorManager.parseKeyStroke("control alt L");
				context.getBuilder().getAcceleratorManager().setAccelerator(openLocAction, ks);
				RibbonActionContributorFactory.updateRichTooltip(openButton, openLocAction, ks);
				openButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						openLocAction.actionPerformed(e);
					}
				});
				openButton.setEnabled(enabled);
				childProperties = new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", "")));
				childProperties.set(RibbonElementPriority.class, RibbonElementPriority.MEDIUM);
				parent.addChild(openButton, childProperties);
				
				newButton = RibbonActionContributorFactory.createCommandButton(RibbonActionContributorFactory.getDummyAction("workspace.action.new.label"));
				newButton.setCommandButtonKind(CommandButtonKind.POPUP_ONLY);
				newButton.setPopupCallback(new PopupPanelCallback() {
					
					public JPopupPanel getPopupPanel(JCommandButton commandButton) {
						JCommandPopupMenu popupmenu = new JCommandPopupMenu();
						AFreeplaneAction action = WorkspaceController.getAction("workspace.action.node.new.folder");
						JCommandMenuButton menuButton = RibbonActionContributorFactory.createCommandMenuButton(action);
						KeyStroke ks = context.getBuilder().getAcceleratorManager().getAccelerator(action.getKey());
						if(ks != null) {
							RibbonActionContributorFactory.updateRichTooltip(menuButton, action, ks);
						}
						popupmenu.addMenuButton(menuButton);
						
						action = WorkspaceController.getAction("workspace.action.node.new.link");
						menuButton = RibbonActionContributorFactory.createCommandMenuButton(action);
						ks = context.getBuilder().getAcceleratorManager().getAccelerator(action.getKey());
						if(ks != null) {
							RibbonActionContributorFactory.updateRichTooltip(menuButton, action, ks);
						}
						popupmenu.addMenuButton(menuButton);
						
						return popupmenu;
					}
				});
				newButton.setEnabled(enabled);
				
				childProperties = new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", "")));
				childProperties.set(RibbonElementPriority.class, RibbonElementPriority.MEDIUM);
				parent.addChild(newButton, childProperties);
			}
			
			@Override
			public void addChild(Object child, ChildProperties properties) {
			}
		};
	}
}
