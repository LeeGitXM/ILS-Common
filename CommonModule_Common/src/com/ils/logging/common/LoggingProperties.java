package com.ils.logging.common;

import ch.qos.logback.classic.Level;

/**
 *   (c) 2020-2021  ILS Automation. All rights reserved.  
 */

/**
 *  Define properties that are common to all scopes.
 */
public interface LoggingProperties   {
	public final static String MODULE_ID = "ils.common";         // See common-module.xml
	public final static String MODULE_NAME = "CommonModule";     // See build-common-module.xml
	
	public final static String PROPERTIES_SCRIPT_PACKAGE = "system.ils.log.properties";
	public final static String CRASH_APPENDER_THRESHOLD = "debug";
	public final static String CRASH_BUFFER_SIZE = "CRASH_BUFFER_SIZE";
	public final static String LOGGING_DATASOURCE = "LOGGING_DATASOURCE";
	
	public final static int DEFAULT_CRASH_BUFFER_SIZE = 500;
	public final static String DEFAULT_CRASH_APPENDER_THRESHOLD = Level.DEBUG.levelStr;
	
	public final static String CRASH_APPENDER_NAME = "CrashAppender";
	public final static String DB_APPENDER_NAME    = "DBAppender"; 
}
