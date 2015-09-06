package com.ils.common.ui;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/**
 * Hold a new data point to be added to a TimeSeriesSparkChart.
 * The plot expects these to arrive as "user data" inside a
 * Notification event (or be added directly).
 * 
 * JSON serializers are included.
 */
public class TimeSeriesDatum implements Serializable {
	private static final long serialVersionUID = 1415210930147352035L;
	private final static String TAG = "TimeSeriesDatum";
	private static final LoggerEx log = LogUtil.getLogger(TimeSeriesDatum.class.getPackage().getName());
	private static final ObjectMapper mapper = new ObjectMapper();
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
	public void setAverage(double ave)  {this.average=ave;}
	public void setTimestamp(long time) {this.timestamp=time;}
	public void setValue(double val)    {this.value=val;}
	
	public String toString() {
		return String.format("%s: %5.3f,  %5.3f at %d sec",TAG,value,average,timestamp/1000);
	}
	/**
	 * Deserialize from a Json string 
	 * @param json
	 * @return the prototype object created from the string
	 */
	public static TimeSeriesDatum fromJson(String json) {
		TimeSeriesDatum datum = new TimeSeriesDatum();
		if( json!=null && json.length()>0 )  {
			try {
				datum = mapper.readValue(json, TimeSeriesDatum.class);
			} 
			catch (JsonParseException jpe) {
				log.warnf("%s.fromJson parse exception (%s)",TAG,jpe.getLocalizedMessage());
			}
			catch(JsonMappingException jme) {
				log.warnf("%s.fromJson mapping exception (%s)",TAG,jme.getLocalizedMessage());
			}
			catch(IOException ioe) {
				log.warnf("%s.fromJson IO exception (%s)",TAG,ioe.getLocalizedMessage());
			}; 
		}
		return datum;
	}

	/**
	 * Serialize into a JSON string
	 */
	public String toJson() {
		String json="";
		try {
			json = mapper.writeValueAsString(this);
		}
		catch(Exception ge) {
			log.warnf("%s: toJson (%s)",TAG,ge.getMessage());
		}
		return json;
	}
}
