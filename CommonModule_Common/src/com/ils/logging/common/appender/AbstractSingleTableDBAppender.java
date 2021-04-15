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
package com.ils.logging.common.appender;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import org.slf4j.MDC;

import com.ils.common.log.LogMaker;
import com.ils.logging.common.CommonProperties;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * This DBAppender inserts logging events into a single database table.
 */
public abstract class AbstractSingleTableDBAppender<E> extends UnsynchronizedAppenderBase<E> {
	private final static String CLSS = "AbstractSingleTableDBAppender";
	protected Connection cxn = null;       // Database connection 
	protected PreparedStatement ps  = null;
	protected final StackTraceElement EMPTY_CALLER_DATA = CallerData.naInstance();
	protected double[] retentionTimes = new double[5];
	
	public AbstractSingleTableDBAppender() {
		this.retentionTimes[0]= CommonProperties.ERROR_DEFAULT_RETENTION;
		this.retentionTimes[1]=CommonProperties.WARNING_DEFAULT_RETENTION;
		this.retentionTimes[2]=CommonProperties.INFO_DEFAULT_RETENTION;
		this.retentionTimes[3]=CommonProperties.DEBUG_DEFAULT_RETENTION;
		this.retentionTimes[4]=CommonProperties.TRACE_DEFAULT_RETENTION;
	}
	/** 
	 * Create the table if it doesn't exist. We simply ignore any error
	 */
	protected String getTableCreateString() {
		String SQL = "CREATE TABLE log (" +
				"id int PRIMARY KEY auto_increment not null,"+
				"process_id int NULL," +
				"thread bigint NULL,"+
				"project char(25) NULL,"+
				"scope char(10) NULL,"+
				"client_id char(12) NULL,"+
				"thread_name char(50) NULL,"+
				"module char(250) NULL,"+
				"logger_name char(100) NOT NULL,"+
				"timestamp datetime NOT NULL,"+
				"log_level int NULL,"+
				"log_level_name char(10) NULL,"+
				"log_message varchar(8000) NOT NULL,"+
				"function_name varchar(100) NULL,"+
				"line_number int NULL,"+
				"retain_until datetime NOT NULL" +
				")";
		return SQL;
	}
	
	protected String getInsertString() {
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO log ");
		sb.append("(process_id,thread,project,scope,client_id,thread_name,module,logger_name,timestamp, ");
		sb.append("log_level,log_level_name,log_message,function_name,line_number,retain_until) ");
		sb.append("VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		return sb.toString();
	}
	
	public double[] getRetentionTimes() { return retentionTimes; }
	public void setRetentionTimes(double[] times)   { retentionTimes = times; }

	@Override
	public void stop() {
		if( cxn!=null ) {
			try {
				//ps.close();
				cxn.close();
			}
			catch( SQLException ignore ) {}
			cxn = null;
		}
		super.stop();
	}

	protected StackTraceElement extractFirstCaller() {
		StackTraceElement[] callerDataArray = Thread.currentThread().getStackTrace();
		StackTraceElement caller = EMPTY_CALLER_DATA;
		if( callerDataArray!=null ) {
			// Search through the stack until we find an element that does not involve logging
			for(StackTraceElement e:callerDataArray) {
				String method = e.getClassName();
				if( method.contains("SingleTableDBAppender")||
						method.contains("ILSLogger") 		||
						method.contains("LoggerEx") 		||
						method.contains("qos.logback.") 			||
						method.contains("util.Logger.") 			||
						method.contains("Thread") ) continue;
				else {
					caller = e;
					break;
				}
			}
		}
		return caller;
	}
	
	protected StackTraceElement extractFirstCaller(StackTraceElement[] callerDataArray) {
		StackTraceElement caller = EMPTY_CALLER_DATA;
		// Search through the stack until we find an element that does not involve logging
		if( callerDataArray!=null ) {
			for(StackTraceElement e:callerDataArray) {
				String method = e.getClassName();
				if( method.contains("SingleTableDBAppender")||
						method.contains("ILSLogger") 		||
						method.contains("LoggerEx") 		||
						method.contains("qos.logback.") 	||
						method.contains("util.Logger") 		||  // Includes LoggerEx
						method.contains("Thread") ) continue;
				else {
					caller = e;
					break;
				}
			}
		}
		return caller;
	}
	
	/**
	 * This is where the work gets done.
	 * By default, do not send to the console. We let the ConsoleAppender
	 * do that. It gives the user more control.
	 */
	@Override
	protected synchronized void append(E e) {
		if( e instanceof LoggingEvent) {
			LoggingEvent event = (LoggingEvent)e;
			//System.out.println(event.getFormattedMessage());

			try {
				Map<String, String> map = event.getMDCPropertyMap();
				StackTraceElement caller = null;
				if( event.hasCallerData() ) caller = extractFirstCaller(event.getCallerData());
				else caller = extractFirstCaller();
				ps.setInt(1, 0 );   											// pid
				ps.setLong(2, Thread.currentThread().getId() );   			// thread
				ps.setString(3, truncate(findProject(map),25)); 			// project
				ps.setString(4, "gateway");   								// scope
				ps.setString(5, "" );   									// client
				ps.setString(6, truncate(event.getThreadName(),50));   		// thread name
				ps.setString(7, truncate(findModule(caller),250) );   		// module
				ps.setString(8, truncate(event.getLoggerName(),100) );   	// logger
				ps.setTimestamp(9, new Timestamp(event.getTimeStamp()));   	// timestamp
				ps.setInt(10,event.getLevel().levelInt);   					// level
				ps.setString(11, event.getLevel().levelStr );   				// level name
				ps.setString(12, truncate(event.getFormattedMessage(),8000) );   // log message
				ps.setString(13, truncate(findMethod(caller),100));   		// function name
				ps.setString(14, truncate(findLine(caller),10 ));   		// line number
				ps.setTimestamp(15, computeRetentionTime(event));   		// retain until
				ps.execute();
			}
			catch( SQLException sqle ) {
				Object msg = sqle.getLocalizedMessage();
				if( msg == null )  msg = sqle.getCause();
				if( msg == null )  msg = event.getFormattedMessage();
				if( msg!=null) {
					System.out.println(String.format("%s.append: Exception setting prepared statement (%s)",CLSS,msg.toString()));
				}
			}
		}
	}
	// Retrieve the clientId name from either the event-specific MDC or the global.
	protected String findClient(Map<String, String> map ) {
		String result = map.get(LogMaker.CLIENT_KEY);
		if( result==null ) result = MDC.get(LogMaker.CLIENT_KEY);
		return result;
	}

	// Retrieve the method (java) or the function (python) from either the map or call stack element
	protected String findLine(StackTraceElement caller ) {
		String result = MDC.get(LogMaker.LINE_KEY);
		if( result==null) result = String.valueOf(caller.getLineNumber());
		return result;
	}
	// Retrieve the method (java) or the function (python) from either the map or call stack element
	protected String findMethod(StackTraceElement caller ) {
		String result = MDC.get(LogMaker.FUNCTION_KEY);
		if( result==null) result = caller.getMethodName();
		return result;
	}
	// Retrieve the method (java) or the function (python) from either the map or call stack element
	protected String findModule(StackTraceElement caller ) {
		String result = MDC.get(LogMaker.MODULE_KEY);
		if( result==null) result = caller.getClassName();
		return result;
	}
	// Retrieve the project name from either the event-specific MDC or the global.
	protected String findProject(Map<String, String> map ) {
		String result = map.get(LogMaker.PROJECT_KEY);
		if( result==null ) result = MDC.get(LogMaker.PROJECT_KEY);
		return result;
	}
	
	/**
	 * Compute a retention date dependent on the severity of the message.
	 * The retention time array has units of days. Need to convert to msecs
	 */
	protected Timestamp computeRetentionTime(ILoggingEvent event) {
		long time = event.getTimeStamp();
		if(      event.getLevel().levelStr.equalsIgnoreCase("ERROR") ) { time = time + (long)(retentionTimes[0]*24*3600*1000); }
		else if( event.getLevel().levelStr.equalsIgnoreCase("WARN") )  { time = time + (long)(retentionTimes[1]*24*3600*1000); }
		else if( event.getLevel().levelStr.equalsIgnoreCase("INFO") )  { time = time + (long)(retentionTimes[2]*24*3600*1000); }
		else if( event.getLevel().levelStr.equalsIgnoreCase("DEBUG") ) { time = time + (long)(retentionTimes[3]*24*3600*1000); }
		else if( event.getLevel().levelStr.equalsIgnoreCase("TRACE") ) { time = time + (long)(retentionTimes[4]*24*3600*1000); }
		return new Timestamp(time);
	}
	/**
	 * Limit a string's length
	 */
	protected String truncate(String in,int limit) {
		String out = "";
		if( in!=null ) {
			if( in.length()< limit) {
				out = in;
			}
			else {
				out = in.substring(0,limit);
			}
		}
		return out;
	}
}
