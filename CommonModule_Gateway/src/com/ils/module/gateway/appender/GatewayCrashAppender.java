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
package com.ils.module.gateway.appender;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import com.ils.logging.common.CircularLoggingEventBuffer;
import com.ils.logging.common.CommonProperties;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * The crash appender stores logging events in a circular buffer
 * until it detects an exception. At that point it flushes its buffer
 * into the normal database table. A marker is applied to avoid
 * looping should there be an exception during log processing. 
 */
public class GatewayCrashAppender extends GatewaySingleTableDBAppender<ILoggingEvent> {
	private final static String CLSS = "GatewayCrashAppender";
	private CircularLoggingEventBuffer buffer = null;
	private final Marker logMarker;

	public GatewayCrashAppender(String connect,GatewayContext ctx,int bufferSize) {
		super(connect,ctx);
		this.buffer = new CircularLoggingEventBuffer(bufferSize);
		this.logMarker = MarkerFactory.getMarker(CommonProperties.LOOP_PREVENTION_MARKER_NAME);
	}
	
	/**
	 * We simply add the event to the circular buffer, unless there is an exception.
	 * In that case, we iterate through the buffer and add all the messages via the 
	 * normal DB appender. Messages with markers from previous logging are ignored.
	 */
	@Override
	protected void append(ILoggingEvent e) {
		LoggingEvent event = (LoggingEvent)e;
		if( event.getMarker().contains(logMarker )) {
			buffer.add(event);
			if( event.getThrowableProxy()!=null) {
				flush();
			}
		}
	}
	/**
	 * Flush events in the circular buffer into the normal appenders. Add a "crash" marker to allow these to
	 * be filtered out, if desired.
	 */
	private void flush() {
		LoggingEvent[] events = buffer.getValues();
		System.out.println(String.format("%s.flush: Flushing %d messages to main database",CLSS,events.length));
		for( LoggingEvent event:events) {
			event.setMarker(logMarker);
			super.append(event);
		}
		buffer.clear();
	}
	
	public int getBufferSize() { return buffer.size(); }
	
	/**
	 * Changing the buffer size clears the buffer.
	 * @param bufferSize count of messages to store to replay in event of a crash
	 */
	public void setBufferSize(int bufferSize) {
		this.buffer = new CircularLoggingEventBuffer(bufferSize);
	}
}
