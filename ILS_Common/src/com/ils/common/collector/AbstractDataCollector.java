/**
 *   (c) 2013-2021  ILS Automation. All rights reserved.
 */
package com.ils.common.collector;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;


/**
 *  The data collector encapsulates the business rules concerning
 *  the sufficiency of receiving inputs for a combined calculation.
 *  In this basic implementation, an instance of this class waits
 *  for inputs on a set of tags and informs the listeners when the
 *  inputs are ready. The getObservation() method is may be called at
 *  any time, even if acquisition is not complete. In the case
 *  of an incomplete observation, any points not refreshed will 
 *  remain at their prior values. 
 *  
 *  The Concrete implementation must, at a minimum, override the 
 *  methods for actual data collection.
 *  
 *  NOTE: A CopyOnWriteArrayList handles synchronization issues internally.
 */
public abstract class AbstractDataCollector  {
	private static final String TAG = "AbstractDataCollector";
	private final ILSLogger log;
	private List<DataCompleteListener> listenerList = new CopyOnWriteArrayList<DataCompleteListener>();
	protected CollectorState state;
	private final Observation prototype;
	private final Hashtable<String,Integer> positionMap;
	private int badReadTolerance = 0;

	/**
	 * Constructor: 
	 * @param obs - the prototype observation. This includes tag names
	 *              sufficient for establishing subscriptions to acquire
	 *              values. This prototype value will be initialized
	 *              to the current values of the tags referenced.
	 */
	public AbstractDataCollector( Observation obs) {
		this.prototype = obs;
		this.log = LogMaker.getLogger(getClass().getPackage().getName());
		this.state = CollectorState.RESET;
		this.positionMap = new Hashtable<String,Integer>();
	}

	public void addDataCompleteListener(DataCompleteListener listener) {
		listenerList.add(listener);
	}

	public void removeDataCompleteListener(DataCompleteListener listener) {
		listenerList.remove(listener);
	}
	/**
	 * Invalidate the indicated point by setting it to null. 
	 * Stop any subscription on the existing point. 
	 * We assume that this is used in an update situation and
	 * not when constructing the initial prototype. Replaces the data points in the prototype.
	 * @param index the index of the old data point.
	 */
	public void removeDataPoint(int index) {
		// Operate on the existing array.
		if( index<prototype.dataPoints.length && prototype.dataPoints[index]!=null ) {
			log.tracef("%s: removeDataPoint. Removing data point at %d (%s)",TAG,index,prototype.dataPoints[index].tagPath);
			stopSubscription(prototype.dataPoints[index]); 
			prototype.dataPoints[index] = null;  // Don't use any more
		}
	}
	
	/**
	 * Append a new data point to the end of the array, invalidating an existing point. 
	 * Stop any subscription on the old point, and start one on the new immediately. 
	 * We assume that this is used in an update situation and
	 * not when constructing the initial prototype. Replaces the data points in the prototype.
	 * 
	 * CAUTION: The old data points may have acquired data. Preserve these values in the new points.
	 * @param point
	 * @param oldIndex the index of the old data point.
	 */
	public void replaceDataPoint(DataPoint point, int oldIndex) {
		if( point==null ) return;
		DataPoint[] newPoints = new DataPoint[prototype.dataPoints.length+1];
		// Move the existing points over - including any recent data values.
		for(int i=0;i<newPoints.length-1;i++) {
			newPoints[i] = prototype.dataPoints[i];
			if( i==oldIndex) {
				stopSubscription(prototype.dataPoints[i]);
				newPoints[i] = null;  // Don't use any more
			}
		}
		newPoints[prototype.dataPoints.length] = point;
		startSubscription(point);
		positionMap.put(point.tagPath, new Integer(point.index));
		prototype.dataPoints = newPoints;
		log.tracef("%s: replaceDataPoint. extended point array to %d with %s",TAG,prototype.dataPoints.length,point.tagPath);
	}
	/**
	 * @return the number of data points being collected.
	 */
	public int getSize() { return prototype.dataPoints.length; }
	
	/**
	 * Remove a data point from active acquisition. The point
	 * remains in the list, but is neutered.
	 * @param index zero-based index of the point to be disabled.
	 */
	public void disableDataPoint(int index) {
		if( index<prototype.dataPoints.length ) {
			DataPoint point = prototype.dataPoints[index];
			if( point.tagPath!=null ) {
				stopSubscription(point);
				positionMap.remove(new Integer(point.index));
			}
			point.tagPath = null;
		}
	}
	/**
	 * Re-enable a data point that had previously been disabled. 
	 * The point's tag path is re-set and a subscription established.
	 * If the point had not been disabled, we do nothing and return false.
	 * 
	 * @param index zero-based index of the point to be disabled.
	 * @param path tag path to the data point to be enabled.
	 * @return true if the operation was successful.
	 */
	public boolean enableDataPoint(int index,String path) {
		boolean result = false;
		if( index<prototype.dataPoints.length ) {
			DataPoint point = prototype.dataPoints[index];
			if( point.tagPath==null ) {
				point.tagPath = path;
				startSubscription(point);
				positionMap.put(point.tagPath, new Integer(point.index));
				result = true;
			}
		}
		return result;
	}
	
	/**
	 * We have received an update from one data point, see if we've got them all.
	 * If so, then inform interested listeners. Ignore data points without
	 * tag names, as these are assumed to be "calculated", not collected.
	 */
	protected void checkCollectionComplete() {
		for( DataPoint point:prototype.dataPoints) {
			if( point!=null && point.tagPath!=null && point.updated==false)  return;   // We're not done yet
		}
		initializeDataPointsForNextCycle();
		fireDataComplete();

	}
	/**
	 * "start" is the signal to activate all our subscriptions and
	 * start the timeout timer. Take the proxy observation and create
	 * a subscription for each data point.
	 */
	public void start() {
		int index = 0;
		for( DataPoint point : prototype.dataPoints ) {
			if( point!=null ) {
				point.updated = false;
				point.missedReads = 0;
				point.badReads    = 0;
				if( point.tagPath!=null ) {
					positionMap.put(point.tagPath, new Integer(point.index));
					startSubscription(point);
				}
				if(log.isTraceEnabled()) {
					log.tracef("%s: start %s (%d)",TAG, (point.tagPath==null?"null":point.tagPath),point.index);
				}
				index++;
			}
			else {
				log.warnf("%s: start: Data point %d of %d is null", TAG,index,prototype.dataPoints.length);
			}
		}
	}

	/**
	 * "stop" is the signal to de-activate all our subscriptions.
	 * This allows us to be garbage-collected.
	 */
	public void stop() {
		for( DataPoint point : prototype.dataPoints ) {
			if( point!=null && point.tagPath!=null ) stopSubscription(point);
		}
		listenerList.clear();
	}

	/**
	 *  Notify all listeners that have registered interest for
	 *  notification of collection complete.
	 */
	protected void fireDataComplete() {
		DataCompleteEvent event = new DataCompleteEvent();
		List<DataCompleteListener> listenersToDisconnect = null;
		
		ListIterator<DataCompleteListener> iterator = listenerList.listIterator();
		while( iterator.hasNext() ) {
			DataCompleteListener listener = iterator.next();
			try {
				listener.dataCollected(event);
			}
			catch( Exception ex ) {
				log.error(TAG+": Exception on data collection complete ("+ex.getMessage()+")", ex);
				// Disconnect any listeners that throw exceptions
				if(listenersToDisconnect==null ) listenersToDisconnect = new ArrayList<DataCompleteListener>();
				listenersToDisconnect.add(listener);
			}
		}
		if(listenersToDisconnect!=null ) {
			for (DataCompleteListener listener:listenersToDisconnect) {
				listenerList.remove(listener);
			}
		}
	}
	
	/**
	 * Increment missed and bad reads, set all points update status to false. 
	 * This method is called whenever a collection of reads is complete. However, 
	 * it may also be called independently from outside this class. An example
	 * of when it may be desirable to call the method independently might be
	 * if results were collected with a getObservation() before notification
	 * that the read was complete.
	 * 
	 * @return the number of data points that had NOT been updated this cycle.
	 */
	public synchronized int initializeDataPointsForNextCycle() {
		// Mark  data points as not-yet-updated
		// in preparation for the next round.
		int count = 0;
		for( DataPoint point:prototype.dataPoints) {
			if( point==null ) continue;
			if( point.updated == false ) {
				count++;
				point.missedReads++;
				if( point.badReads > 0 ) {
					point.badReads++;
					if( point.badReads > badReadTolerance ) point.isGood = false;
				}
			}
			point.updated = false;
		}
		return count;
	}
	/**
	 * Handle receipt of a new quality-only value. This method should be called
	 * by the subclass whenever it receives an update via subscription where the
	 * value is null and the TagProp is not "Property". Presumably the quality
	 * has gone bad and the value is not available.Leave it unchanged.
	 */
	protected synchronized void reportQualityChanged(String path,Date ts, boolean good) {
		// Search the prototype for the data point with this path
		//log.infof("%s: reportQualityChanged: tag path (%s)",TAG,path);
		if( path!=null) {
			Integer indx = positionMap.get(path);
			if( indx!=null) {
				int index = indx.intValue();
				if( index>=0 && index<prototype.dataPoints.length && prototype.dataPoints[index]!=null) {
					DataPoint point = prototype.dataPoints[index];
					point.updated = true;
					point.missedReads = 0;
					if( good ) {
						point.badReads = 0;
						point.isGood = true;
					}
					else if( point.isGood==false){ // Already bad
						point.badReads++;
					}
					else {                         // "provisionally" good			
						point.badReads++;
						if( point.badReads > badReadTolerance) {
							point.isGood = false;
						}
					}					
					point.timestamp = ts;
					prototype.timestamp = ts;    // Update the prototype with the data collection time
					log.tracef("%s: reportQualityChanged %s (index %d at %s)",TAG,path,index,ts.toString());
					checkCollectionComplete();
				}
				else {
					log.errorf("%s: reportQualityChanged: point for %s is null, index=%d",TAG,path,indx);
				}
			}
			else {
				log.errorf("%s: reportQualityChanged: unknown tag path (%s)",TAG,path);
			}
		}
		else {
		   //log.trace(TAG+"reportQualityChanged: null (ignored) ");
		}
	}
	/**
	 * Handle receipt of a new data value.This method should be called
	 * by the subclass whenever it receives an update via subscription.
	 * 
	 * Note that calculated tags (those with null tag paths) cannot be updated
	 * with this method. Those values must be set explicitly.
	 * 
	 * Ignore null (calculated) values. 
	 * NOTE: Missed and bad read accounting is handled in initializeDataPointsForNextCycle
	 *       which must be called externally, since we don't get notified unless there
	 *       is a change.
	 */
	protected synchronized void reportValueChanged(String path,Date ts, Object val, boolean good) {
		// Search the prototype for the data point with this path
		if( val!=null && path!=null) {
			Integer indx = positionMap.get(path);
			if( indx!=null) {
				int index = indx.intValue();
				if( index>=0 && index<prototype.dataPoints.length && prototype.dataPoints[index]!=null) {
					DataPoint point = prototype.dataPoints[index];
					point.updated = true;
					point.missedReads = 0;
					if( good ) {
						point.badReads = 0;
						point.isGood = true;
						point.value = val;
					}
					else if( point.isGood==false){ // Already bad
						point.badReads++;
					}
					else {                         // "provisionally" good			
						point.badReads++;
						if( point.badReads > badReadTolerance) {
							point.isGood = false;
						}
					}					
					point.timestamp = ts;
					prototype.timestamp = ts;    // Update the prototype with the data collection time
					log.tracef("%s: reportValueChanged %s (index %d at %s)",TAG,path,index,ts.toString());
					checkCollectionComplete();
				}
				else {
					log.errorf("%s: reportValueChanged: point for %s is null, index=%d",TAG,path,indx);
				}
			}
			else {
				log.errorf("%s: reportValueChanged: unknown tag path (%s)",TAG,path);
			}
		}
		else {
		   //log.trace(TAG+"reportValueChanged: null (ignored) ");
		}
	}

	/**
	 * The subclass must manage the subscriptions.
	 * @param point the data point representing a tag
	 */
	protected abstract void startSubscription(DataPoint point);
	protected abstract void stopSubscription(DataPoint point);
	
	/**
	 * Set the collector's tolerance to bad read. The value supplied
	 * represents the number of consecutive bad reads before concluding 
	 * that the point quality is bad.
	 * @param tol
	 */
	public void setBadReadTolerance(int tol) {
		this.badReadTolerance = tol;
	}
	
	/**
	 * Access the observation "in work". The object returned
	 * should be treated as "read-only. It is not a clone.
	 * 
	 * @return the current observation.
	 */
	public Observation getObservation() {
		return this.prototype;
	}
	
	/**
	 * Update the timestamp of the prototype observation.
	 * This is used in the case where none of the data points
	 * has been updated for a cycle. Thus the data has not changed,
	 * but a normal collection interval has passed.
	 * 
	 * @param ts new timestamp.
	 */
	public void updatePrototypeTimestamp(Date ts) {
		prototype.timestamp = ts;
	}
	
}
