/**
 *   (c) 2015 ILS Automation. All rights reserved.
 *  
 */
package com.ils.common.persistence;



/**
 *  Define an interface for accessing module properties .
 */
public interface ToolkitProperties   {  
	// These are the common names of toolkit properties that are to be stored in HSQLdb
	public static final String TOOLKIT_PROPERTY_DATABASE            = "Database";           // Production database
	public static final String TOOLKIT_PROPERTY_ISOLATION_DATABASE  = "SecondaryDatabase";  // Database when in isolation
	public static final String TOOLKIT_PROPERTY_PROVIDER            = "Provider";           // Production tag provider
	public static final String TOOLKIT_PROPERTY_ISOLATION_PROVIDER  = "SecondaryProvider";  // Tag provider when in isolation
	public static final String TOOLKIT_PROPERTY_ISOLATION_TIME      = "SecondaryTimeFactor";// Time speedup when in isolation
	
}
