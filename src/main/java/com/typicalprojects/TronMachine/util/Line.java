package com.typicalprojects.TronMachine.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Simple bean representing a Line. Sort of re-inventing the wheel, but this class more safely deals with
 * lines of infinite slope (vertical lines) when finding X and Y points. It also allows finding points
 * off of the line segment (extrapolated).
 * 
 * @author Justin Carrington
 *
 */
public class Line {

	private final Point point1;
	private final Point point2;
	private final double slope;

	/**
	 * Creates a new line. The order of the points matters. The first point is the start of the line segment,
	 * the second is the end. This is important when extrapolating points away from the line using the 
	 * {@link #getExtrapolatedPoint(int, int, int, int)} method.
	 * 
	 * @param point1 The start point of the line segment
	 * @param point2 The end point of the line segment
	 */
	public Line(Point point1, Point point2) {
		this.point1 = point1;
		this.point2 = point2;
		if (point1.x == point2.x) {
			this.slope = Double.POSITIVE_INFINITY;
		} else {
			this.slope = (point1.y - point2.y) / ((double) (point1.x - point2.x));

		}
	}

	/**
	 * Constructs a line similar to {@link #Line(Point, Point)}
	 * 
	 * @param x1 x-coordinate of start point
	 * @param y1 y-coordinate of end point
	 * @param x2 x-coordinate of start point
	 * @param y2 y-coordinate of end point
	 */
	public Line(int x1, int y1, int x2, int y2) {
		this(new Point(x1, y1, false), new Point(x2, y2, false));
	}

	/**
	 * @param x input X value
	 * @return the y coordinate corresponding to an X on the line. If the slope is infinite, this will return
	 *  the maximum java Integer value.
	 */
	public int getY(double x) {

		if (hasInfiniteSlope()) {
			return Integer.MAX_VALUE;
		}
		return (int) (this.slope * (x - point1.x) + point1.y);
	}
	
	/**
	 * @param y input Y value
	 * @return the x coordinate corresponding to an Y on the line. If the slope is zero, this will return
	 *  the maximum java Integer value.
	 */
	public int getX(double y) {

		if (hasInfiniteSlope()) {
			return point2.x;
		} else if (this.slope == 0) {
			return Integer.MAX_VALUE;
		}
		
		return (int) (((y - point1.y) / this.slope) + point1.x);
	}

	/**
	 * Checks if the passed point is within the bounding rectangle created from the two points used to create
	 * this Line object.
	 * 
	 * @param p	the query point
	 * @return true if point is within bounding rectangle
	 */
	public boolean isWithinBoundingBox(Point p) {
		if (p.x >= Math.min(point1.x, point2.x) && p.x <= Math.max(point1.x, point2.x) &&
				p.y >= Math.min(point1.y, point2.y) && p.y <= Math.max(point1.y, point2.y)) {
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @return true if the slope of this line is infinite (vertical line in Cartesian coordinate system)
	 */
	public boolean hasInfiniteSlope() {
		return this.slope == Double.POSITIVE_INFINITY;
	}
	
	/**
	 * Extrapolates this line to a specific point. Essentially, the inputs describe a rectangle which contains
	 * this Line. The point of the extrapolation method is to find the point on this rectangle if the line
	 * were to be extended outward from the SECOND point in the line segment (the end point).
	 * 
	 * @param minX lower X of the bounding rectangle
	 * @param minY lower Y of the bounding rectangle
	 * @param maxX upper X of the bounding rectangle
	 * @param maxY upper Y of the bounding rectangle
	 * @return Point which is on the bounding rectangle for extrapolation
	 */
	public Point getExtrapolatedPoint(int minX, int minY, int maxX, int maxY) {
		
		if (point2.x == minX || point2.y == minY || point2.x == maxX || point2.y == maxY) {
			return null;
		}
		
		if (hasInfiniteSlope()) {
			Point candidate = new Point(this.point1.x, minY, null);
			boolean yGood = false;
			if (point2.y > point1.y) {
				if (candidate.y > point2.y) {
					yGood = true;
				}
			} else {
				if (candidate.y < point2.y) {
					yGood = true;
				}
			}
			if (yGood)
				return candidate;
			else
				return new Point(this.point1.x, maxY, null);
		} else if (this.slope == 0) {
			Point candidate = new Point(minX, this.point1.y, null);
			boolean xGood = false;
			if (point2.x > point1.x) {
				if (candidate.x > point2.x) {
					xGood = true;
				}
			} else {
				if (candidate.x < point2.x) {
					xGood = true;
				}
			}
			if (xGood)
				return candidate;
			else
				return new Point(maxX, this.point1.y, null);
		}
		
		
		Set<Point> points = new HashSet<Point>();
		points.add(new Point(getX(minY), minY, null));
		points.add(new Point(getX(maxY), maxY, null));
		points.add(new Point(minX, getY(minX), null));
		points.add(new Point(maxX, getY(maxX), null));
		
		Iterator<Point> ptItr = points.iterator();
		while(ptItr.hasNext()) {
			if (!isContained(ptItr.next(), minX, minY, maxX, maxY)) {
				ptItr.remove();
			}
		}
		
		ptItr = null;
		if (points.isEmpty())
			throw new IllegalStateException("Illegal line creation.");
		
		for (Point p : points) {
			boolean xGood = false;
			boolean yGood = false;
			if (point2.x > point1.x) {
				if (p.x > point2.x) {
					xGood = true;
				}
			} else {
				if (p.x < point2.x) {
					xGood = true;
				}
			}
			
			if (point2.y > point1.y) {
				if (p.y > point2.y) {
					yGood = true;
				}
			} else {
				if (p.y < point2.y) {
					yGood = true;
				}
			}
			if (xGood && yGood) {
				return p;
			}
		}
		
		throw new IllegalStateException("Illegal line creation.");
		
		
	}
	
	private boolean isContained(Point p, int minX, int minY, int maxX, int maxY) {
		return p.x >= minX && p.x <= maxX && p.y <= maxY && p.y >= minY;
	}

}
