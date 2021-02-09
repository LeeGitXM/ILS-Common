/**
 *   (c) 2020  ILS Automation. All rights reserved.
 *  
 *   Based on sample code in the IA-scripting-module
 *   by Travis Cox.
 */
package com.ils.module.client;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.MDC;
import org.slf4j.LoggerFactory;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
import com.ils.logging.common.CommonProperties;
import com.ils.logging.common.LoggingHookInterface;
import com.ils.logging.common.filter.PatternFilter;
import com.inductiveautomation.ignition.client.gateway_interface.GatewayConnectionManager;
import com.inductiveautomation.ignition.client.model.ClientContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.turbo.TurboFilter;

/**
 *  This class exposes functions to access Gateway parameters from
 *  either client or designer scopes. We basically implement the SystemPropertiesInterface,
 *  except all methods here are static.
 *  
 *  Remote procedure calls are made to the Gateway scope to produce the changes.
 */
public class ClientScriptFunctions  {
	private static String CLSS = "ClientScriptFunctions: ";
	private static LoggingHookInterface hook = null;
	private static ClientContext context = null;
	private static List<String> verboten = new ArrayList<>();
	
	static {
		verboten.add("OutputConsole");
	}
	
	public static void setContext(ClientContext c )     { context = c; }
	public static void setHook(LoggingHookInterface h ) { hook = h; }
	
	/**
	 * @return the named logger
	 */
	public static ILSLogger getLogger(String name) {
		ILSLogger logger = LogMaker.getLogger(name);
		if( context!=null) {
			logger.setProject(context.getProject().getName());
		}
		if( hook!=null) {
			logger.setClientId(hook.getClientId());
		}
		return logger;
	}
	/**
	 * @return the buffer size for the crash logging appender.
	 */
	public static int getCrashAppenderBufferSize()throws Exception{
		int size = -1;
		try {
			size = (int)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "getCrashAppenderBufferSize" );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.getCrashAppenderBufferSize: GatewayException (%s)",CLSS,ge.getMessage()));
		}
		return size;
	}
	
	/**
	 * @return the desired buffer size for the crash logging appender.
	 */
	public static int getGatewayCrashAppenderBufferSize()throws Exception{
		int size = -1;
		try {
			size = (int)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "getGatewayCrashAppenderBufferSize" );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.getGatewayCrashAppenderBufferSize GatewayException (%s)",CLSS,ge.getMessage()));
		}
		return size;
	}
	/**
	 * @return a list of names of loggers known to the Gateway scope. The names are returned from the
	 *         gateway in a single comma-separated string.
	 */
	public static List<String> getGatewayLoggerNames() {
		List<String> list = new ArrayList<>();
		try {
			String concatentedNames = (String)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "getGatewayLoggerNamesAsString" );
			String[] names = concatentedNames.split(",");
			for(  String name:names) {
				list.add(name);
			}
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.getGatewayLoggerNames: GatewayException (%s)",CLSS,ge.getMessage()));
		}
		return list;
	}
	/**
	 * @param loggerName
	 * @return the current level of a logger in Gateway scope
	 */
	public static String getGatewayLoggingLevel(String loggerName) {
		String level = "";
		try {
			level = (String)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "getGatewayLoggingLevel", loggerName);
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.getGatewayLoggingLevel: GatewayException (%s)",CLSS,ge.getMessage()));
		}
		return level;
	}
	/**
	 * @return the directory path to the Ignition installation directory holding jar files.
	 */
	public static String getLibDir()throws Exception{
		String type = "";
		try {
			type = (String)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "getLibDir" );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.getLibDir: GatewayException (%s)",CLSS,ge.getMessage()));
		}
		return type;
	}
	/**
	 * @return a list of names of loggers known in your current scope
	 */
	public static List<String> getLoggerNames() {
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
		String result = "";
		ILSLogger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			Level level = lgr.getLevel();
			if( level!=null ) result = level.toString();
		}
		return result;
	}
	/**
	 * @return the datasource string for the database connection that will hold
	 *         the log messages
	 */
	public static String getLoggingDatasource() throws Exception{
		String type = "";
		try {
			type = (String)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "getLoggingDatasource" );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.getLoggingDatasource: GatewayException (%s)",CLSS,ge.getMessage()));
		}
		return type;
	}
	/**
	 * @return the directory path to the Ignition installation directory holding jar files.
	 */
	public static String getLogsDir()throws Exception{
		String type = "";
		try {
			type = (String)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "getLogsDir" );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.getLogsDir: GatewayException (%s)",CLSS,ge.getMessage()));
		}
		return type;
	}
	/**
	 * @return the directory path to the Ignition installation directory holding jar files.
	 */
	public static String getUserLibDir()throws Exception{
		String type = "";
		try {
			type = (String)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "getUserLibDir" );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.getUserLibDir: GatewayException (%s)",CLSS,ge.getMessage()));
		}
		return type;
	}
	/**
	 * @return the directory path to the Ignition installation directory holding jar files.
	 */
	public static String getWindowsBrowserPath()throws Exception{
		String path = "";
		try {
			path = (String)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "getWindowsBrowserPath" );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.getWindowsBrowserPath: GatewayException (%s)",CLSS,ge.getMessage()));
		}
		return path;
	}
	/**
	 * @param threadName
	 */
	public static void passGatewayLogsOnThread(String threadName) {
		try {
			GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "passGatewayLogsOnThread", threadName );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.passGatewayLogsOnThread: GatewayException (%s)",CLSS,ge.getMessage()));
		}
	}
	/**
	 * All log messages from loggers whose name contains the supplied string will be sent to the appender, regardless of level.
	 * This method can be called multiple times with different patterns.
	 * @param pattern
	 */
	public static void passGatewayPattern(String pattern)  {
		try {
			GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "passGatewayPattern", pattern );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.passGatewayPattern: GatewayException (%s)",CLSS,ge.getMessage()));
		}
	}
	/**
	 * All log messages on the same thread as the caller regardless of level will be sent to the log context filtered by the PassThru filter. 
	 * We expect this to be applied to the database appender.
	 */
	public static void passLogsOnCurrentThread() {
		LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		List<TurboFilter> list = logContext.getTurboFilterList();
		for(TurboFilter filter:list) {
			if( filter instanceof PatternFilter ) {
				((PatternFilter)filter).passCurrentThread();
				break;
			}
		}
	}
	/**
	 * All log messages on the named thread regardless of level will be sent to the appender with the PassThru filter. Thread names 
	 * associated with existing messages can be found in the database table. Other than that it's hard to know.
	 * @param threadName
	 */
	public static void passLogsOnThread(String threadName) {
		hook.getPatternFilter().addThread(threadName);
	}
	/**
	 * All log messages from loggers whose name contains the supplied string will be sent to the appender, regardless of level.  
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
		try {
			GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "resetGatewayPatternFilter" );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.resetGatewayPatternFilter: GatewayException (%s)",CLSS,ge.getMessage()));
		}
	}
	/**
	 * Reset the "Pattern" filter to again respect logging levels
	 */
	public static void resetPatternFilter() {
		hook.getPatternFilter().reset();
	}
	/**
	 * Specify the number of messages to retain in the local crash appender circular buffer.
	 */
	public static void setCrashAppenderBufferSize(int size) {
		hook.setCrashBufferSize(size);
	}
	/**
	 * Specify the number of messages to retain in the gateway crash appender circular buffer.
	 */
	public static void setGatewayCrashAppenderBufferSize(int size) {
		try {
			GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "setGatewayCrashAppenderBufferSize",size );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.setGatewayCrashAppenderBufferSize: GatewayException (%s)",CLSS,ge.getMessage()));
		}
	}
	/**
	 * Set the level of a logger in Gateway scope. The level is changed for everyone and persists until changed again.
	 * @param loggerName
	 * @param level name
	 */
	public static void setGatewayLoggingLevel(String loggerName, String level) {
		try {
			GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					CommonProperties.MODULE_ID, "setGatewayLoggingLevel", loggerName, level );
		}
		catch(Exception ge) {
			System.out.println(String.format("%s.setGatewayLoggingLevel: GatewayException (%s)",CLSS,ge.getMessage()));
		}
	}
	/**
	 * Set a level: ERROR, WARN, INFO, DEBUG, TRACE in your current scope on the named logger.
	 * There is a list of loggers that cause system hangs, if changed to more verbose that INFO.
	 * Do not allow these loggers to be set at all.
	 * @param loggerName
	 * @param level name
	 */
	public static void setLoggingLevel(String loggerName, String level) {
		if( verboten.contains(loggerName)) {
			System.out.println(String.format("%s.setLoggingLevel: Setting the level of %s is prohibited",CLSS,loggerName));
			return;
		}
		ILSLogger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			Level lvl = Level.toLevel(level.toUpperCase());
			lgr.setLevel(lvl);
		}
	}

}
