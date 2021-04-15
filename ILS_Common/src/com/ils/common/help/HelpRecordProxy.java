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
 * A PersistentRecord class defines a record for persistent storage. This class is a
 * duplicate of the HelpRecord contained in the common module. The proxy allows installations
 * that are not using the module to still access the help record.
 */
@SuppressWarnings("serial")
public class HelpRecordProxy extends PersistentRecord  {
	public final static RecordMeta<HelpRecordProxy> META = new RecordMeta<HelpRecordProxy>(
			HelpRecordProxy.class, ILSProperties.HELP_CONFIGURATION_RECORD_CLASS);
	public final static IdentityField Id = new IdentityField(META);
	
	// On the configuration page, we configure:
	// - path to the windows browser
	
	public final static StringField windowsBrowserPath = new StringField(META,
			"WindowsBrowserPath", SFieldFlags.SMANDATORY).setDefault(ILSProperties.DEFAULT_WINDOWS_BROWSER_PATH);

	public final static Category browserCategory = new Category(
			ILSProperties.COMMON_BUNDLE_ROOT+".Categories.Browser_Path", 0).include(windowsBrowserPath);
	

	
	@Override
	public RecordMeta<?> getMeta() {
		return META;
	}

	/** @return the path to the browser used for context-sensitive help. */
	public String getWindowsBrowserPath() {
		return getString(windowsBrowserPath);
	}
}
