/**
 *   (c) 2015  ILS Automation. All rights reserved. 
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
public class BasicToolkitRecordListener implements IRecordListener<ToolkitRecord> {
	private final static String TAG = "ToolkitRecordListener";
	protected final GatewayContext context;
	protected final LoggerEx log;
	
	public BasicToolkitRecordListener(GatewayContext ctx) {
		this.context = ctx;
		this.log = LogUtil.getLogger(getClass().getPackage().getName());
	}

	@Override
	public void recordAdded(ToolkitRecord rec) {
		log.debugf("%s.recordAdded:",TAG);
	}

	@Override
	public void recordDeleted(KeyValue rec) {
		log.debugf("%s.recordDeleted:",TAG);
	}

	@Override
	public void recordUpdated(ToolkitRecord rec) {
		log.infof("%s.recordUpdated: %s = %s",TAG,rec.getName(),rec.getValue());	
	}
	
}
