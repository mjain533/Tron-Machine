package com.typicalprojects.TronMachine.neuronal_migration.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class PnlInstructions {
	
	private JLabel currInstructions;
	
	private JPanel rawPanel;
	
	private final Font fontActiveInstruction = new Font("PingFang TC", Font.BOLD, 13);
	private final Color colorActiveInstruction = new Color(0, 128, 0);
	private final Font fontPassive = new Font("PingFang TC", Font.PLAIN, 13);
	private final Color colorPassive = Color.BLACK;
	
	public PnlInstructions() {
		rawPanel = new JPanel();
		rawPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Instructions", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		
		rawPanel.setLayout(new BorderLayout(0, 0));
		currInstructions = new JLabel(" ");
		currInstructions.setBorder(new EmptyBorder(2, 7, 4, 4));
		rawPanel.add(currInstructions, BorderLayout.CENTER);
		
	}
	
	public void setInstruction(Instruction instruction) {
		currInstructions.setText("<html>" + instruction.getMessage() + "</html>");
		if (instruction.isActive) {
			currInstructions.setFont(fontActiveInstruction);
			currInstructions.setForeground(colorActiveInstruction);
		} else {
			currInstructions.setFont(fontPassive);
			currInstructions.setForeground(colorPassive);
		}
	}
	
	public JPanel getRawPanel() {
		return this.rawPanel;
	}
	
	public enum Instruction {
		SETUP("Setting up, please wait...", false),
		SELECT_FILE("Please select files, then hit Go.", true),
		CANCELING("Canceling run. Please wait...", false),
		SELECT_SLICES("Please wait for images to open at right. Then, select slices of interest and hit Next.", true),
		SAVING_INTERMEDIATES("Saving intermediate files. Please wait...", false),
		PROCESSING_OBJECTS("Processing images. Please wait...", false),
		SELECT_OBJECTS("Please wait for images to open at right. Then, select objects of interest and hit Next.", true),
		SELECT_ROI("Please wait for images to open at right. Then, select ROIs and hit Next.", true);
;
		
		private String message;
		private boolean isActive;
		
		private Instruction(String message, boolean isActive) {
			this.message = message;
			this.isActive = isActive;
		}
		
		public String getMessage() {
			return this.message;
		}
		
		public boolean isActive() {
			return this.isActive;
		}
		
	}
	
}
