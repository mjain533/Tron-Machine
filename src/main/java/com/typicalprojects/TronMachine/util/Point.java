
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

import java.io.Serializable;

/**
 * Class representing a point. Similar to the Java point, except this point stores a field for recording
 * whether a point was the the 3D object counter. This is important for differentiating whether a point was
 * added from the TRON machine or whether it was created by the user.
 * 
 * @author Justin Carrington
 */
public class Point implements Serializable{
	
	private static final long serialVersionUID = -3376251132566371602L;
	
	/** X-coordinate of this point */
	public int x;
	/** Y-coordinate of this point */
	public int y;
	
	/** Z-coordinate of this point */
	public double z;
	
	public int[] additionalData = new int[2];
	
	/** true if from object counter, false otherwise. This field can be null, which will happen when not discussing
	 *  a point from the object selection phase of the TRON machine (i.e. a point on an ROI line).
	 **/
	public Boolean fromObjCounter;
	
	/**
	 * Construct new point
	 * 
	 * @param x X-coordinate of this Point.
	 * @param y Y-coordinate of this Point. 
	 * @param fromObjCounter true if from object counter, false if from user, and null if not involved in the
	 *  object selection process.
	 */
	public Point(int x, int y, Boolean fromObjCounter) {
		this.x = x;
		this.y = y;
		this.fromObjCounter = fromObjCounter;
	}
	
	public Point(int x, int y, double z, Boolean fromObjCounter) {
		this(x, y, fromObjCounter);
		this.z = z;
	}
	
	public Point(int x, int y, double z, int[] extraData, Boolean fromObjCounter) {
		this(x, y, z, fromObjCounter);
		this.additionalData = extraData;
	}
	
	/**
	 * @return true if the X and Y coordinates match (does not consider the object counter information)
	 */
	public boolean equals(Object other) {
		if (!(other instanceof Point))
			return false;
		
		Point otherP = (Point) other;
		
		return this.x == otherP.x && this.y == otherP.y;
	}
	
	/**
	 * Simple hash function (based on X and Y coordinates only)
	 * 
	 * @return the hash number of this point
	 */
	@Override
	public int hashCode() {
	    int hash = 7;
	    hash = 71 * hash + this.x;
	    hash = 71 * hash + this.y;
	    return hash;
	}
	
	/**
	 * Returns this point in the form: Pt @ (x, y)
	 */
	@Override
	public String toString() {
		return "Pt @ (" + x + ", " + y + ")";
	}

	
}
