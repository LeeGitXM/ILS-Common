/**
 *   (c) 2020  ILS Automation. All rights reserved.
 */
package com.ils.log.gateway;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.ils.common.log.LogMaker;
import com.ils.log.common.SystemPropertiesInterface;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;


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

	@Override
	public String getLoggingDatasource() {
		return hook.getLoggingDatasource();
	}

	@Override
	public int getCrashBufferSize() {
		return hook.getCrashBufferSize();
	}

	@Override
	public List<String> getLoggerNames() {
		return getGatewayLoggerNames();
	}
	/**
	 * @return a string of comma-separated logger names
	 */
	public String getGatewayLoggerNamesAsString() {
		LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		List<Logger> loggers = logContext.getLoggerList();
		StringBuffer buf = new StringBuffer();
		for(Logger lgr:loggers) {
			if(buf.length()>0) buf.append(",");
			buf.append(lgr.getName());
		}
		return buf.toString();
	}
	@Override
	public List<String> getGatewayLoggerNames() {
		LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		List<Logger> loggers = logContext.getLoggerList();
		List<String> list = new ArrayList<>();
		for(Logger lgr:loggers) {
			list.add(lgr.getName());
		}
		return list;
	}

	@Override
	public String getLoggingLevel(String loggerName) {
		return getGatewayLoggingLevel(loggerName);
	}
	
	@Override
	public String getGatewayLoggingLevel(String loggerName) {
		String result = "";
		Logger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			Level level = lgr.getLevel();
			if( level!=null ) result = level.toString();
		}
		return result;
	}

	@Override
	public void setLoggingLevel(String loggerName, String level) {
		setGatewayLoggingLevel(loggerName,level);
		
	}
	@Override
	public void setGatewayLoggingLevel(String loggerName, String level) {
		Logger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			Level lvl = Level.toLevel(level.toUpperCase());
			lgr.setLevel(lvl);
		}
	}
	
	@Override
	public void passAllLogsOnCurrentThread() {
		hook.getPassThruFilter().setCurrentThread(Thread.currentThread().getId());
	}

	@Override
	public void passAllLogsOnThread(String threadName) {
		passAllGatewayLogsOnThread(threadName);
		
	}
	@Override
	public void passAllGatewayLogsOnThread(String threadName) {
		hook.getPassThruFilter().addThread(threadName);
	}

	@Override
	public void passAllLogs(String pattern) {
		passAllGatewayLogs(pattern);
	}
	@Override
	public void passAllGatewayLogs(String pattern) {
		hook.getPassThruFilter().addPattern(pattern);
	}

	@Override
	public void resetPassAllFilter() {
		resetGatewayPassAllFilter();
	}

	@Override
	public void resetGatewayPassAllFilter() {
		hook.getPassThruFilter().reset();
	}
}
