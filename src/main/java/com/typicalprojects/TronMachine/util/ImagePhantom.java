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

import java.io.File;
import java.util.List;
import java.util.Map;

import com.typicalprojects.TronMachine.neuronal_migration.OutputOption;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;
import com.typicalprojects.TronMachine.util.ImageContainer.ImageOpenException;

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

	public String open(Map<Integer, Channel> validChannels, File resaveOutputDir, String timeOfRun, List<OutputOption> imagesToOpen) {
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
