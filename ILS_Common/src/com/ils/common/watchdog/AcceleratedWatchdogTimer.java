/**
 *   (c) 2013-2025  ILS Automation. All rights reserved.
 */
package com.ils.common.watchdog;

import java.text.SimpleDateFormat;
import java.util.Date;


/**
 *  The accelerated watchdog timer is designed for use during testing where
 *  the time-scale is shortened from real-time. This is implemented as an
 *  extension to more easily ensure that the production timer does not
 *  tinker with the time scale.
 *  
 *  Interested entities register as TimeoutObservers. 
 */
public class AcceleratedWatchdogTimer extends WatchdogTimer implements Runnable   {
	protected final static String DATETIME_FORMAT = "yyyy/MM/dd hh:mm:ss";
	private final SimpleDateFormat formatter = new SimpleDateFormat(DATETIME_FORMAT);
	private double factor = 1.0;    // Clock speedup factor
	private long testTimeOffset = 0;

	/**
	 * Constructor: This version of the constructor supplies a name.
	 * @param tname timer name
	 */
	public AcceleratedWatchdogTimer(String tname)  {
		super(tname);
	}
	/**
	 * @return the reciprocal of the time factor. It's the speedup factor.
	 */
	public double getFactor() { return 1.0/factor; }
	/**
	 * @return the current test time. The time is updated
	 *         each time a timer expires and offset by the
	 *         specified offset.
	 */
	public long getTestTime() {
		long now = System.nanoTime()/1000000;   // Work in milliseconds
		return (long)((now-testTimeOffset)); 
	}
	/**
	 * Specify the current time as known to a test as an offset from now
	 * expressed in secs since the start of the epoch. Then convert to system time.
	 * Convert this into a millisec offset used to compute 
	 * the test time equivalent of a .
	 * @param testTime in msecs from the start of the unix epoch
	 */
	public void setTestTimeOffset(long offset) { 
		long testTime = System.currentTimeMillis() - offset;
		long now = System.nanoTime()/1000000;   // Work in milliseconds
		this.testTimeOffset = now - testTime; 
		log.infof("%s.setTestTimeOffset: current time is %s",TAG,formatter.format(new Date(testTime)));
	}
	/**
	 * Set the clock speed execution factor. For production
	 * the value should ALWAYS be 1.0. This feature is a 
	 * test speedup capability. NOTE: the time-increment 
	 * factor actually used by this function is the reciprocal
	 * of the value given here.
	 * 
	 * Note that SLOWING execution is a case not yet tested.
	 * Use at your own risk.
	 * 
	 * @param fact
	 */
	public void setFactor(double fact) {
		if( fact>0.0 ) factor = 1.0/fact;
	}

	/**
	 * Insert a new dog into the list in order. Compare the expiration with 
	 * the current time. Scale the difference by the time factor. We are
	 * assuming that the expiration was created by differencing with the
	 * current clock time.
	 * 
	 * This list is assumed never to be empty
	 */
	protected void insert(Watchdog dog) {
		int index=0;
		dog.setActive(true);
		dog.scaleExpiration(factor);
		for(Watchdog wd:dogs ) {
			if(dog.getExpiration()<wd.getExpiration()) {
				dogs.add(index, dog);
				if( index==0) watchdogThread.interrupt();   // We've replaced the head
				return;
			}
			index++;
		}
		dogs.addLast(dog);
	}
}
