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
package com.typicalprojects.TronMachine.neuronal_migration.panels;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.Settings;
import com.typicalprojects.TronMachine.popup.HelpPopup;
import com.typicalprojects.TronMachine.util.DeepDirectoryWalker;
import com.typicalprojects.TronMachine.util.FileBrowser;
import com.typicalprojects.TronMachine.util.FileContainer;
import com.typicalprojects.TronMachine.util.ImagePhantom;
import com.typicalprojects.TronMachine.util.SimpleJList;
import com.typicalprojects.TronMachine.util.SimpleJList.ListDropReceiver;

public class PnlSelectFiles implements ListDropReceiver {


	public static final int STATE_NO_FILE_ADDED = 0;
	public static final int STATE_FILE_ADDED = 1;
	public static final int STATE_FILES_RUNNING = 2;

	private int currDisplayState = -1;

	private JPanel rawPanel;
	private JButton btnSelectFiles;
	private JButton btnRemoveSelectedFile;
	private JButton btnGo;
	private JButton btnCancelRun;
	private JCheckBox chkSelectFolders;
	private JButton btnSelectFilesHelp;
	private SimpleJList<FileContainer> listSelectedFiles;
	private HelpPopup helpPopup;
	private Thread processor;
	private volatile GUI gui;
	private FileBrowser fileBrowser;

	public PnlSelectFiles( GUI guiRef) {

		this.gui = guiRef;
		this.fileBrowser = new FileBrowser(FileBrowser.MODE_BOTH, Arrays.asList("czi", "tif", "tiff"), true);
		rawPanel = new JPanel();
		rawPanel.setVisible(false);
		rawPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		String message = "<html>If this box is checked, then only folders will be selected. When you select a folder, all CZI images within the folder will be selected along with all other CZI images in subfolder 6 levels deep.</html>";

		this.helpPopup = new HelpPopup(220, 350, message);

		JLabel lblSelectFileInstruction = new JLabel("<html>Please select an input image or folder of images (must have extension .czi):</html>");

		btnSelectFiles = new JButton("Select File(s)...");
		btnSelectFiles.setFocusable(false);

		JLabel lblSelectedFiles = new JLabel("Selected Files");

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setFocusable(false);

		btnRemoveSelectedFile = new JButton("Remove");
		btnRemoveSelectedFile.setFocusable(false);

		JSeparator separatorSelectFiles = new JSeparator();
		separatorSelectFiles.setForeground(Color.BLACK);
		separatorSelectFiles.setBackground(Color.BLACK);

		btnGo = new JButton("Go");
		btnGo.setFocusable(true);

		btnCancelRun = new JButton("Cancel Run");
		btnCancelRun.setFocusable(false);

		chkSelectFolders = new JCheckBox("Select Folders");
		chkSelectFolders.setSelected(false);
		chkSelectFolders.setFocusable(false);
		chkSelectFolders.setEnabled(true);

		btnSelectFilesHelp = new JButton("");
		btnSelectFilesHelp.setFocusable(false);
		btnSelectFilesHelp.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("question.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));

		btnSelectFilesHelp.setForeground(Color.BLUE);
		btnSelectFilesHelp.setBorderPainted(false);
		btnSelectFilesHelp.setOpaque(false);
		btnSelectFilesHelp.setBackground(Color.WHITE);


		GroupLayout gl_lblSelectFiles = new GroupLayout(rawPanel);
		gl_lblSelectFiles.setHorizontalGroup(
				gl_lblSelectFiles.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_lblSelectFiles.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_lblSelectFiles.createParallelGroup(Alignment.LEADING)
								.addComponent(separatorSelectFiles, GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
								.addComponent(lblSelectFileInstruction, GroupLayout.DEFAULT_SIZE, 319, Short.MAX_VALUE)
								.addGroup(Alignment.TRAILING, gl_lblSelectFiles.createSequentialGroup()
										.addComponent(btnCancelRun)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnGo))
								.addComponent(btnSelectFiles)
								.addComponent(lblSelectedFiles)
								.addGroup(gl_lblSelectFiles.createSequentialGroup()
										.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 220, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnRemoveSelectedFile))
								/*.addGroup(gl_lblSelectFiles.createSequentialGroup()
										.addComponent(chkSelectFolders)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnSelectFilesHelp))*/
								)
						.addContainerGap())
				);
		gl_lblSelectFiles.setVerticalGroup(
				gl_lblSelectFiles.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_lblSelectFiles.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblSelectFileInstruction)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_lblSelectFiles.createParallelGroup(Alignment.TRAILING)
								.addGroup(gl_lblSelectFiles.createSequentialGroup()
										.addComponent(btnSelectFiles)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(lblSelectedFiles)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, /*61*/80, GroupLayout.PREFERRED_SIZE))
								.addComponent(btnRemoveSelectedFile))
						.addPreferredGap(ComponentPlacement.RELATED)
						/*.addGroup(gl_lblSelectFiles.createParallelGroup(Alignment.BASELINE)
								.addComponent(chkSelectFolders)
								.addComponent(btnSelectFilesHelp))
						.addPreferredGap(ComponentPlacement.RELATED)*/
						.addComponent(separatorSelectFiles, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
						.addGap(6)
						.addGroup(gl_lblSelectFiles.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnGo)
								.addComponent(btnCancelRun))
						.addContainerGap())
				);

		listSelectedFiles = new SimpleJList<FileContainer>(this);
		listSelectedFiles.setFocusable(false);
		scrollPane.setViewportView(listSelectedFiles);
		rawPanel.setLayout(gl_lblSelectFiles);

		this.btnSelectFiles.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				Settings settings = GUI.settings;
				if (settings != null && settings.recentOpenFileLocations != null) {
					fileBrowser.startBrowsing(settings.recentOpenFileLocations, gui.getComponent());
				} else {
					fileBrowser.startBrowsing(null, gui.getComponent());
				}

				List<File> files = fileBrowser.getSelectedFiles();
				processFiles(files);
			}

		});

		this.btnSelectFilesHelp.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				helpPopup.display(gui.getComponent());				
			}

		});

		this.btnRemoveSelectedFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileContainer fc = listSelectedFiles.getSelectedValue();
				if (fc == null)
					return;

				listSelectedFiles.removeItem(fc);

				if (listSelectedFiles.isEmpty()) {
					setDisplayState(STATE_NO_FILE_ADDED);
				} else {
					listSelectedFiles.setSelectedIndex(0);
				}
			}
		});

		this.btnGo.addActionListener(new ActionListener( ) {
			public void actionPerformed(ActionEvent e) {
				if (listSelectedFiles.isEmpty()) return;

				if (GUI.settings.outputLocation == null) {
					JOptionPane.showMessageDialog(null, "<html>You have not set a location to output data.<br><br>Do so in the Preferences.</html>", "No Output Folder", JOptionPane.ERROR_MESSAGE);
					return;
				}

				processor = new Thread(new Runnable() {
					public void run(){


						List<ImagePhantom> futureImages = new LinkedList<ImagePhantom>();
						Enumeration<FileContainer> files = listSelectedFiles.getElements();
						long maxLength = -1;
						while (files.hasMoreElements()  && processor != null) {
							FileContainer container = files.nextElement();
							
							long length = container.file.length();
							if (length > maxLength) {
								maxLength = length;
							}
							int indexOf = container.file.getName().lastIndexOf('.');
							futureImages.add(new ImagePhantom(container.file, container.file.getName().substring(0, indexOf), gui.getLogger(), GUI.settings.createChannelSnapshot()));
						}
						if (processor != null) {
							gui.getWizard().nextState(futureImages);
						}
						long currAvail = Runtime.getRuntime().maxMemory() - Runtime.getRuntime().totalMemory();
						if (currAvail < (maxLength * 1.5)) {
							JOptionPane.showMessageDialog(gui.getPanelDisplay().getImagePanel(), "You only have " + currAvail + " bytes available for processing (RAM) and this might not be enough considering your image size(s).<br>If you run out of RAM, results are unclear. The program may crash or stall.", "Potential Memory Error", JOptionPane.ERROR_MESSAGE);
						}
						processor = null;
					}
				});
				processor.setDaemon(true);
				processor.start();


			}
		});

		this.btnCancelRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});

		this.rawPanel.setVisible(true);
		setDisplayState(STATE_NO_FILE_ADDED);
	}

	public JPanel getRawPanel() {
		return this.rawPanel;
	}

	public void cancel() {
		this.gui.getWizard().cancel();
	}

	public void setDisplayState(int state) {
		this.currDisplayState = state;
		switch (state) {
		case STATE_NO_FILE_ADDED:
			this.btnCancelRun.setEnabled(false);
			this.btnGo.setEnabled(false);
			this.btnRemoveSelectedFile.setEnabled(false);
			this.btnSelectFiles.setEnabled(true);
			this.listSelectedFiles.clear();
			this.chkSelectFolders.setEnabled(true);
			this.listSelectedFiles.setEnabled(true);
			break;
		case STATE_FILE_ADDED:
			this.btnCancelRun.setEnabled(false);
			this.btnGo.setEnabled(true);
			this.btnRemoveSelectedFile.setEnabled(true);
			this.btnSelectFiles.setEnabled(true);
			this.chkSelectFolders.setEnabled(true);
			this.listSelectedFiles.setEnabled(true);
			break;
		case STATE_FILES_RUNNING:
			this.btnCancelRun.setEnabled(true);
			this.btnGo.setEnabled(false);
			this.btnRemoveSelectedFile.setEnabled(false);
			this.btnSelectFiles.setEnabled(false);		
			this.chkSelectFolders.setEnabled(false);
			this.listSelectedFiles.setEnabled(false);
			this.listSelectedFiles.clearSelection();
			break;
		}
	}

	public void cancelOpening() {
		this.processor = null;
	}

	private void processFiles(List<File> files) {
		HashSet<String> endings = new HashSet<String>();
		endings.add("czi");
		endings.add("tiff");
		endings.add("tif");
		
		if (files == null || files.size() == 0)
			return;
		
		Settings settings = GUI.settings;
		
		List<FileContainer> cziFiles = new ArrayList<FileContainer>();
		for (File file : files) {

			if (file.isDirectory()) {
				
				DeepDirectoryWalker ddw = new DeepDirectoryWalker(endings, 6, true, true);

				try {
					cziFiles.addAll(ddw.getFilteredFiles(file));
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, "<html>The folder <em>" + file.getName() + "</em> either has too many files in it or you cannot access it.<br><br>Folders must have less than 500 files.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
					return;
				}


			} else {
				boolean hasEnding = false;
				for (String ending : endings) {
					if (file.getName().endsWith(ending)) {
						hasEnding = true;
						break;
					}
				}
				if (!hasEnding) {
					JOptionPane.showMessageDialog(null, "<html>The file <em>" + file.getName() + "</em> does not have a valid extension.", "File Selection Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				cziFiles.add(new FileContainer(file, false));
			}
		}

		List<File> fileRecents = fileBrowser.getRecents();
		if (fileRecents != null && fileRecents.size() > 0) {
			if (settings.recentOpenFileLocations != null) {
				settings.recentOpenFileLocations.clear();
			} else {
				settings.recentOpenFileLocations = new ArrayList<File>();
			}
			settings.recentOpenFileLocations.addAll(fileRecents);
			settings.needsUpdate= true;
			Settings.SettingsLoader.saveSettings(settings); // if doesn't save, don't notify the user. Just won't save pref.

		}
		

		if (cziFiles.isEmpty()) {
			JOptionPane.showMessageDialog(null, "<html>The selection was neither a CZI nor contained any files with extension '.czi'!", "File Selection Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Enumeration<FileContainer> en = listSelectedFiles.getElements();
		while (en.hasMoreElements()) {
			FileContainer fc = en.nextElement();
			if (cziFiles.contains(fc)) {
				JOptionPane.showMessageDialog(null, "<html>The file <em>" + fc.toString() + "</em> has already been added.<br><br>Duplicate files are not allowed.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		setFileList(cziFiles);
		
		if (listSelectedFiles.getSelectedValue() == null) {
			listSelectedFiles.setSelectedIndex(0);;
		}
		if (currDisplayState == PnlSelectFiles.STATE_NO_FILE_ADDED) {
			setDisplayState(STATE_FILE_ADDED);
		}
	}
	
	public void setFileList(List<FileContainer> files) {
		for (FileContainer fileToAdd : files) {
			listSelectedFiles.addItem(fileToAdd);
		}
	}

	@Override
	public void dropped(List<Object> dropped) {

		List<File> files = new ArrayList<File>();
		for (Object obj : dropped) {
			if (obj instanceof File && ((File) obj).exists()) {
				files.add((File) obj);
			}
		}

		processFiles(files);

	}

}

