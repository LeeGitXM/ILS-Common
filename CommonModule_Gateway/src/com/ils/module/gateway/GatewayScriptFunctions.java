/**
 *   (c) 2020  ILS Automation. All rights reserved.
 *  
 */
package com.ils.module.gateway;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

public class GatewayScriptFunctions  {
	private static String CLSS = "GatewayScriptFunctions: ";
	private static GatewayContext context = null;
	private static ILSGatewayHook hook = null;
	private static List<String> verboten = new ArrayList<>();
			
	static {
		verboten.add("OutputConsole");
	}
	
	public static void setContext(GatewayContext ctx) {
		context = ctx;
	}
	public static void setHook(ILSGatewayHook h ) { hook = h; }

	/**
	 * @return the named logger
	 */
	public static ILSLogger getLogger(String name) {
		return LogMaker.getLogger(name);
	}
	/**
	 * @return the buffer size for the crash logging appender.
	 */
	public static int getCrashAppenderBufferSize()throws Exception{
		return hook.getCrashAppender().getBufferSize();
	}
	/**
	 * @return the buffer size for the crash logging appender.
	 */
	public static int getGatewayCrashAppenderBufferSize()throws Exception{
		return hook.getCrashAppender().getBufferSize();
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
	 * @return the current level of a logger in Gateway scope
	 */
	public static String getGatewayLoggingLevel(String loggerName) {
		String level = "";
		ILSLogger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			level = lgr.getLevel().toString();
		}
		return level;
	}
	public static String getLibDir() { return context.getLibDir().getAbsolutePath(); }
	/**
	 * @return a list of names of loggers known in your current scope (simply defer to the Gateway method).
	 */
	public static List<String> getLoggerNames() {
		return getGatewayLoggerNames();
	}
	/**
	 * @param loggerName
	 * @return the current level of a logger
	 */
	public static String getLoggingLevel(String loggerName) {
		return getGatewayLoggingLevel(loggerName);
	}
	/**
	 * @return the datasource holding the logging database.
	 */
	public static String getLoggingDatasource() { return hook.getLoggingDatasource(); }
	/**
	 */
	public static String getLogsDir() { return context.getLogsDir().getAbsolutePath(); }
	/**
	 */
	public static String getWindowsBrowserPath() { return hook.getWindowsBrowserPath(); }
	/**
	 */
	public static String getUserLibDir() { return context.getUserlibDir().getAbsolutePath(); }
	/**
	 */
	public static void passGatewayLogsOnCurrentThread() {
		hook.getPatternFilter().passCurrentThread();
	}
	/**
	 * @param threadName
	 */
	public static void passGatewayLogsOnThread(String threadName) {
		hook.getPatternFilter().addThread(threadName);
	}
	/**
	 * All log messages from loggers whose name contains the supplied string will be sent to the appender, regardless of level.
	 * This method can be called multiple times with different patterns.
	 * @param pattern
	 */
	public static void passGatewayPattern(String pattern)  {
		hook.getPatternFilter().addPattern(pattern);
	}
	/**
	 * All log messages on the named thread regardless of level will be sent to the appender with the PassThru filter. Thread names 
	 * associated with existing messages can be found in the database table. Other than that it's hard to know.
	 * @param threadName
	 */
	public static void passLogsOnCurrentThread() {
		passGatewayLogsOnCurrentThread();
	}

	/**
	 * All log messages on the named thread regardless of level will be sent to the appender with the PassThru filter. Thread names 
	 * associated with existing messages can be found in the database table. Other than that it's hard to know.
	 * @param threadName
	 */
	public static void passLogsOnThread(String threadName) {
		passGatewayLogsOnThread(threadName);
	}
	/**
	 * Log messages from loggers whose name contains the supplied string will be sent to the appender, regardless of level.  
	 * (We could get fancier and use a regular expression, but I think this is easier on the user). 
	 * Multiple calls just add more messages. This state persists until the whole filter is reset. An empty string will pass all messages. 
	 * It doesn'f matter if the same logger matches multiple criteria, only one message will be sent to the appender
	 * @param pattern
	 */
	public static void passPattern(String pattern) {
		hook.getPatternFilter().addPattern(pattern);
	}
	/**
	 * Reset the Gateway "Pattern" filter to again respect logging levels
	 */
	public static void resetGatewayPatternFilter() {
		hook.getPatternFilter().reset();
	}
	/**
	 * Reset the Gateway "Pattern" filter in the Gateway to again respect logging levels
	 */
	public static void resetPatternFilter() {
		hook.getPatternFilter().reset();
	}
	/**
	 * Specify the number of messages to retain in the local crash appender circular buffer.
	 */
	public static void setCrashAppenderBufferSize(int size) {
		hook.getCrashAppender().setBufferSize(size);
	}
	/**
	 * Specify the number of messages to retain in the gateway crash appender circular buffer.
	 */
	public static void setGatewayCrashAppenderBufferSize(int size) {
		hook.getCrashAppender().setBufferSize(size);
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
		if( verboten.contains(loggerName)) {
			System.out.println(String.format("%s.setLoggingLevel Setting the level of %s is prohibited",CLSS,loggerName));
			return;
		}
		ILSLogger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			Level lvl = Level.toLevel(level.toUpperCase());
			lgr.setLevel(lvl);
		}
	}
}