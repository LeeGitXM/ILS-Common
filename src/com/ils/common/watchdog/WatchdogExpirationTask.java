/**
 *   (c) 2013  ILS Automation. All rights reserved. 
 */
package com.ils.common.watchdog;


/**
 * Call the expiration method on the dog's observer. We do this in a separate thread.
 */
public class WatchdogExpirationTask implements Runnable{
	private final Watchdog dog;
	/**
	 * Constructor.
	 * 
	 * @param dog the watchdog to trigger
	 */
	public WatchdogExpirationTask(Watchdog dog)  {	
		this.dog = dog;
	}
	
	public void run()   { 
		dog.expire();
	}
}
