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
package com.typicalprojects.TronMachine.neuronal_migration;

import java.io.Serializable;

public enum OutputOption implements Serializable {
	
	Channel("Raw", "Chan", 1, "", 1),
	MaxedChannel("Max Projected", "MaxedChan", 1, "MAXED", 1),
	ProcessedFull("Processed-Full", "ProcessedF", 1, "OBJ, DOTS, ORIG", 2),
	ProcessedDots("Processed-Dots", "ProcessedD", 1, "DOTS", 2),
	ProcessedObjects("Processed-Objects", "ProcessedOb", 1, "OBJ", 2),
	ProcessedDotsObjects("Processed-Dots and Objects", "ProcessedDOb", 1, "OBJ AND DOTS", 2),
	ProcessedDotsOriginal("Processed-Dots and Original", "ProcessedDOr", 1, "ORIG AND DOTS", 2),
	ProcessedObjectsOriginal("Processed-Objects and Original", "ProcessedObOr", 1, "OBJ AND ORIG", 2),
	ProcessedDotsNoNum("Processed-Dots no Numbers", "DotsNoNum", 1, "DOTS NO NUM", 2),
	RoiDrawFull("ROIs on Maxed Channel", "ROIDraw", 2, "MAXED + ROIS", 1),
	RoiDrawBlank("ROIs on Blank Image", "RoiDrawBlank", 2, "BLANK ROIS", 3),
	RoiDrawProcessedFull("ROIs on Processed-Full", "ROIDrawF", 2, "OBJ, DOTS, ORIG + ROIS", 2),
	RoiDrawProcessedDots("ROIs on Processed-Dots", "ROIDrawD", 2, "DOTS + ROIS", 2),
	RoiDrawProcessedObjects("ROIs on Processed-Objects", "ROIDrawOb", 2, "OBJ + ROIS", 2),
	RoiDrawProcessedDotsObjects("ROIs on Processed-Dots & Objects", "ROIDrawDOb", 2, "OBJ AND DOTS + ROIS", 2),
	RoiDrawProcessedDotsOriginal("ROIs on Processed-Dots & Original", "ROIDrawDOr", 2, "ORIG AND DOTS + ROIS", 2),
	RoiDrawProcessedObjectsOriginal("ROIs on Processed-Objects & Original", "ROIDrawObOr", 2, "OBJ AND ORIG + ROIS", 2),
	RoiDrawProcessedDotsNoNum("ROIs on Processed-Dots no Numbers", "ROIDrawDNoNum", 2, "DOTS NO NUM + ROIS", 2),
	BinDrawFull("Bins on Raw Channel", "BinDraw", 2, "MAXED + BINS", 2),
	BinDrawBlank("Bins on Blank Image", "BinDrawBlank", 2, "BLANK BINS", 3),
	BinDrawProcessedFull("Bins on Processed-Full", "BinDrawF", 2, "OBJ, DOTS, ORIG + BINS", 2),
	BinDrawProcessedDots("Bins on Processed-Dots", "BinDrawD", 2, "DOTS + BINS", 2),
	BinDrawProcessedObjects("Bins on Processed-Objects", "BinDrawOb", 2, "OBJ + BINS", 2),
	BinDrawProcessedDotsObjects("Bins on Processed-Dots & Objects", "BinDrawDOb", 2, "OBJ AND DOTS + BINS", 2),
	BinDrawProcessedDotsOriginal("Bins on Processed-Dots & Original", "BinDrawDOr", 2, "ORIG AND DOTS + BINS", 2),
	BinDrawProcessedObjectsOriginal("Bins on Processed-Objects & Original", "BinDrawObOr", 2, "OBJ AND ORIG + BINS", 2),
	BinDrawProcessedDotsNoNum("Bins on Processed-Dots no Numbers", "BinDrawDNoNum", 2, "DOTS NO NUM + BINS", 2);


	private String display;
	private String condensed;
	private int list;
	private String imageSuffix;
	private int restrictedOption;
	public static final int ALL_CHANS = 1;
	public static final int ONLY_PROCESSED_CHAN = 2;
	public static final int NO_CHANS = 3;

	private OutputOption(String display, String condensed, int list, String imageSuffix, int restrictedOption) {
		this.display = display;
		this.condensed = condensed;
		this.list = list;
		this.imageSuffix = imageSuffix;
		this.restrictedOption = restrictedOption;
	}
	public int getRestrictedOption() {
		return this.restrictedOption;
	}
	public String getDisplay() {
		return this.display;
	}
	public String getCondensed() {
		return this.condensed;
	}
	public int getList() {
		return this.list;
	}
	public String getImageSuffix() {
		return this.imageSuffix;
	}
	public static OutputOption fromCondensed(String string) {
		for (OutputOption option : OutputOption.values()) {
			if (option.getCondensed().equals(string))
				return option;
		}
		return null;
	}
}
