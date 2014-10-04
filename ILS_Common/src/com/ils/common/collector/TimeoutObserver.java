/**
 *   (c) 2013  ILS Automation. All rights reserved.
 *  
 */
package com.ils.common.collector;


/**
 *  This is the interface by which the count down timer
 *  informs a data collector that a timeout has occurred.
 */
public interface TimeoutObserver   {
	public void timeout();
}
