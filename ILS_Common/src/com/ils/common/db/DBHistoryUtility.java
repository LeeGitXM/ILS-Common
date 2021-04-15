package com.ils.common.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
import com.inductiveautomation.ignition.common.model.values.QualifiedValue;
import com.inductiveautomation.ignition.common.sqltags.history.cache.TagHistoryCache;
import com.inductiveautomation.ignition.common.sqltags.model.TagPath;
import com.inductiveautomation.ignition.gateway.datasource.Datasource;
import com.inductiveautomation.ignition.gateway.datasource.SRConnection;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;



/**
 * This class is a collection of utilities for handling the Ignition history
 * tables directly.
 * 
 * For better performance, the execute and query functions require that the
 * calling method can supply a connection. It is up to the caller to
 * close the connection when it is no longer needed. Failure to do so will
 * quickly lead a resource exhaustion.
 */
public class DBHistoryUtility {
	private final static String TAG = "DBHistoryUtility";
	private final ILSLogger log;
	private final GatewayContext context;
	private final SimpleDateFormat formatter = new SimpleDateFormat("YYYY/mm/dd HH:mm:ss");
	
	public DBHistoryUtility(GatewayContext ctx) {
		this.context = ctx;
		this.log = LogMaker.getLogger(getClass().getPackage().getName());
	}
	
	/**
	 * Access the history data tables directly and delete any history that exists
	 * for the tags. Initialize these tags in the appropriate tables. We are 
	 * guaranteed that the history tables are not partitioned.
	 */
	public void clearHistory(String providerName,List<TagPath> tagPaths) {

		SRConnection cxn = null;
		String SQL = "";
		try {
			cxn = context.getDatasourceManager().getConnection(providerName);
			// Make sure that there is a _exempt_ scan class. Use the latest.
			SQL = "select max(id) from sqlth_scinfo where scname='_exempt_';";
			log.debugf("%s.clearHistory: Find the exempt scan class (%s)", TAG, SQL);
			Object scid = cxn.runScalarQuery(SQL);
			if( scid==null ) {
				String INSERT = "insert into sqlth_scinfo(scname,drvid) values('_exempt_',1);";
				log.debugf("%s.clearHistory: Create an exempt scan class (%s)", TAG, INSERT);
				cxn.runUpdateQuery(INSERT);
				scid = cxn.runScalarQuery(SQL);
			}

			SQL = "select pname from sqlth_partitions;";
			log.debugf("%s.clearHistory: Get the name of the partition (%s)", TAG, SQL); 
			String table = (String)cxn.runScalarQuery(SQL);
			if( table!=null ) {
				log.debug(String.format("%s:clearHistory partition table is %s",TAG,table));
				// Make this the partition for all time (0->MAX_LONG)
				SQL = "update sqlth_partitions set start_time=0, end_time=9223372036854775807 where pname='"+table+"';";
				log.debug(String.format("%s:clearHistory updating master (%s)",TAG,SQL));
				cxn.runUpdateQuery(SQL);


				for(TagPath path:tagPaths ) {
					// Now clear the history for this tag and any others with the same name
					String tag = path.toStringPartial().toLowerCase();
					SQL = "delete from "+table+" where tagid in (select id from sqlth_te where tagpath = '"+tag+"');";
					log.debug(String.format("%s:clearHistory clear history entries (%s)",TAG,SQL));
					cxn.runUpdateQuery(SQL );

					// Find the tag if it exists
					SQL = "select id from sqlth_te where tagpath = '"+tag+"';";
					Object tagid = cxn.runScalarQuery(SQL);
					if( tagid!=null ) {
						SQL = String.format("update sqlth_te set datatype=1,scid=%s, created=0, retired=NULL where id=%s",scid.toString(),tagid.toString());
						log.debug(String.format("%s:clearHistory: Setting attributes for %s (%s)", TAG,tag, SQL));
						cxn.runUpdateQuery(SQL);
					}
					else {
						log.error(String.format("%s: Tag %s does not exist in history table. It must be \"tickled\" with some data values"));
					}   
				}
				// Clear the cache
				TagHistoryCache.reset();
			}
			else {
				log.error(String.format("%s:clearHistory partition table not found for database %s",TAG,providerName));
			}
		}
		catch(SQLException sqle) {
			log.error(String.format("%s:clearHistory SQL error on %s (%s)",TAG,providerName,sqle.getLocalizedMessage()));
		}
		catch(Exception ex) {
			log.error(String.format("%s:clearHistory Exception (%s)",TAG,ex.getMessage()));
		}

		log.infof("%s.clearHistory: clearing %s",TAG,providerName);

	}
	
	/** 
	 * Anytime a connection is "gotten", it should be closed.
	 * @param cxn
	 */
	public void closeConnection(Connection cxn) {
		try {
			cxn.close();
		}
		catch(SQLException sqle) {
			log.warnf("%s.closeConnection: Exception closing connection (%s)",TAG,sqle.getMessage());
		}

	}
	
	public Connection getConnection(String name) {
		Connection cxn = null;
		Datasource ds = context.getDatasourceManager().getDatasource(name);
		if( ds!=null ) {
			try {
				log.tracef("%s.getConnection: Status is %s (%d of %d busy)",TAG,ds.getStatus().name(),
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
	 * Process a list of tags and values.
	 */
	public void setHistoryValue(SRConnection cxn,String historyTable,List<TagPath> tagPaths,List<QualifiedValue> data) throws SQLException{
		int index=0;
		String SQL = "";
		for(TagPath path:tagPaths) {
			QualifiedValue qv = data.get(index);
			// Historical tag variant. Interval is seconds, date.getTime() is millsecs.
			log.tracef("%setHistoryValue: %s=%s (%s)",TAG,path.toStringFull(),qv.getValue().toString(),formatter.format(qv.getTimestamp()));
			SQL = String.format("insert into %s (tagid,floatValue,dataintegrity,t_stamp) select id,%s,192,%d from sqlth_te where tagpath='%s';",
					historyTable,qv.toString(),qv.getTimestamp().getTime(),path.toStringPartial().toLowerCase());
			log.trace(String.format("%s:process: updating history (%s)",TAG,SQL));
			cxn.runUpdateQuery(SQL); 

			index++;
		}
	}
}
