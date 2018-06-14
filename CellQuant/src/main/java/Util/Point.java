package Util;


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
	
	public int hashCode() {
	    return x * 31 + y;
	}
	
	public String toString() {
		return "Pt @ (" + x + ", " + y + ")";
	}
	
}
