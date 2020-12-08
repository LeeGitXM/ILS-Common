/**
 *   (c) 2020  ILS Automation. All rights reserved.
 *  
 *   Based on sample code in the IA-scripting-module
 *   by Travis Cox.
 */
package com.ils.log.client;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.ils.common.log.LogMaker;
import com.ils.log.common.LoggingProperties;
import com.ils.logging.common.filter.PassThruFilter;
import com.inductiveautomation.ignition.client.gateway_interface.GatewayConnectionManager;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

/**
 *  This class exposes functions to access Gateway parameters from
 *  either client or designer scopes. We basically implement the SystemPropertiesInterface,
 *  except all methods here are static.
 *  
 *  Remote procedure calls are made to the Gateway scope to produce the changes.
 */
public class ClientScriptFunctions  {
	private static String CLSS = "GatewayDelegate: ";
	private static final Logger log;
	private static PassThruFilter filter = null;
	
	static {
		log = LogMaker.getLogger(ClientScriptFunctions.class.getCanonicalName());
	}
	
	public static void setFilter(PassThruFilter f ) { filter = f; }
	/**
	 * @return the desired buffer size for the crash logging appender.
	 */
	public static int getCrashBufferSize()throws Exception{
		int size = -1;
		try {
			size = (int)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					LoggingProperties.MODULE_ID, "getCrashBufferSize" );
		}
		catch(Exception ge) {
			log.debug(CLSS+"getCrashBufferSize: GatewayException ("+ge.getMessage()+")");
		}
		return size;
	}
	/**
	 * @return the datasource holding the logging database.
	 */
	public static String getLoggingDatasource()throws Exception{
		String type = "";
		try {
			type = (String)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					LoggingProperties.MODULE_ID, "getLoggingDatasource" );
		}
		catch(Exception ge) {
			log.debug(CLSS+"getLoggingDatasource: GatewayException ("+ge.getMessage()+")");
		}
		return type;
	}
	/**
	 * @return the directory path to the Ignition installation directory holding jar files.
	 */
	public static String getLibDir()throws Exception{
		String type = "";
		try {
			type = (String)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					LoggingProperties.MODULE_ID, "getLibDir" );
		}
		catch(Exception ge) {
			log.debug(CLSS+"getLibDir: GatewayException ("+ge.getMessage()+")");
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
					LoggingProperties.MODULE_ID, "getLogsDir" );
		}
		catch(Exception ge) {
			log.debug(CLSS+"getLogsDir: GatewayException ("+ge.getMessage()+")");
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
					LoggingProperties.MODULE_ID, "getUserLibDir" );
		}
		catch(Exception ge) {
			log.debug(CLSS+"getUserLibDir: GatewayException ("+ge.getMessage()+")");
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
	 * @return a list of names of loggers known to the Gateway scope. The names are returned from the
	 *         gateway in a single comma-separated string.
	 */
	public static List<String> getGatewayLoggerNames() {
		List<String> list = new ArrayList<>();
		try {
			String concatentedNames = (String)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					LoggingProperties.MODULE_ID, "getGatewayLoggerNamesAsString" );
			String[] names = concatentedNames.split(",");
			for(  String name:names) {
				list.add(name);
			}
		}
		catch(Exception ge) {
			log.debug(CLSS+"getGatewayLoggerNames: GatewayException ("+ge.getMessage()+")");
		}
		return list;
	}
	
	/**
	 * @param loggerName
	 * @return the current level of a logger
	 */
	public static String getLoggingLevel(String loggerName) {
		String result = "";
		Logger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			result = lgr.getLevel().toString();
		}
		return result;
	}

	/**
	 * Set a level: ERROR, WARN, INFO, DEBUG, TRACE in your current scope on the named logger.
	 * @param loggerName
	 * @param level name
	 */
	public static void setLoggingLevel(String loggerName, String level) {
		Logger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			Level lvl = Level.toLevel(level.toUpperCase());
			lgr.setLevel(lvl);
		}
	}
	/**
	 * All log messages on the same thread as the caller regardless of level will be sent to the appender filtered by the PassThru filter. 
	 * We expect this to be applied to the database appender.
	 */
	public static void passAllLogsOnCurrentThread() {
		filter.setCurrentThread(Thread.currentThread().getId());
	}

	/**
	 * All log messages on the named thread regardless of level will be sent to the appender with the PassThru filter. Thread names 
	 * associated with existing messages can be found in the database table. Other than that it's hard to know.
	 * @param threadName
	 */
	public static void passAllLogsOnThread(String threadName) {
		filter.addThread(threadName);
	}
	/**
	 * All log messages from loggers whose name contains the supplied string will be sent to the appender, regardless of level.  
	 * (We could get fancier and use a regular ecxpression, but I think this is easier on the user). 
	 * Multiple calls just add more messages. This state persists until the whole filter is reset. An empty string will pass all messages. 
	 * It doesn'f matter if the same logger matches multiple criteria, only one message will be sent to the appender
	 * @param pattern
	 */
	public static void passAllLogs(String pattern) {
		filter.addPattern(pattern);
	}
	/**
	 * Reset the "PassAll" filter to again respect logging levels
	 */
	public static void resetPassAllFilter() {
		filter.reset();
	}

	// Then same for the Gateway ...
	
	/**
	 * @param loggerName
	 * @return the current level of a logger in Gateway scope
	 */
	public static String getGatewayLoggingLevel(String loggerName) {
		String level = "";
		try {
			level = (String)GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					LoggingProperties.MODULE_ID, "getGatewayLoggingLevel", loggerName);
		}
		catch(Exception ge) {
			log.debug(CLSS+"getGatewayLoggingLevel: GatewayException ("+ge.getMessage()+")");
		}
		return level;
	}
	/**
	 * Set the level of a logger in Gateway scope. The level is changed for everyone and persists until changed again.
	 * @param loggerName
	 * @param level name
	 */
	public static void setGatewayLoggingLevel(String loggerName, String level) {
		try {
			GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					LoggingProperties.MODULE_ID, "setGatewayLoggingLevel", loggerName, level );
		}
		catch(Exception ge) {
			log.debug(CLSS+"setGatewayLoggingLevel: GatewayException ("+ge.getMessage()+")");
		}
	}
	/**
	 * @param threadName
	 */
	public static void passAllGatewayLogsOnThread(String threadName) {
		try {
			GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					LoggingProperties.MODULE_ID, "passAllGatewayLogsOnThread", threadName );
		}
		catch(Exception ge) {
			log.debug(CLSS+"passAllGatewayLogsOnThread: GatewayException ("+ge.getMessage()+")");
		}
	}
	/**
	 * All log messages from loggers whose name contains the supplied string will be sent to the appender, regardless of level.
	 * This method can be called multiple times with different patterns.
	 * @param pattern
	 */
	public static void passAllGatewayLogs(String pattern)  {
		try {
			GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					LoggingProperties.MODULE_ID, "passAllGatewayLogs", pattern );
		}
		catch(Exception ge) {
			log.debug(CLSS+"passAllGatewayLogs: GatewayException ("+ge.getMessage()+")");
		}
	}

	/**
	 * Reset the Gateway "PassAll" filter to again respect logging levels
	 */
	public static void resetGatewayPassAllFilter() {
		try {
			GatewayConnectionManager.getInstance().getGatewayInterface().moduleInvoke(
					LoggingProperties.MODULE_ID, "resetGatewayPassAllFilter" );
		}
		catch(Exception ge) {
			log.debug(CLSS+"resetGatewayPassAllFilter: GatewayException ("+ge.getMessage()+")");
		}
	}
}
