/**
 *   (c) 2018-2021  ILS Automation. All rights reserved.
 */
package com.ils.common.log;

import java.util.Iterator;

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachable;


/**
 * The project-aware logger encapsulates a logback logger and maintains the name of its project. 
 * The logger is encased as a member because the logback logger class is final.
 * Change the formatting style from {} to % if the ...f method name is used.
 */
public class ILSLogger implements org.slf4j.Logger,AppenderAttachable<ILoggingEvent> {
	private final Logger logger;
	
	public ILSLogger(Logger lgr) { this.logger = lgr; }
	

	@Override
	public void debug(String arg) {
		logger.debug(arg);
	}

	@Override
	public void debug(String format, Object arg) {
		logger.debug(format,arg);
	}

	@Override
	public void debug(String format, Object... args) {
		logger.debug(format,args);
		
	}
	public void debugf(String format, Object... args) {
		logger.debug(String.format(format, args));
	}

	@Override
	public void debug(String arg, Throwable ex) {
		logger.debug(arg,ex);
	}

	@Override
	public void debug(Marker marker, String arg) {
		logger.debug(marker,arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		logger.debug(format,arg1,arg2);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		logger.debug(marker,format,arg);	
	}

	@Override
	public void debug(Marker marker, String format, Object... args) {
		logger.debug(marker,format,args);
	}

	@Override
	public void debug(Marker marker, String arg1, Throwable ex) {
		logger.debug(marker,arg1,ex);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		logger.debug(marker,format,arg1,arg2);
	}

	@Override
	public void error(String arg) {
		logger.error(arg);
	}

	@Override
	public void error(String format, Object arg1) {
		logger.error(format,arg1);
	}

	@Override
	public void error(String format, Object... args) {
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
		logger.error(marker,arg);	
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		logger.error(format,arg1,arg2);
	}

	@Override
	public void error(Marker marker, String arg1, Object arg2) {
		logger.error(marker,arg1,arg2);
	}

	@Override
	public void error(Marker marker, String format, Object... args) {
		logger.error(marker,format,args);
	}

	@Override
	public void error(Marker marker, String arg1, Throwable ex) {
		logger.error(marker,arg1,ex);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
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
		logger.info(arg);
	}

	@Override
	public void info(String arg0, Object arg1) {
		logger.info(arg0,arg1);
	}

	@Override
	public void info(String format, Object... args) {
		logger.info(format, args);
	}
	
	public void infof(String format, Object... args) {
		logger.info(String.format(format, args));
	}

	@Override
	public void info(String arg, Throwable ex) {
		logger.info(arg,ex);
	}

	@Override
	public void info(Marker marker, String arg) {
		logger.info(marker,arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		logger.info(format,arg1,arg2);
	}

	@Override
	public void info(Marker format, String arg1, Object arg2) {
		logger.info(format,arg1,arg2);
	}

	@Override
	public void info(Marker marker, String format, Object... args) {
		logger.info(marker,format,args);
	}

	@Override
	public void info(Marker marker, String arg1, Throwable ex) {
		logger.info(marker,arg1,ex);
	}

	@Override
	public void info(Marker marker, String arg1, Object arg2, Object arg3) {
		logger.info(marker,arg1,arg2,arg3);
	}

	@Override
	public boolean isDebugEnabled() {
		return isDebugEnabled();
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return isDebugEnabled(marker);
	}

	@Override
	public boolean isErrorEnabled() {
		return isErrorEnabled();
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return isErrorEnabled(marker);
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
		logger.trace(arg0);
	}

	@Override
	public void trace(String format, Object arg) {
		logger.trace(format,arg);
	}

	@Override
	public void trace(String format, Object... args) {
		logger.trace(format,args);
		
	}
	public void tracef(String format, Object... args) {
		logger.trace(String.format(format, args));
	}

	@Override
	public void trace(String arg, Throwable ex) {
		logger.trace(arg,ex);
	}

	@Override
	public void trace(Marker marker, String arg) {
		logger.trace(marker,arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		logger.trace(format,arg1,arg2);
	}

	@Override
	public void trace(Marker marker, String arg1, Object arg2) {
		logger.trace(marker,arg1,arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object... args) {
		logger.trace(marker,format,args);
	}

	@Override
	public void trace(Marker marker, String arg1, Throwable ex) {
		logger.trace(marker,arg1,ex);
	}

	@Override
	public void trace(Marker marker, String arg1, Object arg2, Object arg3) {
		logger.trace(marker,arg1,arg2,arg3);
	}

	@Override
	public void warn(String arg) {
		logger.warn(arg);
	}

	@Override
	public void warn(String format, Object arg) {
		logger.warn(format,arg);
	}

	@Override
	public void warn(String format, Object... args) {
		logger.warn(format,args);	
	}
	
	public void warnf(String format, Object... args) {
		logger.warn(String.format(format, args));
	}

	@Override
	public void warn(String arg0, Throwable ex) {
		logger.warn(arg0,ex);
	}

	@Override
	public void warn(Marker marker, String arg1) {
		logger.warn(marker,arg1);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		logger.warn(format,arg1,arg2);
	}

	@Override
	public void warn(Marker marker, String arg1, Object arg2) {
		logger.warn(marker,arg1,arg2);
	}

	@Override
	public void warn(Marker marker, String format, Object... args) {
		logger.warn(marker,format,args);
	}

	@Override
	public void warn(Marker marker, String arg1, Throwable ex) {
		logger.warn(marker,arg1,ex);
	}

	@Override
	public void warn(Marker marker, String format, Object arg0, Object arg1) {
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
	
}
