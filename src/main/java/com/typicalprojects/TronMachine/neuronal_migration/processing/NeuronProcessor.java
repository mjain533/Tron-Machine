package com.typicalprojects.TronMachine.neuronal_migration.processing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.Wizard;
import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.ImagePhantom;
import com.typicalprojects.TronMachine.util.Logger;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;
import com.typicalprojects.TronMachine.util.ImageContainer.ImageTag;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.RGBStackMerge;
import ij.plugin.ZProjector;

public class NeuronProcessor {

	public static final String SUPP_LBL_MASK = "mask";
	public static final String SUPP_LBL_ORIGINAL = "original";

	private final List<ImagePhantom> imagesDoneProcessing;
	private final List<ImagePhantom> imagesToProcess;
	private final List<Channel> channelsToProcess;
	private Thread threadProcessor;
	private volatile boolean cancelled = false;

	public NeuronProcessor(List<ImagePhantom> images, final Logger progressReporter, final Wizard wizard, List<Channel> chansToProcess) {

		this.imagesToProcess = images;
		this.imagesDoneProcessing = new LinkedList<ImagePhantom>();
		this.channelsToProcess = chansToProcess;

		threadProcessor = new Thread(new Runnable() {
			public void run(){
				try {

					Iterator<ImagePhantom> itr = imagesToProcess.iterator();

					while (!cancelled && itr.hasNext()) {
						ImagePhantom pi = itr.next();
						String error = pi.open(GUI.settings.channelMap, GUI.settings.outputLocation, GUI.dateString, new ArrayList<ImageTag>(Arrays.asList(ImageTag.OrigTiff)));
						if (error != null) {
							error(error, wizard);
							return;
						}
						ImageContainer container = pi.getIC();
						log(progressReporter, "Processing image: " + pi.getTitle() + "");



						Map<String, ResultsTable> tables = new HashMap<String, ResultsTable>();
						ZProjector projector = new ZProjector();
						Map<Channel, ImagePlus> chanMap = container.getOriginals();
						for (Channel chan : GUI.settings.getChannels()) {
							if (channelsToProcess.contains(chan)) {
								log(progressReporter, "Processing " + chan.name() + " channel...");
								Object[] chanProcessed = processChannel(chanMap.get(chan), progressReporter);
								if (cancelled)
									return;

								container.saveSupplementalImage(ImageTag.ObjCountOrigMaskMerge, (ImagePlus) chanProcessed[0], chan);
								container.saveSupplementalImage(ImageTag.MaxProjected, (ImagePlus) chanProcessed[1], chan);
								container.saveSupplementalImage(ImageTag.ObjCountMask, (ImagePlus) chanProcessed[2], chan);								
								
								tables.put(chan.name(), (ResultsTable) chanProcessed[3]);
							} else {
								projector.setImage(container.getChannelOrig(chan, true));
								projector.setMethod(ZProjector.MAX_METHOD);
								projector.doProjection();
								container.saveSupplementalImage(ImageTag.MaxProjected, projector.getProjection(), chan);

							}
						}
						

						log(progressReporter, "Saving " + pi.getTitle() + "...");
						container.saveResultsTables(tables, false);
						imagesDoneProcessing.add(new ImagePhantom(pi.getImageFile(), container.getImageTitle(), progressReporter, container.getCalibration()));
						logDone(progressReporter);
						
						container = null;
						pi = null;

						itr.remove();
						System.gc();

					}

					if (!cancelled) {
						wizard.nextState(imagesDoneProcessing);
					}
					System.gc();
					progressReporter.setCurrentTask("Processing complete.");
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

	private Object[] processChannel(ImagePlus originalImg, Logger progressReporter) {

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
		log(progressReporter, "Objects founds.");

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
		return new Object[] {impFinal, maxProject(originalImg), maxProject(counter.getObjectMap()), counter.getStats()};
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
