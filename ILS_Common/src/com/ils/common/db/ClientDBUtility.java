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
	private static long TXN_TIMEOUT = 60000;
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
	 
}
