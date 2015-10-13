package com.ils.common.ui;

import java.awt.BasicStroke;
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
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesChangeListener;
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
public class TimeSeriesSparkChart implements NotificationListener, SeriesChangeListener {
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
		// Note: The class of the time-series time period class is inferred
		//       when the first data item is added to the set.
		rawSeries = new TimeSeries("ylabel"); 
		meanSeries = new TimeSeries("mean");    

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
		
		final XYItemRenderer renderer = plot.getRenderer();
        if (renderer instanceof StandardXYItemRenderer) {
            final StandardXYItemRenderer rr = (StandardXYItemRenderer) renderer;
            rr.setPlotLines(true);
            rr.setSeriesPaint(0, Color.RED, false);
            rr.setSeriesPaint(1, Color.BLUE, false);
            renderer.setSeriesStroke(0, new BasicStroke(2.0f));
            renderer.setSeriesStroke(1, new BasicStroke(2.0f));
        }
		return chart;  
	}
	//Adding a new value to the time series triggers a SeriesChangeEvent
	public void addDatum(TimeSeriesDatum datum) {
		log.infof("%s.addDatum: %s",TAG,datum.toString());
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

	@Override
	public void seriesChanged(SeriesChangeEvent changeEvent) {
		log.infof("%s.seriesChanged: %s",TAG,changeEvent.toString());
		chartPanel.repaint();
		chartPanel.updateUI();
	}
}
