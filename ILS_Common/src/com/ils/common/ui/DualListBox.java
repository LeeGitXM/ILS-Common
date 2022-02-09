package com.ils.common.ui;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

import com.ils.common.SortedListModel;

public class DualListBox extends JPanel{

	private static final long serialVersionUID = 7655516252196772688L;
	private static final Insets EMPTY_INSETS = new Insets(0,0,0,0);
	private static final String DEFAULT_SOURCE_CHOICE_LABEL = "Available Choices";
	private static final String DEFAULT_DEST_CHOICE_LABEL = "Your Choices";
	public static final String PROPERTY_CHANGE_UPDATE = "Dual Updated";
	private static Icon addIcon = new ImageIcon(DualListBox.class.getResource("/images/arrow_right_blue.png"));
	private static Icon removeIcon = new ImageIcon(DualListBox.class.getResource("/images/arrow_left_blue.png"));
	private JLabel sourceLabel;
	private JList<String> sourceList;
	private SortedListModel<String> sourceListModel;
	private JLabel destLabel;
	private JList<String> destList;
	private SortedListModel<String> destListModel;
	
	private JButton addButton;
	
	private JButton removeButton;
	
	public DualListBox(){
		initScreen();
	}
	
	public void addDestinationElements(List<String> newValue){
		fillListModel(destListModel, newValue);
	}
	
	public void addDestinationElements(ListModel<String> newValue){
		fillListModel(destListModel, newValue);
	}
	
	public void addDestinationElements(String newValue[]){
		fillListModel(destListModel, newValue);
	}
	
	public void addSourceElements(List<String> newValue){
		fillListModel(sourceListModel, newValue);
	}
	
	public void addSourceElements(ListModel<String> newValue){
		fillListModel(sourceListModel, newValue);
	}

	public void addSourceElements(String newValue[]){
		fillListModel(sourceListModel, newValue);
	}
	
	public void clearDestinationListModel(){
		destListModel.clear();
	}	

	
	public void clearSourceListModel(){
		sourceListModel.clear();
	}

	public Iterator<String> destinationIterator(){
		return destListModel.iterator();
	}
	
	public ListCellRenderer<? super String> getDestinationCellRenderer(){
		return destList.getCellRenderer();
	}
	public String getDestinationChoicesTitle(){
		return destLabel.getText();
	}
	public List<String> getDestinations(){
		List<String> list = new ArrayList<>();
		Iterator<String> iter = destListModel.iterator();
		while(iter.hasNext()) {
			list.add(iter.next());
		}
		return list;
	}
	public Color getSelectionBackground(){
		return sourceList.getSelectionBackground();
	}
	
	public Color getSelectionForeground(){
		return sourceList.getSelectionForeground();
	}

	public ListCellRenderer<? super String> getSourceCellRenderer(){
		return sourceList.getCellRenderer();
	}

	public String getSourceChoicesTitle(){
		return sourceLabel.getText();
	}
	
	public int getVisibleRowCount() {
		return sourceList.getVisibleRowCount();
	}
	
	public void setDestinationCellRenderer(ListCellRenderer<String> newValue){
		destList.setCellRenderer(newValue);
	}
	
	public void setDestinationChoicesTitle(String newValue){
		destLabel.setText(newValue);
	}
	public void setDestinationElements(List<String> values){
		clearDestinationListModel();
		String[] valueArray = new String[values.size()];
		int index=0;
		for(String val:values) {
			valueArray[index] = val;
			index++;
		}
		addDestinationElements(valueArray);
	}
	public void setSelectionBackground(Color newValue){
		sourceList.setSelectionBackground(newValue);
		destList.setSelectionBackground(newValue);
	}

	public void setSelectionForeground(Color newValue){
		sourceList.setSelectionForeground(newValue);
		destList.setSelectionForeground(newValue);
	}

	public void setSourceCellRenderer(ListCellRenderer<String> newValue){
		sourceList.setCellRenderer(newValue);
	}
	
	public void setSourceChoicesTitle(String newValue){
		sourceLabel.setText(newValue);
	}
	
	public void setSourceElements(List<String> values){
		clearSourceListModel();
		String[] valueArray = new String[values.size()];
		int index=0;
		for(String val:values) {
			valueArray[index] = val;
			index++;
		}
		addSourceElements(valueArray);
	}
	
	public void setSourceElements(ListModel<String> newValue){
		clearSourceListModel();
		addSourceElements(newValue);
	}
	
	public void setSourceElements(String newValue[]){
		clearSourceListModel();
		addSourceElements(newValue);
	}
	
	public void setVisibleRowCount(int newValue){
		sourceList.setVisibleRowCount(newValue);
		destList.setVisibleRowCount(newValue);
	}
	
	public Iterator<String> sourceIterator(){
		return sourceListModel.iterator();
	}
	
	private void clearDestinationSelected(){
		List<String> selectedList = destList.getSelectedValuesList();
		for (String sel:selectedList){
			destListModel.removeElement(sel);
		}
		destList.getSelectionModel().clearSelection();
		firePropertyChange(PROPERTY_CHANGE_UPDATE, "NA", "BOOP");
	}
	
	private void clearSourceSelected(){
		List<String> selectedList = sourceList.getSelectedValuesList();
		for (String sel:selectedList){
			sourceListModel.removeElement(sel);
		}
		sourceList.getSelectionModel().clearSelection();
		firePropertyChange(PROPERTY_CHANGE_UPDATE, "NA", "BOOP");
	}
	
	private void fillListModel(SortedListModel<String> model, List<String> newValues){
		for (String val:newValues) {
			model.add(val);
		}
	}
	
	private void fillListModel(SortedListModel<String> model, ListModel<String> newValues){
		int size = newValues.getSize();
		for (int i=0; i<size; i++) {
			model.add(newValues.getElementAt(i));
		}
	}
	
	private void fillListModel(SortedListModel<String> model, String newValues[]){
		model.addAll(newValues);
	}

	private void initScreen(){
		setBorder(BorderFactory.createEtchedBorder());
		setLayout(new GridBagLayout());
		sourceLabel = new JLabel(DEFAULT_SOURCE_CHOICE_LABEL);
		sourceListModel = new SortedListModel<String>();
		sourceList = new JList<String>(sourceListModel);
		add(sourceLabel,
				new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER, 
						GridBagConstraints.NONE, EMPTY_INSETS,0,0));
		add(new JScrollPane(sourceList),
				new GridBagConstraints(0,1,1,5,0.5,1,GridBagConstraints.CENTER, 
						GridBagConstraints.BOTH, EMPTY_INSETS,0,0));
		
		addButton = new JButton(addIcon);
		add(addButton,
				new GridBagConstraints(1,2,1,2,0,.25,GridBagConstraints.CENTER, 
						GridBagConstraints.NONE, EMPTY_INSETS,0,0));
		addButton.addActionListener(new AddListener());
		
		// The insets here adds some space around the button, since it is larger than the ADD button, 
		// without the insets, the two scroll areas will touch the button.
		removeButton = new JButton(removeIcon);
		add(removeButton,
				new GridBagConstraints(1,4,1,2,0,.25,GridBagConstraints.CENTER, 
						GridBagConstraints.NONE, new Insets(0,5,0,5),0,0));
		removeButton.addActionListener(new RemoveListener());
		
		destLabel = new JLabel(DEFAULT_DEST_CHOICE_LABEL);
		destListModel = new SortedListModel<String>();
		destList = new JList<String>(destListModel);
		add(destLabel,
				new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.CENTER, 
						GridBagConstraints.NONE, EMPTY_INSETS,0,0));
		add(new JScrollPane(destList),
				new GridBagConstraints(2,1,1,5,0.5,1,GridBagConstraints.CENTER, 
						GridBagConstraints.BOTH, EMPTY_INSETS,0,0));
	}
	
	
	private class AddListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			List<String> selected = sourceList.getSelectedValuesList();
			addDestinationElements(selected);
			clearSourceSelected();
		}
	}
	private class RemoveListener implements ActionListener{
		public void actionPerformed(ActionEvent e) {
			List<String> selected = destList.getSelectedValuesList();
			addSourceElements(selected);
			clearDestinationSelected();
		}
	}
}
