package com.typicalprojects.CellQuant.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import ij.ImagePlus;
import ij.io.Opener;
import ij.measure.Calibration;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

public class ImagePhantom {
	private ImageContainer ic;
	private File imageFile;
	private SynchronizedProgress logger;
	private String title;
	private Calibration cal;

	public ImagePhantom(File imageFile, String titleNoExtension, SynchronizedProgress logger, Calibration cal) {
		this.title = titleNoExtension;
		this.imageFile = imageFile;
		this.logger = logger;
		this.cal = cal;
	}

	public File getImageFile() {
		return this.imageFile;
	}
	public String getTitle() {
		return this.title;
	}

	public String open(Map<Integer, Channel> validChannels, File resaveOutputDir, String timeOfRun, boolean intermediates) {
		// As one CZI
		logger.setProgress("Opening " + title + " ...");
		try {
			if (!intermediates) {
				ImporterOptions io = new ImporterOptions();
				io.setId(imageFile.getPath());
				io.setSplitChannels(true);
				ImagePlus[]  ip= BF.openImagePlus(io);
				this.cal = ip[0].getCalibration();
				if (ip.length != validChannels.size()) {
					logger.setProgress("Failed to open.");
					return "Incorrect channel configuration. Please use Preferences to specify channel mapping.";
				}
				for (int i = 0; i < ip.length; i++) {
					ImagePlus newIP = ip[i].duplicate();
					newIP.setProcessor(newIP.getProcessor().convertToShortProcessor());
					ip[i] = newIP;
				}
				ic = new ImageContainer(ip, this.title, this.imageFile, this.cal, validChannels, resaveOutputDir, timeOfRun);
				logger.setProgress("Success.");
				return null;

			} else {

				Opener opener = new Opener();
				List<Channel> channels = new ArrayList<Channel>();
				List<ImagePlus> images = new ArrayList<ImagePlus>();
				File intermediateFilesDir = ImageContainer.getIntermediateFilesDirectory(title, resaveOutputDir, timeOfRun);
				
				
				for (Channel chan : validChannels.values()){
					File file = new File(intermediateFilesDir + File.separator + title + " Chan-" + chan.getAbbreviation() + ".tiff");
					
					if (file.exists()) {
						channels.add(chan);
						ImagePlus ip = opener.openImage(file.getPath());
						ip.setCalibration(this.cal);
						ip.setTitle(title + " Chan-" + chan.getAbbreviation());
						images.add(ip);
					}

				}
				Map<String, ImagePlus> supp = new HashMap<String, ImagePlus>();
				if (intermediateFilesDir.isDirectory()) {
					for (File file : intermediateFilesDir.listFiles()) {
						if (!file.isDirectory() && file.getName().startsWith("SUPP IMG ")) {
							ImagePlus ip = opener.openImage(file.getPath());
							ip.setCalibration(this.cal);
							String ipTitle = file.getName().substring(9, file.getName().lastIndexOf('.'));
							ip.setTitle(ipTitle);
							supp.put(ipTitle, opener.openImage(file.getPath()));
						}
					}
				}
				
				if (channels.size() != validChannels.size()) {
					logger.setProgress("Failed to open.");
					return "Incorrect channel configuration. Please use Preferences to specify channel mapping.";
				}
				
				ic = new ImageContainer(channels, images, supp, title, this.imageFile, this.cal, resaveOutputDir, timeOfRun);
				logger.setProgress("Success.");
				return null;

			}

		} catch (Exception e) {
			logger.setProgress("Failed to open.");
			System.out.println(e);
			e.printStackTrace();
			return (e.getMessage() == null ? "Unknown reason.":e.getMessage());

		}


	}



	public ImageContainer getIC() {
		return this.ic;
	}




}
