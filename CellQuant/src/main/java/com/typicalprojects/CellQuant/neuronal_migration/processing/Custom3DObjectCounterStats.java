package com.typicalprojects.CellQuant.neuronal_migration.processing;

public class Custom3DObjectCounterStats {
	
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
	
	public Custom3DObjectCounterStats() {
		
	}
	
	public Custom3DObjectCounterStats copyOf() {
		try {
			return (Custom3DObjectCounterStats) this.clone();
		} catch (CloneNotSupportedException e) {
			return null;
			// Will not occur
		}
	}
	
}
