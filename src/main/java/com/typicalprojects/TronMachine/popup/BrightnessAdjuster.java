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
package com.typicalprojects.TronMachine.popup;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JButton;

public class BrightnessAdjuster extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8425926601484264232L;
	private JPanel contentPane;
	private JTextField txtMin;
	private JTextField txtMax;
	private JSlider sliderMin;
	private JSlider sliderMax;
	private boolean changing = false;
	
	private BrightnessChangeReceiver receiver;

	/**
	 * Launch the application.
	 */
	/*public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BrightnessAdjuster frame = new BrightnessAdjuster();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/

	/**
	 * Create the frame.
	 */
	public BrightnessAdjuster(BrightnessChangeReceiver brightReceiver) {
		this.receiver = brightReceiver;
		setResizable(false);
		setTitle("Brightness Adjuster");
		setBounds(100, 100, 315, 220);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));
		
		JPanel mainPanel = new JPanel();
		contentPane.add(mainPanel, BorderLayout.CENTER);
		setAlwaysOnTop(true);
		
		JLabel lblInstructions = new JLabel("Adjust Minimum and Maximum Brightness");
		lblInstructions.setFont(new Font("PingFang TC", Font.BOLD, 14));
		
		JLabel lblMin = new JLabel("Minimum:");
		lblMin.setFont(GUI.smallPlainFont);
		
		sliderMin = new JSlider();
		sliderMin.setFocusable(false);
		txtMin = new JTextField();
		txtMin.setEditable(false);
		txtMin.setColumns(10);
		txtMin.setFocusable(false);
		
		JLabel lblMax = new JLabel("Maximum:");
		lblMin.setFont(GUI.smallPlainFont);
		
		sliderMax = new JSlider();
		sliderMax.setFocusable(false);
		
		txtMax = new JTextField();
		txtMax.setEditable(false);
		txtMax.setColumns(10);
		txtMax.setFocusable(false);
		
		JButton btnClose = new JButton("Close");
		
		this.sliderMin.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				
				if (changing) return;
				
				txtMin.setText(sliderMin.getValue() + "");
				receiver.adjustMinMax(sliderMin.getValue(), sliderMax.getValue());
			}
		});
		
		this.sliderMax.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				
				if (changing) return;
				
				txtMax.setText(sliderMax.getValue() + "");
				receiver.adjustMinMax(sliderMin.getValue(), sliderMax.getValue());

			}
		});
		
		btnClose.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				removeDisplay();
			}
			
		});
		
		GroupLayout gl_mainPanel = new GroupLayout(mainPanel);
		gl_mainPanel.setHorizontalGroup(
			gl_mainPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING, false)
									.addComponent(lblInstructions)
									.addComponent(lblMin)
									.addGroup(gl_mainPanel.createSequentialGroup()
										.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
											.addComponent(sliderMax, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(sliderMin, GroupLayout.PREFERRED_SIZE, 207, GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING)
											.addComponent(txtMax, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE)
											.addComponent(txtMin, 0, 0, Short.MAX_VALUE))))
								.addComponent(lblMax)))
						.addGroup(gl_mainPanel.createSequentialGroup()
							.addGap(113)
							.addComponent(btnClose)))
					.addContainerGap(13, Short.MAX_VALUE))
		);
		gl_mainPanel.setVerticalGroup(
			gl_mainPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_mainPanel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblInstructions)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblMin)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING, false)
						.addComponent(sliderMin, 0, 0, Short.MAX_VALUE)
						.addComponent(txtMin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(lblMax)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_mainPanel.createParallelGroup(Alignment.LEADING, false)
						.addComponent(sliderMax, 0, 0, Short.MAX_VALUE)
						.addComponent(txtMax, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED, 16, Short.MAX_VALUE)
					.addComponent(btnClose)
					.addContainerGap())
		);
		mainPanel.setLayout(gl_mainPanel);
	}
	
	public void removeDisplay() {
		setVisible(false);
		reset();
	}
	
	public void setValues(int min, int max, int currValueMin, int currValueMax) {
		changing = true;
		this.sliderMax.setEnabled(true);
		this.sliderMin.setEnabled(true);
		this.txtMin.setText("" + currValueMin);
		this.txtMax.setText("" + currValueMax);
		this.sliderMin.setMinimum(min);
		this.sliderMax.setMinimum(min);
		this.sliderMin.setMaximum(max);
		this.sliderMax.setMaximum(max);
		this.sliderMin.setValue(currValueMin);
		this.sliderMax.setValue(currValueMax);
		changing = false;
	}

	public void display(Component parent) {
		
		changing = true;
		setLocationRelativeTo(parent);
		setVisible(true);
		changing = false;
	}
	
	public void reset() {
		changing = true;
		this.sliderMin.setEnabled(false);
		this.sliderMax.setEnabled(false);
		this.txtMax.setText("");
		this.txtMin.setText("");
		this.sliderMax.setMinimum(1);
		this.sliderMax.setMaximum(3);
		this.sliderMax.setValue(2);
		this.sliderMin.setMinimum(1);
		this.sliderMin.setMaximum(3);
		this.sliderMin.setValue(2);
		changing = false;
	}
	
	public interface BrightnessChangeReceiver {
		
		public void adjustMinMax(int min, int max);
		
	}
	
}
