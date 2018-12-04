package com.typicalprojects.TronMachine.neuronal_migration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.typicalprojects.TronMachine.util.FileBrowser;
import com.typicalprojects.TronMachine.util.FileContainer;
import com.typicalprojects.TronMachine.util.SimpleJList;
import com.typicalprojects.TronMachine.util.SimpleJList.ListDropReceiver;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.JScrollPane;
import javax.swing.JOptionPane;
import javax.swing.JButton;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class IntermediateProcessingGUI extends JDialog implements ListDropReceiver {

	private static final long serialVersionUID = -7456989140555628711L;
	private final JPanel contentPanel = new JPanel();
	private GUI mainGui;
	private final ButtonGroup btnsObjROI = new ButtonGroup();
	private JButton btnRemoveFile;
	private JButton btnSelectFiles;
	private JRadioButton rdbtnPostROI;
	private JRadioButton rdbtnPostObj;
	private boolean modifying = false;
	private JButton btnRun;
	private JButton btnCancel;
	private SimpleJList<FileContainer> lstInput;
	private FileBrowser fileBrowser;


	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			IntermediateProcessingGUI dialog = new IntermediateProcessingGUI(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public IntermediateProcessingGUI(GUI gui) {
		setTitle("Intermediate State Processing");
		this.mainGui = gui;

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent windowEvent) {

				removeDisplay();

			}
		});

		setBounds(100, 100, 550, 600);
		setMinimumSize(new Dimension(550, 600));
		//getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
		getContentPane().add(contentPanel);

		JPanel pnlInstructions = new JPanel();
		pnlInstructions.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlInstructions.setBackground(new Color(211, 211, 211));

		JPanel pnlSteps = new JPanel();
		pnlSteps.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		this.fileBrowser = new FileBrowser(FileBrowser.MODE_BOTH, Arrays.asList("ser"), false);

		btnCancel = new JButton("Cancel");
		btnCancel.setFocusable(false);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeDisplay();
			}
		});

		btnRun = new JButton("Run");
		btnRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				if (lstInput.isEmpty())
					return;
				
				startProcessing(lstInput.toList(), rdbtnPostObj.isSelected());

			}
		});
		btnRun.setEnabled(false);
		btnRun.setFocusable(false);

		GroupLayout gl_contentPanel = new GroupLayout(contentPanel);
		gl_contentPanel.setHorizontalGroup(
				gl_contentPanel.createParallelGroup(Alignment.TRAILING)
				.addComponent(pnlInstructions, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 530, Short.MAX_VALUE)
				.addGroup(gl_contentPanel.createSequentialGroup()
						.addGap(337)
						.addComponent(btnRun)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(btnCancel))
				.addComponent(pnlSteps, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
				);
		gl_contentPanel.setVerticalGroup(
				gl_contentPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPanel.createSequentialGroup()
						.addComponent(pnlInstructions, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(pnlSteps, GroupLayout.DEFAULT_SIZE, 488, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_contentPanel.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnCancel)
								.addComponent(btnRun)))
				);

		JLabel lblInstructionsStep2 = new JLabel("<html><i>STEP 2:</i> Select files to process. To do this, choose one of the following:<br> "
				+ "<ul>"
				+ "<li>Root Image Output Folder (i.e. in folder in form 'ImgName DATE TIME').</li>"
				+ "<li>Intermediate Files folder (within Root Image Output Folder)</li>"
				+ "<li>Serialization folder (within Intermediate Files folder)</li>"
				+ "<li>The intermediate file itself (ends in .ser, NOT RECOMMENDED)</li>"
				+ "</ul></html>");
		lblInstructionsStep2.setFont(GUI.smallFont);

		JScrollPane scrollPane = new JScrollPane();

		btnRemoveFile = new JButton("Remove");
		btnRemoveFile.setFocusable(false);
		btnRemoveFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				modifying = true;
				List<FileContainer> files = lstInput.getSelectedMult();
				lstInput.removeItems(files);
				lstInput.setSelectedIndex(-1);
				btnRemoveFile.setEnabled(false);
				if (lstInput.isEmpty()) {
					btnRun.setEnabled(false);
				}
				modifying = false;
			}
		});
		btnRemoveFile.setEnabled(false);

		btnSelectFiles = new JButton("Select Files");
		btnSelectFiles.setFocusable(false);
		btnSelectFiles.setEnabled(false);
		btnSelectFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings settings = GUI.settings;
				if (settings != null && settings.recentOpenFileLocations != null) {
					fileBrowser.startBrowsing(settings.recentOpenFileLocations, gui.getComponent());
				} else {
					fileBrowser.startBrowsing(null, gui.getComponent());
				}

				processFiles(fileBrowser.getSelectedFiles());
			}
		});

		JLabel lblInstructionsStep1 = new JLabel("<html><i>STEP 1:</i> Choose which state to start from."
				+ "<ul><li>Post-Object State: After all objects were selected</li>"
				+ "<li>Post-ROI State: After all ROIs were selected</li>"
				+ "</ul></html>");
		lblInstructionsStep1.setFont(new Font("PingFang TC", Font.BOLD, 13));

		rdbtnPostObj = new JRadioButton("Post-Object State");
		rdbtnPostObj.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rdbtnPostObj.isSelected()) {
					btnSelectFiles.setEnabled(true);
				}
			}
		});
		rdbtnPostObj.setFocusable(false);
		btnsObjROI.add(rdbtnPostObj);

		rdbtnPostROI = new JRadioButton("Post-ROI State");
		rdbtnPostROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (rdbtnPostROI.isSelected()) {
					btnSelectFiles.setEnabled(true);
				}
			}
		});
		rdbtnPostROI.setFocusable(false);
		btnsObjROI.add(rdbtnPostROI);
		GroupLayout gl_pnlSteps = new GroupLayout(pnlSteps);
		gl_pnlSteps.setHorizontalGroup(
				gl_pnlSteps.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlSteps.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pnlSteps.createParallelGroup(Alignment.LEADING)
								.addComponent(lblInstructionsStep1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
								.addComponent(lblInstructionsStep2, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
								.addGroup(Alignment.TRAILING, gl_pnlSteps.createSequentialGroup()
										.addComponent(btnRemoveFile)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(btnSelectFiles))
								.addComponent(scrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 502, Short.MAX_VALUE)
								.addGroup(gl_pnlSteps.createSequentialGroup()
										.addComponent(rdbtnPostObj)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(rdbtnPostROI, GroupLayout.PREFERRED_SIZE, 135, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap())
				);
		gl_pnlSteps.setVerticalGroup(
				gl_pnlSteps.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlSteps.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblInstructionsStep1, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_pnlSteps.createParallelGroup(Alignment.BASELINE)
								.addComponent(rdbtnPostObj)
								.addComponent(rdbtnPostROI))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(lblInstructionsStep2)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_pnlSteps.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnSelectFiles)
								.addComponent(btnRemoveFile))
						.addGap(10))
				);

		lstInput = new SimpleJList<FileContainer>(this);
		scrollPane.setViewportView(lstInput);
		lstInput.setFocusable(false);
		lstInput.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (modifying)
					return;
				if (lstInput.getSelectedIndex() >= 0) {
					btnRemoveFile.setEnabled(true);
				}
			}

		});



		pnlSteps.setLayout(gl_pnlSteps);

		JLabel lblInstructions = new JLabel("Select intermediate file folders, select state to start from, and hit 'Go'.");
		lblInstructions.setFont(new Font("PingFang TC", Font.BOLD, 14));

		GroupLayout gl_pnlInstructions = new GroupLayout(pnlInstructions);
		gl_pnlInstructions.setHorizontalGroup(
				gl_pnlInstructions.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlInstructions.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblInstructions)
						.addContainerGap(387, Short.MAX_VALUE))
				);
		gl_pnlInstructions.setVerticalGroup(
				gl_pnlInstructions.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlInstructions.createSequentialGroup()
						.addGap(6)
						.addComponent(lblInstructions)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		pnlInstructions.setLayout(gl_pnlInstructions);
		contentPanel.setLayout(gl_contentPanel);
	}

	private void removeDisplay() {

		reset();
		setVisible(false);
		mainGui.show();
	}
	
	private void startProcessing(List<FileContainer> intermediates, boolean fromObj) {

		removeDisplay();
		if (fromObj) {
			this.mainGui.getWizard().startFromObjState(intermediates);
		} else {
			this.mainGui.getWizard().startFromRoiState(intermediates);
		}
	}

	public void display(Component parent) {

		reset();
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
		repaint();

	}

	public void reset() {
		modifying = true;
		this.btnRemoveFile.setEnabled(false);
		this.btnSelectFiles.setEnabled(false);
		this.btnRun.setEnabled(false);
		this.btnsObjROI.clearSelection();
		this.lstInput.clear();

		modifying = false;
	}

	private void processFiles(List<File> files) {

		if (files == null || files.size() == 0)
			return;

		Settings settings = GUI.settings;

		List<FileContainer> serFiles = new ArrayList<FileContainer>();
		for (File file : files) {
			if (file.isDirectory()) {
				File[] possibleSerFiles = null;
				if (file.getName().equals("Serialization")) {
					possibleSerFiles = file.listFiles();
				} else if (file.getName().equals("Intermediate Files")) {
					File serFileDir = new File(file.getPath() + File.separator + "Serialization");
					if (serFileDir.exists() && serFileDir.isDirectory()) {
						possibleSerFiles = serFileDir.listFiles();
					} else {
						JOptionPane.showMessageDialog(null, "<html>The Intermediate Files folder (<em>" + file.getPath() + "</em>)<br>does not contain a 'Serialization' folder.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else {
					File serFileDir = new File(file.getPath() + File.separator + "Intermediate Files" + File.separator + "Serialization");
					if (serFileDir.exists() && serFileDir.isDirectory()) {
						possibleSerFiles = serFileDir.listFiles();
						
					} else {
						JOptionPane.showMessageDialog(null, "<html>The image output folder (<em>" + file.getPath() + "</em>) does not contain <br> a 'Serialization' folder within the 'Intermediate Files' folder.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				}

				boolean foundFile = false;
				for (File possibleSerFile : possibleSerFiles) {
					if (possibleSerFile.getName().equals("postroistate.ser")) {
						if (this.rdbtnPostObj.isSelected())
							continue;
					} else if (possibleSerFile.getName().equals("postobjstate.ser")) {
						if (this.rdbtnPostROI.isSelected())
							continue;
					} else {
						continue;
					}

					foundFile = true;
					serFiles.add(new FileContainer(possibleSerFile, true));

				}
				if (!foundFile) {
					JOptionPane.showMessageDialog(null, "<html>The folder at <em>" + file.getPath() + "</em> does not<br>contain the "+ (this.rdbtnPostObj.isSelected() ? "post-object" : "post-ROI") + " intermediate state file in its child folders.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

			} else {
				if (!file.getName().endsWith(".ser")) {
					// shouldn't happen if FileBrowser functions correctly
					JOptionPane.showMessageDialog(null, "<html>The file <em>" + file.getName() + "</em> does not have a valid extension (.ser).", "File Selection Error", JOptionPane.ERROR_MESSAGE);
					return;
				} else if (file.getName().equals("postroistate.ser")) {
					if (this.rdbtnPostObj.isSelected()) {
						JOptionPane.showMessageDialog(null, "<html>You selected a post-ROI intermediate state file (postroistate.ser) but<br>have chosen to start from the post-object state", "File Selection Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else if (file.getName().equals("postobjstate.ser")) {
					if (this.rdbtnPostROI.isSelected()) {
						JOptionPane.showMessageDialog(null, "<html>You selected a post-object intermediate state file (postobjstate.ser) but<br>have chosen to start from the post-ROI state.", "File Selection Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else {
					JOptionPane.showMessageDialog(null, "<html>The file <em>" + file.getName() + "</em> does not have a valid name (postobjstate.ser or postroistate.ser).", "File Selection Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				serFiles.add(new FileContainer(file, true));
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


		if (serFiles.isEmpty()) {
			JOptionPane.showMessageDialog(null, "<html>The selection did not yield any intermediate state files (end in .ser)", "File Selection Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		Enumeration<FileContainer> en = this.lstInput.getElements();
		while (en.hasMoreElements()) {
			FileContainer fc = en.nextElement();
			if (serFiles.contains(fc)) {
				JOptionPane.showMessageDialog(null, "<html>The file <em>" + fc.toString() + "</em> has already been added.<br><br>Duplicate files are not allowed.</html>", "File Selection Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}

		for (FileContainer fileToAdd : serFiles) {
			this.lstInput.addItem(fileToAdd);
		}
		this.btnRun.setEnabled(true);

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
