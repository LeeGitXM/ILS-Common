/**
 *   (c) 2020 ILS Automation. All rights reserved.
 *  
 *   Based on sample code in the IA-scripting-module
 *   by Travis Cox.
 */
package com.ils.log.common;

/**
 *  Define the methods supported by both client and gateway sides of this feature.
 *  These are all properties configured in the gateway module properties page.
 */
public interface SystemPropertiesInterface   { 
	/**
	 * @return the datasource string
	 */
	public String getLoggingDatasource();
	/**
	 * @return the desired buffer size for the crash appender
	 */
	public int getCrashBufferSize();
	/**
	 */
	public String getLibDir();
	/**
	 */
	public String getLogsDir();  
	/**
	 */
	public String getUserLibDir(); 
}
