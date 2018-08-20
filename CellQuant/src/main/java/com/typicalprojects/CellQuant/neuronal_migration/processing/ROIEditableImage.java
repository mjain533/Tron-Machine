package com.typicalprojects.CellQuant.neuronal_migration.processing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import com.typicalprojects.CellQuant.neuronal_migration.GUI;
import com.typicalprojects.CellQuant.neuronal_migration.processing.Custom3DCounter.Column;
import com.typicalprojects.CellQuant.util.ImageContainer;
import com.typicalprojects.CellQuant.util.Point;
import com.typicalprojects.CellQuant.util.PolarizedPolygonROI;
import com.typicalprojects.CellQuant.util.SynchronizedProgress;
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.RoiEncoder;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

public class ROIEditableImage {

	private ImageContainer ic;
	private Map<Channel, ResultsTable> tables;
	private Channel drawChannel;
	private GUI gui;
	//private List<String> roi_names = new ArrayList<String>();
	//private List<PolygonRoi> roi_polygons = new ArrayList<PolygonRoi>();
	private List<PolarizedPolygonROI> rois = new ArrayList<PolarizedPolygonROI>();
	private volatile Map<Channel, BufferedImage> cache = new HashMap<Channel, BufferedImage>();
	private boolean selectingPositive = false;
	
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
		return !this.rois.isEmpty();
	}
	
	public boolean hasCreatedValidROIPoints() {
		if (this.points.size() > 1) 
			return true;
		else
			return false;	
		
	}
	
	public void setSelectingPositive() {
		this.cache.clear();
		this.selectingPositive = true;
	}
	
	public boolean isSelectingPositiveRegion() {
		return this.selectingPositive;
	}

	public synchronized void applyMinMax(Channel channel, int min, int max) {
		this.cache.remove(channel);
		ImagePlus ip = this.ic.getImageChannel(channel, false);
		ip.setDisplayRange(min, max);
		ip.updateImage();
	}

	public BufferedImage getPaintedCopy(Channel channelToDrawROI) {
		if (this.cache.containsKey(channelToDrawROI)) {
			return this.cache.get(channelToDrawROI);
		}

		

		Object[] obj = convertPointsToArray();
		ImagePlus dup = new ImagePlus("dup", this.ic.getImageChannel(channelToDrawROI, /*note: changed to false*/false).getProcessor().convertToRGB());
		
		BufferedImage bi = null;
		
		ImageProcessor ip = dup.getProcessor();
		ip.setLineWidth(3);
		ip.setFont(new Font("Arial", Font.BOLD, 20));

		for (PolarizedPolygonROI roiWrapper : this.rois) {
			
			
			PolygonRoi roi = roiWrapper.get();
			
			roi.setFillColor(Color.GREEN);
			
			roi.setStrokeWidth(1000 / 300.0);
			ip.drawOverlay(new Overlay(roi));
			int middleY = Math.max(roi.getPolygon().ypoints[(roi.getPolygon().npoints / 2)] + 3, 10);
			middleY= Math.min(middleY, dup.getDimensions()[1] - 4);
			int middleX = roi.getPolygon().xpoints[(roi.getPolygon().npoints / 2)];

			
			ip.setColor(Color.GREEN);
			if(roiWrapper.hasPositiveSideBeenSelected()) {
				if (roi.getPolygon().npoints > 12) {
					int polySize = (int) (roi.getPolygon().npoints / 6.0);
					// TODO ensure these are within bounds
					
					Map<Integer, Integer> posNegAboveLabels = new HashMap<Integer, Integer>();
					Map<Integer, Integer> posNegBelowLabels = new HashMap<Integer, Integer>();

					posNegAboveLabels.put(roi.getPolygon().xpoints[polySize], roi.getPolygon().ypoints[polySize]-2);
					posNegAboveLabels.put(roi.getPolygon().xpoints[polySize*2], roi.getPolygon().ypoints[polySize*2]-2);
					posNegAboveLabels.put(roi.getPolygon().xpoints[polySize*4], roi.getPolygon().ypoints[polySize*4]-2);
					posNegAboveLabels.put(roi.getPolygon().xpoints[polySize*5], roi.getPolygon().ypoints[polySize*5]-2);
					posNegBelowLabels.put(roi.getPolygon().xpoints[polySize], roi.getPolygon().ypoints[polySize] + 22);
					posNegBelowLabels.put(roi.getPolygon().xpoints[polySize*2], roi.getPolygon().ypoints[polySize*2] + 22);
					posNegBelowLabels.put(roi.getPolygon().xpoints[polySize*4], roi.getPolygon().ypoints[polySize*4] + 22);
					posNegBelowLabels.put(roi.getPolygon().xpoints[polySize*5], roi.getPolygon().ypoints[polySize*5] + 22);
					for (Entry<Integer, Integer> en : posNegAboveLabels.entrySet()) {
						if (roiWrapper.isPositive(en.getKey(), en.getValue()))
							ip.drawString("+", en.getKey(), en.getValue());
						else
							ip.drawString("–", en.getKey(), en.getValue());
					}
					for (Entry<Integer, Integer> en : posNegBelowLabels.entrySet()) {
						if (roiWrapper.isPositive(en.getKey(), en.getValue()))
							ip.drawString("+", en.getKey(), en.getValue());
						else
							ip.drawString("–", en.getKey(), en.getValue());
					}


				}
			}
			
			ip.setColor(Color.RED);

			ip.drawString(roiWrapper.getName(), middleX, middleY, Color.BLACK);

		}

		dup.updateImage();
		
		if (this.selectingPositive) {
			ImageRoi whiteOverlay = new ImageRoi(0, 0, ip.duplicate());
			whiteOverlay.getProcessor().setColor(Color.WHITE);
			whiteOverlay.getProcessor().fillRect(0, 0, ip.getWidth(), ip.getHeight());
			whiteOverlay.setZeroTransparent(true);
			whiteOverlay.setOpacity(0.5);
			ip.drawOverlay(new Overlay(whiteOverlay));
			dup.updateImage();
		}
		
		if (this.points.isEmpty()) {
			bi = dup.getBufferedImage();
			this.cache.put(channelToDrawROI, bi);
			return bi;
		}
		
		//
		
		
		//ip.drawRect(0, 0, ip.getWidth(), ip.getHeight());
		//
		
		if (points.size() == 1) {
			
			
		
			ip.setColor(Color.GREEN);
			int size = (int) (Math.max(dup.getDimensions()[0], dup.getDimensions()[1] ) / 100.0);
			ip.fillRect((int) ((float[]) obj[0])[0] - (size / 2), (int) ((float[]) obj[1])[0] - (size / 2), size, size);
			dup.updateImage();
			bi = dup.getBufferedImage();

		} else {

			PolygonRoi pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], points.size(), Roi.POLYLINE) ;
			pgr.setStrokeColor(Color.GREEN);
			pgr.setFillColor(Color.GREEN);
			double size = Math.max(dup.getDimensions()[0], dup.getDimensions()[1] );

			pgr.setStrokeWidth(size / 300.0);
			if (points.size() > 2) {
				pgr.fitSplineForStraightening();;
			}


			ip.setColor(Color.GREEN);
			ip.drawOverlay(new Overlay(pgr));
			dup.updateImage();
			bi = dup.getBufferedImage();


		}
		
		this.cache.put(channelToDrawROI, bi);
		return bi;


	}

	public void addPoint(Point p) {
		this.cache.clear();
		this.points.add(p);
		
	}

	public boolean convertSelectionToRoi(String name) {
		if (this.points.size() < 2) {
			return false;
		}
		
		for (PolarizedPolygonROI wrapper : this.rois) {
			if (wrapper.getName().equals(name))
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
		double imgSize = Math.max(drawImage.getDimensions()[0], drawImage.getDimensions()[1] );

		pgr.setStrokeWidth(imgSize / 300.0);
		pgr.fitSplineForStraightening();
		
		pgr.setName(name);
		Polygon polygon = pgr.getPolygon();
		int[] newxs = new int[polygon.npoints + 2];
		System.arraycopy(polygon.xpoints, 0, newxs, 1, polygon.npoints);
		int[] newys = new int[polygon.npoints + 2];
		System.arraycopy(polygon.ypoints, 0, newys, 1, polygon.npoints);
		newxs[0] = newStart.x;
		newxs[newxs.length - 1] = newEnd.x;
		newys[0] = newStart.y;
		newys[newys.length - 1] = newEnd.y;


		pgr = new PolygonRoi(newxs, newys, polygon.npoints + 2, Roi.POLYLINE);
		polygon = pgr.getPolygon();
		this.rois.add(new PolarizedPolygonROI(name, pgr, createHalfRegion(polygon.xpoints, polygon.ypoints, new Point(0, 0, false), new Point(0, drawImage.getHeight() - 1, false), new Point(drawImage.getWidth() - 1, drawImage.getHeight() - 1, false), new Point(drawImage.getWidth() - 1, 0, false))));

		this.points.clear();
		this.cache.clear();

		return true;
	}

	public void clearPoints() {
		this.cache.clear();
		this.points.clear();
	}

	public void removeROI(String name) {
		this.cache.clear();
		Iterator<PolarizedPolygonROI> itr = this.rois.iterator();
		while (itr.hasNext()) {
			if (itr.next().getName().equals(name)) {
				itr.remove();
				break;
			}
		}
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

				for (PolarizedPolygonROI roiWrapper : this.rois) {
					PolygonRoi roi = roiWrapper.get();
					roi.setFillColor(Color.GREEN);
					
					roi.setStrokeWidth(1000 / 300.0);
					ip.drawOverlay(new Overlay(roi));
					int middleY = Math.max(roi.getPolygon().ypoints[(roi.getPolygon().npoints / 2)] + 3, 10);
					middleY= Math.min(middleY, roiImg.getDimensions()[1] - 4);
					int middleX = roi.getPolygon().xpoints[(roi.getPolygon().npoints / 2)];
					ip.setColor(Color.RED);
					ip.drawString(roiWrapper.getName(), middleX, middleY, Color.BLACK);
					ip.setColor(Color.GREEN);
					if (roi.getPolygon().npoints > 12) {
						int polySize = (int) (roi.getPolygon().npoints / 6.0);
						// TODO ensure these are within bounds
						
						Map<Integer, Integer> posNegAboveLabels = new HashMap<Integer, Integer>();
						Map<Integer, Integer> posNegBelowLabels = new HashMap<Integer, Integer>();

						posNegAboveLabels.put(roi.getPolygon().xpoints[polySize], roi.getPolygon().ypoints[polySize]-2);
						posNegAboveLabels.put(roi.getPolygon().xpoints[polySize*2], roi.getPolygon().ypoints[polySize*2]-2);
						posNegAboveLabels.put(roi.getPolygon().xpoints[polySize*4], roi.getPolygon().ypoints[polySize*4]-2);
						posNegAboveLabels.put(roi.getPolygon().xpoints[polySize*5], roi.getPolygon().ypoints[polySize*5]-2);
						posNegBelowLabels.put(roi.getPolygon().xpoints[polySize], roi.getPolygon().ypoints[polySize] + 22);
						posNegBelowLabels.put(roi.getPolygon().xpoints[polySize*2], roi.getPolygon().ypoints[polySize*2] + 22);
						posNegBelowLabels.put(roi.getPolygon().xpoints[polySize*4], roi.getPolygon().ypoints[polySize*4] + 22);
						posNegBelowLabels.put(roi.getPolygon().xpoints[polySize*5], roi.getPolygon().ypoints[polySize*5] + 22);
						for (Entry<Integer, Integer> en : posNegAboveLabels.entrySet()) {
							if (roiWrapper.isPositive(en.getKey(), en.getValue()))
								ip.drawString("+", en.getKey(), en.getValue());
							else
								ip.drawString("–", en.getKey(), en.getValue());
						}
						for (Entry<Integer, Integer> en : posNegBelowLabels.entrySet()) {
							if (roiWrapper.isPositive(en.getKey(), en.getValue()))
								ip.drawString("+", en.getKey(), en.getValue());
							else
								ip.drawString("–", en.getKey(), en.getValue());
						}

					}

				}
				
				roiImg.updateImage();
				images.add(roiImg);

			} else if (GUI.channelsToProcess.contains(chan)) { // TODO exclude channels earlier
				channels.add(chan);
				images.add(this.ic.getImageChannel(chan, false));
			}
		}


		return new ImageContainer(channels, images, null, this.ic.getTotalImageTitle(), this.ic.getImgFile(), this.ic.getCalibration(), GUI.outputLocation, GUI.dateString);

	}

	public void saveROIs() {
		for (PolarizedPolygonROI roi : this.rois) {
			RoiEncoder.save(roi.get(), this.ic.getIntermediateFilesDirectory() + File.separator + roi.getName() +".roi");
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


			for (PolarizedPolygonROI roiWrapper : this.rois) {
				newTable.setHeading(counter, "Distance from " + roiWrapper.getName() + " (" + cal.getUnits() +")");
				Map<Integer, Integer> pts = new HashMap<Integer, Integer>();

				Polygon p = roiWrapper.get().getPolygon();

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
					for (PolarizedPolygonROI roi : this.rois) {

						double shortestDist = Double.MAX_VALUE;
						int lowestY = Integer.MAX_VALUE;
						int highestY = Integer.MIN_VALUE;
						Map<Integer, Integer> coordinatePts = roi.getPointsOnLine();
						for (Entry<Integer, Integer> en : coordinatePts.entrySet()) {
							lowestY = Math.min(lowestY, en.getValue());
							highestY = Math.max(highestY, en.getValue());
							double dist = Math.sqrt(Math.pow(en.getKey() - xObjValues[i], 2) + Math.pow(en.getValue() - yObjValues[i], 2));
							if (dist < shortestDist)
								shortestDist = dist;
						}
						

						
						if (roi.isPositive((int) xObjValues[i], (int) yObjValues[i])) {
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
			/*ImagePhantom pi = new ImagePhantom(this.ic.getImgFile(), this.ic.getTotalImageTitle(), this.gui.getProgressReporter(), null);
			pi.open(GUI.channelMap, GUI.outputLocation, GUI.dateString, false);
			ZProjector projector = new ZProjector();
			projector.setImage(
					pi.getIC().getImageChannel(chan, false));
			projector.setMethod(ZProjector.MAX_METHOD);
			projector.doProjection();*/
			progress.setProgress("Recording grayscale values...", -1, -1);
			/*ImageProcessor ip = projector.getProjection().getProcessor();*/
			ImageProcessor ip = this.ic.getSupplementalImage(
					this.ic.getImageChannel(chan, false).getTitle()
					+ " " +
							NeuronProcessor.SUPP_LBL_ORIGINAL)
					.getProcessor();
			for (int i = 0; i < xObjValues.length; i++) {
				newTable.setValue(col, i, ip.getPixelValue((int) xObjValues[i], (int) yObjValues[i]) );

			}
			
			if (!calculations.isEmpty()) {
				progress.setProgress("Running further calculations...", -1, -1);
				if (calculations.contains(Analyzer.Calculation.PERCENT_MIGRATION) && this.rois.size() > 1) {
					
					for (int i = 1; i < this.rois.size(); i++) {
						newTable.setHeading(counter, "%migrated " + this.rois.get(0).getName() + " to " + this.rois.get(i).getName());
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
			e.printStackTrace();
			JOptionPane.showMessageDialog(this.gui.getComponent(), "<html>SHOW THIS TO JUSTIN:<br><br> " + StringUtils.join(e.getStackTrace(), "<br>") + "</html>", "Error", JOptionPane.ERROR_MESSAGE);
		}
		

		return null;

	}
	
	public Point getStretchedPoint(Point p, ImagePlus drawImage) {
		
		int distTop = p.y;
		int distBottom = drawImage.getDimensions()[1] - p.y + 1;
		int distLeft = p.x;
		int distRight = drawImage.getDimensions()[0] - p.x + 1;
		
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
	
	public String selectPositiveRegionForCurrentROI(Point p) {
		this.selectingPositive = false;
		this.cache.clear();
		PolarizedPolygonROI roiPolygon = this.rois.get(this.rois.size() - 1);
		roiPolygon.setPositiveRegion(p.x, p.y);
		return roiPolygon.getName();
	}

	private PolygonRoi createHalfRegion(int[] xPts, int[] yPts, Point upperLeft, Point lowerLeft, Point lowerRight, Point upperRight) {
		
		
		List<Integer> xPtsAdd = new ArrayList<Integer>();
		List<Integer> yPtsAdd = new ArrayList<Integer>();
		xPtsAdd.addAll(Arrays.stream(xPts).boxed().collect(Collectors.toList()));
		yPtsAdd.addAll(Arrays.stream(yPts).boxed().collect(Collectors.toList()));
		Point stopPoint = new Point(xPts[0], yPts[0], null);
		// start at end pt, find closest corner
		// If start pt is closer, go to it and be done. Otherwise add that corner.
		// Detect which direction we are going, and then determine which way through the list we should iterate.
		// Go to next corner, but it start point is closer, then just go to that.
		
		
		boolean continueAdding = true;
		Point currentPoint = new Point(xPts[xPts.length - 1], yPts[yPts.length - 1], null);
		while (continueAdding) {
			Point nextP = _addAnotherPoint(stopPoint, currentPoint, upperLeft, lowerLeft, lowerRight, upperRight);
			if (nextP == null) {
				xPtsAdd.add(stopPoint.x);
				yPtsAdd.add(stopPoint.y);
				continueAdding = false;
			} else {
				xPtsAdd.add(nextP.x);
				yPtsAdd.add(nextP.y);
				currentPoint = nextP;
			}
			
		}
		
		


		return new PolygonRoi(_convertToPrimFloatArray(xPtsAdd), _convertToPrimFloatArray(yPtsAdd), xPtsAdd.size(), Roi.POLYLINE) ;

		
	}
	

	
	private Point _addAnotherPoint(Point stopPoint, Point currentPoint, Point upperLeft, Point lowerLeft, Point lowerRight, Point upperRight) {
		
		if (currentPoint.equals(lowerLeft)) {
			if (stopPoint.y == lowerRight.y)
				return null;
			
			return lowerRight;
		} else if (currentPoint.equals(lowerRight)) {
			if (stopPoint.x == upperRight.x)
				return null;

			return upperRight;
		} else if (currentPoint.equals(upperRight)) {
			if (stopPoint.y == upperLeft.y)
				return null;

			return upperLeft;
		} else if (currentPoint.equals(upperLeft)) {
			if (stopPoint.x == lowerLeft.x)
				return null;

			return lowerLeft;
		} else {
			
			
			if (currentPoint.x == 0) {
				if (stopPoint.x == 0 && stopPoint.y > currentPoint.y) {
					return null;
				}
				return lowerLeft;
			} else if (currentPoint.y == 0) {
				if (stopPoint.y == 0 && stopPoint.x < currentPoint.x) {
					return null;
				}
				return upperLeft;
			} else if (currentPoint.x == upperRight.x) {
				if (stopPoint.x == upperRight.x && stopPoint.y < currentPoint.y) {
					return null;
				}
				return upperRight;
			} else {
				if (stopPoint.y == lowerRight.y && stopPoint.x > currentPoint.x) {
					return null;
				}
				return lowerRight;
			}
			
		}
		
		
	}
	
	public PolarizedPolygonROI getFirstROI() {
		return this.rois.get(0);
	}
	
	private float[] _convertToPrimFloatArray(List<Integer> integers) {
		Integer nonprimitiveResult[] = integers.toArray( new Integer[integers.size()]);  
		float[] primitiveResult = new float[integers.size()];
		for(int i = 0; i < integers.size(); ++i) {
		    primitiveResult[i] = (float) nonprimitiveResult[i];
		}
		return primitiveResult;
	}

}
