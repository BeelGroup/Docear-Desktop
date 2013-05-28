package org.freeplane.core.user;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.mode.Controller;

public class UserAccountController implements IExtension {
	
	private final List<IUserAccountChangeListener> listeners = new ArrayList<IUserAccountChangeListener>();
	
	private IUserAccount activeUser;
	
	
	public static void install(Controller controller) {
		controller.addExtension(UserAccountController.class, new UserAccountController());
	}
	
	public static UserAccountController getController() {
		return Controller.getCurrentController().getExtension(UserAccountController.class);
	}
	
	public void setActiveUser(IUserAccount user) {
		if(user != this.activeUser) {
			fireDeactivate(this.activeUser);
			this.activeUser = user;
			fireActivated(this.activeUser);
		}
		
	}
	
	public IUserAccount getActiveUser() {
		return this.activeUser;
	}
	
	public List<IUserAccount> getUsers() {
		if(activeUser == null) {
			return Collections.emptyList();
		}
		return Arrays.asList(new IUserAccount[]{activeUser});
	}
	
	public void addUserAccountChangeListener(IUserAccountChangeListener listener) {
		if(listener == null) {
			return;
		}
		synchronized (listeners) {
			if(!listeners.contains(listener)) {
				listeners.add(0, listener);
			}
		}
	}
	
	public void removeUserAccountChangeListener(IUserAccountChangeListener listener) {
		if(listener == null) {
			return;
		}
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	private void fireActivated(IUserAccount user) {
		UserAccountChangeEvent evt = new UserAccountChangeEvent(this, user);
		synchronized (listeners) {
			for (IUserAccountChangeListener listener : listeners) {
				listener.activated(evt);
			}
		}
		
	}

	private void fireDeactivate(IUserAccount user) {
		UserAccountChangeEvent evt = new UserAccountChangeEvent(this, user);
		synchronized (listeners) {
			for (IUserAccountChangeListener listener : listeners) {
				listener.aboutToDeactivate(evt);
			}
		}
	}

	
	
}
