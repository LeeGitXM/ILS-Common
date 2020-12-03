/**
 * Logback: the reliable, generic, fast and flexible logging framework.
 * Copyright (C) 1999-2015, QOS.ch. All rights reserved.
 *
 * This program and the accompanying materials are dual-licensed under
 * either the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation
 *
 *   or (per the licensee's choosing)
 *
 * under the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation.
 */
package com.ils.logging.common.filter;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.ils.common.log.LogMaker;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * The pass through filter allows an appender to skip the basic level test and 
 * send the logging event to the appender regardless. This filter applies a series 
 * of tests on the event.
 * 	a) a message on the "current" thread.
 *	b) a message on one of the named threads
 *	c) a message from a logger whoss name matches one of the patterns
 */
public class PassThruFilter extends Filter<ILoggingEvent> {
	private final static String CLSS = "PassThruFilter";
	private static long NO_THREAD = -1;
	private long currentThreadId = NO_THREAD;
	private final List<String> patterns;
	private final List<String> threadNames;
	
	public PassThruFilter() {
		this.patterns = new ArrayList<>();
		this.threadNames = new ArrayList<>();
	}
	
	public void addPattern(String pattern) {
		patterns.add(pattern);
	}
	public void addThread(String name) {
		threadNames.add(name);
	}
	public void setCurrentThread(long id) { this.currentThreadId = id; }

	/**
	 *  If the event has the configured level or more, pass it
	 *  irrespective of the logger configuration.
	 */
	@Override
	public FilterReply decide(ILoggingEvent e) {
		if( e instanceof LoggingEvent) {
			LoggingEvent event= (LoggingEvent) e;
			if( currentThreadId!=NO_THREAD  ||
				threadNames.contains(event.getThreadName()) ||
				patternInName() ){
				return FilterReply.ACCEPT;
			}
		}
		return FilterReply.NEUTRAL;  // Normal comparison applies.
	}
	
	private boolean patternInName() {
		if( patterns.size()>0 ) {
			LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
			for( String pattern:patterns ) {
				for(Logger lgr:logContext.getLoggerList()) {
					if( lgr.getName().contains(pattern)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	public void reset() {
		currentThreadId = NO_THREAD;
		patterns.clear();
		threadNames.clear();
	}
}
