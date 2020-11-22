package com.ils.common.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.inductiveautomation.ignition.common.datasource.DatasourceStatus;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.datasource.Datasource;
import com.inductiveautomation.ignition.gateway.datasource.SRConnection;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;



/**
 * This class is a collection of utilities for database access in the gateway.
 * Unlike in the client, we can execute DDL and query structure.
 * 
 * The execute and query functions operate in two modes. If the connection
 * parameter is null, then the functions open (and close) a connection.
 * Alternatively for better performance, the calling method can supply a connection.
 * In this case the connection is simply used. It is then up to the caller to
 * close. Failure to do so will very quickly lead a resource exhaustion.
 */
public class DBUtility {
	private final static String TAG = "DBUtility";
	private final int BATCH_SIZE = 100;
	private final LoggerEx log;
	private final GatewayContext context;
	
	public DBUtility(GatewayContext ctx) {
		this.context = ctx;
		this.log = LogUtil.getLogger(getClass().getPackage().getName());
	}
	/** 
	 * Any time a connection is "gotten", it should be closed.
	 * @param cxn
	 */
	public void closeConnection(Connection cxn) {
		if( cxn!=null ) {
			try {
				cxn.close();
			}
			catch(SQLException sqle) {
				log.warnf("%s.closeConnection: Exception closing connection (%s)",TAG,sqle.getMessage());
			}
		}
	}
	/**
	 * Execute a sql statement against the named datasource.
	 * The statement may be DDL (e.g. create table())
	 *
	 * @param sql command to execute
	 * @param source a named data-source
	 * @param suppliedConnection a database connection. If null the method will manage.
	 */
	public void executeSQL(String sql,String source,Connection suppliedConnection) {
		Connection cxn = suppliedConnection;
		if( cxn==null ) cxn = getConnection(source);
		if( cxn!=null ) {
			try {
				Statement stmt = cxn.createStatement();
				stmt.executeUpdate(sql);
				stmt.close();
			}
			catch(SQLException sqle) {
				log.warnf("%s.executeSQL: Exception executing %s (%s)",TAG,sql,sqle.getMessage());
			}
			finally {
				if( suppliedConnection==null ) closeConnection(cxn);
			}
		}
		else {
			log.warnf("%s.executeSQL: Datasource %s not found",TAG,source);
		}
	}
	/**
	 * Execute an array of sql statements against the named datasource.
	 * The statements may be DDL (e.g. create table()). On completion
	 * a status string is returned.
	 *
	 * @param sql command to execute
	 * @param source a named data-source
	 * @param mysql
	 * @return a result message on error, otherwise an empty string.
	 */
	public String executeMultilineSQL(String[] lines,String database) {
		return executeMultilineSQL(lines,database,DBMS.ANSI);
	}
	/**
	 * Execute an array of sql statements against the named datasource.
	 * The statements may be DDL (e.g. create table()). On completion
	 * a status string is returned.
	 *
	 * @param sql command to execute
	 * @param source a named data-source
	 * @param mysql
	 * @return a result message on error, otherwise an empty string.
	 */
	public String executeMultilineSQL(String[] lines,String database,DBMS dbms) {
		String result = "";
		Datasource ds = context.getDatasourceManager().getDatasource(database);
		if( ds!=null ) {
			Connection cxn = null;
			int index = 0;
			String sql = "";
			try {
				cxn = ds.getConnection();
				cxn.setAutoCommit(false);
				Statement stmt = cxn.createStatement();
				stmt.setQueryTimeout(300);    // 5 minutes
				int count = lines.length;
				int batchCount = 0;
				
				while( index<count ) {
					sql = lines[index];
					index++;
					// Scrub comments
					sql = scrubSQL(sql,dbms);
					if( sql.isEmpty() ) continue;
					stmt.executeUpdate(sql);
					batchCount++;
					if( batchCount>BATCH_SIZE) {
						batchCount = 0;
						cxn.commit();
					}
				}
				cxn.commit();
				stmt.close();
			}
			catch(SQLTimeoutException sqlte) {
				result = String.format("SQLTimeoutException: line %d executing %s\n(%s: %s)",index-1,sql,database,sqlte.getLocalizedMessage());
				log.warnf("%s.executeMultilineSQL %s",TAG,result);
			}
			catch(SQLException sqle) {
				result = String.format("SQLException: line %d executing %s\n(%s: %s)",index-1,sql,database,sqle.getLocalizedMessage());
				log.warnf("%s.executeMultilineSQL %s",TAG,result);
			}
			finally {
				closeConnection(cxn);
			}
		}
		else {
			result = String.format("Datasource %s does not exist", database);
		}
		return result;
	}
	
	public Connection getConnection(String name) {
		Connection cxn = null;
		Datasource ds = context.getDatasourceManager().getDatasource(name);
		if( ds!=null ) {
			try {
				log.debugf("%s.getConnection: Status is %s (%d of %d busy)",TAG,ds.getStatus().name(),
						ds.getActiveConnections(),ds.getMaxConnections());
				cxn = ds.getConnection();
				cxn.setAutoCommit(true);
				cxn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
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
				rs.close();
			}
			catch(SQLException sqle) {
				log.warnf("%s.getTableNames: Exception finding metadata for %s (%s)",TAG,source,sqle.getMessage());
			}
			finally {
				closeConnection(cxn);
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
				rs.close();
			}
			catch(SQLException sqle) {
				log.warnf("%s.getTableNames: Exception finding metadata for %s (%s)",TAG,source,sqle.getMessage());
			}
			finally {
				closeConnection(cxn);
			}
		}
		else {
			log.warnf("%s.getTableNames: Datasource %s not found",TAG,source);
		}
		return columns;
	}
	/**
	 * Execute a SQL prepared against the named datasource.
	 * The SQL allows for a single argument.
	 *
	 * @param SQL command to execute
	 * @param arg the single argument
	 * @param source a named datasource
	 * @param suppliedConnection a database connection. If null the method will manage.
	 */
	public void runPreparedStatement(String sql,String arg,String source,Connection suppliedConnection) {
		Connection cxn = suppliedConnection;
		if( cxn==null ) cxn = getConnection(source);
		if( cxn!=null ) {
			try {
				PreparedStatement stmt = cxn.prepareStatement(sql);
				if( arg!=null ) stmt.setString(1,arg);
				stmt.execute();
				stmt.close();
			}
			catch(SQLException sqle) {
				log.warnf("%s.runPreparedStatement: Exception executing %s (%s)",TAG,sql,sqle.getMessage());
			}
			finally {
				if(suppliedConnection==null ) closeConnection(cxn);
			}
		}
		else {
			log.warnf("%s.runPreparedStatement: Datasource %s not found",TAG,source);
		}
	}
	/**
	 * Execute a SQL query against the named datasource.
	 * The query is expected to return exactly one value.
	 *
	 * @param sql command to execute
	 * @param source a named datasource
	 * @param suppliedConnection a database connection. If null the method will manage.
	 */
	public String runScalarQuery(String sql,String source,Connection suppliedConnection) {
		Connection cxn = suppliedConnection;
		if( cxn==null ) {
			cxn = getConnection(source);
		}
		String result = "";
		if( cxn!=null ) {
			try {
				Statement stmt = cxn.createStatement();
				ResultSet rs = stmt.executeQuery(sql);
				while( rs.next() ) {
					if( rs.getMetaData().getColumnCount()>0 ) {
						result = rs.getString(1);
						break;
					}
				}
				rs.close();
			}
			catch(SQLException sqle) {
				log.warnf("%s.runScalarQuery: Exception executing %s (%s)",TAG,sql,sqle.getMessage());
			}
			finally {
				if(suppliedConnection==null ) {
					closeConnection(cxn);
				}
			}
		}
		else {
			log.warnf("%s.runScalarQuery: Datasource %s not found",TAG,source);
		}
		return result;
	}
	private String scrubSQL(String sql,DBMS dbms) {
		// Scrub comments
		if( sql.startsWith("\n")) sql = sql.substring(1);
		if(dbms.equals(DBMS.MYSQL))  sql = sql.replaceAll("#.*$", "");   // Mysql comments
		if( sql.endsWith("\n")) sql = sql.substring(0, sql.length()-1);
		if( sql.endsWith("\r")) sql = sql.substring(0, sql.length()-1);
		sql.trim();
		return sql;
	}
}
