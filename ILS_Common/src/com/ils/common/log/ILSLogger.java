/**
 *   (c) 2018-2021  ILS Automation. All rights reserved.
 */
package com.ils.common.log;

import java.util.Iterator;

import org.slf4j.MDC;
import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachable;


/**
 * The project-aware logger encapsulates a logback logger and maintains the name of its project. 
 * The logger is encased as a member because the logback logger class is final.
 * Change the formatting style from {} to % if the ...f method name is used.
 */
public class ILSLogger implements org.slf4j.Logger,AppenderAttachable<ILoggingEvent> {
	private final Logger logger;
	private String clientId = "";
	private String project = "";
	
	public ILSLogger(Logger lgr) {
		this.logger = lgr;
		setLevel(Level.INFO);
	}
	
	public String getClientId() { return this.clientId; }
	public void setClientId(String id) { this.clientId = id; }
	public String getProject() { return this.project; }
	public void setProject(String name) { this.project = name; }

	@Override
	public void debug(String arg) {
		setAttributes();
		logger.debug(arg);
	}

	@Override
	public void debug(String format, Object arg) {
		setAttributes();
		logger.debug(format,arg);
	}

	@Override
	public void debug(String format, Object... args) {
		setAttributes();
		logger.debug(format,args);
		
	}
	public void debugf(String format, Object... args) {
		setAttributes();
		logger.debug(String.format(format, args));
	}

	@Override
	public void debug(String arg, Throwable ex) {
		setAttributes();
		logger.debug(arg,ex);
	}

	@Override
	public void debug(Marker marker, String arg) {
		setAttributes();
		logger.debug(marker,arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		setAttributes();
		logger.debug(format,arg1,arg2);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		setAttributes();
		logger.debug(marker,format,arg);	
	}

	@Override
	public void debug(Marker marker, String format, Object... args) {
		setAttributes();
		logger.debug(marker,format,args);
	}

	@Override
	public void debug(Marker marker, String arg1, Throwable ex) {
		setAttributes();
		logger.debug(marker,arg1,ex);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		logger.debug(marker,format,arg1,arg2);
	}

	@Override
	public void error(String arg) {
		setAttributes();
		logger.error(arg);
	}

	@Override
	public void error(String format, Object arg1) {
		setAttributes();
		logger.error(format,arg1);
	}

	@Override
	public void error(String format, Object... args) {
		setAttributes();
		logger.error(format,args);
	}
	
	public void errorf(String format, Object... args) {
		logger.error(String.format(format, args));
	}

	@Override
	public void error(String arg, Throwable ex) {
		logger.error(arg,ex);
	}

	@Override
	public void error(Marker marker, String arg) {
		setAttributes();
		logger.error(marker,arg);	
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		setAttributes();
		logger.error(format,arg1,arg2);
	}

	@Override
	public void error(Marker marker, String arg1, Object arg2) {
		setAttributes();
		logger.error(marker,arg1,arg2);
	}

	@Override
	public void error(Marker marker, String format, Object... args) {
		setAttributes();
		logger.error(marker,format,args);
	}

	@Override
	public void error(Marker marker, String arg1, Throwable ex) {
		setAttributes();
		logger.error(marker,arg1,ex);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		setAttributes();
		logger.error(marker,format,arg1,arg2);
	}

	public Level getLevel() {
		return logger.getLevel();
	}
	
	public LoggerContext getLoggerContext() {
		return logger.getLoggerContext();
	}
	
	@Override
	public String getName() {
		return logger.getName();
	}

	@Override
	public void info(String arg) {
		setAttributes();
		logger.info(arg);
	}

	@Override
	public void info(String arg0, Object arg1) {
		setAttributes();
		logger.info(arg0,arg1);
	}

	@Override
	public void info(String format, Object... args) {
		setAttributes();
		logger.info(format, args);
	}
	
	public void infof(String format, Object... args) {
		setAttributes();
		logger.info(String.format(format, args));
	}

	@Override
	public void info(String arg, Throwable ex) {
		setAttributes();
		logger.info(arg,ex);
	}

	@Override
	public void info(Marker marker, String arg) {
		setAttributes();
		logger.info(marker,arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		setAttributes();
		logger.info(format,arg1,arg2);
	}

	@Override
	public void info(Marker format, String arg1, Object arg2) {
		setAttributes();
		logger.info(format,arg1,arg2);
	}

	@Override
	public void info(Marker marker, String format, Object... args) {
		setAttributes();
		logger.info(marker,format,args);
	}

	@Override
	public void info(Marker marker, String arg1, Throwable ex) {
		setAttributes();
		logger.info(marker,arg1,ex);
	}

	@Override
	public void info(Marker marker, String arg1, Object arg2, Object arg3) {
		setAttributes();
		logger.info(marker,arg1,arg2,arg3);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return logger.isDebugEnabled(marker);
	}

	@Override
	public boolean isErrorEnabled() {
		return isErrorEnabled();
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return logger.isErrorEnabled(marker);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isInfoEnabled();
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return logger.isInfoEnabled(marker);
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isTraceEnabled();
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return logger.isTraceEnabled(marker);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isWarnEnabled();
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return logger.isWarnEnabled(marker);
	}

	public void setLevel(Level level) {
		logger.setLevel(level);
	}
	
	@Override
	public void trace(String arg0) {
		setAttributes();
		logger.trace(arg0);
	}

	@Override
	public void trace(String format, Object arg) {
		setAttributes();
		logger.trace(format,arg);
	}

	@Override
	public void trace(String format, Object... args) {
		setAttributes();
		logger.trace(format,args);
		
	}
	public void tracef(String format, Object... args) {
		setAttributes();
		logger.trace(String.format(format, args));
	}

	@Override
	public void trace(String arg, Throwable ex) {
		setAttributes();
		logger.trace(arg,ex);
	}

	@Override
	public void trace(Marker marker, String arg) {
		setAttributes();
		logger.trace(marker,arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		setAttributes();
		logger.trace(format,arg1,arg2);
	}

	@Override
	public void trace(Marker marker, String arg1, Object arg2) {
		setAttributes();
		logger.trace(marker,arg1,arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object... args) {
		setAttributes();
		logger.trace(marker,format,args);
	}

	@Override
	public void trace(Marker marker, String arg1, Throwable ex) {
		setAttributes();
		logger.trace(marker,arg1,ex);
	}

	@Override
	public void trace(Marker marker, String arg1, Object arg2, Object arg3) {
		setAttributes();
		logger.trace(marker,arg1,arg2,arg3);
	}

	@Override
	public void warn(String arg) {
		setAttributes();
		logger.warn(arg);
	}

	@Override
	public void warn(String format, Object arg) {
		setAttributes();
		logger.warn(format,arg);
	}

	@Override
	public void warn(String format, Object... args) {
		setAttributes();
		logger.warn(format,args);	
	}
	
	public void warnf(String format, Object... args) {
		setAttributes();
		logger.warn(String.format(format, args));
	}

	@Override
	public void warn(String arg0, Throwable ex) {
		setAttributes();
		logger.warn(arg0,ex);
	}

	@Override
	public void warn(Marker marker, String arg1) {
		setAttributes();
		logger.warn(marker,arg1);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		setAttributes();
		logger.warn(format,arg1,arg2);
	}

	@Override
	public void warn(Marker marker, String arg1, Object arg2) {
		setAttributes();
		logger.warn(marker,arg1,arg2);
	}

	@Override
	public void warn(Marker marker, String format, Object... args) {
		setAttributes();
		logger.warn(marker,format,args);
	}

	@Override
	public void warn(Marker marker, String arg1, Throwable ex) {
		setAttributes();
		logger.warn(marker,arg1,ex);
	}

	@Override
	public void warn(Marker marker, String format, Object arg0, Object arg1) {
		setAttributes();
		logger.warn(marker,format,arg0,arg1);
	}

	// ======================== Appender Attachable ========================
	@Override
	public void addAppender(Appender<ILoggingEvent> arg) {
		logger.addAppender(arg);
	}

	@Override
	public void detachAndStopAllAppenders() {
		logger.detachAndStopAllAppenders();
	}

	@Override
	public boolean detachAppender(Appender<ILoggingEvent> appender) {
		return logger.detachAppender(appender);
	}

	@Override
	public boolean detachAppender(String name) {
		return logger.detachAppender(name);
	}

	@Override
	public Appender<ILoggingEvent> getAppender(String name) {
		return logger.getAppender(name);
	}

	@Override
	public boolean isAttached(Appender<ILoggingEvent> appender) {
		return logger.isAttached(appender);
	}

	@Override
	public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
		return logger.iteratorForAppenders();
	}
	
	public void callAppenders(LoggingEvent event) {
		logger.callAppenders(event);
	}
	
    // Place attributes into the MDC just prior to logging
    // MDC = Mapped Diagnostic Contexts
    private void setAttributes() {
        MDC.put(LogMaker.CLIENT_KEY,this.clientId);
        MDC.put(LogMaker.PROJECT_KEY,this.project);
    }
}
