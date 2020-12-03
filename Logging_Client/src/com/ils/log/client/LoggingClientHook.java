/**
 *   (c) 2020  ILS Automation. All rights reserved. 
 */
package com.ils.log.client;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.ils.common.log.LogMaker;
import com.ils.log.common.LoggingProperties;
import com.ils.logging.common.filter.BypassFilter;
import com.ils.logging.common.filter.PassThruFilter;
import com.ils.logging.gateway.appender.ClientCrashAppender;
import com.ils.logging.gateway.appender.ClientSingleTableDBAppender;
import com.inductiveautomation.ignition.client.model.AbstractClientContext;
import com.inductiveautomation.ignition.client.model.ClientContext;
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.XMLDeserializer;
import com.inductiveautomation.vision.api.client.ClientModuleHook;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;

public class LoggingClientHook implements ClientModuleHook {
	private static final String CLSS = "LoggingClientHook";
	private ClientContext context = null;
	private final PassThruFilter passThruFilter = new PassThruFilter();
	/**
	 * Make the interface script functions available.
	 */
	@Override
	public void initializeScriptManager(ScriptManager mgr) {
		ClientScriptFunctions.setFilter(passThruFilter);
		mgr.addScriptModule(LoggingProperties.PROPERTIES_SCRIPT_PACKAGE,ClientScriptFunctions.class);
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
			String loggingDatasource = ClientScriptFunctions.getLoggingDatasource();
			if( loggingDatasource!=null ) {
				Logger root = LogMaker.getLogger(Logger.ROOT_LOGGER_NAME);
				installDatabaseAppender(root,loggingDatasource);
				
				int bufferSize = ClientScriptFunctions.getCrashBufferSize();
				installCrashAppender(root,loggingDatasource,bufferSize);
				
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
		Appender<ILoggingEvent> appender = new ClientSingleTableDBAppender<ILoggingEvent>(connection,acc,"client");
		appender.setContext(root.getLoggerContext());
		appender.addFilter(passThruFilter);
		appender.start();
		root.addAppender(appender);
		root.info(CLSS+":Installed database appender ...");
	}
	private void installCrashAppender(Logger root,String connection,int bufferSize) {
		AbstractClientContext acc = (AbstractClientContext)context;
		Appender<ILoggingEvent> appender = new ClientCrashAppender(connection,acc,"designer",bufferSize);
		appender.setContext(root.getLoggerContext());
		BypassFilter filter = new BypassFilter();
		filter.setThreshold(Level.TRACE);
		appender.addFilter(filter);
		appender.start();
		root.addAppender(appender);
		root.info(CLSS+":Installed database appender ...");
	}
}
