/**
 *   (c) 2020 ILS Automation. All rights reserved.
 */
package com.ils.logging.common;

import com.ils.common.log.filter.PatternFilter;
import com.ils.logging.common.filter.CrashFilter;

/**
 *  Define the methods shared by the client and designer hook classes and used by the
 *  scripting interface.
 */
public interface LoggingHookInterface   { 
	public CrashFilter getCrashFilter() ;
	public PatternFilter getPatternFilter();
	public void setCrashBufferSize(int size);
}
