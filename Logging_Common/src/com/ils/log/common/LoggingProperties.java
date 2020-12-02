package com.ils.log.common;
/**
 *   (c) 2020  ILS Automation. All rights reserved.  
 */

/**
 *  Define properties that are common to all scopes.
 */
public interface LoggingProperties   {
	public final static String MODULE_ID = "logging";         // See module-logging.xml
	public final static String MODULE_NAME = "Logging";       // See build-logging.xml
	
	public final static String PROPERTIES_SCRIPT_PACKAGE = "system.ils.log.properties";
	public final static String CRASH_BUFFER_SIZE = "CRASH_BUFFER_SIZE";
	public final static String LOGGING_DATASOURCE = "LOGGING_DATASOURCE";
}
