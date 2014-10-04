/**
 *   (c) 2013  ILS Automation. All rights reserved.
 *  
 */
package com.ils.common.watchdog;


/**
 *  This is the interface by which the watchdog timer 
 *  reports a watchdog timeout.
 */
public interface WatchdogObserver   {
	public void evaluate();
}
