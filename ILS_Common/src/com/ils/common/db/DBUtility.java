package com.ils.common.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.inductiveautomation.ignition.common.datasource.DatasourceStatus;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.datasource.Datasource;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;



/**
 * This class is a collection of utilities for database access in the gateway.
 * Unlike in the client, we can execute DDL and query structure.
 */
public class DBUtility {
	private final static String TAG = "TestFrameGatewayRpcDispatcher";
	private final LoggerEx log;
	private final GatewayContext context;
	
	public DBUtility(GatewayContext ctx) {
		this.context = ctx;
		this.log = LogUtil.getLogger(getClass().getPackage().getName());
	}

	/**
	 * Execute a sql statement against the named datasource.
	 * The statement may be DDL (e.g. create table())
	 * @param source the named datasource
	 * @param sql command to execute
	 */
	public void executeSQL(String source,String sql) {
		Connection cxn = getConnection(source);
		if( cxn!=null ) {
			try {
				Statement stmt = cxn.createStatement();
				stmt.executeUpdate(sql);
			}
			catch(SQLException sqle) {
				log.warnf("%s.executeSQL: Exception executing %s (%s)",TAG,sql,sqle.getMessage());
			}
		}
		else {
			log.warnf("%s.executeSQL: Datasource %s not found",TAG,source);
		}
	}
	
	public Connection getConnection(String name) {
		Connection cxn = null;
		Datasource ds = context.getDatasourceManager().getDatasource(name);
		if( ds!=null ) {
			try {
				cxn = ds.getConnection();
			}
			catch(SQLException sqle) {
				log.warnf("%s.getConnection: Exception finding connection %s (%s)",TAG,name,sqle.getMessage());
			}
		}
		return cxn;
	}
	
	/**
	 * @return the names of all valid database connections.
	 */
	public List<String> getDatasourceNames() {
		List<String> names = new ArrayList<>();
		List<Datasource> datasources = context.getDatasourceManager().getDatasources();
		for(Datasource source:datasources) {
			if( source.getStatus().equals(DatasourceStatus.VALID)) {
				names.add(source.getName());
			}
		}
		return names;
	}
	
	/**
	 * @return a list of tables in the named datasource.
	 */
	public List<String> getTableNames(String source) {
		List<String> tables = new ArrayList<>();
		Connection cxn = getConnection(source);
		if(cxn!=null) {
			try {
				DatabaseMetaData md = cxn.getMetaData();
				ResultSet rs = md.getTables(null, null, "%", null);
				while (rs.next()) {
					  tables.add(rs.getString("TABLE_NAME"));  // column 3
				}
			}
			catch(SQLException sqle) {
				log.warnf("%s.getTableNames: Exception finding metadata for %s (%s)",TAG,source,sqle.getMessage());
			}
		}
		else {
			log.warnf("%s.getTableNames: Datasource %s not found",TAG,source);
		}
		return tables;
	}
	/**
	 * @return a list of columns in the named table.
	 */
	public List<String> getColumnNames(String source,String table) {
		List<String> columns = new ArrayList<>();
		Connection cxn = getConnection(source);
		if(cxn!=null) {
			try {
				DatabaseMetaData md = cxn.getMetaData();
				ResultSet rs = md.getColumns(null, null, table, null);
				while (rs.next()) {
					  columns.add(rs.getString("COLUMN_NAME"));  // See also "TYPE_NAME"
				}
			}
			catch(SQLException sqle) {
				log.warnf("%s.getTableNames: Exception finding metadata for %s (%s)",TAG,source,sqle.getMessage());
			}
		}
		else {
			log.warnf("%s.getTableNames: Datasource %s not found",TAG,source);
		}
		return columns;
	}
	
}
