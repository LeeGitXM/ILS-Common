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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * The suppress-by-marker filter is a specialized filter designed for use all except the crash appender.
 * It discards events that are marked as destined for the crash appender only.
 */
public class SuppressByMarkerFilter extends Filter<ILoggingEvent> {
	private final static String CLSS = "CrashFilter";
	private Marker crashMarker;
	

	public SuppressByMarkerFilter() {
		this.crashMarker = MarkerFactory.getMarker(LogMaker.CRASH_MARKER_NAME);
	}
	public String getMarker() { return this.crashMarker.getName(); }
	public void setMarker(String name) { this.crashMarker = MarkerFactory.getMarker(name); }
	/**
	 *  If the event has the configured level or more, pass it
	 *  irrespective of the logger configuration.
	 */
	@Override
	public FilterReply decide(ILoggingEvent event) {
		FilterReply result = FilterReply.NEUTRAL;
		Marker eventMarkers = event.getMarker();
		if( eventMarkers!=null && eventMarkers.contains(crashMarker) ) {
			result = FilterReply.DENY;
		}
		return result;
	}
}
