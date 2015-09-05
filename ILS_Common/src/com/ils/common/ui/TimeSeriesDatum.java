package com.ils.common.ui;

import java.io.Serializable;

/**
 * Hold a new data point to be added to a TimeSeriesSparkChart.
 * The plot expects these to arrive as "user data" inside a
 * Notification event.
 */
public class TimeSeriesDatum implements Serializable {
	private static final long serialVersionUID = -1415210930147352035L;
	private long timestamp;
	private double average;
	private double value;

	
	/** 
	 * Constructor: Sets all attributes.
	 */
	public TimeSeriesDatum(double val,double ave,long time) {
		this.average = ave;
		this.value = val;
		this.timestamp = time;
	}
	/** 
	 * Constructor: No arg version so we're Serializable
	 */
	public TimeSeriesDatum() {
		this(0.0,0.0,0);
	}
	
	public double getAverage() {return this.average;}
	public long getTimestamp() {return this.timestamp;}
	public double getValue() {return this.value;}
	
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public void setAverage(double average) {
		this.average = average;
	}
	public void setValue(double value) {
		this.value = value;
	}
	
}
