/**
 *   (c) 2021  ILS Automation. All rights reserved. 
 */
package com.ils.common.persistence;


import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;

import simpleorm.dataset.SFieldFlags;

/**
 * Save and access toolkit-wide properties in the HSQL persistent database.
 * For documentation relating to the SimpleORM data model:
 * @See: http://simpleorm.org/sorm/whitepaper.html
 */
public class ToolkitProjectRecord extends PersistentRecord {
	private static final long serialVersionUID = 8243351320983681280L;

	public static final String TABLE_NAME = "ILS_Toolkit_Project_Properties";
	
	/*
	public static final RecordMeta<ToolkitProjectRecord> META = new RecordMeta<>(ToolkitProjectRecord.class, TABLE_NAME);
	static SFieldFlags[] primary = {SFieldFlags.SPRIMARY_KEY,SFieldFlags.SMANDATORY};
	static SFieldFlags[] secondary = {SFieldFlags.SMANDATORY};
	public static final StringField Project = new StringField(META, "Project",primary );
	public static final StringField Name = new StringField(META, "Name",primary );
	public static final StringField Value = new StringField(META, "Value",secondary).setDefault("");
	*/
	
	public static final RecordMeta<ToolkitProjectRecord> META = new RecordMeta<>(ToolkitProjectRecord.class, TABLE_NAME);
	
	public static final StringField Project = new StringField(META, "Project", SFieldFlags.SPRIMARY_KEY );
	public static final StringField Name = new StringField(META, "Name", SFieldFlags.SPRIMARY_KEY );
	public static final StringField Value = new StringField(META, "Value", SFieldFlags.SMANDATORY).setDefault("");
	
	public RecordMeta<?> getMeta() {return META; }
	
	public String getProject() { return getString(Project); }
	public String getName() { return getString(Name); }
	public String getValue() { return getString(Value); }
	public void setProject(String str) { setString(Project,str); }
	public void setName(String str) { setString(Name,str); }
	public void setValue(String str) { setString(Value,str); }
}
