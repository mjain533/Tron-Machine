package com.typicalprojects.CellQuant.util;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.typicalprojects.CellQuant.neuronal_migration.GUI;
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import ij.ImagePlus;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import loci.plugins.BF;
import loci.plugins.in.ImporterOptions;

public class ImagePhantom {
	private ImageContainer ic;
	private File pathToImage;
	private File saveDir;
	private GUI gui;
	private String title;
	private Calibration cal;
	private boolean intermediates;

	public ImagePhantom(File imagePath, String titleNoExtension, GUI gui, File saveDir, boolean intermediates, Calibration cal) {
		this.title = titleNoExtension;
		this.pathToImage = imagePath;
		this.gui = gui;
		this.saveDir = saveDir;
		this.cal = cal;
		this.intermediates = intermediates;
	}

	public File getImageFile() {
		return this.pathToImage;
	}
	public String getTitle() {
		return this.title;
	}

	public String open() {
		// As one CZI
		gui.log("Opening " + title + " ...");
		try {
			if (!this.intermediates) {
				ImporterOptions io = new ImporterOptions();
				io.setId(pathToImage.getPath());
				io.setSplitChannels(true);
				ImagePlus[]  ip= BF.openImagePlus(io);
				//System.out.println(ip[0].getCalibration().getY(40));
				this.cal = ip[0].getCalibration();
				ic = new ImageContainer(ip, this.title, this.pathToImage, this.saveDir, this.cal);
				gui.log("Success.");
				return null;

			} else {

				Opener opener = new Opener();
				List<Channel> potentialChannels = new ArrayList<Channel>(GUI.channelMap.values());
				List<Channel> channels = new ArrayList<Channel>();
				List<ImagePlus> images = new ArrayList<ImagePlus>();
				File intermediateFilesDir = new File(this.saveDir.getPath() + File.separator + title + " Intermediate Files");
				
				for (Channel chan : potentialChannels){
					File file = new File(intermediateFilesDir + File.separator + title + " Chan-" + chan.getAbbreviation() + ".tiff");
					if (file.exists()) {
						channels.add(chan);
						ImagePlus ip = opener.openImage(file.getPath());
						ip.setCalibration(this.cal);
						ip.setTitle(title + " Chan-" + chan.getAbbreviation());
						images.add(ip);
					}

				}
				ic = new ImageContainer(channels, images, title, this.pathToImage, this.saveDir, false, this.cal);
				gui.log("Success.");
				return null;

			}

		} catch (Exception e) {
			gui.log("Failed.");
			System.out.println(e);
			e.printStackTrace();
			return (e.getMessage() == null ? "Unknown reason.":e.getMessage());

		}


	}

	public Map<Channel, ResultsTable> tryToOpenResultsTables() {
		
		Map<Channel, ResultsTable> results = new HashMap<Channel, ResultsTable>();
		
		for (Channel chan : GUI.channelsToProcess) {
			results.put(chan, ResultsTable.open2(this.saveDir + File.separator + this.ic.getImageChannel(chan, false).getTitle() + ".txt"));

		}		

		return results;

	}


	public ImageContainer getIC() {
		return this.ic;
	}




}
