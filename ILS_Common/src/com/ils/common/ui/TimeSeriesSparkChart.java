package com.ils.common.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.util.Date;

import javax.management.Notification;
import javax.management.NotificationListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;

import com.inductiveautomation.ignition.common.util.LogUtil;
import com.inductiveautomation.ignition.common.util.LoggerEx;

/* ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 *
 * (C) Copyright 2000-2014, by Object Refinery Limited and Contributors.
 * 
 * Project Info:  http://www.jfree.org/jfreechart/index.html
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * [Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.]
 *
 * -------------------------
 * TimeSeriesChartDemo.java
 * -------------------------
 * (C) Copyright 2003-2014, by Object Refinery Limited and Contributors.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
*/
/**
 * Create a chart for the quick display of time-series data. The chart must 
 * update live as new values are presented. To display in a dialog, the component
 * to add is: getChartPanel(().
 */
public class TimeSeriesSparkChart implements NotificationListener {
	private static final String TAG = "TimeSeriesSparkChart";
	private static final long serialVersionUID = 8598531428961307855L;
	private TimeSeriesCollection timeSeriesCollection;        // Collection of time series data  
	private XYDataset xyDataset;                              // Dataset that will be used for the chart  
	private TimeSeries rawSeries;                               // raw series data
	private TimeSeries meanSeries;                              // mean series data
	private final ChartPanel chartPanel;
	private final String yAxisLabel;
	private final LoggerEx log;

	/**
	 * Constructor:
	 * @param title
	 * @param ylabel
	 */
	public TimeSeriesSparkChart(String title, String ylabel ) {  
		this.yAxisLabel = ylabel; 
		this.log = LogUtil.getLogger(getClass().getPackage().getName());
		timeSeriesCollection = new TimeSeriesCollection();
		rawSeries = new TimeSeries("ylabel",Second.class); 
		meanSeries = new TimeSeries("mean",Second.class);    

		timeSeriesCollection.addSeries(rawSeries);  
		timeSeriesCollection.addSeries(meanSeries);   

		JFreeChart chart = createChart(title,yAxisLabel,timeSeriesCollection);  
		chartPanel = new ChartPanel(chart);  
		chartPanel.setFillZoomRectangle(true);     
	}  

	public ChartPanel getChartPanel() { return this.chartPanel; }
	
	public void setPreferredSize(Dimension dimension) {chartPanel.setPreferredSize(dimension);}

	private JFreeChart createChart(String title,String label,TimeSeriesCollection tsCollection) {  

		JFreeChart chart = ChartFactory.createTimeSeriesChart(  
				title,            // title  
				"",               // x-axis label  
				label,            // y-axis label  
				tsCollection,     // data  
				false,            // create legend?  
				true,             // generate tooltips?  
				false             // generate URLs?  
				);  

		chart.setBackgroundPaint(Color.lightGray);  

		XYPlot plot = (XYPlot) chart.getPlot();  
		DateAxis axis = (DateAxis) plot.getDomainAxis();  
		axis.setAutoRange(true);  
		axis.setFixedAutoRange(60000.0);  
		return chart;  
	}

	public void addDatum(TimeSeriesDatum datum) {
		//log.infof("%s.addDatum: %s",TAG,datum.toString());
		Second secs = new Second(new Date(datum.getTimestamp()));
		this.timeSeriesCollection.getSeries(0).addOrUpdate(secs,datum.getValue());  
		this.timeSeriesCollection.getSeries(1).addOrUpdate(secs,datum.getAverage()); 
	}
	// ============================== Listener Interface =======================
	/**
	 * The user-data inside the notification is expected to be a TimeSeriesDatum.
	 * @param event
	 * @param handback
	 */
	@Override  
	public synchronized void handleNotification(Notification event,Object handback) {  
		if( event.getUserData() instanceof TimeSeriesDatum ) {
			TimeSeriesDatum datum = (TimeSeriesDatum)event.getUserData();
			addDatum(datum); 
		}
	}
}
