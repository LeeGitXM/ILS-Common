package com.ils.logging.common;

import ch.qos.logback.classic.Level;

/**
 *   (c) 2020-2021  ILS Automation. All rights reserved.  
 */

/**
 *  Define properties for the ILS CommonModule that are common to all scopes.
 */
public interface CommonProperties   {
	public final static String MODULE_ID = "ils.common";         // See common-module.xml
	public final static String MODULE_NAME = "CommonModule";     // See build-common-module.xml
	public final static String PROPERTIES_SCRIPT_PACKAGE = "system.ils.log.properties";
	
	// Logging
	// These are property keys from ils_logback.xml 
	public final static String CRASH_APPENDER_THRESHOLD = "CRASH_APPENDER_THRESHOLD"; // Key for threshold string
	public final static String CRASH_BUFFER_SIZE   = "CRASH_BUFFER_SIZE";
	public final static String LOGGING_DATASOURCE  = "LOGGING_DATASOURCE";
	public final static String RETENTION_TIME_DEBUG  = "DEBUG_RETENTION_DAYS";
	public final static String RETENTION_TIME_ERROR  = "ERROR_RETENTION_DAYS";
	public final static String RETENTION_TIME_INFO   = "INFO_RETENTION_DAYS";
	public final static String RETENTION_TIME_TRACE  = "TRACE_RETENTION_DAYS";
	public final static String RETENTION_TIME_WARNING = "WARNING_RETENTION_DAYS";
	public final static String USE_DATABASE_APPENDER = "USE_DATABASE_APPENDER";
	
	public final static String CRASH_APPENDER_NAME = "CrashAppender";
	public final static String DB_APPENDER_NAME    = "DBAppender";
	public final static Level DEFAULT_CRASH_APPENDER_THRESHOLD = Level.DEBUG;
	public final static int DEFAULT_CRASH_BUFFER_SIZE = 500;
	
	public static final double ERROR_DEFAULT_RETENTION = 30.;   // Days
	public static final double WARNING_DEFAULT_RETENTION = 30.;  
	public static final double INFO_DEFAULT_RETENTION = 14.;   
	public static final double DEBUG_DEFAULT_RETENTION = 1.;   
	public static final double TRACE_DEFAULT_RETENTION = 1.;
	
	public final static String DEFAULT_APPENDER_PATTERN = "%d{HH:mm:ss.SSS} %level %logger{35} %msg%n";
}
