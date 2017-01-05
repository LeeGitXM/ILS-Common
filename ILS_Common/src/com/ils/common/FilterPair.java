/**
 *   (c) 2017  ILS Automation. All rights reserved.
 */
package com.ils.common;

/**
 * A simple key-value holder for use with KeyedCircularBuffers.
 * 
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

