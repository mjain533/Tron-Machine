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
package com.typicalprojects.TronMachine.util;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.typicalprojects.TronMachine.neuronal_migration.OutputOption;
import com.typicalprojects.TronMachine.neuronal_migration.OutputParams;

import ij.ImagePlus;
import ij.ImageStack;
import ij.io.FileInfo;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

public class ImageContainer {


	private static final String INTERMED_FILES = "Intermediate Files";

	private Map<OutputOption, Map<Channel, ImagePlus>> images = new HashMap<OutputOption, Map<Channel, ImagePlus>>();

	private String title;
	private transient Calibration cal;
	private transient File outputLocation;
	private transient String timeOfRun;
	private transient File imageFile;
	private int[] dimensions;

	public ImageContainer(String title, File imageFile, File outputLocation, String timeOfRun, List<OutputOption> imagesToOpen, Map<Integer, Channel> validChannels, Calibration cal) throws ImageOpenException {

		try {
			this.cal = cal;
			this.title = title;
			this.outputLocation = outputLocation;
			this.timeOfRun = timeOfRun;
			this.imageFile = imageFile;
			makeSaveDirectory(this.title, this.outputLocation, this.timeOfRun);

			if (imagesToOpen.size() == 0)
				throw new IllegalArgumentException("There are no images for " + this.title + ".");

			for (OutputOption it : imagesToOpen) {
				if (it.equals(OutputOption.Channel)) {

					ImporterOptions io = new ImporterOptions();
					io.setId(imageFile.getPath());
					io.setSplitChannels(true);
					ImagePlus[]  ips= BF.openImagePlus(io);
					this.cal = ips[0].getCalibration();

					if (this.cal != null) {

					}
					if (ips.length != validChannels.size()) {
						throw new ImageOpenException("Incorrect channel configuration. Please use Preferences to specify channel mapping.");
					}
					Map<Channel, ImagePlus> origImages = new HashMap<Channel, ImagePlus>();
					for (Entry<Integer, Channel> chanEn : validChannels.entrySet()) {
						if (chanEn.getKey() < ips.length) {
							ImagePlus ip = ips[chanEn.getKey()];
							ip.setProcessor(ip.getProcessor().convertToShortProcessor());
							ip.setTitle(this.title + " Chan-" + chanEn.getValue().getAbbreviation());
							if (this.dimensions == null) this.dimensions = ip.getDimensions();
							this.applyLUT(ip, chanEn.getValue());
							origImages.put(chanEn.getValue(), ip);
						} else {
							throw new ImageOpenException("Incorrect channel configuration. Please use Preferences to specify channel mapping.");
						}
					}

					this.images.put(OutputOption.Channel, origImages);
				} else if (it.equals(OutputOption.ChannelTiff)) {

					Opener opener = new Opener();
					Map<Channel, ImagePlus> origImages = new HashMap<Channel, ImagePlus>();

					File intermediateFilesDir = getIntermediateFilesDirectory();


					for (Channel chan : validChannels.values()){
						File file = new File(intermediateFilesDir + File.separator + title + " Chan-" + chan.getAbbreviation() + ".tiff");
						if (file.exists()) {
							ImagePlus ip = opener.openImage(file.getPath());
							ip.setCalibration(this.cal);
							ip.setTitle(this.title + " Chan-" + chan.getAbbreviation());
							if (this.dimensions == null) this.dimensions = ip.getDimensions();
							origImages.put(chan, ip);
						}

					}

					if (origImages.size() != validChannels.size()) {
						throw new ImageOpenException("Incorrect channel configuration. Please use Preferences to specify channel mapping.");
					}

					this.images.put(OutputOption.ChannelTiff, origImages);
				} else {
					this.images.put(it, openSupplementalImages(it));
					if (this.dimensions == null) {
						Collection<ImagePlus> ip = this.images.get(it).values();
						if (!ip.isEmpty()) {
							this.dimensions = ip.iterator().next().getDimensions();
						}
					}

				}
			}

			if (this.dimensions[0] < 50 || this.dimensions[1] < 50) {
				throw new Exception("Image size is too small (must be at least 50x50 pixels)");
			}


		} catch (Exception e) {
			e.printStackTrace();
			throw new ImageOpenException(e.getMessage());
		}



	}

	public File getImageFile() {
		return this.imageFile;
	}

	public Calibration getCalibration() {
		return this.cal;
	}

	public int[] getDimensions() {
		return this.dimensions;
	}

	public String getImageTitle() {
		return this.title;
	}

	public void deleteSuppImgFiles(OutputOption it) {
		File intDir = this.getIntermediateFilesDirectory();
		if (intDir.isDirectory()) {
			for (File file : intDir.listFiles()) {
				if (!file.isDirectory() && file.getName().endsWith(".tiff") && file.getName().substring(0, file.getName().lastIndexOf(".tiff")).endsWith(it.getImageSuffix())) {
					file.delete();
				}
			}
		}

	}


	/**
	 * 
	 * @throws NullPointerException if channel doesn't exist or originals aren't open.
	 */
	public ImagePlus getChannelOrig(Channel channel, boolean duplicate) throws NullPointerException {

		ImagePlus ip = getOriginals().get(channel);

		if (ip == null) {
			throw new UnopenedException();
		} else if (duplicate) {
			ImagePlus newImg = ip.duplicate();
			newImg.setTitle(newImg.getTitle().substring(4));
			// REMOVE DUP TAG
			return newImg;
		} else {
			return ip;
		}
	}

	public Map<Channel, ImagePlus> getOriginals() {
		Map<Channel, ImagePlus> ips = this.images.get(OutputOption.Channel);
		if (ips != null)
			return ips;
		else {
			ips = this.images.get(OutputOption.ChannelTiff);
			if (ips != null)
				return ips;
			else
				throw new UnopenedException();
		}
	}

	/**
	 * 
	 * @throws NullPointerException if channel doesn't exist or there is no image stack.
	 */
	public ImagePlus getChannelSliceOrig(Channel channel, int stackPosition, boolean duplicate) throws NullPointerException{

		ImagePlus fullImage = getOriginals().get(channel);
		ImagePlus ip = new ImagePlus(fullImage.getTitle() + " Slc-" + stackPosition, fullImage.getImageStack().getProcessor(stackPosition));

		if (duplicate) {
			ImagePlus newImg = ip.duplicate();
			newImg.setTitle(newImg.getTitle().substring(4));
			// REMOVE DUP TAG
			return newImg;
		} else {
			return ip;
		}

	}

	public ImagePlus getImage(OutputOption it, Channel chan, boolean duplicate) {
		Map<Channel, ImagePlus> ips = this.images.get(it);

		if (ips == null)
			throw new UnopenedException();
		ImagePlus ip = ips.get(chan);
		if (ip == null)
			throw new UnopenedException();

		if (duplicate) {
			ImagePlus newImg = ip.duplicate();
			newImg.setTitle(newImg.getTitle().substring(4));
			// REMOVE DUP TAG
			return newImg;
		} else {
			return ip;
		}

	}

	public Map<Channel, ImagePlus> openSupplementalImages(OutputOption it) {

		File intermediateFilesDir = getIntermediateFilesDirectory(this.title, this.outputLocation, timeOfRun);
		Opener opener = new Opener();

		Map<Channel, ImagePlus> supp = new HashMap<Channel, ImagePlus>();
		if (intermediateFilesDir.isDirectory()) {
			for (File file : intermediateFilesDir.listFiles()) {
				if (!file.isDirectory() && file.getName().substring(0, file.getName().lastIndexOf('.')).endsWith(" " + it.getImageSuffix())) {

					ImagePlus ip = opener.openImage(file.getPath());
					ip.setCalibration(this.cal);

					if (file.getName().contains("Chan-")) {
						String ending = file.getName().substring(file.getName().indexOf("Chan-"));
						if (ending.length() >= 6 && Channel.getChannelByAbbreviation(ending.charAt(5) + "") != null) {
							Channel chan = Channel.getChannelByAbbreviation(ending.charAt(5) + "");
							ip.setTitle(this.title + " Chan-" + ending.charAt(5));
							supp.put(chan, ip);
						} else {
							ip.setTitle(this.title);
							supp.put(null, ip);
						}
					} else {
						ip.setTitle(this.title);
						supp.put(null, ip);
					}

				}
			}
		}

		return supp;
	}

	public ImagePlus openSupplementalImage(OutputOption it) {

		File intermediateFilesDir = getIntermediateFilesDirectory(this.title, this.outputLocation, timeOfRun);
		Opener opener = new Opener();

		if (intermediateFilesDir.isDirectory()) {
			for (File file : intermediateFilesDir.listFiles()) {
				if (!file.isDirectory() && file.getName().substring(0, file.getName().lastIndexOf('.')).endsWith(" " + it.getImageSuffix())) {

					ImagePlus ip = opener.openImage(file.getPath());
					ip.setCalibration(this.cal);

					ip.setTitle(this.title);
					return ip;

				}
			}
		}

		return null;
	}

	public ImagePlus openSupplementalImage(OutputOption it, Channel chan) {
		File intermediateFilesDir = getIntermediateFilesDirectory(this.title, this.outputLocation, timeOfRun);
		Opener opener = new Opener();

		if (intermediateFilesDir.isDirectory()) {
			for (File file : intermediateFilesDir.listFiles()) {
				if (!file.isDirectory() && file.getName().substring(0, file.getName().lastIndexOf('.')).endsWith(" " + it.getImageSuffix())) {

					if (file.getName().contains("Chan-")) {
						String ending = file.getName().substring(file.getName().indexOf("Chan-"));
						if (ending.length() >= 6 && Channel.getChannelByAbbreviation(ending.charAt(5) + "") != null) {

							if (Channel.getChannelByAbbreviation(ending.charAt(5) + "").equals(chan)) {
								ImagePlus ip = opener.openImage(file.getPath());
								ip.setCalibration(this.cal);
								ip.setTitle(this.title + " Chan-" + ending.charAt(5));
								return ip;							
							}
						}
					}

				}
			}
		}

		return null;
	}


	public void setSliceRegion(int lowSlice, int highSlice) {

		Map<Channel, ImagePlus> originals = getOriginals();
		if (originals == null)
			throw new NullPointerException();

		for (Entry<Channel, ImagePlus> en : originals.entrySet()) {
			ImageStack is = en.getValue().getStack().duplicate();
			for (int s = (is.getSize() - highSlice); s > 0; s--) {
				is.deleteLastSlice();
			}

			for (int s = lowSlice; s > 1; s--) {
				is.deleteSlice(1);
			}
			en.setValue(new ImagePlus(en.getValue().getTitle(), is));
		}

	}

	private void deleteResultsTables() {
		File intDir = getIntermediateFilesDirectory();
		if (intDir.isDirectory()) {
			for (File file : intDir.listFiles()) {
				if (!file.isDirectory() && file.getName().endsWith(".txt")) {
					file.delete();
				}
			}
		}

	}

	public void deleteIrrelevantDataExcept(Map<OutputOption, OutputParams> tags) {
		File intDir = getIntermediateFilesDirectory();
		if (intDir.isDirectory()) {
			for (File file : intDir.listFiles()) {
				if (!file.isDirectory()) {
					boolean delete = true;
					for (Entry<OutputOption,OutputParams> en : tags.entrySet()) {
						if (en.getKey() == OutputOption.Channel) {

							for (Channel chan : en.getValue().includedChannels) {
								if (file.getName().endsWith("Chan-" + chan.getAbbreviation() + ".tiff")) {
									delete = false;
									break;
								}
							}
							
						} else if (file.getName().endsWith(" " + en.getKey().getImageSuffix() + ".tiff")) {
							if (en.getKey().getRestrictedOption() == OutputOption.NO_CHANS) {
								delete = false;
								break;
							} else {
								for (Channel chan : en.getValue().includedChannels) {
									if (file.getName().contains("Chan-" + chan.getAbbreviation())) {
										delete = false;
										break;
									}
								}
							}

						}
					}
					if (delete)
						file.delete();
				}
			}
		}
	}

	public void saveOrigImageStacksAsTiffs() {

		for (Entry<Channel, ImagePlus> en : this.images.get(OutputOption.Channel).entrySet()) {

			new FileSaver(en.getValue()).saveAsTiffStack(this.getIntermediateFilesDirectory() + File.separator + this.title + " Chan-" + en.getKey() + ".tiff");
		}
	}

	public void saveResultsTables(Map<String, ResultsTable> results, boolean excel) {

		deleteResultsTables();

		String ext = excel ? ".xlsx" : ".txt";

		if (excel) {
			AdvancedWorkbook aw = new AdvancedWorkbook();
			List<String> keys = new ArrayList<String>(results.keySet());

			for (Channel chan : Channel.values()) {
				if (keys.contains(chan.name())) {
					aw.addSheetFromNeuronCounterResultTable(chan.name(), results.get(chan.name()));
					keys.remove(chan.name());
				}
			}
			for (String key : keys) {
				aw.addSheetFromNeuronCounterResultTable(key, results.get(key));

			}

			aw.save(new File(getSaveDirectory() + File.separator + this.title + " ANALYSIS.xlsx"));
			return;
		}

		for (Entry<String, ResultsTable> en : results.entrySet()) {
			if (en.getValue() != null) {
				try {
					en.getValue().saveAs(getIntermediateFilesDirectory() + File.separator + this.title + " results " + en.getKey() + ext);
				} catch (IllegalStateException e) {
					// Wont happen
					e.printStackTrace();
				} catch (IOException e) {
					// Wont happen
					e.printStackTrace();
				}
			}

		}


	}

	public void saveSupplementalImage(OutputOption it, ImagePlus image) {
		saveSupplementalImage(it, image, null);
	}

	public void saveSupplementalImage(OutputOption it, ImagePlus image, Channel chan) {
		String title = this.title.concat(chan != null ? " Chan-" + chan.abbrev : "");
		image.setTitle(title);
		String savePath = getIntermediateFilesDirectory().getPath() + File.separator + title.concat(" ").concat(it.getImageSuffix()).concat(".tiff");

		if (image.getStackSize() > 1) {
			new FileSaver(image).saveAsTiffStack(savePath);

		} else {
			new FileSaver(image).saveAsTiff(savePath);

		}
		if (!this.images.containsKey(it))
			this.images.put(it, new HashMap<Channel, ImagePlus>());

		this.images.get(it).put(chan, image);
	}

	public Map<String, ResultsTable> tryToOpenResultsTables() {


		File intermediateFilesDir = getIntermediateFilesDirectory(this.title, this.outputLocation, timeOfRun);

		Map<String, ResultsTable> results = new HashMap<String, ResultsTable>();

		if (intermediateFilesDir.isDirectory()) {
			for (File file : intermediateFilesDir.listFiles()) {
				if (!file.isDirectory() && file.getName().endsWith(".txt") && file.getName().contains(" results ")) {

					String nameNoExt = file.getName().substring(0, file.getName().indexOf(".txt"));
					String name = nameNoExt.substring(nameNoExt.lastIndexOf(" results ") + 9);
					results.put(name, ResultsTable.open2(file.getPath()));


				}
			}
		}

		return results;

	}



	public enum Channel {
		GREEN("G", new Color(57, 137, 23), "green"), RED("R", Color.RED, "red"), BLUE("B", Color.BLUE, "blue"), WHITE("W", Color.GRAY, "gray");

		private String abbrev;
		private Color color;
		private String htmlColor;

		private Channel(String abbrev, Color color, String htmlColor) {
			this.abbrev = abbrev;
			this.color = color;
			this.htmlColor = htmlColor;
		}

		public String getAbbreviation() {
			return this.abbrev;
		}

		public Color getColor() {
			return this.color;
		}

		public String getHTMLColor() {
			return this.htmlColor;
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

		public String toReadableString() {
			String origName = super.name();
			return origName.charAt(0) + origName.substring(1).toLowerCase();
		}

		public static Channel parse(String query) {
			if (query == null)
				return null;
			for (Channel chan : values()) {
				if (query.equalsIgnoreCase(chan.name()))
					return chan;
			}
			return null;
		}


	}

	public class ImageOpenException extends Exception {

		private static final long serialVersionUID = 1963273665585902446L;

		public ImageOpenException(String message) {
			super(message);
		}
	}

	public class UnopenedException extends RuntimeException {

		private static final long serialVersionUID = 6071333983874153209L;

	}


	public synchronized int getMin(OutputOption it, Channel channel) {

		Map<Channel, ImagePlus> itImages = this.images.get(it);
		if (itImages == null)
			throw new NullPointerException();

		if (channel == null) {
			if (!itImages.containsKey(null))
				throw new NullPointerException();

			return (int) itImages.get(null).getDisplayRangeMin();
		} else {
			if (!itImages.containsKey(channel))
				throw new NullPointerException();
			return (int) itImages.get(channel).getDisplayRangeMin();
		}


	}

	public synchronized int getMax(OutputOption it, Channel channel) {
		Map<Channel, ImagePlus> itImages = this.images.get(it);
		if (itImages == null)
			throw new NullPointerException();

		if (channel == null) {
			if (!itImages.containsKey(null))
				throw new NullPointerException();

			return (int) itImages.get(null).getDisplayRangeMax();
		} else {
			if (!itImages.containsKey(channel))
				throw new NullPointerException();
			return (int) itImages.get(channel).getDisplayRangeMax();
		}
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

	public void applyLUT(ImagePlus imp, Channel chan) {


		FileInfo fi = new FileInfo();
		fi.reds = new byte[256]; 
		fi.greens = new byte[256]; 
		fi.blues = new byte[256];
		fi.lutSize = 256;
		switch (chan) {
		case GREEN:
			primaryColor(2, fi.reds, fi.greens, fi.blues);
			break;
		case RED:
			primaryColor(4, fi.reds, fi.greens, fi.blues);
			break;
		case BLUE:
			primaryColor(1, fi.reds, fi.greens, fi.blues);
			break;
		default:
			return;
		}
		fi.fileName = "CustomLUT";

		ImageProcessor ip = imp.getChannelProcessor();
		IndexColorModel cm = new IndexColorModel(8, 256, fi.reds, fi.greens, fi.blues);
		ip.setColorModel(cm);
		if (imp.getStackSize()>1)
			imp.getStack().setColorModel(cm);
		imp.updateImage();

	}

	public void printContents() {
		for (Entry<OutputOption, Map<Channel, ImagePlus>> images : this.images.entrySet()) {
			for (Entry<Channel, ImagePlus> imagesEn : images.getValue().entrySet()) {
				System.out.println(images.getKey().getDisplay() + " : " + imagesEn.getKey().abbrev);

			}
		}
	}

	private void primaryColor(int color, byte[] reds, byte[] greens, byte[] blues) {
		for (int i=0; i<256; i++) {
			if ((color&4)!=0)
				reds[i] = (byte)i;
			if ((color&2)!=0)
				greens[i] = (byte)i;
			if ((color&1)!=0)
				blues[i] = (byte)i;
		}
		return;
	}

	public void saveCurrentState(File fileName) throws IOException {
		FileOutputStream fileStream = new FileOutputStream(fileName); 
		ObjectOutputStream out = new ObjectOutputStream(fileStream); 
		out.writeObject(this); 
		out.close(); 
		fileStream.close();

	}

	public static ImageContainer loadFromPreviousState(File stateFile) throws IOException {
		FileInputStream fileInput = new FileInputStream(stateFile); 
		ObjectInputStream in = new ObjectInputStream(fileInput); 

		ImageContainer object1 = null;
		try {
			object1 = (ImageContainer)in.readObject();
		} catch (ClassNotFoundException e) {
			in.close();
			fileInput.close();
			return null;
		} 

		in.close(); 
		fileInput.close(); 
		return object1;

	}

	/*private void writeObject(ObjectOutputStream stream)
			throws IOException {
		stream.defaultWriteObject();
		stream.writeDouble(cal.pixelWidth);
		stream.writeDouble(cal.pixelHeight);
		int numImages = 0;
		for (Map<Channel, ImagePlus> images : this.images.values()) {
			numImages = numImages + images.size();
		}
		stream.writeInt(numImages);
		for (Entry<OutputOption, Map<Channel,ImagePlus>> imagesEn : this.images.entrySet()) {
			for (Entry<Channel, ImageP>)
				stream.writeChars(imagesEn.getKey().getCondensed());
					stream.writeObject(imagesEn.);
					stream.write
		}

	}

	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {
		stream.defaultReadObject();
		this.cal = new Calibration();
		this.cal.xOrigin = 0;
		this.cal.yOrigin = 0;
		this.cal.pixelWidth = stream.readDouble();
		this.cal.pixelHeight = stream.readDouble();
		this.timeOfRun = GUI.dateString;
		this.outputLocation = GUI.settings.outputLocation;
		this.imageFile = null;
	}*/

}
