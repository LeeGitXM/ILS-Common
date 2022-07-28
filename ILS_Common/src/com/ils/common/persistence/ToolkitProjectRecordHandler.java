/**
 *   (c) 2021  ILS Automation. All rights reserved.
 *  
 */
package com.ils.common.persistence;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

/**
 *  This handler provides is a common interface for handling requests to save/restore 
 *  properties in the SQLite persistence engine.
 *  
 */
public class ToolkitProjectRecordHandler  {
	private final static String CLSS = "ToolkitProjectRecordHandler";
	private final LoggerEx log;
	private GatewayContext context = null;
    
	/**
	 * Initialize with instances of the classes to be controlled.
	 */
	public ToolkitProjectRecordHandler(GatewayContext ctx) {
		log = LogUtil.getLogger(getClass().getPackage().getName());
		this.context = ctx;
	}
	

	
	/**
	 * On a failure to find the property, an empty string is returned.
	 */
	public String getToolkitProjectProperty(String projectName,String propertyName) {
		String value = "";
		try {
			log.tracef("%s.getToolkitProperty: getting %s - %s", CLSS, projectName,propertyName);
			ToolkitProjectRecord record = context.getPersistenceInterface().find(ToolkitProjectRecord.META, projectName,propertyName);
			if( record!=null) value =  record.getValue();
		}
		catch(Exception ex) {
			log.warnf("%s.getToolkitProperty: Exception retrieving %s:%s (%s),",CLSS,projectName,propertyName,ex.getMessage());
		}
		return value;
	}
	
	/**
	 * Set a name/value persistent property. Any update listeners will be notified.
	 */
	public void setToolkitProjectProperty(String projectName,String propertyName, String value) {
		try {
			ToolkitProjectRecord record = context.getPersistenceInterface().find(ToolkitProjectRecord.META, projectName,propertyName);
			if( record==null) record = context.getPersistenceInterface().createNew(ToolkitProjectRecord.META);
			if( record!=null) {
				record.setProject(projectName);
				record.setName(propertyName);
				record.setValue(value);
				context.getPersistenceInterface().save(record);
				context.getPersistenceInterface().notifyRecordUpdated(record);
			}
			else {
				log.warnf("%s.setToolkitProjectProperty: %s:%s=%s - failed to create persistence record (%s)",CLSS,projectName,propertyName,value,ToolkitRecord.META.quoteName);
			} 
		}
		catch(Exception ex) {
			log.warnf("%s.setToolkitProjectProperty: Exception setting %s:%s=%s (%s),",CLSS,projectName,propertyName,value,ex.getMessage());
		}
	}

}

