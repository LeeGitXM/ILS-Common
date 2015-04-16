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
	 * Scale the expiration by a supplied time factor. It is imperative
	 * that this be called exactly once per execution immediately
	 * on being added to the execution queue. Note that this is
	 * NOT invoked by the production watchdog timer.
	 * 
	 * @param time, e.g. now is System.nanoTime()/1000000
	 */
	public void scaleExpiration(double fact) { 
		long now = System.nanoTime()/1000000;
		double deltatime =  expiration - now;
		expiration = now + (long)(deltatime*fact);
	}
	/**
	 * Set the number of millisecs into the future for this dog to expire.
	 * If we've already "executed in the future" due to speedup, then 
	 * use that time as the threshold.
	 * @param delay ~ msecs
	 */
	public void setDelay(long delay) {
		long now = System.nanoTime()/1000000;
		this.expiration = delay+ now; 
	}
	/**
	 * Set the number of secs into the future for this dog to expire.
	 * This is a convenience method to avoid the constant time 
	 * conversions when dealing with seconds.
	 * @param delay ~ secs
	 */
	public void setSecondsDelay(double delay) {
		setDelay((long)(delay*1000)); 
 
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
