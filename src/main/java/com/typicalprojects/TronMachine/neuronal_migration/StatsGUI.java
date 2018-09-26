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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.typicalprojects.TronMachine.neuronal_migration.Settings.SettingsLoader;
import com.typicalprojects.TronMachine.neuronal_migration.processing.Analyzer;
import com.typicalprojects.TronMachine.neuronal_migration.processing.Analyzer.Calculation;
import com.typicalprojects.TronMachine.util.AdvancedWorkbook;
import com.typicalprojects.TronMachine.util.FileBrowser;
import com.typicalprojects.TronMachine.util.FileContainer;
import com.typicalprojects.TronMachine.util.SimpleJList;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;

import javax.swing.JButton;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JTextField;
import javax.swing.JSeparator;
import java.awt.Color;

public class StatsGUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6684090290891111529L;
	private JPanel contentPane;
	private JFrame self;
	private GUI mainGUI;
	private SimpleJList<FileContainer> listSelectedFiles;
	private JTextField txtOutputLocation;
	private Thread processor;
	private JButton btnAddFile;
	private JButton btnRemoveFile;
	private JButton btnBrowse;
	private JButton btnProcess;
	private JButton btnDone;
	private volatile boolean processing = false;
	private FileContainer storeDir = null;
	private JLabel lblStatus;
	private FileBrowser fileBrowser = null;
	private FileBrowser fileBrowserSingular = null;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					StatsGUI frame = new StatsGUI(null);
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public StatsGUI(GUI mainGUI) {
		this.mainGUI = mainGUI;
		this.self = this;
		this.fileBrowser = new FileBrowser(FileBrowser.MODE_DIRECTORIES, null, true);
		this.fileBrowserSingular = new FileBrowser(FileBrowser.MODE_DIRECTORIES, null, false);

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		Dimension dimOfWindow = new Dimension(550, 380);
		setBounds(100, 100, dimOfWindow.width, dimOfWindow.height);
		this.setMinimumSize(dimOfWindow);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent windowEvent) {

				runDoneButtonProcessing();

			}
		});

		JPanel pnlMain = new JPanel();
		pnlMain.setBorder(null);
		contentPane.add(pnlMain, BorderLayout.CENTER);

		JLabel lblInstructions = new JLabel("<html>Select excel files for analyzing stats, and an output location, and hit Process.</html>");
		lblInstructions.setFont(new Font("PingFang TC", Font.BOLD, 14));

		JLabel lblInputFiles = new JLabel("Input Files:");
		lblInputFiles.setFont(GUI.smallPlainFont);

		JScrollPane scrollPane = new JScrollPane();

		btnAddFile = new JButton("Add...");
		btnAddFile.setFocusable(false);
		btnAddFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings settings = GUI.settings;
				if (settings != null && settings.recentOpenAnalysisOutputLocations != null) {
					fileBrowser.startBrowsing(settings.recentOpenAnalysisOutputLocations, self);
				} else {
					fileBrowser.startBrowsing(null, self);
				}
	

				List<FileContainer> inputFiles = new ArrayList<FileContainer>();
				List<File> files = fileBrowser.getSelectedFiles();
	
				if (files != null && files.size() != 0) {
					for (int i = 0; i < files.size(); i++) {
						if (!files.get(i).isDirectory())
							continue;
						File[] analysisFile = files.get(i).listFiles(new FilenameFilter() {
							public boolean accept(File directory, String fileName) {
								return fileName.endsWith(".xlsx");
							}
						});
						
						if (analysisFile.length > 0) {
							FileContainer newInputDir = new FileContainer(analysisFile[0]);
							if (inputFiles.contains(newInputDir)) {
								JOptionPane.showMessageDialog(contentPane, "You cannot add duplicate files.", "Error Selecting Files.", JOptionPane.ERROR_MESSAGE);
								return;
							} else {
								inputFiles.add(newInputDir);
							}
						}
					}
					for (FileContainer fc : inputFiles) {
						listSelectedFiles.addItem(fc);
					}
					listSelectedFiles.setSelectedIndex(0);
				}

				if (!listSelectedFiles.isEmpty() && !txtOutputLocation.getText().equals("")) {
					btnProcess.setEnabled(true);
				} else {
					btnProcess.setEnabled(false);
				}
				
				List<File> fileRecents = fileBrowser.getRecents();
				if (fileRecents != null && fileRecents.size() > 0) {
					if (settings.recentOpenAnalysisOutputLocations != null) {
						settings.recentOpenAnalysisOutputLocations.clear();
					}
					settings.recentOpenAnalysisOutputLocations.addAll(fileRecents);
					SettingsLoader.saveSettings(settings);

				}

			}
		});

		btnRemoveFile = new JButton("Remove");
		btnRemoveFile.setFocusable(false);
		btnRemoveFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (listSelectedFiles.getSelectedValue() != null) {
					listSelectedFiles.removeItem(listSelectedFiles.getSelectedValue());
					if (!listSelectedFiles.isEmpty() && !txtOutputLocation.getText().equals("")) {
						btnProcess.setEnabled(true);
					} else {
						btnProcess.setEnabled(false);
					}
				}

			}
		});

		btnProcess = new JButton("Process");
		btnProcess.setEnabled(false);
		btnProcess.setFocusable(false);
		btnProcess.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				startProcessingAsynchronously();

			}
		});

		JLabel lblOutputLocation = new JLabel("Select Output Location:");
		lblOutputLocation.setFont(GUI.smallPlainFont);
		txtOutputLocation = new JTextField();
		txtOutputLocation.setEditable(false);
		txtOutputLocation.setColumns(10);

		btnBrowse = new JButton("Browse");
		btnBrowse.setFocusable(false);
		btnBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				Settings settings = GUI.settings;
				if (settings != null && settings.recentOpenAnalysisOutputLocations != null) {
					fileBrowserSingular.startBrowsing(settings.recentOpenAnalysisOutputLocations, self);
				} else {
					fileBrowserSingular.startBrowsing(null, self);
				}
	

				List<File> file = fileBrowserSingular.getSelectedFiles();
				if (file != null && file.size() != 0) {
						
					storeDir = new FileContainer(file.get(0));
					txtOutputLocation.setText(storeDir.file.getPath());
					
				}

				if (!listSelectedFiles.isEmpty() && !txtOutputLocation.getText().equals("")) {
					btnProcess.setEnabled(true);
				} else {
					btnProcess.setEnabled(false);
				}
				
				List<File> fileRecents = fileBrowserSingular.getRecents();
				if (fileRecents != null && fileRecents.size() > 0) {
					if (settings.recentOpenAnalysisOutputLocations == null) {
						settings.recentOpenAnalysisOutputLocations = new ArrayList<File>();
					}
					settings.recentOpenAnalysisOutputLocations.addAll(fileRecents);
					SettingsLoader.saveSettings(settings);

				}
			}
		});

		JSeparator sepBottom = new JSeparator();
		sepBottom.setForeground(Color.BLACK);
		
		lblStatus = new JLabel("Ready.");
		lblStatus.setFocusable(false);
		lblStatus.setForeground(new Color(0, 128, 0));

		GroupLayout gl_pnlMain = new GroupLayout(pnlMain);
		gl_pnlMain.setHorizontalGroup(
			gl_pnlMain.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlMain.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_pnlMain.createParallelGroup(Alignment.LEADING)
						.addComponent(lblInstructions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)
						.addComponent(lblOutputLocation)
						.addComponent(lblInputFiles)
						.addComponent(sepBottom, GroupLayout.DEFAULT_SIZE, 528, Short.MAX_VALUE)
						.addGroup(Alignment.TRAILING, gl_pnlMain.createSequentialGroup()
							.addGroup(gl_pnlMain.createParallelGroup(Alignment.TRAILING)
								.addComponent(lblStatus, GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
								.addComponent(txtOutputLocation, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE)
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 431, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_pnlMain.createParallelGroup(Alignment.LEADING)
								.addGroup(Alignment.TRAILING, gl_pnlMain.createParallelGroup(Alignment.TRAILING, false)
									.addComponent(btnRemoveFile, 0, 0, Short.MAX_VALUE)
									.addComponent(btnAddFile, GroupLayout.DEFAULT_SIZE, 91, Short.MAX_VALUE)
									.addComponent(btnBrowse, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addComponent(btnProcess, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 92, GroupLayout.PREFERRED_SIZE))))
					.addContainerGap())
		);
		gl_pnlMain.setVerticalGroup(
			gl_pnlMain.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlMain.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblInstructions)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(lblInputFiles)
					.addGap(6)
					.addGroup(gl_pnlMain.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlMain.createSequentialGroup()
							.addComponent(btnAddFile)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnRemoveFile))
						.addGroup(gl_pnlMain.createSequentialGroup()
							.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 121, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(lblOutputLocation)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_pnlMain.createParallelGroup(Alignment.BASELINE)
								.addComponent(txtOutputLocation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnBrowse))))
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addGroup(gl_pnlMain.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnProcess)
						.addComponent(lblStatus))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(sepBottom, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);

		this.listSelectedFiles = new SimpleJList<FileContainer>();

		this.listSelectedFiles.setFocusable(false);
		scrollPane.setViewportView(this.listSelectedFiles);
		pnlMain.setLayout(gl_pnlMain);

		JPanel pnlControl = new JPanel();
		contentPane.add(pnlControl, BorderLayout.SOUTH);

		btnDone = new JButton("Done");
		btnDone.setFocusable(false);
		btnDone.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runDoneButtonProcessing();
			}
		});
		pnlControl.add(btnDone);
	}

	public void removeDisplay() {

		setVisible(false);
		mainGUI.refocus();
	}

	public void display(Component parent) {
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	public boolean isDisplaying() {
		return this.isVisible();
	}

	public synchronized void resetFields() {
		this.processing = false;
		this.processor = null;
		this.txtOutputLocation.setText("");
		this.listSelectedFiles.clear();
		this.btnAddFile.setEnabled(true);
		this.btnBrowse.setEnabled(true);
		this.btnRemoveFile.setEnabled(true);
		this.btnProcess.setEnabled(false);
		this.storeDir = null;

	}

	public void runDoneButtonProcessing() {
		if (this.processing) {
			if (JOptionPane.showConfirmDialog(null, "Do you want to cancel stats processing?", "Exit Status Processing", 
					JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
				this.processing = false;
				this.processor = null;
				removeDisplay();
				resetFields();


			}
		} else {
			removeDisplay();
			resetFields();
		}
	}

	public synchronized void startProcessingAsynchronously() {
		this.processing = true;

		this.processor = new Thread(new Runnable() {
			public void run(){
				try {
					lblStatus.setForeground(Color.DARK_GRAY);
					lblStatus.setText("Preparing...");
					btnAddFile.setEnabled(false);
					btnRemoveFile.setEnabled(false);
					btnBrowse.setEnabled(false);
					btnProcess.setEnabled(false);
					
					AdvancedWorkbook statsWorkbook = new AdvancedWorkbook();
					
					List<FileContainer> inputExcelFiles = Collections.list(listSelectedFiles.getElements());
					Map<Channel, LinkedHashMap<String, double[]>> denseData = new HashMap<Channel, LinkedHashMap<String, double[]>>();
					for (FileContainer inputExcelFile : inputExcelFiles) {
						lblStatus.setText("Opening Analysis File...");

						AdvancedWorkbook inputExcel = new AdvancedWorkbook(inputExcelFile.file);
						LinkedHashMap<String, Map<Channel, double[]>> neuronCounterData = inputExcel.pullNeuronCounterData();
						
						if (!neuronCounterData.entrySet().iterator().hasNext()) {
							continue;
						}
						Map<Channel, double[]> counterDataFirstLine = neuronCounterData.entrySet().iterator().next().getValue();
						lblStatus.setText("Performing Calculations...");

						for (Entry<Channel, double[]> en : counterDataFirstLine.entrySet()) {
							LinkedHashMap<String, double[]> summaryValues = denseData.get(en.getKey());
							if (summaryValues == null) {
								summaryValues = new LinkedHashMap<String, double[]>();
								denseData.put(en.getKey(), summaryValues);
							}
							double[] summaryStatsForChannel = new double[3];
							summaryStatsForChannel[0] = Analyzer.calculate(Calculation.AVERAGE, en.getValue());
							summaryStatsForChannel[1] = Analyzer.calculate(Calculation.STDEV, en.getValue());
							summaryStatsForChannel[2] = Analyzer.calculate(Calculation.NUM, en.getValue());

							summaryValues.put(inputExcelFile.toString().substring(0, inputExcelFile.toString().indexOf(" ANALYSIS")), summaryStatsForChannel);
								
						}
						
					}
					lblStatus.setText("Saving...");
					statsWorkbook.writeSummaryStatsSheets(denseData);
					statsWorkbook.save(new File(storeDir.file.getPath() + File.separator + "SummaryData.xlsx"));
					
					doneProcessingAsynchronously(false);
				} catch (Exception ex) {
					doneProcessingAsynchronously(true);
					JOptionPane.showMessageDialog(contentPane, "<html>There was an error while processing.<br><br> Check to make sure excel files are formatted correctly.</html>", "Error Processing", JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
				}
				

			}
		});
		this.processor.setDaemon(true);
		this.processor.start();
	}

	public synchronized void doneProcessingAsynchronously(boolean error) {
		this.processing = false;
		resetFields();
		if (error) {
			lblStatus.setForeground(Color.RED);
			lblStatus.setText("Error.");
		} else {
			lblStatus.setForeground(new Color(0, 128, 0));
			lblStatus.setText("Done.");
			JOptionPane.showMessageDialog(this.contentPane, "Done processing.", "Finished.", JOptionPane.INFORMATION_MESSAGE);

		}
	}
	

	
}
