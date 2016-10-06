package com.ils.common.tag;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import com.ils.common.db.DBUtility;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.common.sqltags.model.types.DataType;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

/**
 * An adjunct to a tag provider that updates a database table on a tag write. 
 * The database and table names must be specified when the provider is created
 */
public class DatabaseStoringProviderDelegate  {
	private final static String TAG = "DatabaseStoringProviderDelegate";
	private final LoggerEx log;
	private final DBUtility dbUtil;
	private Connection cxn = null;
	private final String name;
	private final String database;  // Connection name
	private final String tableName;
	private PreparedStatement doubleStatement  = null;
	private PreparedStatement integerStatement = null;
	private PreparedStatement stringStatement  = null;
	// Keep private lookup tables instead of querying OutputTagDefinition with each write
	private final Map<String,String> datatypeMap;
	private final Map<String,Integer> tagIdMap;

	public DatabaseStoringProviderDelegate(GatewayContext context,String providerName,String db,String table) {
		this.database = db;
		this.name = providerName;
		this.tableName = table;
		this.datatypeMap = new HashMap<>();
		this.tagIdMap    = new HashMap<>();
		this.dbUtil = new DBUtility(context);
		this.log = LogUtil.getLogger(getClass().getPackage().getName());
		this.initializeTables(false);
	}

	/**
	 * Make sure that the tables required by this provider exist.
	 * @param clear if true, delete the contents of any previously-
	 *              existing tables.
	 */
	public void initializeTables(boolean clear) {
		String SQL = "CREATE TABLE OutputTagDefinition (" +
				     " tagId INTEGER PRIMARY KEY AUTO_INCREMENT,"          +
				     " tableName TEXT not null,"          +
				     " dataType  TEXT not null,"  +
				     " tagPath   TEXT not null)";
		dbUtil.executeSQL(SQL, database, cxn);
		SQL =    "CREATE TABLE "+tableName + "("  +
			     " tstamp DATETIME  not null,"    +
			     " tagId INTEGER not null,"       +
			     " stringValue  TEXT null,"       +
			     " intValue  INTEGER null,"       +
		         " doubleValue DOUBLE null)";
		 dbUtil.executeSQL(SQL, database, cxn);
		SQL = "CREATE INDEX "+tableName+"_INDEX" +
		      " ON "+tableName+"(tstamp,tagId)";
		 dbUtil.executeSQL(SQL, database, cxn);
		 if( clear ) {
			 SQL = "DELETE FROM OutputTagDefinition " +
		           " WHERE tableName = '"+tableName+"'";
			 dbUtil.executeSQL(SQL, database, cxn);
			 SQL = "DELETE FROM " + tableName;
			 dbUtil.executeSQL(SQL, database, cxn);
		 }
	}
	/**
	 * On startup, open a database connection.
	 */
	public void startup() {
		cxn = dbUtil.getConnection(database);
		try {
			doubleStatement = cxn.prepareStatement("INSERT INTO "+tableName+"(tstamp,tagId,doubleValue) VALUES(?,?,?)");
			integerStatement = cxn.prepareStatement("INSERT INTO "+tableName+"(tstamp,tagId,intValue)   VALUES(?,?,?)");
			stringStatement = cxn.prepareStatement("INSERT INTO "+tableName+"(tstamp,tagId,stringValue) VALUES(?,?,?)");
		}
		catch(SQLException sqle) {
			log.warnf("%s.startup: Exception creating prepared statements %s (%s)",TAG,sqle.getMessage());
		}
	}

	public void shutdown() {
		if( cxn!=null ) {
			try {
				doubleStatement.close();
				integerStatement.close();
				stringStatement.close();
				cxn.close();
			}
			catch( SQLException ignore ) {}
			cxn = null;
 		}
	}

	public String getDatabase() { return this.database; }
	public String getName() { return this.name; }
	public String getTable() { return this.tableName; }

	public void configureTag(TagPath path, DataType dType) {
		String tp = path.toStringPartial();   // Disregard the provider
		String type = convertDataType(dType);
		String SQL = "INSERT INTO OutputTagDefinition(tableName,tagPath,dataType) " +
	                 " VALUES('"+tableName+"','" +tp+"','"+type+"')";
		dbUtil.executeSQL(SQL, database, cxn);
		
		SQL = "SELECT tagId FROM OutputTagDefinition " +
			  " WHERE tableName = '"+tableName+"'"     +
			  "   AND tagPath = '"+tp+"'";
		int id = 0;
		try {
			Statement stmt = cxn.createStatement();
			ResultSet rs = stmt.executeQuery(SQL);
			while( rs.next() ) {
				if( rs.getMetaData().getColumnCount()>0 ) {
					id = rs.getInt(1);
					break;
				}
			}
			rs.close();
			log.tracef("%s.configureTag: %s (%s) = %d",TAG,tp,type,id);
			datatypeMap.put(tp,type);
			tagIdMap.put(tp, new Integer(id));
		}
		catch(SQLException sqle) {
			log.warnf("%s.runScalarQuery: Exception executing %s (%s)",TAG,SQL,sqle.getMessage());
		}
	}
	private String convertDataType(DataType dType) {
		String type = "STRING";
		if(dType.isFloatingPoint()) type = "DOUBLE";
		else if(dType.isNumeric()) type  = "INTEGER";
		return type;
	}
	public void removeTag(TagPath path)  {
		datatypeMap.remove(path.toStringFull());
		tagIdMap.remove(path.toStringFull());
		String SQL = "DELETE FROM OutputTagDefinition " +
		             " WHERE tableName = '"+tableName+"'" +
				     "   AND tagPath= '"+path.toStringFull()+"'";
		dbUtil.executeSQL(SQL, database, cxn);
	}


	/**
	 * We need this version of the update method.
	 */
	public void updateValue(TagPath path, QualifiedValue value) {
		writeIntoHistory(path.toStringFull(),value);
	}

	private void writeIntoHistory(String path,QualifiedValue qv) {
		String type = datatypeMap.get(path);
		Integer id  = tagIdMap.get(path);
		if( type!=null && id!=null ) {
			PreparedStatement ps = null;
			try {
				if( type.equalsIgnoreCase("DOUBLE")) {
					ps = doubleStatement;
					double val = Double.parseDouble(qv.getValue().toString());
					ps.setDouble(3, val);
				}
				else if( type.equalsIgnoreCase("INTEGER")) {
					ps = integerStatement;
					double val = Double.parseDouble(qv.getValue().toString());
					ps.setInt(3, (int)val);
				}
				else {
					ps = stringStatement;
					ps.setString(3, qv.getValue().toString());
				}
				ps.setTimestamp(1,new Timestamp(qv.getTimestamp().getTime()));
				ps.setInt(2,id.intValue());
				ps.executeUpdate();
			}
			catch(SQLException sqle) {
				log.warnf("%s.writeIntoHistory: Exception writing tag %s (%s)",TAG,path,sqle.getMessage());
			}	
		}
		else {
			log.warnf("%s.writeIntoHistory: Map missing for tag %s",TAG,path);
		}
	}
}