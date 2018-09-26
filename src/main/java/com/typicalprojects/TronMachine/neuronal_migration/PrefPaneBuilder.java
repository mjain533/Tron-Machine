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

public class PrefPaneBuilder extends JPanel {

	/**
	 * Create the panel.
	 */

	private static final long serialVersionUID = -6622171153906374924L;


	/**
	 * Create the panel.
	 */
	public PrefPaneBuilder() {
		setPreferredSize(new Dimension(518, 384));


		JPanel pnlPixelConverstions = new JPanel();
		pnlPixelConverstions.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlPixelConverstions.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlPixelConverstions.setBackground(new Color(211, 211, 211));

		JLabel lblPixelConversions = new JLabel("Hard Reset");

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
		
		JLabel lblbyClickingreset = new JLabel("<html>By clicking 'Reset to Default' below, you will reset all settings to their default values. This action cannot be undone. This will affect your Channel configuration and you will likely need to re-map channels following a reset. In addition, you will need to re-select an output folder; you will not be able to run the program until an output location has been set.</html>");
		
		JButton btnNewButton = new JButton("Reset to Defaults");

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addContainerGap()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblbyClickingreset, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
								.addComponent(pnlPixelConverstions, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(183)
							.addComponent(btnNewButton)))
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(pnlPixelConverstions, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblbyClickingreset)
					.addGap(12)
					.addComponent(btnNewButton)
					.addContainerGap(220, Short.MAX_VALUE))
		);
		setLayout(groupLayout);
		
		

	}
	

}
