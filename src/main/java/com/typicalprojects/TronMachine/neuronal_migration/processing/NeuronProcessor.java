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
package com.typicalprojects.TronMachine.neuronal_migration.processing;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.OutputOption;
import com.typicalprojects.TronMachine.neuronal_migration.Wizard;
import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.Logger;
import com.typicalprojects.TronMachine.util.ResultsTable;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.RGBStackMerge;
import ij.plugin.ZProjector;

public class NeuronProcessor {

	public static final String SUPP_LBL_MASK = "mask";
	public static final String SUPP_LBL_ORIGINAL = "original";

	private final List<File> imagesToProcess;
	private Thread threadProcessor;
	private volatile boolean cancelled = false;

	public NeuronProcessor(List<File> serializedLocations, final Logger progressReporter, final Wizard wizard) {

		this.imagesToProcess = serializedLocations;

		threadProcessor = new Thread(new Runnable() {
			public void run(){
				LinkedList<File> imagesDoneProcessing = new LinkedList<File>();

				try {

					Iterator<File> itr = imagesToProcess.iterator();

					while (!cancelled && itr.hasNext()) {
						
						log(progressReporter, "Opening image....");

						PreprocessedEditableImage preProcessedImage = PreprocessedEditableImage.loadPreprocessedImage(itr.next());
						logDone(progressReporter);
						
						if (preProcessedImage == null) {
							error("Could not reload saved preprocess state for image.", wizard);
							return;
						}
						ImageContainer ic = preProcessedImage.getContainer();
						log(progressReporter, "Processing image: " + ic.getImageTitle() + "");

						Map<String, ResultsTable> tables = new HashMap<String, ResultsTable>();
						ZProjector projector = new ZProjector();
						Map<Channel, ImagePlus> chanMap = ic.getOriginals();
						for (Channel chan : ic.getRunConfig().channelMap.values()) {
							if (ic.getRunConfig().channelsToProcess.contains(chan)) {
								log(progressReporter, "Processing Channel: " + chan.name());

								Object[] chanProcessed = processChannel(chanMap.get(chan), progressReporter, chan);
								if (cancelled)
									return;
								
								ic.addImage(OutputOption.ProcessedObjectsOriginal, chan, (ImagePlus) chanProcessed[0]);
								ic.addImage(OutputOption.MaxedChannel, chan, (ImagePlus) chanProcessed[1]);
								ic.addImage(OutputOption.ProcessedObjects, chan, (ImagePlus) chanProcessed[2]);
								
								tables.put(chan.name(), (ResultsTable) chanProcessed[3]);
								
							} else {
								projector.setImage(ic.getChannelOrig(chan, true));
								projector.setMethod(ZProjector.MAX_METHOD);
								projector.doProjection();
								ic.addImage(OutputOption.MaxedChannel, chan, projector.getProjection());

							}
							
							ic.removeOriginal(chan, GUI.outputOptionContainsChannel(OutputOption.Channel, chan));

						}
						
						
						log(progressReporter, "Saving " + ic.getImageTitle() + "...");
						ObjectEditableImage oei = preProcessedImage.convertToObjectEditableImage(tables);
						
						tables = null;
						preProcessedImage.deleteSerializedVersion(ic.getSerializeDirectory());
						ic = null;
						preProcessedImage = null;
						
						ObjectEditableImage.saveObjEditableImage(oei, oei.getContainer().getSerializeDirectory());
						imagesDoneProcessing.add(oei.getContainer().getSerializeDirectory());
						logDone(progressReporter);
						
						oei = null;
						itr.remove();
						System.gc();
						
					

					}
					
					System.gc();
					progressReporter.setCurrentTask("Processing complete.");

					if (!cancelled) {
						wizard.nextState(imagesDoneProcessing);
					}
				} catch (OutOfMemoryError error) {
					JOptionPane.showMessageDialog(null, "Java run out of memory!", "Out of Memory", JOptionPane.ERROR_MESSAGE);
					wizard.cancel();
				}
			}
		});
		threadProcessor.setDaemon(true);

	}


	public void cancelProcessing() {
		this.cancelled = true;
		this.threadProcessor = null;
	}

	private void error(String msg, Wizard wizard) {
		if (this.cancelled)
			return;
		JOptionPane.showMessageDialog(null, "<html>There was an error processing images:<br><br>"+msg+"</html>", "Processing Error", JOptionPane.ERROR_MESSAGE);
		wizard.cancel();
	}

	public void run() {
		this.threadProcessor.start();
	}

	private Object[] processChannel(ImagePlus originalImg, Logger progressReporter, Channel chan) {
		System.out.println(originalImg.getStackSize());
		ImagePlus duplicate = originalImg.duplicate();
		duplicate.setTitle(duplicate.getTitle().substring(4));
		
		log(progressReporter, "Applying unsharp mask...");
		IJ.run(duplicate, "Unsharp Mask...", "radius=" + GUI.settings.processingUnsharpMaskRadius + " mask=" + GUI.settings.processingUnsharpMaskWeight + " stack");
		logDone(progressReporter);

		log(progressReporter, "Applying Gaussian Blur...");
		IJ.run(duplicate, "Gaussian Blur...", "sigma=" + GUI.settings.processingGaussianSigma + " stack");
		logDone(progressReporter);
		
		log(progressReporter, "Converting to 8-bit...");
		IJ.run(duplicate, "8-bit", "stack");
		logDone(progressReporter);
	
		log(progressReporter, "Thresholding...");
		new Thresholder(duplicate).threshold(GUI.settings.processingMinThreshold);
		logDone(progressReporter);
		
		log(progressReporter, "Setting binary options...");
		IJ.run("Options...", "iterations=1 count=1 black");
		logDone(progressReporter);
	
		log(progressReporter, "Eroding...");
		IJ.run(duplicate, "Erode", "stack");
		logDone(progressReporter);
		
		log(progressReporter, "Segmenting (watershed)...");
		IJ.run(duplicate, "Watershed", "stack");
		logDone(progressReporter);

		log(progressReporter, "Counting 3D objects...");
		Custome3DObjectCounter counter =new Custome3DObjectCounter(duplicate);

		counter.run(progressReporter, this);
		if (this.cancelled)
			return null;
		log(progressReporter, "Objects found.");

		counter.getStats();
		log(progressReporter, "Processing maps...");
		IJ.setRawThreshold(counter.getObjectMap(), 1, 65535, null);
		IJ.run(counter.getObjectMap(), "Convert to Mask", "method=Default background=Default black");

		IJ.setRawThreshold(counter.getCOMMap(), 1, 255, null);
		IJ.run(counter.getCOMMap(), "Convert to Mask", "method=Default background=Default black");

		ImagePlus originalImage8bit = originalImg.duplicate();

		IJ.run(originalImage8bit, "8-bit", "stack");
		
		logDone(progressReporter);
		log(progressReporter, "Merging, finalizing, and saving...");
		//counter.getObjectMap();  JUST REMOVED August 28th
		ImagePlus impFinal = new ImagePlus(duplicate.getTitle(),RGBStackMerge.mergeStacks(counter.getObjectMap().getImageStack(), originalImage8bit.getImageStack(), null, true));
		originalImage8bit = null;
		impFinal.setTitle(originalImg.getTitle());

		ZProjector projector = new ZProjector();
		projector.setImage(impFinal);
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.doProjection();
		impFinal = projector.getProjection();
		logDone(progressReporter);
		System.gc();
		ImagePlus maxedOriginal = maxProject(originalImg);
		ImageContainer.applyLUT(maxedOriginal, chan);
		return new Object[] {impFinal, maxedOriginal, maxProject(counter.getObjectMap()), counter.getStats()};
	}

	private void log(Logger progressReporter, String info) {
		if (this.cancelled)
			return;

		progressReporter.setCurrentTask(info);

	}
	
	public void logDone(Logger progressReporter) {
		progressReporter.setCurrentTaskComplete();
	}


	public boolean isCancelled() {
		return this.cancelled;
	}


	public static ImagePlus maxProject(ImagePlus image) {
		ImagePlus newI = image.duplicate();
		newI.setTitle(image.getTitle());
		ZProjector projector = new ZProjector();
		projector.setImage(newI);
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.doProjection();
		return projector.getProjection();
	}



}
