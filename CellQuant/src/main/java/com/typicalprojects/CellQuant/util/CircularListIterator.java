package com.typicalprojects.CellQuant.util;

import java.util.List;

public class CircularListIterator<K> {
	
	private int startPosition;
	private boolean forwardDirection;
	private int currPosition;
	private List<K> list;
	
	public CircularListIterator(List<K> list, int startPosition, boolean forwardDirection) {
		this.list = list;
		this.startPosition = startPosition;
		this.currPosition = startPosition;
		this.forwardDirection = forwardDirection;
	}
	
	public K next() {
		
		if (currPosition == -1)
			return null;
		
		K nextElement = list.get(currPosition);
		
		if (forwardDirection) {
			currPosition++;
			if (currPosition >= list.size()) {
				currPosition = 0;
			}
			if (currPosition == startPosition) {
				currPosition = -1;
			}
		} else {
			currPosition--;
			if (currPosition < 0) {
				currPosition = list.size() - 1;
			}
			if (currPosition == startPosition) {
				currPosition = -1;
			}
		}
		
		return nextElement;
		
	}
	
	public boolean hasNext() {
		return this.currPosition != -1;
	}
	
	
	
}
