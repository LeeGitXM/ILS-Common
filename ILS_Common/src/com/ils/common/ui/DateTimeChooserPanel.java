package com.ils.common.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.ui.DateChooserPanel;

/**
 * This is a simple panel that allows entry of time-of-day 
 * using three combo boxes.
 */
public class DateTimeChooserPanel extends JPanel{
	private static final long serialVersionUID = 1653393576145239228L;
	private static final Dimension COMBO_SIZE = new Dimension(20,32);
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
		for(int i = 1; i<12; i++) hour.addItem(String.format("%02d",i));
		for(int i = 0; i<59; i++) minute.addItem(String.format("%02d",i));
		for(int i = 0; i<59; i++) second.addItem(String.format("%02d",i));
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
}
