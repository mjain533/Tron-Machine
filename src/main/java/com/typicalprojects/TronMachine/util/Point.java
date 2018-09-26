
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


public class Point {
	
	public int x;
	public int y;
	public Boolean fromObjCounter;
	
	public Point(int x, int y, Boolean fromObjCounter) {
		this.x = x;
		this.y = y;
		this.fromObjCounter = fromObjCounter;
	}
	
	public boolean equals(Object other) {
		if (!(other instanceof Point))
			return false;
		
		Point otherP = (Point) other;
		
		return this.x == otherP.x && this.y == otherP.y;
	}
	
	@Override
	public int hashCode() {
	    int hash = 7;
	    hash = 71 * hash + this.x;
	    hash = 71 * hash + this.y;
	    return hash;
	}
	
	public String toString() {
		return "Pt @ (" + x + ", " + y + ")";
	}
	
	public boolean softEquals(Point point) {
		if (point.x == this.x || this.y == point.y)
			return true;
		else
			return false;
	}
	
}
