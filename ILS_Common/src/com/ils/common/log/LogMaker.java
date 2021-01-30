/**
 *   (c) 2020-2021  ILS Automation. All rights reserved.
 */
package com.ils.common.log;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * LogMaker is a thin wrapper around LoggerFactory basically providing
 * the convenience of adding a project marker to the log.
 *
 * MDC = Mapped Diagnostic Contexts 
 */
public class LogMaker {
	public static final String CLIENT_KEY = "client";     // client id
	public static final String FILE_KEY = "file";         // name of source file
	public static final String FUNCTION_KEY = "function";   // python function or java method
	public static final String LINE_KEY = "linenumber";     // line number in code
	public static final String MODULE_KEY = "module";       // python module or java package
	public static final String PROJECT_KEY = "project";     // project name
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
		MDC.put(PROJECT_KEY, project);
		Logger lgr;
		if( source instanceof String) {
			lgr =  logContext.getLogger((String)source);
		}
		else {
			lgr =  logContext.getLogger(source.getClass());
		}
		return new ILSLogger(lgr);
	}
}
