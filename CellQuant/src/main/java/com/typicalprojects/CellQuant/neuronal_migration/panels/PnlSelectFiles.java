package com.typicalprojects.CellQuant.neuronal_migration.panels;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;

import com.typicalprojects.CellQuant.neuronal_migration.GUI;
import com.typicalprojects.CellQuant.popup.HelpPopup;
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
	private JCheckBox chkAllOutputSingleFolder;
	private JButton btnSelectFilesHelp;
	private SimpleJList<FileContainer> listSelectedFiles;
	private HelpPopup helpPopup;
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
		
		this.helpPopup = new HelpPopup(300, 500, message, gui.getComponent());
		
		JLabel lblSelectFileInstruction = new JLabel("<html>Please select an input image or folder of images (must have extension .czi):</html>");

		btnSelectFiles = new JButton("Select File(s)...");
		btnSelectFiles.setFocusable(false);

		JLabel lblSelectedFiles = new JLabel("Selected Files");

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setFocusable(false);

		btnRemoveSelectedFile = new JButton("Remove");
		btnRemoveSelectedFile.setFocusable(false);

		JSeparator separatorSelectFiles = new JSeparator();
		separatorSelectFiles.setBackground(Color.GRAY);

		btnGo = new JButton("Go");
		btnGo.setFocusable(true);

		btnCancelRun = new JButton("Cancel Run");
		btnCancelRun.setFocusable(false);

		chkAllOutputSingleFolder = new JCheckBox("All output in single folder");
		chkAllOutputSingleFolder.setSelected(false);
		chkAllOutputSingleFolder.setFocusable(false);

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
										.addComponent(chkAllOutputSingleFolder)
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
										.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 61, GroupLayout.PREFERRED_SIZE))
								.addComponent(btnRemoveSelectedFile))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_lblSelectFiles.createParallelGroup(Alignment.BASELINE)
								.addComponent(chkAllOutputSingleFolder)
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
				JFileChooser fc = new JFileChooser();
				fc.setApproveButtonText("Add Input Location");
				fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
				fc.showOpenDialog(null);
				File file = fc.getSelectedFile();
				if (file != null) {
					if (file.isDirectory() || file.getName().toLowerCase().endsWith(".czi")) {
						
						if (file.isDirectory()) {
							
							if (file.list().length > 500) {
								JOptionPane.showMessageDialog(null, "<html>The folder <em>" + file.getName() + "</em> has too many files in it.<br><br>Folders must have less than 500 files.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							
							List<String> existingNames = new ArrayList<String>();
							Enumeration<FileContainer> en = listSelectedFiles.getElements();
							while (en.hasMoreElements()) {
								existingNames.add(en.nextElement().file.getName());
							}
							
							File[] subFiles = file.listFiles();
							List<File> filesToCheck = new ArrayList<File>();
							for (File subFile : subFiles) {
								if (subFile.isDirectory() || !subFile.exists()) continue;
								if (!subFile.getName().toLowerCase().endsWith(".czi")) continue;	
								filesToCheck.add(subFile);
							}
							if (filesToCheck.isEmpty()) {
								JOptionPane.showMessageDialog(null, "<html>The directory <em>" + file.getName() + "</em> doesn't contain any files with extension '.czi'!", "File Selection Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							String alreadyExists = null;
							for (File subFile : filesToCheck) {
								if (existingNames.contains(subFile.getName())) {
									alreadyExists = subFile.getName();
									break;
								}
							}
							
							if (alreadyExists != null) {
								JOptionPane.showMessageDialog(null, "<html>The file <em>" + alreadyExists + "</em> in this directory has already been added.<br><br>. Duplicate files are not allowed.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							
							for (File fileToAdd : filesToCheck) {
								listSelectedFiles.addItem(new FileContainer(fileToAdd));
							}
							if (listSelectedFiles.getSelected()== null) {
								listSelectedFiles.setSelection(0);
							}
							if (currDisplayState == PnlSelectFiles.STATE_NO_FILE_ADDED) {
								setDisplayState(STATE_FILE_ADDED);
							}
						} else {
							String name = file.getName();
							Enumeration<FileContainer> en = listSelectedFiles.getElements();
							while (en.hasMoreElements()) {
								FileContainer fileContainer = en.nextElement();
								if (fileContainer.file.getName().equals(name)) {
									JOptionPane.showMessageDialog(null, "<html>The file <em>" + file.getName() + "</em> has already been added.<br><br>All files must have unique names.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
									return;
								}
							}
							listSelectedFiles.addItem(new FileContainer(file));
							if (listSelectedFiles.getSelected()== null) {
								listSelectedFiles.setSelection(0);
							} 
							if (currDisplayState == PnlSelectFiles.STATE_NO_FILE_ADDED) {
								setDisplayState(STATE_FILE_ADDED);
							}
							
						}
						
					} else {
						JOptionPane.showMessageDialog(null, "<html>You must select a file with extension '.czi' or a folder.<br><br><em>" + file.getName() + "</em> is not valid.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
		});
		
		this.btnSelectFilesHelp.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				helpPopup.display();				
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
				
				new Thread(new Runnable() {
			        public void run(){
			            
			        	btnGo.setEnabled(false);
						File oneSaveDirectory = null;
						if (chkAllOutputSingleFolder.isSelected()) {
							File directory = new File(GUI.folderName + File.separator + "Neuron Counter Output " + GUI.dateString);
							if (directory.isDirectory()) {
								directory.mkdir();
							}
							oneSaveDirectory = directory;
						}
						
						List<ImagePhantom> futureImages = new LinkedList<ImagePhantom>();
						Enumeration<FileContainer> files = listSelectedFiles.getElements();
						while (files.hasMoreElements()) {
							FileContainer container = files.nextElement();
							
							String saveDir = (oneSaveDirectory == null ? (container.file.getParent() + File.separator + "Neuron Counter Output " + GUI.dateString) : oneSaveDirectory.getPath());
							int indexOf = container.file.getName().lastIndexOf('.');
							futureImages.add(new ImagePhantom(container.file, container.file.getName().substring(0, indexOf), gui, new File(saveDir), false, null));
						}
						gui.getWizard().nextState(futureImages);
			        	
			        }
			    }).start();
				
				
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
			this.chkAllOutputSingleFolder.setEnabled(true);
			this.listSelectedFiles.clear();
			this.listSelectedFiles.getGUIComponent().setEnabled(true);
			break;
		case STATE_FILE_ADDED:
			this.btnCancelRun.setEnabled(false);
			this.btnGo.setEnabled(true);
			this.btnRemoveSelectedFile.setEnabled(true);
			this.btnSelectFiles.setEnabled(true);
			this.chkAllOutputSingleFolder.setEnabled(true);
			this.listSelectedFiles.getGUIComponent().setEnabled(true);
			break;
		case STATE_FILES_RUNNING:
			this.btnCancelRun.setEnabled(true);
			this.btnGo.setEnabled(false);
			this.btnRemoveSelectedFile.setEnabled(false);
			this.btnSelectFiles.setEnabled(false);		
			this.chkAllOutputSingleFolder.setEnabled(false);
			this.listSelectedFiles.getGUIComponent().setEnabled(false);
			this.listSelectedFiles.getGUIComponent().clearSelection();
			break;
		}
	}
	
	private class FileContainer {
		
		private File file;
		
		private FileContainer(File file) {
			this.file = file;
		}
		
		public String toString() {
			return file.getName();
		}
		
	}

}
