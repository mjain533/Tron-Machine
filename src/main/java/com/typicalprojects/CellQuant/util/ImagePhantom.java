package com.typicalprojects.CellQuant.util;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.typicalprojects.CellQuant.util.ImageContainer.Channel;
import com.typicalprojects.CellQuant.util.ImageContainer.ImageOpenException;
import com.typicalprojects.CellQuant.util.ImageContainer.ImageTag;

import ij.measure.Calibration;

public class ImagePhantom {
	private ImageContainer ic;
	private File imageFile;
	private Logger logger;
	private String title;
	private Calibration cal;

	public ImagePhantom(File imageFile, String titleNoExtension, Logger logger, Calibration cal) {
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

	public String open(Map<Integer, Channel> validChannels, File resaveOutputDir, String timeOfRun, List<ImageTag> imagesToOpen) {
		// As one CZI
		logger.setCurrentTask("Opening " + title + "...");
		try {
			ic = new ImageContainer(title, this.imageFile, resaveOutputDir, timeOfRun, imagesToOpen, validChannels, this.cal);
			logger.setCurrentTaskComplete();
			return null;


		} catch (ImageOpenException e) {
			logger.setCurrentTask("Failed to open.");
			System.out.println(e);
			e.printStackTrace();
			return (e.getMessage() == null ? "Unknown reason." : e.getMessage());

		}


	}



	public ImageContainer getIC() {
		return this.ic;
	}




}
