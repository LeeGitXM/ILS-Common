/**
 *   (c) 2020  ILS Automation. All rights reserved. 
 */
package com.ils.module.gateway;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.ils.common.log.LogMaker;
import com.ils.log.common.LoggingProperties;
import com.ils.module.gateway.appender.GatewayCrashAppender;
import com.ils.module.gateway.appender.GatewaySingleTableDBAppender;
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.gateway.clientcomm.ClientReqSession;
import com.inductiveautomation.ignition.gateway.model.AbstractGatewayModuleHook;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.web.models.ConfigCategory;
import com.inductiveautomation.ignition.gateway.web.models.IConfigTab;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.InterpretationContext;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * This is root node for specialty code dealing with the gateway. On startup
 * we obtain the gateway context. It serves as our entry point into the
 * Ignition core.
 * 
 * The primary job of the startup handler is expose Gateway settings on the
 * module configuration page. It also starts the RPC handler.
 */
public class ILSGatewayHook extends AbstractGatewayModuleHook {
	private static final String CLSS = "LoggingGatewayHook";
	private GatewayContext context = null;
	private GatewayRpcDispatcher dispatcher = null;
	private int crashBufferSize = LoggingProperties.DEFAULT_CRASH_BUFFER_SIZE;
	private String loggingDatasource = "";

	public ILSGatewayHook() {
		System.out.println(String.format("%s.LoggingGatewayHook: Initializing...",CLSS));
	}
	
	// NOTE: During this period, the module status is LOADED, not RUNNING
	// Database facilities are not available yet.
	@Override
	public void setup(GatewayContext ctxt) {
		this.context = ctxt;
		dispatcher = new GatewayRpcDispatcher(context,this);
		GatewayScriptFunctions.setContext(context);
		GatewayScriptFunctions.setHook(this);
	}
		
	@Override
	public boolean isFreeModule() { return true; }
	
	@Override
	public void startup(LicenseState licenseState) {
		// Accessing the database should now succeed.
		configureLogging();
	}
	@Override
	public List<ConfigCategory> getConfigCategories() {
		List<ConfigCategory> categories = new ArrayList<>();
		return categories;
	}
	
	@Override 
	public List<IConfigTab> getConfigPanels() {
		List<IConfigTab> panels = new ArrayList<>();
		return panels;
	}
	
	public int getCrashBufferSize() { return crashBufferSize; }
	public String getLoggingDatasource() { return loggingDatasource; }
	
	@Override
	public void initializeScriptManager(ScriptManager mgr) {
		super.initializeScriptManager(mgr);
		mgr.addScriptModule(LoggingProperties.PROPERTIES_SCRIPT_PACKAGE,GatewayScriptFunctions.class);
	}
	@Override
	public void configureFunctionFactory(ExpressionFunctionManager factory) {
		super.configureFunctionFactory(factory);
	}
	
	@Override
	public void shutdown() {
		shutdownLogging();
	}

	@Override
	public Object getRPCHandler(ClientReqSession session, Long projectID) {
		return dispatcher;
	}
	/**
	 * Configure application logging for the database and crash appenders.
	 * The root logger has already been configured.
	 */
	private void configureLogging() {
		LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		System.out.println(String.format("%s: LoggerContext is %s",CLSS,logContext.getClass().getCanonicalName()));
		JoranConfigurator configurator = new JoranConfigurator();
		// Resetting the context clears all logger properties and closes existing appenders
		// It also sets all loggers to DEBUG. We will reset here.
		logContext.reset();
		configurator.setContext(logContext);
		Path configPath = Paths.get(context.getLibDir().getAbsolutePath(),"..","data","logback.xml");
		try {
			byte[] bytes = Files.toByteArray(configPath.toFile());
			configurator.doConfigure(new ByteArrayInputStream(bytes));
			
			String sizeString = configurator.getInterpretationContext().getProperty(LoggingProperties.CRASH_BUFFER_SIZE);
			if( sizeString!=null ) {
				try {
					crashBufferSize = Integer.parseInt(sizeString);
				}
				catch(NumberFormatException nfe) {
					System.out.println(String.format("%s: %s is not a number in logback.xml (%s)",CLSS,LoggingProperties.CRASH_BUFFER_SIZE,nfe.getLocalizedMessage()));
				}
			}
			String loggingDatasource = configurator.getInterpretationContext().getProperty(LoggingProperties.LOGGING_DATASOURCE);
			if( loggingDatasource!=null ) {
				Logger root = LogMaker.getLogger(Logger.ROOT_LOGGER_NAME);
				
				System.out.println(String.format("%s.configureLogging: Reconfigured gateway logger from %s, cxn=%s",CLSS,configPath.toFile().getAbsolutePath(),loggingDatasource));
				installDatabaseAppender(root,loggingDatasource);
				installCrashAppender(root,loggingDatasource,crashBufferSize);
				Iterator<Appender<ILoggingEvent>> iterator = root.iteratorForAppenders();
				System.out.println(String.format("%s.configureLogging: Root (%s) has these appenders",CLSS,root.getName() ));
				while(iterator.hasNext()) {
					System.out.println(String.format("%s.configureLogging: appender .................. (%s)",CLSS,iterator.next().getName() ));
				}
				root.setLevel(Level.INFO);
			}
			else {
				System.out.println(String.format("%s: WARNING: logback.xml must contain a %s property in order to create a DB appender",CLSS,LoggingProperties.LOGGING_DATASOURCE));
			}
		}
		catch(IOException ioe) {
			System.out.println(String.format("%s: Failed to read gateway logger configuration (%s)",CLSS,ioe.getMessage()));
		}
		catch(JoranException je) {
			System.out.println(String.format("%s: Failed to configure gateway logger (%s)",CLSS,je.getMessage()));
		}


		System.out.println(String.format("%s: Configured gateway logger",CLSS));
		
	}
	
	private void installDatabaseAppender(Logger root,String connection) {
		Appender<ILoggingEvent> appender = new GatewaySingleTableDBAppender<ILoggingEvent>(connection,context);
		appender.setContext(root.getLoggerContext());
		appender.setName(LoggingProperties.DB_APPENDER_NAME);
		root.addAppender(appender);
		appender.start();
		System.out.println(String.format("%s: Installed database appender ..",CLSS));
	}
	private void installCrashAppender(Logger root,String connection,int bufferSize) {
		Appender<ILoggingEvent> appender = new GatewayCrashAppender(connection,context,bufferSize);
		appender.setContext(root.getLoggerContext());
		appender.setName(LoggingProperties.CRASH_APPENDER_NAME);
		appender.start();
		root.addAppender(appender);
		System.out.println(String.format("%s: Installed crash appender ..",CLSS));
	}
	
	/**
	 * Remove the logging appenders that we created on startup.
	 * They have all been appended to the root logger.
	 */
	private void shutdownLogging() {
		Logger root = LogMaker.getLogger(Logger.ROOT_LOGGER_NAME);
		root.detachAppender(LoggingProperties.CRASH_APPENDER_NAME);
		root.detachAppender(LoggingProperties.DB_APPENDER_NAME);
	}
}
