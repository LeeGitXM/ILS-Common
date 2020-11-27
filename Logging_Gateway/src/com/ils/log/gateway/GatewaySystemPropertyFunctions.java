/**
 *   (c) 2020  ILS Automation. All rights reserved.
 *  
 */
package com.ils.log.gateway;

import com.ils.log.common.LoggingProperties;
import com.inductiveautomation.ignition.client.gateway_interface.GatewayConnectionManager;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

public class GatewaySystemPropertyFunctions  {
	private static GatewayContext context = null;
	private static LoggingGatewayHook hook = null;

	public static void setContext(GatewayContext ctx) {
		context = ctx;
	}
	
	public static void setHook(LoggingGatewayHook gh) {
		hook = gh;
	}

	/**
	 * @return the datasource holding the logging database.
	 */
	public static String getLoggingDatasource()throws Exception{
		return hook.getLoggingDatasource();
	}
	public static String getLibDir() { return context.getLibDir().getAbsolutePath(); }
	/**
	 */
	public static String getLosgDir() { return context.getLogsDir().getAbsolutePath(); }  
	/**
	 */
	public static String getUserLibDir() { return context.getUserlibDir().getAbsolutePath(); }

}