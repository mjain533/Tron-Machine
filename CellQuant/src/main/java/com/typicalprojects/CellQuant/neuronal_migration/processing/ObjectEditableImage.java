package com.typicalprojects.CellQuant.neuronal_migration.processing;

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
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.gui.NewImage;
import ij.gui.Overlay;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

public class ObjectEditableImage {

	private ImageContainer ic;
	private Map<Channel, ImagePlus> images = new HashMap<Channel, ImagePlus>();
	private Map<Channel, List<Point>> points = new HashMap<Channel, List<Point>>();

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
		
		this.imagePnl.setImage(this.getImgWithDots(chan).getBufferedImage());


	}

	public boolean removePoint(Channel chan, Point p) {

		boolean removed = this.points.get(chan).remove(p);

		this.imagePnl.setImage(this.getImgWithDots(chan).getBufferedImage());
		return removed;

	}
	
	public boolean removePoint(Channel chan, int pointNum) {

		boolean removed = this.points.get(chan).remove(pointNum - 1) != null;

		this.imagePnl.setImage(this.getImgWithDots(chan).getBufferedImage());
		
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
		return new ImageContainer(channels, newImages, this.ic.getTotalImageTitle(), this.ic.getImgFile(), this.ic.getSaveDir(), false, this.ic.getCalibration());

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

		int dotSize = Custome3DObjectCounter.opResultDotsSize;
		int fontSize = Custome3DObjectCounter.opResultFontSize;

		ImagePlus stack = this.ic.getImageChannel(chan, true);

		ImagePlus img=NewImage.createImage(stack.getTitle(), stack.getWidth(), stack.getHeight(),1,8, 1);

		List<Point> chanPoints = this.points.get(chan);
		ImageProcessor ip=img.getProcessor();

		ip.setValue(256);
		ip.setLineWidth(dotSize);
		

		for (Point point : chanPoints) {
			if (point.fromObjCounter) {
				ip.drawDot(point.x, point.y);
			} else {
				ip.drawRect(Math.max(point.x - 1, 0), Math.max(point.y - 1, 0), 2, 2);
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

		ip.setValue(256);
		ip.setFont(new Font("Arial", Font.BOLD, fontSize));
		int counter = 1;
		for (Point point : chanPoints) {
			ip.drawString(""+counter, point.x, point.y);
			counter++;

		}

		img.setDisplayRange(1, 256);
		img.updateImage();
		
		ImageRoi roi = new ImageRoi(0, 0, ip);
		roi.setZeroTransparent(true);
		roi.setOpacity(1.0);
		stack.getProcessor().drawOverlay(new Overlay(roi));
		stack.updateImage();
		return stack;
	}

}
