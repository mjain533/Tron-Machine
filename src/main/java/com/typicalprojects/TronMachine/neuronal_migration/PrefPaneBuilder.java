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
import javax.swing.JCheckBox;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;

import java.awt.Color;
import java.awt.Component;

import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.typicalprojects.TronMachine.neuronal_migration.Preferences2.SettingPage;
import com.typicalprojects.TronMachine.util.SimpleJList;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JScrollPane;
//		setPreferredSize(new Dimension(518, 384));
import javax.swing.JOptionPane;

public class PrefPaneBuilder extends JPanel {

	private static final long serialVersionUID = -6622171153906374924L;
	private JLabel lblError;

	private SettingPage settingsPage;
	private SimpleJList<String> calibrationList;
	private JPanel jpanel;
	private boolean dontadjust = false;
	private JCheckBox chkEnforceLUTs;

	/**
	 * Create the panel.
	 */
	public PrefPaneBuilder() {

		setPreferredSize(new Dimension(518, 384));

		this.settingsPage = SettingPage.ImageSettings;
		this.jpanel = this;

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

		JLabel lblCalibration = new JLabel("Calibration (if not supplied by image file):");

		JScrollPane scrollPane = new JScrollPane();

		JButton btnNewCalibration = new JButton("New Calibration");
		btnNewCalibration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog(jpanel, "Select a name for this calibration:", "New Calibration", JOptionPane.INFORMATION_MESSAGE);
				if (input == null || input.length() == 0)
					return;
				String calib = JOptionPane.showInputDialog(jpanel, "What is the name of the unit to convert pixels to (i.e. microns):", "New Calibration", JOptionPane.INFORMATION_MESSAGE);
				if (calib == null || calib.length() == 0)
					return;
				String ratio = JOptionPane.showInputDialog(jpanel, "What is the conversion? (One pixel equals _____ " + calib + ")", "New Calibration", JOptionPane.INFORMATION_MESSAGE);
				if (calib == null || calib.length() == 0)
					return;
				Double d;
				try {
					d = Double.parseDouble(ratio);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(jpanel, "The conversion you provided was not a decimal number.", "New Calibration Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (d <= 0) {
					JOptionPane.showMessageDialog(jpanel, "The conversion you provided was negative. This is invalid.", "New Calibration Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				String newCalibration = input + " (1 pixel : " + d + " " + calib + ")"; 
				calibrationList.addItem(newCalibration);
				calibrationList.setSelectedValue(newCalibration, true);

			}
		});

		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedString = calibrationList.getSelectedValue();
				if (selectedString == null)
					return;
				else if (calibrationList.getListSize() == 1) {
					JOptionPane.showMessageDialog(jpanel, "You must have at least one calibration", "Calibration Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				calibrationList.removeItem(selectedString);
				calibrationList.setSelectedIndex(0);
			}
		});
		calibrationList = new SimpleJList<String>();

		this.calibrationList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!dontadjust) {
					if (calibrationList.getSelectedIndex() == -1) {
						int index = e.getFirstIndex();
						dontadjust = true;
						if (index > -1) {
							calibrationList.setSelectedIndex(index);
						} else {
							calibrationList.setSelectedIndex(0);
						}
						dontadjust = false;
					} else if (calibrationList.getSelectedIndices().length > 1) {
						dontadjust = true;
						calibrationList.setSelectedIndex(e.getLastIndex());
						dontadjust = false;
					}

				}

			}
		});
		
		JPanel pnlColoring = new JPanel();
		pnlColoring.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlColoring.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlColoring.setBackground(new Color(211, 211, 211));
		
		JLabel lblColoring = new JLabel("Coloring");
		GroupLayout gl_pnlColoring = new GroupLayout(pnlColoring);
		gl_pnlColoring.setHorizontalGroup(
			gl_pnlColoring.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 492, Short.MAX_VALUE)
				.addGroup(gl_pnlColoring.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblColoring, GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
					.addContainerGap())
		);
		gl_pnlColoring.setVerticalGroup(
			gl_pnlColoring.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 23, Short.MAX_VALUE)
				.addComponent(lblColoring, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
		);
		pnlColoring.setLayout(gl_pnlColoring);
		
		chkEnforceLUTs = new JCheckBox("Enforce LUTs (false coloring)");

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(chkEnforceLUTs)
							.addContainerGap())
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
								.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
									.addComponent(pnlPixelConverstions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
									.addComponent(lblCalibration)
									.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE))
								.addContainerGap())
							.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
								.addComponent(btnRemove)
								.addPreferredGap(ComponentPlacement.RELATED, 290, Short.MAX_VALUE)
								.addComponent(btnNewCalibration))
							.addGroup(groupLayout.createSequentialGroup()
								.addComponent(pnlColoring, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
								.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(lblError)
							.addContainerGap(474, Short.MAX_VALUE))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(pnlPixelConverstions, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblCalibration)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnNewCalibration)
						.addComponent(btnRemove))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(pnlColoring, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(chkEnforceLUTs)
					.addPreferredGap(ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
					.addComponent(lblError)
					.addContainerGap())
		);

		scrollPane.setViewportView(calibrationList);
		setLayout(groupLayout);


	}

	public void reset(Settings settings, boolean enabled) {

		this.calibrationList.setItems(settings.calibrations);
		this.calibrationList.setSelectedIndex(settings.calibrationNumber - 1);
		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}
		this.calibrationList.setEnabled(enabled);
	}

	public SettingPage getPageDesignation() {
		return this.settingsPage;
	}

	public JPanel getRawComponent() {
		return this;
	}

	public boolean applyFields(Settings settings) {
		settings.calibrations = this.calibrationList.toList();
		settings.calibrationNumber = this.calibrationList.getSelectedIndex() + 1;
		return true;
	}

	public void displayError(String error) {
		this.lblError.setText(error);
		this.lblError.setVisible(true);
	}

	public void removeError() {
		this.lblError.setVisible(false);
	}
	
}
