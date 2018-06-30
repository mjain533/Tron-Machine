package com.typicalprojects.CellQuant.neuronal_migration.processing;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.typicalprojects.CellQuant.neuronal_migration.processing.Custom3DCounter.Column;
import com.typicalprojects.CellQuant.util.ImageContainer;
import com.typicalprojects.CellQuant.util.ImagePanel;
import com.typicalprojects.CellQuant.util.Point;
import com.typicalprojects.CellQuant.util.Zoom;
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

public class ObjectEditableImage {

	private ImageContainer ic;
	private Map<Channel, ImagePlus> images = new HashMap<Channel, ImagePlus>();
	private Map<Channel, List<Point>> points = new HashMap<Channel, List<Point>>();
	private int dotSize = Custome3DObjectCounter.opResultDotsSize;
	private int fontSize = Custome3DObjectCounter.opResultFontSize;
	private Zoom zoom = Zoom.ZOOM_100;
	
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

	public void addPoint(Channel chan, Point p) {
		
		p.fromObjCounter = false;
		this.points.get(chan).add(p);
		
		this.imagePnl.setImage(this.getImgWithDots(chan).getBufferedImage(), -1, -1, zoom);


	}

	public boolean removePoint(Channel chan, Point p) {

		boolean removed = this.points.get(chan).remove(p);

		this.imagePnl.setImage(this.getImgWithDots(chan).getBufferedImage(), -1, -1, zoom);
		return removed;

	}
	
	public boolean removePoint(Channel chan, int pointNum) {

		boolean removed = this.points.get(chan).remove(pointNum - 1) != null;

		this.imagePnl.setImage(this.getImgWithDots(chan).getBufferedImage(), -1, -1, zoom);
		
		return removed;

	}

	public ImageContainer getContainer() {
		return this.ic;
	}

	public ImageContainer createNewImage() {
		List<Channel> channels = new ArrayList<Channel>();
		List<ImagePlus> newImages = new ArrayList<ImagePlus>();

		for (Channel chan : this.ic.getChannels()) {
			if (this.images.containsKey(chan)) {
				channels.add(chan);
				newImages.add(getImgWithDots(chan));
			} else {
				channels.add(chan);
				newImages.add(this.ic.getImageChannel(chan, true));
			}
		}
		return new ImageContainer(channels, newImages, this.ic.getTotalImageTitle(), this.ic.getImgFile(), false, this.ic.getCalibration());

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
		
		System.out.println(newDotSize);
		System.out.println(newFontSize);
		
		ImagePlus stack = this.ic.getImageChannel(chan, true);

		//ImagePlus img=NewImage.createImage(stack.getTitle(), stack.getWidth(), stack.getHeight(),1,32, 1);
		
		List<Point> chanPoints = this.points.get(chan);
		ImageProcessor ip= stack.getProcessor().convertToRGB();
		
		ip.setColor(Color.MAGENTA);
		ip.setLineWidth(newDotSize);
		

		for (Point point : chanPoints) {
			if (point != null && point.fromObjCounter) {
				ip.drawDot(point.x, point.y);
			} else {
				ip.setColor(Color.MAGENTA);
				ip.drawDot(point.x, point.y);
				ip.setColor(Color.MAGENTA);
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

		ip.setColor(Color.WHITE);;
		ip.setFont(new Font("Arial", Font.BOLD, newFontSize));
		int counter = 1;
		for (Point point : chanPoints) {
			if (point != null && point.fromObjCounter) {
				ip.drawString(""+counter, point.x, point.y);
			} else {
				ip.setColor(Color.WHITE);
				ip.drawString(""+counter, point.x, point.y);
				ip.setColor(Color.WHITE);
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

}
