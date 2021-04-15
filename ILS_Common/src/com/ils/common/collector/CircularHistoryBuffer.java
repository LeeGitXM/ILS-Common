/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common.collector;



import java.util.Date;
import java.util.RandomAccess;

import com.ils.common.log.ILSLogger;
import com.ils.common.log.LogMaker;
/**
 *  A CircularHistoryBuffer is a storage area for Observation
 *  objects. The buffer has a specified capacity. As new points
 *  are added, the oldest points are discarded. The buffer
 *  is thread-safe. The "leader" marker is a null.
 */
public class CircularHistoryBuffer implements RandomAccess {
	private final String TAG = "CircularHistoryBuffer: ";
	private final static long DEFAULT_OBSERVATION_INTERVAL = 60000; // 1 minute
	private final ILSLogger log;
	private final int n;             // buffer length
	private final Observation[] buf; // a List implementing RandomAccess
	private int leader = 0;
	private int size = 0;


	/**
	 * Create a new history buffer with the specified capacity.
	 * We create an additional spot for the "leader" marker position.
	 * 
	 * @param capacity - maximum number of observations retained
	 */
	public CircularHistoryBuffer(int capacity) {
		log = LogMaker.getLogger(getClass().getPackage().getName());
		n = capacity + 1;
		buf = new Observation[n];
		size = 0;
	}
	
	/**
	 * Add a new observation to the buffer, presumably losing the oldest
	 * observation already there. If the observation has no timestamp,
	 * then we assume that it came as a result of a timeout. In that
	 * case, set the time from the previous reading.
	 * 
	 *  There appears to be a race condition where a timeout is not
	 *  reset soon enough on data receipt - causing two identical
	 *  observations. We use the second because it has the real measurement.
	 *  
	 *  It should not be possible to store an observation without a timestamp.
	 * 
	 * @param obs the new observation.
	 */
	public synchronized void add(Observation obs)
	{
		if( obs==null) return;   // No effect
		if( this.size>0 ) {
			int i = wrapIndex(leader-1);
			Observation mostRecent = buf[i];
			if( obs.timestamp==null ) {
				obs.timestamp = new Date(mostRecent.timestamp.getTime()+getObservationInterval()); 
			}
			else if( obs.timestamp.getTime()<=mostRecent.timestamp.getTime()) {
				log.debug(TAG+"Replacing observation at "+obs.timestamp.toString()+", with "+mostRecent.timestamp.toString());
				buf[i]=obs;
				return;
			}
		}
		else if( obs.timestamp==null ){
			obs.timestamp = new Date(0);   // Beginning of the epoch. Should never happen.
		}
		buf[leader] = obs;
		leader = wrapIndex(++leader);
		buf[leader] = null;      // We keep an empty spot
		if(size < n-1) this.size++;
		
		log.tracef("%s: Added observation to history at %s",TAG,obs.timestamp.toString());
	}

	/**
	 * Clear all entries from the buffer.
	 */
	public synchronized void clear()
	{
		int index = 0;
		while(index<n) {
			buf[index]=null;
			index++;
		}
		this.size = 0;      
	}
	/**
	 * Clear all except the current observation from the buffer.
	 */
	public synchronized void clearHistory()
	{
		if( size==0 ) return;
		int i = wrapIndex(leader-1); // Most recent
		int index = 0;
		while(index<n) {
			if( index!=i ) buf[index]=null;
			index++;
		}
		this.size = 1;      
	}
	/**
	 * @return the most recent observation.
	 */
	public synchronized Observation getCurrentObservation() {
		Observation result = null;
		if( size==0  ) return result;
		int i = wrapIndex(leader-1);
		result = buf[i];
		return result;
	}
	
	/**
	 * Obtain a specified data point from the most recent observation.
	 * We assume the intent here is to edit the contents, a calculated
	 * value to remain in the object history. This is not a copy.
	 * 
	 * @param index the index of the data point within the observation.
	 * @return the data point structure.
	 */
	public synchronized DataPoint getNewest(int index) {
		DataPoint result = null;
		if( size==0  ) return result;
		
		int i = wrapIndex(leader-1);
		Observation mostRecent = buf[i];
		if( index>0 && index<mostRecent.dataPoints.length ) {
			result = mostRecent.dataPoints[index];
		}	
		log.debug(TAG+"got newest at "+mostRecent.timestamp.toString());
		return result;
	}
	
	/**
	 * Count the number of observations within a specified time in the past.
	 * @param time  interval in the past, in msecs from the latest 
	 *                timestamp in the buffer.
	 * @return the count of observations within the indicated time.
	 */
	public synchronized int getObservationCount(long time) {
		int count = 0;
		if( size==0  ) return count;
		if( size==1  ) return 1;
		if( time>0 ) {
			int i = wrapIndex(leader-1);
			Observation mostRecent = buf[i];
			Observation past = null;
			long latest = mostRecent.timestamp.getTime();
			count = 1;
			for(; i != leader; i = wrapIndex(--i)){
				past = buf[i];
				if( past==null ) break;
				// getTime returns milliseconds
				if( latest - past.timestamp.getTime() >= time ) break;
				count++;
			}
		}
		// Just return the most recent
		else {
			count = 1;
		}
		
		return count;
	}
	
	/**
	 * Determine the current span of the historical record.
	 * @return the time between the newest and oldest records ~ msecs.
	 */
	public synchronized long getHistoryDuration() {
		long span = 0;
		if( size==0  ) return span;
		if( size==1  ) return span;
		
		int i = wrapIndex(leader-1);
		Observation mostRecent = buf[i];
		long recentTime = mostRecent.timestamp.getTime();
		i = wrapIndex(--i); 
		Observation past = null;
		for(; i != leader; i = wrapIndex(--i)){
			if( buf[i]==null) break;
			past = buf[i];
		}
		if( past!=null ) {
			span = recentTime - past.timestamp.getTime();
		}
		log.debug(TAG+"getHistoryDuration =  "+span);
		return span;
	}
	
	/**
	 * Assuming the intervals between consecutive observations are constant,
	 * return the time differential between the latest and previous. 
	 * 
	 * @return evaluation interval in milliseconds. If there is
	 * only a single entry in the table, return a default.
	 */
	public synchronized long getObservationInterval() {
		long result = DEFAULT_OBSERVATION_INTERVAL;
		if( size>1 ) {
			int i = wrapIndex(leader-1);
			Observation mostRecent = buf[i];
			i = wrapIndex(--i);
			Observation prior = buf[i];
			result = mostRecent.timestamp.getTime() - prior.timestamp.getTime();
		}
		return result;
	}
	
	/**
	 * @return an array of all members, in chronological order
	 */
	public synchronized Observation[] getHistory() {
		Observation[] observations = new Observation[size];
		int i = wrapIndex(leader-size);
		int index = 0;
		while(i != leader) {
			if( buf[i]==null ) break;
			observations[index] = buf[i];
			log.trace(TAG+"getHistory: "+index+" "+buf[i].timestamp.toString());
			i = wrapIndex(++i);
			index++;
		}
		return observations;
	}
	
	/**
	 * @param timeInterval ~ msecs
	 * @return an array of members younger than a specified interval, in chronological order
	 */
	public synchronized Observation[] getHistory(long timeInterval) {
		int count = getObservationCount(timeInterval);
		if( count==0 ) return null;     // No observations at all
		Observation[] observations = new Observation[count];
		int i = wrapIndex(leader-1);
		int index = 0;
		while( index<count) {
			observations[index] = buf[i];
			i = wrapIndex(--i);
			index++;
		}
		return observations;
	}
	
	/**
	 * @param count of of observations in the past to jump tp
	 * @return an array of a specified number of members, in chronological order.
	 */
	public synchronized Observation[] getPastObservations(int count) {
		if( count==0 ) return null;     // No observations at all
		if( count>size()) return null;  // Don't return a partial array
		Observation[] observations = new Observation[count];
		int i = wrapIndex(leader-1);
		int index = 0;
		while( index<count) {
			observations[index] = buf[i];
			i = wrapIndex(--i);
			index++;
		}
		return observations;
	}
	
	/**
	 * Find a data value in the past. Take the value with the next
	 * time stamp greater than or equal to the one specified. If the
	 * buffer is empty, return a null. Presumably we've tested the
	 * history buffer before this call to make sure it is large enough.
	 * 
	 * In the future, this ought to interpolate.
	 * 
	 * @param index  index of the data point desired. If the index
	 *                is out of range, we throw an index-out-of-bounds
	 *                exception.
	 * @param time  interval in the past, in msecs from the latest 
	 *                time stamp in the buffer.
	 * @return the value of the selected data point.
	 */
	public synchronized Object getPastValue(int index,long time) {
		Object result = null;
		if( size==0 ) return result;
		
		log.debug(TAG+"getPastValue: ago = "+time);
		int i = wrapIndex(leader-1);
		Observation prior = buf[i];   // This will be the next-best.
		long recentTime = prior.timestamp.getTime();
		
		for(; i != leader; i = wrapIndex(--i)){
			if( buf[i] != null ) {
				log.debug(TAG+"getPastValue: past="+buf[i].timestamp.toString());
				// getTime returns milliseconds
				if( recentTime - buf[i].timestamp.getTime() >= time ) {
					if( index<buf[i].dataPoints.length ) {
						if(buf[i].dataPoints[index]!=null ) {
							result = buf[i].dataPoints[index].value;
						}
					}
					return result;  
				}
				prior = buf[i];
			}
		}
		// Just return the most recent.
		if( index<prior.dataPoints.length ) result = prior.dataPoints[index].value;
		return result;
	}
	
	/**
	 * Find a data value immediately preceding the current one. If there are less than
	 * two entries return a null.
	 * 
	 * @param index  index of the data point desired. If the index
	 *                is out of range, we throw an index-out-of-bounds
	 *                exception.
	 * @return the value of the selected data point.
	 */
	public synchronized Object getPriorValue(int index) {
		Object result = null;
		if( size < 2 ) return result;
		
		int i = wrapIndex(leader-1);
		i = wrapIndex(--i);
		Observation past = buf[i];
		result =  past.dataPoints[index].value;
		log.debug(TAG+"got prior at "+past.timestamp.toString());
		return result;
	}
	
	/**
	 * @return the current number of observations in the history
	 */
	public int size() {
		return this.size;
	}
		
	/** 
	 * Keep an incrementing index with range of the buffer limits.
	 * It also serves as a safe-guard to insure that any index is
	 * within bounds.
	 * @param i
	 * @return an index into the history buffer, guaranteed to be within range
	 */
	private int wrapIndex(int i) {
		int m = i % n;
		if (m < 0) { // modulus can be negative
			m += n;
		}
		return m;
	}

	/**
	 * This is a debugging aid. We attempt to print a 
	 * meaningful rendering of the buffer.
	 */
	public String toString()
	{
		int i = wrapIndex(leader - size);
		StringBuilder str = new StringBuilder(size());

		while(i != leader){
			if( buf[i]!=null ) str.append(buf[i].toString());
			str.append("\n");
			i = wrapIndex(++i);
		}
		return str.toString();
	}

}
