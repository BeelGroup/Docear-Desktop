package org.docear.plugin.services.features.user;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.IDocearEventListener;
import org.docear.plugin.core.logging.DocearLogger;
import org.docear.plugin.services.ADocearServiceFeature;
import org.docear.plugin.services.DocearServiceException;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.io.DocearConnectionProvider;
import org.docear.plugin.services.features.io.DocearUnauthorizedExceptionEvent;
import org.docear.plugin.services.features.user.action.DocearUserLoginAction;
import org.docear.plugin.services.features.user.action.DocearUserRegistrationAction;
import org.docear.plugin.services.features.user.action.DocearUserServicesAction;
import org.docear.plugin.services.features.user.view.WorkspaceDocearServiceConnectionBar;
import org.docear.plugin.services.features.user.view.WorkspaceDocearServiceConnectionBar.CONNECTION_STATE;
import org.docear.plugin.services.features.user.workspace.DocearWorkspaceSettings;
import org.freeplane.core.user.IUserAccountChangeListener;
import org.freeplane.core.user.UserAccountChangeEvent;
import org.freeplane.core.user.UserAccountController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.ModeController;
import org.freeplane.plugin.workspace.WorkspaceController;
import org.freeplane.plugin.workspace.components.TreeView;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DocearUserController extends ADocearServiceFeature {
	
	public static DocearUser LOCAL_USER = new DocearLocalUser();
	private PropertyChangeListener userPropertyListener;
	private AccountRegisterer registerer = new AccountRegisterer();

	
	public final static String DOCEAR_CONNECTION_USERNAME_PROPERTY = "docear.service.connect.username";
	
	private final WorkspaceDocearServiceConnectionBar connectionBar = new WorkspaceDocearServiceConnectionBar();

	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	public DocearUserController() {
		initListeners();
		setupLastUser();
	}

	protected void setupLastUser() {
		String name = DocearController.getPropertiesController().getProperty(DOCEAR_CONNECTION_USERNAME_PROPERTY);//ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_USERNAME_PROPERTY);
		DocearUser user = loadUser(name);
		user.activate();
	}
	
	public boolean createUserAccount(DocearUser user) throws DocearServiceException {
		if(user != null) {
			registerer.createRegisteredUser(user.getUsername(), user.getPassword(), user.getEmail(), user.isNewsletterEnabled());
			user.setNew();
			return true;
		}
		return false;
	}
	

	public boolean loginUser(DocearUser user) throws DocearServiceException {
		if(user == null || LOCAL_USER.equals(user) || user.getPassword() == null) {
			return false;
		}
		if(user.isValid()) {
			user.setOnline(true);
			return false;
		}
		MultivaluedMap<String, String> formParams = new MultivaluedMapImpl();
		formParams.add("password", user.getPassword());
		Status status = null;
		
		WebResource webRes = ServiceController.getConnectionController().getServiceResource().path("/authenticate/" + user.getName());
		ClientResponse response;
		try {
			response = ServiceController.getConnectionController().post(webRes, formParams);
		}
		catch (Exception e) {
			throw new DocearServiceException(e.getMessage());
		}
		
		try {
			status = response.getClientResponseStatus();
		
			if (Status.OK.equals(status)) {
				String token = response.getHeaders().getFirst("accessToken");
				DocearConnectionProvider.readResponseContent(response.getEntityInputStream());
				user.setEnabled(true);
				user.setAccessToken(token);
				user.setOnline(true);
				if (!user.isTransmissionEnabled()) {
					user.toggleTransmissionEnabled();
				}
				return true;
			}
			else {
				throw new DocearServiceException(DocearConnectionProvider.getErrorMessageString(response));
			}
		} catch (Exception e) {
			throw new DocearServiceException(e.getMessage());
		} 
		finally {
			response.close();
		}
		
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	private void adjustInfoBarConnectionState(DocearUser user) {
		if (user.getAccessToken() != null && user.getAccessToken().trim().length() > 0) {
			connectionBar.setUsername(user.getName());
			connectionBar.setEnabled(true);
			if (user.isTransmissionEnabled()) {
				if(user.isOnline()) {
					connectionBar.setConnectionState(CONNECTION_STATE.CONNECTED);
				}
				else {
					connectionBar.setConnectionState(CONNECTION_STATE.DISCONNECTED);
				}
			}
			else {
				connectionBar.setConnectionState(CONNECTION_STATE.DISABLED);
			}
		}
		else {
			connectionBar.setUsername("");
			connectionBar.setConnectionState(CONNECTION_STATE.NO_CREDENTIALS);
			connectionBar.setEnabled(false);
		}
	}
	
	public static DocearUser getActiveUser() {
		if(UserAccountController.getController().getActiveUser() instanceof DocearUser) {
			return (DocearUser) UserAccountController.getController().getActiveUser();
		}
		else {
			if(UserAccountController.getController().getActiveUser() == null) {
				return LOCAL_USER;
			}
			return new DocearUser(UserAccountController.getController().getActiveUser());
		}
	}
	
	private void initListeners() {
		DocearController.getController().getEventQueue().addEventListener(new IDocearEventListener() {

			public void handleEvent(DocearEvent event) {
				if(event.getEventObject() instanceof LoadWorkspaceEvent) {
					DocearController.getController().getEventQueue().invoke((Runnable) event.getEventObject());
					//((Runnable) event.getEventObject()).run();
				}
				else if (event.getSource().equals(connectionBar) && WorkspaceDocearServiceConnectionBar.ACTION_COMMAND_TOGGLE_CONNECTION_STATE.equals(event.getEventObject())) {
					DocearUser user = getActiveUser();
					user.toggleTransmissionEnabled();
				}
				else if(WorkspaceDocearServiceConnectionBar.CONNECTION_BAR_CLICKED.equals(event.getSource()) ) {
					if(DocearUserController.LOCAL_USER.equals(DocearUserController.getActiveUser())) {
						WorkspaceController.getAction(DocearUserRegistrationAction.KEY).actionPerformed(null);
					}
					else {
						WorkspaceController.getAction(DocearUserServicesAction.KEY).actionPerformed(null);
					}
				}
				else if(event instanceof DocearUnauthorizedExceptionEvent) {
					DocearUser user = getActiveUser();
					if(user!= null && user.getAccessToken() != null && user.getAccessToken().trim().length() > 0) {
						DocearController.getController().getEventQueue().invoke(new Runnable() {
							public void run() {
								DocearUserLoginAction.showLoginWizard(TextUtils.getText("docear.service.unauthorized"));
							}
						});
					}
				}
			}
		});
		
		UserAccountController.getController().addUserAccountChangeListener(new IUserAccountChangeListener() {
			
			public void activated(UserAccountChangeEvent event) {
				if(event.getUser() instanceof DocearUser) {
					DocearUser user = (DocearUser) event.getUser();
					user.addPropertyChangeListener(getUserPropertyChangeListener());
					try {
						try {
							ServiceController.getFeature(DocearWorkspaceSettings.class).loadSettings(user);
						} catch (IOException e) {
							DocearLogger.warn(e);
						}
						if(!loginUser(user) && user.isValid()) {
							//onlineCheck(user);
						}
					} catch (DocearServiceException e) {
						LogUtils.warn(e);
					}
					if(event.getUser() instanceof DocearLocalUser) {
						DocearController.getPropertiesController().setProperty(DOCEAR_CONNECTION_USERNAME_PROPERTY, "");
					}
					else {
						DocearController.getPropertiesController().setProperty(DOCEAR_CONNECTION_USERNAME_PROPERTY, event.getUser().getName());
					}
					adjustInfoBarConnectionState((DocearUser) event.getUser());
					ServiceController.getConnectionController().setDefaultHeader("accessToken", ((DocearUser) event.getUser()).getAccessToken());
					DocearController.getController().getEventQueue().invoke(new DocearEvent(this, new LoadWorkspaceEvent()));
				}
				else {
					getActiveUser().activate();
				}
			}
			
			public void aboutToDeactivate(UserAccountChangeEvent event) {
				if(event.getUser() == null) {
					return;
				}
				if(event.getUser() instanceof DocearUser) {
					((DocearUser)event.getUser()).setOnline(false);
					WorkspaceController.save();
					WorkspaceController.clear();
				}
				event.getUser().removePropertyChangeListener(getUserPropertyChangeListener());
			}
		});
	}
	
	private PropertyChangeListener getUserPropertyChangeListener() {
		if(userPropertyListener == null) {
			userPropertyListener = new PropertyChangeListener() {
				public void propertyChange(PropertyChangeEvent evt) {
					DocearUser user = getActiveUser();
					if (DocearUser.USERNAME_PROPERTY.equals(evt.getPropertyName())) {
						connectionBar.setUsername(String.valueOf(evt.getNewValue()));
					}
					else if (DocearUser.TRANSMISSION_PROPERTY.equals(evt.getPropertyName())) {
						connectionBar.allowTransmission(user.isTransmissionEnabled());
					}
					else if (DocearUser.IS_ONLINE_PROPERTY.equals(evt.getPropertyName())) {
						connectionBar.setConnectionState(CONNECTION_STATE.CONNECTED);
					}
					else if (DocearUser.ACCESS_TOKEN_PROPERTY.equals(evt.getPropertyName())) {
						if(evt.getNewValue() != null) {
							ServiceController.getConnectionController().setDefaultHeader("accessToken", String.valueOf(evt.getNewValue()));
						}
						else {
							ServiceController.getConnectionController().setDefaultHeader("accessToken", null);
						}
					}
					adjustInfoBarConnectionState(user);
				}
			};
		}
		return userPropertyListener;
	}

	public void installView(ModeController modeController) {
		setupView(modeController);
	}
	
	private void setupView(ModeController modeController) {
		TreeView view = ((TreeView)WorkspaceController.getModeExtension(modeController).getView());
		if(view != null) {
			//TODO SERVICE
			if(DocearController.getController().isServiceAvailable()){
				view.addToolBar(connectionBar, TreeView.BOTTOM_TOOLBAR_STACK);
			}
		}
	}

	public DocearUser loadUser(String name) {
		if(name == null || name.trim().isEmpty()) {
			return LOCAL_USER;
		}
		else {
			DocearUser user = new DocearUser();
			user.setUsername(name);
			user.setEnabled(true);
			try {
				ServiceController.getFeature(DocearWorkspaceSettings.class).loadSettings(user);
			} catch (IOException e) {
				LogUtils.severe("Exception in org.docear.plugin.services.features.user.DocearUserController.loadUser(name):"+e.getMessage());
			}
			return user;
		}
	}


	@Override
	protected void installDefaults(ModeController modeController) {
		WorkspaceController.addAction(new DocearUserLoginAction());
		WorkspaceController.addAction(new DocearUserRegistrationAction());
		WorkspaceController.addAction(new DocearUserServicesAction());
	}


	@Override
	public void shutdown() {
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
}
