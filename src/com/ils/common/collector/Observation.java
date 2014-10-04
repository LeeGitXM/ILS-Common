/**
 *   (c) 2013  ILS Automation. All rights reserved.
 *  
 */
package com.ils.common.collector;

import java.util.Date;


/**
 *  An observation is a collection of data points at a particular time. 
 *  This is the structure passed back and forth
 *  between the data collector its receiver. The time-stamp represents
 *  the time of the latest data read in the collection of data points.
 *  
 *  WARNING: The dataPoints array can be swapped out under some circumstances.
 *           Consequently these points should only be referenced as stack variables.
 */
public class Observation implements Cloneable  {
	public Date timestamp;         // Latest date of the data points.
	public DataPoint[] dataPoints; // Data points in this observation
	/**
	 * 
	 * @param ts - time-stamp applicable to all data points
	 * @param data - data points in the observation
	 */
	public Observation(Date ts,DataPoint[] data) {
		this.timestamp = ts;
		this.dataPoints = data;
	}
	
	/**
	 * Make a deep clone of this observation
	 */
	@Override
	public Observation clone() {
		DataPoint[] points = new DataPoint[dataPoints.length];
		int index = 0;
		while( dataPoints!=null && index<dataPoints.length) {
			if( dataPoints[index]!=null ) {
				points[index] = dataPoints[index].clone();
			}
			index++;
		}
	
		Observation obs = null;
		if( timestamp==null )  {
			obs = new Observation(null,points);
		}
		else {
			obs = new Observation((Date)(timestamp.clone()),points);
		}
		return obs;
	}
	
	/**
	 * This is a debugging aid. We attempt to print a 
	 * meaningful rendering of the observation.
	 */
	public String toString()
	{
		StringBuilder str = new StringBuilder();
		int i=0;
		while(i<dataPoints.length){
			str.append(dataPoints[i].toString());
			str.append(",");
			i++;
		}
		return str.toString();
	}
}
