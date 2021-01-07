/**
 *   (c) 2020  ILS Automation. All rights reserved.
 */
package com.ils.logging.common;

import org.apache.log4j.MDC;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 * LogMaker is a thin wrapper around LoggerFactory basically providing
 * the convenience of adding a project marker to the log.
 *
 */
public class LogMaker {
	public static final String CLIENT_KEY = "client";     // client id
	public static final String PROJECT_KEY = "project";   // Project name
	private static final LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
	
	public static Logger getLogger(Object source) {	
		if( source instanceof String) {
			return logContext.getLogger((String)source);
		}
		return logContext.getLogger(source.getClass());
	}
	
	public static Logger getLogger(Object source,String project) {	
		MDC.put(PROJECT_KEY, project);
		if( source instanceof String) {
			return logContext.getLogger((String)source);
		}
		return logContext.getLogger(source.getClass());
	}
}
