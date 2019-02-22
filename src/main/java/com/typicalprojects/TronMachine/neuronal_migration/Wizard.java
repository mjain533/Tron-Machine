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
package com.typicalprojects.TronMachine.neuronal_migration;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JOptionPane;

import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlOptions;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlSelectFiles;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlInstructions.Instruction;
import com.typicalprojects.TronMachine.util.FileContainer;
import com.typicalprojects.TronMachine.util.ImagePhantom;

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
			gui.getLogPanel().setDisplayState(false);
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
		case PROCESSING_ROI:
			gui.getInstructionPanel().setInstruction(Instruction.PROCESSING_OBJECTS);
			gui.getPanelOptions().setDisplayState(PnlOptions.STATE_DISABLED, "Processing ROIs...");
			gui.getPanelDisplay().setDisplayState(false, "Processing ROIs...");
			gui.getPanelOptions().startAnalyzingROIs();
			break;
		}
	}
	
	public synchronized void cancel() {
		if (!this.status.equals(Status.SELECT_FILES)) {
			gui.getInstructionPanel().setInstruction(Instruction.CANCELING);
			this.gui.getPanelOptions().cancelNeuronProcessing();
			this.gui.getSelectFilesPanel().cancelOpening();
			setStatus(Status.SELECT_FILES);
			gui.getLogger().setCurrentTask("Run canceled.");
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
			gui.getLogPanel().setDisplayState(true);
			setStatus(Status.SELECT_SLICES);
			break;
		case SELECT_SLICES:
			setStatus(Status.PROCESSING_OBJECTS);
			
			System.gc();
			break;
		case PROCESSING_OBJECTS:
			gui.getPanelOptions().setImagesForObjSelection((List<File>) input[0]);
			setStatus(Status.SELECT_OB);
			break;
		case SELECT_OB:
			setStatus(Status.SELECT_ROI);
			break;
		case SELECT_ROI:
			System.gc();
			setStatus(Status.PROCESSING_ROI);
			break;
		case PROCESSING_ROI:
			GUI.displayMessage("Processing Complete.", "Done", this.gui.getComponent(), JOptionPane.INFORMATION_MESSAGE);
			setStatus(Status.SELECT_FILES);
			break;
		}
		
	}
	
	public void startFromObjState(List<FileContainer> fcs) {
		this.status = Status.SELECT_OB;
		List<File> files = new ArrayList<File>();
		for (FileContainer fc : fcs) {
			files.add(fc.file);
		}
		gui.getPanelOptions().setImagesForObjSelection(files);
		gui.getSelectFilesPanel().setFileList(fcs);
		gui.setMenuItemsEnabledDuringRun(false);
		gui.getInstructionPanel().setInstruction(Instruction.SELECT_OBJECTS);
		gui.getSelectFilesPanel().setDisplayState(PnlSelectFiles.STATE_FILES_RUNNING);
		gui.getLogPanel().setDisplayState(true);
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
		Date date = new Date();
		GUI.dateString = dateFormat.format(date);
		gui.getPanelDisplay().setDisplayState(false, "Image opening...");
		gui.getPanelOptions().setDisplayState(PnlOptions.STATE_DISABLED, "Image opening...");
		gui.getPanelOptions().startImageObjectSelecting();
		
	}
	
	public void startFromRoiState(List<FileContainer> fcs) {
		this.status = Status.SELECT_OB;
		List<File> files = new ArrayList<File>();
		for (FileContainer fc : fcs) {
			files.add(fc.file);
		}
		gui.getPanelOptions().setImagesForROISelection(files);
		gui.getSelectFilesPanel().setFileList(fcs);
		gui.setMenuItemsEnabledDuringRun(false);
		gui.getInstructionPanel().setInstruction(Instruction.SELECT_ROI);
		gui.getSelectFilesPanel().setDisplayState(PnlSelectFiles.STATE_FILES_RUNNING);
		gui.getLogPanel().setDisplayState(true);
		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
		Date date = new Date();
		GUI.dateString = dateFormat.format(date);
		gui.getPanelDisplay().setDisplayState(false, "Image opening...");
		gui.getPanelOptions().setDisplayState(PnlOptions.STATE_DISABLED, "Image opening...");
		gui.getPanelOptions().startImageROISelecting();
		
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
		SELECT_ROI,
		PROCESSING_ROI;
		
	}
	
}
