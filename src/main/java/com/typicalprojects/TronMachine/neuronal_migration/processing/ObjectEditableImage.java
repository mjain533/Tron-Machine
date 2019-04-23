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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import com.typicalprojects.TronMachine.neuronal_migration.processing.CounterHelper.ObjectColumn;
import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.ImagePanel;
import com.typicalprojects.TronMachine.util.Point;
import com.typicalprojects.TronMachine.util.Zoom;
import com.typicalprojects.TronMachine.util.ResultsTable;
import com.typicalprojects.TronMachine.neuronal_migration.*;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager.Channel;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlDisplay.PnlDisplayPage;

import java.util.Set;

import ij.ImagePlus;
import ij.gui.ImageRoi;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.process.ImageProcessor;

public class ObjectEditableImage implements Serializable{

	private static final long serialVersionUID = -5899135921935041272L; // for serialization
	private ImageContainer ic;
	private Map<Channel, List<Point>> points = new HashMap<Channel, List<Point>>();
	private int dotSize = ObjectCounter.opResultDotsSize;
	private int fontSize = ObjectCounter.opResultFontSize;
	private transient Zoom zoom = Zoom.ZOOM_100;
	private transient boolean creatingDeletionZone = false;
	private transient List<Point> deletionZone = new ArrayList<Point>();
	private transient OBJSelectMeta selectionStateData = null;
	
	private transient Channel postProcessChan1 = null;
	private transient Channel postProcessChan2 = null;
	private transient Map<String, PostProcessImage> postProcessImages = null;
	private transient boolean postObjDots = false;
	private transient boolean postObjMaxed = true;
	
	private transient boolean mask = true;
	private transient boolean original = true;
	private transient boolean dots = true;
	
	private transient ImagePanel imagePnl;
	private transient GUI gui;

	public ObjectEditableImage(GUI gui, ImageContainer ic, Map<String, ResultsTable> results) {
		this.gui = gui;
		this.imagePnl = gui.getPanelDisplay().getImagePanel();
		this.ic = ic;
		this.selectionStateData = new OBJSelectMeta();
		for (Channel chan : ic.getRunConfig().channelMan.getProcessChannels()) {
			this.points.put(chan, new LinkedList<Point>());
			this.selectionStateData.addChannelToLookAt(chan);
		}
		
		for (Entry<String, ResultsTable> en : results.entrySet()) {
			ResultsTable rt = en.getValue();
			Channel chan = ic.getRunConfig().channelMan.parse(en.getKey());
			if (chan == null)
				continue;
			double[] xCoords = rt.getColumnAsDoubles(ObjectColumn.X.getColumnNum());
			double[] yCoords = rt.getColumnAsDoubles(ObjectColumn.Y.getColumnNum());
			double[] zCoords = null;
			
			if (rt.columnExists(ObjectColumn.Z.getColumnNum())) { // Older serializations may not include Z values
				zCoords = rt.getColumnAsDoubles(ObjectColumn.Z.getColumnNum());
			}
			
			List<Point> chanPoints = points.get(chan);
			if (xCoords != null && yCoords != null) {
				
				if (zCoords == null) {
					for (int i = 0; i < xCoords.length; i++) {
						chanPoints.add(new Point(Math.round((float) xCoords[i]), Math.round((float) yCoords[i]), true));
					}
				} else {
					for (int i = 0; i < xCoords.length; i++) {
						chanPoints.add(new Point(Math.round((float) xCoords[i]), Math.round((float) yCoords[i]), Math.round((float) zCoords[i]), true));
					}
				}
				
			}
			
		}
		

	}

	public boolean deleteObjectsWithinDeletionZone(Channel chanToDisplayAfterward) {

		Object[] obj = convertDeletionPointsToArray(true);
		if (obj == null) {
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
	
	public RunConfiguration getRunConfig() {
		return this.ic.getRunConfig();
	}
	
	public OBJSelectMeta getSelectionStateMeta() {
		return this.selectionStateData;
	}

	public void createAndAddNewImagesToIC() {
		
		RunConfiguration rc = this.ic.getRunConfig();
		for (Channel chan : this.ic.getRunConfig().channelMan.getChannels()) {
			if (this.points.containsKey(chan)) {
				this.dots = true;
				this.mask = true;
				this.original = true;
				
				ImagePlus processedFull = getImgWithDots(chan, false, true);
				this.ic.addImage(OutputOption.ProcessedFull, chan, processedFull);
				// Needed this added for display during ROI selection, regardless of saving.
				
				if (rc.channelMan.hasOutput(OutputOption.ProcessedFull, chan)) {
					this.ic.saveSupplementalImage(OutputOption.ProcessedFull, processedFull, chan);
				}
								
				// Shouldn't need this if just implement the getImgWithDots method in ROIEditableImage.
				
				if (rc.channelMan.hasOutput(OutputOption.ProcessedDots, chan)) {
					this.dots = true;
					this.mask = false;
					this.original = false;
					this.ic.saveSupplementalImage(OutputOption.ProcessedDots, getImgWithDots(chan, false, true), chan);
				}
				if (rc.channelMan.hasOutput(OutputOption.ProcessedDotsNoNum, chan)) {
					this.dots = true;
					this.mask = false;
					this.original = false;
					this.ic.saveSupplementalImage(OutputOption.ProcessedDotsNoNum, getImgWithDots(chan, false, false), chan);
				}
				if (rc.channelMan.hasOutput(OutputOption.ProcessedDotsObjects, chan)) {
					this.dots = true;
					this.mask = true;
					this.original = false;
					this.ic.saveSupplementalImage(OutputOption.ProcessedDotsObjects, getImgWithDots(chan, false, true), chan);
				}
				if (rc.channelMan.hasOutput(OutputOption.ProcessedObjects, chan)) {
					this.dots = false;
					this.mask = true;
					this.original = false;
					this.ic.saveSupplementalImage(OutputOption.ProcessedObjects, getImgWithDots(chan, false, true), chan);
				}
				if (rc.channelMan.hasOutput(OutputOption.ProcessedObjectsOriginal, chan)) {
					this.dots = false;
					this.mask = true;
					this.original = true;
					this.ic.saveSupplementalImage(OutputOption.ProcessedObjectsOriginal, getImgWithDots(chan, false, true), chan);
				}
				if (rc.channelMan.hasOutput(OutputOption.ProcessedDotsOriginal, chan)) {
					this.dots = true;
					this.mask = false;
					this.original = true;
					this.ic.saveSupplementalImage(OutputOption.ProcessedDotsOriginal, getImgWithDots(chan, false, true), chan);
				}
			}
			
			if (rc.channelMan.hasOutput(OutputOption.MaxedChannel, chan)) {
				this.dots = false;
				this.mask = false;
				this.original = true;
				this.ic.saveSupplementalImage(OutputOption.MaxedChannel, getImgWithDots(chan, false, true), chan);

			}
			
		}
		this.dots = true;
		this.mask = true;
		this.original = true;
	}
	
	public ROIEditableImage convertToROIEditableImage() {

		return new ROIEditableImage(this.gui, this.getContainer(), this.points);
	}

	/*@SuppressWarnings("deprecation")
	public void updateResultsTables() {
		for (Entry<Channel, List<Point>> en : this.points.entrySet()) {
			ResultsTable rt = new ResultsTable();
			rt.setHeading(0, "ID");
			for (ObjectColumn col : ObjectColumn.values()) {
				rt.setHeading(col.getColumnNum() + 1, col.getTitle());
			}

			int counter = 0;
			for (Point p : en.getValue()) {
				rt.incrementCounter();
				rt.setValue("ID", counter, counter + 1);
				rt.setValue(ObjectColumn.X.getTitle(), counter, p.x);
				rt.setValue(ObjectColumn.Y.getTitle(), counter, p.y);

				counter++;
			}
			this.results.put(en.getKey().name(), rt);
		}
	}*/

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

		List<Point> chanPoints = this.points.get(chan);

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


			if (this.creatingDeletionZone) {

				Object[] obj = convertDeletionPointsToArray(false);

				if (obj != null) {
					if (this.deletionZone.size() == 1) {

						stack.getProcessor().setColor(Color.GREEN);
						int size = (int) (Math.max(stack.getDimensions()[0], stack.getDimensions()[1] ) / 100.0);
						stack.getProcessor().fillRect((int) ((float[]) obj[0])[0] - (size / 2), (int) ((float[]) obj[1])[0] - (size / 2), size, size);
						stack.updateImage();

					} else {
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
	
	public boolean checkPostObjectImages() {
		if (getRunConfig().channelMan.getChannels().size() < 3) {
			boolean resourcesPostProcess = true; // whether the proper resources exist for post-processing
			for (Channel chan : getRunConfig().channelMan.getProcessChannels()) {
				if (!this.ic.containsImage(OutputOption.Channel, chan) ||
						!this.ic.containsImage(OutputOption.ProcessedObjectsStack, chan))
					resourcesPostProcess = false;
			}
			
			if (resourcesPostProcess) {
				return true;
			} else {
				return false;
			}
		} else {
			GUI.displayMessage("Cannot perform post-object processing on this image. You "
					+ "have more than 2 channels selected for object (neuron) identification. "
					+ "Post-object processing is currently only supported for comparing two "
					+ "channels which had object processing.", "Post-Processing Error", gui.getPanelDisplay().getRawPanel(), JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}
	
	public List<PostProcessImage> createPostObjectImages() {
		
		List<PostProcessImage> imagesList = new ArrayList<PostProcessImage>();
		
		StackOverlapper overlapper = new StackOverlapper();
		List<Channel> processChans = getRunConfig().channelMan.getProcessChannels();
		
		if (processChans.get(0).getName().equalsIgnoreCase("green") || processChans.get(1).getName().equals("red")) {
			this.postProcessChan1 = processChans.get(0);
			this.postProcessChan1 = processChans.get(1);
		} else {
			this.postProcessChan1 = processChans.get(1);
			this.postProcessChan1 = processChans.get(0);
		}
		
		overlapper.setImageStacks(this.ic.getImage(OutputOption.Channel, this.postProcessChan1, false), 
				this.ic.getImage(OutputOption.Channel, this.postProcessChan2, false));
		
		overlapper.createOverlapPredictionStack();
		ImagePlus stack = overlapper.getResult();
		overlapper.maxProjectResult();
		imagesList.add(new PostProcessImage("Predicted Overlaps", stack, overlapper.getResult(), "PrOv"));
		overlapper.createOverlapPredictionStack();
		stack = overlapper.getResult();
		overlapper.maxProjectResult();
		imagesList.add(new PostProcessImage("Gradiated (Raw) Overlap", stack, overlapper.getResult(), "RawOv"));
		overlapper.mergeGreenRedInputs();
		stack = overlapper.getResult();
		overlapper.maxProjectResult();
		imagesList.add(new PostProcessImage("Original Channels Merge", stack, overlapper.getResult(), "Orig"));
		stack = null;
		
		overlapper.setImageStacks(this.ic.getImage(OutputOption.ProcessedObjectsStack, this.postProcessChan1, false), 
				this.ic.getImage(OutputOption.ProcessedObjectsStack, this.postProcessChan2, false));
		overlapper.mergeGreenRedInputs();
		stack = overlapper.getResult();
		overlapper.maxProjectResult();
		imagesList.add(2, new PostProcessImage("TRON Objects Merge", stack, overlapper.getResult(), "Obj"));

		for (PostProcessImage ppi : imagesList) {
			this.postProcessImages.put(ppi.getDisplayAbbrev(), ppi);
		}
		
		postObjDots = false;
		postObjMaxed = true;
		
		return imagesList;
	}
	
	public void setDisplayPostObjectDots(boolean displayDots) {
		this.postObjDots = displayDots;
		
		// TODO: need to cause re-display
	}
	
	public void setDisplayPostObjectMax(boolean displayMax) {
		postObjMaxed = displayMax;
		
		// TODO: need to cause re-display AND update options panels
	}
	
	public void getPostObjectImage(String abbrev) {
		
		PostProcessImage ppi = this.postProcessImages.get(abbrev);
		
	}
	
	private void writeObject(ObjectOutputStream stream)
			throws IOException {
		stream.defaultWriteObject();

	}

	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		
		// initialize transient vars
		this.zoom = Zoom.ZOOM_100;
		this.creatingDeletionZone = false;
		this.deletionZone = new ArrayList<Point>();
		
		this.mask = true;
		this.original = true;
		this.dots = true;
		
		this.selectionStateData = new OBJSelectMeta();
		
		// Read objects
		stream.defaultReadObject();
		for (Channel chan : this.points.keySet()) {
			this.selectionStateData.addChannelToLookAt(chan);
		}
		this.gui = GUI.SINGLETON;
		this.imagePnl = this.gui.getPanelDisplay().getImagePanel();

	}
	
	public void deleteSerializedVersion() {
		this.ic.getSerializeFile(ImageContainer.STATE_OBJ).delete();
	}
	
	public static boolean saveObjEditableImage(ObjectEditableImage image, File serializeFile) {
		try {

			FileOutputStream fileStream = new FileOutputStream(serializeFile); 
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
	
	public static ObjectEditableImage loadObjEditableImage(File serializeFile) {
		try {
			if (serializeFile.isDirectory())
				return null;
			if (!serializeFile.exists())
				return null;

	        FileInputStream fileInput = new FileInputStream(serializeFile); 
            ObjectInputStream in = new ObjectInputStream(fileInput); 

            ObjectEditableImage loadedROIImage = (ObjectEditableImage) in.readObject(); 
            //this.gui.getPanelDisplay().setImage(object1.get, zoom, clickX, clickY);
            in.close(); 
            fileInput.close(); 
            return loadedROIImage;

		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static class OBJSelectMeta {
		
		private Set<Channel> channelsToLookAt = new HashSet<Channel>();
		
		public OBJSelectMeta() {
		}
		
		public void addChannelToLookAt(Channel channelToExplore) {
			this.channelsToLookAt.add(channelToExplore);
		}
		
		public void lookAt(Channel chan) {
			this.channelsToLookAt.remove(chan);
		}
		
		public Channel getChannelNotLookedAt() {
			if (this.channelsToLookAt.isEmpty())
				return null;
			
			return this.channelsToLookAt.iterator().next();
		}
		
	}
	
	public static class PostProcessImage implements PnlDisplayPage {

		private ImagePlus ip;
		private ImagePlus ipMaxed;
		private String title;
		private String abbrev;
		
		private PostProcessImage(String title, ImagePlus ipStack, ImagePlus ipMaxed, String abbreviation) {
			this.title = title;
			this.ip = ipStack;
			this.ipMaxed = ipMaxed;
			this.abbrev = abbreviation;
		}
		
		public String getDisplayAbbrev() {
			return abbrev;
		}
		
		public ImagePlus getImageStack() {
			return this.ip;
		}
		
		public ImagePlus getImageMaxed() {
			return this.ip;
		}
		
		public String getTitle(boolean max) {
			if (!max) {
				return this.title;

			} else {
				return this.title.concat(" (Max Z-Project)");
			}
		}
		
		public boolean isStack() {
			return this.ip.isStack();
		}
		
	}

}

