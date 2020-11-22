/**
 *   (c) 2020  ILS Automation. All rights reserved.
 */
package com.ils.log.gateway;

import com.ils.log.common.SystemPropertiesInterface;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;



/**
 *  The RPC Dispatcher is the point of entry for incoming RCP requests.
 */
public class GatewayRpcDispatcher implements SystemPropertiesInterface {

	private final GatewayContext context;
	private final LoggingGatewayHook hook;

	/**
	 * Constructor. On instantiation, the dispatcher creates instances
	 * of all required handlers.
	 */
	public GatewayRpcDispatcher(GatewayContext cntx,LoggingGatewayHook hk) {
		this.context = cntx;
		this.hook = hk;
	}


	public String getLibDir() { return context.getLibDir().getAbsolutePath(); }
	/**
	 */
	public String getLogsDir() { return context.getLogsDir().getAbsolutePath(); }  
	/**
	 */
	public String getUserLibDir() { return context.getUserlibDir().getAbsolutePath(); }
	
}
