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

import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * The pass through filter allows an appender to skip the basic level test and 
 * send the logging event to the appender regardless. This filter applies a series 
 * of tests on the event.
 * 	a) a message on the "current" thread.
 *	b) a message on one of the named threads
 *	c) a message from a logger whoss name matches one of the patterns
 */
public class PassThruFilter extends TurboFilter {
	private final static String CLSS = "PassThruFilter";
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
	public void passCurrentThread() {
		threadNames.add(Thread.currentThread().getName());
	}
	/**
	 *  If the logger name matches the pattern or the 
	 *  irrespective of the logger level.
	 */
	@Override
	public FilterReply decide(Marker marker, Logger logger, Level level,String format, Object[] params, Throwable t) {
		String threadName = Thread.currentThread().getName();
		if( threadNames.contains(threadName) ||
				patternInName(logger) ){
			return FilterReply.ACCEPT;
		}
		return FilterReply.NEUTRAL;  // Normal comparison applies.
	}

	private boolean patternInName(Logger lgr) {
		for( String pattern:patterns ) {
			if( lgr.getName().contains(pattern)) {
				return true;
			}
		}
		return false;
	}
	
	public void reset() {
		patterns.clear();
		threadNames.clear();
	}
}
