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

import java.sql.SQLException;

import com.ils.common.db.DBUtility;
import com.ils.log.common.appender.AbstractSingleTableDBAppender;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

/**
 * This DBAppender inserts logging events into a single database table\
 * in gateway scope
 */
public class GatewaySingleTableDBAppender<E> extends AbstractSingleTableDBAppender<E> {
	private final static String CLSS = "GatewaySingleTableDBAppender";
	private final DBUtility dbUtil;
	private final GatewayContext context;
	private final String db;               // Database connection 

	public GatewaySingleTableDBAppender(String connect,GatewayContext ctx) {
		this.context = ctx;
		this.db = connect;
		this.dbUtil = new DBUtility(context);
	}

	@Override
	public void start() {
		cxn = dbUtil.getConnection(db);
		if( cxn!=null ) {
			try {
				String SQL = getTableCreateString();
				dbUtil.executeSQL(SQL, db,cxn);
				
				SQL = getInsertString();
				ps = cxn.prepareStatement(SQL);
			}
			catch(SQLException sqle) {
				System.out.println(String.format("%s.start: Error creating prepared statement (%s)",CLSS,sqle.getCause()));
			}
		}
		else {
			System.out.println(String.format("%s.start: Failed to open datasource connection(%s)",CLSS,db));
		}
		super.start();
	}
}
