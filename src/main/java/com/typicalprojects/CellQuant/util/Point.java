package com.typicalprojects.CellQuant.util;


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
