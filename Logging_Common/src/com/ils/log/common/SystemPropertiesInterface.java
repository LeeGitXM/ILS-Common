/**
 *   (c) 2020 ILS Automation. All rights reserved.
 *  
 *   Based on sample code in the IA-scripting-module
 *   by Travis Cox.
 */
package com.ils.log.common;

import java.util.List;

/**
 *  Define the methods supported by both client and gateway sides of this feature.
 *  These are all properties configured in the gateway module properties page.
 */
public interface SystemPropertiesInterface   { 
	/**
	 * @return the datasource string
	 */
	public String getLoggingDatasource();
	/**
	 * @return the desired buffer size for the crash appender
	 */
	public int getCrashBufferSize();
	/**
	 */
	public String getLibDir();
	/**
	 */
	public String getLogsDir();  
	/**
	 */
	public String getUserLibDir(); 
	/**
	 * @return a list of names of loggers known in your current scope
	 */
	public List getLoggerNames();

	/**
	 * @return a list of names of loggers known to the Gateway scope
	 */
	public List getGatewayLoggerNames();
	/**
	 * @param loggerName
	 * @return the current level of a logger
	 */
	public String getLoggingLevel(String loggerName); 

	/**
	 * Set a level: ERROR, WARN, INFO, DEBUG, TRACE in your current scope on the named logger.
	 * @param loggerName
	 * @param level name
	 */
	public void setLoggingLevel(String loggerName, String level);
	/**
	 * All log messages on the same thread as the caller regardless of level will be sent to the appender filtered by the PassThru filter. 
	 * We expect this to be applied to the database appender.
	 */
	public void passAllLogsOnCurrentThread();

	/**
	 * All log messages on the named thread regardless of level will be sent to the appender with the PassThru filter. Thread names 
	 * associated with existing messages can be found in the database table. Other than that it's hard to know.
	 * @param threadName
	 */
	public void passAllLogsOnThread(String threadName);
	/**
	 * All log messages from loggers whose name contains the supplied string will be sent to the appender, regardless of level.  
	 * (We could get fancier and use a regular ecxpression, but I think this is easier on the user). 
	 * Multiple calls just add more messages. This state persists until the whole filter is reset. An empty string will pass all messages. 
	 * It doesn'f matter if the same logger matches multiple criteria, only one message will be sent to the appender
	 * @param pattern
	 */
	public void passAllLogs( String pattern);
	/**
	 * Reset the "PassAll" filter to again respect logging levels
	 */
	public void resetPassAllFilter();

	// Then same for the Gateway ...
	
	/**
	 * @param loggerName
	 * @return the current level of a logger in Gateway scope
	 */
	public String getGatewayLoggingLevel(String loggerName);
	/**
	 * Set the level of a logger in Gateway scope. The level is changed for everyone and persists until changed again.
	 * @param loggerName
	 * @param level name
	 */
	public void setGatewayLoggingLevel(String loggerName, String level);
	/**
	 * 
	 * @param threadName
	 */
	public void passAllGatewayLogsOnThread(String threadName);
	/**
	 * All log messages from loggers whose name contains the supplied string will be sent to the appender, regardless of level.
	 * @param pattern
	 */
	public void passAllGatewayLogs(String pattern);

	/**
	 * Reset the Gateway "PassAll" filter to again respect logging levels
	 */
	public void resetGatewayPassAllFilter();
}
