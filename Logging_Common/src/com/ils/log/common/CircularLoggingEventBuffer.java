/**
 *   (c) 2013-2017  ILS Automation. All rights reserved.
 */
package com.ils.log.common;

import java.util.RandomAccess;
import ch.qos.logback.classic.spi.LoggingEvent;
/**
 *  A CircularBuffer is class that contains an array of LoggingEvents. It has a specified 
 *  capacity. As new points are added, the oldest points are discarded. 
 *  The buffer is thread-safe. The "leader" marker is a null.
 *  
 *  NOTE: I tried to do this generically, but you can't create a generic array.
 */
public class CircularLoggingEventBuffer implements RandomAccess {
	private final int n;             // buffer length
	private final LoggingEvent[] buf;
	private int leader = 0;
	private int size = 0;            // current number of entries


	/**
	 * Create a new real-value buffer with the specified capacity.
	 * We create an additional spot for the "leader" marker position.
	 * 
	 * @param capacity - maximum number of observations retained
	 */
	public CircularLoggingEventBuffer(int capacity) {
		if( capacity<0 ) capacity = 0;
		n = capacity + 1;
		buf = new LoggingEvent[n];
		size = 0;
	}
	
	/**
	 * Add a new value to the buffer, presumably losing the oldest
	 * value already there. 
	 * 
	 * @param val the new value.
	 */
	public synchronized void add(LoggingEvent val)
	{ 
		buf[leader] = val;
		leader = wrapIndex(++leader);
		if(size < n-1) this.size++;
	}

	/**
	 * Clear all entries from the buffer.
	 */
	public synchronized void clear()
	{
		this.leader = 0;
		this.size = 0;      
	}


	
	/**
	 * @return an array of all members, in chronological order
	 */
	public synchronized LoggingEvent[] getValues() {
		LoggingEvent[] values = new LoggingEvent[size];
		int i = wrapIndex(leader-size);
		int index = 0;
		while(i != leader){
			//log.trace(String.format("%s getValues %f at %d", TAG,buf[i],i));
			values[index] = buf[i];
			index++;
			i = wrapIndex(++i);
		}
		return values;
	}
	
	/**
	 * @return an array of all members, in chronological order
	 */
	public synchronized LoggingEvent[] getLastNValues(int count) {
		LoggingEvent[] values = new LoggingEvent[count];
		int i = wrapIndex(leader-count);
		int index = 0;
		while(i != leader){
			//log.trace(String.format("%s getValues %f at %d", TAG,buf[i],i));
			values[index] = buf[i];
			index++;
			i = wrapIndex(++i);
		}
		return values;
	}

	/** Get the most recently added  value */
	public synchronized LoggingEvent getLastValue() {
		if(size == 0) throw new IllegalArgumentException("Empty buffer");
		return buf[wrapIndex(leader-1)];
	}

	/** Get the next most recently added  value */
	public synchronized LoggingEvent getNextToLastValue() {
		if(size < 2) throw new IllegalArgumentException("Empty buffer");
		return buf[wrapIndex(leader-2)];
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
			str.append(buf[i]);
			str.append(",");
			i = wrapIndex(++i);
		}
		return str.toString();
	}

}
