/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common.collector;

import java.util.Date;


/**
 *  A data point is the structure passed back and forth
 *  between the data collector and receiver. It contains the
 *  tag name and value, if any. It also contains read-status
 *  information.
 *  
 *  The index is used to assign the data point its rightful position in
 *  the observation. index ranges from 0 to len -1;
 */
public class DataPoint implements Cloneable  {
	public int index;         // Position of this data point in the observation
	public int badReads;      // Consecutive bad reads
	public int missedReads;   // Consecutive read timeouts
	public boolean isGood;
	public String tagPath;    // The full tag path
	public Date timestamp;
	public Object value;      // The value (data type varies)
	public boolean updated;
	
	/**
	 * Create a new data point. If the tag name is null,
	 * then we assume that the value is "calculated" an
	 * does not require a subscription.
	 * @param indx
	 * @param tag
	 */
	public DataPoint(int indx,String tag) {
		this.index = indx;
		this.tagPath = tag;
		this.timestamp = null;
		this.isGood = true;
		this.value = null;
		this.updated = true;    // The initial value is an update
		this.badReads = 0;
		this.missedReads = 0;
	}
	
	/**
	 * Make a deep clone of this data point
	 * -- make the gross assumption that the clone is being made as a result of
	 *    a no-change in data value. In this case we increment badReads.  
	 */
	@Override
	public DataPoint clone() {
		DataPoint clone = new DataPoint(index,tagPath);
		clone.timestamp = (timestamp==null?null:new Date(timestamp.getTime()));
		clone.isGood = isGood;
		clone.value = value;             // Treat the value as if it were immutable
		clone.badReads = badReads;
		clone.missedReads = missedReads;
		return clone;
	}
	
	/**
	 * This is a debugging aid.
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		str.append(index);
		str.append(":");
		if(value!=null) str.append(value);
		str.append((isGood?"(GOOD)":"(BAD)"));
		str.append(".");
		str.append(badReads);
		return str.toString();
	}
}
