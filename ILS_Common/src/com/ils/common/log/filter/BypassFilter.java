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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/**
 * The bypass filter allows an appender to skip the basic level test and 
 * pass the logging event regardless. Initialize with a high threshold.
 */
public class BypassFilter extends TurboFilter {
	private final static String CLSS = "BypassFilter";
	private Level threshold = Level.ERROR;
	
	public void setThreshold(String thresh) { 
		this.threshold = Level.toLevel(thresh); 
		System.out.println(String.format("%s.setMarker: Passing events greated than %s",CLSS,thresh));
	}
	public String getThreshold() { return this.threshold.levelStr; }

	/**
	 *  If the event level matches or exceeds the configured filter level, then pass
	 *  it, irrespective of the logger level.
	 */
	@Override
	public FilterReply decide(Marker marker, Logger logger, Level level,String format, Object[] params, Throwable t) {
		if( level.isGreaterOrEqual(threshold)) {
			return FilterReply.ACCEPT;
		}
		else {
			return FilterReply.NEUTRAL;  // Normal comparison applies.
		}
	}
}
