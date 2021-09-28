/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common.collector;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 *  This is a generic time-out timer for use with a classes that need to wait
 *  until an interval expires. On expiration the timer can be is automatically
 *  reset to count-down again for the next cycle. The call-back takes place in
 *  the same thread. The interval calculation is not altered by the computation
 *  time used within the call-back (unless it overwrites the trigger interval).
 *  
 *  Interested entities register as TimeoutObservers. 
 */
public class TimeoutTimer implements Runnable   {
	private final static String TAG = "TimeoutTimer: ";
	private final LoggerEx log;
	private final int timeout;    // ~ msecs
	private final List<TimeoutObserver> observers;
	private boolean stopped = true;
	private Thread thread = null; 

	/**
	 * Constructor: Creates a timeout timer.
	 * @param timeout
	 */
	public TimeoutTimer(int timeout)  {
		log = LogUtil.getLogger(getClass().getPackage().getName());
		log.debug(TAG+"New timer with timout = "+timeout);
		if (timeout < 1) {
			throw new IllegalArgumentException("timeout must be greater than zero.");
		}
		this.timeout = timeout;
		this.observers = new CopyOnWriteArrayList<TimeoutObserver>();
	}

	public void addTimeoutObserver(final TimeoutObserver to) {
		if( to!=null) observers.add(to);
	}

	public void removeTimeoutObserver(final TimeoutObserver to) {
		if( to!=null) observers.remove(to);
	}

	/**
	 * Inform the observers of the timeout. This involves 
	 * complete processing of all the models. Run this in
	 * the same thread.
	 */
	protected final void fireTimeoutOccured() {
		log.debug(TAG+"TIMEOUT ");
		try {
			for(TimeoutObserver to:observers) {
				if( to!=null) to.timeout();
			}
		}
		catch(RuntimeException ex) {
			log.error(TAG+"Exception in worker thread ("+ex.getLocalizedMessage()+")",ex);
		}
	}

	public synchronized void start() {
		stopped = false;
		thread = new Thread(this, "TimeoutTimer");
		log.debug(String.format("%s START %d",TAG,thread.hashCode()));
		thread.setDaemon(true);
		thread.start();
	}

	public synchronized void stop() {
		log.debug(TAG+"STOPPED");
		stopped = true;
		if(thread!=null) {
			thread.interrupt();
			notifyAll();
		}
	}
	
	public synchronized void reset() {
		if(thread!=null) {
			log.debug(TAG+"RESET");
			thread.interrupt();
		}
	}

	public synchronized void run() {
		final long interval = timeout;
		long now = System.currentTimeMillis();
		long triggerTime = now + interval;
		long waitTime = 0;

		while( !stopped  ) {
			try {
				waitTime = triggerTime - now;
				triggerTime += interval;
				if( waitTime>0 ) {
					log.debug(TAG+"WAIT for "+waitTime);
					wait(waitTime);
				}
				if (!stopped) fireTimeoutOccured();
			} 
			// On an interrupt, simply reset the target time and don't fire
			// This is the case where the count down did not complete.
			catch (InterruptedException e) {}
			catch( Exception ex ) {
				log.error(TAG+" Exception during timeout processing ("+ex.getLocalizedMessage()+")",ex);  // Prints stack trace
			}
			now = System.currentTimeMillis();
		}
		log.debug(String.format("%s TERMINATING %d",TAG,Thread.currentThread().hashCode()));
	}
}
