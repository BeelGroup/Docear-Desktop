package org.docear.plugin.pdfutilities.features;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.docear.plugin.core.features.DocearMapModelController;
import org.docear.plugin.core.logger.DocearLogEvent;
import org.docear.plugin.core.ui.SwingWorkerDialog;
import org.docear.plugin.core.util.DirectoryFileFilter;
import org.docear.plugin.core.util.HtmlUtils;
import org.docear.plugin.core.util.NodeUtilities;
import org.docear.plugin.core.workspace.model.DocearWorkspaceProject;
import org.docear.plugin.pdfutilities.PdfUtilitiesController;
import org.docear.plugin.pdfutilities.actions.AbstractMonitoringAction;
import org.docear.plugin.pdfutilities.features.DocearNodeMonitoringExtension.DocearExtensionKey;
import org.docear.plugin.pdfutilities.features.IAnnotation.AnnotationType;
import org.docear.plugin.pdfutilities.map.AnnotationController;
import org.docear.plugin.pdfutilities.map.MapConverter;
import org.docear.plugin.pdfutilities.pdf.PdfAnnotationImporter;
import org.docear.plugin.pdfutilities.pdf.PdfFileFilter;
import org.docear.plugin.pdfutilities.util.CustomFileFilter;
import org.docear.plugin.pdfutilities.util.CustomFileListFilter;
import org.docear.plugin.pdfutilities.util.MonitoringUtils;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.LinkController;
import org.freeplane.features.map.INodeView;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.workspace.URIUtils;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.view.swing.map.MapView;
import org.freeplane.view.swing.map.NodeView;
import org.jdesktop.swingworker.SwingWorker;

import de.intarsys.pdf.cos.COSRuntimeException;
import de.intarsys.pdf.parser.COSLoadException;

public class MonitoringWorker extends SwingWorker<Map<AnnotationID, Collection<IAnnotation>>, AnnotationModel[]> {
	private final List<NodeModel> targets;
	List<URI> monitorFiles = new ArrayList<URI>();
	List<URI> otherFilesLinkedInMindMap = new ArrayList<URI>();
	List<MapModel> monitoredMindmaps = new ArrayList<MapModel>();
	Map<AnnotationID, List<NodeModel>> nodeIndex = new HashMap<AnnotationID, List<NodeModel>>();
	Map<AnnotationID, AnnotationModel> importedFiles = new HashMap<AnnotationID, AnnotationModel>();
	Map<AnnotationID, AnnotationModel> importedOtherFiles = new HashMap<AnnotationID, AnnotationModel>();
	List<NodeModel> orphanedNodes = new ArrayList<NodeModel>();
	List<AnnotationModel> newAnnotations = new ArrayList<AnnotationModel>();
	Map<String, List<NodeModel>> equalChildIndex = new HashMap<String, List<NodeModel>>();
	Map<AnnotationID, Collection<IAnnotation>> conflicts = new HashMap<AnnotationID, Collection<IAnnotation>>();
	private boolean isfolded;
	private boolean canceledDuringPasting;
	private NodeModel currentTarget;
	private long time;

	public MonitoringWorker(List<NodeModel> targets) {
		this.targets = targets;
		time = System.currentTimeMillis();
	}

	protected Map<AnnotationID, Collection<IAnnotation>> doInBackground() throws Exception {
		DocearController.getController().getSemaphoreController().lock("MindmapUpdate");
		NodeView.setModifyModelWithoutRepaint(true);
		MapView.setNoRepaint(true);
		try {
			// Controller.getCurrentController().getViewController().getMapView().setVisible(false);
			for (final NodeModel target : targets) {
				currentTarget = target;
				File file = MonitoringUtils.getPdfDirFromMonitoringNode(target);
				if(file == null) {
					fireStatusUpdate(SwingWorkerDialog.DETAILS_LOG_TEXT, null, "corresponding project is not loaded");
					continue;
				}
				URI uri = file.toURI();
				if (uri != null) {
					DocearController.getController().getDocearEventLogger()
							.appendToLog(this, DocearLogEvent.MONITORING_FOLDER_READ, URIUtils.getAbsoluteURI(uri));
				}
	
				if (canceled()) return conflicts;
				String textWithoutHTML = HtmlUtils.extractText(target.getText());
				fireStatusUpdate(SwingWorkerDialog.SET_SUB_HEADLINE, null,
						TextUtils.getText("AbstractMonitoringAction.6") + textWithoutHTML + TextUtils.getText("AbstractMonitoringAction.7")); //$NON-NLS-1$ //$NON-NLS-2$
				fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
	
				if (!cleanUpCollections()) continue;
	
				if (!setupPreconditions(target)) continue;
	
				if (!buildNodeIndex(target)) continue;
	
				if (!loadMonitoredFiles(target)) continue;
	
				if (!searchNewAndConflictedNodes()) continue;
	
				if (!searchingOrphanedNodes(target)) continue;
	
				isfolded = target.isFolded();
				if (newAnnotations.size() > 100) {
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.8")); //$NON-NLS-1$
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							target.setFolded(true);
						}
					});
				}
				canceledDuringPasting = true;
				if (!pasteNewNodesAndRemoveOrphanedNodes(target)) {
					canceledDuringPasting = false;
					if (newAnnotations.size() > 100) {
						fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
						fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.9")); //$NON-NLS-1$
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								target.setFolded(isfolded);
							}
						});
					}
					continue;
				}
	
				DocearEvent event = new DocearEvent(this, (DocearWorkspaceProject) WorkspaceController.getProject(target.getMap()), DocearEventType.MINDMAP_ADD_PDF_TO_NODE, true);
				DocearController.getController().dispatchDocearEvent(event);
				if (newAnnotations.size() > 100) {
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_INDETERMINATE, null, null);
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.9")); //$NON-NLS-1$
					SwingUtilities.invokeAndWait(new Runnable() {
						public void run() {
							target.setFolded(isfolded);
						}
					});
				}
	
			}
			return conflicts;
		}
		finally {
			closeAll();
		}
	}
	
	private boolean closeAll() {
		Map<String, MapModel> maps = Controller.getCurrentController().getMapViewManager().getMaps();
		try {
			for (Entry<String, MapModel> entry : maps.entrySet()) {
				monitoredMindmaps.remove(entry.getValue());
			}
			for (MapModel map : monitoredMindmaps) {
				map.destroy();
			}
			new Thread( new Runnable() {
				public void run() {
					System.gc();	
				}
			}).start();
			return true;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
//		monitoredMindmaps.clear();
//		nodeIndex.clear();
//		importedFiles.clear();
//		newAnnotations.clear();
//		equalChildIndex.clear();
//		orphanedNodes.clear();
//		importedOtherFiles.clear();
//		otherFilesLinkedInMindMap.clear();
		return false;
	}

	private boolean searchingOrphanedNodes(NodeModel target) throws InterruptedException, InvocationTargetException {
		fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
		fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.11")); //$NON-NLS-1$
		int count = 0;
		for (AnnotationID id : nodeIndex.keySet()) {
			if (canceled()) return false;
			count++;
			fireProgressUpdate(100 * count / nodeIndex.keySet().size());
			if (importedFiles.containsKey(id)) continue;
			for (NodeModel node : nodeIndex.get(id)) {
				if (!isMonitoringNodeChild(target, node)) continue;
				AnnotationNodeModel annotation = AnnotationController.getAnnotationNodeModel(node);
				if (annotation == null) continue;
				if (annotation.getAnnotationType() == null) continue;
				if (annotation.getAnnotationType().equals(AnnotationType.FILE)) continue;
				if (orphanedNodes.contains(node)) continue;
				try {
					File file = URIUtils.getAbsoluteFile(URIUtils.getAbsoluteURI(node));
					if (file != null && !file.exists()) {
						orphanedNodes.add(node);
						continue;
					}
					else if (file != null) {
						File monitoringDirectory = MonitoringUtils.getPdfDirFromMonitoringNode(target);
						if (file.getPath().startsWith(monitoringDirectory.getPath())) {
							AnnotationModel annoation = new PdfAnnotationImporter().searchAnnotation(URIUtils.getAbsoluteURI(node), node);
							if (annoation == null) {
								orphanedNodes.add(node);
							}
						}
					}

				}
				catch (Exception e) {
					LogUtils.info("Exception during import file: " + URIUtils.getAbsoluteURI(node)); //$NON-NLS-1$
				}
			}
		}
		return true;
	}

	private boolean isMonitoringNodeChild(NodeModel monitoringNode, NodeModel node) {
		List<NodeModel> pathToRoot = Arrays.asList(node.getPathToRoot());
		return pathToRoot.contains(monitoringNode);
	}

	private boolean cleanUpCollections() {
		monitorFiles.clear();
		monitoredMindmaps.clear();
		nodeIndex.clear();
		importedFiles.clear();
		newAnnotations.clear();
		equalChildIndex.clear();
		orphanedNodes.clear();
		importedOtherFiles.clear();
		otherFilesLinkedInMindMap.clear();
		return true;
	}

	@Override
	protected void done() {
		DocearController.getController().getSemaphoreController().unlock("MindmapUpdate");
		try {
			if (this.isCancelled() || Thread.currentThread().isInterrupted()) {
				if (newAnnotations.size() > 100 && canceledDuringPasting) {
					if (currentTarget != null) {
						currentTarget.setFolded(isfolded);
					}
				}					
				this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, TextUtils.getText("AbstractMonitoringAction.15")); //$NON-NLS-1$
			}
			else {
				if (currentTarget != null) {
					DocearController.getController().dispatchDocearEvent(new DocearEvent(this, (DocearWorkspaceProject) WorkspaceController.getProject(currentTarget.getMap()), DocearEventType.UPDATE_MAP, currentTarget.getMap()));
				}
				this.firePropertyChange(SwingWorkerDialog.IS_DONE, null, TextUtils.getText("AbstractMonitoringAction.16")); //$NON-NLS-1$					
			}
		}
		finally {
			NodeView.setModifyModelWithoutRepaint(false);
			MapView.setNoRepaint(false);
		}
		if (currentTarget!= null) {
			MapModel map = currentTarget.getMap();
			LogUtils.info("updating view for map: " + map.getTitle());
			for(INodeView nodeView : map.getRootNode().getViewers()) {
				if(nodeView instanceof NodeView) {
					((NodeView) nodeView).updateAll();
				}
			}
		}
		time = System.currentTimeMillis() - time;
		System.out.println("execution time: " + (time / 1000));

	}

	private boolean canceled() throws InterruptedException {
		// Thread.sleep(1L);
		return (this.isCancelled() || Thread.currentThread().isInterrupted());
	}

	private boolean pasteNewNodesAndRemoveOrphanedNodes(NodeModel target) throws InterruptedException, InvocationTargetException {
		fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
		fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.17")); //$NON-NLS-1$

		for (AnnotationModel annotation : newAnnotations) {
			if (canceled()) return false;
			try {
				fireProgressUpdate(100 * newAnnotations.indexOf(annotation) / newAnnotations.size());
				if (annotation.isInserted()) continue;
				// PDF's with no new annotations should not be imported,
				// see Ticket #283
				if (annotation.getAnnotationType().equals(AnnotationType.PDF_FILE) && annotation.getChildren().size() > 0
						&& !annotation.hasNewChildren()) continue;
				Stack<NodeModel> treePathStack = getTreePathStack(annotation, target);
				if (canceled()) return false;
				NodeModel tempTarget = target;
				while (!treePathStack.isEmpty()) {
					NodeModel insertNode = treePathStack.pop();
					NodeModel equalChild = getEqualChild(insertNode);
					if (equalChild == null) {
						final NodeModel finalTarget = tempTarget;
						final NodeModel finalInsertNode = insertNode;
						if (canceled()) return false;
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								int newNodePostion = AnnotationController.getAnnotationPosition(finalInsertNode);
								boolean pasted = false;
								for (NodeModel child : finalTarget.getChildren()) {
									int childPosition = AnnotationController.getAnnotationPosition(child);
									if (childPosition > newNodePostion) {
										// finalTarget.insert(finalInsertNode,
										// finalTarget.getChildPosition(child));
										((MMapController) Controller.getCurrentModeController().getMapController()).addNewNode(finalInsertNode,
												finalTarget, finalTarget.getChildPosition(child), finalTarget.isNewChildLeft());
										pasted = true;
										break;
									}
								}
								if (!pasted) {
									((MMapController) Controller.getCurrentModeController().getMapController()).addNewNode(finalInsertNode,
											finalTarget, finalTarget.getChildCount(), finalTarget.isNewChildLeft());
								}
							}
						});
						tempTarget = insertNode;
						if (!equalChildIndex.containsKey(insertNode.getText())) {
							equalChildIndex.put(insertNode.getText(), new ArrayList<NodeModel>());
						}
						equalChildIndex.get(insertNode.getText()).add(insertNode);
					}
					else {
						tempTarget = equalChild;
					}
				}
			}
			catch (InterruptedException e) {
			}
			catch (Exception e) {
				LogUtils.warn(e);
			}
		}
		if (orphanedNodes.size() > 0) {
			if (canceled()) return false;
			try {
				int result = UITools.showConfirmDialog(target,
						TextUtils.getText("AbstractMonitoringAction.18"), TextUtils.getText("AbstractMonitoringAction.18"), JOptionPane.YES_NO_OPTION); //$NON-NLS-1$ //$NON-NLS-2$
				if (result == JOptionPane.OK_OPTION) {
					fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
					fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.20")); //$NON-NLS-1$
					for (final NodeModel node : orphanedNodes) {
						SwingUtilities.invokeAndWait(new Runnable() {
							public void run() {
								try {
									if (node.getParentNode() != null) {
										node.removeFromParent();
									}
								}
								catch (Exception e) {
									LogUtils.warn(e);
								}
							}
						});
					}
				}
			}
			catch (Exception e) {
				LogUtils.warn(e);
			}
		}
		return true;
	}

	private NodeModel getEqualChild(NodeModel insertNode) {
		if (equalChildIndex.containsKey(insertNode.getText())) {
			for (NodeModel equalChild : equalChildIndex.get(insertNode.getText())) {
				if (isEqualNode(equalChild, insertNode)) {
					return equalChild;
				}
			}
		}
		return null;
	}

	private URI getAbsoluteURIFromNode(NodeModel node, MapModel map) {
		try {
			return URIUtils.resolveURI(map.getURL().toURI(), NodeUtilities.getLink(node));
		} catch (Exception e) {
			LogUtils.info("Exception in "+AbstractMonitoringAction.class +".getAbsoluteURIFromNode(NodeModel,MapModel): "+e.getMessage());
		}
		return null;
	}

	private boolean isEqualNode(NodeModel node1, NodeModel node2) {
		if (!node1.getText().equals(node2.getText())) {
			return false;
		}
		URI uri1 = getAbsoluteURIFromNode(node1, node1.getMap());
		URI uri2 = getAbsoluteURIFromNode(node2, node2.getMap());
		if (uri1 != null && uri2 != null) {
			if (!uri1.equals(uri2)) {
				return false;
			}
		}
		if (uri1 != null && uri2 == null) {
			return false;
		}
		if (uri1 == null && uri2 != null) {
			return false;
		}
		if (node1.containsExtension(DocearNodeMonitoringExtension.class) && !node2.containsExtension(DocearNodeMonitoringExtension.class)) {
			return false;
		}
		if (node2.containsExtension(DocearNodeMonitoringExtension.class) && !node1.containsExtension(DocearNodeMonitoringExtension.class)) {
			return false;
		}
		if (node1.containsExtension(IAnnotation.class) && !node2.containsExtension(IAnnotation.class)) {
			return false;
		}
		if (node2.containsExtension(IAnnotation.class) && !node1.containsExtension(IAnnotation.class)) {
			return false;
		}
		if (node1.containsExtension(DocearNodeMonitoringExtension.class) && !node2.containsExtension(DocearNodeMonitoringExtension.class)) {
			return false;
		}
		if (node1.containsExtension(AnnotationModel.class) && !node2.containsExtension(AnnotationModel.class)) {
			return false;
		}
		if (node2.containsExtension(AnnotationModel.class) && !node1.containsExtension(AnnotationModel.class)) {
			return false;
		}
		if (node1.containsExtension(AnnotationModel.class) && node2.containsExtension(AnnotationModel.class)) {
			IAnnotation anno1 = AnnotationController.getAnnotationNodeModel(node1);
			IAnnotation anno2 = AnnotationController.getAnnotationNodeModel(node2);
			if (anno1.getAnnotationType() != anno2.getAnnotationType()) {
				return false;
			}
			if (!anno1.getAnnotationID().equals(anno2.getAnnotationID())) {
				return false;
			}
		}
		if (node1.containsExtension(IAnnotation.class) && node2.containsExtension(IAnnotation.class)) {
			IAnnotation anno1 = AnnotationController.getAnnotationNodeModel(node1);
			IAnnotation anno2 = AnnotationController.getAnnotationNodeModel(node2);
			if (anno1.getAnnotationType() != anno2.getAnnotationType()) {
				return false;
			}
			if (!anno1.getAnnotationID().equals(anno2.getAnnotationID())) {
				return false;
			}
		}
		return true;
	}

	private Stack<NodeModel> getTreePathStack(AnnotationModel annotation, NodeModel target) throws InterruptedException {
		Stack<NodeModel> result = new Stack<NodeModel>();
		AnnotationModel tempAnnotation = annotation;
		do {
			if (canceled()) return result;
			// Scripting Error Bugfix
			if (tempAnnotation.getTitle() != null && tempAnnotation.getTitle().length() > 1 && tempAnnotation.getTitle().charAt(0) == '=') {
				tempAnnotation.setTitle(" " + tempAnnotation.getTitle()); //$NON-NLS-1$
			}
			NodeModel node = ((MMapController) Controller.getCurrentModeController().getMapController()).newNode(tempAnnotation.getTitle(),
					target.getMap());
			AnnotationController.setModel(node, tempAnnotation);
			NodeUtilities.setLinkFrom(tempAnnotation.getSource(), node);
			result.push(node);
			tempAnnotation.setInserted(true);
			tempAnnotation = tempAnnotation.getParent();
		} while (tempAnnotation != null);
		if (!isFlattenSubfolders(target)) {
			File pdfDirFile = MonitoringUtils.getPdfDirFromMonitoringNode(target);
			File annoFile = URIUtils.getAbsoluteFile(annotation.getSource());
			if (annoFile != null) {
				File parent = annoFile.getParentFile();
				while (parent != null && !MonitoringUtils.isParent(pdfDirFile, parent)/*parent.equals(pdfDirFile)*/) {
					if (canceled()) return result;
					NodeModel node = ((MMapController) Controller.getCurrentModeController().getMapController()).newNode(parent.getName(),
							target.getMap());
					DocearNodeMonitoringExtensionController.setEntry(node, DocearExtensionKey.MONITOR_PATH, null);
					NodeUtilities.setLinkFrom(LinkController.normalizeURI(parent.toURI()), node);
					result.push(node);
					parent = parent.getParentFile();
				}
			}
		}
		return result;
	}

	private boolean searchNewAndConflictedNodes() throws InterruptedException, InvocationTargetException {
		fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
		fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.22")); //$NON-NLS-1$
		int count = 0;
		for (AnnotationID id : importedFiles.keySet()) {
			if (canceled()) return false;
			fireProgressUpdate(100 * count / importedFiles.keySet().size());
			if (!nodeIndex.containsKey(id)) {
				importedFiles.get(id).setNew(true);
				newAnnotations.add(importedFiles.get(id));
			}
			else {
				AnnotationModel importedAnnotation = importedFiles.get(id);
				for (NodeModel node : nodeIndex.get(id)) {
					AnnotationNodeModel oldAnnotation = AnnotationController.getAnnotationNodeModel(node);
					if (oldAnnotation != null) {
						if (oldAnnotation.getAnnotationType() == null) continue;
						if (oldAnnotation.getAnnotationType().equals(AnnotationType.PDF_FILE)) continue;
						if (oldAnnotation.getAnnotationType().equals(AnnotationType.FILE)) continue;
						String oldAnnotationWithoutHTML = HtmlUtils.extractText(oldAnnotation.getTitle());
						String importedAnnotationWithoutHTML = HtmlUtils.extractText(importedAnnotation.getTitle());
						String importedAnnotationTitle = importedAnnotation.getTitle().replace("\r", "").replace("\n", "").replace("\t", "")
								/*.replace(" ", "")*/;
						importedAnnotationWithoutHTML = importedAnnotationWithoutHTML.replace("\r", "").replace("\n", "").replace("\t", "")/*.replace(" ", "")*/;
						String oldAnnotationTitle = oldAnnotation.getTitle().replace("\r", "").replace("\n", "").replace("\t", "")/*.replace(" ", "")*/;
						oldAnnotationWithoutHTML = oldAnnotationWithoutHTML.replace("\r", "").replace("\n", "").replace("\t", "")/*.replace(" ", "")*/;
						if (!importedAnnotationTitle.trim().equals(oldAnnotationTitle.trim())
								&& !importedAnnotationTitle.trim().equals(oldAnnotationWithoutHTML.trim()) && !importedAnnotationWithoutHTML.trim().equals(oldAnnotationWithoutHTML.trim())) {
							importedAnnotation.setConflicted(true);
							AnnotationController.addConflictedAnnotation(importedAnnotation, conflicts);
							for (NodeModel conflictedNode : nodeIndex.get(id)) {
								AnnotationNodeModel conflictedAnnotation = AnnotationController.getAnnotationNodeModel(conflictedNode);
								if (conflictedAnnotation != null) {
									AnnotationController.addConflictedAnnotation(conflictedAnnotation, conflicts);
								}
							}
							break;
						}
					}
				}

			}
			count++;
		}
		return true;
	}

	private boolean loadMonitoredFiles(NodeModel target) throws InterruptedException, InvocationTargetException {
		fireStatusUpdate(SwingWorkerDialog.SET_PROGRESS_BAR_DETERMINATE, null, null);
		fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.23")); //$NON-NLS-1$
		for (URI uri : monitorFiles) {
			if (canceled()) return false;
			try {
				fireProgressUpdate(100 * monitorFiles.indexOf(uri) / monitorFiles.size());
				File file = URIUtils.getAbsoluteFile(uri);
				fireStatusUpdate(SwingWorkerDialog.NEW_FILE, null, file.getName());
				if (new PdfFileFilter().accept(uri)) {
					AnnotationModel pdf = new PdfAnnotationImporter().importPdf(uri);
					addAnnotationsToImportedFiles(pdf, target);
				}
				else {
					AnnotationModel annotation = new AnnotationModel(0, AnnotationType.FILE);
					annotation.setSource(uri);
					annotation.setTitle(file.getName());
					AnnotationID id = annotation.getAnnotationID();
					if (!importedFiles.containsKey(id)) {
						importedFiles.put(id, annotation);
					}
				}
			}
			catch (IOException e) {
				LogUtils.info("IOexception during update file: " + uri); //$NON-NLS-1$
			}
			catch (COSRuntimeException e) {
				LogUtils.info("COSRuntimeException during update file: " + uri); //$NON-NLS-1$
			}
			catch (COSLoadException e) {
				LogUtils.info("COSLoadException during update file: " + uri); //$NON-NLS-1$
			}
		}
		return true;
	}

	private void addAnnotationsToImportedFiles(AnnotationModel annotation, NodeModel target) throws InterruptedException {
		if (canceled()) return;
		AnnotationID id = annotation.getAnnotationID();

		if (!importedFiles.containsKey(id)) {
			importedFiles.put(id, annotation);
		}

		for (AnnotationModel child : annotation.getChildren()) {
			child.setParent(annotation);
			addAnnotationsToImportedFiles(child, target);
		}
	}

	private boolean buildNodeIndex(NodeModel target) throws InterruptedException, InvocationTargetException {
		fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.27")); //$NON-NLS-1$
		for (MapModel map : monitoredMindmaps) {
			if (canceled()) return false;
			buildAnnotationNodeIndex(map.getRootNode());
		}
		buildEqualChildIndex(target.getChildren());
		if (canceled()) return false;
		return true;
	}

	private void buildAnnotationNodeIndex(NodeModel node) throws InterruptedException {
		if (canceled()) return;
		AnnotationNodeModel annotation = AnnotationController.getAnnotationNodeModel(node);

		if (annotation != null && annotation.getAnnotationID() != null) {
			AnnotationID id = annotation.getAnnotationID();
			if (!nodeIndex.containsKey(id)) {
				nodeIndex.put(id, new ArrayList<NodeModel>());
			}
			nodeIndex.get(id).add(node);
		}

		for (NodeModel child : node.getChildren()) {
			buildAnnotationNodeIndex(child);
		}
	}

	private void buildEqualChildIndex(List<NodeModel> children) throws InterruptedException {
		if (canceled()) return;
		for (NodeModel child : children) {
			if (!equalChildIndex.containsKey(child.getText())) {
				equalChildIndex.put(child.getText(), new ArrayList<NodeModel>());
			}
			equalChildIndex.get(child.getText()).add(child);
			buildEqualChildIndex(child.getChildren());
		}
	}

	private boolean setupPreconditions(NodeModel target) throws InterruptedException, InvocationTargetException {

		fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.28")); //$NON-NLS-1$
		if (canceled()) return false;
		File monitoringDirectory = MonitoringUtils.getPdfDirFromMonitoringNode(target);
		if (monitoringDirectory == null) {
			UITools.informationMessage(TextUtils.getText("AbstractMonitoringAction.29")); //$NON-NLS-1$
			return false;
		}
		CustomFileListFilter monitorFileFilter = new CustomFileListFilter(DocearController.getPropertiesController().getProperty(
				TextUtils.getText("AbstractMonitoringAction.30"))); //$NON-NLS-1$
		monitorFiles = getFilteredFileList(monitoringDirectory, monitorFileFilter, isMonitorSubDirectories(target));

		fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.31")); //$NON-NLS-1$
		if (canceled()) return false;
		Collection<URI> mindmapDirectories = MonitoringUtils.getMindmapDirFromMonitoringNode(target);
		Collection<URI> mindmapFiles = new ArrayList<URI>();
		for (URI uri : mindmapDirectories) {
			uri = URIUtils.resolveURI(URIUtils.getAbsoluteURI(target.getMap()), uri);
			File dirFile = URIUtils.getFile(uri);
			if (dirFile == null || !dirFile.exists()) continue;
			if (dirFile.isDirectory()) {
				mindmapFiles.addAll(getFilteredFileList(dirFile, new CustomFileFilter(".*[.][mM][mM]"), isMonitorSubDirectories(target))); //$NON-NLS-1$
			}
			else {
				mindmapFiles.add(uri);
			}
		}
		if (target.getMap().getFile() != null && !mindmapFiles.contains(target.getMap().getFile().toURI())) {
			mindmapFiles.add(target.getMap().getFile().toURI());
		}

		fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.33")); //$NON-NLS-1$
		if (canceled()) return false;
		monitoredMindmaps = NodeUtilities.getMapsFromUris(mindmapFiles);
		if (updateMindmaps(monitoredMindmaps)) {
			monitoredMindmaps = NodeUtilities.getMapsFromUris(mindmapFiles);
		}

		return true;
	}

	private boolean updateMindmaps(Collection<MapModel> maps) throws InterruptedException, InvocationTargetException {
		List<MapModel> mapsToUpdate = new ArrayList<MapModel>();
		for (MapModel map : maps) {
			if (DocearMapModelController.getModel(map) == null) {
				mapsToUpdate.add(map);
			}
		}

		if (mapsToUpdate.size() > 0) {
			int result = UITools.showConfirmDialog(null, getMessage(mapsToUpdate), getTitle(mapsToUpdate), JOptionPane.OK_CANCEL_OPTION);
			if (result == JOptionPane.OK_OPTION) {
				fireStatusUpdate(SwingWorkerDialog.PROGRESS_BAR_TEXT, null, TextUtils.getText("AbstractMonitoringAction.34") + mapsToUpdate.size() + TextUtils.getText("AbstractMonitoringAction.35")); //$NON-NLS-1$ //$NON-NLS-2$
				if (!MapConverter.convert(mapsToUpdate)) {
					fireStatusUpdate(SwingWorkerDialog.IS_CANCELED, null, TextUtils.getText("AbstractMonitoringAction.36")); //$NON-NLS-1$
				}
			}
			else {
				fireStatusUpdate(SwingWorkerDialog.IS_CANCELED, null, TextUtils.getText("AbstractMonitoringAction.36")); //$NON-NLS-1$
			}
			return true;
		}
		else {
			return false;
		}
	}

	private boolean isFlattenSubfolders(NodeModel target) {
		int value = NodeUtilities.getAttributeIntValue(target, PdfUtilitiesController.MON_FLATTEN_DIRS);
		switch (value) {
		default:
			return false;
		case 1:
			return true;
		}
	}

	private boolean isMonitorSubDirectories(NodeModel target) {
		int value = NodeUtilities.getAttributeIntValue(target, PdfUtilitiesController.MON_SUBDIRS);
		switch (value) {

		default:
			return false;
		case 1:
			return true;
		case 2:
			return DocearController.getPropertiesController().getBooleanProperty("docear_subdir_monitoring"); //$NON-NLS-1$
		}
	}

	private String getMessage(List<MapModel> mapsToConvert) {
		if (mapsToConvert.size() > 1) {
			return mapsToConvert.size() + TextUtils.getText("AbstractMonitoringAction.39") + TextUtils.getText("update_splmm_to_docear_explanation"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		else if (mapsToConvert.size() == 1) {
			return mapsToConvert.get(0).getTitle()
					+ TextUtils.getText("AbstractMonitoringAction.41") + TextUtils.getText("update_splmm_to_docear_explanation"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return ""; //$NON-NLS-1$
	}

	private String getTitle(List<MapModel> mapsToConvert) {
		if (mapsToConvert.size() > 1) {
			return mapsToConvert.size() + TextUtils.getText("AbstractMonitoringAction.44"); //$NON-NLS-1$
		}
		else if (mapsToConvert.size() == 1) {
			return mapsToConvert.get(0).getTitle() + TextUtils.getText("AbstractMonitoringAction.45"); //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	private void fireStatusUpdate(final String propertyName, final Object oldValue, final Object newValue) throws InterruptedException,
			InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				firePropertyChange(propertyName, oldValue, newValue);
			}
		});
	}

	private void fireProgressUpdate(final int progress) throws InterruptedException, InvocationTargetException {
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				if (progress < 0) {
					setProgress(0);
					return;
				}
				if (progress > 100) {
					setProgress(100);
					return;
				}
				setProgress(progress);
			}
		});
	}

	private List<URI> getFilteredFileList(File monitoringDir, FileFilter fileFilter, boolean readSubDirectories) {
		List<URI> result = new ArrayList<URI>();
		Collection<File> tempResult = new ArrayList<File>();
		if(monitoringDir == null || !monitoringDir.isDirectory()) return result;
		
		File[] monitorFiles = monitoringDir.listFiles(fileFilter);
		if(monitorFiles != null && monitorFiles.length > 0){
			tempResult.addAll(Arrays.asList(monitorFiles));
		}	
		for(File file : tempResult){
			result.add(file.toURI());
		}
		if(readSubDirectories){
			File[] subDirs = monitoringDir.listFiles(new DirectoryFileFilter());
			if(subDirs != null && subDirs.length > 0){
				for(File subDir : subDirs){
					result.addAll(getFilteredFileList(subDir, fileFilter, readSubDirectories));
				}
			}			
		}		
		return result;
	}
}