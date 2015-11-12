package com.ils.common.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.sql.Date;
import java.sql.Timestamp;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.ui.DateChooserPanel;

/**
 * This is a simple panel that allows entry of time-of-day 
 * using a DateChooser plus three combo boxes for the time.
 */
public class DateTimeChooserPanel extends JPanel{
	private static final long serialVersionUID = 1653393576145239228L;
	private static final Dimension COMBO_SIZE = new Dimension(20,24);
	private JComboBox<String> hour, minute, second;
	private DateChooserPanel dateChooser;

	public DateTimeChooserPanel(){
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		initUI();
	}

	public void initUI(){
		dateChooser = new DateChooserPanel();
		add(dateChooser);
		add(createTimeLabel());
		add(createTimePanel());
		add(Box.createVerticalGlue());
	}

	private JPanel createTimePanel() {
		JPanel panel = new JPanel();
		hour = new JComboBox<String>();
		hour.setPreferredSize(COMBO_SIZE);
		minute = new JComboBox<String>();
		minute.setPreferredSize(COMBO_SIZE);
		second = new JComboBox<String>();
		second.setPreferredSize(COMBO_SIZE);
		for(int i = 0; i<24; i++) hour.addItem(String.format("%02d",i));
		for(int i = 0; i<60; i++) minute.addItem(String.format("%02d",i));
		for(int i = 0; i<60; i++) second.addItem(String.format("%02d",i));
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
		panel.add(Box.createHorizontalGlue());
		panel.add(hour);
		panel.add(minute);
		panel.add(second);
		panel.add(Box.createHorizontalGlue());
		panel.setAlignmentX(CENTER_ALIGNMENT);
		return panel;
	}
	
	private JLabel createTimeLabel() {
		JLabel label = new JLabel("  Hr : Min : Sec");
		label.setFont(new Font("Tahoma", Font.PLAIN, 12));
		label.setForeground(Color.BLACK);
		label.setAlignmentX(CENTER_ALIGNMENT);
		return label;
	}
	
	/** 
	 * @return a timestamp representing all the user selections
	 */
	public Timestamp getTimestamp() {
		// Start with the calendar date portion.
		long time = dateChooser.getDate().getTime();
		int hr = hour.getSelectedIndex(); 
		if( hr>=0) time += hr*3600*1000;
		int min = minute.getSelectedIndex(); 
		if( min>=0) time += min*60*1000;
		int sec = second.getSelectedIndex(); 
		if( sec>=0) time += sec*1000;
		
		return new Timestamp(time);
	}
	/** 
	 * @return a timestamp representing all the user selections
	 */
	public void setTimestamp(Timestamp ts) {
		long time = ts.getTime();
		// Start with the calendar date portion.
		long days = time/(24*60*60*1000);
		dateChooser.setDate(new Date(days*24*60*60*1000));
		time = time - days*24*60*60*1000;    // Remainder in a day
		long hr = time/(60*60*1000);
		hour.setSelectedIndex((int)hr);
		time = time - hr*60*60*1000;          // Remainder in the hour 
		long min = time/(60*1000);
		minute.setSelectedIndex((int)min); 
		time = time - min*60*1000;           // Remainder in the minute
		long sec = time/1000;
		second.setSelectedIndex((int)sec); 
	}
}
