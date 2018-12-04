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

import com.typicalprojects.TronMachine.neuronal_migration.RunConfiguration;
import com.typicalprojects.TronMachine.util.ImageContainer.ImageOpenException;


public class ImagePhantom {
	private ImageContainer ic;
	private File imageFile;
	private Logger logger;
	private String title;
	private RunConfiguration runConfig;

	public ImagePhantom(File imageFile, String titleNoExtension, Logger logger, RunConfiguration runConfig) {
		this.title = titleNoExtension;
		this.imageFile = imageFile;
		this.logger = logger;
		this.runConfig = runConfig;
	}

	public File getImageFile() {
		return this.imageFile;
	}
	public String getTitle() {
		return this.title;
	}

	public String openOriginal(File resaveOutputDir, String timeOfRun) {
		// As one CZI
		logger.setCurrentTask("Opening " + title + "...");
		try {
			ic = new ImageContainer(title, this.imageFile, resaveOutputDir, timeOfRun, this.runConfig);
			logger.setCurrentTaskComplete();
			return null;


		} catch (ImageOpenException e) {
			logger.setCurrentTask("Failed to open.");
			e.printStackTrace();
			return (e.getMessage() == null ? "Unknown reason." : e.getMessage());

		}


	}

	public ImageContainer getIC() {
		return this.ic;
	}




}
