/**
 *   (c) 2020-2021  ILS Automation. All rights reserved.
 */
package com.ils.common.log;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * LogMaker is a thin wrapper around LoggerFactory basically providing
 * the convenience of adding a project marker to the log. Care must be taken 
 * if there are multiple projects, so that the MDC values of project/client
 * are updated just before logging.
 *
 * MDC = Mapped Diagnostic Contexts 
 */
public class LogMaker {
	public static final String CLIENT_KEY = "client";     // client id
	public static final String FUNCTION_KEY = "function";   // python function or java method
	public static final String LINE_KEY = "linenumber";     // line number in code
	public static final String MODULE_KEY = "module";       // python module or java package
	public static final String PROJECT_KEY = "project";     // project name
	// Marker names used by the filters
	public final static String CRASH_MARKER_NAME            = "crash";        // Marks an event destined for the crash logger only.
	public final static String LOOP_PREVENTION_MARKER_NAME  = "no-loop";      // Used to prevent circular processing during logging
	
	private static final LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
	
	public static ILSLogger getLogger(Object source) {	
		Logger lgr;
		if( source instanceof String) {
			lgr =  logContext.getLogger((String)source);
		}
		else {
			lgr =  logContext.getLogger(source.getClass());
		}
		return new ILSLogger(lgr);
	}
	
	public static ILSLogger getLogger(Object source,String project) {	
		Logger lgr;
		if( source instanceof String) {
			lgr =  logContext.getLogger((String)source);
		}
		else {
			lgr =  logContext.getLogger(source.getClass());
		}
		ILSLogger ilogger = new ILSLogger(lgr);
		ilogger.setProject(project);
		return ilogger;
	}
}
