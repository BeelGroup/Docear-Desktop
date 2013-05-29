package org.docear.plugin.services.features.user;

import java.io.IOException;

import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.services.DocearServiceException;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.IDocearServiceFeature;
import org.docear.plugin.services.features.io.DocearConnectionProvider;
import org.freeplane.core.resources.ResourceController;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class DocearUserController implements IDocearServiceFeature {
	public static DocearUser LOCAL_USER = new DocearUser() {
		public String getName() { 
			return "local";
		};
	};
	static DocearUserController ctrl;
	
	public final static String DOCEAR_CONNECTION_USERNAME_PROPERTY = "docear.service.connect.username";
	public final static String DOCEAR_CONNECTION_TOKEN_PROPERTY = "docear.service.connect.token";
	
	public DocearUserController() {
		String name = ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_USERNAME_PROPERTY);
		DocearUser user = loadUser(name);
		user.activate();
	}
	
	public DocearUser loadUser(String name) {
		if(name == null) {
			return LOCAL_USER;
		}
		else {
			DocearUser user = new DocearUser();
			user.setUsername(name);
			String token = ResourceController.getResourceController().getProperty(DOCEAR_CONNECTION_TOKEN_PROPERTY);
			user.setAccessToken(token);
			return user;
		}
	}
	
	public static DocearUserController getController() {
		if(ctrl == null) {
			ctrl = new DocearUserController();
		}
		return ctrl;
	}
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/
	
	

	public void loginUser(DocearUser user) throws DocearServiceException {
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
				user.setAccessToken(token);
				user.setOnline(true);
			}
			else {
				throw new DocearServiceException(DocearConnectionProvider.getErrorMessageString(response));
			}
		} catch (IOException e) {
			throw new DocearServiceException(e.getMessage());
		} 
		finally {
			response.close();
		}
		
	}

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
