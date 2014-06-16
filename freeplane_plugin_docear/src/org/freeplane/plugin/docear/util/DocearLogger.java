package org.freeplane.plugin.docear.util;

import org.freeplane.core.util.LogUtils;

public final class DocearLogger {
	
	public static void log(String msg) {
		info("[DocearCore] ", msg);
	}
	
	public static void info(String info) {
		info("[DocearCore] ", info);
	}
	
	public static void info(String prefix, String info) {
		LogUtils.info(prefix+info);
	}
	
	public static void warn(String msg) {
		warn("[DocearCore] ", msg);
	}
	
	public static void warn(String msg, Throwable cause) {
		warn("[DocearCore] ", msg, cause);
	}
	
	public static void warn(String prefix, String msg) {
		LogUtils.warn(prefix+msg);
	}
	
	public static void warn(String prefix, String msg, Throwable cause) {
		LogUtils.warn(prefix+msg, cause);
	}
	
	public static void warn(Throwable cause) {
		LogUtils.warn(cause);
	}
	
	public static void error(String msg) {
		error("[DocearCore] ", msg);
	}
	
	public static void error(String msg, Throwable cause) {
		error("[DocearCore] ", msg, cause);
	}
	
	public static void error(String prefix, String msg) {
		LogUtils.severe(prefix+msg);
	}
	
	public static void error(String prefix, String msg, Throwable cause) {
		LogUtils.severe(prefix+msg, cause);
	}
	
	public static void error(Throwable cause) {
		LogUtils.severe(cause);
	}
}
