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
import java.util.HashSet;
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
import com.typicalprojects.TronMachine.neuronal_migration.RunConfiguration;
import com.typicalprojects.TronMachine.popup.Displayable;
import com.typicalprojects.TronMachine.popup.MultiSelectPopup;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager.Channel;
import com.typicalprojects.TronMachine.util.Analyzer;
import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.Line;
import com.typicalprojects.TronMachine.util.Logger;
import com.typicalprojects.TronMachine.util.Point;
import com.typicalprojects.TronMachine.util.PolarizedPolygonROI;
import com.typicalprojects.TronMachine.util.ResultsTable;
import com.typicalprojects.TronMachine.util.Toolbox;
import com.typicalprojects.TronMachine.util.Analyzer.Calculation;

import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.io.RoiEncoder;
import ij.measure.Calibration;
import ij.process.ImageProcessor;

public class ROIEditableImage implements Serializable {

	private static final long serialVersionUID = 9162213686232534708L; // For serialization

	private ImageContainer ic;
	private Map<Channel, List<Point>> objData;

	private transient GUI gui = null;

	private List<PolarizedPolygonROI> rois = new ArrayList<PolarizedPolygonROI>();
	private transient volatile Map<Channel, ImagePlus> cache = new HashMap<Channel, ImagePlus>();
	private transient boolean selectingPositive = false;
	private PolarizedPolygonROI currentlyCreating = null;
	private Map<Tag, PolarizedPolygonROI> tags = new HashMap<Tag, PolarizedPolygonROI>();
	private transient MultiSelectPopup<Tag> multiSelectPopup = new MultiSelectPopup<Tag>();
	private ROISelectMeta selectionStateData = null;

	private List<Point> points = new ArrayList<Point>();

	public ROIEditableImage(GUI gui, ImageContainer ic, Map<Channel, List<Point>> objData) {

		this.gui = gui;
		this.ic =ic;
		this.objData = objData;
		this.selectionStateData = new ROISelectMeta();

	}

	public ImageContainer getContainer() {
		return this.ic;
	}

	public RunConfiguration getRunConfig() {
		return this.ic.getRunConfig();
	}

	public ROISelectMeta getSelectionStateMeta() {
		return this.selectionStateData;
	}

	public boolean hasROIs() {
		return !this.rois.isEmpty();
	}

	public List<PolarizedPolygonROI> getROIs() {
		return this.rois;
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

	public ImagePlus getPaintedCopy(Channel channelToDrawROI) {
		

		if (this.cache.containsKey(channelToDrawROI)) {
			return this.cache.get(channelToDrawROI);
		}


		Object[] obj = convertPointsToArray();
		ImagePlus dup = null;

		if (getRunConfig().channelMan.isProcessChannel(channelToDrawROI)) {
			dup = new ImagePlus("dup", this.ic.getImage(OutputOption.ProcessedFull, channelToDrawROI, true)
					.getProcessor().convertToRGB());
		} else {
			dup = new ImagePlus("dup", this.ic.getImage(OutputOption.MaxedChannel, channelToDrawROI, true)
					.getProcessor().convertToRGB());
		}


		ImageProcessor ip = dup.getProcessor();
		double imgSize = Math.max(dup.getDimensions()[0], dup.getDimensions()[1] );
		float strokeWidth = ((float) imgSize) / 300.0f;
		double orthogonalDistanceFromLine = imgSize / 68;
		Font font = new Font("Arial", Font.BOLD, (int) (imgSize / 50));
		ip.setFont(font);
		
		if (points.size() == 1) {



			ip.setColor(Color.GREEN);
			int size = (int) (Math.min(dup.getDimensions()[0], dup.getDimensions()[1] ) / 100.0);
			ip.fillRect((int) ((float[]) obj[0])[0] - (size / 2), (int) ((float[]) obj[1])[0] - (size / 2), size, size);
			dup.updateImage();

		} else if (points.size() > 1) {

			PolygonRoi pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], points.size(), Roi.POLYLINE) ;
			pgr.setStrokeColor(Color.GREEN);
			pgr.setFillColor(Color.GREEN);

			pgr.setStrokeWidth(strokeWidth);
			if (points.size() > 2) {
				pgr.fitSplineForStraightening();;
			}

			ip.setColor(Color.GREEN);
			ip.drawOverlay(new Overlay(pgr));
			dup.updateImage();


		}
		
		for (int i = 0; i <= this.rois.size(); i++) {

			PolarizedPolygonROI roiWrapper;
			PolygonRoi roi;
			if (i == this.rois.size()) {
				if (this.currentlyCreating != null) {
					roiWrapper = this.currentlyCreating;
					roi = this.currentlyCreating.get();
				} else {
					break;
				}
			} else {
				roiWrapper = this.rois.get(i);
				roi = roiWrapper.get();
			}

			roi.setStrokeColor(Color.GREEN);
			roi.setStrokeWidth(strokeWidth);
			ip.drawOverlay(new Overlay(roi));
			
			int middleY = roi.getPolygon().ypoints[(roi.getPolygon().npoints / 2)];
			middleY= Math.min(middleY, dup.getDimensions()[1] - 1 - (ip.getFontMetrics().getHeight()/ 2));
			middleY= Math.max(middleY, 0 + (ip.getFontMetrics().getHeight() / 2));
			int middleX = roi.getPolygon().xpoints[(roi.getPolygon().npoints / 2)];
			middleX= Math.min(middleX, dup.getDimensions()[0] - 1 - (ip.getFontMetrics().stringWidth(roiWrapper.getName())/ 2));
			middleX= Math.max(middleX, 0 + (ip.getFontMetrics().stringWidth(roiWrapper.getName()) / 2));
						
			ip.setColor(Color.YELLOW);
			if(roiWrapper.positiveRegionIsSet()) {
				if (roi.getPolygon().npoints > 12) {
					int polySize = (int) (roi.getPolygon().npoints / 6.0);

					Map<Integer, int[]> posNegLabels = new HashMap<Integer, int[]>();
					posNegLabels.put(polySize, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize, orthogonalDistanceFromLine));
					posNegLabels.put(polySize*2, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*2, orthogonalDistanceFromLine));
					posNegLabels.put(polySize*4, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*4, orthogonalDistanceFromLine));
					posNegLabels.put(polySize*5, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*5, orthogonalDistanceFromLine));

					for (Entry<Integer, int[]> en : posNegLabels.entrySet()) {

						int[] val = en.getValue();
						if (val == null)
							continue;

						try {
							boolean firstIsPositive = ic.isWithinImageBounds(val[0], val[1]) ? roiWrapper.isPositive(val[0], val[1]) : !roiWrapper.isPositive(val[2], val[3]);
							if (firstIsPositive) {
								Toolbox.drawString(ip, font, "+", val[0], val[1], Color.YELLOW, null);
								Toolbox.drawString(ip, font, "-", val[2], val[3], Color.YELLOW, null);

							} else {
								Toolbox.drawString(ip, font, "-", val[0], val[1], Color.YELLOW, null);
								Toolbox.drawString(ip, font, "+", val[2], val[3], Color.YELLOW, null);
								

							}
						} catch (Exception e) {
							// out of bounds;
							continue;
						}
					}


				}
			}
			
			Toolbox.drawStringWithBorderBox(ip, font, roiWrapper.getName(), middleX, middleY, Color.RED, Color.BLACK, Color.YELLOW);

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


		this.cache.put(channelToDrawROI, dup);
		return dup;


	}
	
	public void addPoint(Point p) {
		
		if (!this.points.isEmpty()) {
			Point nearest = this.points.get(this.points.size() - 1);
			if (Analyzer.calculate(Calculation.DIST, p.x, nearest.x, p.y, nearest.y) < 2.5) {
				return;
			}
		}
		
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
		
		// Create first polygon with given points
		Object[] obj = convertPointsToArray();
		PolygonRoi pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], points.size(), Roi.POLYLINE) ;
		pgr.fitSplineForStraightening();

		// Find add add extrapolation points
		int pointsToTake = 5;
		Polygon poly = pgr.getPolygon();
		if (poly.npoints < 5) {
			pointsToTake = 2;
		}
		Line extrapolationLine1 = new Line(poly.xpoints[pointsToTake - 1], poly.ypoints[pointsToTake - 1], poly.xpoints[0], poly.ypoints[0]);
		Line extrapolationLine2 = new Line(poly.xpoints[poly.npoints - pointsToTake], poly.ypoints[poly.npoints - pointsToTake], poly.xpoints[poly.npoints - 1], poly.ypoints[poly.npoints - 1]);
		int[] dims = this.ic.getDimensions();
		Point extrapP1 = null;
		Point extrapP2 = null;
		try {
			extrapP1 = extrapolationLine1.getExtrapolatedPoint(0, 0, dims[0] - 1, dims[1] - 1);
			extrapP2 = extrapolationLine2.getExtrapolatedPoint(0, 0, dims[0] - 1, dims[1] - 1);
		} catch (Exception e) {
			e.printStackTrace();
			extrapP1 = getStretchedPoint(this.points.get(0), dims);
			extrapP2 = getStretchedPoint(this.points.get(this.points.size() - 1), dims);

		}
		if (extrapP1 != null) {
			this.points.add(0, extrapP1);
		}
		if (extrapP2 != null) {
			this.points.add(extrapP2);
		}
		
		// Re-create the new polygon
		obj = convertPointsToArray();
		pgr = new PolygonRoi((float[]) obj[0], (float[]) obj[1], points.size(), Roi.POLYLINE) ;
		pgr.fitSplineForStraightening();

		ImagePlus drawImage = this.ic.getImage(OutputOption.MaxedChannel, getRunConfig().channelMan.getPrimaryROIDrawChan(), false);

		// Set draw properties
		pgr.setStrokeColor(Color.GREEN);
		pgr.setFillColor(Color.GREEN);
		double imgSize = Math.min(drawImage.getDimensions()[0], drawImage.getDimensions()[1] );
		pgr.setStrokeWidth(imgSize / 300.0);

		poly = pgr.getPolygon();
		int[] newxs = new int[poly.npoints + 2];
		System.arraycopy(poly.xpoints, 0, newxs, 1, poly.npoints);
		int[] newys = new int[poly.npoints + 2];
		System.arraycopy(poly.ypoints, 0, newys, 1, poly.npoints);
		newxs[0] = points.get(0).x;
		newxs[newxs.length - 1] = points.get(points.size() - 1).x;
		newys[0] =  points.get(0).y;
		newys[newys.length - 1] = points.get(points.size() - 1).y;


		pgr = new PolygonRoi(newxs, newys, poly.npoints + 2, Roi.POLYLINE);
		if (lineCollides(pgr, this.rois)) {
			return false;
		}

		poly = pgr.getPolygon();
		this.currentlyCreating = new PolarizedPolygonROI(name, pgr, 
				createHalfRegion(poly.xpoints, poly.ypoints, new Point(0, 0, false), new Point(0, drawImage.getHeight() - 1, false), new Point(drawImage.getWidth() - 1, drawImage.getHeight() - 1, false), new Point(drawImage.getWidth() - 1, 0, false))); 
		
		this.points.clear();
		this.cache.clear();

		return true;

	}

	public void clearPoints() {
		this.cache.clear();
		this.points.clear();
	}

	public void removeROI(PolarizedPolygonROI line) {
		this.cache.clear();
		this.rois.remove(line);

		Iterator<Entry<Tag, PolarizedPolygonROI>> itr = this.tags.entrySet().iterator();
		boolean addRef = false;
		while (itr.hasNext()) {
			Entry<Tag, PolarizedPolygonROI> entry = itr.next();
			if (entry.getValue().equals(line)) {
				if (entry.getKey().equals(Tag.REF_PERCMIG)) {
					addRef = true;
				}
				itr.remove();
			}
		}
		if (addRef && !this.rois.isEmpty()) {
			this.tags.put(Tag.REF_PERCMIG, this.rois.get(0));
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

	public void createAndSaveNewImages() {

		Map<ImagePlus, Object[]> ipToDrawROIS = new HashMap<ImagePlus, Object[]>();

		RunConfiguration rc = this.ic.getRunConfig();
		for (Channel chan : this.ic.getRunConfig().channelMan.getChannels()) {
			if (rc.channelMan.isProcessChannel(chan)) {


				if (rc.channelMan.hasOutput(OutputOption.RoiDrawProcessedDots, chan)) {
					ipToDrawROIS.put(getImgWithDots(chan, true, true, false, false), new Object[] {chan, OutputOption.RoiDrawProcessedDots});
				}
				if (rc.channelMan.hasOutput(OutputOption.RoiDrawProcessedDotsNoNum, chan)) {
					ipToDrawROIS.put(getImgWithDots(chan, false, true, false, false), new Object[] {chan, OutputOption.RoiDrawProcessedDotsNoNum});
				}
				if (rc.channelMan.hasOutput(OutputOption.RoiDrawProcessedDotsObjects, chan)) {
					ipToDrawROIS.put(getImgWithDots(chan, true, true, true, false), new Object[] {chan, OutputOption.RoiDrawProcessedDotsObjects});
				}
				if (rc.channelMan.hasOutput(OutputOption.RoiDrawProcessedDotsOriginal, chan)) {
					ipToDrawROIS.put(getImgWithDots(chan, true, true, false, true), new Object[] {chan, OutputOption.RoiDrawProcessedDotsOriginal});
				}
				if (rc.channelMan.hasOutput(OutputOption.RoiDrawProcessedFull, chan)) {
					ipToDrawROIS.put(getImgWithDots(chan, true, true, true, true), new Object[] {chan, OutputOption.RoiDrawProcessedFull});
				}
				if (rc.channelMan.hasOutput(OutputOption.RoiDrawProcessedObjects, chan)) {
					ipToDrawROIS.put(getImgWithDots(chan, true, false, true, false), new Object[] {chan, OutputOption.RoiDrawProcessedObjects});
				}
				if (rc.channelMan.hasOutput(OutputOption.RoiDrawProcessedObjectsOriginal, chan)) {
					ipToDrawROIS.put(getImgWithDots(chan, true, false, true, true), new Object[] {chan, OutputOption.RoiDrawProcessedObjectsOriginal});
				}	

			}

			if (rc.channelMan.hasOutput(OutputOption.RoiDrawFull, chan)) {
				ipToDrawROIS.put(ic.getImage(OutputOption.MaxedChannel, chan, true), new Object[] {chan, OutputOption.RoiDrawFull});
			}

		}

		if (rc.channelMan.hasOutput(OutputOption.RoiDrawBlank)) {
			int[] dim = this.ic.getDimensions();
			ipToDrawROIS.put(new ImagePlus("BlankROIDraw", new BufferedImage(dim[0], dim[1], BufferedImage.TYPE_INT_RGB)), new Object[] {null, OutputOption.RoiDrawBlank});

		}
		if (rc.channelMan.hasOutput(OutputOption.RoiDrawMinBlank)) {
			int[] dim = this.ic.getDimensions();
			ipToDrawROIS.put(new ImagePlus("BlankROIDrawMin", new BufferedImage(dim[0], dim[1], BufferedImage.TYPE_INT_RGB)), new Object[] {null, OutputOption.RoiDrawMinBlank});

		}

		for (Entry<ImagePlus, Object[]> imgEntry : ipToDrawROIS.entrySet()) {
			ImagePlus roiImg = imgEntry.getKey();
			OutputOption op = (OutputOption) imgEntry.getValue()[1];
			roiImg.setProcessor(roiImg.getProcessor().convertToColorProcessor());
			ImageProcessor ip = roiImg.getProcessor();
			
			//ip.setLineWidth(3);
			double imgSize = Math.max(roiImg.getDimensions()[0], roiImg.getDimensions()[1] );
			float strokeWidth = ((float) imgSize) / 300.0f;
			double orthogonalDistanceFromLine = imgSize / 68;
			Font font = new Font("Arial", Font.BOLD, (int) (imgSize / 50));

			
			for (int i = 0; i <= this.rois.size(); i++) {

				PolarizedPolygonROI roiWrapper;
				PolygonRoi roi;
				if (i == this.rois.size()) {
					if (this.currentlyCreating != null) {
						roiWrapper = this.currentlyCreating;
						roi = this.currentlyCreating.get();
					} else {
						break;
					}
				} else {
					roiWrapper = this.rois.get(i);
					roi = roiWrapper.get();
				}
				
				roi.setStrokeWidth(strokeWidth);
				if (op.equals(OutputOption.RoiDrawMinBlank)) {
					roi.setStrokeColor(Color.WHITE);
					ip.setColor(Color.WHITE);
					ip.drawOverlay(new Overlay(roi));
					continue;
				} else {
					roi.setStrokeColor(Color.GREEN);
					ip.setColor(Color.GREEN);
					ip.drawOverlay(new Overlay(roi));
				}
				
				
				
				int middleY = roi.getPolygon().ypoints[(roi.getPolygon().npoints / 2)];
				middleY= Math.min(middleY, roiImg.getDimensions()[1] - 1 - (ip.getFontMetrics().getHeight()/ 2));
				middleY= Math.max(middleY, 0 + (ip.getFontMetrics().getHeight() / 2));
				int middleX = roi.getPolygon().xpoints[(roi.getPolygon().npoints / 2)];
				middleX= Math.min(middleX, roiImg.getDimensions()[0] - 1 - (ip.getFontMetrics().stringWidth(roiWrapper.getName())/ 2));
				middleX= Math.max(middleX, 0 + (ip.getFontMetrics().stringWidth(roiWrapper.getName()) / 2));
				
				
				ip.setColor(Color.YELLOW);
				if(roiWrapper.positiveRegionIsSet()) {
					if (roi.getPolygon().npoints > 12) {
						int polySize = (int) (roi.getPolygon().npoints / 6.0);

						Map<Integer, int[]> posNegLabels = new HashMap<Integer, int[]>();
						posNegLabels.put(polySize, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize, orthogonalDistanceFromLine));
						posNegLabels.put(polySize*2, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*2, orthogonalDistanceFromLine));
						posNegLabels.put(polySize*4, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*4, orthogonalDistanceFromLine));
						posNegLabels.put(polySize*5, this._getOrthogonalPointsFromPolyLine(roi.getPolygon(), polySize*5, orthogonalDistanceFromLine));

						for (Entry<Integer, int[]> en : posNegLabels.entrySet()) {

							int[] val = en.getValue();
							if (val == null)
								continue;

							try {
								boolean firstIsPositive = ic.isWithinImageBounds(val[0], val[1]) ? roiWrapper.isPositive(val[0], val[1]) : !roiWrapper.isPositive(val[2], val[3]);
								if (firstIsPositive) {
									Toolbox.drawString(ip, font, "+", val[0], val[1], Color.YELLOW, null);
									Toolbox.drawString(ip, font, "-", val[2], val[3], Color.YELLOW, null);

								} else {
									Toolbox.drawString(ip, font, "-", val[0], val[1], Color.YELLOW, null);
									Toolbox.drawString(ip, font, "+", val[2], val[3], Color.YELLOW, null);
									

								}
							} catch (Exception e) {
								// out of bounds;
								continue;
							}
						}


					}
				}
				
				Toolbox.drawStringWithBorderBox(ip, font, roiWrapper.getName(), middleX, middleY, Color.RED, Color.BLACK, Color.YELLOW);

			}

			roiImg.updateImage();
			if (imgEntry.getValue()[0] == null) {
				this.ic.saveSupplementalImage(op, roiImg);
			} else {
				this.ic.saveSupplementalImage(op, roiImg, (Channel) imgEntry.getValue()[0]);

			}
		}

	}

	public String getTags(PolarizedPolygonROI roi, boolean includeHTMLFontTag) {
		if (this.tags.values().contains(roi)) {

			String output = "";
			String comma = "";
			for (Tag tag : Tag.values()) {
				if (this.tags.containsKey(tag) && this.tags.get(tag).equals(roi)) {
					if (includeHTMLFontTag) {
						output = output.concat(comma).concat("<font color='").concat(tag.getHTMLColor()).concat("'>").concat(tag.getAbbrev()).concat("</font>");
					} else {
						output = output.concat(comma).concat(tag.getAbbrev());
					}
					comma = ", ";
				}
			}

			return output;

		} else {
			return null;
		}
	}


	public String validateTags() {
		if (!this.tags.containsKey(Tag.REF_PERCMIG)) {
			return "You have not set an ROI as the reference for calculations.";
		}

		if (GUI.settings.calculateBins) {
			if (!this.tags.containsKey(Tag.BIN1)) {
				return "You have not set the start ROI for binning.";
			} else if (!this.tags.containsKey(Tag.BIN2)) {
				return "You have not set the end ROI for binning.";
			}
		}

		return null;
	}

	public void promptUserForTag(PolarizedPolygonROI roi) {
		this.selectionStateData.setTagsEdited();
		List<Tag> options = new ArrayList<Tag>();
		if (!this.tags.containsKey(Tag.REF_PERCMIG) || !this.tags.get(Tag.REF_PERCMIG).equals(roi)) {
			options.add(Tag.REF_PERCMIG);
		}
		if (GUI.settings.calculateBins) {

			if (!this.tags.containsKey(Tag.BIN1) || !this.tags.get(Tag.BIN1).equals(roi)) {
				options.add(Tag.BIN1);
			}

			if (!this.tags.containsKey(Tag.BIN2) || !this.tags.get(Tag.BIN2).equals(roi)) {
				options.add(Tag.BIN2);
			}

		}

		if (options.isEmpty()) {
			GUI.displayMessage("No tag options available.", "Apply Tags", this.gui.getPanelDisplay().getRawPanel(), JOptionPane.WARNING_MESSAGE);
			return;
		}
		List<Tag> selected = this.multiSelectPopup.getUserInput(this.gui.getPanelOptions().getRawPanel(), "Select tags:", options, new HashSet<Tag>());
		if (selected == null || selected.isEmpty())
			return;

		if (selected.contains(Tag.BIN1)) {
			if (selected.contains(Tag.BIN2)) {
				GUI.displayMessage("An ROI cannot be assigned as start and end of binning", "Tag Select Error", gui.getComponent(), JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (this.tags.containsKey(Tag.BIN2) && this.tags.get(Tag.BIN2).equals(roi)) {
				this.tags.remove(Tag.BIN2);
			}
			this.tags.put(Tag.BIN1, roi);

		} else if (selected.contains(Tag.BIN2)) {
			if (this.tags.containsKey(Tag.BIN1) && this.tags.get(Tag.BIN1).equals(roi)) {
				this.tags.remove(Tag.BIN1);
			}
			this.tags.put(Tag.BIN2, roi);

		}

		if (selected.contains(Tag.REF_PERCMIG)) {
			this.tags.put(Tag.REF_PERCMIG, roi);
		}

	} 

	public ImagePlus getImgWithDots(Channel chan, boolean includeTextOnDots, boolean dots, boolean mask, boolean original){

		int newFontSize = ObjectCounter.opResultFontSize;
		int newDotSize = ObjectCounter.opResultDotsSize;

		ImagePlus stack = null;
		if (mask) {
			if (original) {
				stack = 	this.ic.getImage(OutputOption.ProcessedObjectsOriginal, chan, true);
				// Will have Color processor
			} else {
				stack = new ImagePlus("temp", this.ic.getImage(OutputOption.ProcessedObjects, chan, false).getProcessor().convertToRGB());
				// Convert from byte to color processor
			}
		} else if (original) {
			if (dots) {
				stack = new ImagePlus("temp", this.ic.getImage(OutputOption.MaxedChannel, chan, false).getProcessor().convertToRGB());
				// Convert from short to color processor
			} else {
				stack = 	this.ic.getImage(OutputOption.MaxedChannel, chan, true);
				// Will have short processor
			}
		} else {

			int[] dim = this.ic.getDimensions();
			stack = new ImagePlus("test", new BufferedImage(dim[0], dim[1], BufferedImage.TYPE_INT_RGB));
			// Color processor
		}

		if (!this.objData.isEmpty() && this.objData.containsKey(chan)) {

			List<Point> chanPoints = this.objData.get(chan);
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
				for (Point point : chanPoints) {
					if (point != null && point.fromObjCounter) {
						ip.drawDot(point.x, point.y);
					} else {
						ip.setColor(Color.BLUE);
						ip.drawDot(point.x, point.y);
						ip.setColor(Color.BLUE);
					}
				}

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

				ImageRoi roi = new ImageRoi(0, 0, ip);
				roi.setZeroTransparent(true);
				roi.setOpacity(1.0);
				stack.getProcessor().drawOverlay(new Overlay(roi));
				stack.updateImage();
			}

		}


		return stack;
	}

	public void saveROIs() {
		for (PolarizedPolygonROI roi : this.rois) {
			RoiEncoder.save(roi.get(), this.ic.getSerializeDirectory() + File.separator + roi.getName() +".roi");
		}
	}

	public Map<String, ResultsTable> processMigration(Logger progress, List<Analyzer.Calculation> calculations) {		

		Map<String, ResultsTable> map = new HashMap<String, ResultsTable>();
		for (Entry<Channel, List<Point>> en : this.objData.entrySet()) {
			map.put(en.getKey().getName(), calculateDistances(en.getValue(), en.getKey(), progress, calculations));
		}

		if (GUI.settings.calculateBins) {
			ResultsTable rt = processBins(progress);
			if (map.containsKey("BINS")) {
				int i = 1;
				for (i = 1; i <= Integer.MAX_VALUE; i++) {
					if (!map.containsKey("BINS_" + i)) {
						map.put("BINS_" + i, rt);
						break;
					}
				}
			} else {
				if (rt != null) {
					map.put("BINS", rt);
				}
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


			binnedRegion = new BinnedRegionMod(this.tags.get(Tag.BIN1), this.tags.get(Tag.BIN2), GUI.settings.numberOfBins, 2, this.ic.getDimensions(), gui.getLogger());


		} catch (BinnedRegionMod.MalformedBinException e1) {
			GUI.displayMessage("There was an error processing bins. This is likely due to misshapen bin lines. Bin data will be neglected.", 
					"Bin Error", this.gui.getComponent(), JOptionPane.ERROR_MESSAGE);
			return null;
		}

		logger.setCurrentTask("Copying bins onto images... ");

		Map<ImagePlus, Object[]> ipToDrawBINS = new HashMap<ImagePlus, Object[]>();

		RunConfiguration rc = this.ic.getRunConfig();
		for (Channel chan : this.ic.getRunConfig().channelMan.getChannels()) {
			if (rc.channelMan.isProcessChannel(chan)) {


				if (rc.channelMan.hasOutput(OutputOption.BinDrawProcessedDots, chan)) {
					ipToDrawBINS.put(getImgWithDots(chan, true, true, false, false), new Object[] {chan, OutputOption.BinDrawProcessedDots});
				}
				if (rc.channelMan.hasOutput(OutputOption.BinDrawProcessedDotsNoNum, chan)) {
					ipToDrawBINS.put(getImgWithDots(chan, false, true, false, false), new Object[] {chan, OutputOption.BinDrawProcessedDotsNoNum});
				}
				if (rc.channelMan.hasOutput(OutputOption.BinDrawProcessedDotsObjects, chan)) {
					ipToDrawBINS.put(getImgWithDots(chan, true, true, true, false), new Object[] {chan, OutputOption.BinDrawProcessedDotsObjects});
				}
				if (rc.channelMan.hasOutput(OutputOption.BinDrawProcessedDotsOriginal, chan)) {
					ipToDrawBINS.put(getImgWithDots(chan, true, true, false, true), new Object[] {chan, OutputOption.BinDrawProcessedDotsOriginal});
				}
				if (rc.channelMan.hasOutput(OutputOption.BinDrawProcessedFull, chan)) {
					ipToDrawBINS.put(getImgWithDots(chan, true, true, true, true), new Object[] {chan, OutputOption.BinDrawProcessedFull});
				}
				if (rc.channelMan.hasOutput(OutputOption.BinDrawProcessedObjects, chan)) {
					ipToDrawBINS.put(getImgWithDots(chan, true, false, true, false), new Object[] {chan, OutputOption.BinDrawProcessedObjects});
				}
				if (rc.channelMan.hasOutput(OutputOption.BinDrawProcessedObjectsOriginal, chan)) {
					ipToDrawBINS.put(getImgWithDots(chan, true, false, true, true), new Object[] {chan, OutputOption.BinDrawProcessedObjectsOriginal});
				}	

			}

			if (rc.channelMan.hasOutput(OutputOption.BinDrawFull, chan)) {
				ipToDrawBINS.put(ic.getImage(OutputOption.MaxedChannel, chan, true), new Object[] {chan, OutputOption.BinDrawFull});
			}

		}

		if (rc.channelMan.hasOutput(OutputOption.BinDrawBlank)) {
			int[] dim = this.ic.getDimensions();
			ipToDrawBINS.put(new ImagePlus("BlankBINDraw", new BufferedImage(dim[0], dim[1], BufferedImage.TYPE_INT_RGB)), new Object[] {null, OutputOption.BinDrawBlank});

		}
		
		if (rc.channelMan.hasOutput(OutputOption.BinDrawMinBlank)) {
			int[] dim = this.ic.getDimensions();
			ipToDrawBINS.put(new ImagePlus("BlankBINDrawMin", new BufferedImage(dim[0], dim[1], BufferedImage.TYPE_INT_RGB)), new Object[] {null, OutputOption.BinDrawMinBlank});

		}


		for (Entry<ImagePlus, Object[]> imgEntry : ipToDrawBINS.entrySet()) {
			ImagePlus imgToDrawBinLines = imgEntry.getKey();
			imgToDrawBinLines.setProcessor(imgToDrawBinLines.getProcessor().convertToColorProcessor());
			OutputOption op = (OutputOption) imgEntry.getValue()[1];
			binnedRegion.drawBinLines(imgToDrawBinLines.getProcessor(), !op.equals(OutputOption.BinDrawMinBlank));
			imgToDrawBinLines.updateImage();
			if (imgEntry.getValue()[0] == null) {
				this.ic.saveSupplementalImage(op, imgToDrawBinLines);
			} else {
				this.ic.saveSupplementalImage(op, imgToDrawBinLines, (Channel) imgEntry.getValue()[0]);

			}

		}

		logger.setCurrentTaskComplete();

		if (binnedRegion.binLinesOverlap()) {
			GUI.displayMessage("There was an error processing bins. Bin lines overlap.", "Bin error",this.gui.getComponent(), JOptionPane.ERROR_MESSAGE);
			return null;
		}

		// SETUP
		LinkedHashMap<Integer, Map<Channel, Integer>> pointsBinned = new LinkedHashMap<Integer, Map<Channel, Integer>>();
		HashMap<Channel, Integer> totalNumCells = new HashMap<Channel,Integer>();

		List<Channel> processChans = getRunConfig().channelMan.getProcessChannels();
		for (int i = 1; i <= GUI.settings.numberOfBins; i++) {
			pointsBinned.put(i, new HashMap<Channel, Integer>());

			for (Channel chan : processChans) {
				pointsBinned.get(i).put(chan, 0);
			}
		}
		pointsBinned.put(-1, new HashMap<Channel, Integer>());
		for (Channel chan : this.objData.keySet()) {
			pointsBinned.get(-1).put(chan, 0);
		}


		ResultsTable rt = new ResultsTable();
		rt.setHeading(0, "Bin");
		int counter = 1;
		for (Channel chan : processChans) {
			rt.setHeading(counter, chan.getName() + " Cells (Num)");
			rt.setHeading(counter + 1, chan.getName() + " Cells (%)");

			counter = counter + 2;
		}

		logger.setCurrentTask("Binning points... ");

		for (Channel chan : processChans) {
			Iterator<Point> ptsItr = this
					.objData
					.get(chan)
					.iterator();

			while (ptsItr.hasNext()) {
				Point p = ptsItr.next();
				int bin = binnedRegion.getEnclosingBin(p.x, p.y);
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
			for (Channel chan : processChans) {
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
	private ResultsTable calculateDistances(List<Point> points, Channel chan, Logger progress, List<Analyzer.Calculation> calculations) {

		try {

			if (points.isEmpty()) {
				ResultsTable newTable = new ResultsTable();
				newTable.setHeading(0, "Object Num");
				newTable.setHeading(1, "X (pixels)");
				newTable.setHeading(2, "Y (pixels)");
				return newTable;
			}

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

			Map<PolarizedPolygonROI, Integer> columnsForEachROI = new HashMap<PolarizedPolygonROI, Integer>(); 

			for (PolarizedPolygonROI roiWrapper : this.rois) {
				newTable.setHeading(counter, "Distance from " + roiWrapper.getName() + " (" + units +")");
				columnsForEachROI.put(roiWrapper, counter);
				counter++;

			}

			// Distance to ROIs
			try {
				progress.setCurrentTask("Measuring distance to ROIs...");
				for (int i = 0; i < points.size(); i++) {
					progress.setCurrentTaskProgress(i + 1, points.size());
					Point p = points.get(i);
					newTable.incrementCounter();
					newTable.addValue(0, i + 1);
					newTable.addValue(1, p.x);
					newTable.addValue(2, p.y);

					int r = 3;
					for (PolarizedPolygonROI roi : this.rois) {

						double shortestDist = Double.MAX_VALUE;
						int lowestY = Integer.MAX_VALUE;
						int highestY = Integer.MIN_VALUE;
						Map<Integer, Integer> coordinatePts = roi.getPointsOnLine();
						for (Entry<Integer, Integer> en : coordinatePts.entrySet()) {
							lowestY = Math.min(lowestY, en.getValue());
							highestY = Math.max(highestY, en.getValue());
							double dist = Math.sqrt(Math.pow(en.getKey() - p.x, 2) + Math.pow(en.getValue() - p.y, 2));
							if (dist < shortestDist)
								shortestDist = dist;
						}


						if (roi.isPositive(p.x, p.y)) {
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

			// Grayscale values
			newTable.setHeading(counter, "Grayscale Value");

			progress.setCurrentTask("Recording grayscale values...");	
			ImageProcessor ip = this.ic.getImage(OutputOption.MaxedChannel, chan, false)
					.getProcessor();
			for (int i = 0; i < points.size(); i++) {
				progress.setCurrentTaskProgress(i + 1, points.size());
				Point p = points.get(i);
				newTable.setValue(counter, i, ip.getPixelValue(p.x, p.y));

			}
			progress.setCurrentTaskComplete();
			counter++;

			// Other calculations
			if (!calculations.isEmpty()) {
				progress.setCurrentTask("Running further calculations...");
				if (calculations.contains(Analyzer.Calculation.PERCENT_MIGRATION) && this.rois.size() > 1) {

					PolarizedPolygonROI roiRef = this.tags.get(Tag.REF_PERCMIG);
					int numRoisCalcd = 1;
					for (PolarizedPolygonROI currRoi : this.rois) {
						if (currRoi.equals(roiRef))
							continue;

						progress.setCurrentTaskProgress(numRoisCalcd, this.rois.size() - 1);
						newTable.setHeading(counter, "%migrated " + roiRef + " to " + currRoi.getName());
						double[] firstLineDist = newTable.getColumnAsDoubles(columnsForEachROI.get(roiRef));
						double[] secondLineDist = newTable.getColumnAsDoubles(columnsForEachROI.get(currRoi));
						for (int j = 0; j < firstLineDist.length && j < secondLineDist.length; j++) {
							newTable.setValue(counter, j, Analyzer.calculate(Analyzer.Calculation.PERCENT_MIGRATION, firstLineDist[j], secondLineDist[j]));
						}
						counter++;
						numRoisCalcd++;
					}

				}
				progress.setCurrentTaskComplete();
			}


			System.gc();
			return newTable;
		} catch (Exception e) {
			e.printStackTrace();
			GUI.displayMessage("<html>SHOW THIS TO JUSTIN:<br><br> " + StringUtils.join(e.getStackTrace(), "<br>") + "</html>", "Error",this.gui.getComponent(),  JOptionPane.ERROR_MESSAGE);
		}


		return null;

	}

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

	public PolarizedPolygonROI selectPositiveRegionForCurrentROI(Point p) {
		this.selectingPositive = false;
		this.cache.clear();
		PolarizedPolygonROI roi = this.currentlyCreating;
		this.currentlyCreating = null;
		roi.setPositiveRegion(p.x, p.y);
		this.rois.add(roi);
		if (!this.tags.containsKey(Tag.REF_PERCMIG)) {
			this.tags.put(Tag.REF_PERCMIG, roi);
		}
		if (GUI.settings.calculateBins) {
			if (!this.tags.containsKey(Tag.BIN1)) {
				this.tags.put(Tag.BIN1, roi);
			} else if (!this.tags.containsKey(Tag.BIN2)) {
				this.tags.put(Tag.BIN2, roi);
			}
		}
		return roi;
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

		// Basically need to find points for constructing a line.
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

	public void deleteSerializedVersion() {
		this.ic.getSerializeFile(ImageContainer.STATE_ROI).delete();
	}

	private void writeObject(ObjectOutputStream stream)
			throws IOException {
		stream.defaultWriteObject();

	}

	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {

		// initialize transient vars
		this.cache = new HashMap<Channel, ImagePlus>();
		this.selectingPositive = false;
		this.multiSelectPopup = new MultiSelectPopup<Tag>();
		// read objects
		stream.defaultReadObject();
		this.gui = GUI.SINGLETON;

		if (!GUI.settings.calculateBins) {
			this.tags.remove(Tag.BIN1);
			this.tags.remove(Tag.BIN2);
		}

	}


	public static boolean saveROIEditableImage(ROIEditableImage image, File serialize) {
		try {
			FileOutputStream fileStream = new FileOutputStream(serialize); 
			ObjectOutputStream out = new ObjectOutputStream(fileStream); 
			out.writeObject(image); 
			out.close(); 
			fileStream.close();
			return true;
		}catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	public static ROIEditableImage loadROIEditableImage(File serialize) {
		try {

			FileInputStream fileInput = new FileInputStream(serialize); 
			ObjectInputStream in = new ObjectInputStream(fileInput); 


			ROIEditableImage loadedROIImage = (ROIEditableImage) in.readObject(); 
			//this.gui.getPanelDisplay().setImage(object1.get, zoom, clickX, clickY);
			in.close(); 
			fileInput.close(); 

			return loadedROIImage;

		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static class ROISelectMeta implements Serializable {

		private static final long serialVersionUID = -7227819070476401649L;
		private boolean tagsEdited = false;

		public ROISelectMeta() {
		}

		public void setTagsEdited() {
			this.tagsEdited = true;
		}

		public boolean haveTagsBeenEdited() {
			return this.tagsEdited;
		}

	}

}
enum Tag implements Displayable {
	REF_PERCMIG("Percent Migration Reference", "R", Color.RED), BIN1( "Binning Start", "B1", Color.BLUE), BIN2("Binning End", "B2", Color.BLUE);

	private String msg;
	private String abbrev;
	private Color color;
	private String htmlColor;
	private Tag(String message, String abbrev, Color color) {
		this.msg = message;
		this.abbrev = abbrev;
		this.color = color;
		this.htmlColor = "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue()+ ")";
	}

	public String getMsg() {
		return this.msg;
	}

	public String getAbbrev() {
		return this.abbrev;
	}

	public String getHTMLColor() {
		return this.htmlColor;
	}

	public String toString() {
		return this.msg;
	}

	@Override
	public String getListDisplayText() {
		return this.msg + " (" + this.abbrev + ")";
	}

	@Override
	public Color getListDisplayColor() {
		return this.color;
	}

	@Override
	public Font getListDisplayFont() {
		return GUI.smallBoldFont;
	}
}

