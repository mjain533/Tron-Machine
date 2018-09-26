package com.typicalprojects.TronMachine.util;

import java.awt.Polygon;
import java.util.HashMap;
import java.util.Map;

import ij.gui.PolygonRoi;

public class PolarizedPolygonROI {
	
	private final String name;
	private final PolygonRoi polyROI;
	private final PolygonRoi oneSide;
	private Boolean oneSideIsPositive = null;
	
	public PolarizedPolygonROI(String name, PolygonRoi polyROI, PolygonRoi oneSide) {
		this.name = name;
		this.polyROI = polyROI;
		this.oneSide = oneSide;
	}
	
	public String getName() {
		return this.name;
	}
	
	public java.awt.Point[] getContainedHalfPoints() {
		return this.oneSide.getContainedPoints();
	}
	
	public PolygonRoi get() {
		return this.polyROI;
	}
	
	public boolean positiveRegionIsSet() {
		return this.oneSideIsPositive != null;
	}
	
	public void setPositiveRegion(int x, int y) {
		if (oneSide.contains(x, y)) {
			oneSideIsPositive = true;
		} else {
			oneSideIsPositive = false;
		}
	}
	
	public Map<Integer, Integer> getPointsOnLine() {
		Polygon p = polyROI.getPolygon();
		Map<Integer, Integer> mapping = new HashMap<Integer, Integer>();
		for (int polygonPt = 0; polygonPt < p.npoints; polygonPt ++) {
			mapping.put(p.xpoints[polygonPt], p.ypoints[polygonPt]);
		}
		return mapping;
	}
	
	public boolean hasPositiveSideBeenSelected() {
		return oneSideIsPositive != null;
	}
	
	public boolean isPositive(int x, int y) {
		
		return oneSideIsPositive == this.oneSide.contains(x, y);
		
	}
	
}
