package com.ils.common.ui;

import java.io.Serializable;

/**
 * Hold a new data point to be added to a TimeSeriesSparkChart.
 * The plot expects these to arrive as "user data" inside a
 * Notification event.
 */
public class TimeSeriesDatum implements Serializable {
	private static final String TAG = "TimeSeriesDatum";
	private final long timestamp;
	private final double average;
	private final double value;

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
		this.average = 0.0;
		this.value = 0.0;
		this.timestamp = 0;
	}
	public double getAverage() {return this.average;}
	public long getTimestamp() {return this.timestamp;}
	public double getValue() {return this.value;}
	
}
