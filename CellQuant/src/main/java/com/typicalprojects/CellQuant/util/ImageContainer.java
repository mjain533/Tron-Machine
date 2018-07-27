package com.typicalprojects.CellQuant.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.measure.ResultsTable;

public class ImageContainer {
	
	private static final String INTERMED_FILES = "Intermediate Files";
	
	private List<Channel> channels = new ArrayList<Channel>();
	private List<ImagePlus> images = new ArrayList<ImagePlus>();
	
	private Map<String, ImagePlus> supplementaryImages = new HashMap<String, ImagePlus>();
	
	private String title;
	private File imgFile;
	private Calibration cal;
	private File outputLocation;
	private String timeOfRun;

	public ImageContainer(ImagePlus[] img, String fileTitle, File imgFile, Calibration cal, Map<Integer, Channel> validChannels, File outputLocation, String timeOfRun) throws IllegalArgumentException{
		this.imgFile = imgFile;
		this.cal = cal;
		this.title = fileTitle;
		this.outputLocation = outputLocation;
		this.timeOfRun = timeOfRun;
		System.out.println("Opened 2");

		makeSaveDirectory(this.title, this.outputLocation,this.timeOfRun);

		if (img.length == 0)
			throw new IllegalArgumentException("There are no channels for " + fileTitle + ".");

		for (int i = 0; i < 4 && i < img.length; i++) {

			if (!validChannels.containsKey(i)) {
				continue;
			}
			img[i].setIJMenuBar(true);
			if (!fileTitle.contains("Chan-")) {
				
				img[i].setTitle(fileTitle + " Chan-" + validChannels.get(i).getAbbreviation());
			}
			img[i].setCalibration(cal);
			channels.add(validChannels.get(i));
			images.add(img[i]);

		}
		
		deleteImgFiles();

	}
	
	private ImageContainer(List<ImagePlus> newImages, List<Channel> newChannels, ImageContainer container) throws IllegalArgumentException{
		this.imgFile = container.imgFile;
		this.cal = container.cal;
		this.title = container.title;
		this.outputLocation = container.outputLocation;
		this.timeOfRun = container.timeOfRun;
		System.out.println("Opened 1");
		makeSaveDirectory(this.title, this.outputLocation, this.timeOfRun);

		if (newImages.size() == 0)
			throw new IllegalArgumentException("There are no channels for " + this.title + ".");

		for (int i = 0; i < newImages.size(); i++) {

			if (!newImages.get(i).getTitle().contains("Chan-")) {
				
				newImages.get(i).setTitle(this.title + " Chan-" + newChannels.get(i).getAbbreviation());
			}
			newImages.get(i).setCalibration(cal);
			this.channels.add(newChannels.get(i));
			this.images.add(newImages.get(i));

		}
		
		deleteImgFiles();

	}
	
	
	public ImageContainer(List<Channel> channelsToSet, List<ImagePlus> imagesToSet, Map<String, ImagePlus> supplementalImgs, String title, File imgFile, Calibration cal, File outputLocation, String timeOfRun) {
		this.cal = cal;
		this.imgFile = imgFile;
		this.outputLocation = outputLocation;
		this.timeOfRun = timeOfRun;
		
		makeSaveDirectory(title, outputLocation, timeOfRun);


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
		
		System.out.println("Opened ");
		if (supplementalImgs != null) {
			System.out.println("With size of " + supplementalImgs.size());

			this.supplementaryImages = supplementalImgs;
			
			deleteSuppImgFiles();
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
	
	private void deleteSuppImgFiles() {
		File intDir = this.getIntermediateFilesDirectory();
		if (intDir.isDirectory()) {
			for (File file : intDir.listFiles()) {
				if (!file.isDirectory() && file.getName().endsWith(".tiff") && file.getName().startsWith("SUPP IMG ")) {
					file.delete();
				}
			}
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
		if (indexOf < 0)
			return null;
		
		if (duplicate) {
			ImagePlus newImg = images.get(indexOf).duplicate();
			newImg.setTitle(newImg.getTitle().substring(4));
			return newImg;
		} else {
			return images.get(indexOf);
		}
	}

	public ImagePlus getImage(Channel channel, int stackPosition, boolean duplicate) {

		int indexOf = channels.indexOf(channel);


		ImagePlus ip = new ImagePlus(images.get(indexOf).getTitle() + " Slc-" + stackPosition, images.get(indexOf).getImageStack().getProcessor(stackPosition));
		
		if (duplicate) {
			ImagePlus newImg = ip.duplicate();
			newImg.setTitle(newImg.getTitle().substring(4));
			return newImg;
		} else {
			return ip;
		}
	}

	public ImagePlus getImage(int channelNum, int stackPosition, boolean duplicate) {


		ImagePlus ip = new ImagePlus(images.get(channelNum).getTitle() + " Slc-" + stackPosition, images.get(channelNum).getImageStack().getProcessor(stackPosition));
		if (duplicate) {
			ImagePlus newImg = ip.duplicate();
			newImg.setTitle(newImg.getTitle().substring(4));
			return newImg;
		} else {
			return ip;
		}
	}
	
	public ImagePlus getSupplementalImage(String key) {
		return this.supplementaryImages.get(key);
	}
	
	public Map<String, ImagePlus> getAllSupplementalImages() {
		return this.supplementaryImages;
	}
	
	public void addSupplementalImage(String key, ImagePlus image) {
		System.out.println("Added via method");
		this.supplementaryImages.put(key, image);
	}

	public Channel getChannel(int num) {
		return channels.get(num);
	}

	public ImageContainer setSliceRegion(int lowSlice, int highSlice) {

		List<ImagePlus> newImages = new ArrayList<ImagePlus>();

		for (int i = 0; i < images.size(); i++) {
			ImagePlus img = images.get(i);
			ImageStack is = img.getStack().duplicate();
			for (int s = (is.getSize() - highSlice); s > 0; s--) {
				is.deleteLastSlice();
			}

			for (int s = lowSlice; s > 1; s--) {
				is.deleteSlice(1);
			}
			newImages.add(new ImagePlus(img.getTitle(), is));
		}

		return new ImageContainer(newImages, this.channels, this);

	}

	private void deleteResultsTables(String date) {

		for (Channel chan : Channel.values()) {
			ImagePlus potential = getImageChannel(chan, false);
			if (potential != null) {
				File file = new File(getSaveDirectory() + File.separator + getImageChannel(chan, false).getTitle() + ".txt");
				if (file.exists()) {
					file.delete();
				}

			}
		}
	}

	public void saveResultsTable(Map<Channel, ResultsTable> results, String date, boolean excel) {

		deleteResultsTables(date);

		String ext = excel ? ".xlsx" : ".txt";
		
		if (excel) {
			AdvancedWorkbook aw = new AdvancedWorkbook();
			for (Entry<Channel, ResultsTable> en : results.entrySet()) {
				if (en.getValue() == null)
					continue;
				aw.addSheetFromNeuronCounterResultTable(en.getKey().name(), en.getValue());
			}

			aw.save(new File(getSaveDirectory() + File.separator + this.title + " ANALYSIS.xlsx"));
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


	public void save(boolean saveSupp) {
		
		try {
			String savePathPrefix = this.getIntermediateFilesDirectory().getPath() + File.separator;
			for (int i = 0; i < images.size(); i++) {

				String savePath = savePathPrefix + images.get(i).getTitle() + ".tiff";
				
				if (images.get(i).getStackSize() > 1) {
					new FileSaver(images.get(i)).saveAsTiffStack(savePath);
				} else {
					new FileSaver(images.get(i)).saveAsTiff(savePath);
				}
			}
			
			if (saveSupp) {
				for (Entry<String, ImagePlus> en : this.supplementaryImages.entrySet()) {

					String savePath = savePathPrefix + "SUPP IMG " + en.getKey() + ".tiff";
					
					if (en.getValue().getStackSize() > 1) {
						new FileSaver(en.getValue()).saveAsTiffStack(savePath);
					} else {
						new FileSaver(en.getValue()).saveAsTiff(savePath);
					}
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}


	}
	
	public Map<Channel, ResultsTable> tryToOpenResultsTables(List<Channel> channelsToOpenResults) {
		
		Map<Channel, ResultsTable> results = new HashMap<Channel, ResultsTable>();
		
		for (Channel chan : channelsToOpenResults) {
			results.put(chan, ResultsTable.open2(getSaveDirectory(this.title, this.outputLocation, this.timeOfRun) + File.separator + getImageChannel(chan, false).getTitle() + ".txt"));

		}		

		return results;

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
	

	
	public synchronized int getMin(Channel channel) {

		return (int) this.images.get(this.channels.indexOf(channel)).getDisplayRangeMin();
		
	}
	
	public synchronized int getMax(Channel channel) {
		return (int) this.images.get(this.channels.indexOf(channel)).getDisplayRangeMax();
	}
	
	private static void makeSaveDirectory(String imageTitle, File outputLocation, String timeOfRun) {
		File file = new File(outputLocation.getPath() + File.separator + imageTitle + " " + timeOfRun);
		if (!file.isDirectory()) {
			file.mkdir();
		}
		File intermed = new File(file.getPath() + File.separator + INTERMED_FILES);
		if (!intermed.isDirectory()) {
			intermed.mkdir();
		}
	}
	
	public File getSaveDirectory() throws IllegalStateException{
		
		if (this.outputLocation == null) {
			throw new IllegalStateException();
		}
		return new File(this.outputLocation.getPath() + File.separator + this.title + " " + this.timeOfRun);
	}
	
	public File getIntermediateFilesDirectory()throws IllegalStateException {
		if (this.outputLocation == null) {
			throw new IllegalStateException();
		}
		return new File(this.outputLocation.getPath() + File.separator + this.title + " " + this.timeOfRun + File.separator + INTERMED_FILES);

	}
	
	public static File getSaveDirectory(String imageTitle, File outputLocation, String timeOfRun) throws IllegalStateException {

		makeSaveDirectory(imageTitle, outputLocation, timeOfRun);
		return new File(outputLocation.getPath() + File.separator + imageTitle + " " + timeOfRun);

	}
	
	
	public static File getIntermediateFilesDirectory(String imageTitle, File outputLocation, String timeOfRun) throws IllegalStateException {
		if (outputLocation == null) {
			throw new IllegalStateException();
		}
		makeSaveDirectory(imageTitle, outputLocation, timeOfRun);
		return new File(outputLocation.getPath() + File.separator + imageTitle + " " + timeOfRun + File.separator + INTERMED_FILES);

	}
	

	
}
