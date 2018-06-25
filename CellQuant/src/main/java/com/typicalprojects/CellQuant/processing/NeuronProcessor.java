package com.typicalprojects.CellQuant.processing;

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
import com.typicalprojects.CellQuant.util.ProspectiveImage;
import com.typicalprojects.CellQuant.util.SynchronizedProgress;
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.RGBStackMerge;
import ij.plugin.ZProjector;

public class NeuronProcessor {
	
	private final List<ProspectiveImage> imagesDoneProcessing;
	private final List<ProspectiveImage> imagesToProcess;
	private final Set<Channel> channelsToProcess;
	private Thread threadProcessor;
	private volatile boolean cancelled = false;
	
	public NeuronProcessor(List<ProspectiveImage> images, final GUI gui, final SynchronizedProgress progressReporter, final Wizard wizard, Set<Channel> chansToProcess) {
		
		this.imagesToProcess = images;
		this.imagesDoneProcessing = new LinkedList<ProspectiveImage>();
		this.channelsToProcess = chansToProcess;
		
		threadProcessor = new Thread(new Runnable() {
			public void run(){
				
				Iterator<ProspectiveImage> itr = imagesToProcess.iterator();
				
				while (!cancelled && itr.hasNext()) {
					ProspectiveImage pi = itr.next();
					String error = pi.open();
					if (error != null) {
						error(error, wizard);
						return;
					}
					ImageContainer container = pi.getIC();
					progressReporter.setProgress("Processing " + pi.getTitle() + "...", -1, -1);
					
					
					List<Channel> channels = new LinkedList<Channel>();
					List<ImagePlus> images = new LinkedList<ImagePlus>();
					Map<Channel, ResultsTable> tables = new HashMap<Channel, ResultsTable>();
					ZProjector projector = new ZProjector();
					for (Channel chan : container.getChannels()) {
						channels.add(chan);

						if (channelsToProcess.contains(chan)) {

							Object[] chanProcessed = processChannel(container.getImageChannel(chan, true), progressReporter);
							images.add((ImagePlus) chanProcessed[0]);							
							tables.put(chan, (ResultsTable) chanProcessed[1]);
						} else {
							projector.setImage(container.getImageChannel(chan, true));
							projector.setMethod(ZProjector.MAX_METHOD);
							projector.doProjection();
							images.add(projector.getProjection());

						}
					}
					
					progressReporter.setProgress("Saving...", -1, -1);
					ImageContainer newContainer = new ImageContainer(channels, images, container.getTotalImageTitle(), container.getImgFile(), container.getSaveDir(), false, container.getCalibration());
					newContainer.save(GUI.dateString);
					newContainer.saveResultsTable(tables, GUI.dateString, false);
					imagesDoneProcessing.add(new ProspectiveImage(pi.getImageFile(), newContainer.getTotalImageTitle(), gui, newContainer.getSaveDir(), true, newContainer.getCalibration()));
					
					newContainer = null;
					container = null;
					pi = null;
										
					
					itr.remove();
					System.gc();
					progressReporter.setProgress("Success.", -1, -1);
					
				}
				
				if (!cancelled) {
					wizard.nextState(imagesDoneProcessing);
				}
				System.gc();
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
	
	private Object[] processChannel(ImagePlus channelDup, SynchronizedProgress progressReporter) {
		
		ImagePlus originalDup = channelDup.duplicate();
		
		IJ.run(channelDup, "Unsharp Mask...", "radius=20 mask=0.80 stack");
		progressReporter.setProgress("Applying unsharp mask...", -1, -1);
		
		IJ.run(channelDup, "Gaussian Blur...", "sigma=0.50 stack");
		progressReporter.setProgress("Applying Gaussian Blur...", -1, -1);

		IJ.run(channelDup, "8-bit", "stack");
		progressReporter.setProgress("Converting to 8-bit...", -1, -1);
		
		new Thresholder(channelDup).threshold();
		progressReporter.setProgress("Thresholding...", -1, -1);
		
		IJ.run("Options...", "iterations=1 count=1 black");
		progressReporter.setProgress("Setting binary options...", -1, -1);
		
		IJ.run(channelDup, "Erode", "stack");
		progressReporter.setProgress("Eroding...", -1, -1);
		
		IJ.run(channelDup, "Watershed", "stack");
		progressReporter.setProgress("Segmenting (watershed)...", -1, -1);
		
		Custome3DObjectCounter counter =new Custome3DObjectCounter(channelDup);
		progressReporter.setProgress("Counting 3D objects...", -1, -1);

		counter.run(progressReporter);
		counter.getStats();

		progressReporter.setProgress("Processing object map...", -1, -1);

		IJ.setRawThreshold(counter.getObjectMap(), 1, 255, null);
		IJ.run(counter.getObjectMap(), "Convert to Mask", "method=Default background=Default black");
		
		progressReporter.setProgress("Processing center of mass map map...", -1, -1);
		
		IJ.setRawThreshold(counter.getCOMMap(), 1, 255, null);
		IJ.run(counter.getCOMMap(), "Convert to Mask", "method=Default background=Default black");

		progressReporter.setProgress("Converting original to 8-bit...", -1, -1);

		IJ.run(originalDup, "8-bit", "stack");
		
		progressReporter.setProgress("Merging...", -1, -1);

		ImagePlus /*firstTwo*/impFinal = new ImagePlus(channelDup.getTitle(),RGBStackMerge.mergeStacks(counter.getObjectMap().getImageStack(), originalDup.getImageStack(), null, true));
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
		impFinal.setTitle(channelDup.getTitle());
		
		ZProjector projector = new ZProjector();
		projector.setImage(impFinal);
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.doProjection();
		impFinal = projector.getProjection();
		
		progressReporter.setProgress("Done.", -1,-1);
		
		return new Object[] {impFinal, counter.getStats()};
	}
	
}
