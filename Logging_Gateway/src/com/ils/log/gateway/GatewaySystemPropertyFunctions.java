/**
 *   (c) 2020  ILS Automation. All rights reserved.
 *  
 */
package com.ils.log.gateway;

import com.inductiveautomation.ignition.gateway.model.GatewayContext;

public class GatewaySystemPropertyFunctions  {
	private static GatewayContext context = null;

	public static void setContext(GatewayContext ctx) {
		context = ctx;
	}

	public static String getLibDir() { return context.getLibDir().getAbsolutePath(); }
	/**
	 */
	public static String getLosgDir() { return context.getLogsDir().getAbsolutePath(); }  
	/**
	 */
	public static String getUserLibDir() { return context.getUserlibDir().getAbsolutePath(); }

}