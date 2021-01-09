/**
 *   (c) 2021  ILS Automation. All rights reserved. 
 */
package com.ils.common.help;

import com.ils.common.ILSProperties;
import com.inductiveautomation.ignition.gateway.localdb.persistence.Category;
import com.inductiveautomation.ignition.gateway.localdb.persistence.IdentityField;
import com.inductiveautomation.ignition.gateway.localdb.persistence.PersistentRecord;
import com.inductiveautomation.ignition.gateway.localdb.persistence.RecordMeta;
import com.inductiveautomation.ignition.gateway.localdb.persistence.StringField;

import simpleorm.dataset.SFieldFlags;

/**
 * A PersistentRecord class defines a record for persistent storage. Here we define
 * properties for the ILS Common module.
 */
@SuppressWarnings("serial")
public class HelpRecord extends PersistentRecord  {
	public final static RecordMeta<HelpRecord> META = new RecordMeta<HelpRecord>(
			HelpRecord.class, ILSProperties.HELP_CONFIGURATION_RECORD_CLASS);

	public final static IdentityField Id = new IdentityField(META);
	
	// On the configuration page, we configure:
	// - path to the windows browser
	
	public final static StringField windowsBrowserPath = new StringField(META,
			"WindowsBrowserPath", SFieldFlags.SMANDATORY).setDefault(ILSProperties.DEFAULT_WINDOWS_BROWSER_PATH);

	public final static Category browserCategory = new Category(
			"ils.Categories.Browser_Path", 0).include(windowsBrowserPath);
	
	
	@Override
	public RecordMeta<?> getMeta() {
		return META;
	}

	/** @return the hostname of the site that generates reports. */
	public String getWindowsBrowserPath() {
		return getString(windowsBrowserPath);
	}
}
