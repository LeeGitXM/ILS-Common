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
package com.ils.module.client.appender;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;

import com.ils.common.db.ClientDBUtility;
import com.ils.common.log.LogMaker;
import com.ils.logging.common.CommonProperties;
import com.ils.logging.common.appender.AbstractSingleTableDBAppender;
import com.inductiveautomation.ignition.client.model.AbstractClientContext;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * This DBAppender inserts logging events into a single database table 
 * in client/designer scope.
 */
public class ClientSingleTableDBAppender<E> extends AbstractSingleTableDBAppender<E> {
	private final static String CLSS = "ClientSingleTableDBAppender";
	private final ClientDBUtility dbUtil;
	private final String db;               // Database connection 
	private final String insertString;
	private String scope;
	private final PatternLayout layout;

	/**
	 * 
	 * @param connect
	 * @param ctx
	 * @param s scope (client or designer)
	 */
	public ClientSingleTableDBAppender(String connect,AbstractClientContext ctx,String s) {
		this.db = connect;  // Datasource
		this.dbUtil = new ClientDBUtility(ctx);
		this.insertString = getInsertString();
		this.scope = s;
		this.layout = new PatternLayout();
		layout.setPattern(CommonProperties.DEFAULT_APPENDER_PATTERN);
	}
	

	@Override
	public void start() {
		//System.out.println(CLSS+ ".start ");
		try {
			String SQL = getTableCreateString();
			dbUtil.executeUpdateQuery(SQL,db);
		}
		catch(SQLException sqle) {
			System.out.println(String.format("%s.start: Error creating prepared statement (%s)",CLSS,sqle.getLocalizedMessage()));
		}
		catch(Exception ex) {
			System.out.println(String.format("%s.start: Exception creating prepared statement (%s)",CLSS,ex.getLocalizedMessage()));
		}
		layout.start();
		super.start();
	}
	
	@Override
	public void stop() {
		super.stop();
		layout.stop();
	}
	
	/**
	 * This is where the work gets done 
	 */
	@Override
	protected synchronized void append(E e) {
		if( e instanceof LoggingEvent) {
			LoggingEvent event = (LoggingEvent)e;
			if( event.getLoggerName().equalsIgnoreCase("OutputConsole")) return;
			String output = layout.doLayout(event);
			System.out.println(output);
			Object[] args = new Object[16];
			try {
				Map<String, String> map = event.getMDCPropertyMap();
				StackTraceElement caller = null;
				if( event.hasCallerData() ) caller = extractFirstCaller(event.getCallerData());
				else caller = extractFirstCaller();
				args[0] = 0;   										  // pid
				args[1] = Thread.currentThread().getId();   		  // thread
				args[2] = truncate(map.get(LogMaker.PROJECT_KEY),25); // project
				args[3] = scope;   								      // scope
				args[4] = truncate(map.get(LogMaker.CLIENT_KEY),25);  // client
				args[5] = truncate(event.getThreadName(),50);   	// thread name
				args[6] = truncate(caller.getClassName(),50);   	// module
				args[7] = truncate(event.getLoggerName(),50);   	// logger
				args[8] =  new Timestamp(event.getTimeStamp());   	// timestamp
				args[9] = event.getLevel().levelInt;   				// level
				args[10]= event.getLevel().levelStr;   				// level name
				args[11]= truncate(event.getFormattedMessage(),4000);  // log message
				args[12]= truncate(caller.getMethodName(),25);   	// function name
				args[13]= truncate(caller.getFileName(),25);   		// filename
				args[14]= caller.getLineNumber();   				// line number
				args[15]= computeRetentionTIme(event);   			// retain until
				dbUtil.executePreparedStatement(insertString, db, args);
			}
			catch( Exception sqle ) {
				Object msg = sqle.getLocalizedMessage();
				if( msg == null )  msg = sqle.getCause();
				if( msg == null )  msg = event.getFormattedMessage();
				if( msg!=null) {
					System.out.println(String.format("%s.append: Exception setting prepared statement (%s)",CLSS,msg.toString()));
				}
			}
		}
	}
}
