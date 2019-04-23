/*
 * (C) Copyright 2018 Justin Carrington.
 *
 *  This file is part of TronMachine.

 *  TronMachine is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TronMachine is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.

 *  You should have received a copy of the GNU General Public License
 *  along with TronMachine.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Justin Carrington
 *     Russell Taylor
 *     Kendra Taylor
 *     Erik Dent
 *     
 */
package com.typicalprojects.TronMachine.util;

import java.util.List;

/**
 * This is a PSEUDO-iterator, it does NOT follow the implementation conventions of a typical iterator and is
 * merely used as a utility for circling around image corners and points during ROI creation in the TRON
 * machine.
 * 
 * @author Justin Carrington
 *
 * @param <K> The object type which will be iterated on.
 */
public class CircularListIterator<K> {
	
	private int startPosition;
	private boolean forwardDirection;
	private int currPosition;
	private List<K> list;
	
	/**
	 * Constructs a new iterator from the given position in a specific direction.
	 * 
	 * @param list				The list to iterate over, which is NEVER modified by this object.
	 * @param startPosition		The start position in the list. Must be a valid position.
	 * @param forwardDirection	true if the iterator should move forward (go up indices and then wrap around
	 *  to the 0th index, false for reverse)
	 */
	public CircularListIterator(List<K> list, int startPosition, boolean forwardDirection) {
		this.list = list;
		this.startPosition = startPosition;
		this.currPosition = startPosition;
		this.forwardDirection = forwardDirection;
	}
	
	/**
	 * @return next item in the list. Returns null if there is no next item.
	 */
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
	
	/**
	 * @return true if there are more items available (i.e. the iterator has not covered every item in the
	 * list with the {@link #next()} call.
	 */
	public boolean hasNext() {
		return this.currPosition != -1;
	}
	
}
