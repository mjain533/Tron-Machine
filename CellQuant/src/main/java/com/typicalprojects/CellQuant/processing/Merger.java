package com.typicalprojects.CellQuant.processing;

import java.awt.Color;
import java.awt.image.IndexColorModel;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;
import ij.process.LUT;

public class Merger {

	//
	private boolean ignoreLuts = true;



	/** Combines up to seven grayscale stacks into one RGB or composite stack. */
	public ImagePlus mergeStacks(ImagePlus[] imagesWithStacks) {
		
		ImagePlus imp2 = mergeHyperstacks(imagesWithStacks, true);
		return imp2;

	}
	public ImagePlus mergeHyperstacks(ImagePlus[] images, boolean keep) {
		int n = images.length;
		int channels = 0;
		for (int i=0; i<n; i++) {
			if (images[i]!=null) channels++;
		}
		if (channels<2) return null;
		ImagePlus[] images2 = new ImagePlus[channels];
		Color[] defaultColors = {Color.red,Color.green,Color.blue,Color.white,Color.cyan,Color.magenta,Color.yellow};
		Color[] colors = new Color[channels];
		int j = 0;
		for (int i=0; i<n; i++) {
			if (images[i]!=null) {
				images2[j] = images[i];
				if (i<defaultColors.length)
					colors[j] = defaultColors[i];
				j++;
			}
		}
		images = images2;
		ImageStack[] stacks = new ImageStack[channels];
		for (int i=0; i<channels; i++) {
			ImagePlus imp2 = images[i];
			if (isDuplicate(i,images))
				imp2 = imp2.duplicate();
			stacks[i] = imp2.getStack();
		}
		ImagePlus imp = images[0];
		int w = imp.getWidth();
		int h = imp.getHeight();
		int slices = imp.getNSlices();
		int frames = imp.getNFrames();
		ImageStack stack2 = new ImageStack(w, h);
		//IJ.log("mergeHyperstacks: "+w+" "+h+" "+channels+" "+slices+" "+frames);
		int[] index = new int[channels];
		for (int t=0; t<frames; t++) {
			for (int z=0; z<slices; z++) {
				for (int c=0; c<channels; c++) {
					ImageProcessor ip = stacks[c].getProcessor(index[c]+1);
					if (keep)
						ip = ip.duplicate();
					stack2.addSlice(null, ip);
					if (keep)
						index[c]++;
					else
						stacks[c].deleteSlice(1);
				}
			}
		}
		String title = imp.getTitle();
		if (title.startsWith("C1-"))
			title = title.substring(3);
		else
			title = frames>1?"Merged":"Composite";
			ImagePlus imp2 = new ImagePlus(title, stack2);
			imp2.setDimensions(channels, slices, frames);
			imp2 = new CompositeImage(imp2, IJ.COMPOSITE);
			boolean allGrayLuts = true;
			for (int c=0; c<channels; c++) {
				if (images[c].getProcessor().isColorLut()) {
					allGrayLuts = false;
					break;
				}
			}
			for (int c=0; c<channels; c++) {
				ImageProcessor ip = images[c].getProcessor();
				IndexColorModel cm = (IndexColorModel)ip.getColorModel();
				LUT lut = null;
				if (c<colors.length && colors[c]!=null && (ignoreLuts||allGrayLuts)) {
					lut = LUT.createLutFromColor(colors[c]);
					lut.min = ip.getMin();
					lut.max = ip.getMax();
				} else
					lut =  new LUT(cm, ip.getMin(), ip.getMax());
				((CompositeImage)imp2).setChannelLut(lut, c+1);
			}
			imp2.setOpenAsHyperStack(true);
			return imp2;
	}
	private boolean isDuplicate(int index, ImagePlus[] images) {
		for (int i=0; i<index; i++) {
			if (images[index]==images[i])
				return true;
		}
		return false;
	}

	/** Deprecated; replaced by mergeChannels(). */
	public ImagePlus createComposite(int w, int h, int d, ImageStack[] stacks, boolean keep) {
		ImagePlus[] images = new ImagePlus[stacks.length];
		for (int i=0; i<stacks.length; i++)
			images[i] = new ImagePlus(""+i, stacks[i]);
		return mergeHyperstacks(images, keep);
	}




}
