/**
 *   (c) 2020-2021  ILS Automation. All rights reserved. 
 */
package com.ils.module.gateway;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.google.common.io.Files;
import com.ils.common.log.LogMaker;
import com.ils.logging.common.CommonProperties;
import com.ils.logging.common.filter.CrashFilter;
import com.ils.logging.common.filter.PatternFilter;
import com.ils.module.gateway.appender.GatewayCrashAppender;
import com.ils.module.gateway.appender.GatewaySingleTableDBAppender;
import com.ils.module.gateway.meta.HelpParameterEditPage;
import com.ils.module.gateway.meta.HelpRecord;
import com.inductiveautomation.ignition.common.BundleUtil;
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
import ch.qos.logback.classic.spi.TurboFilterList;
import ch.qos.logback.classic.turbo.TurboFilter;
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
public class ILSGatewayHook extends AbstractGatewayModuleHook {
	private static final String CLSS = "LoggingGatewayHook";
	public static final String BUNDLE_NAME = "ilsmodule";       // Properties file is ilsmodule.properties
	public static final String BUNDLE_ROOT = "ils";             // Name of the bundle
	private static final String CATEGORY_NAME = "ILSParameters";
	public static final ConfigCategory helpCategory = new ConfigCategory(CATEGORY_NAME,BUNDLE_ROOT+".menu.category");
	private static HelpRecord helpRec = null;
	private GatewayContext context = null;
	private GatewayRpcDispatcher dispatcher = null;
	private GatewayCrashAppender crashAppender = null;
	private String loggingDatasource = "";
	private final CrashFilter crashFilter;
	private PatternFilter patternFilter = null;
	
	static {
		// Access the resource bundle
		BundleUtil.get().addBundle(BUNDLE_ROOT,ILSGatewayHook.class,BUNDLE_NAME);
	}

	public ILSGatewayHook() {
		System.out.println(String.format("%s: Initializing...",CLSS));
		crashFilter = new CrashFilter();
	}
	
	public CrashFilter getCrashFilter() { return this.crashFilter; }
	public PatternFilter getPatternFilter() { return this.patternFilter; }
	
	// NOTE: During this period, the module status is LOADED, not RUNNING
	// Database facilities are not available yet.
	@Override
	public void setup(GatewayContext ctxt) {
		this.context = ctxt;
		dispatcher = new GatewayRpcDispatcher(context,this);
		GatewayScriptFunctions.setContext(context);
		GatewayScriptFunctions.setHook(this);
		// Register the help parameter record - this creates a table in the internal
        // database if necessary. Force there to be a single row.
        try {
            context.getSchemaUpdater().updatePersistentRecords(HelpRecord.META);
        }
        catch (SQLException e) {
        	System.out.println(String.format("%s.setup: Error registering settings record type.",CLSS, e.getLocalizedMessage()));
        }
	}
		
	@Override
	public boolean isFreeModule() { return true; }
	
	@Override
	public void startup(LicenseState licenseState) {
		// Accessing the database should now succeed.
		configureLogging();
		// Configure the help record. Create a default value, then call "make
        // sure this record exists". That works based on the id, which we're
        // defining to be 0. The license statement is read-only. Otherwise
		// things are writable. If not valid, a message is logged.
		// Force there to be a single row.
		try {
			helpRec = context.getPersistenceInterface().createNew(HelpRecord.META);
			helpRec.setLong(HelpRecord.Id, 0L);   
			String currentPath = helpRec.getWindowsBrowserPath();
			if( currentPath==null || currentPath.isEmpty() ) currentPath = CommonProperties.DEFAULT_WINDOWS_BROWSER_PATH;
			helpRec.setString(HelpRecord.windowsBrowserPath, currentPath);
			context.getSchemaUpdater().ensureRecordExists(helpRec);
		}
		catch(Exception ex) {
			System.out.println(String.format("%s.startup: Failed to create persistant record (%s)",CLSS, ex.getLocalizedMessage()));;
		}
	}
	@Override
	public List<ConfigCategory> getConfigCategories() {
		List<ConfigCategory> categories = new ArrayList<>();
		categories.add(helpCategory);
		return categories;
	}
	
	@Override 
	public List<IConfigTab> getConfigPanels() {
		List<IConfigTab> panels = new ArrayList<>();
		panels.add(HelpParameterEditPage.MENU_ENTRY);
		return panels;
	}
	
	public GatewayCrashAppender getCrashAppender() { return crashAppender; }
	public String getLoggingDatasource() { return loggingDatasource; }
	
	@Override
	public void initializeScriptManager(ScriptManager mgr) {
		super.initializeScriptManager(mgr);
		mgr.addScriptModule(CommonProperties.PROPERTIES_SCRIPT_PACKAGE,GatewayScriptFunctions.class);
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
	
	public String getWindowsBrowserPath() {
		return "";
	}
	/**
	 * Configure application logging for the database and crash appenders.
	 * The root logger has already been configured. The pattern filter is
	 * defined in the logback.xml configuration file. 
	 */
	private void configureLogging() {
		LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		System.out.println(String.format("%s: LoggerContext is %s",CLSS,logContext.getClass().getCanonicalName()));
		JoranConfigurator configurator = new JoranConfigurator();
		// Resetting the context clears all logger properties and closes existing appenders
		// It also sets all loggers to DEBUG. We will reset here.
		logContext.reset();
		configurator.setContext(logContext);
		Path configPath = Paths.get(context.getLibDir().getAbsolutePath(),"..","data","ils_logback.xml");
		try {
			byte[] bytes = Files.toByteArray(configPath.toFile());
			configurator.doConfigure(new ByteArrayInputStream(bytes));
			
			String sizeString = configurator.getInterpretationContext().getProperty(CommonProperties.CRASH_BUFFER_SIZE);
			int crashBufferSize = CommonProperties.DEFAULT_CRASH_BUFFER_SIZE;
			if( sizeString!=null ) {
				try {
					crashBufferSize = Integer.parseInt(sizeString);
				}
				catch(NumberFormatException nfe) {
					System.out.println(String.format("%s: %s is not a number in ils_logback.xml (%s)",CLSS,CommonProperties.CRASH_BUFFER_SIZE,nfe.getLocalizedMessage()));
				}
			}
			String threshold = configurator.getInterpretationContext().getProperty(CommonProperties.CRASH_APPENDER_THRESHOLD);
			if( threshold==null) threshold = "debug";
			if( threshold!=null ) {
				crashFilter.setThreshold(threshold);
			}
			
			String loggingDatasource = configurator.getInterpretationContext().getProperty(CommonProperties.LOGGING_DATASOURCE);
			if( loggingDatasource!=null ) {
				Logger root = LogMaker.getLogger(Logger.ROOT_LOGGER_NAME);
				root.setLevel(Level.INFO);
				installDatabaseAppender(root,loggingDatasource);
				installCrashAppender(root,loggingDatasource,crashBufferSize);
				Iterator<Appender<ILoggingEvent>> iterator = root.iteratorForAppenders();
				System.out.println(String.format("%s.configureLogging: Root (%s) has these appenders",CLSS,root.getName() ));
				while(iterator.hasNext()) {
					System.out.println(String.format("%s.configureLogging: appender .................. (%s)",CLSS,iterator.next().getName() ));
				}
			}
			else {
				System.out.println(String.format("%s: WARNING: ils_logback.xml must contain a %s property in order to create a DB appender",CLSS,CommonProperties.LOGGING_DATASOURCE));
			}
			// Find the pattern filter
			TurboFilterList list = logContext.getTurboFilterList();
			Iterator<TurboFilter> iter  = list.iterator();
			while( iter.hasNext()) {
				TurboFilter filter = iter.next();
				if( filter instanceof PatternFilter ) {
					patternFilter = (PatternFilter)filter;
					break;
				}
			}
			System.out.println(String.format("%s.configureLogging: Reconfigured gateway logger from %s, threshold %s for %s, cxn=%s",CLSS,configPath.toFile().getAbsolutePath(),
					threshold,sizeString,loggingDatasource));
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
		appender.setName(CommonProperties.DB_APPENDER_NAME);
		root.addAppender(appender);
		appender.start();
		System.out.println(String.format("%s: Installed database appender ..",CLSS));
	}
	private void installCrashAppender(Logger root,String connection,int bufferSize) {
		crashAppender = new GatewayCrashAppender(connection,context,bufferSize);
		crashAppender.setContext(root.getLoggerContext());
		crashAppender.setName(CommonProperties.CRASH_APPENDER_NAME);
		crashAppender.addFilter(crashFilter);
		crashAppender.start();
		root.addAppender(crashAppender);
		System.out.println(String.format("%s: Installed crash appender ..",CLSS));
	}
	
	/**
	 * Remove the logging appenders that we created on startup.
	 * They have all been appended to the root logger.
	 */
	private void shutdownLogging() {
		Logger root = LogMaker.getLogger(Logger.ROOT_LOGGER_NAME);
		root.detachAppender(CommonProperties.CRASH_APPENDER_NAME);
		root.detachAppender(CommonProperties.DB_APPENDER_NAME);
	}
}
