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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Map;

import com.ils.common.db.DBUtility;
import com.ils.common.log.LogMaker;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

import ch.qos.logback.classic.spi.CallerData;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * This DBAppender inserts logging events into a single database table.
 */
public class SingleTableDBAppender<E> extends UnsynchronizedAppenderBase<E> {
	private final static String CLSS = "SingleTableDBAppender";
	private final DBUtility dbUtil;
	private Connection cxn = null;
	private final GatewayContext context;
	private final String db;               // Database connection 
	private PreparedStatement ps  = null;
	private final StackTraceElement EMPTY_CALLER_DATA = CallerData.naInstance();

	public SingleTableDBAppender(String connect,GatewayContext ctx) {
		this.context = ctx;
		this.db = connect;
		this.dbUtil = new DBUtility(context);


	}

	/** 
	 * Create the table if it doesn't exist. We simply ignore any error
	 */
	private void initializeTable(String source, Connection connection) {
		String SQL = "CREATE TABLE log (" +
				"id int PRIMARY KEY auto_increment not null,"+
				"process_id int NULL," +
				"thread bigint NULL,"+
				"project char(25) NULL,"+
				"scope char(10) NULL,"+
				"client_id char(12) NULL,"+
				"thread_name char(50) NULL,"+
				"module char(50) NULL,"+
				"logger_name char(50) NOT NULL,"+
				"timestamp datetime NOT NULL,"+
				"log_level int NULL,"+
				"log_level_name char(10) NULL,"+
				"log_message varchar(4000) NOT NULL,"+
				"function_name varchar(25) NULL,"+
				"filename varchar(25) NULL,"+
				"line_number int NULL,"+
				"retain_until datetime NOT NULL" +
				")";
		
		dbUtil.executeSQL(SQL, source, connection);
	}
	@Override
	public void start() {
		System.out.println(CLSS+""
				+ ""
				+ ".start ");
		cxn = dbUtil.getConnection(db);
		if( cxn!=null ) {
			try {
				initializeTable(db,cxn);
				StringBuilder sb = new StringBuilder();
				sb.append("INSERT INTO log ");
				sb.append("(process_id,thread,project,scope,client_id,thread_name,module,logger_name,timestamp, ");
				sb.append("log_level,log_level_name,log_message,function_name,filename,line_number,retain_until) ");
				sb.append("VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
				String SQL = sb.toString();
				ps = cxn.prepareStatement(SQL);
			}
			catch(SQLException sqle) {
				System.out.println(String.format("%s.start: Error creating prepared statement (%s)",CLSS,sqle.getLocalizedMessage()));
			}
		}
		else {
			System.out.println(String.format("%s.start: Failed to open datasource connection(%s)",CLSS,db));
		}
		super.start();
	}

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

	private StackTraceElement extractFirstCaller() {
		StackTraceElement[] callerDataArray = Thread.currentThread().getStackTrace();
		StackTraceElement caller = EMPTY_CALLER_DATA;
		if( callerDataArray.length>0) caller = callerDataArray[0];
		return caller;
	}

	/**
	 * This is where the work gets done to 
	 */
	@Override
	protected void append(E e) {
		if( e instanceof LoggingEvent) {
			LoggingEvent event = (LoggingEvent)e;
			System.out.println(String.format("%s.append: %s",CLSS,event.getFormattedMessage()));

			try {
				Map<String, String> map = event.getMDCPropertyMap();
				StackTraceElement caller = extractFirstCaller();
				ps.setInt(1, 0 );   											// pid
				ps.setLong(2, Thread.currentThread().getId() );   			// thread
				ps.setString(3, map.get(LogMaker.PROJECT_KEY));             // project
				ps.setString(4, "gateway");   								// scope
				ps.setInt(5, -1 );   										// client
				ps.setString(6, event.getThreadName());   					// thread name
				ps.setString(7, caller.getClassName() );   					// module
				ps.setString(8, event.getLoggerName() );   					// logger
				ps.setTimestamp(9, new Timestamp(event.getTimeStamp()));   	// timestamp
				ps.setInt(10,event.getLevel().levelInt);   					// level
				ps.setString(11, event.getLevel().levelStr );   				// level name
				ps.setString(12, event.getFormattedMessage() );   			// log message
				ps.setString(13, caller.getMethodName());   					// function name
				ps.setString(14, truncate(caller.getFileName(),25));   		// filename
				ps.setInt(15, caller.getLineNumber() );   					// line number
				ps.setTimestamp(16, computeRetentionTIme(event));   			// retain until
				ps.execute();
			}
			catch( SQLException sqle ) {
				System.out.println(String.format("%s.append: Exception setting prepared statement (%s)",CLSS,sqle.getMessage()));
			}
		}
	}
	
	/**
	 * Compute a retention date dependent on the severity of the message
	 */
	private Timestamp computeRetentionTIme(ILoggingEvent event) {
		long time = event.getTimeStamp();
		return new Timestamp(time);
	}
	/**
	 * Limit a string's length
	 */
	private String truncate(String in,int limit) {
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
