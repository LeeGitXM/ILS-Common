/**
 *   (c) 2013-2015  ILS Automation. All rights reserved.
 *  
 */
package com.ils.common.watchdog;

import java.util.Date;

import com.inductiveautomation.ignition.common.model.values.BasicQualifiedValue;
import com.inductiveautomation.ignition.common.model.values.Quality;


/**
 *  This version of a qualified value creator accepts a timer in the
 *  constructor as a way of getting test-time time stamp
 */
public class TestAwareQualifiedValue extends BasicQualifiedValue  {
	private static final long serialVersionUID = 5170215720921103760L;

	/**
	 * Create a qualified value from just a value. If in a testing 
	 * situation, set the time to the test time. 
	 * @param observer
	 */
	public TestAwareQualifiedValue(WatchdogTimer timer,Object value) {
		super(value);
		if( timer instanceof AcceleratedWatchdogTimer  ) {
			AcceleratedWatchdogTimer awt = (AcceleratedWatchdogTimer)timer;
			setTimestamp(new Date(awt.getTestTime()));
		}
	}
	/**
	 * Create a qualified value from just a value. If in a testing 
	 * situation, set the time to the test time. 
	 * @param observer
	 */
	public TestAwareQualifiedValue(WatchdogTimer timer,Object value,Quality q) {
		super(value,q);
		if( timer instanceof AcceleratedWatchdogTimer  ) {
			AcceleratedWatchdogTimer awt = (AcceleratedWatchdogTimer)timer;
			setTimestamp(new Date(awt.getTestTime()));
		}
	}
}
