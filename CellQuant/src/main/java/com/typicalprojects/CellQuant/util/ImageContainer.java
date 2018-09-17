package com.typicalprojects.CellQuant.util;

import java.awt.Color;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	private Map<ImageTag, Map<Channel, ImagePlus>> images = new HashMap<ImageTag, Map<Channel, ImagePlus>>();

	private String title;
	private Calibration cal;
	private File outputLocation;
	private String timeOfRun;
	private File imageFile;
	private int[] dimensions;

	public ImageContainer(String title, File imageFile, File outputLocation, String timeOfRun, List<ImageTag> imagesToOpen, Map<Integer, Channel> validChannels, Calibration cal) throws ImageOpenException {
		
		try {
			this.cal = cal;
			this.title = title;
			this.outputLocation = outputLocation;
			this.timeOfRun = timeOfRun;
			this.imageFile = imageFile;
			makeSaveDirectory(this.title, this.outputLocation, this.timeOfRun);

			if (imagesToOpen.size() == 0)
				throw new IllegalArgumentException("There are no images for " + this.title + ".");
			
			for (ImageTag it : imagesToOpen) {
				if (it.equals(ImageTag.Orig)) {
					
					ImporterOptions io = new ImporterOptions();
					io.setId(imageFile.getPath());
					io.setSplitChannels(true);
					ImagePlus[]  ips= BF.openImagePlus(io);
					this.cal = ips[0].getCalibration();
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
							origImages.put(chanEn.getValue(), ip);
						} else {
							throw new ImageOpenException("Incorrect channel configuration. Please use Preferences to specify channel mapping.");
						}
					}
					
					this.images.put(ImageTag.Orig, origImages);
				} else if (it.equals(ImageTag.OrigTiff)) {
					
					Opener opener = new Opener();
					Map<Channel, ImagePlus> origImages = new HashMap<Channel, ImagePlus>();

					File intermediateFilesDir = getIntermediateFilesDirectory();
					
					
					for (Channel chan : validChannels.values()){
						File file = new File(intermediateFilesDir + File.separator + title + " Chan-" + chan.getAbbreviation() + ".tiff");
						System.out.println(file.getPath());
						if (file.exists()) {
							ImagePlus ip = opener.openImage(file.getPath());
							ip.setCalibration(this.cal);
							ip.setTitle(this.title + " Chan-" + chan.getAbbreviation());
							if (this.dimensions == null) this.dimensions = ip.getDimensions();
							origImages.put(chan, ip);
						}

					}
					
					if (origImages.size() != validChannels.size()) {
						System.out.println(origImages.size() + " : " + validChannels.size());
						throw new ImageOpenException("Incorrect channel configuration. Please use Preferences to specify channel mapping.");
					}
					
					this.images.put(ImageTag.OrigTiff, origImages);
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

	public void deleteSuppImgFiles(ImageTag it) {
		File intDir = this.getIntermediateFilesDirectory();
		if (intDir.isDirectory()) {
			for (File file : intDir.listFiles()) {
				if (!file.isDirectory() && file.getName().endsWith(".tiff") && file.getName().substring(0, file.getName().lastIndexOf(".tiff")).endsWith(it.getTag())) {
					file.delete();
				}
			}
		}

	}

	/**
	 * 
	 * @throws NullPointerException if the supplied channel doesn't exist or original image isn't opened.
	 */
	public int getOrigStackSize(Channel chan) throws NullPointerException {
		
		if (this.images.containsKey(ImageTag.Orig)) {
			return this.images.get(ImageTag.Orig).values().iterator().next().getStackSize();
		} else if (this.images.containsKey(ImageTag.OrigTiff)) {
			return this.images.get(ImageTag.OrigTiff).values().iterator().next().getStackSize();
		} else {
			throw new NullPointerException();
		}
	}

	/**
	 * 
	 * @throws NullPointerException if channel doesn't exist or originals aren't open.
	 */
	public ImagePlus getChannelOrig(Channel channel, boolean duplicate) throws NullPointerException {
		
		ImagePlus ip = getOriginals().get(channel);

		if (duplicate) {
			ImagePlus newImg = ip.duplicate();
			newImg.setTitle(newImg.getTitle().substring(4));
			// REMOVE DUP TAG
			return newImg;
		} else if (ip == null) {
			throw new NullPointerException();
		} else {
			return ip;
		}
	}
	
	public Map<Channel, ImagePlus> getOriginals() {
		Map<Channel, ImagePlus> ips = this.images.get(ImageTag.Orig);
		if (ips != null)
			return ips;
		else
			return this.images.get(ImageTag.OrigTiff);
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
	
	public ImagePlus getImage(ImageTag it, Channel chan, boolean duplicate) {
		Map<Channel, ImagePlus> ips = this.images.get(it);
		
		if (ips == null)
			return null;
		ImagePlus ip = ips.get(chan);
		if (ip == null)
			return null;
		
		if (duplicate) {
			ImagePlus newImg = ip.duplicate();
			newImg.setTitle(newImg.getTitle().substring(4));
			// REMOVE DUP TAG
			return newImg;
		} else {
			return ip;
		}

	}

	public Map<Channel, ImagePlus> openSupplementalImages(ImageTag it) {

		File intermediateFilesDir = getIntermediateFilesDirectory(this.title, this.outputLocation, timeOfRun);
		Opener opener = new Opener();

		Map<Channel, ImagePlus> supp = new HashMap<Channel, ImagePlus>();
		if (intermediateFilesDir.isDirectory()) {
			for (File file : intermediateFilesDir.listFiles()) {
				if (!file.isDirectory() && file.getName().substring(0, file.getName().lastIndexOf('.')).endsWith(" " + it.getTag())) {

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

	public ImagePlus openSupplementalImage(ImageTag it) {

		File intermediateFilesDir = getIntermediateFilesDirectory(this.title, this.outputLocation, timeOfRun);
		Opener opener = new Opener();

		if (intermediateFilesDir.isDirectory()) {
			for (File file : intermediateFilesDir.listFiles()) {
				if (!file.isDirectory() && file.getName().substring(0, file.getName().lastIndexOf('.')).endsWith(" " + it.getTag())) {

					ImagePlus ip = opener.openImage(file.getPath());
					ip.setCalibration(this.cal);

					ip.setTitle(this.title);
					return ip;

				}
			}
		}

		return null;
	}

	public ImagePlus openSupplementalImage(ImageTag it, Channel chan) {
		File intermediateFilesDir = getIntermediateFilesDirectory(this.title, this.outputLocation, timeOfRun);
		Opener opener = new Opener();

		if (intermediateFilesDir.isDirectory()) {
			for (File file : intermediateFilesDir.listFiles()) {
				if (!file.isDirectory() && file.getName().substring(0, file.getName().lastIndexOf('.')).endsWith(" " + it.getTag())) {

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
	
	public void deleteIrrelevantDataExcept(Set<ImageTag> tags) {
		File intDir = getIntermediateFilesDirectory();
		if (intDir.isDirectory()) {
			for (File file : intDir.listFiles()) {
				if (!file.isDirectory()) {
					boolean delete = true;
					for (ImageTag tag : tags) {
						if (file.getName().endsWith(" " + tag.getTag() + ".tiff")) {
							delete = false;
							break;
						}
					}
					if (delete)
						file.delete();
				}
			}
		}
	}
	
	public void saveOrigImageStacksAsTiffs() {
		
		for (Entry<Channel, ImagePlus> en : this.images.get(ImageTag.Orig).entrySet()) {
			
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
	
	public void saveSupplementalImage(ImageTag it, ImagePlus image) {
		saveSupplementalImage(it, image, null);
	}
	
	public void saveSupplementalImage(ImageTag it, ImagePlus image, Channel chan) {
		String title = this.title.concat(chan != null ? " Chan-" + chan.abbrev : "");
		image.setTitle(title);
		String savePath = getIntermediateFilesDirectory().getPath() + File.separator + title.concat(" ").concat(it.getTag()).concat(".tiff");

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

	public enum ImageTag {

		Orig(""), OrigTiff(""), MaxProjected("MAXED"), Objects("OBJECTS"), ObjCountMask("OBJ MASK"), ObjCountOrigMaskMerge("OBJ MASK MERGE"),
		Rois("ROIS"), Bins("BINS");

		private String tag;

		private ImageTag(String tag) {
			this.tag = tag;
		}

		public String getTag() {
			return this.tag;
		}

	}

	public enum Channel {
		GREEN("G", new Color(57, 137, 23)), RED("R", Color.RED), BLUE("B", Color.BLUE), WHITE("W", Color.GRAY);

		private String abbrev;
		private Color color;

		private Channel(String abbrev, Color color) {
			this.abbrev = abbrev;
			this.color = color;
		}

		public String getAbbreviation() {
			return this.abbrev;
		}

		public Color getColor() {
			return this.color;
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



	public synchronized int getMin(ImageTag it, Channel channel) {
		
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

	public synchronized int getMax(ImageTag it, Channel channel) {
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

}
