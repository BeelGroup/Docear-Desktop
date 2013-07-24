package org.docear.plugin.services.features.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.ws.rs.core.MediaType;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.services.DocearServiceException;
import org.docear.plugin.services.DocearServiceException.DocearServiceExceptionType;
import org.docear.plugin.services.ServiceController;
import org.freeplane.core.util.LogUtils;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.multipart.FormDataMultiPart;


public class FiletransferClient {
	//private final static Client client;
	public static final String START_UPLOAD = "docear.service.upload.start";
	public static final String STOP_UPLOAD = "docear.service.upload.stop";
	public static final String NO_CONNECTION = "docear.service.connection.problem";
	
		

	private WebResource serviceResource;
	
	public FiletransferClient(String restPath) {
		serviceResource = ServiceController.getConnectionController().getServiceResource();
		serviceResource = serviceResource.path("/user/" + ServiceController.getCurrentUser().getUsername() + "/" + restPath);
	}
	
	public boolean sendFile(File file, boolean deleteIfTransferred) throws DocearServiceException {
		if (!ServiceController.getCurrentUser().isTransmissionEnabled() || !ServiceController.getCurrentUser().isOnline() || file == null) {
			return false;
		}
		DocearController.getController().getEventQueue().dispatchEvent(new DocearEvent(this.getClass(), START_UPLOAD));
		FileInputStream inStream = null;
		try {
			inStream = new FileInputStream(file);
			int size = inStream.available();
			byte[] data = new byte[size];
			if(inStream.read(data) == size) {
				FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
				formDataMultiPart.field("file", data, MediaType.APPLICATION_OCTET_STREAM_TYPE);					
				
				ClientResponse response = ServiceController.getConnectionController().post(serviceResource.type(MediaType.MULTIPART_FORM_DATA_TYPE), formDataMultiPart);
				try {
					if(response==null || !response.getClientResponseStatus().equals(ClientResponse.Status.OK)) {
						//System.out.println(response.getEntity(String.class));
						
						throw new IOException("file upload not accepted ("+ response+"; "+ServiceController.getConnectionController().getDefaultHeader("accessToken")+"):"+response.getEntity(String.class));
					}
					else if (deleteIfTransferred) {
						try {
							inStream.close();
							response.close();
						}
						catch (Exception e) {
						}
						System.gc();
						file.delete();
					}
				}
				finally {
					response.close();
				}
			}
			else {
				throw new IOException("incomplete read ("+file.getPath()+")");
			}
		}
		catch(ClientHandlerException ex) {
			DocearController.getController().getEventQueue().dispatchEvent(new DocearEvent(this.getClass(), NO_CONNECTION));
			throw new DocearServiceException("no connection to the server", DocearServiceExceptionType.NO_CONNECTION);
		}
		catch (Exception ex) {
			LogUtils.warn("Could not upload "+ file.getPath(), ex);
			DocearController.getController().getEventQueue().dispatchEvent(new DocearEvent(this.getClass(), STOP_UPLOAD));
			return false;
		}
		finally {
			try {
				inStream.close();
			} 
			catch (Exception e) {				
			}
		}
		DocearController.getController().getEventQueue().dispatchEvent(new DocearEvent(this.getClass(), STOP_UPLOAD));
		return true;
	}
	
	public boolean sendFiles(File[] files, boolean deleteIfTransferred) throws DocearServiceException {
		for(File file : files) {
			if(!ServiceController.getCurrentUser().isTransmissionEnabled()) {
				break;
			}
			if(!sendFile(file, deleteIfTransferred)) {
				return false;
			}
		}		
		return true;		
	}
}
