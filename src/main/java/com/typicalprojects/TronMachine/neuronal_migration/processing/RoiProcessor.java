package com.typicalprojects.TronMachine.neuronal_migration.processing;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.JOptionPane;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.Wizard;
import com.typicalprojects.TronMachine.util.Logger;
import com.typicalprojects.TronMachine.util.ResultsTable;


public class RoiProcessor {


	private final List<File> imagesToProcess;
	private Thread threadProcessor;
	private volatile boolean cancelled = false;

	public RoiProcessor(List<File> serializedLocations, final Logger progressReporter, final Wizard wizard) {

		this.imagesToProcess = serializedLocations;

		threadProcessor = new Thread(new Runnable() {
			public void run(){

				try {
					
					for (File file : imagesToProcess) {
						progressReporter.setCurrentTask("Loading state...");
						ROIEditableImage roiImg = ROIEditableImage.loadROIEditableImage(file);
						progressReporter.setCurrentTaskComplete();						
						
						progressReporter.setCurrentTask("Performing calculations...");
						// TODO: allow user to re-order the ROIS in the ROI list to determine binning or calculations.
						List<Analyzer.Calculation> calculationsToComplete = Arrays.asList(Analyzer.Calculation.PERCENT_MIGRATION);
						Map<String, ResultsTable> results = roiImg.processMigration(progressReporter, calculationsToComplete);
						progressReporter.setCurrentTask("Saving resources...");
						
						if (!GUI.settings.saveIntermediates) {
							roiImg.deleteSerializedVersion(roiImg.getContainer().getSerializeDirectory());
						}
						roiImg.saveROIs();
						roiImg.createAndSaveNewImages();
						roiImg.getContainer().saveResultsTables(results, true);
						
						progressReporter.setCurrentTaskComplete();
						
						if (cancelled)
							return;
					}
					

					if (!cancelled) {
						wizard.nextState();
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

	public void run() {
		this.threadProcessor.start();
	}

	public boolean isCancelled() {
		return this.cancelled;
	}

}
