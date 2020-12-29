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

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * The SuppressByMarkerFilter filter allows an appender to be configured
 * to skip processing of events that were generated due to certain turbo filters.
 */
public class SuppressByMarkerFilter extends Filter<ILoggingEvent> {
	private final static String CLSS = "SuppressByMarkerFilter";
	private Marker marker = null;
	
	/**
	 * Specify the marker which, if found, causes the message to be suppressed
	 */
	public void setMarker(String name) {
		marker = MarkerFactory.getMarker(name);
		System.out.println(String.format("%s.setMarker: Suppressing events marked with: %s",CLSS,name));
	}
	public String getMarker() { return this.marker.getName(); }
	/**
	 *  If the event has the configured level or more, pass it
	 *  irrespective of the logger configuration.
	 */
	@Override
	public FilterReply decide(ILoggingEvent event) {
		if( marker!=null ) {
			Marker eventMarkers = event.getMarker();
			if( eventMarkers!=null && eventMarkers.contains(marker)) {
				return FilterReply.DENY;
			}
		}
		return FilterReply.NEUTRAL;  // Normal comparison applies.
	}
}
