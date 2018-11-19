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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
import com.typicalprojects.TronMachine.neuronal_migration.OutputOption;
import com.typicalprojects.TronMachine.neuronal_migration.processing.Custom3DCounter.Column;
import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.Logger;
import com.typicalprojects.TronMachine.util.Point;
import com.typicalprojects.TronMachine.util.PolarizedPolygonROI;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;

import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.RoiEncoder;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

public class ROIEditableImage implements Serializable {

	private transient ImageContainer ic;
	private Map<String, float[][]> ptsData;

	private transient Channel drawChannel;
	private transient GUI gui = null;

	private List<PolarizedPolygonROI> rois = new ArrayList<PolarizedPolygonROI>();
	private transient volatile Map<Channel, BufferedImage> cache = new HashMap<Channel, BufferedImage>();
	private transient boolean selectingPositive = false;

	private List<Point> points = new ArrayList<Point>();

	@SuppressWarnings("deprecation")
	public ROIEditableImage(ImageContainer ic, Channel drawChannel, Map<String, ResultsTable> tables, GUI gui) {
		this.gui = gui;
		this.ic =ic;
		this.drawChannel = drawChannel;
		this.ptsData = new HashMap<String, float[][]>();
		for (Entry<String, ResultsTable> tableEn : tables.entrySet()) {
			ResultsTable input = tableEn.getValue();
			int idIndex = input.getColumnIndex("ID");
			int xIndex = input.getColumnIndex(Column.X.getTitle());
			int yIndex = input.getColumnIndex(Column.Y.getTitle());
			if (idIndex == ResultsTable.COLUMN_NOT_FOUND || xIndex == ResultsTable.COLUMN_NOT_FOUND || yIndex == ResultsTable.COLUMN_NOT_FOUND) {
				this.ptsData.put(tableEn.getKey(), new float[][]{new float[0], new float[0], new float[0]});
			} else {
				this.ptsData.put(tableEn.getKey(), new float[][]{input.getColumn(input.getColumnIndex("ID")),
					input.getColumn(input.getColumnIndex(Column.X.getTitle())),
					input.getColumn(input.getColumnIndex(Column.Y.getTitle()))});
			}

		}

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
		ImagePlus ip = this.ic.getImage(OutputOption.MaxedChannel, channel, false);
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
					getImage(OutputOption.ProcessedFull, channelToDrawROI, /*note: changed to false*/true)
					.getProcessor()
					.convertToRGB());
		} else {
			dup = new ImagePlus("dup", this.ic.getImage(OutputOption.MaxedChannel, channelToDrawROI, /*note: changed to false*/true).getProcessor().convertToRGB());
		}

		BufferedImage bi = null;

		ImageProcessor ip = dup.getProcessor();
		//ip.setLineWidth(3);
		double imgSize = Math.min(dup.getDimensions()[0], dup.getDimensions()[1] );

		ip.setFont(new Font("Arial", Font.BOLD, (int) (imgSize / 50)));


		for (PolarizedPolygonROI roiWrapper : this.rois) {


			PolygonRoi roi = roiWrapper.get();

			roi.setFillColor(Color.GREEN);


			roi.setStrokeWidth(imgSize / 300.0);
			ip.drawOverlay(new Overlay(roi));
			int middleY = Math.max(roi.getPolygon().ypoints[(roi.getPolygon().npoints / 2)] + 3, 10);
			middleY= Math.min(middleY, dup.getDimensions()[1] - 4);
			int middleX = roi.getPolygon().xpoints[(roi.getPolygon().npoints / 2)];

			ip.setColor(Color.GREEN);
			if(roiWrapper.positiveRegionIsSet()) {
				if (roi.getPolygon().npoints > 12) {
					int polySize = (int) (roi.getPolygon().npoints / 6.0);

					Map<Integer, int[]> posNegLabels = new HashMap<Integer, int[]>();
					posNegLabels.put(polySize, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize, 15));
					posNegLabels.put(polySize*2, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*2, 15));
					posNegLabels.put(polySize*4, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*4, 15));
					posNegLabels.put(polySize*5, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*5, 15));

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
			int size = (int) (Math.min(dup.getDimensions()[0], dup.getDimensions()[1] ) / 100.0);
			ip.fillRect((int) ((float[]) obj[0])[0] - (size / 2), (int) ((float[]) obj[1])[0] - (size / 2), size, size);
			dup.updateImage();
			bi = dup.getBufferedImage();

		} else {

			PolygonRoi pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], points.size(), Roi.POLYLINE) ;
			pgr.setStrokeColor(Color.GREEN);
			pgr.setFillColor(Color.GREEN);
			double size = Math.min(dup.getDimensions()[0], dup.getDimensions()[1] );

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

		ImagePlus drawImage = this.ic.getImage(OutputOption.MaxedChannel, this.drawChannel, false);


		Point newStart = getStretchedPoint(this.points.get(0), drawImage.getDimensions());
		if (newStart != null) {
			this.points.add(0, newStart);
		}

		Point newEnd = getStretchedPoint(this.points.get(this.points.size() - 1), drawImage.getDimensions());

		if (newEnd != null) {

			this.points.add(newEnd);
		}

		Object[] obj = convertPointsToArray();
		
		PolygonRoi pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], points.size(), Roi.POLYLINE) ;

		pgr.setStrokeColor(Color.GREEN);
		pgr.setFillColor(Color.GREEN);
		double imgSize = Math.min(drawImage.getDimensions()[0], drawImage.getDimensions()[1] );

		pgr.setStrokeWidth(imgSize / 300.0);
		pgr.fitSplineForStraightening();

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
		
		if (lineCollides(pgr, this.rois)) {
			return false;
		}
		
		polygon = pgr.getPolygon();
		this.rois.add(new PolarizedPolygonROI(name, pgr, createHalfRegion(polygon.xpoints, polygon.ypoints, new Point(0, 0, false), new Point(0, drawImage.getHeight() - 1, false), new Point(drawImage.getWidth() - 1, drawImage.getHeight() - 1, false), new Point(drawImage.getWidth() - 1, 0, false))));
		
		try {
			/*File file = new File("testFileSerialize.txt");
			FileOutputStream fileStream = new FileOutputStream(file); 
	        ObjectOutputStream out = new ObjectOutputStream(fileStream); 
	        // Method for serialization of object 
	        out.writeObject(this.rois.get(this.rois.size() - 1)); 
	        System.out.println("positivesel: " + this.rois.get(this.rois.size() - 1).positiveRegionIsSet());
	        out.close(); 
	        fileStream.close();
	        
	        FileInputStream fileInput = new FileInputStream(file); 
            ObjectInputStream in = new ObjectInputStream(fileInput); 
              
            // Method for deserialization of object 
            PolarizedPolygonROI object1 = (PolarizedPolygonROI)in.readObject(); 
              
            in.close(); 
            fileInput.close(); 
              
            System.out.println("Object has been deserialized "); 
            System.out.println("Name = " + object1.getName()); 
            StringBuilder sb = new StringBuilder();
            for (Entry<Integer, Integer> en : object1.getPointsOnLine().entrySet()) {
            		sb.append("(" + en.getKey() + "," + en.getValue() + ") ");
            }
            System.out.println("points = " + sb.toString()); 
            sb.setLength(0);
            for (java.awt.Point p : object1.getContainedHalfPoints()) {
        			sb.append("(" + p.x + "," + p.y + ") ");
            }
            System.out.println("points2 = " + sb.toString()); 
            System.out.println("positivesel = " + object1.positiveRegionIsSet());*/ 
			
			File file = new File("testFileSerialize.txt");
			FileOutputStream fileStream = new FileOutputStream(file); 
	        ObjectOutputStream out = new ObjectOutputStream(fileStream); 
	        // Method for serialization of object 
	        out.writeObject(this); 
	        out.close(); 
	        fileStream.close();
	        
	        FileInputStream fileInput = new FileInputStream(file); 
            ObjectInputStream in = new ObjectInputStream(fileInput); 
              
            // Method for deserialization of object 
            ROIEditableImage object1 = (ROIEditableImage)in.readObject(); 
              
            in.close(); 
            fileInput.close(); 
              
            System.out.println("Object has been deserialized "); 
            System.out.println("ROIS = " + object1.rois.size()); 

			
		}catch (Exception e) {
			e.printStackTrace();
		}
		 
          
        System.out.println("Object has been serialized"); 
		
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

	public void createAndSaveNewImage() {
		
		Map<ImagePlus, Object[]> ipToDrawROIS = new HashMap<ImagePlus, Object[]>();
		if (GUI.settings.enabledOptions.containsKey(OutputOption.RoiDrawFull)) {
			for (Channel chan : GUI.settings.enabledOptions.get(OutputOption.RoiDrawFull).includedChannels) {
				ipToDrawROIS.put(this.ic.getImage(OutputOption.MaxedChannel, chan, true), new Object[] {chan, OutputOption.RoiDrawFull});
			}
		}
		for (Channel chan : GUI.settings.channelsToProcess) {
			if (GUI.outputOptionContainsChannel(OutputOption.RoiDrawProcessedDots, chan)) {
				ipToDrawROIS.put(this.ic.getImage(OutputOption.ProcessedDots, chan, true), new Object[] {chan, OutputOption.RoiDrawProcessedDots});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.RoiDrawProcessedDotsNoNum, chan)) {
				ipToDrawROIS.put(this.ic.getImage(OutputOption.ProcessedDotsNoNum, chan, true), new Object[] {chan, OutputOption.RoiDrawProcessedDotsNoNum});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.RoiDrawProcessedDotsObjects, chan)) {
				ipToDrawROIS.put(this.ic.getImage(OutputOption.ProcessedDotsObjects, chan, true), new Object[] {chan, OutputOption.RoiDrawProcessedDotsObjects});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.RoiDrawProcessedDotsOriginal, chan)) {
				ipToDrawROIS.put(this.ic.getImage(OutputOption.ProcessedDotsOriginal, chan, true), new Object[] {chan, OutputOption.RoiDrawProcessedDotsOriginal});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.RoiDrawProcessedFull, chan)) {
				ipToDrawROIS.put(this.ic.getImage(OutputOption.ProcessedFull, chan, true), new Object[] {chan, OutputOption.RoiDrawProcessedFull});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.RoiDrawProcessedObjects, chan)) {
				ipToDrawROIS.put(this.ic.getImage(OutputOption.ProcessedObjects, chan, true), new Object[] {chan, OutputOption.RoiDrawProcessedObjects});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.RoiDrawProcessedObjectsOriginal, chan)) {
				ipToDrawROIS.put(this.ic.getImage(OutputOption.ProcessedObjectsOriginal, chan, true), new Object[] {chan, OutputOption.RoiDrawProcessedObjectsOriginal});
			}

		}
		if (GUI.settings.enabledOptions.containsKey(OutputOption.RoiDrawBlank)) {
			int[] dim = this.ic.getDimensions();
			ipToDrawROIS.put(new ImagePlus("test", new BufferedImage(dim[0], dim[1], BufferedImage.TYPE_INT_RGB)), new Object[] {null, OutputOption.RoiDrawBlank});

		}
		
		for (Entry<ImagePlus, Object[]> imgEntry : ipToDrawROIS.entrySet()) {
			ImagePlus roiImg = imgEntry.getKey();
			roiImg.setProcessor(roiImg.getProcessor().convertToColorProcessor());
			ImageProcessor ip = roiImg.getProcessor();
			
			double size = Math.min(roiImg.getDimensions()[0], roiImg.getDimensions()[1] );

			ip.setFont(new Font("Arial", Font.BOLD, (int) (size / 50)));

			for (PolarizedPolygonROI roiWrapper : this.rois) {
				PolygonRoi roi = roiWrapper.get();
				roi.setFillColor(Color.GREEN);


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
					posNegLabels.put(polySize, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize, 15));
					posNegLabels.put(polySize*2, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*2, 15));
					posNegLabels.put(polySize*4, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*4, 15));
					posNegLabels.put(polySize*5, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*5, 15));

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
			if (imgEntry.getValue()[0] == null) {
				this.ic.saveSupplementalImage((OutputOption) imgEntry.getValue()[1], roiImg);
			} else {
				this.ic.saveSupplementalImage((OutputOption) imgEntry.getValue()[1], roiImg, (Channel) imgEntry.getValue()[0]);

			}
		}

	}

	public void saveROIs() {
		for (PolarizedPolygonROI roi : this.rois) {
			RoiEncoder.save(roi.get(), this.ic.getIntermediateFilesDirectory() + File.separator + roi.getName() +".roi");
		}
	}

	public Map<String, ResultsTable> processMigration(Logger progress, List<Analyzer.Calculation> calculations) {		

		Map<String, ResultsTable> map = new HashMap<String, ResultsTable>();
		for (Entry<String, float[][]> en : this.ptsData.entrySet()) {
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
		Map<ImagePlus, Object[]> ipToDrawBINS = new HashMap<ImagePlus, Object[]>();
		if (GUI.settings.enabledOptions.containsKey(OutputOption.BinDrawFull)) {
			for (Channel chan : GUI.settings.enabledOptions.get(OutputOption.BinDrawFull).includedChannels) {
				ipToDrawBINS.put(this.ic.getImage(OutputOption.MaxedChannel, chan, true), new Object[] {chan, OutputOption.BinDrawFull});
			}
		}
		for (Channel chan : GUI.settings.channelsToProcess) {
			if (GUI.outputOptionContainsChannel(OutputOption.BinDrawProcessedDots, chan)) {
				ipToDrawBINS.put(this.ic.getImage(OutputOption.ProcessedDots, chan, true), new Object[] {chan, OutputOption.BinDrawProcessedDots});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.BinDrawProcessedDotsNoNum, chan)) {
				ipToDrawBINS.put(this.ic.getImage(OutputOption.ProcessedDotsNoNum, chan, true), new Object[] {chan, OutputOption.BinDrawProcessedDotsNoNum});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.BinDrawProcessedDotsObjects, chan)) {
				ipToDrawBINS.put(this.ic.getImage(OutputOption.ProcessedDotsObjects, chan, true), new Object[] {chan, OutputOption.BinDrawProcessedDotsObjects});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.BinDrawProcessedDotsOriginal, chan)) {
				ipToDrawBINS.put(this.ic.getImage(OutputOption.ProcessedDotsOriginal, chan, true), new Object[] {chan, OutputOption.BinDrawProcessedDotsOriginal});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.BinDrawProcessedFull, chan)) {
				ipToDrawBINS.put(this.ic.getImage(OutputOption.ProcessedFull, chan, true), new Object[] {chan, OutputOption.BinDrawProcessedFull});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.BinDrawProcessedObjects, chan)) {
				ipToDrawBINS.put(this.ic.getImage(OutputOption.ProcessedObjects, chan, true), new Object[] {chan, OutputOption.BinDrawProcessedObjects});
			}
			if (GUI.outputOptionContainsChannel(OutputOption.BinDrawProcessedObjectsOriginal, chan)) {
				ipToDrawBINS.put(this.ic.getImage(OutputOption.ProcessedObjectsOriginal, chan, true), new Object[] {chan, OutputOption.BinDrawProcessedObjectsOriginal});
			}

		}
		if (GUI.settings.enabledOptions.containsKey(OutputOption.BinDrawBlank)) {
			int[] dim = this.ic.getDimensions();
			 ipToDrawBINS.put(new ImagePlus("test", new BufferedImage(dim[0], dim[1], BufferedImage.TYPE_INT_RGB)), new Object[] {null, OutputOption.BinDrawBlank});

		}
		for (Entry<ImagePlus, Object[]> imgEntry : ipToDrawBINS.entrySet()) {
			ImagePlus imgToDrawBinLines = imgEntry.getKey();
			imgToDrawBinLines.setProcessor(imgToDrawBinLines.getProcessor().convertToColorProcessor());
			binnedRegion.drawBinLines(imgToDrawBinLines.getProcessor());
			imgToDrawBinLines.updateImage();
			if (imgEntry.getValue()[0] == null) {
				this.ic.saveSupplementalImage((OutputOption) imgEntry.getValue()[1], imgToDrawBinLines);
			} else {
				this.ic.saveSupplementalImage((OutputOption) imgEntry.getValue()[1], imgToDrawBinLines, (Channel) imgEntry.getValue()[0]);

			}

		}
		
		logger.setCurrentTaskComplete();
		
		if (binnedRegion.binLinesOverlap()) {
			JOptionPane.showMessageDialog(this.gui.getComponent(), "There was an error processing bins. Bin lines overlap.", "Bin error", JOptionPane.ERROR_MESSAGE);
			return null;
		}

		// SETUP
		LinkedHashMap<Integer, Map<Channel, Integer>> pointsBinned = new LinkedHashMap<Integer, Map<Channel, Integer>>();
		HashMap<Channel, Integer> totalNumCells = new HashMap<Channel,Integer>();
		
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
			rt.setHeading(counter, chan.toReadableString() + " Cells (Num)");
			rt.setHeading(counter + 1, chan.toReadableString() + " Cells (%)");

			counter = counter + 2;
		}

		logger.setCurrentTask("Binning points... ");

		for (Channel chan : GUI.settings.channelsToProcess) {
			float[][] input = this.ptsData.get(chan.name());


			for (int i =0; i < input[1].length && i < input[2].length; i++) {
				int bin = binnedRegion.getEnclosingBin((int) input[1][i], (int) input[2][i]);
				Map<Channel, Integer> channelBinPtCount;
				if (bin < 1) {
					if (GUI.settings.excludePtsOutsideBin) {
						continue;
					} else if (GUI.settings.includePtsNearestBin) {
						if (bin == -1) {
							channelBinPtCount = pointsBinned.get(1);
						} else {
							channelBinPtCount = pointsBinned.get(GUI.settings.numberOfBins);
						}
					} else {
						channelBinPtCount = pointsBinned.get(-1);
					}
				} else {
					channelBinPtCount = pointsBinned.get(bin);
				}
				channelBinPtCount.put(chan, channelBinPtCount.get(chan) + 1);
				Integer totNum = totalNumCells.get(chan);
				if (totNum == null)
					totNum =0;
				totNum++;
				totalNumCells.put(chan, totNum);
			}

		}


		for (Entry<Integer, Map<Channel, Integer>> binCounts : pointsBinned.entrySet()) {
			if (binCounts.getKey() == -1 && (GUI.settings.excludePtsOutsideBin || GUI.settings.includePtsNearestBin)) {
				continue;
			}
			rt.incrementCounter();
			rt.addValue(0, binCounts.getKey());
			counter = 1;
			for (Channel chan : GUI.settings.channelsToProcess) {
				int numCellsInBin = binCounts.getValue().get(chan);
				int numCellsTotal = totalNumCells.get(chan);
				double percent = (((int) ((((double) numCellsInBin) / numCellsTotal) * 10000)) / 100.0);
				rt.addValue(counter, binCounts.getValue().get(chan));
				rt.addValue(counter + 1, percent);

				counter = counter + 2;
			}
			

		}
		logger.setCurrentTaskComplete();

		return rt;
		



	}

	@SuppressWarnings("deprecation")
	private ResultsTable calculateDistances(float[][] input, Channel chan, Logger progress, List<Analyzer.Calculation> calculations) {

		try {

			if (input[0].length == 0 || input[1].length == 0 || input[2].length == 0) {
				ResultsTable newTable = new ResultsTable();
				newTable.setHeading(0, "Object Num");
				newTable.setHeading(1, "X (pixels)");
				newTable.setHeading(2, "Y (pixels)");
				return newTable;
			}

			float[] ids = input[0];
			float[] xObjValues = input[1];
			float[] yObjValues = input[2];

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
			ImageProcessor ip = this.ic.getImage(OutputOption.MaxedChannel, chan, false)
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
	
    /*private void writeObject(ObjectOutputStream stream)
            throws IOException {
    		stream.defaultWriteObject();
    		HashMap<String, String> results = new HashMap<String,String>();
    		for (Entry<String, ResultsTable> rt : this.) {
    			rt.getValue().add
    		}
        stream.writeObject(name);
        stream.writeInt(id);
        stream.writeObject(DOB);
    }

    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        name = (String) stream.readObject();
        id = stream.readInt();
        DOB = (String) stream.readObject();
    }*/

	public static Point getStretchedPoint(Point p, int[] dimensions) {

		int distTop = p.y;
		int distBottom = dimensions[1] - p.y - 1;
		int distLeft = p.x;
		int distRight = dimensions[0] - p.x - 1;

		int min = Math.min(Math.min(distTop, distBottom), Math.min(distLeft, distRight));

		if (min != 0) {
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



	private int[] _getOrthogonalPointsFromPolyLine(Polygon polygon, int pos, double distance) {

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

	
	private static boolean lineCollides(PolygonRoi roi, List<PolarizedPolygonROI> otherRois) {
		
		Polygon currRoi = roi.getPolygon();
		int[] xpts = currRoi.xpoints;
		int[] ypts = currRoi.ypoints;

		for (int i = 0; i < otherRois.size(); i++) {

			Polygon other = otherRois.get(i).get().getPolygon();
			for (int ptCurr = 0; ptCurr < currRoi.npoints; ptCurr++) {
				for (int ptOther = 0; ptOther < other.npoints; ptOther++) {
					if (xpts[ptCurr] == other.xpoints[ptOther] && ypts[ptCurr] == other.ypoints[ptOther]) {
						return true;
					}
				}
			}

		}
		
		return false;
		
	}

}
