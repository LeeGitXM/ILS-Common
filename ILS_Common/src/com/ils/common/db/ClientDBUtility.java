package com.ils.common.db;


import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
import com.inductiveautomation.ignition.client.model.AbstractClientContext;
import com.inductiveautomation.ignition.client.script.ClientDBUtilities;
import com.inductiveautomation.ignition.common.BasicDataset;
import com.inductiveautomation.ignition.common.Dataset;
import com.inductiveautomation.ignition.common.script.builtin.AbstractDBUtilities;

/**
 * A convenience class for executing queries from Client/Designer scope.
 */
public class ClientDBUtility extends ClientDBUtilities {
	private final static String clss = "ClientDBUtility";
	private final static long TXN_TIMEOUT = 60000;
	private final ILSLogger log;
	
	public ClientDBUtility(AbstractClientContext context)  {
		super(context);
		this.log = LogMaker.getLogger(getClass().getPackage().getName());
	}
	
	public Dataset runQuery(String sql,String datasource) {
		Dataset ds = new BasicDataset();
		try {
			String txn = _beginTransaction(datasource,AbstractDBUtilities.READ_UNCOMMITTED,TXN_TIMEOUT);
			ds = _runQuery(sql,datasource,txn);
			 _closeTransaction(txn);
		}
		catch(Exception ex) {
			log.warn("ClientDBUtility.runQuery: Exception running query ("+ex.getMessage()+")",ex);
		}
		return ds;
	}
	public Integer runUpdateQuery(String SQL,String db,String txid,boolean getIds,boolean skipAudit) {
		Integer rowsAffected = 0;
		try {
			rowsAffected = _runUpdateQuery(SQL,db,txid,getIds,skipAudit);
		}
		catch(Exception ex) {
			log.warn("ClientDBUtility.runUpdateQuery: Exception running update  ("+ex.getMessage()+")",ex);
		}
		return rowsAffected;
	}
	public Integer runPreparedStatement(String SQL,String db,String txid,boolean getIds,boolean skipAudit,Object[] args) {
		Integer rowsAffected = 0;
		try {
			rowsAffected = _runPrepStmt(SQL,db,txid,getIds,skipAudit,args);
		}
		catch(Exception ex) {
			log.warn("ClientDBUtility.runPreparedStatement: Exception running prepared statement ("+ex.getMessage()+")",ex);
		}
		return rowsAffected;
	}
}
