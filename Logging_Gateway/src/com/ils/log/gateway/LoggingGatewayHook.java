/**
 *   (c) 2020  ILS Automation. All rights reserved. 
 */
package com.ils.log.gateway;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.ils.common.log.LogMaker;
import com.ils.log.common.LoggingProperties;
import com.ils.logging.common.filter.BypassFilter;
import com.ils.logging.common.filter.PassThruFilter;
import com.ils.logging.gateway.appender.GatewayCrashAppender;
import com.ils.logging.gateway.appender.GatewaySingleTableDBAppender;
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
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.joran.spi.JoranException;

/**
 * This is root node for specialty code dealing with the gateway. On startup
 * we obtain the gateway context. It serves as our entry point into the
 * Ignition core.
 * 
 * The primary job of the startup handler is expose Gateway settings on the
 * module configuration page. It also starts the RPC handler.
 */
public class LoggingGatewayHook extends AbstractGatewayModuleHook {
	private static final String CLSS = "LoggingGatewayHook";
	private GatewayContext context = null;
	private GatewayRpcDispatcher dispatcher = null;
	private Logger log = null;
	private int crashBufferSize = LoggingProperties.DEFAULT_CRASH_BUFFER_SIZE;
	private String loggingDatasource = "";
	private final PassThruFilter passThruFilter = new PassThruFilter();

	public LoggingGatewayHook() {
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
	public PassThruFilter getPassThruFilter() { return passThruFilter; }
	
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
	}

	@Override
	public Object getRPCHandler(ClientReqSession session, Long projectID) {
		return dispatcher;
	}
	/**
	 * Configure application logging for the database appender.
	 * Even if the configuration fails, we still have the default configuration.
	 */
	private void configureLogging() {
		LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();

		System.out.println(String.format("%s: LoggerContext is %s",CLSS,logContext.getClass().getCanonicalName()));
		JoranConfigurator configurator = new JoranConfigurator();
		logContext.reset();
		configurator.setContext(logContext);
		Path configPath = Paths.get(context.getLibDir().getAbsolutePath(),"..","data","logback.xml");
		try {
			byte[] bytes = Files.toByteArray(configPath.toFile());
			configurator.doConfigure(new ByteArrayInputStream(bytes));
			try {
				String sizeString = configurator.getInterpretationContext().getProperty(LoggingProperties.CRASH_BUFFER_SIZE);
				if( sizeString!=null ) {
					crashBufferSize = Integer.parseInt(sizeString);
				}
			}
			catch(NumberFormatException nfe) {
				System.out.println(String.format("%s: %s is not a number in logback.xml (%s)",CLSS,LoggingProperties.CRASH_BUFFER_SIZE,nfe.getLocalizedMessage()));
			}
			loggingDatasource = configurator.getInterpretationContext().getProperty(LoggingProperties.LOGGING_DATASOURCE);
			System.out.println(String.format("%s.configureLogging: Configured gateway logger from %s, cxn=%s",CLSS,configPath.toFile().getAbsolutePath(),loggingDatasource));
			if( loggingDatasource!=null ) {
				Logger root = LogMaker.getLogger(Logger.ROOT_LOGGER_NAME);
				installDatabaseAppender(root,loggingDatasource);
				
				installCrashAppender(root,loggingDatasource,crashBufferSize);
			}
			else {
				System.out.println(String.format("%s: WARNING: %s must contain a %s property in order to create a DB appender",CLSS,configPath.toFile().getAbsolutePath(),LoggingProperties.LOGGING_DATASOURCE));
			}
		}
		catch(IOException ioe) {
			System.out.println(String.format("%s: Failed to read gateway logger configuration (%s)",CLSS,ioe.getMessage()));
		}
		catch(JoranException je) {
			System.out.println(String.format("%s: Failed to configure gateway logger (%s)",CLSS,je.getMessage()));
		}			
		log = LogMaker.getLogger(this);
		log.info("Created gateway logger");
		
	}
	
	private void installDatabaseAppender(Logger root,String connection) {
		Appender<ILoggingEvent> appender = new GatewaySingleTableDBAppender<ILoggingEvent>(connection,context);
		appender.setContext(root.getLoggerContext());
		appender.addFilter(passThruFilter);
		appender.start();
		root.addAppender(appender);
		root.info("Installed database appender ...");
	}
	private void installCrashAppender(Logger root,String connection,int bufferSize) {
		Appender<ILoggingEvent> appender = new GatewayCrashAppender(connection,context,bufferSize);
		appender.setContext(root.getLoggerContext());
		BypassFilter filter = new BypassFilter();
		filter.setThreshold(Level.TRACE);
		appender.addFilter(filter);
		appender.start();
		root.addAppender(appender);
		root.info(CLSS+":Installed database appender ...");
	}
}
