package org.docear.plugin.services.features.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.SwingUtilities;
import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.io.IOExceptionWithCause;
import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.io.ProgressInputStream;
import org.docear.plugin.services.ADocearServiceFeature;
import org.docear.plugin.services.DocearServiceException;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.mode.ModeController;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.client.apache.ApacheHttpClient;
import com.sun.jersey.client.apache.config.DefaultApacheHttpClientConfig;
import com.sun.jersey.core.util.StringKeyStringValueIgnoreCaseMultivaluedMap;
import com.sun.jersey.multipart.impl.MultiPartWriter;

public class DocearConnectionProvider extends ADocearServiceFeature {
	public static final int CONNECTION_TIMEOUT = 10000;
	private Object proxyAuthentication = new Object();

	private static ApacheHttpClient client = ApacheHttpClient.create();

	static {
		updateClientConfiguration(true);
	}
	
	private Map<String, String> headerMap = new LinkedHashMap<String, String>();
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	protected WebResource getWebResource(URI uri) {
		synchronized (client) {
			WebResource resource = client.resource(uri);			
			return resource;
		}

	}
	
	public WebResource getServiceResource() {		
		WebResource resource = client.resource(getOnlineServiceUri());		
		return resource;
	}
	
	private static InputStream getErrorMessageInputStream(ClientResponse response) {
		Status status = response.getClientResponseStatus();
		InputStream is = response.getEntityInputStream();
		if(status != null) {
			// rewrite http server error messages
			if(status.getStatusCode() >= 500 || status.getStatusCode() == 408 || status.getStatusCode() == 413  || status.getStatusCode() == 414) {
				is = new ByteArrayInputStream(TextUtils.getText("docear.service.error.server", "[missing translation]").getBytes());
			}
			else if(status.getStatusCode() == 404) {
				is = new ByteArrayInputStream(TextUtils.getText("docear.service.error.not_found", "[missing translation]").getBytes());
			}
		}
		return is;
	}	
	
	public URI getOnlineServiceUri() {		
		if (System.getProperty("org.docear.localhost", "false").equals("true")) {
			return URI.create("http://127.0.0.1:8080/");
		}	
		return URI.create("https://api.docear.org/");		
	}
	
	public void setDefaultHeader(String key, String value) {
		if(key == null) {
			return;
		}
		synchronized (headerMap) {
			if(value == null) {
				headerMap.remove(key);
			}
			else {
				headerMap.put(key, value);
			}
		}
	}
	
	public String getDefaultHeader(String key) {
		if(key == null) {
			return null;
		}
		synchronized (headerMap) {
			return headerMap.get(key);
		}
	}
	
	public String[] getDefaultHeaderKeys() {
		synchronized (headerMap) {
			return headerMap.keySet().toArray(new String[0]);
		}
	}
	
	public static String getErrorMessageString(ClientResponse response) {
		try {
			return readResponseContent(getErrorMessageInputStream(response));
		}
		catch (Exception e) {
		}
		return "";
	}
	
	public static String readResponseContent(InputStream is) throws IOException {
		int chr;
		StringBuilder message = new StringBuilder();
		while ((chr = is.read()) > -1) {
			message.append((char) chr);
		}
		is.close();
		return message.toString();
	}
	
	private static void updateClientConfiguration(boolean prefChanged) {
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(DocearConnectionProvider.class.getClassLoader());
		try {
			DefaultApacheHttpClientConfig cc = new DefaultApacheHttpClientConfig();
			if (DocearProxyAuthenticator.useProxyServer()) {
				String host = DocearProxyAuthenticator.getHost();
				String port = DocearProxyAuthenticator.getPort();
				String username = DocearProxyAuthenticator.getUsername();
				String proxyPassword = DocearProxyAuthenticator.getPassword();

				cc.getProperties().put(DefaultApacheHttpClientConfig.PROPERTY_PROXY_URI, "http://" + host + ":" + port + "/");
				if (username != null && username.length() > 0 && proxyPassword != null) {
					try {
						cc.getState().setProxyCredentials(AuthScope.ANY_REALM, host, Integer.parseInt(port), username, proxyPassword);
					}
					catch (NumberFormatException e) {
						LogUtils.severe(e);
					}
				}
			}
			synchronized (client) {
				if (prefChanged) {
					cc.getClasses().add(MultiPartWriter.class);
					client = ApacheHttpClient.create(cc);
					client.setConnectTimeout(CONNECTION_TIMEOUT);
					client.setReadTimeout(CONNECTION_TIMEOUT*3);
				}
				else {
					client.getProperties().put(DefaultApacheHttpClientConfig.PROPERTY_HTTP_STATE, cc.getState());
				}
			}
		}
		finally {
			Thread.currentThread().setContextClassLoader(contextClassLoader);
		}
	}
	
	public DocearServiceResponse put(String path) {
		return put(path, null);
	}

	public DocearServiceResponse put(String path, MultivaluedMap<String, String> params) {

		try {
			if (params == null) {
				params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
			}
			
			ClientResponse response = put(getServiceResource().path(path).queryParams(params), ClientResponse.class);
			try {
				Status status = response.getClientResponseStatus();
				if (status != null && status.equals(Status.OK)) {
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.OK,
							response.getEntityInputStream());
				}
				else if (status != null && status.equals(Status.NO_CONTENT)) {
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.NO_CONTENT,
							response.getEntityInputStream());
				}
				else if (status != null && status.equals(Status.UNAUTHORIZED)) {
					DocearController.getController().getEventQueue().dispatchEvent(new DocearUnauthorizedExceptionEvent(this));
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.UNAUTHORIZED,
							getErrorMessageInputStream(response));
				}
				else {
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.FAILURE,
							getErrorMessageInputStream(response));
				}
			}
			finally {
				response.close();
			}
		}
		catch (ClientHandlerException e) {
			if (e.getCause() instanceof UnknownHostException || e.getCause() instanceof NoRouteToHostException
					|| e.getCause() instanceof SocketTimeoutException || e.getCause() instanceof ConnectException) {
				return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.UNKNOWN_HOST,
						new ByteArrayInputStream(e.getMessage().getBytes()));
			}
			else {
				return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.FAILURE,
						new ByteArrayInputStream(e.getMessage().getBytes()));
			}
		}
		catch (Exception e) {
			return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.FAILURE, new ByteArrayInputStream(
					e.getMessage().getBytes()));
		}

	}
	
	protected <T> T put(Builder builder, Class<T> c) throws Exception {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new Exception("Never call the webservice from the event dispatch thread.");
		}
		synchronized (proxyAuthentication) {
			try {
				appendDefaultHeaders(builder);
				return builder.put(c);
			}
			catch (Exception e) {
				LogUtils.info(e.getCause().toString());
				try {
					if (raiseProxyCredentialsDialog(e)) {
						return put(builder, c);
					}
					else {
						throw (e);
					}
				}
				catch (Exception ex) {
					throw (ex);
				}
			}
			finally {
				client.getClientHandler().getHttpClient().getHttpConnectionManager().closeIdleConnections(100);
			}
		}
	}

	protected <T> T put(WebResource webResource, Class<T> c) throws Exception {
		return put(webResource.getRequestBuilder(), c);
	}
	
	public DocearServiceResponse post(String path, MultivaluedMap<String, String> params) {

		try {
			if (params == null) {
				params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
			}
			
			ClientResponse response = post(getServiceResource().path(path), params);
			try {
				Status status = response.getClientResponseStatus();
				if (status != null && status.equals(Status.OK)) {
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.OK,
							response.getEntityInputStream());
				}
				else if (status != null && status.equals(Status.NO_CONTENT)) {
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.NO_CONTENT,
							response.getEntityInputStream());
				}
				else if (status != null && status.equals(Status.UNAUTHORIZED)) {
					DocearController.getController().getEventQueue().dispatchEvent(new DocearUnauthorizedExceptionEvent(this));
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.UNAUTHORIZED,
							getErrorMessageInputStream(response));
				}
				else {
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.FAILURE,
							getErrorMessageInputStream(response));
				}
			}
			finally {
				response.close();
			}
		}
		catch (ClientHandlerException e) {
			if (e.getCause() instanceof UnknownHostException || e.getCause() instanceof NoRouteToHostException
					|| e.getCause() instanceof SocketTimeoutException || e.getCause() instanceof ConnectException) {
				return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.UNKNOWN_HOST,
						new ByteArrayInputStream(e.getMessage().getBytes()));
			}
			else {
				return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.FAILURE,
						new ByteArrayInputStream(e.getMessage().getBytes()));
			}
		}
		catch (Exception e) {
			return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.FAILURE, new ByteArrayInputStream(
					e.getMessage().getBytes()));
		}

	}
	
	public ClientResponse post(WebResource webResource, Object requestEntity) throws Exception {
		return post(webResource.getRequestBuilder(), requestEntity);
	}

	public ClientResponse post(Builder builder, Object requestEntity) throws Exception {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new Exception("Never call the webservice from the event dispatch thread.");
		}
		synchronized (proxyAuthentication) {
			try {
				appendDefaultHeaders(builder);
				ClientResponse response = builder.post(ClientResponse.class, requestEntity);
				try {
					Status status = response.getClientResponseStatus();
					if (status != null && status.equals(Status.UNAUTHORIZED)) {
						DocearController.getController().getEventQueue().dispatchEvent(new DocearUnauthorizedExceptionEvent(this));
					}
				}	
				catch (Exception e) {
				}
				return response;
			}
			catch (Exception e) {
				LogUtils.info(e.getCause().toString());
				try {
					if (raiseProxyCredentialsDialog(e)) {
						return post(builder, requestEntity);
					}
					else {
						throw (e);
					}
				}
				catch (Exception ex) {
					throw (ex);
				}
			}
			finally {
				client.getClientHandler().getHttpClient().getHttpConnectionManager().closeIdleConnections(200);
			}
		}
	}

	public DocearServiceResponse get(String path) {
		return get(path, null);
	}
	
	public DocearServiceResponse get(String path, MultivaluedMap<String, String> params) {
		try {
			if (params == null) {
				params = new StringKeyStringValueIgnoreCaseMultivaluedMap();
			}
			
			ClientResponse response = get(getServiceResource().path(path).queryParams(params), ClientResponse.class);
			try {
				Status status = response.getClientResponseStatus();
				if (status != null && status.equals(Status.OK)) {
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.OK,
							response.getEntityInputStream());
				}
				else if (status != null && status.equals(Status.NO_CONTENT)) {
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.NO_CONTENT,
							response.getEntityInputStream());
				}
				else if (status != null && status.equals(Status.UNAUTHORIZED)) {
					DocearController.getController().getEventQueue().dispatchEvent(new DocearUnauthorizedExceptionEvent(this));
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.UNAUTHORIZED,
							getErrorMessageInputStream(response));
				}
				else {
					return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.FAILURE,
							getErrorMessageInputStream(response));
				}
			}
			finally {
				response.close();
			}
		}
		catch (ClientHandlerException e) {
			if (e.getCause() instanceof UnknownHostException || e.getCause() instanceof NoRouteToHostException
					|| e.getCause() instanceof SocketTimeoutException || e.getCause() instanceof ConnectException) {
				return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.UNKNOWN_HOST,
						new ByteArrayInputStream(e.getMessage().getBytes()));
			}
			else {
				return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.FAILURE,
						new ByteArrayInputStream(e.getMessage().getBytes()));
			}
		}
		catch (Exception e) {
			return new DocearServiceResponse(org.docear.plugin.services.features.io.DocearServiceResponse.Status.FAILURE, new ByteArrayInputStream(
					e.getMessage().getBytes()));
		}
	}
	
	protected <T> T get(WebResource webResource, Class<T> c) throws Exception {
		return get(webResource.getRequestBuilder(), c);
	}

	protected <T> T get(Builder builder, Class<T> c) throws Exception {
		if (SwingUtilities.isEventDispatchThread()) {
			throw new IOException("Never call the webservice from the event dispatch thread.");
		}
		synchronized (proxyAuthentication) {
			try {
				appendDefaultHeaders(builder);
				return builder.get(c);
			}
			catch (Exception e) {
				LogUtils.info(e.getCause().toString());
				try {
					if (raiseProxyCredentialsDialog(e)) {
						return get(builder, c);
					}
					else {
						throw (e);
					}
				}
				catch (Exception ex) {
					throw (ex);
				}
			}
			finally {
				client.getClientHandler().getHttpClient().getHttpConnectionManager().closeIdleConnections(100);
			}
		}
	}
	
	
	
	private void appendDefaultHeaders(Builder builder) {
		synchronized (headerMap) {
			for(Entry<String, String> entry : headerMap.entrySet()) {
				builder.header(entry.getKey(), entry.getValue());
			}
		}
	}

	private boolean raiseProxyCredentialsDialog(Exception e) throws Exception {
		return false;
		//does not work currently
//		if (e instanceof ClientHandlerException 
//				&& e.getCause() != null 
//				&& e.getCause().getCause() != null 
//				&& e.getCause().getCause() instanceof IOException 
//				&& DocearProxyAuthenticator.useProxyServer()) {
//			
//			if(!DocearProxyAuthenticator.requestAuthenticationData()) {
//				throw e;
//			}
//			return true;
//		}
//		return false;
	}

	public InputStream getDownloadStream(URI uri) throws IOException, DocearServiceException {
		URL url = uri.toURL();
		InputStream inStream = null;
		int length = 0;
		if("ftp".equals(url.getProtocol())) {
			URLConnection conn;
			if(DocearProxyAuthenticator.useProxyServer()) {
				String host = DocearProxyAuthenticator.getHost();
				String port = DocearProxyAuthenticator.getPort();
				if(port == null) {
					port = "1080"; //SOCKS DEFAULT
				}
				conn = url.openConnection(new Proxy(Type.HTTP, new InetSocketAddress(host, Integer.parseInt(port))));
			}
			else {
				conn = url.openConnection();
			}
			inStream = conn.getInputStream();
			length = inStream.available()-1;
		}
		else 
		{
			try {
				WebResource webResource = getWebResource(uri);
				ClientResponse response = get(webResource, ClientResponse.class);
				inStream = response.getEntityInputStream();
				length = response.getLength();
			} 
			catch (Exception e) {
				throw new IOExceptionWithCause(e);
			}
		}
		return new ProgressInputStream(inStream, uri.toURL(), length);
	}

	@Override
	protected void installDefaults(ModeController modeController) {
	}

	@Override
	public void shutdown() {
	}
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
}
