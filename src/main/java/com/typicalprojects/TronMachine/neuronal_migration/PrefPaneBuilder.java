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

import javax.swing.JPanel;

import java.awt.Dimension;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;

import java.awt.Color;
import javax.swing.border.LineBorder;



import java.awt.Font;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.JScrollPane;
import javax.swing.JList;

public class PrefPaneBuilder extends JPanel {

	/**
	 * Create the panel.
	 */
		
		
	
	private static final long serialVersionUID = -6622171153906374924L;
	private JLabel lblError;
	private JTextField minThresh;
	private JTextField txtUnsharpRadius;
	private JTextField txtUnsharpWidth;
	private JTextField txtGaussianSigma;

	/**
	 * Create the panel.
	 */
	public PrefPaneBuilder() {
		setPreferredSize(new Dimension(518, 384));


		JPanel pnlPixelConverstions = new JPanel();
		pnlPixelConverstions.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlPixelConverstions.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlPixelConverstions.setBackground(new Color(211, 211, 211));

		JLabel lblPixelConversions = new JLabel("Image Pixel Units");

		GroupLayout gl_pnlPixelConverstions = new GroupLayout(pnlPixelConverstions);
		gl_pnlPixelConverstions.setHorizontalGroup(
				gl_pnlPixelConverstions.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlPixelConverstions.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblPixelConversions, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlPixelConverstions.setVerticalGroup(
				gl_pnlPixelConverstions.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblPixelConversions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		pnlPixelConverstions.setLayout(gl_pnlPixelConverstions);

		lblError = new JLabel("Error");
		lblError.setVisible(false);
		lblError.setFont(GUI.mediumFont);
		lblError.setForeground(Color.RED);
		
		JLabel lblNewLabel = new JLabel("Calibration (if not supplied by image file):");
		
		JScrollPane scrollPane = new JScrollPane();
		
		JButton btnNewButton = new JButton("New Calibration");
		
		JButton btnRemove = new JButton("Remove");

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(pnlPixelConverstions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(lblError, Alignment.TRAILING)
								.addComponent(lblNewLabel)
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE))
							.addContainerGap())
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnRemove)
							.addPreferredGap(ComponentPlacement.RELATED, 275, Short.MAX_VALUE)
							.addComponent(btnNewButton))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(pnlPixelConverstions, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblNewLabel)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnNewButton)
						.addComponent(btnRemove))
					.addPreferredGap(ComponentPlacement.RELATED, 161, Short.MAX_VALUE)
					.addComponent(lblError)
					.addContainerGap())
		);
		
		JList<String> list = new JList<String>();
		scrollPane.setViewportView(list);
		setLayout(groupLayout);
		
		

	}
	
	
	
	public void displayError(String error) {
		this.lblError.setText(error);
		this.lblError.setVisible(true);
	}
	
	public void removeError() {
		this.lblError.setVisible(false);
	}
	
	public boolean applyFields(Settings settings) {
		int minThresh = -1;
		int unsharpRadius = -1;
		double unsharpWeight = -1;
		double gaussianSigma = -1;

		try {
			String text = this.minThresh.getText();
			minThresh = Integer.parseInt(text);
			if (minThresh < 0 || minThresh > 255)
				throw new Exception();
		} catch (Exception e) {
			displayError("Thresholding minimum must be an integer between 0 and 255.");
			return false;
		}
		
		try {
			String text = this.txtUnsharpRadius.getText();
			unsharpRadius = Integer.parseInt(text);
			if (unsharpRadius < 1 || unsharpRadius > 1000)
				throw new Exception();
		} catch (Exception e) {
			displayError("Unsharp mask radius must be an integer between 1 and 1000.");
			return false;
		}
		
		try {
			String text = this.txtUnsharpWidth.getText();
			unsharpWeight = Double.parseDouble(text);
			if (unsharpWeight < 0.1 || unsharpWeight > 0.9)
				throw new Exception();
		} catch (Exception e) {
			displayError("Unsharp mask radius must be a decimal between 0.1 and 0.9.");
			return false;
		}
		
		try {
			String text = this.txtGaussianSigma.getText();
			gaussianSigma = Double.parseDouble(text);
			if (gaussianSigma < 0.01 || gaussianSigma > 100)
				throw new Exception();
		} catch (Exception e) {
			displayError("Gaussian blur sigma must be a decimal between 0.01 and 100.");
			return false;
		}
		settings.processingMinThreshold = minThresh;
		settings.processingUnsharpMaskRadius = unsharpRadius;
		settings.processingUnsharpMaskWeight = unsharpWeight;
		settings.processingGaussianSigma = gaussianSigma;

		removeError();
		return true;

	}
}
