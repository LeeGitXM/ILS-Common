/**
 *   (c) 2020  ILS Automation. All rights reserved.
 */

package com.ils.module.designer;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
import com.ils.logging.common.CommonProperties;
import com.ils.logging.common.LoggingHookInterface;
import com.ils.logging.common.filter.CrashFilter;
import com.ils.logging.common.filter.PatternFilter;
import com.ils.logging.common.python.PythonExec;
import com.ils.module.client.ClientScriptFunctions;
import com.ils.module.client.appender.ClientCrashAppender;
import com.ils.module.client.appender.ClientSingleTableDBAppender;
import com.inductiveautomation.ignition.client.model.AbstractClientContext;
import com.inductiveautomation.ignition.client.model.ClientContext;
import com.inductiveautomation.ignition.common.expressions.ExpressionFunctionManager;
import com.inductiveautomation.ignition.common.licensing.LicenseState;
import com.inductiveautomation.ignition.common.script.ScriptManager;
import com.inductiveautomation.ignition.common.xmlserialization.deserialization.XMLDeserializer;
import com.inductiveautomation.ignition.designer.model.AbstractDesignerModuleHook;
import com.inductiveautomation.ignition.designer.model.DesignerContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.TurboFilterList;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.OutputStreamAppender;

public class ILSDesignerHook extends AbstractDesignerModuleHook implements LoggingHookInterface  {
	private static final String CLSS = "LoggingDesignerHook";
	private String clientId = null;
	private ClientContext context = null;
	private ClientCrashAppender crashAppender = null;
	private final CrashFilter crashFilter;
	private PatternFilter patternFilter = null;

	public ILSDesignerHook() {
		System.out.println(String.format("%s: Initializing...",CLSS));
		crashFilter = new CrashFilter();
	}
	
	public CrashFilter getCrashFilter() { return this.crashFilter; }
	public PatternFilter getPatternFilter() { return this.patternFilter; }
	public void setCrashBufferSize(int size) { crashAppender.setBufferSize(size); }
	
	/**
	 * Make the interface script functions available.
	 */
	@Override
	public void initializeScriptManager(ScriptManager mgr) {
		mgr.addScriptModule(CommonProperties.PROPERTIES_SCRIPT_PACKAGE,ClientScriptFunctions.class);
	}

	@Override
	public void configureDeserializer(XMLDeserializer arg0) {
	}
	
	@Override
	public Map<String,String> createPermissionKeys() {
		return new HashMap<>();
	}
	
	@Override
	public String getClientId() { return this.clientId; }
	
	@Override
	public void notifyActivationStateChanged(LicenseState arg0) {	
	}

	@Override
	public void shutdown() {
		shutdownLogging();
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
		ClientScriptFunctions.setHook(this);
		ClientScriptFunctions.setContext(context);
		PythonExec.setContext(context);
		// Set client ID from script (we can't figure out how else)
		try {
			String code = "system.util.getClientId";
			PythonExec pexec = new PythonExec(code,String.class);  // Specify the return class
			clientId = (String)pexec.exec();
			MDC.put(LogMaker.CLIENT_KEY,clientId);
			System.out.println(String.format("%s.configureLogging: clientId = %s",CLSS,clientId));
		}
		catch(Exception ex) {
			System.out.println(String.format("%s: Exception running script ... (%s)",CLSS,ex.getLocalizedMessage()));
		}
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
		// Resetting the context clears all logger properties and closes existing appenders
		// It also sets all loggers to DEBUG.
		logContext.reset();
		try {
			int crashBufferSize = ClientScriptFunctions.getCrashAppenderBufferSize();
			String threshold = ClientScriptFunctions.getCrashAppenderThreshold();
			crashFilter.setThreshold(threshold);
			String loggingDatasource = ClientScriptFunctions.getLoggingDatasource();
			if( loggingDatasource!=null ) {
				ILSLogger root = LogMaker.getLogger(Logger.ROOT_LOGGER_NAME);
				root.setLevel(Level.INFO);
				
				installDatabaseAppender(root,loggingDatasource,logContext);
				installCrashAppender(root,loggingDatasource,logContext,crashBufferSize);
				Iterator<Appender<ILoggingEvent>> iterator = root.iteratorForAppenders();
				PatternLayoutEncoder pattern = new PatternLayoutEncoder();
				pattern.setPattern(CommonProperties.DEFAULT_APPENDER_PATTERN);
				System.out.println(String.format("%s.configureLogging: Root (%s) has these appenders",CLSS,root.getName()));
				while(iterator.hasNext()) {
					Appender app = iterator.next();
					if( app instanceof OutputStreamAppender ) {
						((OutputStreamAppender)app).setEncoder(pattern);
					}
					System.out.println(String.format("%s.configureLogging: appender .................. (%s)",CLSS,app.getName() ));
				}
			}
		}
		catch(Exception ex) {
			System.out.println(String.format("%s: Failed to create logging appenders (%s)\n",CLSS,ex.getMessage()));
			ex.printStackTrace();
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
		System.out.println(String.format("%s: Created Designer logger ...",CLSS));
		
	}
	private void installDatabaseAppender(ILSLogger root,String connection,LoggerContext ctx) {
		AbstractClientContext acc = (AbstractClientContext)context;
		Appender<ILoggingEvent> appender = new ClientSingleTableDBAppender<ILoggingEvent>(connection,acc,ctx,"designer");
		appender.setContext(root.getLoggerContext());
		appender.setName(CommonProperties.DB_APPENDER_NAME);
		appender.start();
		root.addAppender(appender);
		System.out.println(String.format("%s: Installed database appender ...",CLSS));
	}
	private void installCrashAppender(ILSLogger root,String connection,LoggerContext ctx,int bufferSize) {
		AbstractClientContext acc = (AbstractClientContext)context;
		crashAppender = new ClientCrashAppender(connection,acc,ctx,"designer",bufferSize);
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
		ILSLogger root = LogMaker.getLogger(Logger.ROOT_LOGGER_NAME);
		root.detachAppender(CommonProperties.CRASH_APPENDER_NAME);
		root.detachAppender(CommonProperties.DB_APPENDER_NAME);
	}
}
