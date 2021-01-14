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
	public final static String CRASH_APPENDER_THRESHOLD = "CRASH_APPENDER_THRESHOLD"; // Key for threshold string
	public final static String CRASH_BUFFER_SIZE   = "CRASH_BUFFER_SIZE";
	public final static String LOGGING_DATASOURCE  = "LOGGING_DATASOURCE";
	public final static String CRASH_APPENDER_NAME = "CrashAppender";
	public final static String DB_APPENDER_NAME    = "DBAppender";
	public final static Level DEFAULT_CRASH_APPENDER_THRESHOLD = Level.DEBUG;
	public final static int DEFAULT_CRASH_BUFFER_SIZE = 500;
	public final static String LOOP_PREVENTION_MARKER_NAME  = "no-loop";        // Used to prevent circular processing during logging
}
