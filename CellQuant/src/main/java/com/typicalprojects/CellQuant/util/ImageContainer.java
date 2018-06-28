package com.typicalprojects.CellQuant.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.typicalprojects.CellQuant.neuronal_migration.GUI;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.measure.ResultsTable;

public class ImageContainer {
	
	private static final String INTERMED_FILES = "Intermediate Files";
	
	private List<Channel> channels = new ArrayList<Channel>();
	private List<ImagePlus> images = new ArrayList<ImagePlus>();
	
	private String title;
	private File imgFile;
	private Calibration cal;

	public ImageContainer(ImagePlus[] img, String fileTitle, File imgFile, Calibration cal) throws IllegalArgumentException{
		this.imgFile = imgFile;
		this.cal = cal;
		this.title = fileTitle;

		makeSaveDirectory(title);

		if (img.length == 0)
			throw new IllegalArgumentException("There are no channels for " + fileTitle + ".");

		for (int i = 0; i < 4 && i < img.length; i++) {

			if (!GUI.channelMap.containsKey(i)) {
				continue;
			}
			img[i].setIJMenuBar(true);
			if (!fileTitle.contains("Chan-")) {
				img[i].setTitle(fileTitle + " Chan-" + GUI.channelMap.get(i).getAbbreviation());
			}
			img[i].setCalibration(cal);
			channels.add(GUI.channelMap.get(i));
			images.add(img[i]);

		}
		
		deleteImgFiles();

	}
	
	
	public ImageContainer(List<Channel> channelsToSet, List<ImagePlus> imagesToSet, String title, File imgFile, boolean sameStackSize, Calibration cal) {
		this.cal = cal;
		this.imgFile = imgFile;
		
		makeSaveDirectory(title);


		if (channelsToSet.size() == 0 || channelsToSet.size() != imagesToSet.size()) {
			
			throw new IllegalArgumentException("Invalid channel numbers for " + title + ".");
		}		

		this.title = title;
		for (int i = 0; i < channelsToSet.size(); i++) {

			imagesToSet.get(i).setIJMenuBar(true);
			if (!title.contains("Chan-")) {
				imagesToSet.get(i).setTitle(title + " Chan-" + channelsToSet.get(i).getAbbreviation());
			}
			imagesToSet.get(i).setCalibration(cal);
			this.channels.add(channelsToSet.get(i));
			this.images.add(imagesToSet.get(i));

		}

		
		deleteImgFiles();

	}
	
	public Calibration getCalibration() {
		return this.cal;
	}
	
	public File getImgFile() {
		return this.imgFile;
	}
	

	public String getTotalImageTitle() {
		return this.title;
	}

	private void deleteImgFiles() {
		for (int i = 0; i < images.size(); i++) {
			new File(this.getIntermediateFilesDirectory().getPath() + File.separator + images.get(i).getTitle() + ".tiff").delete();

		}

	}

	public int getStackSize(Channel chan) {

		return images.get(channels.indexOf(chan)).getStackSize();
	}

	public int getNumberOfChannels() {
		return channels.size();
	}

	public List<Channel> getChannels() {
		return channels;
	}

	public ImagePlus getImageChannel(Channel channel, boolean duplicate) {

		int indexOf = channels.indexOf(channel);

		return (duplicate ? images.get(indexOf).duplicate() : images.get(indexOf));
	}

	public ImagePlus getImage(Channel channel, int stackPosition, boolean duplicate) {

		int indexOf = channels.indexOf(channel);


		ImagePlus ip = new ImagePlus(images.get(indexOf).getTitle() + " Slc-" + stackPosition, images.get(indexOf).getImageStack().getProcessor(stackPosition));
		return (duplicate ? ip.duplicate() : ip);
	}

	public ImagePlus getImage(int channelNum, int stackPosition, boolean duplicate) {


		ImagePlus ip = new ImagePlus(images.get(channelNum).getTitle() + " Slc-" + stackPosition, images.get(channelNum).getImageStack().getProcessor(stackPosition));
		return (duplicate ? ip.duplicate() : ip);
	}

	public Channel getChannel(int num) {
		return channels.get(num);
	}

	public ImageContainer setSliceRegion(int lowSlice, int highSlice) {

		ImagePlus[] newImages = new ImagePlus[images.size()];

		for (int i = 0; i < images.size(); i++) {
			ImagePlus img = images.get(i);
			ImageStack is = img.getStack().duplicate();
			for (int s = (is.getSize() - highSlice); s > 0; s--) {
				is.deleteLastSlice();
			}

			for (int s = lowSlice; s > 1; s--) {
				is.deleteSlice(1);
			}
			newImages[i] = new ImagePlus(img.getTitle(), is);
		}

		return new ImageContainer(newImages, this.title, this.imgFile, this.cal);

	}

	private void deleteResultsTables(String date) {

		for (Channel chan : GUI.channelsToProcess) {
			File file = new File(getSaveDirectory() + File.separator + getImageChannel(chan, false).getTitle() + ".txt");
			if (file.exists()) {
				file.delete();
			}
		}
	}

	public void saveResultsTable(Map<Channel, ResultsTable> results, String date, boolean excel) {

		deleteResultsTables(date);

		String ext = excel ? ".xlsx" : ".txt";
		
		if (excel) {
			AdvancedWorkbook aw = new AdvancedWorkbook();
			for (Channel chan : GUI.channelsToProcess) {
				ResultsTable table = results.get(chan);
				if (table == null)
					continue;
				
				aw.addSheetFromResultTable(chan.name(), table);
			}
			aw.save(new File(getSaveDirectory() + File.separator + "analysis.xlsx"));
			return;
		}
		for (Entry<Channel, ResultsTable> en : results.entrySet()) {
			try {
				en.getValue().saveAs(this.getSaveDirectory().getPath() + File.separator + getImageChannel(en.getKey(), false).getTitle() + ext);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	public void save(String date) {
		
		try {
			for (int i = 0; i < images.size(); i++) {

				String savePath = this.getIntermediateFilesDirectory().getPath() + File.separator + images.get(i).getTitle() + ".tiff";
				
				if (images.get(i).getStackSize() > 1) {
					new FileSaver(images.get(i)).saveAsTiffStack(savePath);
				} else {
					new FileSaver(images.get(i)).saveAsTiff(savePath);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	
	

	public enum Channel {
		GREEN("G"), RED("R"), BLUE("B"), WHITE("W");

		private String abbrev;

		private Channel(String abbrev) {
			this.abbrev = abbrev;
		}

		public String getAbbreviation() {
			return this.abbrev;
		}

		public static Channel getChannelByAbbreviation(String abbrev) {

			for (Channel chan : values()) {
				if (chan.getAbbreviation().equals(abbrev)) {
					return chan;
				}
			}
			return null;
		}

		public String toString() {
			return this.abbrev;
		}


	}
	
	public synchronized void applyMinMax(Channel channel, int min, int max) {
		ImagePlus ip = this.images.get(this.channels.indexOf(channel));
		ip.setDisplayRange(min, max);
		ip.updateImage();
	}
	
	public synchronized int getMin(Channel channel) {

		return (int) this.images.get(this.channels.indexOf(channel)).getDisplayRangeMin();
		
	}
	
	public synchronized int getMax(Channel channel) {
		return (int) this.images.get(this.channels.indexOf(channel)).getDisplayRangeMax();
	}
	
	private static void makeSaveDirectory(String title) {
		if (GUI.outputLocation == null) {
			throw new IllegalStateException();
		}
		File file = new File(GUI.outputLocation.getPath() + File.separator + title + " " + GUI.dateString);
		if (!file.isDirectory()) {
			file.mkdir();
		}
		File intermed = new File(file.getPath() + File.separator + INTERMED_FILES);
		if (!intermed.isDirectory()) {
			intermed.mkdir();
		}
	}
	
	public File getSaveDirectory() throws IllegalStateException{
		
		if (GUI.outputLocation == null) {
			throw new IllegalStateException();
		}
		return new File(GUI.outputLocation.getPath() + File.separator + this.title + " " + GUI.dateString);
	}
	
	public File getIntermediateFilesDirectory()throws IllegalStateException {
		if (GUI.outputLocation == null) {
			throw new IllegalStateException();
		}
		return new File(GUI.outputLocation.getPath() + File.separator + this.title + " " + GUI.dateString + File.separator + INTERMED_FILES);

	}
	
	public static File getSaveDirectory(String imageTitle) throws IllegalStateException {
		if (GUI.outputLocation == null) {
			throw new IllegalStateException();
		}
		makeSaveDirectory(imageTitle);
		return new File(GUI.outputLocation.getPath() + File.separator + imageTitle + " " + GUI.dateString);

	}
	
	
	public static File getIntermediateFilesDirectory(String imageTitle) throws IllegalStateException {
		if (GUI.outputLocation == null) {
			throw new IllegalStateException();
		}
		makeSaveDirectory(imageTitle);
		return new File(GUI.outputLocation.getPath() + File.separator + imageTitle + " " + GUI.dateString + File.separator + INTERMED_FILES);

	}
	
}
