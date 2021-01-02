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

import com.ils.common.ILSProperties;
import com.ils.logging.common.LoggingProperties;

import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterReply;

/**
 * The crash filter is a specialized filter designer for use with the crash appender.
 * It acts as a threshold filter to reduce the message levels regurgitated after a crash.
 * It also stops re-propagation of messages created during debugging from, for example,
 * the pattern appender, or the crash appender itself.
 */
public class CrashFilter extends ThresholdFilter {
	private final static String CLSS = "CrashFilter";
	private final Marker logMarker;
	

	public CrashFilter() {
		this.logMarker = MarkerFactory.getMarker(ILSProperties.LOOP_PREVENTION_MARKER_NAME);
		this.setLevel(LoggingProperties.CRASH_APPENDER_THRESHOLD);
	}
	public String getLevel() { return this.getLevel(); }
	/**
	 *  If the event has the configured level or more, pass it
	 *  irrespective of the logger configuration.
	 */
	@Override
	public FilterReply decide(ILoggingEvent event) {
		// First check level
		FilterReply result = super.decide(event);
		if( !result.equals(FilterReply.DENY)) {
			Marker eventMarkers = event.getMarker();
			if( eventMarkers!=null && eventMarkers.contains(logMarker)) {
				result = FilterReply.DENY;
			}
		}
		return result;
	}
}
