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


import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.ils.logging.common.CommonProperties;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * The crash filter is a specialized filter designer for use with the crash appender.
 * It acts as a threshold filter to reduce the message levels regurgitated after a crash.
 * It also stops re-propagation of messages created during debugging from, for example,
 * the pattern appender, or the crash appender itself.
 */
public class CrashFilter extends Filter<ILoggingEvent> {
	private final static String CLSS = "CrashFilter";
	private final Marker logMarker;
	private Level threshold;
	

	public CrashFilter() {
		this.logMarker = MarkerFactory.getMarker(CommonProperties.LOOP_PREVENTION_MARKER_NAME);
		this.threshold = CommonProperties.DEFAULT_CRASH_APPENDER_THRESHOLD;
	}
	public String getThreshold() { return this.threshold.levelStr; }
	public void setThreshold(String thresh) { this.threshold = Level.toLevel(thresh.toLowerCase()); }
	/**
	 *  If the event has the configured level or more, pass it
	 *  irrespective of the logger configuration.
	 */
	@Override
	public FilterReply decide(ILoggingEvent event) {
		FilterReply result = FilterReply.NEUTRAL;
		// First check level
		if( !event.getLevel().isGreaterOrEqual(threshold) ) {
			result = FilterReply.DENY;
		}
		else  {
			Marker eventMarkers = event.getMarker();
			if( eventMarkers!=null && eventMarkers.contains(logMarker)) {
				result = FilterReply.DENY;
			}
		}
		return result;
	}
}
