package org.docear.plugin.core.logging;

import org.freeplane.core.util.LogUtils;

public class DocearLogger {

	public static void info(Throwable cause) {
		LogUtils.info("Excepton in "+getExceptionTrace(cause)+": "+cause.getMessage());
	}
	
	public static void info(String msg) {
		LogUtils.info(msg);
	}
	
	public static void warn(String msg) {
		LogUtils.warn(msg);
	}
	
	public static void warn(Throwable cause) {
		LogUtils.warn("Excepton in "+getExceptionTrace(cause)+": "+cause.getMessage());
	}
	
	public static void error(Throwable cause) {
		LogUtils.severe("Excepton in "+getExceptionTrace(cause)+": "+cause.getMessage());
	}
	
	public static void error(String msg) {
		LogUtils.severe(msg);
	}
	
	public static String getExceptionTrace(final Throwable cause) {
		StringBuilder trace = new StringBuilder();
		StringBuilder nesting = new StringBuilder();
		if(cause != null) {
			int trials = 10;
			Throwable actualCause = cause;
			while((trials-- > 0) 
					&& actualCause.getCause() != null 
					&& actualCause.getCause() != actualCause) {
				nesting.append("\n\tover ");
				appendTopTraceElement(nesting, actualCause);
				actualCause = actualCause.getCause();
			}
			appendTopTraceElement(trace, actualCause);
			trace.append(nesting.toString());
		}
		return trace.toString();
	}

	private static void appendTopTraceElement(StringBuilder trace, Throwable cause) {
		StackTraceElement[] elements = cause.getStackTrace();
		if(elements != null && elements.length > 0) {
			trace.append(elements[0].getClassName());
			trace.append(".");
			trace.append(elements[0].getMethodName());
			trace.append(" line ");
			trace.append(elements[0].getLineNumber());
		}
	}

}
