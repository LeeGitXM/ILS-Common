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
import org.slf4j.MarkerFactory;

import com.ils.logging.common.CommonProperties;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * This filter provides a way of passing messages regardless of level provided 
 * the message matches certain patterns. This filter applies a series 
 * of tests on the event.
 * 	a) a message on the "current" thread.
 *	b) a message on one of the named threads
 *	c) a message from a logger whose name matches one of the patterns
 * NOTE: This being a turbo filter, all appenders are affected.
 * 
 * The filter adds a marker to messages that are specially enabled
 * because of the pattern.
 */
public class PatternFilter extends TurboFilter {
	private final static String CLSS = "PatternFilter";
	private final List<String> patterns;
	private final List<String> threadNames;
	private final Marker loggingMarker;
	
	public PatternFilter() {
		this.patterns = new ArrayList<>();
		this.threadNames = new ArrayList<>();
		this.loggingMarker = MarkerFactory.getMarker(CommonProperties.LOOP_PREVENTION_MARKER_NAME);
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
	 *  If the logger name matches the pattern irrespective of the logger level we allow it. 
	 *  This filtering is a debugging activity so we mark it as such. This allows subsequent 
	 *  filters to weed out messages that might cause loops.
	 */
	@Override
	public FilterReply decide(Marker marker, Logger logger, Level level,String format, Object[] params, Throwable t) {
		if( marker!=null ) {
			if(marker.contains(loggingMarker)) return FilterReply.DENY;  // We've seen this already
			marker.add(loggingMarker);
		}
		else {
			marker = loggingMarker;
		}
		String threadName = Thread.currentThread().getName();
		if( threadNames.contains(threadName) ||
				patternInName(logger) ){
			return FilterReply.ACCEPT;
		}
		
		//System.out.println(String.format("PatternFilter.decide: %s= %s (%s)",logger.getName(),format,(t==null?"NULL":t.getCause().getMessage())));
		if( format==null || logger==null || logger.getName().isEmpty() ) {
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
