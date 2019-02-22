package com.typicalprojects.TronMachine.util;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class Line {

	private final Point point1;
	private final Point point2;
	private final double slope;

	public Line(Point point1, Point point2) {
		this.point1 = point1;
		this.point2 = point2;
		if (point1.x == point2.x) {
			this.slope = Double.POSITIVE_INFINITY;
		} else {
			this.slope = (point1.y - point2.y) / ((double) (point1.x - point2.x));

		}
	}

	public Line(int x1, int y1, int x2, int y2) {
		this(new Point(x1, y1, false), new Point(x2, y2, false));
	}


	public int getY(double x) {

		if (hasInfiniteSlope()) {
			return Integer.MAX_VALUE;
		}
		return (int) (this.slope * (x - point1.x) + point1.y);
	}
	
	public int getX(double y) {

		if (hasInfiniteSlope()) {
			return point2.x;
		} else if (this.slope == 0) {
			return Integer.MAX_VALUE;
		}
		
		return (int) (((y - point1.y) / this.slope) + point1.x);
	}

	public boolean isWithinBoundingBox(Point p) {
		if (p.x >= Math.min(point1.x, point2.x) && p.x <= Math.max(point1.x, point2.x) &&
				p.y >= Math.min(point1.y, point2.y) && p.y <= Math.max(point1.y, point2.y)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean hasInfiniteSlope() {
		return this.slope == Double.POSITIVE_INFINITY;
	}
	
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
