/**
 *   (c) 2020  ILS Automation. All rights reserved.
 */
package com.ils.module.gateway;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
import com.ils.logging.common.ModulePropertiesInterface;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;


/**
 *  The RPC Dispatcher is the point of entry for incoming RCP requests.
 */
public class GatewayRpcDispatcher implements ModulePropertiesInterface {

	private final GatewayContext context;
	private final ILSGatewayHook hook;

	/**
	 * Constructor. On instantiation, the dispatcher creates instances
	 * of all required handlers.
	 */
	public GatewayRpcDispatcher(GatewayContext cntx,ILSGatewayHook hk) {
		this.context = cntx;
		this.hook = hk;
	}
	
	
	@Override
	public String getLoggingDatasource() {
		return hook.getLoggingDatasource();
	}
	@Override
	public int getCrashAppenderBufferSize() {
		return hook.getCrashAppender().getBufferSize();
	}
	@Override
	public int getGatewayCrashAppenderBufferSize() {
		return hook.getCrashAppender().getBufferSize();
	}

	@Override
	public String getGatewayLoggingLevel(String loggerName) {
		String result = "";
		ILSLogger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			Level level = lgr.getLevel();
			if( level!=null ) result = level.levelStr;
		}
		return result;
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
	public String getLibDir() { return context.getSystemManager().getLibDir().getAbsolutePath(); }
	
	@Override
	public List<String> getLoggerNames() {
		return getGatewayLoggerNames();
	}
	@Override
	public String getLoggingLevel(String loggerName) {
		return getGatewayLoggingLevel(loggerName);
	}
	/**
	 */
	@Override
	public String getLogsDir() { return context.getSystemManager().getLogsDir().getAbsolutePath(); } 
	
	@Override 
	public double[] getRetentionTimes() {
		return hook.getRetentionTimes();
	}
	/**
	 * @return the execution path for the browser used to display context-sensitive help.
	 */
	public String getWindowsBrowserPath() {
		String path = hook.getWindowsBrowserPath();
		if( path==null ) path = "BROWSER_PATH_MUST_BE_CONFIGURED_VIA_CommonModule";
		return path;
	}
	/**
	 */
	public String getUserLibDir() { return context.getSystemManager().getUserLibDir().getAbsolutePath(); }

	@Override
	public void passGatewayLogsOnThread(String threadName) {
		hook.getPatternFilter().addThread(threadName);
	}
	@Override
	public void passGatewayPattern(String pattern) {
		hook.getPatternFilter().addPattern(pattern);
	}
	@Override
	public void passLogsOnCurrentThread() {
		hook.getPatternFilter().passCurrentThread();
	}
	@Override
	public void passLogsOnThread(String threadName) {
		hook.getPatternFilter().addThread(threadName);
		
	}
	@Override
	public void passPattern(String pattern) {
		passGatewayPattern(pattern);
	}
	@Override
	public void resetGatewayPatternFilter() {
		hook.getPatternFilter().reset();
	}
	@Override
	public void resetPatternFilter() {
		resetGatewayPatternFilter();
	}
	@Override
	public void setCrashAppenderBufferSize(int size) {
		hook.getCrashAppender().setBufferSize(size);
	}
	@Override
	public void setGatewayCrashAppenderBufferSize(int size) {
		hook.getCrashAppender().setBufferSize(size);
		
	}
	@Override
	public void setGatewayLoggingLevel(String loggerName, String level) {
		ILSLogger lgr =  LogMaker.getLogger(loggerName);
		if( lgr!=null ) {
			Level lvl = Level.toLevel(level.toUpperCase());
			lgr.setLevel(lvl);
		}
	}
	@Override
	public void setLoggingLevel(String loggerName, String level) {
		setGatewayLoggingLevel(loggerName,level);
	}
	@Override 
	public boolean useDatabaseAppender() {
		return hook.useDatabaseAppender();
	}

}
