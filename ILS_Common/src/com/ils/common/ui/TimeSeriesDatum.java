package com.ils.common.ui;

/**
 * Hold a new data point to be added to a TimeSeriesSparkChart.
 * The plot expects these to arrive as "user data" inside a
 * Notification event.
 */
public class TimeSeriesDatum {
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
	
	public double getAverage() {return average;}
	public long getTimestamp() {return timestamp;}
	public double getValue() {return value;}
	
}
