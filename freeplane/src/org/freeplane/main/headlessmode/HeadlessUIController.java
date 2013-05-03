/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry
 *
 *  This file author is Dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.main.headlessmode;

import java.awt.Component;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Rectangle;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.RootPaneContainer;

import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.ui.ViewController;

/**
 * @author Dimitry Polivaev
 * 24.12.2012
 */
public class HeadlessUIController implements ViewController {
	final private AtomicLong workingThreadId = new AtomicLong();
	final private ExecutorService worker = Executors.newSingleThreadExecutor(new ThreadFactory() {
		public Thread newThread(Runnable r) {
			final Thread thread = Executors.defaultThreadFactory().newThread(r);
			workingThreadId.set(thread.getId());
			return thread;
		}
	}) ;
	public Rectangle getFrameSize() {
		return new Rectangle();
	}

	public void setFrameSize(Rectangle frameSize) {
		
	}

	public void changeNoteWindowLocation() {
		
	}

	public void err(String msg) {
		
	}

	public RootPaneContainer getRootPaneContainer() {
		return null;
	}

	public Container getContentPane() {
		return null;
	}

	public Frame getFrame() {
		return null;
	}

	public FreeplaneMenuBar getFreeplaneMenuBar() {
		return null;
	}

	public JFrame getJFrame() {
		return null;
	}

	public JComponent getStatusBar() {
		return null;
	}

	public void init(Controller controller) {
		
	}

	public void insertComponentIntoSplitPane(JComponent noteViewerComponent) {
		
	}

	public boolean isApplet() {
		return false;
	}

	public boolean isMenubarVisible() {
		return false;
	}

	public void openDocument(URI uri) throws IOException {
		
	}

	public void openDocument(URL fileToUrl) throws Exception {
		
	}

	public void out(String msg) {
		
	}

	public void addStatusInfo(String key, String info) {
		
	}

	public void addStatusInfo(String key, Icon icon) {
		
	}

	public void addStatusInfo(String key, String info, Icon icon) {
		
	}

	public void addStatusInfo(String key, String info, Icon icon, String tooltip) {
		
	}

	public void addStatusComponent(String key, Component component) {
		
	}

	public void removeStatus(String key) {
		
	}

	public void removeSplitPane() {
		
	}

	public void saveProperties() {
		
	}

	public void selectMode(ModeController oldModeController, ModeController newModeController) {
		
	}

	public void setMenubarVisible(boolean visible) {
		
	}

	public void setTitle(String title) {
		
	}

	public void setWaitingCursor(boolean b) {
		
	}

	public void viewNumberChanged(int number) {
		
	}

	public String completeVisiblePropertyKey(JComponent toolBar) {
		return "";
	}

	public void addObjectTypeInfo(Object value) {
		
	}

	public boolean quit() {
		return true;
	}

	public boolean isDispatchThread() {
		return workingThreadId.get() == Thread.currentThread().getId();
    }

	public void invokeLater(Runnable runnable) {
		worker.execute(runnable);
    }

	public void invokeAndWait(Runnable runnable) throws InterruptedException, InvocationTargetException, ExecutionException {
	    worker.submit(runnable).get();
	    
    }

	public boolean isHeadless() {
	    return true;
    }

	public boolean areScrollbarsVisible() {
		return false;
	}

	public void setScrollbarsVisible(boolean b) {
		
	}
	
	
}