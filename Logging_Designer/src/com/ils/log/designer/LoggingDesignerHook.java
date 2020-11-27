/**
 *   (c) 2020  ILS Automation. All rights reserved.
 */

package com.ils.log.designer;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.ils.common.log.LogMaker;
import com.ils.log.client.SystemPropertiesScriptFunctions;
import com.ils.log.common.LoggingProperties;
import com.ils.logging.gateway.appender.ClientSingleTableDBAppender;
import com.inductiveautomation.ignition.client.model.AbstractClientContext;
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.XMLDeserializer;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

public class LoggingDesignerHook extends AbstractDesignerModuleHook  {
	private static final String CLSS = "LoggingDesignerHook";
	private DesignerContext context = null;

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
	public void startup(DesignerContext ctx, LicenseState arg1) throws Exception {
		this.context = ctx;
		configureLogging();
	}
	
	@Override
	public void configureFunctionFactory(ExpressionFunctionManager factory) {
	}
	
	/**
	 * Configure application logging for the database appender.
	 * Even if the configuration fails, we still have the default configuration.
	 */
	private void configureLogging() {
		LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		System.out.println(String.format("%s: LoggerContext is %s",CLSS,logContext.getClass().getCanonicalName()));
		logContext.reset();
		try {
			String loggingDatasource = SystemPropertiesScriptFunctions.getLoggingDatasource();
			if( loggingDatasource!=null ) {
				Logger root = LogMaker.getLogger(Logger.ROOT_LOGGER_NAME);
				installDatabaseAppender(root,loggingDatasource);
			}
			else {
				System.out.println(String.format("%s: WARNING: Creation of DB appender failed",CLSS));
			}
		}
		catch(Exception ioe) {
			System.out.println(String.format("%s: Failed to read gateway logger configuration (%s)",CLSS,ioe.getMessage()));
		}
		Logger log = LogMaker.getLogger(this);
		log.info("Created designer logger");
		
	}
	private void installDatabaseAppender(Logger root,String connection) {
		AbstractClientContext acc = (AbstractClientContext)context;
		Appender<ILoggingEvent> appender = new ClientSingleTableDBAppender<ILoggingEvent>(connection,acc,"designer");
		appender.setContext(root.getLoggerContext());
		appender.start();
		root.addAppender(appender);
		root.info(CLSS+":Installed database appender ...");
	}
	
}
