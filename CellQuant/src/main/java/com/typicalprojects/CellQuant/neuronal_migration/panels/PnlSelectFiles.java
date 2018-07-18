package com.typicalprojects.CellQuant.neuronal_migration.panels;

import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
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

import com.typicalprojects.CellQuant.neuronal_migration.GUI;
import com.typicalprojects.CellQuant.neuronal_migration.Preferences;
import com.typicalprojects.CellQuant.popup.HelpPopup;
import com.typicalprojects.CellQuant.util.DeepDirectoryWalker;
import com.typicalprojects.CellQuant.util.FileContainer;
import com.typicalprojects.CellQuant.util.ImagePhantom;
import com.typicalprojects.CellQuant.util.SimpleJList;

public class PnlSelectFiles {


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


	public PnlSelectFiles( GUI guiRef) {
		this.gui = guiRef;
		rawPanel = new JPanel();
		rawPanel.setVisible(false);
		rawPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		String message = "<html>If this box is checked, then a single output folder named "
				+ "with today's date and time will be created within the "+GUI.folderName+" folder, "
				+ "which is in the same directory as this program.<br><br>"
				+ "If unchecked, then an output folder will be created at the location of each individual image. "
				+ "This folder will be titled Neuron Counter Output [DATE]. If a directory of images was selected for "
				+ "processing, then one single output folder will be created within that directory, simply named "
				+ "Neuron Counter Output [DATE].</html>";

		this.helpPopup = new HelpPopup(320, 500, message);

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
								.addGroup(gl_lblSelectFiles.createSequentialGroup()
										.addComponent(chkSelectFolders)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnSelectFilesHelp)))
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
						.addGroup(gl_lblSelectFiles.createParallelGroup(Alignment.BASELINE)
								.addComponent(chkSelectFolders)
								.addComponent(btnSelectFilesHelp))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(separatorSelectFiles, GroupLayout.PREFERRED_SIZE, 14, GroupLayout.PREFERRED_SIZE)
						.addGap(6)
						.addGroup(gl_lblSelectFiles.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnGo)
								.addComponent(btnCancelRun))
						.addContainerGap())
				);

		listSelectedFiles = new SimpleJList<FileContainer>();
		listSelectedFiles.getGUIComponent().setFocusable(false);
		scrollPane.setViewportView(listSelectedFiles.getGUIComponent());
		rawPanel.setLayout(gl_lblSelectFiles);

		this.btnSelectFiles.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				if (chkSelectFolders.isSelected()) {
					System.setProperty("apple.awt.fileDialogForDirectories", "true"); 
				}

				FileDialog fd = new FileDialog(gui.getComponent());
				if (GUI.lastSelectFileLocation != null) {
					fd.setDirectory(GUI.lastSelectFileLocation.getPath());
				}
				fd.setMode(FileDialog.LOAD);
				fd.setMultipleMode(true);
				fd.setVisible(true);
				
				File[] files = fd.getFiles();
				File lastSelectLocation = null;
				if (files != null && files.length != 0) {
					
					List<FileContainer> cziFiles = new ArrayList<FileContainer>();
					for (File file : files) {
						
						if (file.isDirectory()) {
							if (lastSelectLocation == null)
								lastSelectLocation = file;
							
							DeepDirectoryWalker ddw = new DeepDirectoryWalker(".czi", 6);
							
							try {
								cziFiles.addAll(ddw.getFilteredFiles(file));
							} catch (IOException e1) {
								JOptionPane.showMessageDialog(null, "<html>The folder <em>" + file.getName() + "</em> either has too many files in it or you cannot access it.<br><br>Folders must have less than 500 files.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
								System.setProperty("apple.awt.fileDialogForDirectories", "false"); 
								return;
							}
							
							
						} else {
							if (lastSelectLocation == null) {
								lastSelectLocation = file.getParentFile();
							}
							if (!file.getName().endsWith(".czi")){
								JOptionPane.showMessageDialog(null, "<html>The file <em>" + file.getName() + "</em> is not a .czi file.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
								System.setProperty("apple.awt.fileDialogForDirectories", "false"); 
								return;
							}
							cziFiles.add(new FileContainer(file));
						}
					}
					
					if (lastSelectLocation != null) {
						GUI.lastSelectFileLocation = lastSelectLocation;
						try {
							Preferences.writeSettingsFromGUI();
						} catch (IOException e1) {
							// TODO shouldn't happen
							e1.printStackTrace();
						}
					}
					if (cziFiles.isEmpty()) {
						JOptionPane.showMessageDialog(null, "<html>The selection was neither a CZI nor contained any files with extension '.czi'!", "File Selection Error", JOptionPane.ERROR_MESSAGE);
						System.setProperty("apple.awt.fileDialogForDirectories", "false"); 
						return;
					}
					Enumeration<FileContainer> en = listSelectedFiles.getElements();
					while (en.hasMoreElements()) {
						FileContainer fc = en.nextElement();
						if (cziFiles.contains(fc)) {
							JOptionPane.showMessageDialog(null, "<html>The file <em>" + fc.toString() + "</em> has already been added.<br><br>. Duplicate files are not allowed.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
							System.setProperty("apple.awt.fileDialogForDirectories", "false"); 
							return;
						}
					}
					
					for (FileContainer fileToAdd : cziFiles) {
						listSelectedFiles.addItem(fileToAdd);
					}
					if (listSelectedFiles.getSelected()== null) {
						listSelectedFiles.setSelection(0);
					}
					if (currDisplayState == PnlSelectFiles.STATE_NO_FILE_ADDED) {
						setDisplayState(STATE_FILE_ADDED);
					}
					
					System.setProperty("apple.awt.fileDialogForDirectories", "false"); 

				}
			}

		});

		this.btnSelectFilesHelp.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				helpPopup.display(gui.getComponent());				
			}

		});

		this.btnRemoveSelectedFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				FileContainer fc = listSelectedFiles.getSelected();
				if (fc == null)
					return;

				listSelectedFiles.removeItem(fc);

				if (listSelectedFiles.isEmpty()) {
					setDisplayState(STATE_NO_FILE_ADDED);
				} else {
					listSelectedFiles.setSelection(0);
				}
			}
		});

		this.btnGo.addActionListener(new ActionListener( ) {
			public void actionPerformed(ActionEvent e) {
				if (listSelectedFiles.isEmpty()) return;

				if (GUI.outputLocation == null) {
					JOptionPane.showMessageDialog(null, "<html>You have not set a location to output data.<br><br>Do so in the Preferences.</html>", "No Output Folder", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				processor = new Thread(new Runnable() {
					public void run(){
						

						List<ImagePhantom> futureImages = new LinkedList<ImagePhantom>();
						Enumeration<FileContainer> files = listSelectedFiles.getElements();
						while (files.hasMoreElements()  && processor != null) {
							FileContainer container = files.nextElement();

							int indexOf = container.file.getName().lastIndexOf('.');
							futureImages.add(new ImagePhantom(container.file, container.file.getName().substring(0, indexOf), gui.getProgressReporter(), null));
						}
						if (processor != null) {
							gui.getWizard().nextState(futureImages);
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
				System.out.println("Cancel");
				cancel();
				System.out.println("Cancel2");
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
			this.listSelectedFiles.getGUIComponent().setEnabled(true);
			break;
		case STATE_FILE_ADDED:
			this.btnCancelRun.setEnabled(false);
			this.btnGo.setEnabled(true);
			this.btnRemoveSelectedFile.setEnabled(true);
			this.btnSelectFiles.setEnabled(true);
			this.chkSelectFolders.setEnabled(true);
			this.listSelectedFiles.getGUIComponent().setEnabled(true);
			break;
		case STATE_FILES_RUNNING:
			this.btnCancelRun.setEnabled(true);
			this.btnGo.setEnabled(false);
			this.btnRemoveSelectedFile.setEnabled(false);
			this.btnSelectFiles.setEnabled(false);		
			this.chkSelectFolders.setEnabled(false);
			this.listSelectedFiles.getGUIComponent().setEnabled(false);
			this.listSelectedFiles.getGUIComponent().clearSelection();
			break;
		}
	}
	
	public void cancelOpening() {
		this.processor = null;
	}

}

