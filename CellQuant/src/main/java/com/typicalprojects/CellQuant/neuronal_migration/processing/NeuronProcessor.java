package com.typicalprojects.CellQuant.neuronal_migration.processing;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JOptionPane;

import com.typicalprojects.CellQuant.neuronal_migration.GUI;
import com.typicalprojects.CellQuant.neuronal_migration.Wizard;
import com.typicalprojects.CellQuant.util.ImageContainer;
import com.typicalprojects.CellQuant.util.ImagePhantom;
import com.typicalprojects.CellQuant.util.SynchronizedProgress;
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

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
	private final Set<Channel> channelsToProcess;
	private Thread threadProcessor;
	private volatile boolean cancelled = false;

	public NeuronProcessor(List<ImagePhantom> images, final GUI gui, final SynchronizedProgress progressReporter, final Wizard wizard, Set<Channel> chansToProcess) {

		this.imagesToProcess = images;
		this.imagesDoneProcessing = new LinkedList<ImagePhantom>();
		this.channelsToProcess = chansToProcess;

		threadProcessor = new Thread(new Runnable() {
			public void run(){
				try {

					Iterator<ImagePhantom> itr = imagesToProcess.iterator();

					while (!cancelled && itr.hasNext()) {
						ImagePhantom pi = itr.next();
						String error = pi.open(GUI.channelMap, GUI.outputLocation, GUI.dateString, true);
						if (error != null) {
							error(error, wizard);
							return;
						}
						ImageContainer container = pi.getIC();
						log(progressReporter, "Processing " + pi.getTitle() + "...");


						List<Channel> channels = new LinkedList<Channel>();
						List<ImagePlus> images = new LinkedList<ImagePlus>();
						Map<String, ImagePlus> supp = new HashMap<String, ImagePlus>();
						Map<Channel, ResultsTable> tables = new HashMap<Channel, ResultsTable>();
						ZProjector projector = new ZProjector();
						for (Channel chan : container.getChannels()) {
							channels.add(chan);

							if (channelsToProcess.contains(chan)) {
								ImagePlus duplicate = container.getImageChannel(chan, true);
								Object[] chanProcessed = processChannel(duplicate, progressReporter);
								if (cancelled)
									return;

								images.add((ImagePlus) chanProcessed[0]);
								supp.put(duplicate.getTitle() + " " + SUPP_LBL_ORIGINAL, (ImagePlus)chanProcessed[1]);
								supp.put(duplicate.getTitle() + " " + SUPP_LBL_MASK, (ImagePlus) chanProcessed[2]);

								tables.put(chan, (ResultsTable) chanProcessed[3]);
							} else {
								projector.setImage(container.getImageChannel(chan, true));
								projector.setMethod(ZProjector.MAX_METHOD);
								projector.doProjection();
								images.add(projector.getProjection());

							}
						}

						log(progressReporter, "Saving...");

						ImageContainer newContainer = new ImageContainer(channels, images, supp, container.getTotalImageTitle(), container.getImgFile(), container.getCalibration(), GUI.outputLocation, GUI.dateString);
						newContainer.save(true);
						newContainer.saveResultsTable(tables, GUI.dateString, false);
						imagesDoneProcessing.add(new ImagePhantom(pi.getImageFile(), newContainer.getTotalImageTitle(), gui.getProgressReporter(), newContainer.getCalibration()));

						newContainer = null;
						container = null;
						pi = null;


						itr.remove();
						System.gc();
						log(progressReporter, "Success.");

					}

					if (!cancelled) {
						wizard.nextState(imagesDoneProcessing);
					}
					System.gc();

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

	private Object[] processChannel(ImagePlus originalImg, SynchronizedProgress progressReporter) {

		ImagePlus duplicate = originalImg.duplicate();
		duplicate.setTitle(duplicate.getTitle().substring(4));

		IJ.run(duplicate, "Unsharp Mask...", "radius=20 mask=0.80 stack");
		log(progressReporter, "Applying unsharp mask...");

		IJ.run(duplicate, "Gaussian Blur...", "sigma=0.50 stack");
		log(progressReporter, "Applying Gaussian Blur...");

		IJ.run(duplicate, "8-bit", "stack");
		log(progressReporter, "Converting to 8-bit...");

		new Thresholder(duplicate).threshold();
		log(progressReporter, "Thresholding...");

		IJ.run("Options...", "iterations=1 count=1 black");
		log(progressReporter, "Setting binary options...");

		IJ.run(duplicate, "Erode", "stack");
		log(progressReporter, "Eroding...");

		IJ.run(duplicate, "Watershed", "stack");
		log(progressReporter, "Segmenting (watershed)...");

		Custome3DObjectCounter counter =new Custome3DObjectCounter(duplicate);
		log(progressReporter, "Counting 3D objects...");

		counter.run(progressReporter, this);
		if (this.cancelled)
			return null;
		counter.getStats();

		log(progressReporter, "Processing object map...");
		IJ.setRawThreshold(counter.getObjectMap(), 1, 65535, null);
		IJ.run(counter.getObjectMap(), "Convert to Mask", "method=Default background=Default black");


		log(progressReporter, "Processing center of mass map map...");

		IJ.setRawThreshold(counter.getCOMMap(), 1, 255, null);
		IJ.run(counter.getCOMMap(), "Convert to Mask", "method=Default background=Default black");

		log(progressReporter, "Converting original to 8-bit...");

		IJ.run(originalImg, "8-bit", "stack");

		log(progressReporter, "Merging...");
		counter.getObjectMap();
		ImagePlus /*firstTwo*/impFinal = new ImagePlus(duplicate.getTitle(),RGBStackMerge.mergeStacks(counter.getObjectMap().getImageStack(), originalImg.getImageStack(), null, true));
		//ImageStack stack = new ImageStack(firstTwo.getWidth(), firstTwo.getHeight());

		/*for (int i = 1; i <= firstTwo.getNSlices(); i++) {
			ImagePlus combine = new ImagePlus("slice", firstTwo.getStack().getProcessor(i).duplicate());
			ImagePlus add = new ImagePlus("addition", counter.getCOMMap().getStack().getProcessor(i).duplicate());
			ImageRoi roi = new ImageRoi(0, 0, add.getProcessor());
			roi.setZeroTransparent(true);
			roi.setOpacity(1.0);
			combine.getProcessor().drawOverlay(new Overlay(roi));
			combine.updateImage();
			stack.addSlice(combine.getProcessor());
		}*/
		//ImagePlus impFinal = new ImagePlus();
		//impFinal.setStack(stack);
		impFinal.setTitle(originalImg.getTitle());

		ZProjector projector = new ZProjector();
		projector.setImage(impFinal);
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.doProjection();
		impFinal = projector.getProjection();
		log(progressReporter, "Done.");
		System.gc();
		return new Object[] {impFinal, maxProject(originalImg), maxProject(counter.getObjectMap()), counter.getStats()};
	}

	private void log(SynchronizedProgress progressReporter, String info) {
		if (this.cancelled)
			return;

		progressReporter.setProgress(info, -1, -1);

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
