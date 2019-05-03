package com.typicalprojects.TronMachine.neuronal_migration.processing;



import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.Logger;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.NewImage;
import ij.plugin.RGBStackMerge;
import ij.plugin.ZProjector;
import ij.process.ByteProcessor;


public class StackOverlapper {
	
	private ImagePlus ip1 = null;
	private ImagePlus ip2 = null;
	private ImagePlus result = null;
	
	
	public StackOverlapper() {
		
	}
	
	public void setImageStacks(ImagePlus ip1, ImagePlus ip2) {
		if (!ip1.isStack() || !ip2.isStack()) {
			throw new IllegalArgumentException();
		}
		
		this.ip1 = ip1;
		this.ip2 = ip2;
		result = null;
	}
	
	private void convertInputsTo8bit() {
		IJ.run(ip1, "8-bit", "stack");
		IJ.run(ip2, "8-bit", "stack");

	}
	
	private void convertInputsTo16bit() {
		IJ.run(ip1, "16-bit", "stack");
		IJ.run(ip2, "16-bit", "stack");

	}
	
	public void thresholdInputs(int minThreshold, Logger logger, String taskName) {
		
		convertInputsTo8bit();
		new Thresholder(ip1).threshold(minThreshold, logger, taskName);
		IJ.run("Options...", "iterations=1 count=1 black");

		new Thresholder(ip2).threshold(minThreshold, logger, taskName);
		IJ.run("Options...", "iterations=1 count=1 black");

	}
	
	public void mergeGreenRedInputs(boolean convertTo8Bit) {
		
		if (convertTo8Bit) {
			convertInputsTo8bit();
		} else if (ip1.getBitDepth() != 16 || ip2.getBitDepth() != 16) {
			convertInputsTo16bit();
		}
		result = new ImagePlus("",RGBStackMerge.mergeStacks(ip2.getImageStack(), ip1.getImageStack(), null, true));
	}
	
	public ImagePlus getResult() {
		return this.result;
	}
	
	public void createOverlapPredictionStack() {
		
		convertInputsTo8bit();
		ImageStack is1 = ip1.getStack();
		ImageStack is2 = ip2.getStack();
		int stackSize = is1.getSize();

		
		int image1Min = new Thresholder(ip1).getThreshold(0);
		int image1Max = -1;
		int image2Min = new Thresholder(ip2).getThreshold(0);
		int image2Max = -1;
		
		for (int i = 1; i <= stackSize; i++) {
			ByteProcessor cp = (ByteProcessor) is1.getProcessor(i);
			for (int col = 0; col < is1.getWidth(); col++) {
				for (int row = 0; row < is1.getHeight(); row++) {
					int val = cp.get(col, row);
					if (val < image1Min) {
						cp.set(col, row, 0);
					} else if (val > image1Max) {
						image1Max = val;
					}
				}
			}
		}
		
		for (int i = 1; i <= stackSize; i++) {
			ByteProcessor cp = (ByteProcessor) is2.getProcessor(i);
			for (int col = 0; col < is2.getWidth(); col++) {
				for (int row = 0; row < is2.getHeight(); row++) {
					int val = cp.get(col, row);
					if (val < image2Min) {
						cp.set(col, row, 0);
					} else if (val > image2Max) {
						image2Max = val;
					}
				}
			}
		}
		
		double image1Range = image1Max - image1Min;
		double image2Range = image2Max - image2Min;
		
		
		ImagePlus rawOverlap = NewImage.createByteImage("Mask 0.9", is1.getWidth(), is1.getHeight(), is1.getSize(), NewImage.FILL_BLACK);
		for (int z = 1; z <= ip1.getStackSize(); z++) {
			ByteProcessor cp1 = (ByteProcessor) is1.getProcessor(z);
			ByteProcessor cp2 = (ByteProcessor) is2.getProcessor(z);
			ByteProcessor cp3 = (ByteProcessor) rawOverlap.getStack().getProcessor(z);


			for (int x = 0; x < ip1.getWidth(); x++) {
				for (int y = 0; y < ip1.getHeight(); y++) {
					int val1 = cp1.get(x, y);
					int val2 = cp2.get(x, y);
					
					if (val1> 0 && val2 > 0) {
						if (Math.abs(((cp1.get(x, y) - image1Min) / image1Range) - ((cp2.get(x, y) - image2Min) / image2Range)) < 0.9){
							cp3.set(x, y, 255);
						}
					}
					
				}
			}
		}
				
		//IJ.run(rawOverlap, "Close-", "stack");
		IJ.run(rawOverlap, "Open", "stack");
				
		this.result = rawOverlap;

		
	}
	
	public void createOverlapGradientStack() {
		
		convertInputsTo8bit();
		ImageStack is1 = ip1.getStack();
		ImageStack is2 = ip2.getStack();
		int stackSize = is1.getSize();
		
		int image1Min = new Thresholder(ip1).getThreshold(0);
		int image1Max = -1;
		int image2Min = new Thresholder(ip2).getThreshold(0);
		int image2Max = -1;
		
		for (int i = 1; i <= stackSize; i++) {
			ByteProcessor cp = (ByteProcessor) is1.getProcessor(i);
			for (int col = 0; col < is1.getWidth(); col++) {
				for (int row = 0; row < is1.getHeight(); row++) {
					int val = cp.get(col, row);
					if (val < image1Min) {
						cp.set(col, row, 0);
					} else if (val > image1Max) {
						image1Max = val;
					}
				}
			}
		}
		
		for (int i = 1; i <= stackSize; i++) {
			ByteProcessor cp = (ByteProcessor) is2.getProcessor(i);
			for (int col = 0; col < is2.getWidth(); col++) {
				for (int row = 0; row < is2.getHeight(); row++) {
					int val = cp.get(col, row);
					if (val < image2Min) {
						cp.set(col, row, 0);
					} else if (val > image2Max) {
						image2Max = val;
					}
				}
			}
		}
		
		double image1Range = image1Max - image1Min;
		double image2Range = image2Max - image2Min;

		
		ImagePlus gradientOverlap = NewImage.createByteImage("Gradient", is1.getWidth(), is1.getHeight(), is1.getSize(), NewImage.FILL_BLACK);
		for (int z = 1; z <= ip1.getStackSize(); z++) {
			ByteProcessor cp1 = (ByteProcessor) is1.getProcessor(z);
			ByteProcessor cp2 = (ByteProcessor) is2.getProcessor(z);
			ByteProcessor cp3 = (ByteProcessor) gradientOverlap.getStack().getProcessor(z);


			for (int x = 0; x < ip1.getWidth(); x++) {
				for (int y = 0; y < ip1.getHeight(); y++) {
					int val1 = cp1.get(x, y);
					int val2 = cp2.get(x, y);
					
					
					if (val1> 0 && val2 > 0) {
						
						cp3.set(x, y, (int) Math.min(255, (255 * (1.0 - Math.abs(((cp1.get(x, y) - image1Min) / image1Range) - ((cp2.get(x, y) - image2Min) / image2Range))))));
					}
					
				}
			}
		}
		
		ImageContainer.applyInfernoLUT(gradientOverlap);
		this.result = gradientOverlap;
		
	}
	
	public void maxProjectResult() {
		if (result == null || !result.isStack())
			return;
		
		ZProjector projector = new ZProjector();
		projector.setImage(result);
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.doProjection();
		result = projector.getProjection();
	}
	
}
