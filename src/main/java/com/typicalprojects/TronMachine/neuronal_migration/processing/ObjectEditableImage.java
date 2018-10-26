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
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.processing.Custom3DCounter.Column;
import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.ImagePanel;
import com.typicalprojects.TronMachine.util.Point;
import com.typicalprojects.TronMachine.util.Zoom;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;
import com.typicalprojects.TronMachine.neuronal_migration.*;


import java.util.Set;

import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

public class ObjectEditableImage {

	private ImageContainer ic;
	private Map<Channel, List<Point>> points = new HashMap<Channel, List<Point>>();
	private int dotSize = Custome3DObjectCounter.opResultDotsSize;
	private int fontSize = Custome3DObjectCounter.opResultFontSize;
	private Zoom zoom = Zoom.ZOOM_100;
	private boolean creatingDeletionZone;
	private List<Point> deletionZone = new ArrayList<Point>();


	private boolean mask = true;
	private boolean original = true;
	private boolean dots = true;


	private ImagePanel imagePnl;

	public ObjectEditableImage(ImagePanel ip, ImageContainer ic, List<Channel> images, Map<String, ResultsTable> results) {
		this.imagePnl=ip;
		this.ic = ic;
		for (Channel chan : images) {
			this.points.put(chan, new LinkedList<Point>());
		}
		





		for (Entry<String, ResultsTable> en : results.entrySet()) {
			ResultsTable rt = en.getValue();
			Channel chan = Channel.parse(en.getKey());
			if (chan == null)
				continue;
			double[] xCoords = rt.getColumnAsDoubles(Column.X.getColumnNum());
			double[] yCoords = rt.getColumnAsDoubles(Column.Y.getColumnNum());
			List<Point> chanPoints = points.get(chan);
			if (xCoords != null && yCoords != null) {
				for (int i = 0; i < xCoords.length; i++) {
					chanPoints.add(new Point(Math.round((float) xCoords[i]), Math.round((float) yCoords[i]), true));
				}
			}
			
		}

	}

	public boolean deleteObjectsWithinDeletionZone(Channel chanToDisplayAfterward) {

		Object[] obj = convertDeletionPointsToArray(true);
		if (obj == null) {
			System.out.println("called null");
			this.deletionZone.clear();
			this.creatingDeletionZone = false;

			this.imagePnl.setImage(this.getImgWithDots(chanToDisplayAfterward).getBufferedImage(), -1, -1, zoom);

			return false;

		}

		Polygon pg = new PolygonRoi((float[]) obj[0], (float[]) obj[1], this.deletionZone.size(), Roi.POLYLINE).getPolygon() ;
		for (Entry<Channel, List<Point>> en : this.points.entrySet()) {
			Iterator<Point> channelPtsItr = en.getValue().iterator();
			while (channelPtsItr.hasNext()) {
				Point chnlPt = channelPtsItr.next();
				if (pg.contains(chnlPt.x, chnlPt.y)) {
					channelPtsItr.remove();
				}
			}
		}

		this.deletionZone.clear();
		this.creatingDeletionZone = false;

		this.imagePnl.setImage(this.getImgWithDots(chanToDisplayAfterward).getBufferedImage(), -1, -1, zoom);

		return true;
	}

	public void addDeletionZonePoint(Point p, Channel chanToDisplayAfterward) {
		p.fromObjCounter = false;
		this.creatingDeletionZone = true;
		this.deletionZone.add(p);

		this.imagePnl.setImage(this.getImgWithDots(chanToDisplayAfterward).getBufferedImage(), -1, -1, zoom);

	}

	public void cancelDeletionZone(Channel chanToDisplayAfterward) {
		this.deletionZone.clear();
		this.creatingDeletionZone = false;
		this.imagePnl.setImage(this.getImgWithDots(chanToDisplayAfterward).getBufferedImage(), -1, -1, zoom);

	}

	public boolean isCreatingDeletionZone() {
		return this.creatingDeletionZone;
	}

	public void setCreatingDeletionZone(boolean creatingDeletionZone) {
		this.creatingDeletionZone = creatingDeletionZone;
	}

	public void addPoint(Channel chan, Point p) {

		p.fromObjCounter = false;
		this.points.get(chan).add(p);

		this.imagePnl.setImage(this.getImgWithDots(chan).getBufferedImage(), -1, -1, zoom);


	}

	public Point getNearestPoint(Channel chan, Point p) {
		int range = this.dotSize;
		if (this.zoom !=null && !this.zoom.equals(Zoom.ZOOM_100)) {
			for (int j = 0; j < this.zoom.getLevel() - Zoom.ZOOM_100.getLevel(); j++) {
				range = (int) (range / 1.2);
			}
		}
		List<Point> chanPoints = this.points.get(chan);
		Point currClosest = null;
		double dist = -1;
		for (Point chanPoint : chanPoints) {
			if (Math.abs(chanPoint.x - p.x) < range && Math.abs(chanPoint.y - p.y) < range) {

				double newDist = Math.sqrt(Math.pow(chanPoint.x - p.x, 2) + Math.pow(chanPoint.y - p.y, 2));
				if (newDist < dist || dist < 0) {
					dist = newDist;
					currClosest = chanPoint;
				}
			}
		}

		return currClosest;

	}

	public boolean removePoint(Channel chan, Point p) {

		boolean removed = this.points.get(chan).remove(p);

		this.imagePnl.setImage(this.getImgWithDots(chan).getBufferedImage(), -1, -1, zoom);
		return removed;

	}

	public boolean removePoints(Channel chan, Set<Integer> points) {
		boolean validPoints = true;

		try {
			List<Integer> pointsList = new ArrayList<Integer>(points);
			pointsList.sort(null);
			for (int i = pointsList.size() - 1; i >= 0; i--) {
				validPoints = this.points.get(chan).remove((int) pointsList.get(i) - 1) != null ? validPoints : false;
			}
			this.imagePnl.setImage(this.getImgWithDots(chan).getBufferedImage(), -1, -1, zoom);

		}catch(Exception e) {
			validPoints = false;
		}

		return validPoints;

	}

	public boolean removePoint(Channel chan, int pointNum) {
		boolean removed = false;
		try {
			removed = this.points.get(chan).remove(pointNum - 1) != null;

			this.imagePnl.setImage(this.getImgWithDots(chan).getBufferedImage(), -1, -1, zoom);

		} catch (Exception e) {
			removed = false;
		}

		return removed;

	}

	public ImageContainer getContainer() {
		return this.ic;
	}

	public void createAndSaveNewImage() {

		for (Channel chan : Channel.values()) {
			if (this.points.containsKey(chan)) {
				this.dots = true;
				this.mask = true;
				this.original = true;
				this.ic.saveSupplementalImage(OutputOption.ProcessedFull, getImgWithDots(chan, false, true), chan);
				// Need for ROI selection. Will delete these irrelevant files at end.


				if (GUI.outputOptionsContainChannel(Arrays.asList(OutputOption.MaxedChannel, 
						OutputOption.RoiDrawFull, OutputOption.BinDrawFull), chan)) {
					this.dots = false;
					this.mask = false;
					this.original = true;
					this.ic.saveSupplementalImage(OutputOption.MaxedChannel, getImgWithDots(chan, false, true), chan);
				}
				if (GUI.outputOptionsContainChannel(Arrays.asList(OutputOption.ProcessedDots, 
						OutputOption.BinDrawProcessedDots, OutputOption.RoiDrawProcessedDots), chan)) {
					this.dots = true;
					this.mask = false;
					this.original = false;
					this.ic.saveSupplementalImage(OutputOption.ProcessedDots, getImgWithDots(chan, false, true), chan);
				}
				if (GUI.outputOptionsContainChannel(Arrays.asList(OutputOption.ProcessedDotsNoNum, 
						OutputOption.BinDrawProcessedDotsNoNum, OutputOption.RoiDrawProcessedDotsNoNum), chan)) {
					this.dots = true;
					this.mask = false;
					this.original = false;
					this.ic.saveSupplementalImage(OutputOption.ProcessedDotsNoNum, getImgWithDots(chan, false, false), chan);
				}
				if (GUI.outputOptionsContainChannel(Arrays.asList(OutputOption.ProcessedDotsObjects, 
						OutputOption.BinDrawProcessedDotsObjects, OutputOption.RoiDrawProcessedDotsObjects), chan)) {
					this.dots = true;
					this.mask = true;
					this.original = false;
					this.ic.saveSupplementalImage(OutputOption.ProcessedDotsObjects, getImgWithDots(chan, false, true), chan);
				}
				if (GUI.outputOptionsContainChannel(Arrays.asList(OutputOption.ProcessedObjects, 
						OutputOption.BinDrawProcessedObjects, OutputOption.RoiDrawProcessedObjects), chan)) {
					this.dots = false;
					this.mask = true;
					this.original = false;
					this.ic.saveSupplementalImage(OutputOption.ProcessedObjects, getImgWithDots(chan, false, true), chan);
				}
				if (GUI.outputOptionsContainChannel(Arrays.asList(OutputOption.ProcessedObjectsOriginal, 
						OutputOption.BinDrawProcessedObjectsOriginal, OutputOption.RoiDrawProcessedObjectsOriginal), chan)) {
					this.dots = false;
					this.mask = true;
					this.original = true;
					this.ic.saveSupplementalImage(OutputOption.ProcessedObjectsOriginal, getImgWithDots(chan, false, true), chan);
				}
				if (GUI.outputOptionsContainChannel(Arrays.asList(OutputOption.ProcessedDotsOriginal, 
						OutputOption.BinDrawProcessedDotsOriginal, OutputOption.RoiDrawProcessedDotsOriginal), chan)) {
					this.dots = true;
					this.mask = false;
					this.original = true;
					this.ic.saveSupplementalImage(OutputOption.ProcessedDotsOriginal, getImgWithDots(chan, false, true), chan);
				}
			} else {
				this.dots = false;
				this.mask = false;
				this.original = true;
				this.ic.saveSupplementalImage(OutputOption.MaxedChannel, getImgWithDots(chan, false, true), chan);
				// Need for ROI selection. Will delete these irrelevant files at end.

			}
			
		}
		this.dots = true;
		this.mask = true;
		this.original = true;
	}

	@SuppressWarnings("deprecation")
	public Map<String, ResultsTable> createNewResultsTables() {
		Map<String, ResultsTable> tables = new HashMap<String, ResultsTable>();
		for (Entry<Channel, List<Point>> en : this.points.entrySet()) {
			ResultsTable rt = new ResultsTable();
			rt.setHeading(0, "ID");
			for (Column col : Column.values()) {
				rt.setHeading(col.getColumnNum() + 1, col.getTitle());
			}

			int counter = 0;
			for (Point p : en.getValue()) {
				rt.incrementCounter();
				rt.setValue("ID", counter, counter + 1);
				rt.setValue(Column.X.getTitle(), counter, p.x);
				rt.setValue(Column.Y.getTitle(), counter, p.y);

				counter++;
			}
			tables.put(en.getKey().name(), rt);
		}
		return tables;
	}

	@SuppressWarnings("deprecation")
	public ResultsTable createNewResultsTable(Channel chan) {

		ResultsTable rt = new ResultsTable();
		rt.setHeading(0, "ID");
		for (Column col : Column.values()) {
			rt.setHeading(col.getColumnNum() + 1, col.getTitle());
		}

		int counter = 0;
		for (Point p : this.points.get(chan)) {
			rt.incrementCounter();
			rt.setValue("ID", counter, counter + 1);
			rt.setValue(Column.X.getTitle(), counter, p.x);
			rt.setValue(Column.Y.getTitle(), counter, p.y);

			counter++;
		}

		return rt;

	}

	public ImagePlus getImgWithDots(Channel chan) {
		return getImgWithDots(chan, true, true);
	}


	public ImagePlus getImgWithDots(Channel chan, boolean takeZoomIntoAccount, boolean includeTextOnDots){

		int newFontSize = this.fontSize;
		int newDotSize = this.dotSize;
		if (takeZoomIntoAccount) {
			if (this.zoom !=null && !this.zoom.equals(Zoom.ZOOM_100)) {
				for (int j = 0; j < this.zoom.getLevel() - Zoom.ZOOM_100.getLevel(); j++) {
					newFontSize = (int) (newFontSize / 1.2);
					newDotSize = (int) (newDotSize / 1.2);

				}
			}
		}
		

		
		ImagePlus stack = null;
		if (mask) {
			if (original) {
				stack = 	this.ic.getImage(OutputOption.ProcessedObjectsOriginal, chan, true);
			} else {
				stack = this.ic.getImage(OutputOption.ProcessedObjects, chan, false);
			}
		} else if (original) {
			stack = 	this.ic.getImage(OutputOption.MaxedChannel, chan, false);
		} else {
			/*ImagePlus dupDummy = this.ic.getImageChannel(chan, true);
			ImageRoi roi = new ImageRoi(0, 0, dupDummy.getProcessor());
			roi.setZeroTransparent(false);
			roi.setOpacity(1.0);
			dupDummy.getProcessor().drawOverlay(new Overlay(roi));
			dupDummy.updateImage();*/
			int[] dim = this.ic.getDimensions();
			stack = new ImagePlus("test", new BufferedImage(dim[0], dim[1], BufferedImage.TYPE_INT_RGB));
		}


		List<Point> chanPoints = this.points.get(chan);
		stack = new ImagePlus("temp", stack.getProcessor().convertToRGB()); // NOTE: changed teh stack to RGB, rather than just turning the copy of stack's processor for dot creation to RGB

		if (chanPoints != null) {

			if (dots) {
				ImageProcessor ip = stack.getProcessor().duplicate();
				
				ip.setColor(Color.BLUE);
				ip.setLineWidth(newDotSize);


				for (Point point : chanPoints) {
					if (point != null && point.fromObjCounter) {
						ip.drawDot(point.x, point.y);
					} else {
						ip.setColor(Color.BLUE);
						ip.drawDot(point.x, point.y);
						ip.setColor(Color.BLUE);
					}
				}
				/*for (int y=0; y<stack.getHeight(); y++){
					for (int x=0; x<stack.getWidth(); x++){

						if (ip.getPixelValue(x, y) < 0.5) {
							ip.setValue(0);
							ip.setColor(Color.BLACK);
							ip.drawPixel(x, y);

						}				

					}
				}
				img.updateImage();*/

				if (includeTextOnDots) {
					ip.setColor(Color.LIGHT_GRAY);
					ip.setFont(new Font("Arial", Font.BOLD, newFontSize));
					int counter = 1;
					for (Point point : chanPoints) {
						if (point != null && point.fromObjCounter) {
							ip.drawString(""+counter, point.x, point.y);
						} else {
							ip.setColor(Color.LIGHT_GRAY);
							ip.drawString(""+counter, point.x, point.y);
							ip.setColor(Color.LIGHT_GRAY);
						}
						counter++;

					}
				}
				
				//img.setDisplayRange(1, Integer.MAX_VALUE);
				//img.updateImage();

				ImageRoi roi = new ImageRoi(0, 0, ip);
				roi.setZeroTransparent(true);
				roi.setOpacity(1.0);
				stack.getProcessor().drawOverlay(new Overlay(roi));
				stack.updateImage();
			}


			if (this.creatingDeletionZone) {

				Object[] obj = convertDeletionPointsToArray(false);

				if (obj != null) {
					if (this.deletionZone.size() == 1) {

						stack.getProcessor().setColor(Color.GREEN);
						int size = (int) (Math.max(stack.getDimensions()[0], stack.getDimensions()[1] ) / 100.0);
						stack.getProcessor().fillRect((int) ((float[]) obj[0])[0] - (size / 2), (int) ((float[]) obj[1])[0] - (size / 2), size, size);
						stack.updateImage();

					} else {
						System.out.println(Arrays.toString((float[]) obj[0]));
						PolygonRoi pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], this.deletionZone.size(), Roi.POLYLINE) ;
						pgr.setStrokeColor(Color.GREEN);
						pgr.setFillColor(Color.GREEN);
						double size = Math.max(stack.getDimensions()[0], stack.getDimensions()[1] );

						pgr.setStrokeWidth(size / 300.0);

						stack.getProcessor().setColor(Color.GREEN);
						stack.getProcessor().drawOverlay(new Overlay(pgr));
						stack.updateImage();

					}
				}


			}
		}


		return stack;
	}

	public void setFontSize(int fontSize) {
		this.fontSize = fontSize;
	}

	public void setDotSize(int dotSize) {
		this.dotSize = dotSize;
	}

	public void clearZoom() {
		this.zoom = Zoom.ZOOM_100;
	}

	public Zoom getZoom() {
		return this.zoom;
	}

	public void setZoom(Zoom newZoom) {
		this.zoom = newZoom;
	}

	public Zoom getNextZoom() {
		return this.zoom.getNextZoomLevel();
	}

	public Zoom getPreviousZoomLevel() {
		return this.zoom.getPreviousZoomLevel();
	}

	private Object[] convertDeletionPointsToArray(boolean finished) {

		if (finished) {
			if (this.deletionZone.size() <= 2) {
				return null;
			}
			float[] xCoords = new float[this.deletionZone.size() + 1];
			float[] yCoords = new float[this.deletionZone.size() + 1];
			int counter = 0;
			for (Point p : this.deletionZone) {
				xCoords[counter] = p.x;
				yCoords[counter] = p.y;
				counter++;
			}
			xCoords[deletionZone.size()] = xCoords[0];
			yCoords[deletionZone.size()] = xCoords[0];

			return new Object[] {xCoords, yCoords};
		} else {
			if (this.deletionZone.isEmpty()) {
				return null;
			}
			float[] xCoords = new float[this.deletionZone.size()];
			float[] yCoords = new float[this.deletionZone.size()];
			int counter = 0;
			for (Point p : this.deletionZone) {
				xCoords[counter] = p.x;
				yCoords[counter] = p.y;
				counter++;
			}
			return new Object[] {xCoords, yCoords};
		}

	}
	
	public void setDisplaying(boolean original, boolean mask, boolean dots, Channel channelToDisplayAfterUpdate) {
		
		if (!original && !mask && !dots) {
			throw new IllegalArgumentException();
		}
		
		this.original = original;
		this.mask = mask;
		this.dots = dots;

		this.imagePnl.setImage(this.getImgWithDots(channelToDisplayAfterUpdate).getBufferedImage(), -1, -1, zoom);

		
		
	}

}
