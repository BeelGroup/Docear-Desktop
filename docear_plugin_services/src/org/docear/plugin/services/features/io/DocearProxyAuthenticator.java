package org.docear.plugin.services.features.io;

import java.awt.EventQueue;
import java.net.Authenticator;
import java.net.PasswordAuthentication;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.services.features.io.view.ProxyAuthenticationPanel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

public class DocearProxyAuthenticator extends Authenticator {
	public static final String DOCEAR_PROXY_PORT = "docear.proxy_port";
	public static final String DOCEAR_PROXY_HOST = "docear.proxy_host";
	public static final String DOCEAR_USE_PROXY = "docear.use_proxy";
	public static final String DOCEAR_PROXY_USERNAME = "docear.proxy_username";
	
	
	private static boolean alreadyCanceled = false;	
	private static boolean okSelected = false;
	
	private static char[] proxyPassword = null;

	public static void showDialog() {
		showDialog(false);
	}
	public static void showDialog(boolean forced){
		okSelected = false;
		if(EventQueue.isDispatchThread()) {
			return;
		}
		if(!isProxyCanceled() || forced) {
			ProxyAuthenticationPanel panel = new ProxyAuthenticationPanel();
			boolean wasSelected = panel.getChckbxUseProxy().isSelected();
			int result = JOptionPane.showConfirmDialog(null, panel, TextUtils.getText("docear.proxy.connect.dialog.title"), JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			if(result == JOptionPane.OK_OPTION){
				DocearController.getPropertiesController().setProperty(DocearProxyAuthenticator.DOCEAR_USE_PROXY, panel.getChckbxUseProxy().isSelected());
				DocearController.getPropertiesController().setProperty(DocearProxyAuthenticator.DOCEAR_PROXY_HOST, panel.getHostField().getText());
				DocearController.getPropertiesController().setProperty(DocearProxyAuthenticator.DOCEAR_PROXY_PORT, panel.getPortField().getText());
				DocearController.getPropertiesController().setProperty(DocearProxyAuthenticator.DOCEAR_PROXY_USERNAME, panel.getUsernameField().getText());
				setPassword(panel.getPasswordField().getPassword());
				if(wasSelected!=panel.getChckbxUseProxy().isSelected()) {
					
				}
				okSelected = true;
				setProxyCanceled(false);
				return;
			}
			setProxyCanceled(true);
		}
	}

	private static boolean isProxyCanceled() {
		return alreadyCanceled;
	}

	private static void setProxyCanceled(boolean b) {
		alreadyCanceled = b;
	}

	public static void setPassword(char[] password) {
		proxyPassword = password;
	}
	
	public static boolean useProxyServer() {
		return false;
		// proxy seems not to work properly
//		return Boolean.parseBoolean(ResourceController.getResourceController().getProperty(DOCEAR_USE_PROXY, "false"));
	}

	public static String getHost() {
		return ResourceController.getResourceController().getProperty(DOCEAR_PROXY_HOST, "");
	}

	public static String getPort() {
		return ResourceController.getResourceController().getProperty(DOCEAR_PROXY_PORT, "");
	}

	public static String getUsername() {
		return ResourceController.getResourceController().getProperty(DOCEAR_PROXY_USERNAME, "");
	}
	
	public static String getPassword() {
		return new String(proxyPassword);
	}

	public static boolean requestAuthenticationData() {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					showDialog();
				}
			});
			return okSelected;
		} catch (Exception e) {
			LogUtils.warn(e);
		}
		return false;
	}
	
	protected PasswordAuthentication getPasswordAuthentication() {
		LogUtils.info(getRequestorType() + " (" + this.getRequestingHost() + ":" + this.getRequestingPort() + "): " +this.getRequestingPrompt() + " "+ this.getRequestingProtocol() + "/" + getRequestingScheme()+" for "+ getRequestingURL() +"   "+getRequestingSite());
		String username = ResourceController.getResourceController().getProperty(DOCEAR_PROXY_USERNAME);
		char[] password = proxyPassword;
		if(username == null || password == null) {
			requestAuthenticationData();
			username = ResourceController.getResourceController().getProperty(DOCEAR_PROXY_USERNAME);
			password = proxyPassword;
		}
    	return new PasswordAuthentication(username, password);
	}

}
