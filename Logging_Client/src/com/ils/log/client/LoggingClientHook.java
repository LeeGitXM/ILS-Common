/**
 *   (c) 2020  ILS Automation. All rights reserved. 
 */
package com.ils.log.client;

import java.util.HashMap;
import java.util.Map;

import com.ils.log.common.LoggingProperties;
import com.inductiveautomation.ignition.client.model.ClientContext;
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.XMLDeserializer;
import com.inductiveautomation.vision.api.client.ClientModuleHook;

public class LoggingClientHook implements ClientModuleHook {

	/**
	 * Make the interface script functions available.
	 */
	@Override
	public void initializeScriptManager(ScriptManager mgr) {
		mgr.addScriptModule(LoggingProperties.PROPERTIES_SCRIPT_PACKAGE,SystemPropertiesScriptFunctions.class);
	}

	@Override
	public void configureDeserializer(XMLDeserializer arg0) {
	}
	
	@Override
	public Map<String,String> createPermissionKeys() {
		return new HashMap<>();
	}
	
	@Override
	public void notifyActivationStateChanged(LicenseState arg0) {	
	}

	@Override
	public void shutdown() {
	}

	/**
	 * On module startup initialize the root logger to log to the console. Then add a "SingleTableDBAppender"
	 * using the database connection defined in the gateway.
	 * @param ctx
	 * @param arg1
	 * @throws Exception
	 */
	@Override
	public void startup(ClientContext ctx, LicenseState arg1) throws Exception {
	}
	
	@Override
	public void configureFunctionFactory(ExpressionFunctionManager factory) {
	}

}
