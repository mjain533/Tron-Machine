package com.typicalprojects.CellQuant.processing;

import com.typicalprojects.CellQuant.util.SynchronizedProgress;

import ij.ImagePlus;
import ij.measure.ResultsTable;

public class Custome3DObjectCounter {

	private ImagePlus imageStack;


	
	public static int maxSize = 5000; // always mix pixel number
	public static int minSize = 200; // Can be set
	private final int threshold;
	
	public static final int NONE = 0;
	public static final int NUM_ONLY = 1;
	public static final int DOT_ONLY = 2;
	public static final int NUM_AND_DOTS = 3;
	
	public static boolean opExcludeOnEdges = false;
	
	public static boolean opCalcImgObjects = true;
	public static boolean opCalcImgSurfaces = false;
	public static boolean opCalcImgCentroids = false;
	public static boolean opCalcImgCenterOfMass = true;
	
	public static boolean opCalcStats = true;
	public static boolean opCalcSummary = false;
	
	// IN Obj Counter Options
	public static int opResultDotsSize = 10;
	public static int opResultFontSize = 20;
	public static boolean opResultWhiteNum = true;
	
	public static int opImgObjectsDispType = NONE;
	public static int opImgSurfacesDispType = NONE;
	public static int opImgCentroidsDispType = NONE;
	public static int opImgCenterOfMassDispType = NUM_AND_DOTS;
	
	public static Custom3DObjectCounterStats statsPrefs = new Custom3DObjectCounterStats();
	
	private ImagePlus objectMap = null;
	private ImagePlus surfaceMap = null;
	private ImagePlus centroidMap = null;
	private ImagePlus centerOfMassMap = null;
	
	private String summary = null;
	private ResultsTable stats = null;
		
	public Custome3DObjectCounter(ImagePlus imageStack) throws IllegalArgumentException {
		if (imageStack.getBitDepth()>16){
			throw new IllegalArgumentException();
		}

		this.imageStack = imageStack;
		this.threshold = Math.max(1, imageStack.getImageStack().getProcessor(1).getAutoThreshold());
	}

	public void run(SynchronizedProgress progress) throws IllegalStateException {


		Custom3DCounter OC = new Custom3DCounter(this.imageStack, this.threshold, minSize, maxSize, opExcludeOnEdges, progress);
		
		
		if (opCalcImgObjects) { 
			boolean dispNum = false;
			int fontSize = 0;
			if (opImgObjectsDispType == NUM_ONLY || opImgObjectsDispType == NUM_AND_DOTS) {
				fontSize = opResultFontSize;
				dispNum = true;
			}
			this.objectMap = OC.getObjMap(dispNum, fontSize);
		}
		if (opCalcImgSurfaces){
			boolean dispNum = false;
			int fontSize = 0;
			if (opImgSurfacesDispType == NUM_ONLY || opImgSurfacesDispType == NUM_AND_DOTS) {
				fontSize = opResultFontSize;
				dispNum = true;
			}
			this.surfaceMap = OC.getSurfPixMap(dispNum, opResultWhiteNum, fontSize);
		}
		if (opCalcImgCentroids) {
			boolean dispNum = false;
			int fontSize = 0;
			if (opImgCentroidsDispType == NUM_ONLY || opImgCentroidsDispType == NUM_AND_DOTS) {
				fontSize = opResultFontSize;
				dispNum = true;
			}
			
			int dotsSize = 0;
			if (opImgCentroidsDispType == DOT_ONLY || opImgCentroidsDispType == NUM_AND_DOTS) {
				dotsSize = opResultDotsSize;
			}
			this.centroidMap = OC.getCentroidMap(dispNum, opResultWhiteNum, dotsSize, fontSize);
		}
		if (opCalcImgCenterOfMass) {
			boolean dispNum = false;
			int fontSize = 0;

			if (opImgCenterOfMassDispType == NUM_ONLY || opImgCenterOfMassDispType == NUM_AND_DOTS) {
				fontSize = opResultFontSize;
				dispNum = true;
			}
			
			int dotsSize = 0;
			if (opImgCenterOfMassDispType == DOT_ONLY || opImgCenterOfMassDispType == NUM_AND_DOTS) {
				dotsSize = opResultDotsSize;
			}
			this.centerOfMassMap = OC.getCentreOfMassMap(dispNum, opResultWhiteNum, dotsSize, fontSize);
		}


		if (opCalcStats) this.stats = OC.createStatisticsTable(statsPrefs);

		if (opCalcSummary) this.summary = OC.createSummary();
		
		OC = null;
		System.gc();
	}
	
	public ImagePlus getObjectMap() {		
		return this.objectMap;
	}
	
	public ImagePlus getSurfacesMap() {
		
		return this.surfaceMap;
	}
	
	public ImagePlus getCentroidsMap() {
		
		return this.centroidMap;
	}
	
	public ImagePlus getCOMMap() {
		
		return this.centerOfMassMap;
	}
	
	public String getSummary() {
		return this.summary;
	}
	
	public ResultsTable getStats() {
		return this.stats;
	}

}
