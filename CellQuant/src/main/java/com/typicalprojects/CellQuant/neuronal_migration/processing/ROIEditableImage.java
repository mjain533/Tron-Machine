package com.typicalprojects.CellQuant.neuronal_migration.processing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

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
	
	public boolean hasCreatedValidROIPoints() {
		if (this.points.size() > 1) 
			return true;
		else
			return false;
				
		
	}


	public BufferedImage getPaintedCopy(Channel channelToDrawROI) {

		if (this.points.isEmpty()) {

			return this.ic.getImageChannel(channelToDrawROI, false).getProcessor().convertToRGB().getBufferedImage();

		}

		Object[] obj = convertPointsToArray();

		if (points.size() == 1) {
			
			ImagePlus dup = new ImagePlus("dup", this.ic.getImageChannel(channelToDrawROI, true).getProcessor().convertToRGB());
			
		
			dup.getProcessor().setColor(Color.GREEN);
			int size = (int) (Math.max(dup.getDimensions()[0], dup.getDimensions()[1] ) / 100.0);
			dup.getProcessor().fillRect((int) ((float[]) obj[0])[0] - (size / 2), (int) ((float[]) obj[1])[0] - (size / 2), size, size);
			dup.updateImage();
			return dup.getBufferedImage();

		} else {
			ImagePlus dup = new ImagePlus("Dup", this.ic.getImageChannel(channelToDrawROI, true).getProcessor().convertToRGB());

			PolygonRoi pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], points.size(), Roi.POLYLINE) ;
			pgr.setStrokeColor(Color.GREEN);
			pgr.setFillColor(Color.GREEN);
			double size = Math.max(dup.getDimensions()[0], dup.getDimensions()[1] );

			pgr.setStrokeWidth(size / 300.0);
			if (points.size() > 2) {
				pgr.fitSplineForStraightening();;
			}


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
		
		
		
		Point newStart = getStretchedPoint(this.points.get(0), drawImage);
		
		if (newStart != null) {
			this.points.add(0, newStart);
		}
		
		Point newEnd = getStretchedPoint(this.points.get(this.points.size() - 1), drawImage);
		
		if (newEnd != null) {
			this.points.add(newEnd);
		}

		Object[] obj = convertPointsToArray();

		PolygonRoi pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], points.size(), Roi.POLYLINE) ;
		pgr.setStrokeColor(Color.GREEN);
		pgr.setFillColor(Color.GREEN);
		double size = Math.max(drawImage.getDimensions()[0], drawImage.getDimensions()[1] );

		pgr.setStrokeWidth(size / 300.0);
		System.out.println("SPLINED");
		pgr.fitSplineForStraightening();
		pgr.setName(name);
		this.roi_names.add(name);
		this.roi_polygons.add(pgr);
		this.points.clear();

		return true;
	}

	public void clearPoints() {
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


				roiImg.setProcessor(roiImg.getProcessor().convertToColorProcessor());
				ImageProcessor ip = roiImg.getProcessor();
				ip.setLineWidth(3);
				ip.setFont(new Font("Arial", Font.BOLD, 20));

				for (int i = 0; i < this.roi_names.size(); i++) {
					String name = this.roi_names.get(i);
					PolygonRoi roi = this.roi_polygons.get(i);
					roi.setFillColor(Color.GREEN);
					
					roi.setStrokeWidth(1000 / 300.0);
					ip.drawOverlay(new Overlay(roi));
					int middleY = Math.max(roi.getPolygon().ypoints[(roi.getPolygon().npoints / 2)] + 3, 10);
					middleY= Math.min(middleY, roiImg.getDimensions()[1] - 4);
					int middleX = roi.getPolygon().xpoints[(roi.getPolygon().npoints / 2)];
					ip.setColor(Color.RED);
					ip.drawString(name, middleX, middleY, Color.BLACK);

				}

				roiImg.updateImage();
				images.add(roiImg);

			} else if (GUI.channelsToProcess.contains(chan)) { // TODO exclude channels earlier
				channels.add(chan);
				images.add(this.ic.getImageChannel(chan, false));
			}
		}


		return new ImageContainer(channels, images, this.ic.getTotalImageTitle(), this.ic.getImgFile(), this.ic.getCalibration(), GUI.outputLocation, GUI.dateString);

	}

	public void saveROIs() {
		for (PolygonRoi roi : this.roi_polygons) {
			RoiEncoder.save(roi, this.ic.getIntermediateFilesDirectory() + File.separator + roi.getName() +".roi");
		}
	}

	public Map<Channel, ResultsTable> process(SynchronizedProgress progress, List<Analyzer.Calculation> calculations) {		

		Map<Channel, ResultsTable> map = new HashMap<Channel, ResultsTable>();
		for (Entry<Channel, ResultsTable> en : this.tables.entrySet()) {
			map.put(en.getKey(), calculateDistances(en.getValue(), en.getKey(), progress, calculations));
		}
		return map;
	}

	@SuppressWarnings("deprecation")
	private ResultsTable calculateDistances(ResultsTable input, Channel chan, SynchronizedProgress progress, List<Analyzer.Calculation> calculations) {

		try {
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
			counter++;

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
						int lowestY = Integer.MAX_VALUE;
						int highestY = Integer.MIN_VALUE;
						for (Entry<Integer, Integer> en : coordinatePts.entrySet()) {
							lowestY = Math.min(lowestY, en.getValue());
							highestY = Math.max(highestY, en.getValue());
							double dist = Math.sqrt(Math.pow(en.getKey() - xObjValues[i], 2) + Math.pow(en.getValue() - yObjValues[i], 2));
							if (dist < shortestDist)
								shortestDist = dist;
						}
						
						Integer yCoordOnROILine = coordinatePts.get((int)xObjValues[i]);
						if (yCoordOnROILine == null) {
							if (lowestY == 0) {
								yCoordOnROILine = lowestY;
							} else {
								yCoordOnROILine = highestY;
							}
						}
						
						if (yObjValues[i] > yCoordOnROILine) {
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
			ImagePhantom pi = new ImagePhantom(this.ic.getImgFile(), this.ic.getTotalImageTitle(), this.gui.getProgressReporter(), null);
			pi.open(GUI.channelMap, GUI.outputLocation, GUI.dateString, true);
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
			
			if (!calculations.isEmpty()) {
				progress.setProgress("Running further calculations...", -1, -1);
				if (calculations.contains(Analyzer.Calculation.PERCENT_MIGRATION) && this.roi_names.size() > 1) {
					
					for (int i = 1; i < this.roi_names.size(); i++) {
						newTable.setHeading(counter, "%migrated " + this.roi_names.get(0) + " to " + this.roi_names.get(i));
						double[] firstLineDist = newTable.getColumnAsDoubles(3);
						double[] secondLineDist = newTable.getColumnAsDoubles(3 + i);
						for (int j = 0; j < firstLineDist.length && j < secondLineDist.length; j++) {
							newTable.setValue(counter, j, Analyzer.calculate(Analyzer.Calculation.PERCENT_MIGRATION, firstLineDist[j], secondLineDist[j]));
						}
						counter++;
					}

				}
			}
			
			progress.setProgress("Done.", -1, -1);
			
			

			System.gc();
			return newTable;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this.gui.getComponent(), "<html>SHOW THIS TO JUSTIN:<br><br> " + StringUtils.join(e.getStackTrace(), "<br>") + "</html>", "Error", JOptionPane.ERROR_MESSAGE);
		}
		

		return null;

	}
	
	public Point getStretchedPoint(Point p, ImagePlus drawImage) {
		
		int distTop = p.y;
		int distBottom = drawImage.getDimensions()[1] - p.y - 1;
		int distLeft = p.x;
		int distRight = drawImage.getDimensions()[0] - p.x - 1;
		
		int min = Math.min(Math.min(distTop, distBottom), Math.min(distLeft, distRight));
		
		if (min != 0) {
			if (min == distTop) {
				return new Point(p.x, 0, false);
			} else if (min == distBottom) {
				return new Point(p.x, drawImage.getDimensions()[1] - 1, false);
			} else if (min == distLeft) {
				return new Point(0, p.y, false);
			} else if (min == distRight) {
				return new Point(drawImage.getDimensions()[0] - 1, p.y, false);
			}
		}
		
		return null;
	}



}
