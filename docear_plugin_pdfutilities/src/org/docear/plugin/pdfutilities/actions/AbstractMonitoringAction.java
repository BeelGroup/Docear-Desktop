package org.docear.plugin.pdfutilities.actions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import javax.swing.Icon;

import org.docear.addons.highlights.IHighlightsImporter;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.docear.plugin.pdfutilities.addons.DocearAddonController;
import org.docear.plugin.pdfutilities.features.AnnotationID;
import org.docear.plugin.pdfutilities.features.AnnotationModel;
import org.docear.plugin.pdfutilities.features.IAnnotation;
import org.docear.plugin.pdfutilities.features.MonitoringWorker;
import org.docear.plugin.pdfutilities.ui.conflict.ImportConflictDialog;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.EnabledAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.SaveAll;
import org.jdesktop.swingworker.SwingWorker;


@EnabledAction(checkOnNodeChange = true)
public abstract class AbstractMonitoringAction extends AFreeplaneAction {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AbstractMonitoringAction(String key) {
		super(key);
	}

	public abstract void setEnabled();

	public AbstractMonitoringAction(String key, String title, Icon icon) {
		super(key, title, icon);
	}

	public static void updateNodesAgainstMonitoringDir(NodeModel target, boolean saveall) {
		List<NodeModel> list = new ArrayList<NodeModel>();
		list.add(target);
		AbstractMonitoringAction.updateNodesAgainstMonitoringDir(list, saveall);
	}

	public static void updateNodesAgainstMonitoringDir(final List<NodeModel> targets, boolean saveall) {
		if (saveall) {
			new SaveAll().actionPerformed(null);
		}

		try {
			SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]> thread = getMonitoringThread(targets);

			SwingWorkerDialog workerDialog = new SwingWorkerDialog(Controller.getCurrentController().getViewController().getJFrame());
			workerDialog.setHeadlineText(TextUtils.getText("AbstractMonitoringAction.0")); //$NON-NLS-1$
			if(DocearAddonController.getController().hasPlugin(IHighlightsImporter.class)){
				workerDialog.setSubHeadlineText(TextUtils.getText("AbstractMonitoringAction.1")); //$NON-NLS-1$
			}
			else{
				workerDialog.setSubHeadlineText(TextUtils.getText("AbstractMonitoringAction.1.noAddon")); //$NON-NLS-1$
			}
			workerDialog.showDialog(thread);
			workerDialog = null;
			Map<AnnotationID, Collection<IAnnotation>> conflicts = thread.get();

			if (conflicts != null && conflicts.size() > 0) {
				ImportConflictDialog dialog = new ImportConflictDialog(Controller.getCurrentController().getViewController().getJFrame(), conflicts);
				dialog.showDialog();
			}
			thread = null;
		}
		catch (CancellationException e) {
			DocearController.getController().getDocearEventLogger().appendToLog(AbstractMonitoringAction.class, DocearLogEvent.MONITORING_FOLDER_READ_ABORTED);
			LogUtils.info("CancellationException during monitoring update."); //$NON-NLS-1$
		}
		catch (InterruptedException e) {
			LogUtils.info("InterruptedException during monitoring update."); //$NON-NLS-1$
		}
		catch (ExecutionException e) {
			LogUtils.warn(e);
			LogUtils.warn("ExecutionException during monitoring update."); //$NON-NLS-1$
			LogUtils.warn(e.getCause());
		}
		catch (Exception e) {
			LogUtils.warn(e);
			LogUtils.warn("===================================="); //$NON-NLS-1$
			LogUtils.warn(e.getCause());			
		}
		// System.gc();
	}

	public static SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]> getMonitoringThread(final List<NodeModel> targets) {
		return new MonitoringWorker(targets);
	}
	
	
}