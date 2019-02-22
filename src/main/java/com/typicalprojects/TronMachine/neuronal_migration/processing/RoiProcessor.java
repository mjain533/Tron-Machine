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
	private volatile Logger progressReporter;
	private volatile boolean cancelled = false;

	public RoiProcessor(List<File> serializedLocations, final Logger progress, final Wizard wizard) {
		
		this.progressReporter = progress;
		this.imagesToProcess = serializedLocations;

		threadProcessor = new Thread(new Runnable() {
			public void run(){

				try {
					
					for (File file : imagesToProcess) {
						log("Loading state...");
						ROIEditableImage roiImg = ROIEditableImage.loadROIEditableImage(file);
						logDone();					
						
						log("Performing calculations...");

						List<Analyzer.Calculation> calculationsToComplete = Arrays.asList(Analyzer.Calculation.PERCENT_MIGRATION);
						Map<String, ResultsTable> results = roiImg.processMigration(progressReporter, calculationsToComplete);
						log("Saving resources...");
						

						// save
						if (!GUI.settings.saveIntermediates) {
							roiImg.deleteSerializedVersion();
						}
						roiImg.saveROIs();
						roiImg.createAndSaveNewImages();
						roiImg.getContainer().saveResultsTables(results, true);
						
						logDone();
						
						if (cancelled)
							return;
					}
					

					if (!cancelled) {
						wizard.nextState();
					}
				} catch (OutOfMemoryError error) {
					GUI.displayMessage("Java run out of memory!", "Out of Memory", null, JOptionPane.ERROR_MESSAGE);
					wizard.cancel();
				}
			}
		});
		threadProcessor.setDaemon(true);

	}

	public void cancelProcessing() {
		this.cancelled = true;
		this.threadProcessor = null;
		this.progressReporter = null;
	}

	public void run() {
		this.threadProcessor.start();
	}

	public boolean isCancelled() {
		return this.cancelled;
	}
	
	private synchronized void log(String info) {
		if (this.cancelled)
			return;

		progressReporter.setCurrentTask(info);

	}
	
	public synchronized void logDone() {
		if (this.cancelled)
			return;
		
		progressReporter.setCurrentTaskComplete();
	}

}
