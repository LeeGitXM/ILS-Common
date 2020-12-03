/**
 *   (c) 2020  ILS Automation. All rights reserved.
 *  
 */
package com.ils.log.gateway;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.ils.common.log.LogMaker;
import com.ils.logging.common.filter.PassThruFilter;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class GatewayScriptFunctions  {
	private static GatewayContext context = null;
	private static LoggingGatewayHook hook = null;

	public static void setContext(GatewayContext ctx) {
		context = ctx;
	}
	public static void setHook(LoggingGatewayHook h ) { hook = h; }


	/**
	 * @return the datasource holding the logging database.
	 */
	public static String getLoggingDatasource() { return hook.getLoggingDatasource(); }
	
	public static String getLibDir() { return context.getLibDir().getAbsolutePath(); }
	/**
	 */
	public static String getLogsDir() { return context.getLogsDir().getAbsolutePath(); }  
	/**
	 */
	public static String getUserLibDir() { return context.getUserlibDir().getAbsolutePath(); }
	
	/**
	 * @return a list of names of loggers known in your current scope (simply defer to the Gateway method).
	 */
	public static List<String> getLoggerNames() {
		return getGatewayLoggerNames();
	}
	/**
	 * @return a list of names of loggers known to the Gateway scope.
	 */
	public static List<String> getGatewayLoggerNames() {
		LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		List<Logger> loggers = logContext.getLoggerList();
		List<String> list = new ArrayList<>();
		for(Logger lgr:loggers) {
			list.add(lgr.getName());
		}
		return list;
	}
	
	/**
	 * @param loggerName
	 * @return the current level of a logger
	 */
	public static String getLoggingLevel(String loggerName) {
		return getGatewayLoggingLevel(loggerName);
	}
	/**
	 * @param loggerName
	 * @return the current level of a logger in Gateway scope
	 */
	public static String getGatewayLoggingLevel(String loggerName) {
		String level = "";
		Logger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			level = lgr.getLevel().toString();
		}
		return level;
	}
	
	/**
	 * Set a level: ERROR, WARN, INFO, DEBUG, TRACE in your current scope on the named logger.
	 * @param loggerName
	 * @param level name
	 */
	public static void setLoggingLevel(String loggerName, String level) {
		setGatewayLoggingLevel(loggerName,level);
	}
	/**
	 * Set the level of a logger in Gateway scope. The level is changed for everyone and persists until changed again.
	 * @param loggerName
	 * @param level name
	 */
	public static void setGatewayLoggingLevel(String loggerName, String level) {
		Logger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			Level lvl = Level.toLevel(level.toUpperCase());
			lgr.setLevel(lvl);
		}
	}

	/**
	 * All log messages on the named thread regardless of level will be sent to the appender with the PassThru filter. Thread names 
	 * associated with existing messages can be found in the database table. Other than that it's hard to know.
	 * @param threadName
	 */
	public static void passAllLogsOnThread(String threadName) {
		passAllGatewayLogsOnThread(threadName);
	}
	/**
	 * @param threadName
	 */
	public static void passAllGatewayLogsOnThread(String threadName) {
		hook.getPassThruFilter().addThread(threadName);
	}
	
	/**
	 * All log messages from loggers whose name contains the supplied string will be sent to the appender, regardless of level.  
	 * (We could get fancier and use a regular expression, but I think this is easier on the user). 
	 * Multiple calls just add more messages. This state persists until the whole filter is reset. An empty string will pass all messages. 
	 * It doesn'f matter if the same logger matches multiple criteria, only one message will be sent to the appender
	 * @param pattern
	 */
	public static void passAllLogs(String pattern) {
		passAllGatewayLogs(pattern);
	}
	/**
	 * All log messages from loggers whose name contains the supplied string will be sent to the appender, regardless of level.
	 * This method can be called multiple times with different patterns.
	 * @param pattern
	 */
	public static void passAllGatewayLogs(String pattern)  {
		hook.getPassThruFilter().addPattern(pattern);
	}
	/**
	 * Reset the "PassAll" filter to again respect logging levels
	 */
	public static void resetPassAllFilter() {
		resetGatewayPassAllFilter();
	}
	/**
	 * Reset the Gateway "PassAll" filter to again respect logging levels
	 */
	public static void resetGatewayPassAllFilter() {
		hook.getPassThruFilter().reset();
	}
}