/**
 *   (c) 2012-2015  ILS Automation. All rights reserved.
 *  
 *   Class contains static constants that have meaning to the ILS-Common library.
 */
package com.ils.common;


/**
 *  Global properties for the ILS-Common library.
 */
public interface ILSProperties   {
	// These are the standard node names for the Gateway module configuration menus
	public static String ROOT                     = "ILS";                      // Root of Gateway menu tree
	public final static String LOOP_PREVENTION_MARKER_NAME  = "no-loop";        // Used to prevent circular processing during logging
	public final static String DEFAULT_WINDOWS_BROWSER_PATH  = "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe";       // Common location for Chrome browser on a Windows system
	public static String HELP_CONFIGURATION = "HELP CONFIGURATION";   // 
	public static String HELP_CONFIGURATION_RECORD_CLASS = "ILS_HELP_PROPERTIES";   // Table name for context sensitive help meta
	public final static String TIMESTAMP_FORMAT   = "yyyy.MM.dd HH:mm:ss.SSS";     // Format for writing timestamp
}
