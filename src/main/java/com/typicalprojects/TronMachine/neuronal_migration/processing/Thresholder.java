package com.typicalprojects.TronMachine.neuronal_migration.processing;

import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.StackConverter;

public class Thresholder {
	
	private final ImagePlus imp;
	
	private final boolean opIgnoreWhite = false;
	private final boolean opIgnoreBlack = false;
	
	private final boolean opWhiteObjblackBackground = true;
	private final boolean opSetThresholdInsteadOfThreshold = false;
	private final boolean opUseStack = true;
	private final boolean opUseHistogram = false;

	
	public Thresholder(ImagePlus imp) {
		this.imp = imp;
	}
	
	@SuppressWarnings("unused")
	public void threshold(int minThreshold) {

		if (imp.getBitDepth()!=8 && imp.getBitDepth()!=16) {
			throw new IllegalArgumentException();
		}
		
		int stackSize = this.imp.getStackSize();

		
		boolean success = false;
		if (stackSize>1 && (this.opUseStack || this.opUseHistogram) ) {
			if (this.opUseHistogram) {// one global histogram
				Object[] result = execute(minThreshold);
				if (((Integer) result[0]) != -1 && imp.getBitDepth()==16)
					new StackConverter(imp).convertToGray8();
			}
			else{ // slice by slice
				success=true;
				for (int k=1; k<=stackSize; k++){
					imp.setSlice(k);
					Object[] result = execute(minThreshold);
					if (((Integer) result[0]) == -1) success = false;// the threshold existed
				}
				if (success && imp.getBitDepth()==16)
					new StackConverter(imp).convertToGray8();
			}
			imp.setSlice(1);
		}
		else { //just one slice, leave as it is
			Object[] result = execute(minThreshold);
			if(((Integer) result[0]) != -1 && stackSize==1 &&  imp.getBitDepth()==16) {
				imp.setDisplayRange(0, 65535);  
				imp.setProcessor(null, imp.getProcessor().convertToByte(true));
			}
		}
		// 5 - If all went well, show the image:
		// not needed here as the source image is binarised

	}
	//IJ.showStatus(IJ.d2s((System.currentTimeMillis()-start)/1000.0, 2)+" seconds");

	/** Execute the plugin functionality.
	 * @return an Object[] array with the threshold and the ImagePlus.
	 * Does NOT show the new, image; just returns it. */
	private Object[] execute(int minThreshold) {

		// 0 - Check validity of parameters
		if (null == imp) return null;
		int threshold=-1;
		int currentSlice = imp.getCurrentSlice();
		ImageProcessor ip = imp.getProcessor();
		int xe = ip.getWidth();
		int ye = ip.getHeight();
		int x, y, c=0;
		int b = imp.getBitDepth()==8?255:65535;
		if (this.opWhiteObjblackBackground){
			c=b;
			b=0;
		}
		int [] data = (ip.getHistogram());
		int [] temp = new int [data.length];

		//1 Do it
		if (imp.getStackSize()==1){
			ip.snapshot();
		} else if (this.opUseHistogram){

			temp=data;
			for(int i=1; i<=imp.getStackSize();i++) {

				if (i==currentSlice)
					continue;
				imp.setSliceWithoutUpdate(i);
				ip = imp.getProcessor();
				temp= ip.getHistogram();
				for(int j=0; j<data.length; j++) {
					data[j]+=temp[j];
				}
			}
			imp.setSliceWithoutUpdate(currentSlice);
		}

		if (this.opIgnoreBlack) data[0]=0;
		if (this.opIgnoreWhite) data[data.length - 1]=0;

		// bracket the histogram to the range that holds data to make it quicker
		int minbin=-1, maxbin=-1;
		for (int i=0; i<data.length; i++){
			if (data[i]>0) maxbin = i;
		}
		for (int i=data.length-1; i>=0; i--){
			if (data[i]>0) minbin = i;
		}

		int [] data2 = new int [(maxbin-minbin)+1];
		for (int i=minbin; i<=maxbin; i++){
			data2[i-minbin]= data[i];;
		}

		if (data2.length < 2){
			threshold = 0;
			
		} else {
			threshold = IJDefault(data2);
			
			
			
		}
		

		threshold+=minbin; // add the offset of the histogram

		if (threshold < minThreshold) {
			threshold = minThreshold;
		}
		// show threshold in log window if required
		if (threshold>-1) { 
			//threshold it
			if (this.opSetThresholdInsteadOfThreshold){
				if (this.opWhiteObjblackBackground) 
					imp.getProcessor().setThreshold(threshold+1, data.length - 1, ImageProcessor.RED_LUT);//IJ.setThreshold(threshold+1, data.length - 1);
				else
					imp.getProcessor().setThreshold(0, threshold, ImageProcessor.RED_LUT);//IJ.setThreshold(0,threshold);
			}
			else{
				imp.setDisplayRange(0, Math.max(b,c)); //otherwise we can never set the threshold 
				if(this.opUseHistogram) {
					for(int j=1; j<=imp.getStackSize(); j++) {
						imp.setSlice(j);
						ip=imp.getProcessor();
						//IJ.log(""+j+": "+ data[j]);
						for( y=0;y<ye;y++) {
							for(x=0;x<xe;x++){
								if(ip.getPixel(x,y)>threshold)
									ip.putPixel(x,y,c);
								else
									ip.putPixel(x,y,b);
							}
						}
					}//threshold all of them
				}
				else{
					for( y=0;y<ye;y++) {
						for(x=0;x<xe;x++){
							if(ip.getPixel(x,y)>threshold)
								ip.putPixel(x,y,c);
							else
								ip.putPixel(x,y,b);
						}
					}
				} //just this slice
				imp.getProcessor().setThreshold(data.length - 1, data.length - 1, ImageProcessor.NO_LUT_UPDATE);
			}
		}
		imp.updateAndDraw();
		return new Object[] {threshold, imp};
	}

	public static int IJDefault(int [] data ) {
		// Original IJ implementation for compatibility.
		int level;
		int maxValue = data.length - 1;
		double result, sum1, sum2, sum3, sum4;

		int min = 0;
		while ((data[min]==0) && (min<maxValue))
			min++;
		int max = maxValue;
		while ((data[max]==0) && (max>0))
			max--;
		if (min>=max) {
			level = data.length/2;
			return level;
		}

		int movingIndex = min;

		do {
			sum1=sum2=sum3=sum4=0.0;
			for (int i=min; i<=movingIndex; i++) {
				sum1 += i*data[i];
				sum2 += data[i];
			}
			for (int i=(movingIndex+1); i<=max; i++) {
				sum3 += i*data[i];
				sum4 += data[i];
			}			
			result = (sum1/sum2 + sum3/sum4)/2.0;
			movingIndex++;
		} while ((movingIndex+1)<=result && movingIndex<max-1);

		level = (int)Math.round(result);
		return level;
	}

	public static int MaxEntropy(int [] data ) {
		
		
		int threshold=-1;
		int ih, it;
		int first_bin;
		int last_bin;
		double tot_ent;  /* total entropy */
		double max_ent;  /* max entropy */
		double ent_back; /* entropy of the background pixels at a given threshold */
		double ent_obj;  /* entropy of the object pixels at a given threshold */
		double [] norm_histo = new double[data.length]; /* normalized histogram */
		double [] P1 = new double[data.length]; /* cumulative normalized histogram */
		double [] P2 = new double[data.length];

		int total =0;
		for (ih = 0; ih < data.length; ih++ )
			total+=data[ih];

		for (ih = 0; ih < data.length; ih++ )
			norm_histo[ih] = (double)data[ih]/total;

		P1[0]=norm_histo[0];
		P2[0]=1.0-P1[0];
		for (ih = 1; ih < data.length; ih++ ){
			P1[ih]= P1[ih-1] + norm_histo[ih];
			P2[ih]= 1.0 - P1[ih];
		}

		/* Determine the first non-zero bin */
		first_bin=0;
		for (ih = 0; ih < data.length; ih++ ) {
			if ( !(Math.abs(P1[ih])<2.220446049250313E-16)) {
				first_bin = ih;
				break;
			}
		}

		/* Determine the last non-zero bin */
		last_bin=data.length - 1;
		for (ih = data.length - 1; ih >= first_bin; ih-- ) {
			if ( !(Math.abs(P2[ih])<2.220446049250313E-16)) {
				last_bin = ih;
				break;
			}
		}

		// Calculate the total entropy each gray-level
		// and find the threshold that maximizes it 
		max_ent = Double.MIN_VALUE;

		for ( it = first_bin; it <= last_bin; it++ ) {
			/* Entropy of the background pixels */
			ent_back = 0.0;
			for ( ih = 0; ih <= it; ih++ )  {
				if ( data[ih] !=0 ) {
					ent_back -= ( norm_histo[ih] / P1[it] ) * Math.log ( norm_histo[ih] / P1[it] );
				}
			}

			/* Entropy of the object pixels */
			ent_obj = 0.0;
			for ( ih = it + 1; ih < data.length; ih++ ){
				if (data[ih]!=0){
					ent_obj -= ( norm_histo[ih] / P2[it] ) * Math.log ( norm_histo[ih] / P2[it] );
				}
			}

			/* Total entropy */
			tot_ent = ent_back + ent_obj;

			// IJ.log(""+max_ent+"  "+tot_ent);
			if ( max_ent < tot_ent ) {
				max_ent = tot_ent;
				threshold = it;
			}
		}
		return threshold;
	}

}
