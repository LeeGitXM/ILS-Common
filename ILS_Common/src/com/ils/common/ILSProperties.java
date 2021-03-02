/**
 *   (c) 2012-2021  ILS Automation. All rights reserved.
 *  
 *   Class contains static constants that have meaning to the ILS-Common library.
 *   NOTE: This class should be removed. ROOT is used only by ils-common and AED,
 *                                       TIMESTAMP_FORMAT is used only by SFC
 */
package com.ils.common;


/**
 *  Global properties for the ILS-Common library.
 */
public interface ILSProperties   {
	// These are the standard node names for the Gateway module configuration menus
	public static String ROOT                     = "ILS";                      // Root of Gateway menu tree
	public final static String TIMESTAMP_FORMAT   = "yyyy.MM.dd HH:mm:ss.SSS";     // Format for writing timestamp
	// For context-sensitive help
	public static String HELP_CONFIGURATION = "ILS Common Module";   // Name of menu node for help configuration. See ilsmodule.properties (?)
	public static String HELP_CONFIGURATION_RECORD_CLASS = "ILS_HELP_PROPERTIES";   // Table name for context sensitive help meta
	public final static String DEFAULT_WINDOWS_BROWSER_PATH  = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";       // Common location for Chrome browser on a Windows system
	
	// These are preferences keys for sharing between applications
	public final static String ILS_PREFERENCES_DOMAIN = "ILS_Preferences";
	public final static String PREFERENCES_PROJECT_EXIM_DIRECTORY = "ProjectExImDirectory"; 
	
	// These are resource bundle names for the help property screens
	public static final String COMMON_BUNDLE_NAME = "ilsmodule";       // Properties file is ilsmodule.properties
	public static final String COMMON_BUNDLE_ROOT = "ilscommon";       // Name of the bundle
}
