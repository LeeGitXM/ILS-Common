/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common.collector;

import java.util.RandomAccess;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
/**
 *  A CircularRealBuffer contains an array of doubles. It has a specified 
 *  capacity. As new points are added, the oldest points are discarded. 
 *  The buffer is thread-safe. The "leader" marker is a null.
 */
public class CircularRealBuffer implements RandomAccess {
	private final String TAG = "CircularRealBuffer: ";
	private final LoggerEx log;
	private final int n;             // buffer length
	private final double[] buf;
	private int leader = 0;
	private int size = 0;            // current number of entries


	/**
	 * Create a new history buffer with the specified capacity.
	 * We create an additional spot for the "leader" marker position.
	 * 
	 * @param capacity - maximum number of observations retained
	 */
	public CircularRealBuffer(int capacity) {
		log = LogUtil.getLogger(getClass().getPackage().getName());
		log.debug(String.format("%s create with capacity %d", TAG,capacity));
		n = capacity + 1;
		buf = new double[n];
		size = 0;
	}
	
	/**
	 * Add a new value to the buffer, presumably losing the oldest
	 * value already there. 
	 * 
	 * @param val the new value.
	 */
	public synchronized void add(double val)
	{ 
		log.debug(String.format("%s add %f at %d", TAG,val,leader));
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
	public synchronized double[] getValues() {
		double[] values = new double[size];
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
	public synchronized double[] getLastNValues(int n) {
		double[] values = new double[n];
		int i = wrapIndex(leader-n);
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
	public synchronized double getLastValue() {
		if(size == 0) throw new IllegalArgumentException("Empty buffer");
		return buf[wrapIndex(leader-1)];
	}

	/** Get the next most recently added  value */
	public synchronized double getNextToLastValue() {
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
