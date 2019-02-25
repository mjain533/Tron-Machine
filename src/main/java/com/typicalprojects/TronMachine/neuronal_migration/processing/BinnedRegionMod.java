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

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.processing.Analyzer.Calculation;
import com.typicalprojects.TronMachine.util.CircularListIterator;
import com.typicalprojects.TronMachine.util.Logger;
import com.typicalprojects.TronMachine.util.Point;
import com.typicalprojects.TronMachine.util.PolarizedPolygonROI;

import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class BinnedRegionMod {

	private List<Bin> bins = new ArrayList<Bin>();
	private List<BinLine> binLines = new ArrayList<BinLine>();
	private int[] dimensions;
	private PolarizedPolygonROI firstLine;
	private PolarizedPolygonROI lastLine;
	public Map<Integer, Map<Point, Double>> points = new HashMap<Integer, Map<Point, Double>>();

	/**
	 * 
	 * 
	 * @param polygon1
	 * @param polygon2
	 * @param numBinds
	 * @param binPrecision
	 * @param dimensions Array length 2, where index 0 = width, index 1 = height
	 * @throws MalformedBinException 
	 */
	public BinnedRegionMod(PolarizedPolygonROI polygon1ROI, PolarizedPolygonROI polygon2ROI, int numBins, double binPrecision, int[] dimensions, Logger logger) throws MalformedBinException {



		this.dimensions = dimensions;
		try {
			firstLine = polygon1ROI;
			lastLine = polygon2ROI;
			Polygon polygon1 = polygon1ROI.get().getPolygon();
			Polygon polygon2 = polygon2ROI.get().getPolygon();
			
			PolygonRoi binRegion = getPolygonFromLines(polygon1ROI.get(), polygon2ROI.get());
			logger.setCurrentTask("Computing Bin Region...");
			java.awt.Point[] polygonBoundedPoints = binRegion.getContainedPoints();
			int counter = 1;
			int maxProgress = polygonBoundedPoints.length / 1000;
			logger.setCurrentTaskProgress(1, maxProgress);
			for (java.awt.Point polygonBoundedPoint : polygonBoundedPoints) {
				counter++;
				if (counter % 1000 == 0)
					logger.setCurrentTaskProgress(counter / 1000, maxProgress);
				int x = polygonBoundedPoint.x;
				int y = polygonBoundedPoint.y;
				// For this x / y pixel, identify distance nearest point on either line.
				int bestIndex = -1;
				double bestDistance = -1;
				for (int k = 0; k < polygon1.npoints; k++) {
					if (bestIndex == -1) {
						bestIndex = k;
						bestDistance = Analyzer.calculate(Calculation.DIST, polygon1.xpoints[k], x, polygon1.ypoints[k], y);
					} else {
						double distance = Analyzer.calculate(Calculation.DIST, polygon1.xpoints[k], x, polygon1.ypoints[k], y);
						if (distance < bestDistance) {
							bestIndex = k;
							bestDistance = distance;
						}
					}
				}

				int bestIndex2 = -1;
				double bestDistance2 = -1;
				for (int k = 0; k < polygon2.npoints; k++) {
					if (bestIndex2 == -1) {
						bestIndex2 = k;
						bestDistance2 = Analyzer.calculate(Calculation.DIST, polygon2.xpoints[k], x, polygon2.ypoints[k], y);
					} else {
						double distance = Analyzer.calculate(Calculation.DIST, polygon2.xpoints[k], x, polygon2.ypoints[k], y);
						if (distance < bestDistance2) {
							bestIndex2 = k;
							bestDistance2 = distance;
						}
					}
				}

								
				int binLimitFromOne;
				int binLimitFromTwo;
				if ((numBins & 1) == 0 ) {
					binLimitFromOne = (numBins / 2);
					binLimitFromTwo = binLimitFromOne - 1;
				} else {
					binLimitFromOne = (int) Math.floor((numBins / 2.0));
					binLimitFromTwo = binLimitFromOne;
				}

				for (int i = 1; i <= binLimitFromOne; i++) {
					double diff = Math.abs(bestDistance - (bestDistance2 * (i / (double) (numBins - i))));
					Map<Point, Double> pts = this.points.get(i);
					if (pts == null) {
						pts = new HashMap<Point, Double>();
						this.points.put(i, pts);
					}
					if (diff <= 1) {
						pts.put(new Point(x, y, null), diff);
					}
				}
				for (int i = 1; i <= binLimitFromTwo; i++) {
					double diff = Math.abs(bestDistance2 - (bestDistance * (i / (double) (numBins - i))));
					Map<Point, Double> pts = this.points.get(numBins - i);
					if (pts == null) {
						pts = new HashMap<Point, Double>();
						this.points.put(numBins - i, pts);
					}

					if (diff <= 1) {
						pts.put(new Point(x, y, null), diff);
					}

				}
			}
			
			logger.setCurrentTaskComplete();

			
			Map<Integer, List<Point>> cleanedPoints = _cleanPointData();
			for (int i = 1; i < numBins; i++) {
				List<Point> linePoints = cleanedPoints.get(i);
				
				if (linePoints == null) {
					throw new MalformedBinException();

				}
				
				this.binLines.add(new BinLine(linePoints, dimensions, i));
			}
			this.binLines.add(0, new BinLine(polygon1, 0));
			this.binLines.add(new BinLine(polygon2, numBins));
			
			this.bins = calculateBins(this.binLines);
			
			logger.setCurrentTaskComplete();


		} catch (Exception e) {
			e.printStackTrace();
			throw new MalformedBinException();
		}


	}
	
	private PolygonRoi getPolygonFromLines(PolygonRoi line1, PolygonRoi line2) {
		
		List<Integer> xPts = new ArrayList<Integer>();
		List<Integer> yPts = new ArrayList<Integer>();
		List<Point> perimPoints = _getPerimeterPoints();
		Polygon polygon1 = line1.getPolygon();
		Polygon polygon2 = line2.getPolygon();
		Point end = new Point(polygon1.xpoints[0], polygon1.ypoints[0], null);
		Point start = new Point(polygon1.xpoints[polygon1.npoints - 1], polygon1.ypoints[polygon1.npoints - 1], null);
		Point pg2end1 = new Point(polygon2.xpoints[0], polygon2.ypoints[0], null);
		Point pg2end2 = new Point(polygon2.xpoints[polygon2.npoints - 1], polygon2.ypoints[polygon2.npoints - 1], null);
		
		xPts.addAll(Arrays.stream(polygon1.xpoints).boxed().collect(Collectors.toList()));
		yPts.addAll(Arrays.stream(polygon1.ypoints).boxed().collect(Collectors.toList()));
		
		List<Integer> tempxPts = new ArrayList<Integer>();
		List<Integer> tempyPts = new ArrayList<Integer>();
		CircularListIterator<Point> circularItr = new CircularListIterator<Point>(perimPoints, perimPoints.indexOf(start), true);
		circularItr.next();
		
		Point pointToContinueOffOf = null;
		boolean switchDirection = false;
		while (circularItr.hasNext()) {
			Point perimPoint = circularItr.next();
			if (perimPoint.equals(end)) {
				tempxPts.clear();
				tempyPts.clear();
				switchDirection = true;
				break;
			} else if (perimPoint.equals(pg2end1)) {

				xPts.addAll(tempxPts);
				yPts.addAll(tempyPts);
				tempxPts.clear();
				tempyPts.clear();

				xPts.addAll(Arrays.stream(polygon2.xpoints).boxed().collect(Collectors.toList()));
				yPts.addAll(Arrays.stream(polygon2.ypoints).boxed().collect(Collectors.toList()));
				
				pointToContinueOffOf = new Point(polygon2.xpoints[polygon2.npoints - 1], polygon2.ypoints[polygon2.npoints - 1], null);
				break;
			} else if (perimPoint.equals(pg2end2)) {

				xPts.addAll(tempxPts);
				yPts.addAll(tempyPts);
				tempxPts.clear();
				tempyPts.clear();
				for (int i = polygon2.npoints - 1; i >= 0; i--) {

					xPts.add(polygon2.xpoints[i]);
					yPts.add(polygon2.ypoints[i]);

				}
				pointToContinueOffOf = new Point(polygon2.xpoints[0], polygon2.ypoints[0], null);
				break;

			} else {
				if ((perimPoint.x == 0 && (perimPoint.y == 0 || perimPoint.y == dimensions[1]-1)) ||
						(perimPoint.x == dimensions[0]-1 && (perimPoint.y == 0 || perimPoint.y == dimensions[1] - 1))) {
					tempxPts.add(perimPoint.x);
					tempyPts.add(perimPoint.y);
				}

			}
		}
		if (switchDirection) {
			circularItr = new CircularListIterator<Point>(perimPoints, perimPoints.indexOf(start), false);
			circularItr.next();
			while (circularItr.hasNext()) {
				Point perimPoint = circularItr.next();
				if (perimPoint.equals(pg2end1)) {

					xPts.addAll(tempxPts);
					yPts.addAll(tempyPts);
					tempxPts.clear();
					tempyPts.clear();

					xPts.addAll(Arrays.stream(polygon2.xpoints).boxed().collect(Collectors.toList()));
					yPts.addAll(Arrays.stream(polygon2.ypoints).boxed().collect(Collectors.toList()));
					
					pointToContinueOffOf = new Point(polygon2.xpoints[polygon2.npoints - 1], polygon2.ypoints[polygon2.npoints - 1], null);
					break;
				} else if (perimPoint.equals(pg2end2)) {

					xPts.addAll(tempxPts);
					yPts.addAll(tempyPts);
					tempxPts.clear();
					tempyPts.clear();
					for (int i = polygon2.npoints - 1; i >= 0; i--) {

						xPts.add(polygon2.xpoints[i]);
						yPts.add(polygon2.ypoints[i]);

					}
					pointToContinueOffOf = new Point(polygon2.xpoints[0], polygon2.ypoints[0], null);
					break;

				} else {
					if ((perimPoint.x == 0 && (perimPoint.y == 0 || perimPoint.y == dimensions[1]-1)) ||
							(perimPoint.x == dimensions[0]-1 && (perimPoint.y == 0 || perimPoint.y == dimensions[1] - 1))) {
						tempxPts.add(perimPoint.x);
						tempyPts.add(perimPoint.y);
					}

				}
			}
		}

		circularItr = new CircularListIterator<Point>(perimPoints, perimPoints.indexOf(pointToContinueOffOf), !switchDirection);
		circularItr.next();
		while (circularItr.hasNext()) {
			Point perimPoint = circularItr.next();
			if (perimPoint.equals(end)) {
				xPts.addAll(tempxPts);
				yPts.addAll(tempyPts);
				xPts.add(end.x);
				yPts.add(end.y);
				break;
			} else if ((perimPoint.x == 0 && (perimPoint.y == 0 || perimPoint.y == dimensions[1]-1)) ||
					(perimPoint.x == dimensions[0]-1 && (perimPoint.y == 0 || perimPoint.y == dimensions[1] - 1))) {
				tempxPts.add(perimPoint.x);
				tempyPts.add(perimPoint.y);
			}

		}
		
		int[] newXPointsArray =  new int[xPts.size()];
		int[] newYPointsArray =  new int[yPts.size()];
		for (int i = 0; i < xPts.size(); i++) {
			newXPointsArray[i] = xPts.get(i);
			newYPointsArray[i] = yPts.get(i);
		}
		xPts = null;
		yPts = null;

		return new PolygonRoi(newXPointsArray, newYPointsArray, newYPointsArray.length, PolygonRoi.POLYGON);


	}
	
	public Map<Integer, List<Point>> _cleanPointData() {
		
		Map<Integer, List<Point>> lines = new HashMap<Integer, List<Point>>();
		
		for (Entry<Integer, Map<Point, Double>> en : this.points.entrySet()) {
			
			List<Point> newOrderedPoints = new ArrayList<Point>();
			Map<Point, Double> map = en.getValue();
			// First find point closest to a side

			Point ptNearSide = null;
			int closestDist = -1;
			for (Point p : map.keySet()) {
				int dist = _distanceToSide(p);
				if (dist == 0) {
					ptNearSide = p;
					closestDist = 0;
					break;
				} else if (ptNearSide == null) {
					closestDist = dist;
					ptNearSide = p;
				} else if (dist < closestDist) {
					closestDist = dist;
					ptNearSide = p;
				}
			}
			
			map.remove(ptNearSide);
			
			Point pointAtSide = getStretchedPoint(ptNearSide, false);
			if (pointAtSide != null) { // ptNearSide is not AT the side. Get that point and add
				newOrderedPoints.add(pointAtSide);
			}
			newOrderedPoints.add(ptNearSide);
			Point currPoint = ptNearSide;
			while (!map.isEmpty()) {
				Iterator<Entry<Point, Double>> itr = map.entrySet().iterator();
				double pixDistance = -1;
				Point potentiallyNextPoint = null;
				while (itr.hasNext()) {
					Entry<Point, Double> entry = itr.next();
					if (potentiallyNextPoint == null) {
						potentiallyNextPoint = entry.getKey();
						pixDistance = Analyzer.calculate(Calculation.DIST, potentiallyNextPoint.x, currPoint.x, potentiallyNextPoint.y, currPoint.y);
					} else {
						Point comparatorP = entry.getKey();
						double potNewPixDistance = Analyzer.calculate(Calculation.DIST, comparatorP.x, currPoint.x, comparatorP.y, currPoint.y);
						if (potNewPixDistance < 3.0) {
							itr.remove();
						} else if (potNewPixDistance < pixDistance) {
							pixDistance = potNewPixDistance;
							potentiallyNextPoint = comparatorP;
						}
					}
				}
				
				itr = null;
				map.remove(potentiallyNextPoint);
				newOrderedPoints.add(potentiallyNextPoint);
				currPoint = potentiallyNextPoint;
			}
			
			Point lastPoint = newOrderedPoints.get(newOrderedPoints.size() - 1);
			Point lastPointAtSide = getStretchedPoint(lastPoint, false);
			if (lastPointAtSide != null)
				newOrderedPoints.add(lastPointAtSide);
			lines.put(en.getKey(), newOrderedPoints);
		}
		
		return lines;
		
	}
	
	public int _distanceToSide(Point p) {
		int distTop = p.y;
		int distBottom = dimensions[1] - p.y - 1;
		int distLeft = p.x;
		int distRight = dimensions[0] - p.x - 1;

		int min = Math.min(Math.min(distTop, distBottom), Math.min(distLeft, distRight));

		return min;
	}
	

	public void drawBinLines(ImageProcessor ip, boolean includeText) {
		double size = Math.min(dimensions[0], dimensions[1] );

		ip.setFont(new Font("Arial", Font.BOLD, (int) (size / 50)));

		
		int middleY = 0;
		int middleX = 0;
		
		if (includeText) {
			PolygonRoi roi = firstLine.get();
			ip.setColor(Color.GREEN);
			roi.setFillColor(Color.GREEN);
			roi.setStrokeColor(Color.GREEN);
			roi.setStrokeWidth(size / 300.0);		
			ip.drawOverlay(new Overlay(roi));
			middleY = Math.max(roi.getPolygon().ypoints[(roi.getPolygon().npoints / 2)] + 3, 10);
			middleY= Math.min(middleY, dimensions[1] - 4);
			middleX = roi.getPolygon().xpoints[(roi.getPolygon().npoints / 2)];
			ip.setColor(Color.RED);
			ip.drawString(firstLine.getName(), middleX, middleY, Color.BLACK);
			
			roi = lastLine.get();
			roi.setFillColor(Color.GREEN);
			roi.setStrokeColor(Color.GREEN);

			roi.setStrokeWidth(size / 300.0);
			ip.drawOverlay(new Overlay(roi));
			middleY = Math.max(roi.getPolygon().ypoints[(roi.getPolygon().npoints / 2)] + 8, 10);
			middleY= Math.min(middleY, dimensions[1] - 4);
			middleX = roi.getPolygon().xpoints[(roi.getPolygon().npoints / 2)];
			ip.setColor(Color.RED);
			ip.drawString(lastLine.getName(), middleX, middleY, Color.BLACK);	
		} else {
			PolygonRoi roi = firstLine.get();
			ip.setColor(Color.WHITE);
			roi.setFillColor(Color.WHITE);
			roi.setStrokeColor(Color.WHITE);
			roi.setStrokeWidth(size / 300.0);		
			ip.drawOverlay(new Overlay(roi));
			roi = lastLine.get();
			roi.setFillColor(Color.WHITE);
			roi.setStrokeColor(Color.WHITE);
			roi.setStrokeWidth(size / 300.0);	
			ip.drawOverlay(new Overlay(roi));

		}

		

			
		
		///
		if (includeText) {
			ip.setColor(Color.GREEN);
		} else {
			ip.setColor(Color.WHITE);
		}
		for (int i = 1 ; i < binLines.size() - 1; i++) {
			for (int j = 0; j < this.binLines.get(i).xPts.length; j++) {
				ip.drawPixel(this.binLines.get(i).xPts[j], this.binLines.get(i).yPts[j]);
			}

		}
		
		if (GUI.settings.drawBinLabels && includeText) {
			ip.setColor(Color.RED);
			ip.setFont(new Font("Arial", Font.BOLD, (int) (size / 70)));
			for (int i = 1; i < this.binLines.size(); i++) {
				BinLine binline1 = this.binLines.get(i - 1);
				BinLine binline2 = this.binLines.get(i);
				middleY = (int) ((Math.max(binline1.yPts[binline1.yPts.length / 2], 10) +  Math.max(binline2.yPts[binline2.yPts.length / 2], 10)) / 2.0) + 5;
				middleX = (int) ((Math.max(binline1.xPts[binline1.xPts.length / 2], 10) +  Math.max(binline2.xPts[binline2.xPts.length / 2], 10)) / 2.0);
				ip.drawString("Bin " + i, middleX, middleY, Color.BLACK);

			}
		}
		

	}

	public class BinLine {

		private int[] xPts;
		private int[] yPts;

		public BinLine(List<Point> points, int[] imageDimensions, int binLineNum) {

			int[] xpts = new int[points.size()];
			int[] ypts = new int[points.size()];
			
			for (int i = 0; i < points.size(); i++) {
				xpts[i] = points.get(i).x;
				ypts[i] = points.get(i).y;
			}

			PolygonRoi pgr = new PolygonRoi(xpts, ypts, xpts.length, Roi.POLYLINE) ;
			pgr.fitSplineForStraightening();			
			Polygon polygon = pgr.getPolygon();
			int[] newxs = new int[polygon.npoints + 2];
			System.arraycopy(polygon.xpoints, 0, newxs, 1, polygon.npoints);
			int[] newys = new int[polygon.npoints + 2];
			System.arraycopy(polygon.ypoints, 0, newys, 1, polygon.npoints);
			newxs[0] = points.get(0).x;
			newxs[newxs.length - 1] = points.get(points.size() - 1).x;
			newys[0] = points.get(0).y;
			newys[newys.length - 1] = points.get(points.size() - 1).y;
			xPts = newxs;
			yPts = newys;
			
			List<int[]> newListsAfterX = makeContinuous(this.xPts, this.yPts);
			this.xPts = newListsAfterX.get(0);
			this.yPts = newListsAfterX.get(1);
			List<int[]> newListsAfterY = makeContinuous(this.yPts, this.xPts);
			this.yPts = newListsAfterY.get(0);
			this.xPts = newListsAfterY.get(1);

		}

		private BinLine(Polygon pg, int binNum) {
			
			Point beginStretch = getStretchedPoint(new Point(pg.xpoints[0], pg.ypoints[0], null), false);
			Point endStretch = getStretchedPoint(new Point(pg.xpoints[pg.npoints - 1], pg.ypoints[pg.npoints - 1], null), false);

			if (beginStretch != null) {
				if (endStretch != null) {
					int[] newxs = new int[pg.npoints + 2];
					System.arraycopy(pg.xpoints, 0, newxs, 1, pg.npoints);
					int[] newys = new int[pg.npoints + 2];
					System.arraycopy(pg.ypoints, 0, newys, 1, pg.npoints);
					newxs[0] = beginStretch.x;
					newxs[newxs.length - 1] = endStretch.x;
					newys[0] = beginStretch.y;
					newys[newys.length - 1] = endStretch.y;
					this.xPts = newxs;
					this.yPts = newys;
				} else {
					int[] newxs = new int[pg.npoints + 1];
					System.arraycopy(pg.xpoints, 0, newxs, 1, pg.npoints);
					int[] newys = new int[pg.npoints + 1];
					System.arraycopy(pg.ypoints, 0, newys, 1, pg.npoints);
					newxs[0] = beginStretch.x;
					newys[0] = beginStretch.y;
					this.xPts = newxs;
					this.yPts = newys;
				}
				
			} else if (endStretch != null) {
				int[] newxs = new int[pg.npoints + 1];
				System.arraycopy(pg.xpoints, 0, newxs, 0, pg.npoints);
				int[] newys = new int[pg.npoints + 1];
				System.arraycopy(pg.ypoints, 0, newys, 0, pg.npoints);
				newxs[newxs.length - 1] = endStretch.x;
				newys[newys.length - 1] = endStretch.y;
				this.xPts = newxs;
				this.yPts = newys;
			} else {
				this.xPts = pg.xpoints;
				this.yPts = pg.ypoints;
			}
			
			List<int[]> newListsAfterX = makeContinuous(this.xPts, this.yPts);
			this.xPts = newListsAfterX.get(0);
			this.yPts = newListsAfterX.get(1);
			List<int[]> newListsAfterY = makeContinuous(this.yPts, this.xPts);
			this.yPts = newListsAfterY.get(0);
			this.xPts = newListsAfterY.get(1);


		}

	}

	public class MalformedBinException extends Exception {

		private static final long serialVersionUID = 1L;	

	}

	public int getEnclosingBin(Point p) {


		for (Bin bin : this.bins) {
			if (bin.containsPoint(p))
				return bin.binNum;
		}

		return -2;

	}

	public int getEnclosingBin(int x, int y) {


		for (Bin bin : this.bins) {
			if (bin.containsPoint(x, y))
				return bin.binNum;
		}

		return -2;

	}
	
	public boolean binLinesOverlap() {
		return binLinesCollide(this.binLines);
	}

	public class Bin {

		private Polygon container;
		private int binNum;

		public Bin(int binNum, Polygon container) {
			this.binNum = binNum;
			this.container = container;
		}

		public boolean containsPoint(Point p) {
			return container.contains(p.x, p.y);
		}

		public boolean containsPoint(int x, int y) {
			return container.contains(x, y);
		}

		public int getBinNum() {
			return binNum;
		}

	}

	private List<Bin> calculateBins(List<BinLine> binLines) {

		List<Point> perimPoints = _getPerimeterPoints();
		List<Bin> bins = new ArrayList<Bin>();
		Point previousPt1 = null;
		Point previousPt2 = null;
		BinLine previousBinLine = null;
		int binNum = -1;

		for (BinLine line : binLines) {
			
			List<Integer> newXPoints = new ArrayList<Integer>();
			List<Integer> newYPoints = new ArrayList<Integer>();
			Point pEnd = new Point(line.xPts[0], line.yPts[0], null);
			Point pStart = new Point(line.xPts[line.xPts.length - 1], line.yPts[line.yPts.length - 1], null);

			int startIndex = perimPoints.indexOf(pStart);
			CircularListIterator<Point> circularItr = new CircularListIterator<Point>(perimPoints, startIndex, true);
			circularItr.next();

			if (binNum == -1) {
				BinLine oppLine = binLines.get(binLines.size() - 1);
				previousPt1 = getStretchedPoint(new Point(oppLine.xPts[0], oppLine.yPts[0], null), true);
				previousPt2 = getStretchedPoint(new Point(oppLine.xPts[oppLine.xPts.length - 1], oppLine.yPts[oppLine.yPts.length - 1], null), true);

				boolean switchDirection = false;
				while (circularItr.hasNext()) {
					Point perimPoint = circularItr.next();

					if (perimPoint.equals(pEnd)) {
						newXPoints.add(pEnd.x);
						newYPoints.add(pEnd.y);
						newXPoints.addAll(0, Arrays.stream(line.xPts).boxed().collect(Collectors.toList()));
						newYPoints.addAll(0, Arrays.stream(line.yPts).boxed().collect(Collectors.toList()));

						break;
					} else if (perimPoint.equals(previousPt1) || perimPoint.equals(previousPt2)) {
						newXPoints.clear();
						newYPoints.clear();
						switchDirection = true;
						break;
					} else {
						newXPoints.add(perimPoint.x);
						newYPoints.add(perimPoint.y);
					}
				}
				if (switchDirection) {
					circularItr = new CircularListIterator<Point>(perimPoints, startIndex, false);
					circularItr.next();
					while (circularItr.hasNext()) {
						Point perimPoint = circularItr.next();
						if (perimPoint.equals(pEnd)) {
							newXPoints.add(pEnd.x);
							newYPoints.add(pEnd.y);
							newXPoints.addAll(0, Arrays.stream(line.xPts).boxed().collect(Collectors.toList()));
							newYPoints.addAll(0, Arrays.stream(line.yPts).boxed().collect(Collectors.toList()));
							break;
						}
						newXPoints.add(perimPoint.x);
						newYPoints.add(perimPoint.y);
					}
				}

			} else {
				Point pointToContinueOffOf = null;
				boolean switchDirection = false;
				while (circularItr.hasNext()) {
					Point perimPoint = circularItr.next();
					if (perimPoint.equals(pEnd)) {
						newXPoints.clear();
						newYPoints.clear();
						switchDirection = true;
						break;
					} else if (perimPoint.equals(previousPt1)) {

						newXPoints.addAll(0, Arrays.stream(line.xPts).boxed().collect(Collectors.toList()));
						newYPoints.addAll(0, Arrays.stream(line.yPts).boxed().collect(Collectors.toList()));

						newXPoints.addAll(Arrays.stream(previousBinLine.xPts).boxed().collect(Collectors.toList()));
						newYPoints.addAll(Arrays.stream(previousBinLine.yPts).boxed().collect(Collectors.toList()));
						pointToContinueOffOf = previousPt2;
						break;
					} else if (perimPoint.equals(previousPt2)) {

						newXPoints.addAll(0, Arrays.stream(line.xPts).boxed().collect(Collectors.toList()));
						newYPoints.addAll(0, Arrays.stream(line.yPts).boxed().collect(Collectors.toList()));
						for (int i = previousBinLine.xPts.length - 1; i >= 0; i--) {

							newXPoints.add(previousBinLine.xPts[i]);
							newYPoints.add(previousBinLine.yPts[i]);
						}
						pointToContinueOffOf = previousPt1;
						break;

					} else {
						newXPoints.add(perimPoint.x);
						newYPoints.add(perimPoint.y);
					}
				}
				if (switchDirection) {
					circularItr = new CircularListIterator<Point>(perimPoints, startIndex, false);
					circularItr.next();
					while (circularItr.hasNext()) {
						Point perimPoint = circularItr.next();
						if (perimPoint.equals(previousPt1)) {
							newXPoints.addAll(0, Arrays.stream(line.xPts).boxed().collect(Collectors.toList()));
							newYPoints.addAll(0, Arrays.stream(line.yPts).boxed().collect(Collectors.toList()));
							newXPoints.addAll(Arrays.stream(previousBinLine.xPts).boxed().collect(Collectors.toList()));
							newYPoints.addAll(Arrays.stream(previousBinLine.yPts).boxed().collect(Collectors.toList()));

							pointToContinueOffOf = previousPt2;
							break;
						} else if (perimPoint.equals(previousPt2)) {

							newXPoints.addAll(0, Arrays.stream(line.xPts).boxed().collect(Collectors.toList()));
							newYPoints.addAll(0, Arrays.stream(line.yPts).boxed().collect(Collectors.toList()));
							for (int i = previousBinLine.xPts.length - 1; i >= 0; i--) {
								newXPoints.add(previousBinLine.xPts[i]);
								newYPoints.add(previousBinLine.yPts[i]);
							}
							pointToContinueOffOf = previousPt1;
							break;

						} else {
							newXPoints.add(perimPoint.x);
							newYPoints.add(perimPoint.y);
						}
					}
				}

				circularItr = new CircularListIterator<Point>(perimPoints, perimPoints.indexOf(pointToContinueOffOf), !switchDirection);
				circularItr.next();
				while (circularItr.hasNext()) {
					Point perimPoint = circularItr.next();
					if (perimPoint.equals(pEnd)) {
						newXPoints.add(pEnd.x);
						newYPoints.add(pEnd.y);
						break;
					}
					newXPoints.add(perimPoint.x);
					newYPoints.add(perimPoint.y);
				}
			}
			previousPt1 = pEnd;
			previousPt2 = pStart;
			previousBinLine = line;

			int[] newXPointsArray =  new int[newXPoints.size()];
			int[] newYPointsArray =  new int[newYPoints.size()];
			for (int i = 0; i < newXPoints.size(); i++) {
				newXPointsArray[i] = newXPoints.get(i);
				newYPointsArray[i] = newYPoints.get(i);
			}

			bins.add(new Bin(binNum, new PolygonRoi(newXPointsArray, newYPointsArray, newYPointsArray.length, PolygonRoi.POLYGON).getPolygon()));
			if (binNum == binLines.size() - 1) {
				binNum = -1;
			} else if (binNum == -1){
				binNum = 1;
			} else {
				binNum++;
			}
		}

		return bins;

	}


	private List<Point> _getPerimeterPoints() {
		List<Point> perimPoints = new ArrayList<Point>();
		for (int i = 0; i < dimensions[0] -1; i++) {
			perimPoints.add(new Point(i, 0, null));
		}
		for (int i = 0; i < dimensions[1] -1; i++) {
			perimPoints.add(new Point(dimensions[0] - 1, i, null));
		}
		for (int i = dimensions[0] - 1; i > 0; i--) {
			perimPoints.add(new Point(i, dimensions[1] - 1, null));
		}
		for (int i = dimensions[1] - 1; i > 0; i--) {
			perimPoints.add(new Point(0, i, null));
		}
		return perimPoints;
	}


	private Point getStretchedPoint(Point p, boolean returnOriginal) {

		int distTop = p.y;
		int distBottom = dimensions[1] - p.y + 1;
		int distLeft = p.x;
		int distRight = dimensions[0] - p.x + 1;

		int min = Math.min(Math.min(distTop, distBottom), Math.min(distLeft, distRight));

		if (min != 0) {
			if (min == distTop) {
				return new Point(p.x, 0, false);
			} else if (min == distBottom) {
				return new Point(p.x, dimensions[1] - 1, false);
			} else if (min == distLeft) {
				return new Point(0, p.y, false);
			} else if (min == distRight) {
				return new Point(dimensions[0] - 1, p.y, false);
			}
		}
		if (returnOriginal)
			return p;
		else
			return null;
	}
	
	public static List<int[]> makeContinuous(int[] listToMakeContinuous, int[] list2) {
		List<Integer> listCont = new ArrayList<Integer>();
		List<Integer> newList2 = new ArrayList<Integer>();
		int previousListCont = -1;
		int previousList2 = -1;
		for (int i = 0; i <  listToMakeContinuous.length; i++) {
			Integer nextInt = listToMakeContinuous[i];
			if (previousListCont == -1 || Math.abs(nextInt - previousListCont) == 1) {
				previousListCont = nextInt;
				previousList2 = list2[i];
				listCont.add(nextInt);
				newList2.add(list2[i]);
				continue;
			}

			int numToAdd = Math.abs(nextInt - previousListCont) - 1;
			boolean add = nextInt > previousListCont;
			for (int n = 0; n < numToAdd; n++) {
				if (add)
					listCont.add(previousListCont + n + 1);
				else
					listCont.add(previousListCont - n - 1);

				newList2.add((int) ((previousList2 + list2[i]) / 2.0));
			}
			previousListCont = nextInt;
			previousList2 = list2[i];
			listCont.add(nextInt);
			newList2.add(list2[i]);
		}
		List<int[]> output = new ArrayList<int[]>();
		output.add(listCont.stream().mapToInt(i->i).toArray());
		output.add(newList2.stream().mapToInt(i->i).toArray());

		return output;
	}
	
	private static boolean binLinesCollide(List<BinLine> lines) {
		
		for (int i = 0; i < lines.size(); i++) {
			
			BinLine line = lines.get(i);
			
			for (int j = i + 1; j < lines.size(); j++) {
				BinLine other = lines.get(j);

				for (int ptCurr = 0; ptCurr < line.xPts.length; ptCurr++) {
					for (int ptOther = 0; ptOther < other.xPts.length; ptOther++) {
						if (line.xPts[ptCurr] == other.xPts[ptOther] && line.yPts[ptCurr] == other.yPts[ptOther]) {
							return true;
						}
					}
				}
			}
			
		}
		
		return false;
		
	}


}
