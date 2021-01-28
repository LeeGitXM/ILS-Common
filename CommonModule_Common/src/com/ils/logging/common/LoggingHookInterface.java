/**
 *   (c) 2020 ILS Automation. All rights reserved.
 */
package com.ils.logging.common;

import com.ils.logging.common.filter.CrashFilter;
import com.ils.logging.common.filter.PatternFilter;

/**
 *  Define the methods shared by the client and designer hook classes and used by the
 *  scripting interface.
 */
public interface LoggingHookInterface   {
	public String getClientId();
	public CrashFilter getCrashFilter() ;
	public PatternFilter getPatternFilter();
	public void setCrashBufferSize(int size);
}
