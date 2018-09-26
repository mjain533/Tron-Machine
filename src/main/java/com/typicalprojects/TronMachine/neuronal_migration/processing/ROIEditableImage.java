package com.typicalprojects.TronMachine.neuronal_migration.processing;

import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.swing.JOptionPane;

import org.apache.commons.lang.StringUtils;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.processing.Custom3DCounter.Column;
import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.Logger;
import com.typicalprojects.TronMachine.util.Point;
import com.typicalprojects.TronMachine.util.PolarizedPolygonROI;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;
import com.typicalprojects.TronMachine.util.ImageContainer.ImageTag;

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
	private Map<String, ResultsTable> tables;
	private Channel drawChannel;
	private GUI gui;
	//private List<String> roi_names = new ArrayList<String>();
	//private List<PolygonRoi> rdoi_polygons = new ArrayList<PolygonRoi>();
	private List<PolarizedPolygonROI> rois = new ArrayList<PolarizedPolygonROI>();
	private volatile Map<Channel, BufferedImage> cache = new HashMap<Channel, BufferedImage>();
	private boolean selectingPositive = false;

	private List<Point> points = new ArrayList<Point>();

	public ROIEditableImage(ImageContainer ic, Channel drawChannel, Map<String, ResultsTable> tables, GUI gui) {
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
		ImagePlus ip = this.ic.getImage(ImageTag.MaxProjected, channel, false);
		ip.setDisplayRange(min, max);
		ip.updateImage();
	}

	public BufferedImage getPaintedCopy(Channel channelToDrawROI) {
		if (this.cache.containsKey(channelToDrawROI)) {
			return this.cache.get(channelToDrawROI);
		}



		Object[] obj = convertPointsToArray();
		ImagePlus dup = null;
		if (GUI.settings.channelsToProcess.contains(channelToDrawROI)) {
			dup = new ImagePlus("dup", 
					this.
					ic.
					getImage(ImageTag.Objects, channelToDrawROI, /*note: changed to false*/false)
					.getProcessor()
					.convertToRGB());
		} else {
			dup = new ImagePlus("dup", this.ic.getImage(ImageTag.MaxProjected, channelToDrawROI, /*note: changed to false*/false).getProcessor().convertToRGB());
		}

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

					Map<Integer, int[]> posNegLabels = new HashMap<Integer, int[]>();
					posNegLabels.put(polySize, this._getOrthogonalPointsForDrawString(roi.getPolygon(), polySize, 15));
					posNegLabels.put(polySize*2, this._getOrthogonalPointsForDrawString(roi.getPolygon(), polySize*2, 15));
					posNegLabels.put(polySize*4, this._getOrthogonalPointsForDrawString(roi.getPolygon(), polySize*4, 15));
					posNegLabels.put(polySize*5, this._getOrthogonalPointsForDrawString(roi.getPolygon(), polySize*5, 15));

					for (Entry<Integer, int[]> en : posNegLabels.entrySet()) {

						int[] val = en.getValue();
						if (val == null)
							continue;

						try {
							if (roiWrapper.isPositive(val[0], val[1])) {

								ip.drawString("+", val[0]-6, val[1]+13);
								ip.drawString("–", val[2]-5, val[3]+11);
							} else {
								ip.drawString("–", val[0]-5, val[1]+11);
								ip.drawString("+", val[2]-6, val[3]+13);

							}
						} catch (Exception e) {
							// out of bounds;
							continue;
						}
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
		System.out.println("test");
		if (this.points.size() < 2) {
			return false;
		}

		for (PolarizedPolygonROI wrapper : this.rois) {
			if (wrapper.getName().equals(name))
				return false;
		}

		ImagePlus drawImage = this.ic.getImage(ImageTag.MaxProjected, this.drawChannel, false);


		Point newStart = getStretchedPoint(this.points.get(0), drawImage.getDimensions());
		System.out.println("test2");
		if (newStart != null) {
			System.out.println("New start: " + newStart.x + ", " + newStart.y);
			this.points.add(0, newStart);
		}

		Point newEnd = getStretchedPoint(this.points.get(this.points.size() - 1), drawImage.getDimensions());

		if (newEnd != null) {
			System.out.println("New end: " + newEnd.x + ", " + newEnd.y);

			this.points.add(newEnd);
		}

		Object[] obj = convertPointsToArray();
		
		PolygonRoi pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], points.size(), Roi.POLYLINE) ;

		pgr.setStrokeColor(Color.GREEN);
		pgr.setFillColor(Color.GREEN);
		double imgSize = Math.max(drawImage.getDimensions()[0], drawImage.getDimensions()[1] );

		pgr.setStrokeWidth(imgSize / 300.0);
		pgr.fitSplineForStraightening();
		System.out.println("test4");

		if (newStart == null) {
			newStart = this.points.get(0);
		}
		
		if (newEnd == null) {
			newEnd = this.points.get(this.points.size() - 1);
		}
		
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
		System.out.println("test5");
		this.rois.add(new PolarizedPolygonROI(name, pgr, createHalfRegion(polygon.xpoints, polygon.ypoints, new Point(0, 0, false), new Point(0, drawImage.getHeight() - 1, false), new Point(drawImage.getWidth() - 1, drawImage.getHeight() - 1, false), new Point(drawImage.getWidth() - 1, 0, false))));

		this.points.clear();
		this.cache.clear();
		System.out.println("test6");

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

	public void createAndSaveNewImage() {



		ImagePlus roiImg = this.ic.getImage(ImageTag.MaxProjected, GUI.settings.channelForROIDraw, true);


		roiImg.setProcessor(roiImg.getProcessor().convertToColorProcessor());
		ImageProcessor ip = roiImg.getProcessor();
		ip.setLineWidth(3);
		ip.setFont(new Font("Arial", Font.BOLD, 20));

		for (PolarizedPolygonROI roiWrapper : this.rois) {
			PolygonRoi roi = roiWrapper.get();
			roi.setFillColor(Color.GREEN);

			double size = Math.max(roiImg.getDimensions()[0], roiImg.getDimensions()[1] );

			roi.setStrokeWidth(size / 300.0);
			ip.drawOverlay(new Overlay(roi));
			int middleY = Math.max(roi.getPolygon().ypoints[(roi.getPolygon().npoints / 2)] + 3, 10);
			middleY= Math.min(middleY, roiImg.getDimensions()[1] - 4);
			int middleX = roi.getPolygon().xpoints[(roi.getPolygon().npoints / 2)];
			ip.setColor(Color.RED);
			ip.drawString(roiWrapper.getName(), middleX, middleY, Color.BLACK);
			ip.setColor(Color.GREEN);
			if (roi.getPolygon().npoints > 12) {
				int polySize = (int) (roi.getPolygon().npoints / 6.0);

				Map<Integer, int[]> posNegLabels = new HashMap<Integer, int[]>();
				posNegLabels.put(polySize, this._getOrthogonalPointsForDrawString(roi.getPolygon(), polySize, 15));
				posNegLabels.put(polySize*2, this._getOrthogonalPointsForDrawString(roi.getPolygon(), polySize*2, 15));
				posNegLabels.put(polySize*4, this._getOrthogonalPointsForDrawString(roi.getPolygon(), polySize*4, 15));
				posNegLabels.put(polySize*5, this._getOrthogonalPointsForDrawString(roi.getPolygon(), polySize*5, 15));

				for (Entry<Integer, int[]> en : posNegLabels.entrySet()) {

					int[] val = en.getValue();
					if (val == null)
						continue;

					try {
						if (roiWrapper.isPositive(val[0], val[1])) {

							ip.drawString("+", val[0]-6, val[1]+13);
							ip.drawString("–", val[2]-5, val[3]+11);
						} else {
							ip.drawString("–", val[0]-5, val[1]+11);
							ip.drawString("+", val[2]-6, val[3]+13);

						}
					} catch (Exception e) {
						// out of bounds;
						continue;
					}
				}

			}

		}

		roiImg.updateImage();

		this.ic.saveSupplementalImage(ImageTag.Rois, roiImg, GUI.settings.channelForROIDraw);


	}

	public void saveROIs() {
		for (PolarizedPolygonROI roi : this.rois) {
			RoiEncoder.save(roi.get(), this.ic.getIntermediateFilesDirectory() + File.separator + roi.getName() +".roi");
		}
	}

	public Map<String, ResultsTable> processDistances(Logger progress, List<Analyzer.Calculation> calculations) {		

		Map<String, ResultsTable> map = new HashMap<String, ResultsTable>();
		for (Entry<String, ResultsTable> en : this.tables.entrySet()) {
			map.put(en.getKey(), calculateDistances(en.getValue(), Channel.parse(en.getKey()), progress, calculations));
		}

		if (GUI.settings.calculateBins) {
			ResultsTable rt = processBins(progress);
			if (rt != null) {
				map.put("BINS", rt);
			}
		}

		return map;
	}

	@SuppressWarnings("deprecation")
	public ResultsTable processBins(Logger logger) {

		if (this.rois.size() <= 1) {
			return null;
		}
		BinnedRegionMod binnedRegion = null;
		try {
			binnedRegion = new BinnedRegionMod(this.rois.get(0), this.rois.get(this.rois.size() - 1), GUI.settings.numberOfBins, 2, this.ic.getDimensions(), gui.getLogger());


		} catch (BinnedRegionMod.MalformedBinException e1) {
			JOptionPane.showMessageDialog(this.gui.getComponent(), "There was an error processing bins. This is likely due to misshapen bin lines. Bin data will be neglected.", "Bin error", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		logger.setCurrentTask("Copying bins onto images... ");
		for (Channel chan : GUI.settings.channelToDrawBin) {
			ImagePlus imgToDrawBinLines = null;

			if (GUI.settings.channelsToProcess.contains(chan) && !GUI.settings.channelForROIDraw.equals(chan)) {
				imgToDrawBinLines = this.ic.getImage(ImageTag.Objects, chan, true);
			} else {
				imgToDrawBinLines = this.ic.getImage(ImageTag.MaxProjected, chan, true);
			}
			imgToDrawBinLines.setProcessor(imgToDrawBinLines.getProcessor().convertToColorProcessor());
			binnedRegion.drawBinLines(imgToDrawBinLines.getProcessor());
			imgToDrawBinLines.updateImage();
			this.ic.saveSupplementalImage(ImageTag.Bins, imgToDrawBinLines, chan);
		}
		logger.setCurrentTaskComplete();

		LinkedHashMap<Integer, Map<Channel, Integer>> pointsBinned = new LinkedHashMap<Integer, Map<Channel, Integer>>();

		for (int i = 1; i <= GUI.settings.numberOfBins; i++) {
			pointsBinned.put(i, new HashMap<Channel, Integer>());
			for (Channel chan : GUI.settings.channelsToProcess) {
				pointsBinned.get(i).put(chan, 0);
			}
		}
		pointsBinned.put(-1, new HashMap<Channel, Integer>());
		for (Channel chan : GUI.settings.channelsToProcess) {
			pointsBinned.get(-1).put(chan, 0);
		}

		ResultsTable rt = new ResultsTable();
		rt.setHeading(0, "Bin");
		int counter = 1;
		for (Channel chan : GUI.settings.channelsToProcess) {
			rt.setHeading(counter, chan.toReadableString() + " Cells");
			counter++;
		}

		logger.setCurrentTask("Binning points... ");

		for (Channel chan : GUI.settings.channelsToProcess) {
			ResultsTable input = this.tables.get(chan.name());
			if (input == null) {
				continue;
			}
			int xIndex = input.getColumnIndex(Column.X.getTitle());
			int yIndex = input.getColumnIndex(Column.Y.getTitle());
			if (xIndex == ResultsTable.COLUMN_NOT_FOUND || yIndex == ResultsTable.COLUMN_NOT_FOUND) {
				continue;
			}
			float[] xObjValues = input.getColumn(input.getColumnIndex(Column.X.getTitle()));
			float[] yObjValues = input.getColumn(input.getColumnIndex(Column.Y.getTitle()));

			for (int i =0; i < xObjValues.length && i < yObjValues.length; i++) {
				int bin = binnedRegion.getEnclosingBin((int) xObjValues[i], (int) yObjValues[i]);
				Map<Channel, Integer> channelBinPtCount = pointsBinned.get(bin);
				channelBinPtCount.put(chan, channelBinPtCount.get(chan) + 1);
			}

		}


		for (Entry<Integer, Map<Channel, Integer>> binCounts : pointsBinned.entrySet()) {
			if (binCounts.getKey() != -1 || !GUI.settings.excludePtsOutsideBin) {
				rt.incrementCounter();
				rt.addValue(0, binCounts.getKey());
				counter = 1;
				for (Channel chan : GUI.settings.channelsToProcess) {
					rt.addValue(counter, binCounts.getValue().get(chan));
					counter++;
				}
			}

		}
		logger.setCurrentTaskComplete();

		return rt;
		



	}


	@SuppressWarnings("deprecation")
	private ResultsTable calculateDistances(ResultsTable input, Channel chan, Logger progress, List<Analyzer.Calculation> calculations) {

		try {
			int idIndex = input.getColumnIndex("ID");
			int xIndex = input.getColumnIndex(Column.X.getTitle());
			int yIndex = input.getColumnIndex(Column.Y.getTitle());
			if (idIndex == ResultsTable.COLUMN_NOT_FOUND || xIndex == ResultsTable.COLUMN_NOT_FOUND || yIndex == ResultsTable.COLUMN_NOT_FOUND) {
				ResultsTable newTable = new ResultsTable();
				newTable.setHeading(0, "Object Num");
				newTable.setHeading(1, "X (pixels)");
				newTable.setHeading(2, "Y (pixels)");
				return newTable;
			}

			float[] ids = input.getColumn(input.getColumnIndex("ID"));
			float[] xObjValues = input.getColumn(input.getColumnIndex(Column.X.getTitle()));
			float[] yObjValues = input.getColumn(input.getColumnIndex(Column.Y.getTitle()));

			progress.setCurrentTask("Cleaning Data...");
			ResultsTable newTable = new ResultsTable();
			Calibration calib = this.ic.getCalibration();
			String stringCalib = GUI.settings.calibrations.get(GUI.settings.calibrationNumber - 1);
			stringCalib = stringCalib.substring(stringCalib.lastIndexOf('('));
			String[] number = stringCalib.substring(stringCalib.indexOf(":") + 2, stringCalib.lastIndexOf(')')).split(" ", 2);
			Double converter = Double.parseDouble(number[0]);
			String units = calib == null ? number[1] : calib.getUnits();

			newTable.setHeading(0, "Object Num");
			newTable.setHeading(1, "X (pixels)");
			newTable.setHeading(2, "Y (pixels)");

			int counter = 3;
			List<Map<Integer, Integer>> coords = new ArrayList<Map<Integer, Integer>>();


			

			for (PolarizedPolygonROI roiWrapper : this.rois) {
				newTable.setHeading(counter, "Distance from " + roiWrapper.getName() + " (" + units +")");
				Map<Integer, Integer> pts = new HashMap<Integer, Integer>();

				Polygon p = roiWrapper.get().getPolygon();

				for (int polygonPt = 0; polygonPt < p.npoints; polygonPt ++) {
					pts.put(p.xpoints[polygonPt], p.ypoints[polygonPt]);
				}

				coords.add(pts);

				counter++;

			}
			progress.setCurrentTaskComplete();

			newTable.setHeading(counter, "Grayscale Value");
			counter++;

			try {
				progress.setCurrentTask("Measuring distance to ROIs...");
				for (int i = 0; i < xObjValues.length; i++) {
					progress.setCurrentTaskProgress(i + 1, xObjValues.length);
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
							if (calib != null) {
								newTable.addValue(r, calib.getX(shortestDist)); // could be X or Y, just for conversion

							} else {
								newTable.addValue(r, shortestDist * converter); // could be X or Y, just for conversion

							}
						} else {
							if (calib != null) {
								newTable.addValue(r, -1 * calib.getX(shortestDist));

							} else {
								newTable.addValue(r, -1 * shortestDist * converter);

							}
						}
						r++;
					}


				}

			}catch (Exception e) {
				e.printStackTrace();
			}
			progress.setCurrentTaskComplete();

			int col = newTable.getColumnIndex("Grayscale Value");
			/*ImagePhantom pi = new ImagePhantom(this.ic.getImgFile(), this.ic.getTotalImageTitle(), this.gui.getProgressReporter(), null);
			pi.open(GUI.channelMap, GUI.outputLocation, GUI.dateString, false);
			ZProjector projector = new ZProjector();
			projector.setImage(
					pi.getIC().getImageChannel(chan, false));
			projector.setMethod(ZProjector.MAX_METHOD);
			projector.doProjection();*/
			progress.setCurrentTask("Recording grayscale values...");
			/*ImageProcessor ip = projector.getProjection().getProcessor();*/
			ImageProcessor ip = this.ic.getImage(ImageTag.MaxProjected, chan, false)
					.getProcessor();

			for (int i = 0; i < xObjValues.length; i++) {
				progress.setCurrentTaskProgress(i + 1, xObjValues.length);
				newTable.setValue(col, i, ip.getPixelValue((int) xObjValues[i], (int) yObjValues[i]) );

			}
			progress.setCurrentTaskComplete();

			if (!calculations.isEmpty()) {
				progress.setCurrentTask("Running further calculations...");
				if (calculations.contains(Analyzer.Calculation.PERCENT_MIGRATION) && this.rois.size() > 1) {

					for (int i = 1; i < this.rois.size(); i++) {
						progress.setCurrentTaskProgress(i, this.rois.size() - 1);
						newTable.setHeading(counter, "%migrated " + this.rois.get(0).getName() + " to " + this.rois.get(i).getName());
						double[] firstLineDist = newTable.getColumnAsDoubles(3);
						double[] secondLineDist = newTable.getColumnAsDoubles(3 + i);
						for (int j = 0; j < firstLineDist.length && j < secondLineDist.length; j++) {
							newTable.setValue(counter, j, Analyzer.calculate(Analyzer.Calculation.PERCENT_MIGRATION, firstLineDist[j], secondLineDist[j]));
						}
						counter++;
					}

				}
				progress.setCurrentTaskComplete();
			}


			System.gc();
			return newTable;
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this.gui.getComponent(), "<html>SHOW THIS TO JUSTIN:<br><br> " + StringUtils.join(e.getStackTrace(), "<br>") + "</html>", "Error", JOptionPane.ERROR_MESSAGE);
		}


		return null;

	}

	public static Point getStretchedPoint(Point p, int[] dimensions) {

		System.out.println("Dimensions: " + dimensions[0] + "," + dimensions[1]);
		int distTop = p.y;
		int distBottom = dimensions[1] - p.y - 1;
		int distLeft = p.x;
		int distRight = dimensions[0] - p.x - 1;

		int min = Math.min(Math.min(distTop, distBottom), Math.min(distLeft, distRight));

		if (min != 0) {
			System.out.println("Not zero to wall");
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
		System.out.println("stop point: " + stopPoint.x + "," + stopPoint.y);
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

		System.out.println("reached");


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



	private int[] _getOrthogonalPointsForDrawString(Polygon polygon, int pos, double distance) {

		int minPos = Math.max(0, pos - 5);
		int maxPos = Math.min(polygon.npoints - 1, pos + 5);

		if (minPos == maxPos)
			return null;

		double x1 = polygon.xpoints[minPos];
		double x2 = polygon.xpoints[maxPos];
		double y1 = polygon.ypoints[minPos];
		double y2 = polygon.ypoints[maxPos];

		double midx = (x1 + x2) / 2.0;
		double midy = (y1 + y2) / 2.0;
		if (y2 - y1 == 0) {
			return new int[] {
					(int) (midx),
					(int) (midy - distance),
					(int) (midx),
					(int) (midy + distance),

			};
		} else {
			double slope = -1.0 * (x2 - x1) / (y2 - y1);
			double xDiff = (distance / Math.sqrt(1 + Math.pow(slope, 2)));

			return new int[] {
					(int) (midx - xDiff),
					(int) ((slope * -1.0 * xDiff) + midy),
					(int) (midx + xDiff),
					(int) ((slope * xDiff) + midy),

			};

		}




	}

}
