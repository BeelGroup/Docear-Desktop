package org.docear.plugin.services.features.user;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.MultivaluedMap;

import org.docear.plugin.services.DocearServiceException;
import org.docear.plugin.services.DocearServiceException.DocearServiceExceptionType;
import org.docear.plugin.services.ServiceController;
import org.docear.plugin.services.features.io.DocearConnectionProvider;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.omg.CORBA.portable.UnknownException;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;

public class AccountRegisterer {
	private static final int USER_TYPE_REGISTERED = 2;
//	private static final int USER_TYPE_ANONYMOUS = 3;
	
	private class TaskState {
		public Exception ex = null;
	}
	
	public AccountRegisterer() {
	}

	public void createRegisteredUser(String name, String password, String email, Boolean newsLetter) throws DocearServiceException {
		createUser(name, password, email, null, newsLetter, null);
	}


	private void createUser(final String name, final String password, final String email, final Integer birthYear, final Boolean newsLetter, final Boolean isMale) throws DocearServiceException {
		
		ExecutorService execSrv = Executors.newSingleThreadExecutor();
		Future<TaskState> future = execSrv.submit(new Callable<TaskState>() {
			public TaskState call() throws Exception {
				TaskState state = new TaskState();
				final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
				try {
					Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
					MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
					queryParams.add("userName", name);
					queryParams.add("password", password);
					queryParams.add("retypedPassword", password);
					queryParams.add("userType", "" + USER_TYPE_REGISTERED);
					queryParams.add("eMail", email);
					queryParams.add("firstName", null);
					queryParams.add("middleName", null);
					queryParams.add("lastName", null);
					queryParams.add("birthYear", null);
					queryParams.add("generalNewsLetter", newsLetter == null ? null : newsLetter.toString());
					queryParams.add("isMale", isMale == null ? null : isMale.toString());
		
					WebResource res = ServiceController.getConnectionController().getServiceResource().path("/user/" + name);
					long time = System.currentTimeMillis();
					ClientResponse response = ServiceController.getConnectionController().post(res, queryParams);
					LogUtils.info("user registration took (ms): "+(System.currentTimeMillis()-time));
					if(Thread.interrupted()) {
						throw new DocearServiceException("request aborted");
					}
					try {
						if (response.getClientResponseStatus() != Status.OK) {
							throw new DocearServiceException(DocearConnectionProvider.getErrorMessageString(response));
						}
					}
					finally {
						response.close();
					}
				}
				catch (DocearServiceException e) {
					LogUtils.info("DocearServiceException in AccountRegisterer.createUser().Future: "+e.getMessage());
					state.ex = e;
				}
				catch (ClientHandlerException e) {
					LogUtils.info("ClientHandlerException in AccountRegisterer.createUser().Future: "+e.getMessage());
					state.ex = new DocearServiceException(TextUtils.getText("docear.service.connect.no_connection"), DocearServiceExceptionType.NO_CONNECTION);
				}
				catch (Exception e) {
					LogUtils.info("Exception in AccountRegisterer.createUser().Future: "+e.getMessage());
					state.ex = new DocearServiceException(TextUtils.getText("docear.service.connect.unknown_error"));
				}
				finally {
					Thread.currentThread().setContextClassLoader(contextClassLoader);
				}
				return state;
			}
		});
		long time = System.currentTimeMillis();
		TaskState ts = null;
		try {			
			ts = future.get(DocearConnectionProvider.CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			future.cancel(true);
			execSrv.shutdown();
			throw new DocearServiceException("registration failed because of: interrupted", DocearServiceExceptionType.SIGNUP_FAILED);
		} catch (ExecutionException e) {
			future.cancel(true);
			execSrv.shutdown();
			throw new DocearServiceException("registration failed because of: "+e.getMessage(), DocearServiceExceptionType.SIGNUP_FAILED);
		} catch (TimeoutException e) {
			future.cancel(true);
			execSrv.shutdown();
			throw new DocearServiceException("registration failed because of: timeout", DocearServiceExceptionType.SIGNUP_FAILED);
		}
		finally {
			LogUtils.info("user creation wait (ms): "+(System.currentTimeMillis()-time));
		}
		
		
		if(ts.ex != null) {
			if(ts.ex instanceof DocearServiceException) {
				throw (DocearServiceException)ts.ex;
			}
			throw new UnknownException(ts.ex);
			
		}
	}
}
