/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common.collector;


/**
 *  This is the listener interface that allows a DataCollector to
 *  report to its model that all necessary information is in place
 *  to start a calculation.
 */
public interface DataCompleteListener   {
	public void dataCollected(DataCompleteEvent evnt);
}
