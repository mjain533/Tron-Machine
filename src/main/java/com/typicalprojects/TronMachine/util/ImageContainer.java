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
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.OutputOption;
import com.typicalprojects.TronMachine.neuronal_migration.OutputParams;
import com.typicalprojects.TronMachine.neuronal_migration.RunConfiguration;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager.Channel;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileInfo;
import ij.io.FileSaver;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;



public class ImageContainer implements Serializable {


	private static final long serialVersionUID = -6576786838224324311L; // For serialization
	private static final String INTERMED_FILES = "Intermediate Files";
	
	public static final String STATE_ROI = "postroistate.ser";
	public static final String STATE_OBJ = "postobjstate.ser";
	public static final String STATE_SLC = "postslicestate.ser";

	private transient Map<OutputOption, Map<Channel, ImagePlus>> images = new HashMap<OutputOption, Map<Channel, ImagePlus>>();

	private String title;
	private transient Calibration cal;
	private transient File outputLocation;
	private transient String timeOfRun;
	private transient File imageFile;
	private int[] dimensions;
	private RunConfiguration runConfig;
	
	private static IndexColorModel fireLUT = null;
	
	static {
		
		FileInfo fi = new FileInfo();
		fi.reds = new byte[256]; 
		fi.greens = new byte[256]; 
		fi.blues = new byte[256];
		
		Scanner scanner = new Scanner(ImageContainer.class.getResourceAsStream("/LUT.txt"));
		while (scanner.hasNext()) {
			String line = scanner.nextLine();
			String[] pieces = line.split("\\t");
			
			int index = Integer.parseInt(pieces[0]);
			fi.reds[index] = (byte) Integer.parseInt(pieces[1]);
			fi.greens[index] = (byte) Integer.parseInt(pieces[2]);
			fi.blues[index] = (byte) Integer.parseInt(pieces[3]);

		}
		
		fi.lutSize = 256;
		fi.fileName = "OverlapLUT";

		fireLUT = new IndexColorModel(8, 256, fi.reds, fi.greens, fi.blues);
		
		scanner.close();
	}

	public ImageContainer(String title, File imageFile, File outputLocation, String timeOfRun, RunConfiguration runConfig) throws ImageOpenException {

		try {
			this.runConfig = runConfig;
			this.title = title;
			this.outputLocation = outputLocation;
			this.timeOfRun = timeOfRun;
			this.imageFile = imageFile;
			makeSaveDirectory(this.title, this.outputLocation, this.timeOfRun);

			// Try BioFormats first for advanced formats (.czi, etc.), fall back to ImageJ Opener
			ImagePlus[] ips = null;
			try {
				ImporterOptions options = new ImporterOptions();
				options.setId(this.imageFile.getAbsolutePath());
				options.setSplitChannels(true);
				options.setQuiet(true);
				ips = BF.openImagePlus(options);
			} catch (Exception e) {
				// Fallback to ImageJ Opener for standard formats
				Opener opener = new Opener();
				ImagePlus imagePlus = opener.openImage(this.imageFile.getPath());
				if (imagePlus == null) {
					throw new ImageOpenException("Failed to open image file: " + this.imageFile.getPath());
				}
				ips = new ImagePlus[] { imagePlus };
			}
			
			this.cal = ips[0].getCalibration();

			if (this.cal == null) {
				this.cal = new Calibration();
				this.cal.xOrigin = 0;
				this.cal.yOrigin = 0;
				String stringCalibration = GUI.settings.calibrations.get(GUI.settings.calibrationNumber-1);
				String[] pieces = stringCalibration.substring(stringCalibration.indexOf("(1 pixel : ") + 11, stringCalibration.lastIndexOf(")")).split(" ");
				this.cal.setUnit(pieces[1]);
				this.cal.pixelHeight = Double.parseDouble(pieces[0]);
				this.cal.pixelWidth = Double.parseDouble(pieces[0]);

			}
			if (ips.length != runConfig.channelMan.getNumberOfChannels(false)) {
				throw new ImageOpenException("Incorrect channel configuration. Please use Preferences to specify channel mapping.");
			}
			Map<Channel, ImagePlus> origImages = new HashMap<Channel, ImagePlus>();
			for (Entry<Channel, Integer> chanEn : runConfig.channelMan.getChannelIndices().entrySet()) {
				if (chanEn.getValue() == -1)
					continue;
				
				if (chanEn.getValue() < ips.length) {
					ImagePlus ip = ips[chanEn.getValue()];
					ip.setProcessor(ip.getProcessor().convertToShortProcessor());
					ip.setTitle(this.title + " Chan-" + chanEn.getKey().getAbbrev());
					if (this.dimensions == null) this.dimensions = ip.getDimensions();
					if (GUI.settings.enforceLUTs) {
						applyLUT(ip, chanEn.getKey().getImgColor());
					} else {
						applyLUT(ip, new Color(255, 255, 255));
					}
					
					origImages.put(chanEn.getKey(), ip);
				} else {
					throw new ImageOpenException("Incorrect channel configuration. Please use Preferences to specify channel mapping.");
				}
			}

			this.images.put(OutputOption.Channel, origImages);



			if (this.dimensions[0] < 50 || this.dimensions[1] < 50) {
				throw new Exception("Image size is too small (must be at least 50x50 pixels)");
			}


		} catch (Exception e) {
			e.printStackTrace();
			throw new ImageOpenException(e.getMessage());
		}



	}

	/*public File getImageFile() {
		return this.imageFile;
	}*/

	public Calibration getCalibration() {
		return this.cal;
	}

	public int[] getDimensions() {
		return this.dimensions;
	}
	
	public boolean isWithinImageBounds(int x, int y) {
		return (x >= 0 && y >= 0 && x < this.dimensions[0] && y < this.dimensions[1]);
	}

	public String getImageTitle() {
		return this.title;
	}
	
	/**
	 * @return run configuration for this image (may differ from current settings in preferences if reloading data)
	 */
	public RunConfiguration getRunConfig() {
		return this.runConfig;
	}
	
	/**
	 * Adds an image to this image container (does not save it)
	 * 
	 * @param option		The type of image
	 * @param chan		The channel for the image, of null if the image type does not require a channel designation
	 * @param image		The image to add to this {@link ImageContainer}
	 */
	public void addImage(OutputOption option, Channel chan, ImagePlus image) {

		if (!this.images.containsKey(option))
			this.images.put(option, new HashMap<Channel, ImagePlus>());
		
		if (chan == null) {
			this.images.get(option).put(null, image);
		} else {
			this.images.get(option).put(chan, image);
		}

	}
	
	/**
	 * Gets the original stack image for a specific channel
	 * 
	 * @throws NullPointerException if channel doesn't exist or originals aren't open.
	 */
	public ImagePlus getChannelOrig(Channel channel, boolean duplicate) throws UnopenedException {

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
	
	/**
	 * Retrieves original images (split by channel)
	 * 
	 * @return Original image stacks (slice-modified if this step has been completed)
	 * @throws UnopenedException if originals are not in this image container
	 */
	public Map<Channel, ImagePlus> getOriginals() throws UnopenedException {
		Map<Channel, ImagePlus> ips = this.images.get(OutputOption.Channel);
		if (ips == null)
			throw new UnopenedException();
		else 
			return ips;
	}
	
	/**
	 * Tests if an image was saved to the file system
	 * 
	 * @param option		The type of image
	 * @param chan		The channel of the image (or null if the type of image doesn't require a channel designation)
	 * @return			true if the image was saved in the current intermediate files directory
	 */
	public boolean imageWasSaved(OutputOption option, Channel chan) {
		
		String queueTitle = this.title.concat(chan != null ? " Chan-" + chan.getAbbrev() : "");
		if (option != OutputOption.Channel) {
			queueTitle = queueTitle.concat(" ").concat(option.getImageSuffix());
		}
		queueTitle = queueTitle.concat(".tiff");

		return new File(this.getIntermediateFilesDirectory() + File.separator + queueTitle).exists();
	}
	
	/**
	 * Tests if this {@link ImageContainer} contains a specific image. It may be useful to test if the
	 * specified image is contained before trying to open specified image, because opening an already-opened
	 * image is a waste of resources and time.
	 * 
	 * @param option		The image type
	 * @param chan		Channel designation required by the image option, if a channel designation is required.
	 * @return true if the image is contained within this {@link ImageContainer}
	 */
	public boolean containsImage(OutputOption option, Channel chan) {
		
		if (this.images.get(option) == null)
			return false;
		
		if (option.getRestrictedOption() == OutputOption.NO_CHANS) {
			return this.images.get(option).get(null) != null;
		} else {
			return this.images.get(option).get(chan) != null;
		}
		
	}
	
	
	/**
	 * Removes the original images from this image container, optionally saving. If originals are not part of
	 * this IC, then do nothing.
	 * 
	 * @param save if should save the tiff stacks while at it.
	 * @throws UnopenedException if the original image tiff stacks were not open
	 */
	public void removeOriginalFromIC(Channel chan, boolean save) {
		
		if (save) saveOriginal(chan);
		
		Map<Channel, ImagePlus> originals = this.images.get(OutputOption.Channel);
		if (originals == null || !originals.containsKey(chan))
			throw new UnopenedException("Original Images Not Open");
		originals.remove(chan);
		if (originals.isEmpty())
			this.images.remove(OutputOption.Channel);
	}
	
	/**
	 * Removes an image from the {@link ImageContainer}.
	 * 
	 * @param output		The type of image to remove
	 * @param chan		The channel of the image to remove, or null to remove all images for this image type.
	 */
	public void removeImageFromIC(OutputOption output, Channel chan) {
		
		if (output.getRestrictedOption() == OutputOption.NO_CHANS) {
			this.images.remove(output);
		} else if (chan == null){
			this.images.remove(output);
		} else if (this.images.get(output) != null) {
			this.images.get(output).remove(chan);
		}

	}
	
	/**
	 * Saves the original images to the intermediate files folder
	 * 
	 * @param chan	The channel to save
	 * @throws UnopenedException if the original images are not opened in this ImageContainer at this point
	 */
	public void saveOriginal(Channel chan) {
		
		Map<Channel, ImagePlus> originals = this.images.get(OutputOption.Channel);
		if (originals == null || !originals.containsKey(chan))
			throw new UnopenedException("Original Images Not Open");
		
		ImagePlus orig = originals.get(chan);
		if (orig.isStack()) {
			new FileSaver(orig).saveAsTiffStack(this.getIntermediateFilesDirectory() + File.separator + this.title + " Chan-" + chan.getAbbrev() + ".tiff");

		} else {
			new FileSaver(orig).saveAsTiff(this.getIntermediateFilesDirectory() + File.separator + this.title + " Chan-" + chan.getAbbrev() + ".tiff");

		}
	}
	
	// TOOD: make sure that the logging is okay. Stopped using open() from ImagePhantom, which normally does some logging,
	// so make sure this still is good.
	

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
	
	/**
	 * Retrives an image from this {@link ImageContainer}
	 * 
	 * @param it			The type of image type
	 * @param chan		The channel for this image, if the image type requires a channel designation
	 * @param duplicate	true if the image should be duplicated or if the original image in this {@link ImageContainer} should be used.
	 * @return the query image
	 */
	public ImagePlus getImage(OutputOption it, Channel chan, boolean duplicate) {
		Map<Channel, ImagePlus> ips = this.images.get(it);

		if (ips == null)
			throw new UnopenedException();
		ImagePlus ip = null;

		if (it.getRestrictedOption() == OutputOption.NO_CHANS) {
			ip = ips.get(null);
		} else {
			ip = ips.get(chan);
		}
		if (ip == null)
			throw new UnopenedException();

		if (duplicate) {
			ImagePlus newImg = ip.duplicate();
			newImg.setTitle(newImg.getTitle().substring(4));
			// REMOVE DUP TAG
			return newImg;
		} else {
			if (chan != null) {
				applyLUT(ip, chan.getImgColor());
			}
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
						if (ending.length() >= 6 && this.runConfig.channelMan.parse(ending.charAt(5) + "") != null) {
							Channel chan = this.runConfig.channelMan.parse(ending.charAt(5) + "");
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
						if (ending.length() >= 6 && this.runConfig.channelMan.parse(ending.charAt(5) + "") != null) {

							if (this.runConfig.channelMan.parse(ending.charAt(5) + "").equals(chan)) {
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

							for (Channel chan : this.runConfig.channelMan.getOutputParamChannels(en.getValue())) {
								if (file.getName().endsWith("Chan-" + chan.getAbbrev() + ".tiff")) {
									delete = false;
									break;
								}
							}

						} else if (file.getName().endsWith(" " + en.getKey().getImageSuffix() + ".tiff")) {
							if (en.getKey().getRestrictedOption() == OutputOption.NO_CHANS) {
								delete = false;
								break;
							} else {
								for (Channel chan : this.runConfig.channelMan.getOutputParamChannels(en.getValue())) {
									if (file.getName().contains("Chan-" + chan.getAbbrev())) {
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


	public void saveResultsTables(Map<String, ResultsTable> results, boolean excel) {

		deleteResultsTables();

		String ext = excel ? ".xlsx" : ".txt";

		if (excel) {
			try {
				AdvancedWorkbook aw = new AdvancedWorkbook();
				List<String> keys = new ArrayList<String>(results.keySet());

				for (Channel chan : this.runConfig.channelMan.getOrderedChannels()) {
					if (keys.contains(chan.getName())) {
						aw.addSheetFromNeuronCounterResultTable(chan.getName(), results.get(chan.getName()));
						keys.remove(chan.getName());
					}
				}
				for (String key : keys) {
					aw.addSheetFromNeuronCounterResultTable(key, results.get(key));

				}

				File saveFile = new File(getSaveDirectory() + File.separator + this.title + " ANALYSIS.xlsx");
				boolean saveSuccess = aw.save(saveFile);
				if (!saveSuccess) {
					System.err.println("WARNING: Failed to save Excel file: " + saveFile.getAbsolutePath());
				}
			} catch (IllegalStateException e) {
				System.err.println("ERROR: Could not determine save directory");
				e.printStackTrace();
			} catch (Exception e) {
				System.err.println("ERROR: Unexpected error while saving results tables");
				e.printStackTrace();
			}
		} else {
			for (Entry<String, ResultsTable> en : results.entrySet()) {
				if (en.getValue() != null) {
					try {
						PrintWriter pw = new PrintWriter(new File(getIntermediateFilesDirectory() + File.separator + this.title + " results " + en.getKey() + ext));
						pw.write(en.getValue().convertToString());
						pw.flush();
						pw.close();
					} catch (IllegalStateException e) {
						// Wont happen
						System.err.println("ERROR: Could not determine intermediate files directory");
						e.printStackTrace();
					} catch (IOException e) {
						// Wont happen
						System.err.println("ERROR: IOException while saving text results file");
						e.printStackTrace();
					}
				}

			}
		}




	}

	public void saveSupplementalImage(OutputOption it, ImagePlus image) {
		saveSupplementalImage(it, image, null);
	}

	public void saveSupplementalImage(OutputOption it, ImagePlus image, Channel chan) {
		String title = this.title.concat(chan != null ? " Chan-" + chan.getAbbrev() : "");
		image.setTitle(title);
		String savePath = getIntermediateFilesDirectory().getPath() + File.separator + title.concat(" ").concat(it.getImageSuffix()).concat(".tiff");

		if (image.getStackSize() > 1) {
			new FileSaver(image).saveAsTiffStack(savePath);

		} else {
			new FileSaver(image).saveAsTiff(savePath);

		}
		
	}

	public Map<String, ResultsTable> tryToOpenResultsTablesFromTxt() {

		File intermediateFilesDir = getIntermediateFilesDirectory();

		Map<String, ResultsTable> results = new HashMap<String, ResultsTable>();

		if (intermediateFilesDir.isDirectory()) {
			for (File file : intermediateFilesDir.listFiles()) {
				if (!file.isDirectory() && file.getName().endsWith(".txt") && file.getName().contains(" results ")) {
					

					try {
						ResultsTable rt = new ResultsTable();
						String nameNoExt = file.getName().substring(0, file.getName().indexOf(".txt"));
						String name = nameNoExt.substring(nameNoExt.lastIndexOf(" results ") + 9);
						FileInputStream fis = new FileInputStream(file);
						byte[] data = new byte[(int) file.length()];
						fis.read(data);
						fis.close();
						rt.loadFromString(new String(data, "UTF-8"));
						results.put(name, rt);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
	


				}
			}
		}

		return results;

	}

	public class ImageOpenException extends Exception {

		private static final long serialVersionUID = 1963273665585902446L;

		public ImageOpenException(String message) {
			super(message);
		}
	}

	public class UnopenedException extends RuntimeException {

		private static final long serialVersionUID = 6071333983874153209L;
		
		public UnopenedException(){
			super();
		}
		
		public UnopenedException(String message) {
			super(message);
		}

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
		
		File file = new File(this.outputLocation.getPath() + File.separator + this.title + " " + this.timeOfRun);
		if (!file.isDirectory())
			file.mkdir();
				
		return file;
	}

	public File getIntermediateFilesDirectory()throws IllegalStateException {
		if (this.outputLocation == null) {
			throw new IllegalStateException();
		}
		

		File file = new File(getSaveDirectory().getPath() + File.separator + INTERMED_FILES);;
		if (!file.isDirectory())
			file.mkdir();

		return file;

	}
	
	public File getSerializeDirectory()throws IllegalStateException {
		if (this.outputLocation == null) {
			throw new IllegalStateException();
		}
		File serializeDir = new File(getIntermediateFilesDirectory().getPath() + File.separator + "Serialization");
		if (!serializeDir.exists())
			serializeDir.mkdir();		
		
		return serializeDir;

	}
	
	public File getSerializeFile(String state)throws IllegalStateException {
		if (this.outputLocation == null) {
			throw new IllegalStateException();
		}
		File file = new File(getSerializeDirectory().getPath() + File.separator + state);
		
		return file;

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
	
	public static void applyLUT(ImagePlus imp, Color color) {
		int red = color.getRed();
		int green = color.getGreen();
		int blue = color.getBlue();

		FileInfo fi = new FileInfo();
		fi.reds = new byte[256]; 
		fi.greens = new byte[256]; 
		fi.blues = new byte[256];
		fi.lutSize = 256;
		
		for (int i=0; i<256; i++) {
			fi.reds[i] = (byte)(red * (i / 255.0));
			fi.greens[i] = (byte)(green * (i / 255.0));
			fi.blues[i] = (byte)(blue * (i / 255.0));
		
		}
		
		fi.fileName = "CustomLUT";
		// If image is RGB (24-bit), IndexColorModel is not supported. Apply per-pixel tint instead.
		if (imp.getBitDepth() == 24) {
			// Apply tint by multiplying each channel by the tint color fraction.
			int w = imp.getWidth();
			int h = imp.getHeight();
			int stackSize = imp.getStackSize();
			for (int s = 1; s <= stackSize; s++) {
				ImageProcessor proc = imp.getStack().getProcessor(s);
				// operate on ColorProcessor pixels via getPixel and set
				int[] pixels = (int[]) proc.getPixels();
				for (int i = 0; i < pixels.length; i++) {
					int p = pixels[i];
					int r = (p >> 16) & 0xFF;
					int g = (p >> 8) & 0xFF;
					int b = p & 0xFF;
					int nr = (r * red) / 255;
					int ng = (g * green) / 255;
					int nb = (b * blue) / 255;
					pixels[i] = (0xFF << 24) | (nr << 16) | (ng << 8) | nb;
				}
			}
			imp.updateImage();
			return;
		}

		ImageProcessor ip = imp.getChannelProcessor();
		IndexColorModel cm = new IndexColorModel(8, 256, fi.reds, fi.greens, fi.blues);
		ip.setColorModel(cm);
		if (imp.getStackSize()>1)
			imp.getStack().setColorModel(cm);
		imp.updateImage();
	}
	
	public static IndexColorModel getInfernoLUT() {
		return fireLUT;
	}
	
	public static void applyInfernoLUT(ImagePlus imp) {
		
		if (imp.getBitDepth() != 8) {
			IJ.run(imp, "8-bit", "stack");

		}
		ImageProcessor ip = imp.getChannelProcessor();
		ip.setColorModel(fireLUT);
		if (imp.getStackSize()>1)
			imp.getStack().setColorModel(fireLUT);
		imp.updateImage();
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

	private void writeObject(ObjectOutputStream stream)
			throws IOException {

		stream.defaultWriteObject();
		stream.writeDouble(cal.pixelWidth);
		stream.writeDouble(cal.pixelHeight);
		stream.writeUTF(cal.getUnit());
		int numImages = 0;
		for (Map<Channel, ImagePlus> images : this.images.values()) {
			numImages = numImages + images.size();
		}
		stream.writeInt(numImages);
		for (Entry<OutputOption, Map<Channel,ImagePlus>> imagesEn : this.images.entrySet()) {
			for (Entry<Channel, ImagePlus> imageEn : imagesEn.getValue().entrySet()) {
				stream.writeUTF(imagesEn.getKey().getCondensed());
				stream.writeObject(imageEn.getKey());
				stream.flush();
				byte[] bytes = new FileSaver(imageEn.getValue()).serialize();
				stream.writeInt(bytes.length);
				/*ByteArrayOutputStream buffer = new ByteArrayOutputStream();
				if (imageEn.getKey().equals(Channel.GREEN)) {
					imageEn.getValue().show();
				}
				;
		        ImageIO.write(new FileSaver(imageEn.getValue()).serialize(), "tiff", buffer);*/
		        stream.write(bytes);
		        /*stream.writeInt(buffer.size()); // Prepend image with byte count
		        buffer.writeTo(stream);   */ 
				stream.flush();
			}
		}


	}

	private void readObject(ObjectInputStream stream)
			throws IOException, ClassNotFoundException {

		// NOTE: this may need to be adjusted for future updates.

		// initialize transient vars
		this.images = new HashMap<OutputOption, Map<Channel, ImagePlus>>();
		this.imageFile = null;

		// read serialized object
		stream.defaultReadObject();
		this.cal = new Calibration();
		this.cal.xOrigin = 0;
		this.cal.yOrigin = 0;
		this.cal.pixelWidth = stream.readDouble();
		this.cal.pixelHeight = stream.readDouble();
		this.cal.setUnit(stream.readUTF());
		this.timeOfRun = GUI.dateString;
		this.outputLocation = GUI.settings.outputLocation;
		int numImages = stream.readInt();
		Opener opener = new Opener();
		for (int i = 0; i < numImages; i++) {
			OutputOption outputOption = OutputOption.fromCondensed((String) stream.readUTF());
			Channel chan = (Channel) stream.readObject();
			
			int size = stream.readInt(); // Read byte count

	        byte[] buffer = new byte[size];
	        stream.readFully(buffer); // Make sure you read all bytes of the image

			ImagePlus image = opener.deserialize(buffer);
			Map<Channel,ImagePlus> optionImages = this.images.get(outputOption);
			if (optionImages == null) {
				optionImages = new HashMap<Channel,ImagePlus>();
				this.images.put(outputOption, optionImages);
			}
			optionImages.put(chan, image);
		}
	}

}
