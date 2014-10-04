/**
 *   (c) 2013  ILS Automation. All rights reserved.
 *  
 */
package com.ils.common.watchdog;

import java.util.UUID;


/**
 *  A watchdog is a task with a timeout. On expiration the observer
 *  is triggered. "petting" the dog delays timeout, perhaps indefinitely.
 */
public class Watchdog  {
	private final String name;
	private final UUID uuid;
	private final WatchdogObserver observer;
	private long expiration = 0;
	private boolean active = false;

	/**
	 * Create a watch dog task. 
	 * @param observer
	 */
	public Watchdog(String name,WatchdogObserver observer) {
		this.name = name;
		this.uuid = UUID.randomUUID();
		this.observer = observer;
		this.active = false;
	}
	
	/**
	 * The active flag is managed entirely by the WatchdogTimer
	 * @return true if the dog is in the timer's input queue.
	 */
	public boolean isActive() { return this.active; }
	/**
	 * @param yesNo the new dog active state
	 */
	public void setActive(boolean yesNo) { this.active = yesNo; }
	
	/**
	 * The expiration is the time that this watchdog will expire
	 * relative to the run-time of the virtual machine.
	 * @return watchdog expiration time ~ msecs.
	 */
	public long getExpiration() { return expiration;}
	/**
	 * Set the number of millisecs into the future for this dog to expire.
	 * @param delay ~ msecs
	 */
	public void setDelay(long delay) {
		this.expiration = delay+System.nanoTime()/1000000; 
	}
	/**
	 * Set the number of secs into the future for this dog to expire.
	 * This is a convenience method to avoid the constant time 
	 * conversions when dealing with seconds.
	 * @param delay ~ secs
	 */
	public void setSecondsDelay(double delay) {
		this.expiration = (long)(delay*1000)+System.nanoTime()/1000000; 
	}
	public void decrementExpiration(long delta) { this.expiration = expiration-delta; if( expiration<0) expiration=0;}
	public UUID getUUID() { return uuid; }
	
	/**
	 * Watchdog has expired, evaluate the observer. 
	 */
	public void expire() {
		if(observer!=null) observer.evaluate();
	}
	/**
	 * Two watchdogs are equal if their Ids are equal
	 */
	
	@Override
	public boolean equals(Object object){
		if(object instanceof Watchdog && ((Watchdog)object).getUUID() == this.uuid){
		    return true;
		} 
		else {
		    return false;
		}
	}
	// If we override equals, then we also need to override hashCode()
	@Override
	public int hashCode() {
		return getUUID().hashCode();
	}
	
	@Override
	public String toString() {
		return String.format("Watchdog: %s expires in %d ms",name,getExpiration()-System.nanoTime()/1000000);
	}
}
