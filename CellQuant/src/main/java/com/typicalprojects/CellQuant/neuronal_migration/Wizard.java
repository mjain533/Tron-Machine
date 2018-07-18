package com.typicalprojects.CellQuant.neuronal_migration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import com.typicalprojects.CellQuant.neuronal_migration.panels.PnlOptions;
import com.typicalprojects.CellQuant.neuronal_migration.panels.PnlSelectFiles;
import com.typicalprojects.CellQuant.neuronal_migration.panels.PnlInstructions.Instruction;
import com.typicalprojects.CellQuant.util.ImagePhantom;

public class Wizard {
	
	private volatile Status status;
	private volatile GUI gui;
	
	public Wizard(GUI gui) {
		this.gui = gui;
		setStatus(Status.SELECT_FILES);
	}
	
	private void setStatus(Status status) {
		this.status = status;
		
		switch(status) {
		case SETUP:
			gui.getInstructionPanel().setInstruction(Instruction.SETUP);
			break;
		case SELECT_FILES:
			this.gui.setMenuItemsEnabledDuringRun(true);
			this.gui.setBrightnessAdjustOptionEnabled(false);
			this.gui.getPanelOptions().cancelNeuronProcessing();
			DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
			Date date = new Date();
			GUI.dateString = dateFormat.format(date);
			gui.getSelectFilesPanel().setDisplayState(PnlSelectFiles.STATE_NO_FILE_ADDED);
			gui.getPanelDisplay().setDisplayState(false, null);
			gui.getPanelOptions().setDisplayState(PnlOptions.STATE_DISABLED, null);
			gui.getInstructionPanel().setInstruction(Instruction.SELECT_FILE); // THIS LAST
			break;
		case SELECT_SLICES:
			this.gui.setMenuItemsEnabledDuringRun(false);
			gui.getInstructionPanel().setInstruction(Instruction.SELECT_SLICES);
			gui.getSelectFilesPanel().setDisplayState(PnlSelectFiles.STATE_FILES_RUNNING);
			gui.getPanelDisplay().setDisplayState(false, "Image opening...");
			gui.getPanelOptions().setDisplayState(PnlOptions.STATE_DISABLED, "Image opening...");
			gui.getPanelOptions().startSliceSelecting();
			break;
		case PROCESSING_OBJECTS:
			gui.getInstructionPanel().setInstruction(Instruction.PROCESSING_OBJECTS);
			gui.getPanelOptions().setDisplayState(PnlOptions.STATE_DISABLED, "Processing images...");
			gui.getPanelDisplay().setDisplayState(false, "Processing images...");

			gui.getPanelOptions().startProcessingImageObjects();
			break;
		case SELECT_OB:
			gui.getInstructionPanel().setInstruction(Instruction.SELECT_OBJECTS);
			gui.getPanelDisplay().setDisplayState(false, "Image opening...");
			gui.getPanelOptions().setDisplayState(PnlOptions.STATE_DISABLED, "Image opening...");
			gui.getPanelOptions().startImageObjectSelecting();
			//gui.getPanelDisplay().setDisplayState(PnlDisplay.s, disabledMsg);
			break;
		case SELECT_ROI:
			gui.setBrightnessAdjustOptionEnabled(true);
			gui.getInstructionPanel().setInstruction(Instruction.SELECT_ROI);
			gui.getPanelDisplay().setDisplayState(false, "Image opening...");
			gui.getPanelOptions().setDisplayState(PnlOptions.STATE_DISABLED, "Image opening...");
			gui.getPanelOptions().startImageROISelecting();
			break;
		}
	}
	
	public synchronized void cancel() {
		if (!this.status.equals(Status.SELECT_FILES)) {
			gui.getInstructionPanel().setInstruction(Instruction.CANCELING);
			this.gui.getPanelOptions().cancelNeuronProcessing();
			this.gui.getSelectFilesPanel().cancelOpening();
			setStatus(Status.SELECT_FILES);
			gui.log("Run canceled.");
		} else {
			this.gui.getPanelOptions().cancelNeuronProcessing();
			this.gui.getSelectFilesPanel().cancelOpening();
			setStatus(Status.SELECT_FILES);
		}

	}
	
	@SuppressWarnings("unchecked")
	public synchronized void nextState(Object... input) {
		
		switch (this.status) {
		case SETUP:
			setStatus(Status.SELECT_FILES);
			break;
		case SELECT_FILES:
			this.gui.getPanelOptions().setImagesForSliceSelection((List<ImagePhantom>) input[0]);
			setStatus(Status.SELECT_SLICES);
			break;
		case SELECT_SLICES:
			setStatus(Status.PROCESSING_OBJECTS);
			
			System.gc();
			break;
		case PROCESSING_OBJECTS:
			gui.getPanelOptions().setImagesForObjSelection((List<ImagePhantom>) input[0]);
			setStatus(Status.SELECT_OB);
			break;
		case SELECT_OB:
			setStatus(Status.SELECT_ROI);
			break;
		case SELECT_ROI:
			JOptionPane.showMessageDialog(this.gui.getComponent(), "Processing Complete.", "Done", JOptionPane.INFORMATION_MESSAGE);
			setStatus(Status.SELECT_FILES);
			break;
		}
		
	}
	
	public Status getStatus() {
		return this.status;
	}
	
	public enum Status {
		SETUP,
		SELECT_FILES,
		SELECT_SLICES,
		PROCESSING_OBJECTS,
		SELECT_OB,
		SELECT_ROI;
		
	}
	
}
