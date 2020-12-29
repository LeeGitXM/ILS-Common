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
package com.ils.common.log.filter;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * The pass through filter provides a way of passing messages regardless of level provided 
 * the message matches certain patterns. This filter applies a series 
 * of tests on the event.
 * 	a) a message on the "current" thread.
 *	b) a message on one of the named threads
 *	c) a message from a logger whose name matches one of the patterns
 * NOTE: This being a turbo filter, all appenders are affected.
 */
public class PassThruFilter extends TurboFilter {
	private final static String CLSS = "PassThruFilter";
	private final List<String> patterns;
	private final List<String> threadNames;
	//private final Marker crashMarker;
	
	public PassThruFilter() {
		this.patterns = new ArrayList<>();
		this.threadNames = new ArrayList<>();
		//this.crashMarker = MarkerFactory.getMarker(LoggingProperties.CRASH_MARKER);
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
	 *  irrespective of the logger level. This filtering is a
	 *  debugging activity, so we don't need to pass on crash flush actions.
	 *  Besides these cause an infinite loop.
	 */
	@Override
	public FilterReply decide(Marker marker, Logger logger, Level level,String format, Object[] params, Throwable t) {
		String threadName = Thread.currentThread().getName();
		if( threadNames.contains(threadName) ||
				patternInName(logger) ){
			//if( marker.contains(crashMarker)) return FilterReply.DENY;
			return FilterReply.ACCEPT;
		}
		
		//System.out.println(String.format("PassThruFilter.decide: %s= %s (%s)",logger.getName(),format,(t==null?"NULL":t.getCause().getMessage())));
		if( format==null || logger==null || logger.getName().isEmpty() ) {
			//System.out.println(String.format("PassThruFilter.decide: empty logger or message"));
			return FilterReply.NEUTRAL;
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
