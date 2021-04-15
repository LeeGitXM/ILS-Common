/**
 *   (c) 2014-2017  ILS Automation. All rights reserved.
 *  
 */
package com.ils.common.persistence;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
import com.inductiveautomation.ignition.gateway.model.GatewayContext;

/**
 *  This handler provides is a common interface for handling requests to save/restore 
 *  properties in the SQLite persistence engine.
 *  
 */
public class ToolkitRecordHandler  {
	private final static String TAG = "ToolkitRecordHandler";
	private final ILSLogger log;
	private GatewayContext context = null;
    
	/**
	 * Initialize with instances of the classes to be controlled.
	 */
	public ToolkitRecordHandler(GatewayContext ctx) {
		log = LogMaker.getLogger(getClass().getPackage().getName());
		this.context = ctx;
	}
	

	
	/**
	 * On a failure to find the property, an empty string is returned.
	 */
	public String getToolkitProperty(String propertyName) {
		String value = "";
		try {
			ToolkitRecord record = context.getPersistenceInterface().find(ToolkitRecord.META, propertyName);
			if( record!=null) value =  record.getValue();
		}
		catch(Exception ex) {
			log.warnf("%s.getToolkitProperty: Exception retrieving %s (%s),",TAG,propertyName,ex.getMessage());
		}
		return value;
	}
	
	/**
	 * Set a name/value persistent property. Any update listeners will be notified.
	 */
	public void setToolkitProperty(String propertyName, String value) {
		try {
			ToolkitRecord record = context.getPersistenceInterface().find(ToolkitRecord.META, propertyName);
			if( record==null) record = context.getPersistenceInterface().createNew(ToolkitRecord.META);
			if( record!=null) {
				record.setName(propertyName);
				record.setValue(value);
				context.getPersistenceInterface().save(record);
				context.getPersistenceInterface().notifyRecordUpdated(record);
			}
			else {
				log.warnf("%s.setToolkitProperty: %s=%s - failed to create persistence record (%s)",TAG,propertyName,value,ToolkitRecord.META.quoteName);
			} 
		}
		catch(Exception ex) {
			log.warnf("%s.setToolkitProperty: Exception setting %s=%s (%s),",TAG,propertyName,value,ex.getMessage());
		}
	}

}

