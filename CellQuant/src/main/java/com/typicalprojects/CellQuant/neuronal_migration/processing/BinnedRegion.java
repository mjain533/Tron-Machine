package com.typicalprojects.CellQuant.neuronal_migration.processing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.typicalprojects.CellQuant.neuronal_migration.GUI;
import com.typicalprojects.CellQuant.neuronal_migration.processing.Analyzer.Calculation;
import com.typicalprojects.CellQuant.util.CircularListIterator;
import com.typicalprojects.CellQuant.util.Logger;
import com.typicalprojects.CellQuant.util.Point;
import com.typicalprojects.CellQuant.util.PolarizedPolygonROI;

import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class BinnedRegion {

	private List<BinLine> binLines = new ArrayList<BinLine>();
	private List<Bin> bins = new ArrayList<Bin>();
	private int[] dimensions;
	private PolarizedPolygonROI firstLine;
	private PolarizedPolygonROI lastLine;


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
	public BinnedRegion(PolarizedPolygonROI polygon1ROI, PolarizedPolygonROI polygon2ROI, int numBins, double binPrecision, int[] dimensions, Logger logger) throws MalformedBinException {
		this.dimensions = dimensions;
		try {
			firstLine = polygon1ROI;
			lastLine = polygon2ROI;

			Polygon polygon1 = polygon1ROI.get().getPolygon();
			Polygon polygon2 = polygon2ROI.get().getPolygon();
			binPrecision = binPrecision > 100 ? 100 : binPrecision;
			binPrecision = binPrecision < 1 ? 1 : binPrecision;

			int scaledBinPrecision = (int) (Math.max(polygon1.npoints, polygon2.npoints) * ((binPrecision) / 100.0));
			
			
			double indexDistance1 = polygon1.npoints / ((double) scaledBinPrecision);
			double indexDistance2 = polygon2.npoints / ((double) scaledBinPrecision);
			Point[][] points = new Point[scaledBinPrecision][];

			logger.setCurrentTask("Drawing bins...");

			for (int i = 1; i <= scaledBinPrecision; i++) {
				int binLocateLineIndex1 = Math.max(Math.min((int) (indexDistance1 * i), polygon1.npoints - 1), 0);
				int binLocateLineIndex2 = Math.max(Math.min((int) (indexDistance2 * i), polygon2.npoints - 1), 0);
				Line binLocateLine = new Line(polygon1.xpoints[binLocateLineIndex1], polygon1.ypoints[binLocateLineIndex1],
						polygon2.xpoints[binLocateLineIndex2], polygon2.ypoints[binLocateLineIndex2]);

				points[i - 1] = binLocateLine.getSpacedSectionsWithinLine(numBins - 1);

			}

			this.binLines.add(new BinLine(polygon1, 0));

			for (int j = 0; j < numBins - 1; j++) {
				List<Point> binPoints = new ArrayList<Point>();

				for (int k = 0; k < points.length; k++) {
					binPoints.add(points[k][j]);
				}

				this.binLines.add(new BinLine(binPoints, dimensions, j + 1));

			}
			this.binLines.add(new BinLine(polygon2, numBins));
			
			this.bins = calculateBins();
			logger.setCurrentTaskComplete();


		} catch (Exception e) {
			e.printStackTrace();
			throw new MalformedBinException();
		}


	}

	public void drawBinLines(ImageProcessor ip) {
		ip.setFont(new Font("Arial", Font.BOLD, 20));

		PolygonRoi roi = firstLine.get();
		roi.setFillColor(Color.GREEN);

		double size = Math.max(dimensions[0], dimensions[1] );

		roi.setStrokeWidth(size / 300.0);		
		ip.drawOverlay(new Overlay(roi));
		int middleY = Math.max(roi.getPolygon().ypoints[(roi.getPolygon().npoints / 2)] + 3, 10);
		middleY= Math.min(middleY, dimensions[1] - 4);
		int middleX = roi.getPolygon().xpoints[(roi.getPolygon().npoints / 2)];
		ip.setColor(Color.RED);
		ip.drawString(firstLine.getName(), middleX, middleY, Color.BLACK);
		
		roi = lastLine.get();
		roi.setFillColor(Color.GREEN);

		roi.setStrokeWidth(size / 300.0);
		ip.drawOverlay(new Overlay(roi));
		middleY = Math.max(roi.getPolygon().ypoints[(roi.getPolygon().npoints / 2)] + 8, 10);
		middleY= Math.min(middleY, dimensions[1] - 4);
		middleX = roi.getPolygon().xpoints[(roi.getPolygon().npoints / 2)];
		ip.setColor(Color.RED);
		ip.drawString(firstLine.getName(), middleX, middleY, Color.BLACK);
		
		ip.setColor(Color.GREEN);
		for (int i = 1 ; i < this.binLines.size() - 1; i++) {
			for (int j = 0; j < this.binLines.get(i).xPts.size(); j++) {
				ip.drawPixel(this.binLines.get(i).xPts.get(j), this.binLines.get(i).yPts.get(j));
			}

		}
		ip.setColor(Color.RED);
		ip.setFont(new Font("Arial", Font.BOLD, 13));
		
		if (GUI.settings.drawBinLabels) {
			for (int i = 1; i < this.binLines.size(); i++) {
				BinLine binline1 = this.binLines.get(i - 1);
				BinLine binline2 = this.binLines.get(i);
				middleY = (int) ((Math.max(binline1.yPts.get(binline1.yPts.size() / 2), 10) +  Math.max(binline2.yPts.get(binline2.yPts.size() / 2), 10)) / 2.0) + 5;
				middleX = (int) ((Math.max(binline1.xPts.get(binline1.xPts.size() / 2), 10) +  Math.max(binline2.xPts.get(binline2.xPts.size() / 2), 10)) / 2.0);
				ip.drawString("Bin " + i, middleX, middleY, Color.BLACK);

			}
		}
		

	}

	private class Line {

		private final Point point1;
		private final Point point2;
		private final double slope;

		private Line(Point point1, Point point2) {
			this.point1 = point1;
			this.point2 = point2;
			if (point1.x == point2.x) {
				this.slope = Double.POSITIVE_INFINITY;
			} else {
				this.slope = (point1.y - point2.y) / ((double) (point1.x - point2.x));

			}
		}

		private Line(int x1, int y1, int x2, int y2) {
			this(new Point(x1, y1, false), new Point(x2, y2, false));
		}

		public Point[] getSpacedSectionsWithinLine(int numberOfSectionLines) {

			// number of bins = 4 sections
			//
			Point[] points = new Point[numberOfSectionLines];

			double distance = Analyzer.calculate(Calculation.DIST, point1.x, point2.x, point1.y, point2.y) / ((double) numberOfSectionLines + 1.0);
			for (int i = 1; i <= numberOfSectionLines; i++) {
				Point[] sepPts = _getPointDownLine(distance * i, this, point1);
				points[i - 1] = isWithinBoundingBox(sepPts[0]) ? sepPts[0] : sepPts[1];
			}

			return points;

		}

		public int getY(double x) {

			if (slope == Double.POSITIVE_INFINITY) {
				return Integer.MAX_VALUE;
			}
			return (int) (this.slope * (x - point1.x) + point1.y);
		}

		public Point getProjectedPoint(int projectionDistance) {
			Point[] potentialPts =  _getPointDownLine(projectionDistance, this, this.point2);
			return !isWithinBoundingBox(potentialPts[0]) ? potentialPts[0] : potentialPts[1];
		}

		public boolean isWithinBoundingBox(Point p) {
			if (p.x >= Math.min(point1.x, point2.x) && p.x <= Math.max(point1.x, point2.x) &&
					p.y >= Math.min(point1.y, point2.y) && p.y <= Math.max(point1.y, point2.y)) {
				return true;
			} else {
				return false;
			}
		}

		private boolean hasInfiniteSlope() {
			return this.slope == Double.POSITIVE_INFINITY;
		}

	}

	public class BinLine {

		private List<Integer> xPts = new LinkedList<Integer>();
		private List<Integer> yPts = new LinkedList<Integer>();

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

			for (int i = 0; i < polygon.npoints; i++) {
				xPts.add(polygon.xpoints[i]);
				yPts.add(polygon.ypoints[i]);
			}
			Line endExtension = null;
			Line beginningExtension = null;

			if (xPts.size() >= 8) {
				endExtension = new Line(xPts.get(xPts.size() - 8), yPts.get(yPts.size() - 8), xPts.get(xPts.size() - 1), yPts.get(yPts.size() - 1));
				beginningExtension = new Line(xPts.get(25), yPts.get(25), xPts.get(0), yPts.get(0));
			} else {
				endExtension = new Line(xPts.get(xPts.size() - 2), yPts.get(yPts.size() - 2), xPts.get(xPts.size() - 1), yPts.get(yPts.size() - 1));
				beginningExtension = new Line(xPts.get(1), yPts.get(1), xPts.get(0), yPts.get(0));

			}


			boolean edgeFound = false;
			int counter = 1;
			while (!edgeFound) {
				Point pt = endExtension.getProjectedPoint(counter);
				if (pt.x < 0 || pt.x > (imageDimensions[0] - 1) || pt.y < 0 || pt.y > (imageDimensions[1] - 1)) {
					edgeFound = true;
				} else {
					xPts.add(pt.x);
					yPts.add(pt.y);
				}
				counter++;
			}

			edgeFound = false;
			counter = 1;
			while (!edgeFound) {
				Point pt = beginningExtension.getProjectedPoint(counter);
				if (pt.x < 0 || pt.x > (imageDimensions[0] - 1) || pt.y < 0 || pt.y > (imageDimensions[1] - 1)) {
					edgeFound = true;
				} else {
					xPts.add(0, pt.x);
					yPts.add(0, pt.y);
				}
				counter++;
			}

			List<List<Integer>> newListsAfterX = _makeContinuous(this.xPts, this.yPts);
			this.xPts = newListsAfterX.get(0);
			this.yPts = newListsAfterX.get(1);
			List<List<Integer>> newListsAfterY = _makeContinuous(this.yPts, this.xPts);
			this.yPts = newListsAfterY.get(0);
			this.xPts = newListsAfterY.get(1);

		}

		private BinLine(Polygon pg, int binNum) {
			this.xPts = Arrays.stream(pg.xpoints).boxed().collect(Collectors.toList());
			this.yPts = Arrays.stream(pg.ypoints).boxed().collect(Collectors.toList());

			List<List<Integer>> newListsAfterX = _makeContinuous(this.xPts, this.yPts);
			this.xPts = newListsAfterX.get(0);
			this.yPts = newListsAfterX.get(1);
			List<List<Integer>> newListsAfterY = _makeContinuous(this.yPts, this.xPts);
			this.yPts = newListsAfterY.get(0);
			this.xPts = newListsAfterY.get(1);

		}


		private List<List<Integer>> _makeContinuous(List<Integer> listToMakeContinuous, List<Integer> list2) {
			List<Integer> listCont = new ArrayList<Integer>();
			List<Integer> newList2 = new ArrayList<Integer>();
			int previousListCont = -1;
			int previousList2 = -1;
			for (int i = 0; i <  listToMakeContinuous.size(); i++) {
				Integer nextInt = listToMakeContinuous.get(i);
				if (previousListCont == -1 || Math.abs(nextInt - previousListCont) == 1) {
					previousListCont = nextInt;
					previousList2 = list2.get(i);
					listCont.add(nextInt);
					newList2.add(list2.get(i));
					continue;
				}

				int numToAdd = Math.abs(nextInt - previousListCont) - 1;
				boolean add = nextInt > previousListCont;
				for (int n = 0; n < numToAdd; n++) {
					if (add)
						listCont.add(previousListCont + n + 1);
					else
						listCont.add(previousListCont - n - 1);

					newList2.add((int) ((previousList2 + list2.get(i)) / 2.0));
				}
				previousListCont = nextInt;
				previousList2 = list2.get(i);
				listCont.add(nextInt);
				newList2.add(list2.get(i));
			}
			List<List<Integer>> output = new ArrayList<List<Integer>>();
			output.add(listCont);
			output.add(newList2);
			return output;
		}




	}

	public class MalformedBinException extends Exception {

		private static final long serialVersionUID = 1L;	

	}


	private static Point[] _getPointDownLine(double distance, Line line, Point startPointOnLine) {
		if (line.hasInfiniteSlope()) {
			return new Point[] {new Point(startPointOnLine.x, (int) (startPointOnLine.y + distance), false),
					new Point(startPointOnLine.x, (int) (startPointOnLine.y - distance), false)};
		} else {
			double xDiff = (distance / Math.sqrt(1 + Math.pow(line.slope, 2)));

			return new Point[] {
					new Point((int) (startPointOnLine.x + xDiff), line.getY(startPointOnLine.x + xDiff), false),
					new Point((int) (startPointOnLine.x - xDiff), line.getY(startPointOnLine.x - xDiff), false),
			};


		}
	}

	public int getEnclosingBin(Point p) {


		for (Bin bin : this.bins) {
			if (bin.containsPoint(p))
				return bin.binNum;
		}

		return -1;

	}
	
	public int getEnclosingBin(int x, int y) {


		for (Bin bin : this.bins) {
			if (bin.containsPoint(x, y))
				return bin.binNum;
		}

		return -1;

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

	private List<Bin> calculateBins() {

		List<Point> perimPoints = _getPerimeterPoints();
		List<Bin> bins = new ArrayList<Bin>();
		Point previousPt1 = null;
		Point previousPt2 = null;
		BinLine previousBinLine = null;
		int binNum = -1;

		for (BinLine line : this.binLines) {
			List<Integer> newXPoints = new ArrayList<Integer>();
			List<Integer> newYPoints = new ArrayList<Integer>();
			Point pEnd = getStretchedPoint(new Point(line.xPts.get(0), line.yPts.get(0), null));
			Point pStart = getStretchedPoint(new Point(line.xPts.get(line.xPts.size() - 1), line.yPts.get(line.yPts.size() - 1), null));

			

			int startIndex = perimPoints.indexOf(pStart);
			CircularListIterator<Point> circularItr = new CircularListIterator<Point>(perimPoints, startIndex, true);
			circularItr.next();

			if (previousBinLine == null || binNum == -1) {
				BinLine oppLine = previousBinLine == null ? this.binLines.get(this.binLines.size() - 1) : this.binLines.get(0);
				previousPt1 = getStretchedPoint(new Point(oppLine.xPts.get(0), oppLine.yPts.get(0), null));
				previousPt2 = getStretchedPoint(new Point(oppLine.xPts.get(oppLine.xPts.size() - 1), oppLine.yPts.get(oppLine.yPts.size() - 1), null));
				boolean switchDirection = false;
				while (circularItr.hasNext()) {
					Point perimPoint = circularItr.next();

					if (perimPoint.equals(pEnd)) {
						newXPoints.add(pEnd.x);
						newYPoints.add(pEnd.y);
						newXPoints.addAll(0, line.xPts);
						newYPoints.addAll(0, line.yPts);

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
							newXPoints.addAll(0, line.xPts);
							newYPoints.addAll(0, line.yPts);
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

						newXPoints.addAll(0, line.xPts);
						newYPoints.addAll(0, line.yPts);

						newXPoints.addAll(previousBinLine.xPts);
						newYPoints.addAll(previousBinLine.yPts);
						pointToContinueOffOf = previousPt2;
						break;
					} else if (perimPoint.equals(previousPt2)) {

						newXPoints.addAll(0, line.xPts);
						newYPoints.addAll(0, line.yPts);
						for (int i = previousBinLine.xPts.size() - 1; i >= 0; i--) {
							newXPoints.add(previousBinLine.xPts.get(i));
							newYPoints.add(previousBinLine.yPts.get(i));
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

							newXPoints.addAll(0, line.xPts);
							newYPoints.addAll(0, line.yPts);

							newXPoints.addAll(previousBinLine.xPts);
							newYPoints.addAll(previousBinLine.yPts);
							pointToContinueOffOf = previousPt2;
							break;
						} else if (perimPoint.equals(previousPt2)) {

							newXPoints.addAll(0, line.xPts);
							newYPoints.addAll(0, line.yPts);
							for (int i = previousBinLine.xPts.size() - 1; i >= 0; i--) {
								newXPoints.add(previousBinLine.xPts.get(i));
								newYPoints.add(previousBinLine.yPts.get(i));
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
			if (binNum == this.binLines.size() - 1) {
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


	private Point getStretchedPoint(Point p) {

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
		return p;
	}


}
