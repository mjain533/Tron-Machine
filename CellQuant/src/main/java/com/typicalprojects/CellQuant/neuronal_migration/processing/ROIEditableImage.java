package com.typicalprojects.CellQuant.neuronal_migration.processing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.typicalprojects.CellQuant.neuronal_migration.GUI;
import com.typicalprojects.CellQuant.neuronal_migration.processing.Custom3DCounter.Column;
import com.typicalprojects.CellQuant.util.ImageContainer;
import com.typicalprojects.CellQuant.util.Point;
import com.typicalprojects.CellQuant.util.ImagePhantom;
import com.typicalprojects.CellQuant.util.SynchronizedProgress;
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.RoiEncoder;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;

public class ROIEditableImage {

	private ImageContainer ic;
	private Map<Channel, ResultsTable> tables;
	private Channel drawChannel;
	private GUI gui;
	private List<String> roi_names = new ArrayList<String>();
	private List<PolygonRoi> roi_polygons = new ArrayList<PolygonRoi>();

	private List<Point> points = new ArrayList<Point>();

	public ROIEditableImage(ImageContainer ic, Channel drawChannel, Map<Channel, ResultsTable> tables, GUI gui) {
		this.gui = gui;
		this.ic =ic;
		this.drawChannel = drawChannel;
		this.tables = tables;
	}

	public ImageContainer getContainer() {
		return this.ic;
	}

	public boolean hasROIs() {
		return !this.roi_names.isEmpty();
	}


	public BufferedImage getPaintedCopy(Channel channelToDrawROI) {

		if (this.points.isEmpty()) {

			return this.ic.getImageChannel(channelToDrawROI, false).getProcessor().convertToRGB().getBufferedImage();

		}

		Object[] obj = convertPointsToArray();

		if (points.size() == 1) {
			
			ImagePlus dup = new ImagePlus("dup", this.ic.getImageChannel(channelToDrawROI, false).getProcessor().convertToRGB());
			
		
			dup.getProcessor().setColor(Color.GREEN);
			dup.getProcessor().drawOval((int) ((float[]) obj[0])[0], (int) ((float[]) obj[1])[0], 6, 6);
			dup.updateImage();
			return dup.getBufferedImage();

		} else {

			PolygonRoi pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], points.size(), Roi.POLYLINE) ;
			pgr.setStrokeColor(Color.GREEN);
			pgr.setFillColor(Color.GREEN);
			pgr.setStrokeWidth(3);
			if (points.size() > 2) {
				pgr.fitSplineForStraightening();;
			}


			ImagePlus dup = new ImagePlus("Dup", this.ic.getImageChannel(channelToDrawROI, false).getProcessor().convertToRGB());

			dup.getProcessor().setColor(Color.GREEN);
			dup.getProcessor().drawOverlay(new Overlay(pgr));
			dup.updateImage();
			return dup.getBufferedImage();


		}


	}

	public void addPoint(Point p) {
		this.points.add(p);
	}

	public boolean convertSelectionToRoi(String name) {
		if (this.points.size() < 2) {
			return false;
		} else if (this.roi_names.contains(name)) {
			return false;
		}
		ImagePlus drawImage = this.ic.getImageChannel(this.drawChannel, false);

		int start;
		int finish;
		if ((((double) this.points.get(0).x) / drawImage.getDimensions()[0]) > 0.5) {
			start = drawImage.getDimensions()[0];
			finish = 0;
		} else {
			finish = drawImage.getDimensions()[0];
			start = 0;
		}
		
		if (this.points.get(0).x != start) {
			this.points.add(0, new Point(start, this.points.get(0).y, null));
		}
		if (this.points.get(this.points.size() - 1).x != finish) {
			this.points.add(new Point(finish, this.points.get(this.points.size()-1).y, null));
		}

		Object[] obj = convertPointsToArray();

		PolygonRoi pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], points.size(), Roi.POLYLINE) ;
		pgr.setStrokeColor(Color.GREEN);
		pgr.setFillColor(Color.GREEN);
		pgr.setStrokeWidth(3);
		pgr.fitSplineForStraightening();
		pgr.setName(name);
		this.roi_names.add(name);
		this.roi_polygons.add(pgr);
		this.points.clear();

		return true;
	}

	public void clearPints() {
		this.points.clear();
	}

	public void removeROI(String name) {
		int index = this.roi_names.indexOf(name);
		this.roi_names.remove(index);
		this.roi_polygons.remove(index);
	}

	public boolean hasPoints() {
		return !this.points.isEmpty();
	}

	private Object[] convertPointsToArray() {

		float[] xCoords = new float[this.points.size()];
		float[] yCoords = new float[this.points.size()];
		int counter = 0;
		for (Point p : this.points) {
			xCoords[counter] = p.x;
			yCoords[counter] = p.y;
			counter++;
		}
		return new Object[] {xCoords, yCoords};
	}

	public ImageContainer getNewImage() {

		List<Channel> channels = new ArrayList<Channel>();
		List<ImagePlus> images = new ArrayList<ImagePlus>();
		for (Channel chan : this.ic.getChannels()) {
			if (chan.equals(GUI.channelForROIDraw)) {
				channels.add(chan);
				ImagePlus roiImg = this.ic.getImageChannel(chan, true);

				int startPixVal = 256;

				roiImg.setProcessor(roiImg.getProcessor().convertToColorProcessor());
				ImageProcessor ip = roiImg.getProcessor();
				ip.setColor(Color.WHITE);
				ip.setValue(256);
				ip.setLineWidth(3);
				ip.setFont(new Font("Arial", Font.BOLD, 20));

				for (int i = 0; i < this.roi_names.size(); i++) {
					String name = this.roi_names.get(i);
					PolygonRoi roi = this.roi_polygons.get(i);
					roi.setStrokeWidth(3);
					ip.drawOverlay(new Overlay(roi));
					Rectangle rect = roi.getBounds();
					ip.drawString(name, (int) (((double) (rect.x + rect.width)) / 2.0), rect.y, Color.RED);
					startPixVal = startPixVal - 10;
					if (startPixVal < 50) {
						startPixVal = 256 - (50-startPixVal);
					}
				}

				roiImg.updateImage();
				images.add(roiImg);

			} else if (GUI.channelsToProcess.contains(chan)) { // TODO exclude channels earlier
				channels.add(chan);
				images.add(this.ic.getImageChannel(chan, false));
			}
		}


		return new ImageContainer(channels, images, this.ic.getTotalImageTitle(), this.ic.getImgFile(), true, this.ic.getCalibration());

	}

	public void saveROIs() {
		for (PolygonRoi roi : this.roi_polygons) {
			RoiEncoder.save(roi, this.ic.getIntermediateFilesDirectory() + File.separator + roi.getName() +".roi");
		}
	}

	public Map<Channel, ResultsTable> process(SynchronizedProgress progress) {		

		Map<Channel, ResultsTable> map = new HashMap<Channel, ResultsTable>();
		for (Entry<Channel, ResultsTable> en : this.tables.entrySet()) {
			map.put(en.getKey(), calculateDistances(en.getValue(), en.getKey(), progress));
		}
		return map;
	}

	@SuppressWarnings("deprecation")
	private ResultsTable calculateDistances(ResultsTable input, Channel chan, SynchronizedProgress progress) {

		float[] ids = input.getColumn(input.getColumnIndex("ID"));
		float[] xObjValues = input.getColumn(input.getColumnIndex(Column.X.getTitle()));
		float[] yObjValues = input.getColumn(input.getColumnIndex(Column.Y.getTitle()));
		progress.setProgress("Preparing results...", -1, -1);
		ResultsTable newTable = new ResultsTable();
		newTable.setHeading(0, "Object Num");
		newTable.setHeading(1, "X (pixels)");
		newTable.setHeading(2, "Y (pixels)");

		int counter = 3;
		List<Map<Integer, Integer>> coords = new ArrayList<Map<Integer, Integer>>();


		Calibration cal = this.ic.getCalibration();


		for (String roiName : this.roi_names) {
			newTable.setHeading(counter, "Distance from " + roiName + " (" + cal.getUnits() +")");
			Map<Integer, Integer> pts = new HashMap<Integer, Integer>();

			Polygon p = this.roi_polygons.get(this.roi_names.indexOf(roiName)).getPolygon();

			for (int polygonPt = 0; polygonPt < p.npoints; polygonPt ++) {
				pts.put(p.xpoints[polygonPt], p.ypoints[polygonPt]);
			}

			coords.add(pts);

			counter++;

		}

		newTable.setHeading(counter, "Grayscale Value");

		try {
			for (int i = 0; i < xObjValues.length; i++) {
				progress.setProgress("Measuring distance to ROIs... ", i + 1, xObjValues.length);
				newTable.incrementCounter();
				newTable.addValue(0, ids[i]);
				newTable.addValue(1, xObjValues[i]);
				newTable.addValue(2, yObjValues[i]);

				int r = 3;
				for (Map<Integer, Integer> coordinatePts : coords) {

					double shortestDist = Double.MAX_VALUE;

					for (Entry<Integer, Integer> en : coordinatePts.entrySet()) {

						double dist = Math.sqrt(Math.pow(en.getKey() - xObjValues[i], 2) + Math.pow(en.getValue() - yObjValues[i], 2));
						if (dist < shortestDist)
							shortestDist = dist;
					}

					if (yObjValues[i] > coordinatePts.get((int)xObjValues[i])) {
						newTable.addValue(r, cal.getX(shortestDist)); // could be X or Y, just for conversion
					} else {
						newTable.addValue(r, -1 * cal.getX(shortestDist));
					}
					r++;
				}


			}

		}catch (Exception e) {
			e.printStackTrace();
		}
		progress.setProgress("Success. ", -1, -1);

		int col = newTable.getColumnIndex("Grayscale Value");
		ImagePhantom pi = new ImagePhantom(this.ic.getImgFile(), this.ic.getTotalImageTitle(), this.gui, false, null);
		pi.open();
		ZProjector projector = new ZProjector();
		projector.setImage(pi.getIC().getImageChannel(chan, false));
		pi = null;
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.doProjection();
		progress.setProgress("Recording grayscale values...", -1, -1);
		ImageProcessor ip = projector.getProjection().getProcessor();
		System.out.println(newTable.getLastColumn());

		for (int i = 0; i < xObjValues.length; i++) {

			newTable.setValue(col, i, ip.getPixelValue((int) xObjValues[i], (int) yObjValues[i]) );

		}

		progress.setProgress("Done.", -1, -1);

		System.gc();

		return newTable;


	}



}
