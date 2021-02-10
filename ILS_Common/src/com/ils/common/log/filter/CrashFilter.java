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


import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.ils.common.log.LogMaker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * This filter marks low priority pre-events with a marker that all appenders
 * except for the crash appender filter out.
 */
public class CrashFilter extends TurboFilter {
	private final static String CLSS = "CrashFilter";
	private final Marker crashMarker;
	
	public CrashFilter() {
		this.crashMarker = MarkerFactory.getMarker(LogMaker.CRASH_MARKER_NAME);
	}
	
	/**
	 *  If the level is lower priority than the logger level, then mark it for crash appender only.
	 */
	@Override
	public FilterReply decide(Marker marker, Logger logger, Level level,String format, Object[] params, Throwable t) {
		if( marker!=null ) {
			if(marker.contains(crashMarker)) return FilterReply.DENY;  // We've seen this already
			marker.add(crashMarker);
		}
		
		return FilterReply.ACCEPT;  // All are accepted in prep for crash reporter
	}
}
