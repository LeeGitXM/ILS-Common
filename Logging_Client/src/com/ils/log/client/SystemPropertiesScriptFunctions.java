/**
 *   (c) 2020  ILS Automation. All rights reserved.
 *  
 *   Based on sample code in the IA-scripting-module
 *   by Travis Cox.
 */
package com.ils.log.client;

import com.ils.common.log.LogMaker;
import com.ils.log.common.LoggingProperties;
import com.inductiveautomation.ignition.client.gateway_interface.GatewayConnectionManager;

import ch.qos.logback.classic.Logger;

/**
 *  This class exposes functions to access Gateway parameters from
 *  either client or designer scopes. We basically implement the SystemPropertiesInterface,
 *  except all methods here are static.
 *  
 *  Remote procedure calls are made to the Gateway scope to produce the changes.
 */
public class SystemPropertiesScriptFunctions  {
	private static String CLSS = "GatewayDelegate: ";
	private static final Logger log;
	
	static {
		log = LogMaker.getLogger(SystemPropertiesScriptFunctions.class.getCanonicalName());
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
}
