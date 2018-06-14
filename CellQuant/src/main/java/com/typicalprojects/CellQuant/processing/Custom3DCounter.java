package com.typicalprojects.CellQuant.processing;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.process.ImageProcessor;

import java.awt.Font;
import java.util.Arrays;
import java.util.Vector;

import com.typicalprojects.CellQuant.SynchronizedProgress;


public class Custom3DCounter {
	
	public enum Column {
		
		X("X", 0), Y("Y", 1);
		
		private String title;
		private int colNum;
		
		private Column(String title, int colNum) {
			this.title = title;
			this.colNum = colNum;
		}
		
		public int getColumnNum() {
			return this.colNum;
		}
		
		public String getTitle() {
			return this.title;
		}
		
		public String toString() {
			return this.getTitle();
		}
		
	}
	
	private final int thr, width, height, nbSlices, length, depth, minSize, maxSize;
	private final boolean exclude;
	private final String title;
	boolean[] isSurf;
	Calibration cal;
	private int nbObj=0, nbSurfPix=0;
	int[] imgArray, objID, IDcount, surfList;
	boolean[] IDisAtEdge;
	int[][] surfCoord;
	float[][] centreOfMass, centroid;
	int[][] imgPixelIntensities;
	Vector<Object3D> obj;

	boolean gotCentreOfMass=false, gotCentroid=false, gotSurfList=false, gotSurfCoord=false;

	public Custom3DCounter(final ImagePlus img, int thr, int minSize, int maxSize, boolean exclude, SynchronizedProgress progressID) {

		this.thr = thr;
		this.width=img.getWidth();
		this.height=img.getHeight();
		this.nbSlices=img.getNSlices();
		this.length = this.width * this.height * this.nbSlices;
		this.depth=img.getBitDepth();
		this.title=img.getTitle();
		this.cal=img.getCalibration();
		this.exclude = exclude;
		this.minSize=minSize;
		this.maxSize=maxSize;

		if (depth!=8 && depth!=16) throw new IllegalArgumentException("3D object counter expects 8- or 16-bits images only");

		this.nbObj=this.length;

		this.imgArray=new int[this.length];

		imgArrayModifier(img);

		findObjects(progressID);

	}



	/** Generates the connexity analysis.
	 */
	private void findObjects(SynchronizedProgress progressID) {
		//First ID attribution
		int currID=0;
		int currPos=0;
		int minID=0;
		int surfPix=0;
		int pos, currPixID;
		int neigbX, neigbY, neigbZ;

		/*
         Finding the structures:
		 *The minID tag is initialized with the current value of tag (currID).If thresholded,
		 *the neighborhood of the current pixel is collected. For each of those 13 pixels,
		 *the value is retrieved and tested against minID: only the minimum of the two is kept.
		 *As anterior pixels have already been tagged, only two possibilities may exists:
		 *1-The minimum is currID: we start a new structure and currID should be incremented
		 *2-The minimum is not currID: we continue an already existing structure
		 *Each time a new pixel is tagged, a counter of pixels in the current tag is incremented.
		 */

		objID=new int[length];

		for (int z=1; z<=nbSlices; z++){
			for (int y=0; y<height; y++){
				for (int x=0; x<width; x++){
					if (minID==currID) currID++;
					if (imgArray[currPos]!=0){
						minID=currID;
						minID=minAntTag(minID, x, y, z);
						objID[currPos]=minID;
					}
					currPos++;
				}
			}
			//IJ.showStatus("Finding structures "+z*100/nbSlices+"%");
			progressID.setProgress("Step 1/3: Finding structures", z, nbSlices);
		}

		IDcount=new int[currID];
		for (int i=0; i<length; i++) IDcount[objID[i]]++;

		IDisAtEdge=new boolean[currID];
		Arrays.fill(IDisAtEdge, false);
		/*
		 *Connecting structures:
		 *The first tagging of structure may have led to shearing apart pieces of a same structure
		 *This part will connect them back by attributing the minimal retrieved tag among the 13 neighboring
		 *pixels located prior to the current pixel + the centre pixel and will replace all the values of those pixels
		 *by the minimum value.
		 */
		isSurf=new boolean[length];
		currPos=0;
		minID=1;

		for (int z=1; z<=nbSlices; z++){
			for (int y=0; y<height; y++){
				for (int x=0; x<width; x++){
					if (imgArray[currPos]!=0){
						minID=objID[currPos];
						surfPix=0;
						//Find the minimum tag in the neighbours pixels
						for (neigbZ=z-1; neigbZ<=z+1; neigbZ++){
							for (neigbY=y-1; neigbY<=y+1; neigbY++){
								for (neigbX=x-1; neigbX<=x+1; neigbX++){
									//Following line is important otherwise objects might be linked from one side of the stack to the other !!!
									if (neigbX>=0 && neigbX<width && neigbY>=0 && neigbY<height && neigbZ>=1 && neigbZ<=nbSlices){
										pos=offset(neigbX, neigbY, neigbZ);
										if (imgArray[pos]!=0){
											if ((nbSlices>1 && ((neigbX==x && neigbY==y && neigbZ==z-1) ||(neigbX==x && neigbY==y && neigbZ==z+1))) ||(neigbX==x && neigbY==y-1 && neigbZ==z) ||(neigbX==x && neigbY==y+1 && neigbZ==z) ||(neigbX==x-1 && neigbY==y && neigbZ==z) ||(neigbX==x+1 && neigbY==y && neigbZ==z)) surfPix++;
											minID=Math.min(minID, objID[pos]);
										}
									}
								}
							}
						}
						if ((surfPix!=6 && nbSlices>1) || (surfPix!=4 && nbSlices==1)){
							isSurf[currPos]=true;
							nbSurfPix++;
						}else{
							isSurf[currPos]=false;
						}
						//Replacing tag by the minimum tag found
						for (neigbZ=z-1; neigbZ<=z+1; neigbZ++){
							for (neigbY=y-1; neigbY<=y+1; neigbY++){
								for (neigbX=x-1; neigbX<=x+1; neigbX++){
									//Following line is important otherwise objects might be linked from one side of the stack to the other !!!
									if (neigbX>=0 && neigbX<width && neigbY>=0 && neigbY<height && neigbZ>=1 && neigbZ<=nbSlices){
										pos=offset(neigbX, neigbY, neigbZ);
										if (imgArray[pos]!=0){
											currPixID=objID[pos];
											if (currPixID>minID) replaceID(currPixID, minID);
										}
									}
								}
							}
						}

						//Check if the current particle is touching an edge
						if(x==0 || y==0 || x==width-1 || y==height-1 || (nbSlices!=1 && (z==1 || z==nbSlices))) IDisAtEdge[minID]=true;
					}
					currPos++;
				}
			}
			progressID.setProgress("Step 2/3: Connecting structures", z, nbSlices);
		}

		int newCurrID=0;

		//Renumbering of all the found objects and update of their respective number of pixels while filtering based on the number of pixels
		for (int i=1; i<IDcount.length; i++){
			if ((IDcount[i]!=0 && IDcount[i]>=minSize && IDcount[i]<=maxSize)&& (!exclude || !(exclude && IDisAtEdge[i]))){
				newCurrID++;
				int nbPix=IDcount[i];
				replaceID(i, newCurrID);
				IDcount[newCurrID]=nbPix;
			}else{
				replaceID(i,0);
			}
			progressID.setProgress("Step 3/3: Renumbering structures", i, IDcount.length - 1);
		}

		nbObj=newCurrID;

		getObjects();
	}

	/** Generates the objects list.
	 */
	private void getObjects(){

		obj=new Vector<Object3D>();

		for (int i=0; i<nbObj; i++) obj.add(new Object3D(IDcount[i+1], cal));
		IDcount=null;

		int currPos=0;
		for (int z=1; z<=nbSlices; z++){
			for (int y=0; y<height; y++){
				for (int x=0; x<width; x++){
					int currID=objID[currPos];
					if (currID!=0){
						float surf=0;
						if (nbSlices==1) surf=(float) (cal.pixelWidth*cal.pixelHeight);
						if (isSurf[currPos] && nbSlices>1){
							surf=(float) (2*(cal.pixelHeight*cal.pixelDepth+cal.pixelWidth*cal.pixelDepth+cal.pixelWidth*cal.pixelHeight));
							//Look at the 6 exposed surfaces
							if (x>0 && objID[offset (x-1, y, z)]==currID) surf-=cal.pixelHeight*cal.pixelDepth;
							if (x<width-1 && objID[offset (x+1, y, z)]==currID) surf-=cal.pixelHeight*cal.pixelDepth;
							if (y>0 && objID[offset (x, y-1, z)]==currID) surf-=cal.pixelWidth*cal.pixelDepth;
							if (y<height-1 && objID[offset (x, y+1, z)]==currID) surf-=cal.pixelWidth*cal.pixelDepth;
							if (z>1 && objID[offset (x, y, z-1)]==currID) surf-=cal.pixelWidth*cal.pixelHeight;
							if (z<=nbSlices-1 && objID[offset (x, y, z+1)]==currID) surf-=cal.pixelWidth*cal.pixelHeight;
						}
						((Object3D) (obj.get(currID-1))).addVoxel(x, y, z, imgArray[currPos], isSurf[currPos], surf);
					}
					currPos++;
				}
			}
		}
		imgArray=null;
		System.gc();


	}

	
	/**
	 * Returns the object at the provided index, as an Object3D
	 * @param index the index of the object to return
	 * @return an Object3D or null idf the index is out of bounds
	 */
	public Object3D getObject(int index){

		if (index<0 || index>=nbObj) return null;
		return (Object3D) obj.get(index);
	}

	/**
	 * Add the provided Object3D to the list 
	 * @param object Object3D to add
	 */
	public void addObject(Object3D object){

		obj.add(object);
		nbObj++;
	}

	/**
	 * Removes the Object3D stored at the provided index. Does nothing if index is out of bounds.
	 * @param index index of the Object3D to be removed
	 */
	public void removeObject(int index){

		if (!(index<0 || index>=nbObj)){
			obj.remove(index);
			nbObj--;
		}
	}


	/**
	 * Returns the list of all found objects.
	 *
	 * @return the list of all found objects as a Object3D array.
	 */
	public Vector<Object3D> getObjectsList(){
		return obj;
	}

	/**
	 * Returns the objects map.
	 * @param drawNb should be true if numbers have to be drawn at each coordinate stored in cenArray (boolean).
	 * @param fontSize font size of the numbers to be shown (integer).
	 * @return an ImagePlus containing all found objects, each one carrying pixel value equal to its ID.
	 */
	public ImagePlus getObjMap(boolean drawNb, int fontSize){
		if (!gotCentroid) populateCentroid();
		return buildImg(objID, coord2imgArray(centroid), "Objects map of "+title, false, drawNb, true, 0, fontSize);
	}

	/**
	 * Returns the objects map as a 1D integer array.
	 *
	 * @return an ImagePlus containing all found objects, each one carrying pixel value equal to its ID.
	 */
	public int[] getObjMapAsArray(){
		return objID;
	}

	/** Generates and fills the "centreOfMass" array.
	 */
	private void populateCentreOfMass(){
		
		centreOfMass=new float[obj.size()][3];

		for (int i=0; i<obj.size(); i++){
			Object3D currObj=(Object3D) obj.get(i);
			float [] tmp=currObj.c_mass;
			for (int j=0; j<3; j++) centreOfMass[i][j]=tmp[j];
		}
		gotCentreOfMass=true;
	}

	/**
	 * Returns the centres of masses' list.
	 *
	 * @return the coordinates of all found centres of masses as a dual float array ([ID][0:x, 1:y, 2:z]).
	 */
	public float[][] getCentreOfMassList(){
		if (!gotCentreOfMass) populateCentreOfMass();
		return centreOfMass;
	}

	/**
	 * Returns the centres of masses' map.
	 * @param drawNb should be true if numbers have to be drawn at each coordinate stored in cenArray (boolean).
	 * @param whiteNb should be true if numbers have to appear white  (boolean).
	 * @param dotSize size of the dots to be drawn (integer).
	 * @param fontSize font size of the numbers to be shown (integer).* @return an ImagePlus containing all centres of masses, each one carrying pixel value equal to its ID.
	 */
	public ImagePlus getCentreOfMassMap(boolean drawNb, boolean whiteNb, int dotSize, int fontSize){
		if (!gotCentreOfMass) populateCentreOfMass();
		int[] array=coord2imgArray(centreOfMass);
		return buildImg(array, array, "Centres of mass map of "+title, true, drawNb, whiteNb, dotSize, fontSize);
	}

	/**
	 * Returns the centres of masses' map.
	 *
	 * @return an ImagePlus containing all centres of masses, each one carrying pixel value equal to its ID.
	 */
	public ImagePlus getCentreOfMassMap(){
		if (!gotCentreOfMass) populateCentreOfMass();
		int[] array=coord2imgArray(centreOfMass);
		return buildImg(array, null, "Centres of mass map of "+title, true, false, false, 5, 0);
	}

	/** Generates and fills the "centroid" array.
	 */
	private void populateCentroid(){
		centroid=new float[obj.size()][3];

		for (int i=0; i<obj.size(); i++){
			Object3D currObj=(Object3D) obj.get(i);
			float [] tmp=currObj.centroid;
			for (int j=0; j<3; j++) centroid[i][j]=tmp[j];
		}
		gotCentroid=true;
	}

	/**
	 * Returns the centroïds' list.
	 *
	 * @return the coordinates of all found centroïds as a dual float array ([ID][0:x, 1:y, 2:z]).
	 */
	public float[][] getCentroidList(){
		if (!gotCentroid) populateCentroid();
		return centroid;
	}

	/**
	 * Returns the centroïds' map.
	 * @param drawNb should be true if numbers have to be drawn at each coordinate stored in cenArray (boolean).
	 * @param whiteNb should be true if numbers have to appear white  (boolean).
	 * @param dotSize size of the dots to be drawn (integer).
	 * @param fontSize font size of the numbers to be shown (integer).* @return an ImagePlus containing all centroïds, each one carrying pixel value equal to its ID.
	 */
	public ImagePlus getCentroidMap(boolean drawNb, boolean whiteNb, int dotSize, int fontSize){
		if (!gotCentroid) populateCentroid();
		int[] array=coord2imgArray(centroid);
		return buildImg(array, array, "Centroids map of "+title, true, drawNb, whiteNb, dotSize, fontSize);
	}

	/**
	 * Returns the centroïds' map.
	 *
	 * @return an ImagePlus containing all centroïds, each one carrying pixel value equal to its ID.
	 */
	public ImagePlus getCentroidMap(){
		if (!gotCentroid) populateCentroid();
		int[] array=coord2imgArray(centroid);
		return buildImg(array, null, "Centroids map of "+title, true, false, false, 5, 0);
	}

	/** Generates and fills the "surface" array.
	 */
	private void populateSurfList(){

		surfList=new int[length];
		for (int i=0; i<length; i++) surfList[i]=isSurf[i]?objID[i]:0;
		gotSurfList=true;
	}

	/**
	 * Returns the surface pixels' list.
	 *
	 * @return the coordinates of all pixels found at the surface of objects as a mono-dimensional integer array.
	 */
	public int[] getSurfPixList(){
		if (!gotSurfList) populateSurfList();
		return surfList;
	}

	/** Generates and fills the "surfArray" array.
	 */
	private void populateSurfPixCoord(){
		int index=0;

		surfCoord=new int[nbSurfPix][4];

		for (int i=0; i<nbObj; i++){
			Object3D currObj=(Object3D) obj.get(i);
			for (int j=0; j<currObj.surf_size; j++){
				surfCoord[index][0]=i+1;
				for (int k=1; k<4; k++) surfCoord[index][k]=currObj.obj_voxels[j][k-1];
				index++;
			}
		}
	}

	/**
	 * Returns the surface pixels coordinates' list.
	 *
	 * @return the coordinates of all pixels found at the surface of objects as a dual integer array([index][0:x, 1:y, 2:z, 3:ID]).
	 */
	public int[][] getSurfPixCoord(){
		if (!gotSurfCoord) populateSurfPixCoord();
		return surfCoord;
	}

	/**
	 * Returns the surface pixels' map.
	 * @param drawNb should be true if numbers have to be drawn at each coordinate stored in cenArray (boolean).
	 * @param whiteNb should be true if numbers have to appear white  (boolean).
	 * @param fontSize font size of the numbers to be shown (integer).* @return an ImagePlus containing all pixels found at the surface of objects, each one carrying pixel value equal to its ID.
	 */
	public ImagePlus getSurfPixMap(boolean drawNb, boolean whiteNb, int fontSize){
		if (!gotSurfList) populateSurfList();
		if (!gotCentroid) populateCentroid();
		return buildImg(surfList, coord2imgArray(centroid), "Surface map of "+title, false, drawNb, whiteNb, 0, fontSize);
	}

	/**
	 * Returns the surface pixels' map.
	 *
	 * @return an ImagePlus containing all pixels found at the surface of objects, each one carrying pixel value equal to its ID.
	 */
	public ImagePlus getSurfPixMap(){
		if (!gotSurfList) populateSurfList();
		return buildImg(surfList, null, "Surface map of "+title, false, false, false, 0, 0);
	}

	/** Transforms a coordinates array ([ID][0:x, 1:y, 3:z]) to a linear array containing all pixels one next to the other.
	 *
	 *@return the linear array as an integer array.
	 */
	private int[] coord2imgArray(float[][] coord){
		int[] array=new int[length];
		for (int i=0; i<coord.length; i++)array[offset((int) coord[i][0], (int) coord[i][1], (int) coord[i][2])]=i+1;
		return array;
	}

	/** Set to zero pixels below the threshold in the "imgArray" arrays.
	 */
	private void imgArrayModifier(ImagePlus img){
		int index=0;
		for (int i=1; i<=nbSlices; i++){
			img.setSlice(i);
			for (int j=0; j<height; j++){
				for (int k=0; k<width; k++){
					imgArray[index]=img.getProcessor().getPixel(k, j);
					if (imgArray[index]<thr){
						imgArray[index]=0;
						nbObj--;
					}
					index++;
				}
			}
		}

		if (nbObj<=0){
			IJ.error("No object found");
			return;
		}
	}

	/** Returns an ResultsTable containing statistics on objects:
	 * <ul>
	 * <li>Volume and Surface: number of pixel forming the structures and at its surface respectively.</li>
	 * <li>StdDev, Median, IntDen, Min and Max: standard deviation, median, sum, minimum and maximum of all intensities for the current object.</li>
	 * <li>X, Y and Z: coordinates of the current object's centroïd.</li>
	 * <li>XM, YM and ZM: coordinates of the current object's centre of mass.</li>
	 * <li>BX, BY and BZ: coordinates of the top-left corner of the current object's bounding box.</li>
	 * <li>B-width, B-height and B-depth: current object's bounding box dimensions.</li>
	 * </ul>
	 */
	@SuppressWarnings("deprecation")
	public ResultsTable createStatisticsTable(Custom3DObjectCounterStats stats){
		//float calXYZ=(float) (cal.pixelWidth*cal.pixelHeight*cal.pixelDepth);
		//String unit=cal.getUnit();

		//String[] header={"", "Surface ("+unit+"^2)", "Nb of obj. voxels", "Nb of surf. voxels", "IntDen", "Mean", "StdDev", "Median", "Min", "Max", "X", "Y", "Z", "Mean dist. to surf. ("+unit+")", "SD dist. to surf. ("+unit+")", "Median dist. to surf. ("+unit+")", "XM", "YM", "ZM", "BX", "BY", "BZ", "B-width", "B-height", "B-depth"};
		ResultsTable rt = new ResultsTable();
		for (Column col : Column.values()) {
			rt.setHeading(col.colNum, col.title);
		}
		for (int i=0; i<nbObj; i++){
			rt.incrementCounter();
			Object3D currObj=(Object3D) obj.get(i);
			
			/*if (stats.calcVolume) rt.setValue("Volume ("+unit+"^3)", i, currObj.size*calXYZ);
			if (stats.calcSurface) rt.setValue("Surface ("+unit+"^2)", i, currObj.surf_cal);
			if (stats.calcNbObjVoxels) rt.setValue("Nb of obj. voxels", i, currObj.size);
			if (stats.calcNbSurfVoxels) rt.setValue("Nb of surf. voxels", i, currObj.surf_size);
			if (stats.calcIntegratedDensity) rt.setValue("IntDen", i, currObj.int_dens);*/
			//if (stats.calcMeanGrayVal) rt.setValue(Column.Grayscale.title, i, currObj.mean_gray);
			/*if (stats.calcStDevGrayVal) rt.setValue("StdDev", i, currObj.SD);
			if (stats.calcMedianGrayVal) rt.setValue("Median", i, currObj.median);
			if (stats.calcMinGrayVal) rt.setValue("Min", i, currObj.min);
			if (stats.calcMaxGrayVal) rt.setValue("Max", i, currObj.max);*/


			if (/*stats.calcCentroid*/true){
				float[] tmpArray=currObj.centroid;
				rt.setValue(Column.X.title, i, tmpArray[0]);
				rt.setValue(Column.Y.title, i, tmpArray[1]);
				/*if (nbSlices!=1) rt.setValue("Z", i, tmpArray[2]);*/
			}

			/*if (stats.calcMeanDistSurf) rt.setValue("Mean dist. to surf. ("+unit+")", i, currObj.mean_dist2surf);
			if (stats.calcStDevDistToSurf) rt.setValue("SD dist. to surf. ("+unit+")", i, currObj.SD_dist2surf);
			if (stats.calcMeanDistSurf) rt.setValue("Median dist. to surf. ("+unit+")", i, currObj.median_dist2surf);

			if (stats.calcCOM){
				float[] tmpArray=currObj.c_mass;
				rt.setValue("XM", i, tmpArray[0]);
				rt.setValue("YM", i, tmpArray[1]);
				if (nbSlices!=1) rt.setValue("ZM", i, tmpArray[2]);
			}

			if (stats.calcBB){
				int[] tmpArrayInt=currObj.bound_cube_TL;
				rt.setValue("BX", i, tmpArrayInt[0]);
				rt.setValue("BY", i, tmpArrayInt[1]);
				if (nbSlices!=1) rt.setValue("BZ", i, tmpArrayInt[2]);

				rt.setValue("B-width", i, currObj.bound_cube_width);
				rt.setValue("B-height", i, currObj.bound_cube_height);
				if (nbSlices!=1) rt.setValue("B-depth", i, currObj.bound_cube_depth);
			}*/

		}

		return rt;
	}

	/** Returns a summary containing the image name and the number of retrieved objects including the set filter size and threshold.
	 */
	public String createSummary(){
		return title+": "+nbObj+" objects detected (Size filter set to "+minSize+"-"+maxSize+" voxels, threshold set to: "+thr+").";
	}

	/** Returns an ResultsTable containing coordinates of the surface pixels for all objects:
	 * <ul>
	 * <li>Object ID: current object number.</li>
	 * <li>X, Y and Z: coordinates of the current object's surface pixel.</li>
	 * </ul>
	 */
	@SuppressWarnings("deprecation")
	public ResultsTable createSurfPix(){
		if (!gotSurfCoord) populateSurfPixCoord();

		String[] header={"Object ID", "X", "Y", "Z"};
		ResultsTable rt=new ResultsTable();
		for (int i=0; i<header.length; i++) rt.setHeading(i, header[i]);
		for (int i=0; i<surfCoord.length; i++){
			rt.incrementCounter();
			for (int j=0; j<4; j++) rt.setValue(j, i, surfCoord[i][j]);
		}

		return rt;
	}

	/** Returns the index where to find the informations corresponding to pixel (x, y, z).
	 * @param m x coordinate of the pixel.
	 * @param n y coordinate of the pixel.
	 * @param o z coordinate of the pixel.
	 * @return the index where to find the informations corresponding to pixel (x, y, z).
	 */
	private int offset(int m,int n,int o){
		if (m+n*width+(o-1)*width*height>=width*height*nbSlices){
			return width*height*nbSlices-1;
		}else{
			if (m+n*width+(o-1)*width*height<0){
				return 0;
			}else{
				return m+n*width+(o-1)*width*height;
			}
		}
	}

	/** Returns the minimum anterior tag among the 13 previous pixels (4 pixels in 2D).
	 * @param initialValue: value to which the 13 (or 4) retrieved values should be compared to
	 * @param x coordinate of the current pixel.
	 * @param y coordinate of the current pixel.
	 * @param z coordinate of the current pixel.
	 * @return the minimum found anterior tag as an integer.
	 */
	private int minAntTag(int initialValue, int x, int y, int z){
		int min=initialValue;
		int currPos;

		for (int neigbY=y-1; neigbY<=y+1; neigbY++){
			for (int neigbX=x-1; neigbX<=x+1; neigbX++){
				//Following line is important otherwise objects might be linked from one side of the stack to the other !!!
				if (neigbX>=0 && neigbX<width && neigbY>=0 && neigbY<height && z-1>=1 && z-1<=nbSlices){
					currPos=offset(neigbX, neigbY, z-1);
					if (imgArray[currPos]!=0) min=Math.min(min, objID[currPos]);
				}
			}
		}

		for (int neigbX=x-1; neigbX<=x+1; neigbX++){
			//Following line is important otherwise objects might be linked from one side of the stack to the other !!!
			if (neigbX>=0 && neigbX<width && y-1>=0 && y-1<height && z>=1 && z<=nbSlices){
				currPos=offset(neigbX, y-1, z);
				if (imgArray[currPos]!=0) min=Math.min(min, objID[currPos]);
			}
		}

		//Following line is important otherwise objects might be linked from one side of the stack to the other !!!
		if (x-1>=0 && x-1<width && y>=0 && y<height && z>=1 && z<=nbSlices ){
			currPos=offset(x-1, y, z);
			if (imgArray[currPos]!=0 && x>=1 && y>=0 && z>=1) min=Math.min(min, objID[currPos]);
		}

		return min;
	}

	/** Replaces one object ID by another within the objID array.
	 * @param old value to be replaced.
	 * @param new value to be replaced by. </P>
	 * NB: the arrays carrying the number of pixels/surface pixels carrying those IDs will also be updated.
	 */
	private void replaceID(int oldVal, int newVal){
		if (oldVal!=newVal){
			int nbFoundPix=0;
			for (int i=0; i<objID.length; i++){
				if (objID[i]==oldVal){
					objID[i]=newVal;
					nbFoundPix++;
				}
				if (nbFoundPix==IDcount[oldVal]) i=objID.length;
			}
			IDcount[oldVal]=0;
			IDcount[newVal]+=nbFoundPix;
		}
	}

	/** Generates the ImagePlus based on Counter3D object width, height and number of slices, the input array and title.
	 * @param imgArray containing the pixels intensities (integer array).
	 * @param cenArray containing the coordinates of pixels where the labels should be put (integer array).
	 * @param title to attribute to the ImagePlus (string).
	 * @param drawDots should be true if dots should be drawn instead of a single pixel for each coordinate of imgArray (boolean).
	 * @param drawNb should be true if numbers have to be drawn at each coordinate stored in cenArray (boolean).
	 * @param whiteNb should be true if numbers have to appear white  (boolean).
	 * @param dotSize size of the dots to be drawn (integer).
	 * @param fontSize font size of the numbers to be shown (integer).
	 */
	private ImagePlus buildImg(int[] imgArray, int[] cenArray, String title, boolean drawDots, boolean drawNb, boolean whiteNb, int dotSize, int fontSize){
		int index=0;
		int imgDepth=16;
		float min=imgArray[0];
		float max=imgArray[0];

		for (int i=0; i<imgArray.length; i++){
			int currVal=imgArray[i];
			min=Math.min(min, currVal);
			max=Math.max(max, currVal);
		}

		if (max<256) imgDepth=8;
		ImagePlus img=NewImage.createImage(title, width, height, nbSlices, imgDepth, 1);

		for (int z=1; z<=nbSlices; z++){
			img.setSlice(z);
			ImageProcessor ip=img.getProcessor();
			for (int y=0; y<height; y++){
				for (int x=0; x<width; x++){
					int currVal=imgArray[index];
					if (currVal!=0) {
						ip.setValue(currVal);
						if (drawDots) {
							ip.setLineWidth(dotSize);
							ip.drawDot(x, y);
						} else {
							ip.putPixel(x, y, currVal);
						}
					}
					index++;
				}
			}
		}

		index=0;
		if (drawNb && cenArray!=null){
			for (int z=1; z<=nbSlices; z++){
				IJ.showStatus("Numbering objects...");
				img.setSlice(z);
				ImageProcessor ip=img.getProcessor();
				ip.setValue(Math.pow(2, imgDepth));
				ip.setFont(new Font("Arial", Font.BOLD, fontSize));
				for (int y=0; y<height; y++){
					for (int x=0; x<width; x++){
						int currVal=cenArray[index];
						if (currVal!=0){
							if (!whiteNb) ip.setValue(currVal);
							ip.drawString(""+currVal, x, y);
							
						}
						index++;
					}
				}
			}
		}
		IJ.showStatus("");
		img.setSlice(1);
		img.setCalibration(cal);
		img.setDisplayRange(min, max);
		img.updateImage();
		return img;
	}
}
