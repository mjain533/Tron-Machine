package com.typicalprojects.CellQuant.neuronal_migration;

import javax.swing.JPanel;
import javax.swing.JSpinner;

import java.awt.Dimension;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JLabel;
import java.awt.Color;
import javax.swing.border.LineBorder;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JTextField;
import javax.swing.JButton;

public class asdf extends JPanel {

	/**
	 * Create the panel.
	 */
		
		
	
	private static final long serialVersionUID = -6622171153906374924L;
	private JLabel lblError;
	private JCheckBox chkDrawGreen;
	private JCheckBox chkDrawRed;
	private JCheckBox chkDrawBlue;
	private JCheckBox chkDrawWhite;
	private JSpinner spnNumBins;
	private JCheckBox chkDrawBinLabels;
	private JCheckBox chkCalcBins;

	/**
	 * Create the panel.
	 */
	public asdf() {
		setPreferredSize(new Dimension(518, 384));

		JPanel pnlBinOptions = new JPanel();
		pnlBinOptions.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlBinOptions.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlBinOptions.setBackground(new Color(211, 211, 211));
		
		JLabel lblBinSettings = new JLabel("Bin Settings");
		
		GroupLayout gl_pnlBinOptions = new GroupLayout(pnlBinOptions);
		gl_pnlBinOptions.setHorizontalGroup(
			gl_pnlBinOptions.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlBinOptions.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblBinSettings, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_pnlBinOptions.setVerticalGroup(
			gl_pnlBinOptions.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblBinSettings, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
		);
		pnlBinOptions.setLayout(gl_pnlBinOptions);
		
		chkCalcBins = new JCheckBox("Calculate Bins");
		chkCalcBins.setFocusable(false);
		
		JLabel lblNumberOfBins = new JLabel("Number of Bins:");
		
		spnNumBins = new JSpinner();
		((DefaultEditor) spnNumBins.getEditor()).getTextField().setEditable(false);
		spnNumBins.setBackground(Color.WHITE);
		spnNumBins.setFocusable(false);
		
		JPanel pnlBinOutput = new JPanel();
		pnlBinOutput.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlBinOutput.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlBinOutput.setBackground(new Color(211, 211, 211));
		
		JLabel lblBinOutput = new JLabel("Binning Output");
		GroupLayout gl_pnlBinOutput = new GroupLayout(pnlBinOutput);
		gl_pnlBinOutput.setHorizontalGroup(
			gl_pnlBinOutput.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 492, Short.MAX_VALUE)
				.addGroup(gl_pnlBinOutput.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblBinOutput, GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_pnlBinOutput.setVerticalGroup(
			gl_pnlBinOutput.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 23, Short.MAX_VALUE)
				.addComponent(lblBinOutput, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
		);
		pnlBinOutput.setLayout(gl_pnlBinOutput);
		
		JCheckBox chkExcludeOutsider = new JCheckBox("Exclude points outside of bin region");
		chkExcludeOutsider.setFocusable(false);
		
		chkDrawBinLabels = new JCheckBox("Draw bin labels");
		chkDrawBinLabels.setFocusable(false);
		
		JLabel lblChanDrawBin = new JLabel("Channels to Draw Bins:");
		
		chkDrawGreen = new JCheckBox("Green");
		chkDrawGreen.setFocusable(false);
		
		chkDrawRed = new JCheckBox("Red");
		chkDrawRed.setFocusable(false);
		
		chkDrawBlue = new JCheckBox("Blue");
		chkDrawBlue.setFocusable(false);
		
		chkDrawWhite = new JCheckBox("White");
		chkDrawWhite.setFocusable(false);
		
		lblError = new JLabel("Error:");
		lblError.setFont(GUI.mediumFont);
		lblError.setForeground(Color.RED);
		

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addComponent(chkDrawBinLabels)
						.addComponent(chkCalcBins)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblNumberOfBins)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(spnNumBins, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(chkDrawGreen)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(chkDrawRed)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(chkDrawBlue)
							.addPreferredGap(ComponentPlacement.UNRELATED)
							.addComponent(chkDrawWhite))
						.addComponent(chkExcludeOutsider)
						.addComponent(pnlBinOptions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
						.addComponent(lblError)
						.addComponent(pnlBinOutput, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblChanDrawBin))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(pnlBinOptions, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(chkCalcBins)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(chkDrawBinLabels)
					.addGap(10)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNumberOfBins)
						.addComponent(spnNumBins, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(12)
					.addComponent(pnlBinOutput, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(chkExcludeOutsider)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblChanDrawBin)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(chkDrawGreen)
						.addComponent(chkDrawRed)
						.addComponent(chkDrawBlue)
						.addComponent(chkDrawWhite))
					.addPreferredGap(ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
					.addComponent(lblError)
					.addContainerGap())
		);
		setLayout(groupLayout);
		
		

	}
	
	public void displayError(String error) {
		this.lblError.setText(error);
		this.lblError.setVisible(true);
	}
	
	public void removeError() {
		this.lblError.setVisible(false);
	}
}
