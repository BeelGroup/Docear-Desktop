package org.freeplane.plugin.remote.server;

import org.slf4j.LoggerFactory;


public class Logger {

	private static Logger instance;
	
	private final org.slf4j.Logger logger;

	private Logger() {
		//change class loader
		final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(Activator.class.getClassLoader());
		//create logger
		logger = (ch.qos.logback.classic.Logger)LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
		

		//set back to original class loader
		Thread.currentThread().setContextClassLoader(contextClassLoader);
	}
	
	private static Logger getInstance() {
		if(instance == null) {
			instance = new Logger();
		}
		return instance;
	}
	
	public static org.slf4j.Logger getLogger() {
		return getInstance().logger;
	}
}
