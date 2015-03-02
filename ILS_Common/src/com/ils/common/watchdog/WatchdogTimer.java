/**
 *   (c) 2013-2025  ILS Automation. All rights reserved.
 */
package com.ils.common.watchdog;

import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 *  The watchdog timer manages a collection of "watchdogs". The dogs
 *  are sorted by expiration time. "petting" the dogs resets the timeout
 *  perhaps indefinitely. Once the petting stops, the dog's "evaluate"
 *  method is invoked.
 *  
 *  Interested entities register as TimeoutObservers. 
 */
public class WatchdogTimer implements Runnable   {
	private final static String TAG = "WatchdogTimer";
	private final static int IDLE_DELAY = 60000;    // One minute
	private static int THREAD_POOL_SIZE = 20;       // Evaluate threads
	private final LoggerEx log;
	private final LinkedList<Watchdog> dogs;
	private boolean stopped = true;
	private final ExecutorService threadPool;
	private Thread watchdogThread = null;
	private final Watchdog idleDog;
	private double factor = 1.0;    // Clock speedup factor

	/**
	 * Constructor: Creates a timeout timer. The timer thread is started and
	 *              runs continuously until a stop is issued. If no other
	 *              watchdogs are active, the idle dog is used.
	 *              The dogs hold their absolute expiration times and are
	 *              sorted accordingly.
	 * @param timeout
	 */
	public WatchdogTimer()  {
		log = LogUtil.getLogger(getClass().getPackage().getName());
		this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		this.idleDog = new Watchdog("IDLE",null);
		idleDog.setDelay(IDLE_DELAY);
		this.dogs = new LinkedList<Watchdog>();
		dogs.push(idleDog);
		stopped = false;
		watchdogThread = new Thread(this, "WatchdogTimer");
		watchdogThread.setDaemon(true);
		watchdogThread.start();
		log.infof("%s.START watchdog thread %s (%d)",TAG,watchdogThread.getName(),watchdogThread.hashCode());

	}

	/**
	 * Add a new timer to the list. It holds an absolute expiration time. 
	 * The list is never empty, there is at least the IDLE dog.
	 * @param dog to be added
	 */
	public void addWatchdog(final Watchdog dog) {
		if(dog==null)  return;   // Ignore
		 insert(dog);
	}
	/**
	 * Set the clock speed execution factor. For production
	 * the value should ALWAYS be 1.0. This feature is a 
	 * test speedup capability. NOTE: the time-increment 
	 * fact actually used by this function is the reciprical
	 * of the value given here.
	 * @param fact
	 */
	public void setFactor(double fact) {
		if( fact>0.0 ) factor = 1.0/fact;
	}

	/**
	 * Remove the specified watchdog from the list.
	 * If this is the fist dog in the list, then restart
	 * the timer. We assume that the IDLE dog will never
	 * be removed. 
	 * @param dog to be removed
	 */
	public synchronized void removeWatchdog(final Watchdog dog) {
		if( dog!=null) {
			log.debugf("%s: Removing dog %s",TAG,dog.toString());
			int index = dogs.indexOf(dog);
			if( index>=0 ) {
				dogs.remove(index);
				dog.setActive(false);
				if( index==0) {   // We popped the top
					watchdogThread.interrupt();
				}
			}
			else {
				log.debugf("%s.removeWatchdog: Unrecognized watchdog (%s)",TAG,dog.toString());
			}
		}
	}
	
	/**
	 * "pet" a watchdog.
	 * Change the time of a specified watchdog. If the watchdog
	 * is not currently in the list, insert it.
	 * @param dog the dog to update. It has already been set 
	 *        with the new expiration time. 
	 */
	public synchronized void updateWatchdog(final Watchdog dog) {
		if( dog==null ) return;
		log.debugf("%s: Updating (pet) dog %s",TAG,dog.toString());
		int index = dogs.indexOf(dog);
		if( index>=0 ) {
			dogs.remove(index);
		}
		// Add dog back in (or for the first time)
		// -- this may trigger an interrupt
		insert(dog);
	}
	
	/**
	 * Insert a new dog into the list in order.
	 * This list is assumed never to be empty
	 */
	private void insert(Watchdog dog) {
		int index=0;
		dog.setActive(true);
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

	/**
	 * If the top dog is the IDLE dog, then simply "pet" it.
	 * Otherwise pop the top dog and inform its observer of the expiration. 
	 * Run the observer in its own thread. 
	 */
	protected final void fireWatchdog() {
		Watchdog head = dogs.getFirst();
		if( head.equals(idleDog) ) {
			idleDog.setDelay(IDLE_DELAY);
			updateWatchdog(idleDog);
		}
		else {
			Watchdog dog = dogs.pop();
			log.debugf("%s.fireWatchdog: %s ",TAG,dog.toString());
			dog.setActive(false);
			threadPool.execute(new WatchdogExpirationTask(dog));
		}
	}

	/**
	 * This is for a restart. Use a new thread.
	 */
	public synchronized void start() {
		if( stopped ) {
			dogs.clear();
			dogs.push(idleDog);
			stopped = false;
			watchdogThread = new Thread(this, "WatchdogTimer");
			watchdogThread.setDaemon(true);
			watchdogThread.start();
			log.infof("%s.RESTART watchdog thread %s (%d)",TAG,watchdogThread.getName(),watchdogThread.hashCode());
		}
	}

	/**
	 * On stop, set all the dogs to inactive.
	 */
	public synchronized void stop() {
		if( !stopped ) {
			for(Watchdog wd:dogs ) {
				wd.setActive(false);
			}
			log.debug(TAG+"STOPPED");
			stopped = true;
			if(watchdogThread!=null) {
				watchdogThread.interrupt();
			}
		}
	}
	
	/**
	 * A timeout causes the head to be notified, then pops up the next dog. 
	 */
	public synchronized void run() {
		while( !stopped  ) {
			long now = System.nanoTime()/1000000;   // Work in milliseconds
			Watchdog head = dogs.getFirst();
			long waitTime = (long)((head.getExpiration()-now)*factor);
			try {
				if( waitTime>0 ) {
					log.tracef("%s.run: WAIT for %d ms",TAG,waitTime);
					wait(waitTime);
				}
				log.tracef("%s.run: wait complete ---",TAG);
				if (!stopped) fireWatchdog();
			} 
			// An interruption merely shortcuts firing the expiration
			catch (InterruptedException e) {
				log.tracef("%s.run: wait interrupted ---",TAG);
			}
			catch( Exception ex ) {
				log.errorf(TAG+": Exception during timeout processing ("+ex.getLocalizedMessage()+")",ex);  // Prints stack trace
			} 
		}
		log.infof("%s.run: END watchdog thread %s (%d)",TAG,watchdogThread.getName(),watchdogThread.hashCode());
	}
}
