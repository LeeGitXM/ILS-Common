/**
 *   (c) 2020 ILS Automation. All rights reserved.
 *  
 *   Based on sample code in the IA-scripting-module
 *   by Travis Cox.
 */
package com.ils.logging.common;

import java.util.List;

/**
 *  Define the methods that support the ILS CommonModule. Most of these deal with logging.
 */
public interface ModulePropertiesInterface   { 
	/**
	 * @return the datasource string for the database connection that will hold
	 *         the log messages
	 */
	public String getLoggingDatasource();
	/**
	 * @return the buffer size for the crash appender in the current scope
	 */
	public int getCrashAppenderBufferSize();
	/**
	 * @return the buffer size for the gateway crash appender
	 */
	public int getGatewayCrashAppenderBufferSize();
	/**
	 * @return a list of names of loggers known to the Gateway scope
	 */
	public List<String> getGatewayLoggerNames();
	/**
	 * @param loggerName
	 * @return the current threshold level of a logger in Gateway scope
	 */
	public String getGatewayLoggingLevel(String loggerName);
	/**
	 */
	public String getLibDir();
	/**
	 * @param loggerName
	 * @return the threshold level for message propagation for of logger in the current application scope
	 */
	public String getLoggingLevel(String loggerName); 
	/**
	 * @return a list of names of loggers known in your current scope
	 */
	public List<String> getLoggerNames();
	/**
	 * @return the absolute path to the "logs" sub-directory of the current Ignition installation
	 */
	public String getLogsDir();
	/**
	 * @return retention times for database messages by severity: ERROR,WARNING,INFO,DEBUG and TRACE.
	 *         
	 */
	public double[] getRetentionTimes();
	/**
	 * The browser path is used for Windows systems only. Mac and Linux systems used the default browser
	 * and require no configuration.
	 * 
	 * @return the execution path for the browser used to display context-sensitive help.
	 */
	public String getWindowsBrowserPath();
	/**
	 * @return the absolute path to the "user-lib" sub-directory of the current Ignition installation
	 */
	public String getUserLibDir(); 
	/**
	 * 
	 * @param threadName
	 */
	public void passGatewayLogsOnThread(String threadName);
	/**
	 * All log messages from loggers whose name contains the supplied string will be sent to the appender, regardless of level.
	 * This applies to loggers running in Gateway scope.
	 * @param pattern
	 */
	public void passGatewayPattern(String pattern);
	/**
	 * All log messages on the same thread as the caller regardless of level will be sent to the logger context filtered by the pattern filter. 
	 * We expect this to be applied to the database appender.
	 */
	public void passLogsOnCurrentThread();
	/**
	 * All log messages on the named thread regardless of level will be sent to the logging context filtered by a pattern filter. Thread names 
	 * associated with existing messages can be found in the database table. Other than that it's hard to know. This method applies to
	 * logging in the current application scope.
	 * @param threadName
	 */
	public void passLogsOnThread(String threadName);
	/**
	 * All log messages from loggers whose name contains the supplied string will be sent to the appenders, regardless of level.  
	 * (We could get fancier and use a regular expression, but I think this is easier on the user). 
	 * Multiple calls just add more messages. This state persists until the whole filter is reset. An empty string will pass all messages. 
	 * It doesn'f matter if the same logger matches multiple criteria, only one message will be sent to the appender
	 * @param pattern
	 */
	public void passPattern( String pattern);
	/**
	 * Reset the Gateway pattern filter to again respect logging levels
	 */
	public void resetGatewayPatternFilter();
	/**
	 * Reset the pattern filter to again respect threshold levels of individual loggers
	 */
	public void resetPatternFilter();
	/**
	 * Set the size of the local crash appender's circular buffer.
	 */
	public void setCrashAppenderBufferSize(int size);
	/**
	 * Set the size of the Gateway crash filter circular buffer.
	 */
	public void setGatewayCrashAppenderBufferSize(int size);
	/**
	 * Set the level of a logger in Gateway scope. The level is changed for everyone and persists until changed again.
	 * @param loggerName
	 * @param level name
	 */
	public void setGatewayLoggingLevel(String loggerName, String level);
	/**
	 * Set a level: ERROR, WARN, INFO, DEBUG, TRACE on the named logger in the current scope .
	 * @param loggerName
	 * @param level name
	 */
	public void setLoggingLevel(String loggerName, String level);
	/**
	 * @return true to indicate desire to use the database logging and crash reported features.
	 */
	public boolean useDatabaseAppender();
}
