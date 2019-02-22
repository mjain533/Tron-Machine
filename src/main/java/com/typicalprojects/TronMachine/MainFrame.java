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

package com.typicalprojects.TronMachine;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import java.awt.Font;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;

import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.awt.event.ActionEvent;

/**
 * The primary point of application launch. The user may then select a program to continue. This menu can
 * be returned to later, so it is NOT disposed of. All subsequent programs are created via workers in this
 * class.
 * 
 * @author Justin Carrington
 */
public class MainFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8653687111169656048L;
	private JPanel contentPane;

	private static MainFrame SINGLETON = null;;
	private WaitingFrame wf = null;
	//private static WaitingFrame waitingFrame = new WaitingFrame();
	private GUI guiWindow;

	/**
	 * Launch the application.
	 * 
	 * @param args launch parameters, we ignore
	 */
	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
				} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
						| UnsupportedLookAndFeelException e1) {
					e1.printStackTrace();
				}

				SINGLETON = new MainFrame();
				SINGLETON.setVisible(true);

			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setTitle("The TRON Machine");



		wf = new WaitingFrame();


		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 350, 250);
		setLocationRelativeTo(null);
		contentPane = new JPanel();
		this.setResizable(false);

		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JLabel lblNewLabel = new JLabel("Please select a program.");
		lblNewLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
		lblNewLabel.setFont(lblNewLabel.getFont().deriveFont(lblNewLabel.getFont().getStyle() | Font.BOLD, lblNewLabel.getFont().getSize() + 3f));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		contentPane.add(lblNewLabel, BorderLayout.NORTH);

		JPanel panel = new JPanel();
		panel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
		contentPane.add(panel, BorderLayout.CENTER);

		JButton btnQuantifyMigration = new JButton("<html><center>Quantify Neuronal Migration</center></html>")  ;
		btnQuantifyMigration.setOpaque(true);
		btnQuantifyMigration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							if (guiWindow == null) {
								wf.show(SINGLETON);
								setVisible(false);
								
								MySwingWorker worker = new MySwingWorker(SINGLETON);
								worker.execute();
								worker.addPropertyChangeListener(new PropertyChangeListener() {
									@Override
									public void propertyChange(final PropertyChangeEvent event) {
										switch (event.getPropertyName()) {
										case "progress":
											break;
										case "state":
											switch ((StateValue) event.getNewValue()) {
											case DONE:
												try {
													final GUI gui = worker.get();
													guiWindow = gui;
													gui.show();
													wf.disappear();

												} catch (final Exception e) {
													e.printStackTrace();
													GUI.displayMessage("Initialization error.", "Startup Error", null, JOptionPane.ERROR_MESSAGE);
													wf.disappear();
													System.exit(0);
												}

												break;
											default:
												break;
											}
											break;
										}
									}
								});
								//GUI gui = worker.get();
								//guiWindow = gui;
								/*if (guiWindow == null) {
									guiWindow = new GUI(SINGLETON);
								}*/
								//wf.disappear();

								//guiWindow.show();
							} else {
								
								guiWindow.show();
								setVisible(false);
							}
							

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}
		});
		btnQuantifyMigration.setFont(btnQuantifyMigration.getFont().deriveFont(btnQuantifyMigration.getFont().getStyle() | Font.BOLD));
		btnQuantifyMigration.setFocusable(false);
		btnQuantifyMigration.setIconTextGap(0);
		btnQuantifyMigration.setAlignmentY(0.0f);;

		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap(73, Short.MAX_VALUE)
					.addComponent(btnQuantifyMigration, GroupLayout.PREFERRED_SIZE, 185, GroupLayout.PREFERRED_SIZE)
					.addGap(68))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addGap(36)
					.addComponent(btnQuantifyMigration, GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
					.addGap(38))
		);
		panel.setLayout(gl_panel);



	}
	
	/**
	 * Re-displays the main menu, if returned to from another program.
	 */
	public void reshow() {
		setVisible(true);
	}
}

/**
 * Intended only for local use. See description for {@link MainFrame}
 * 
 * @author Justin Carrington
 */
class MySwingWorker extends SwingWorker<GUI, String> {

	private MainFrame mf;

	protected MySwingWorker(MainFrame mf) {
		this.mf = mf;
	}

	protected GUI doInBackground() throws Exception {
		GUI gui = new GUI(mf);
		return gui;
	}

}
