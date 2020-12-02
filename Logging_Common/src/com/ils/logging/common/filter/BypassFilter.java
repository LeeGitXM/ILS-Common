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


import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * The bypass filter allows an appender to skip the basic level test and 
 * send the logging event to the appender regardless.
 */
public class BypassFilter extends Filter<ILoggingEvent> {
	private final static String CLSS = "BypassFilter";
	private Level threshold = Level.TRACE;
	
	public void setThreshold(Level thresh) { this.threshold = thresh; }

	/**
	 *  If the event has the configured level or more, pass it
	 *  irrespective of the logger configuration.
	 */
	@Override
	public FilterReply decide(ILoggingEvent event) {
		if( event.getLevel().isGreaterOrEqual(threshold)) {
			return FilterReply.ACCEPT;
		}
		else {
			return FilterReply.NEUTRAL;  // Normal comparison applies.
		}
	}
}
