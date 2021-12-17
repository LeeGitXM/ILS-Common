/**
 *   (c) 2015-2021  ILS Automation. All rights reserved. 
 */
package com.ils.common.persistence;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IRecordListener;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;
import com.inductiveautomation.ignition.gateway.web.models.KeyValue;

/**
 * Respond to changes in our toolkit properties in the HSQL persistent database.
 * For documentation relating to the SimpleORM data model:
 */
public class BasicToolkitProjectRecordListener implements IRecordListener<ToolkitProjectRecord> {
	private final static String CLSS = "BasicToolkitProjectRecordListener";
	protected final GatewayContext context;
	protected final LoggerEx log;
	
	public BasicToolkitProjectRecordListener(GatewayContext ctx) {
		this.context = ctx;
		this.log = LogUtil.getLogger(getClass().getPackage().getName());
	}

	@Override
	public void recordAdded(ToolkitProjectRecord rec) {
		log.debugf("%s.recordAdded:",CLSS);
	}

	@Override
	public void recordDeleted(KeyValue rec) {
		log.debugf("%s.recordDeleted:",CLSS);
	}

	@Override
	public void recordUpdated(ToolkitProjectRecord rec) {
		log.infof("%s.recordUpdated: %s = %s",CLSS,rec.getName(),rec.getValue());	
	}
	
}
