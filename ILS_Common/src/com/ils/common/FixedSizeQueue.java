/**
 *   (c) 2014-2017  ILS Automation. All rights reserved. 
 */
package com.ils.common;

import java.util.LinkedList;

public class FixedSizeQueue<E> extends LinkedList<E>  {
	private static final long serialVersionUID = 5843873110116467006L;
	private int bufferSize = 10;
	
	public FixedSizeQueue(int length) {
		this.bufferSize = length;
	}
	
	public int getBufferSize() { return bufferSize; }
	
	public synchronized void setBufferSize(int size) {
		// Whittle down the list, if necessary
		if( size<1 ) size = 0;
		while( this.size()>size ) {
			remove();
		}
		bufferSize = size;
	}
	
	@Override
    public synchronized boolean add(E o) {
        if( bufferSize>0) {
        	super.add(o);
        	while (size() > bufferSize) {
        		remove(); 
        	}
        }
        return true;
    }
}
