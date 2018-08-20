package com.typicalprojects.CellQuant.neuronal_migration.processing;

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

import java.util.Set;

import com.typicalprojects.CellQuant.neuronal_migration.GUI;
import com.typicalprojects.CellQuant.neuronal_migration.processing.Custom3DCounter.Column;
import com.typicalprojects.CellQuant.util.ImageContainer;
import com.typicalprojects.CellQuant.util.ImagePanel;
import com.typicalprojects.CellQuant.util.Point;
import com.typicalprojects.CellQuant.util.Zoom;
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

public class ObjectEditableImage {

	private ImageContainer ic;
	private Map<Channel, ImagePlus> images = new HashMap<Channel, ImagePlus>();
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

	public ObjectEditableImage(ImagePanel ip, ImageContainer ic, Map<Channel, ImagePlus> images, Map<Channel, ResultsTable> results) {
		this.imagePnl=ip;
		this.ic = ic;
		for (Channel chan : images.keySet()) {
			this.points.put(chan, new LinkedList<Point>());
		}

		for (Entry<Channel, ResultsTable> en : results.entrySet()) {
			ResultsTable rt = en.getValue();

			double[] xCoords = rt.getColumnAsDoubles(Column.X.getColumnNum());
			double[] yCoords = rt.getColumnAsDoubles(Column.Y.getColumnNum());
			List<Point> chanPoints = points.get(en.getKey());
			for (int i = 0; i < xCoords.length; i++) {
				chanPoints.add(new Point(Math.round((float) xCoords[i]), Math.round((float) yCoords[i]), true));
			}
		}

		this.images = images;
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

	public ImageContainer createNewImage() {
		List<Channel> channels = new ArrayList<Channel>();
		List<ImagePlus> newImages = new ArrayList<ImagePlus>();
		this.mask = true;
		this.original = true;
		this.dots = true;
		for (Channel chan : this.ic.getChannels()) {
			if (this.images.containsKey(chan)) {
				channels.add(chan);
				newImages.add(getImgWithDots(chan));
			} else {
				channels.add(chan);
				newImages.add(this.ic.getImageChannel(chan, true));
			}
		}
		return new ImageContainer(channels, newImages, this.ic.getAllSupplementalImages(), this.ic.getTotalImageTitle(), this.ic.getImgFile(), this.ic.getCalibration(), GUI.outputLocation, GUI.dateString);

	}

	@SuppressWarnings("deprecation")
	public Map<Channel, ResultsTable> createNewResultsTables() {
		Map<Channel, ResultsTable> tables = new HashMap<Channel, ResultsTable>();
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
			tables.put(en.getKey(), rt);
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



	public ImagePlus getImgWithDots(Channel chan){

		int newFontSize = this.fontSize;
		int newDotSize = this.dotSize;
		if (this.zoom !=null && !this.zoom.equals(Zoom.ZOOM_100)) {
			for (int j = 0; j < this.zoom.getLevel() - Zoom.ZOOM_100.getLevel(); j++) {
				newFontSize = (int) (newFontSize / 1.2);
				newDotSize = (int) (newDotSize / 1.2);

			}
		}

		
		ImagePlus stack = null;
		if (mask) {
			if (original) {
				stack = 	this.ic.getImageChannel(chan, true);
			} else {
				stack = this.ic.getSupplementalImage(this.ic.getImageChannel(chan, false).getTitle() + " " + NeuronProcessor.SUPP_LBL_MASK);
			}
		} else if (original) {
			stack = this.ic.getSupplementalImage(this.ic.getImageChannel(chan, false).getTitle() + " " + NeuronProcessor.SUPP_LBL_ORIGINAL);
		} else {
			/*ImagePlus dupDummy = this.ic.getImageChannel(chan, true);
			ImageRoi roi = new ImageRoi(0, 0, dupDummy.getProcessor());
			roi.setZeroTransparent(false);
			roi.setOpacity(1.0);
			dupDummy.getProcessor().drawOverlay(new Overlay(roi));
			dupDummy.updateImage();*/
			int[] dim = this.ic.getImageChannel(chan, true).getDimensions();
			stack = new ImagePlus("test", new BufferedImage(dim[0], dim[1], BufferedImage.TYPE_INT_RGB));
		}


		List<Point> chanPoints = this.points.get(chan);
		stack = new ImagePlus(stack.getTitle(), stack.getProcessor().convertToRGB()); // NOTE: changed teh stack to RGB, rather than just turning the copy of stack's processor for dot creation to RGB

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
