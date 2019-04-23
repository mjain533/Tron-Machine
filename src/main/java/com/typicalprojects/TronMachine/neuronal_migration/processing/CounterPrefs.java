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
package com.typicalprojects.TronMachine.neuronal_migration.processing;

public class CounterPrefs {
	
	public boolean calcVolume = false; 
	public boolean calcSurface = false; 
	public boolean calcNbObjVoxels = false; 
	public boolean calcNbSurfVoxels = false; 
	public boolean calcIntegratedDensity = false; 
	public boolean calcMeanGrayVal = false;
	public boolean calcStDevGrayVal = false; 
	public boolean calcMedianGrayVal = true; 
	public boolean calcMinGrayVal = false;
	public boolean calcMaxGrayVal = false;
	public boolean calcCentroid = false;
	public boolean calcMeanDistSurf = false;
	public boolean calcStDevDistToSurf = false;
	public boolean calcStDevMedianDistToSurf = false;
	public boolean calcCOM = true;
	public boolean calcBB = false;
	
	public CounterPrefs() {
		
	}
	
	public CounterPrefs copyOf() {
		try {
			return (CounterPrefs) this.clone();
		} catch (CloneNotSupportedException e) {
			return null;
			// Will not occur
		}
	}
	
}
