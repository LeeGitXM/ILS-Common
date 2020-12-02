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
package com.ils.logging.gateway.appender;

import com.ils.log.common.CircularLoggingEventBuffer;
import com.inductiveautomation.ignition.client.model.AbstractClientContext;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * The crash appender stores logging events in a circular buffer
 * until it detects an exception. At that point it flushes its buffer
 * into the normal database table. 
 */
public class ClientCrashAppender extends ClientSingleTableDBAppender<ILoggingEvent> {
	private final static String CLSS = "ClientCrashAppender";
	private final CircularLoggingEventBuffer buffer;
	/**
	 * 
	 * @param connect
	 * @param ctx
	 * @param s scope (client of designer)
	 */
	public ClientCrashAppender(String connect,AbstractClientContext ctx,String s,int bufferSize) {
		super(connect,ctx,s);
		this.buffer = new CircularLoggingEventBuffer(bufferSize);
	}
	
	/**
	 * We simply add the event to the circular buffer, unless there is an exception.
	 * In that case, we iterate through the buffer and add all the messages via the 
	 * normal DB appender. 
	 */
	@Override
	protected void append(ILoggingEvent e) {
		LoggingEvent event = (LoggingEvent)e;
		buffer.add(event);
		if( event.getThrowableProxy()!=null) {
			flush();
		}

	}
	
	private void flush() {
		LoggingEvent[] events = buffer.getValues();
		System.out.println(String.format("%s.flush: Flushing %d messages to main database",CLSS,events.length));
		for( LoggingEvent event:events) {
			super.append(event);
		}
		buffer.clear();
	}
}
