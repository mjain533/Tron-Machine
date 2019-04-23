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

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;
import javax.swing.JButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class HelpPopup extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3705358982414585334L;
	private JPanel contentPane;
	private int height;
	private int width;

	
	/*public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					HelpPopup frame = new HelpPopup(400,200,"", null);
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
	public HelpPopup(int height, int width, String message) {
		this.height = height;
		this.width = width;
		setFont(GUI.smallPlainFont);
		setTitle("Help");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setBounds(100, 100, width, height);
		//setBounds(100, 100, 400, 200);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		JLabel lblIcon = new JLabel("");
		lblIcon.setFocusable(false);
		lblIcon.setVerticalAlignment(SwingConstants.TOP);
		lblIcon.setBorder(new EmptyBorder(10, 10, 0, 0));
		//new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/question.png")));
		lblIcon.setIcon(new ImageIcon(new ImageIcon(HelpPopup.class.getResource("/question.png")).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));

		//lblIcon.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("question.png")).getImage().getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
		
		
		JLabel lblText = new JLabel(message);
		lblText.setVisible(true);
		lblText.setFocusable(false);
		lblText.setBorder(new EmptyBorder(10, 10, 10, 10));
		lblText.setVerticalAlignment(SwingConstants.TOP);
		
		JButton btnOkay = new JButton("Okay");
		btnOkay.setFocusable(false);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(lblIcon)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblText, GroupLayout.DEFAULT_SIZE, 304, Short.MAX_VALUE))
						.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
							.addContainerGap(309, Short.MAX_VALUE)
							.addComponent(btnOkay)))
					.addContainerGap())
		);
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(lblText, GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
						.addComponent(lblIcon))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnOkay)
					.addContainerGap())
		);
		contentPane.setLayout(gl_contentPane);
		
		btnOkay.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				disappear();
			}
			
		});
	}
	
	public void display(Component component) {
		this.setBounds(100, 100, this.width, this.height);
		this.setLocationRelativeTo(component);
		this.setVisible(true);
	}

	public void disappear() {
		this.setVisible(false);
	}
	
}
