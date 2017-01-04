/**
 *   (c) 2013  ILS Automation. All rights reserved.
 */
package com.ils.common;



import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.RandomAccess;

import org.python.core.PyDictionary;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;
/**
 *  A KeyedCircularBuffer is a storage area for objects that are accompanied
 *  with a dictionary used for filtering. As with other circular buffers,
 *  the internal buffer has a specified capacity. As new points
 *  are added, the oldest points are discarded. The buffer
 *  is thread-safe. The "leader" marker is a null.
 *  
 *  This class has methods that allow retrieval of a set number of 
 *  objects that are, optionally, filtered on a key and value.
 */
public class KeyedCircularBuffer implements RandomAccess {
	private final String TAG = "KeyedCircularBuffer: ";
	private final LoggerEx log;
	private final int n;             // buffer length
	private final FilterableValue[] buf; // a List implementing RandomAccess
	private int leader = 0;
	private int size = 0;


	/**
	 * Create a new history buffer with the specified capacity.
	 * We create an additional spot for the "leader" marker position.
	 * 
	 * @param capacity - maximum number of observations retained
	 */
	public KeyedCircularBuffer(int capacity) {
		log = LogUtil.getLogger(getClass().getPackage().getName());
		n = capacity + 1;
		buf = new FilterableValue[n];
		size = 0;
	}
	
	/**
	 * Add a new key-value to the buffer, presumably losing the oldest
	 * key-value already there.
	 * 
	 * @param map a dictionary of filter terms
	 * @param data the value.
	 */
	public synchronized void add(Map<String,String>map,Object data) {
		if( data==null || map==null) return;   // No effect
		log.debugf("%s add %s at %d", TAG,data.toString(),leader);
		FilterableValue fv = new FilterableValue(leader,map,data);
		buf[leader] = fv;
		leader = wrapIndex(++leader);
		if(size < n-1) this.size++;
	}

	/**
	 * Clear all entries from the buffer.
	 */
	public synchronized void clear() {
		int index = 0;
		while(index<n) {
			buf[index]=null;
			index++;
		}
		this.size = 0;      
	}
	
	
	/**
	 * @return an array of all members, in chronological order up to a specified
	 *         maximum count. The trailing entries of the array may be null.
	 */
	public synchronized FilterableValue[] getRecentValues(int max) {
		if( max>size ) max = size;
		FilterableValue[] values = new FilterableValue[max];
		int i = wrapIndex(leader-max);
		int index = 0;
		while(i != leader) {
			if( buf[i]==null ) break;
			values[index] = buf[i];
			i = wrapIndex(++i);
			index++;
		}
		return values;
	}
	
	/**
	 * @return an array of members that match the filter, in chronological order up to a specified
	 *         maximum count. The trailing entries of the array may be null.
	 */
	public synchronized FilterableValue[] getRecentValuesFiltered(List<FilterPair> filters,int max) {
		if( max>size ) max = size;
		FilterableValue[] values = new FilterableValue[max];
		int i = wrapIndex(leader-max);
		int index = 0;
		while(i != leader) {
			if( buf[i]==null ) break;
			if( buf[i].matches(filters)) {
				values[index] = buf[i];
				index++;
			}
			i = wrapIndex(++i);
		}
		return values;
	}
	
	/**
	 * @return an array of members that match the filter and are newer than the specified start index, 
	 *         in chronological order up to a specified maximum count. The trailing entries of the array may be null.
	 */
	public synchronized FilterableValue[] getRecentValuesFilteredFromIndex(List<FilterPair> filters,int start,int max) {
		if( max>size ) max = size;
		FilterableValue[] values = new FilterableValue[max];
		int i = wrapIndex(leader-max);
		int index = 0;
		while(i != leader) {
			if( buf[i]==null ) break;
			if( buf[i].getIndex()<start) break;
			if( buf[i].matches(filters)) {
				values[index] = buf[i];
				index++;
			}
			i = wrapIndex(++i);
		}
		return values;
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
	
	public class FilterableValue {
		private final int index;
		private final List<FilterPair> filterList;
		private final Object value;
		
		public FilterableValue(int indx,Map<String,String> map, Object val) {
			this.index = indx;
			this.filterList = new ArrayList<>();
			for(String key:map.keySet()) {
				filterList.add(new FilterPair(key,map.get(key)));
			}
			this.value = val;
		}

		public int getIndex() { return this.index; }
		public boolean matches(List<FilterPair>keys) {
			for( FilterPair key:keys) {
				if( !filterList.contains(key)) return false;
			}
			return true;
		}
		public Object getValue() { return this.getValue(); }
		public String toString() {
			return String.format("%d: %s",index,value.toString());
		}
	}
	
	/**
	 * If the filter contains one of these it passes. Make the 
	 * class comparable by implementing hashCode and equals.
	 */
	public class FilterPair {
		private final String key;
		private final String value;
		
		public FilterPair(String s1, String s2) {
			this.key = s1;
			this.value= s2;
		}
		
		@Override
		public boolean equals(Object arg) {
			boolean result = false;
			if( arg instanceof FilterPair ) {
				FilterPair that = (FilterPair)arg;
				if( this.key.equals(that.key) &&
					this.value.equals(that.value) ) {
					result = true;
				}
			}
			return result;
		}
		@Override
		public int hashCode() {
			return key.hashCode()-value.hashCode();
		}
	}
}
