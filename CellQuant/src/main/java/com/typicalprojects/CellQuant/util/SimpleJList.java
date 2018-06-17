package com.typicalprojects.CellQuant.util;

import java.util.Enumeration;

import javax.swing.DefaultListModel;
import javax.swing.JList;


public class SimpleJList<K> {
	
	private final DefaultListModel<K> listModel;
	private final JList<K> jList;
	
	public SimpleJList() {
		this.listModel = new DefaultListModel<K>();
		this.jList = new JList<K>();
		this.jList.setModel(this.listModel);
	}
	
	public SimpleJList(SimpleJList<K> copy) {
		this();
		for (int i = 0; i < copy.listModel.size(); i++) {
			this.addItem(copy.listModel.getElementAt(i));
		}
	}
	
	public Enumeration<K> getElements() {
		return this.listModel.elements();
	}
	
	public JList<K> getGUIComponent() {
		return this.jList;
	}
	
	public void clear() {
		clearSelection();
		this.listModel.clear();
	}
	
	public K getSelected() {
		
		return this.jList.getSelectedValue();
		
	}
	
	public int getSelectedIndex() {
		return this.jList.getSelectedIndex();
	}
	
	public void clearSelection() {
		
		this.jList.clearSelection();
		
	}
	
	public void addItem(K item) {
		
		this.listModel.addElement(item);
		
	}
	
	public void setSelection(int index) {
		this.jList.setSelectedIndex(index);
	}
	
	public int getSize() {
		return this.listModel.size();
	}
	
	public boolean isEmpty() {
		return this.listModel.isEmpty();
	}
	
	public void removeItem(K item) {
		
		boolean exists = true;
		while (exists) {
			exists = this.listModel.removeElement(item);
		}
	}
	
}
