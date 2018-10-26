package com.typicalprojects.TronMachine.neuronal_migration;

public enum OutputOption {
	
	Channel("Channel (Raw)", "Chan", 1, "", 1),
	ChannelTiff("Channel (Tiff)", "ChanTiff", 1, "", 1),
	MaxedChannel("Max Projected Channel", "MaxedChan", 1, "MAXED", 1),
	ProcessedFull("Processed Chan-Full", "ProcessedF", 1, "OBJ, DOTS, ORIG", 2),
	ProcessedDots("Processed Chan-Dots", "ProcessedD", 1, "DOTS", 2),
	ProcessedObjects("Processed Chan-Objects", "ProcessedOb", 1, "OBJ", 2),
	ProcessedDotsObjects("Processed Chan-Dots and Objects", "ProcessedDOb", 1, "OBJ AND DOTS", 2),
	ProcessedDotsOriginal("Processed Chan-Dots and Original", "ProcessedDOr", 1, "ORIG AND DOTS", 2),
	ProcessedObjectsOriginal("Processed Chan-Objects and Original", "ProcessedObOr", 1, "OBJ AND ORIG", 2),
	ProcessedDotsNoNum("Processed Chan-Dots no Numbers", "DotsNoNum", 1, "DOTS NO NUM", 2),
	RoiDrawFull("ROIs on Raw Maxed Channel", "ROIDraw", 2, "MAXED + ROIS", 1),
	RoiDrawBlank("ROIs on Blank Image", "RoiDrawBlank", 2, "BLANK ROIS", 3),
	RoiDrawProcessedFull("ROIs on Processed Chan-Full", "ROIDrawF", 2, "OBJ, DOTS, ORIG + ROIS", 2),
	RoiDrawProcessedDots("ROIs on Processed Chan-Dots", "ROIDrawD", 2, "DOTS + ROIS", 2),
	RoiDrawProcessedObjects("ROIs on Processed Chan-Objects", "ROIDrawOb", 2, "OBJ + ROIS", 2),
	RoiDrawProcessedDotsObjects("ROIs on Processed Chan-Dots & Objects", "ROIDrawDOb", 2, "OBJ AND DOTS + ROIS", 2),
	RoiDrawProcessedDotsOriginal("ROIs on Processed Chan-Dots & Original", "ROIDrawDOr", 2, "ORIG AND DOTS + ROIS", 2),
	RoiDrawProcessedObjectsOriginal("ROIs on Processed Chan-Objects & Original", "ROIDrawObOr", 2, "OBJ AND ORIG + ROIS", 2),
	RoiDrawProcessedDotsNoNum("ROIs on Processed Chan-Dots no Numbers", "ROIDrawDNoNum", 2, "DOTS NO NUM + ROIS", 2),
	BinDrawFull("Bins on Raw Channel", "BinDraw", 2, "MAXED + BINS", 2),
	BinDrawBlank("Bins on Blank Image", "BinDrawBlank", 2, "BLANK BINS", 3),
	BinDrawProcessedFull("Bins on Processed Chan-Full", "BinDrawF", 2, "OBJ, DOTS, ORIG + BINS", 2),
	BinDrawProcessedDots("Bins on Processed Chan-Dots", "BinDrawD", 2, "DOTS + BINS", 2),
	BinDrawProcessedObjects("Bins on Processed Chan-Objects", "BinDrawOb", 2, "OBJ + BINS", 2),
	BinDrawProcessedDotsObjects("Bins on Processed Chan-Dots & Objects", "BinDrawDOb", 2, "OBJ AND DOTS + BINS", 2),
	BinDrawProcessedDotsOriginal("Bins on Processed Chan-Dots & Original", "BinDrawDOr", 2, "ORIG AND DOTS + BINS", 2),
	BinDrawProcessedObjectsOriginal("Bins on Processed Chan-Objects & Original", "BinDrawObOr", 2, "OBJ AND ORIG + BINS", 2),
	BinDrawProcessedDotsNoNum("Bins on Processed Chan-Dots no Numbers", "BinDrawDNoNum", 2, "DOTS NO NUM + BINS", 2);


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
