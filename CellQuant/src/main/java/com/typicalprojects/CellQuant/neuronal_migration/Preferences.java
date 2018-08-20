package com.typicalprojects.CellQuant.neuronal_migration;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringUtils;

import com.typicalprojects.CellQuant.neuronal_migration.panels.PnlSelectFiles;
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JSeparator;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JCheckBox;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextField;

public class Preferences extends JFrame {

	public enum Pref implements Comparable<Pref>{
		
		ChannelToProcess("ChProc"), ChannelForROIDraw("ChROI"), OutputLocation("O"), ChannelMapping("Chans"), LastLocation("L");
		
		private String tag;
		
		private Pref(String tag) {
			this.tag = tag;
		}
		
		public String getTag() {
			return this.tag;
		}
		
		public String toString() {
			return this.tag;
		}
		
	}
	
	private static final long serialVersionUID = 6738766494677442465L;
	private JPanel contentPane;
	private GUI mainGUI;
	private JLabel lblError;
	private JCheckBox chkB;
	private JCheckBox chkW;
	private JCheckBox chkG;
	private JCheckBox chkR;
	private JComboBox<String> comBoxCh3;
	private JComboBox<String> comBoxCh2;
	private JComboBox<String> comBoxCh1;
	private JComboBox<String> comBoxCh0;
	private JComboBox<Channel> comBoxChSelectingROI;
	private JTextField txtSavePath;

	/**
	 * Create the frame.
	 */
	public Preferences(GUI gui) {

		this.mainGUI = gui;

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent windowEvent) {

				if (JOptionPane.showConfirmDialog(null, "Do you want to apply changes?", "Exit Preferences", 
						JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
					
					String errors = null;
					try {
						errors = applyPreferences(true);
					} catch (IOException e) {
						resetPreferences();
						removeDisplay();
						JOptionPane.showMessageDialog(null, "<html>Your changes were not applied because there was an error:<br><br>Could not write to file</html>", "Error", JOptionPane.ERROR_MESSAGE);
					}
					
					if (errors != null) {
						resetPreferences();
						removeDisplay();
						JOptionPane.showMessageDialog(null, "<html>Your changes were not applied because there was an error:<br><br>"+errors+"</html>", "Error", JOptionPane.ERROR_MESSAGE);
					} else {
						removeDisplay();

					}
					
				} else {
					resetPreferences();
					removeDisplay();
				}

			}
		});


		setTitle("Preferences");
		setBounds(100, 100, 620, 400);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setVisible(true);
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);
		setVisible(false);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		JLabel label = new JLabel("<html>These preferences are stored in the settings.txt file in the folder named \"Neuronal_Migration_Resources\" within the directory of this program. If you delete this file, preferences will be lost.</html>");
		label.setFont(new Font("PingFang TC", Font.BOLD, 14));
		label.setBorder(new EmptyBorder(0, 0, 0, 0));

		GridBagConstraints gbc_label = new GridBagConstraints();
		gbc_label.insets = new Insets(0, 0, 5, 0);
		gbc_label.fill = GridBagConstraints.BOTH;
		gbc_label.gridx = 0;
		gbc_label.gridy = 0;
		contentPane.add(label, gbc_label);

		JSeparator separator = new JSeparator();
		separator.setOpaque(true);
		separator.setBounds(new Rectangle(0, 0, 1, 12));
		separator.setSize(new Dimension(1, 12));
		separator.setMinimumSize(new Dimension(600, 1));
		separator.setPreferredSize(new Dimension(1, 12));

		separator.setForeground(Color.BLACK);
		separator.setBackground(Color.BLACK);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.insets = new Insets(0, 0, 5, 0);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 1;
		contentPane.add(separator, gbc_separator);

		JLabel lblChannelConfig = new JLabel("1. Channel Configuration");
		lblChannelConfig.setFont(GUI.smallFont);
		GridBagConstraints gbc_lblChannelConfig = new GridBagConstraints();
		gbc_lblChannelConfig.insets = new Insets(0, 0, 5, 0);
		gbc_lblChannelConfig.anchor = GridBagConstraints.WEST;
		gbc_lblChannelConfig.gridx = 0;
		gbc_lblChannelConfig.gridy = 2;
		contentPane.add(lblChannelConfig, gbc_lblChannelConfig);

		JPanel pnlChannelConfig = new JPanel();
		pnlChannelConfig.setBorder(new EmptyBorder(0, 10, 0, 0));
		GridBagConstraints gbc_pnlChannelConfig = new GridBagConstraints();
		gbc_pnlChannelConfig.insets = new Insets(0, 0, 5, 0);
		gbc_pnlChannelConfig.fill = GridBagConstraints.BOTH;
		gbc_pnlChannelConfig.gridx = 0;
		gbc_pnlChannelConfig.gridy = 3;
		contentPane.add(pnlChannelConfig, gbc_pnlChannelConfig);

		JLabel lblChan0 = new JLabel("Chan 0:");
		lblChan0.setFont(GUI.smallPlainFont);
		
		List<String> values = new ArrayList<String>();
		for (Channel chan : Channel.values()) {
			values.add(chan.toString());
		}
		values.add("None");
		comBoxCh0 = new JComboBox<String>();
		comBoxCh0.setFocusable(false);
		comBoxCh0.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));

		JLabel lblChan1 = new JLabel("Chan 1:");
		lblChan1.setFont(GUI.smallPlainFont);

		comBoxCh1 = new JComboBox<String>();
		comBoxCh1.setFocusable(false);
		comBoxCh1.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));

		JLabel lblChan2 = new JLabel("Chan 2:");
		lblChan2.setFont(GUI.smallPlainFont);

		comBoxCh2 = new JComboBox<String>();
		comBoxCh2.setFocusable(false);
		comBoxCh2.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));


		JLabel lblChan3 = new JLabel("Chan 3:");
		lblChan3.setFont(GUI.smallPlainFont);

		comBoxCh3 = new JComboBox<String>();
		comBoxCh3.setFocusable(false);
		comBoxCh3.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));

		JLabel lblChannelProcess = new JLabel("Channels to Process:");
		lblChannelProcess.setFont(GUI.smallPlainFont);

		chkG = new JCheckBox("G");
		chkG.setFocusable(false);
		if (GUI.channelsToProcess.contains(Channel.GREEN)) {
			chkG.setSelected(true);
		}

		chkW = new JCheckBox("W");
		chkW.setFocusable(false);
		if (GUI.channelsToProcess.contains(Channel.WHITE)) {
			chkW.setSelected(true);
		}

		chkR = new JCheckBox("R");
		chkR.setFocusable(false);
		if (GUI.channelsToProcess.contains(Channel.RED)) {
			chkR.setSelected(true);
		}

		chkB = new JCheckBox("B");
		chkB.setFocusable(false);
		if (GUI.channelsToProcess.contains(Channel.BLUE)) {
			chkB.setSelected(true);
		}

		JLabel lblChannelSelectROI = new JLabel("Channel to Select ROIs");
		lblChannelSelectROI.setFont(GUI.smallPlainFont);

		comBoxChSelectingROI = new JComboBox<Channel>();
		comBoxChSelectingROI.setFocusable(false);
		comBoxChSelectingROI.setModel(new DefaultComboBoxModel<Channel>(Channel.values()));
		comBoxChSelectingROI.setSelectedItem(GUI.channelForROIDraw);
		GroupLayout gl_pnlChannelConfig = new GroupLayout(pnlChannelConfig);
		gl_pnlChannelConfig.setHorizontalGroup(
				gl_pnlChannelConfig.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlChannelConfig.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pnlChannelConfig.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_pnlChannelConfig.createSequentialGroup()
										.addComponent(lblChan0)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(comBoxCh0, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
										.addGap(4)
										.addComponent(lblChan1)
										.addGap(5)
										.addComponent(comBoxCh1, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(lblChan2)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(comBoxCh2, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE)
										.addGap(3)
										.addComponent(lblChan3)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(comBoxCh3, GroupLayout.PREFERRED_SIZE, 85, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_pnlChannelConfig.createSequentialGroup()
										.addComponent(lblChannelProcess)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(chkG)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(chkW)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(chkR)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(chkB))
								.addGroup(gl_pnlChannelConfig.createSequentialGroup()
										.addComponent(lblChannelSelectROI)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(comBoxChSelectingROI, GroupLayout.PREFERRED_SIZE, 90, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		gl_pnlChannelConfig.setVerticalGroup(
				gl_pnlChannelConfig.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlChannelConfig.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pnlChannelConfig.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblChan0)
								.addComponent(comBoxCh0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblChan1)
								.addComponent(comBoxCh1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblChan2)
								.addComponent(comBoxCh2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblChan3)
								.addComponent(comBoxCh3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_pnlChannelConfig.createParallelGroup(Alignment.LEADING)
								.addComponent(lblChannelProcess)
								.addGroup(gl_pnlChannelConfig.createParallelGroup(Alignment.BASELINE)
										.addComponent(chkG)
										.addComponent(chkW)
										.addComponent(chkR)
										.addComponent(chkB)))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_pnlChannelConfig.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblChannelSelectROI)
								.addComponent(comBoxChSelectingROI, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addContainerGap(34, Short.MAX_VALUE))
				);
		pnlChannelConfig.setLayout(gl_pnlChannelConfig);

		lblError = new JLabel("Error");
		lblError.setVisible(false);
		
		JLabel lblSaveLocation = new JLabel("2. Save Location");
		lblSaveLocation.setFont(GUI.smallFont);
		GridBagConstraints gbc_lblSaveLocation = new GridBagConstraints();
		gbc_lblSaveLocation.anchor = GridBagConstraints.WEST;
		gbc_lblSaveLocation.insets = new Insets(0, 0, 5, 0);
		gbc_lblSaveLocation.gridx = 0;
		gbc_lblSaveLocation.gridy = 4;
		contentPane.add(lblSaveLocation, gbc_lblSaveLocation);
		
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(0, 10, 0, 0));
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.NORTH;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 5;
		contentPane.add(panel, gbc_panel);
		
		JLabel lblSavePath = new JLabel("Path:");
		lblSavePath.setFont(GUI.smallPlainFont);
		
		txtSavePath = new JTextField();
		txtSavePath.setEditable(false);
		txtSavePath.setColumns(10);
		
		JButton btnBrowseSavePath = new JButton("Browse...");
		btnBrowseSavePath.setFocusable(false);
		btnBrowseSavePath.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (PnlSelectFiles.isMac()) {
					System.setProperty("apple.awt.fileDialogForDirectories", "true"); 

					FileDialog fd = new FileDialog(gui.getComponent());
					fd.setMode(FileDialog.LOAD);
					fd.setMultipleMode(false);
					fd.setVisible(true);


					File[] file = fd.getFiles();
					if (file != null && file.length != 0) {
						File oldFile = GUI.outputLocation;
						try {

							
							GUI.outputLocation = file[0];
							writeSettingsFromGUI();
							
							txtSavePath.setText(file[0].getPath());

						} catch (IOException exception) {
							GUI.outputLocation = oldFile;
							JOptionPane.showConfirmDialog(null, "Could not save settings file.", "Error Saving.", JOptionPane.ERROR_MESSAGE);
						}
					}
					System.setProperty("apple.awt.fileDialogForDirectories", "false"); 
				} else {
					JFileChooser fChoos = new JFileChooser();

					fChoos.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					
					fChoos.showOpenDialog(null);
					File file = fChoos.getSelectedFile();
					if (file != null) {
						File oldFile = GUI.outputLocation;
						try {

							
							GUI.outputLocation = file;
							writeSettingsFromGUI();
							
							txtSavePath.setText(file.getPath());

						} catch (IOException exception) {
							GUI.outputLocation = oldFile;
							JOptionPane.showConfirmDialog(null, "Could not save settings file.", "Error Saving.", JOptionPane.ERROR_MESSAGE);
						}
					}
				}
				

			}
		});
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(lblSavePath)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtSavePath, GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnBrowseSavePath))
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblSavePath)
						.addComponent(txtSavePath, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(btnBrowseSavePath))
					.addContainerGap(22, Short.MAX_VALUE))
		);
		panel.setLayout(gl_panel);
		lblError.setFont(GUI.smallFont);
		lblError.setForeground(Color.RED);
		GridBagConstraints gbc_lblError = new GridBagConstraints();
		gbc_lblError.insets = new Insets(0, 0, 5, 0);
		gbc_lblError.gridx = 0;
		gbc_lblError.gridy = 6;
		contentPane.add(lblError, gbc_lblError);

		JPanel panel_1 = new JPanel();
		panel_1.setMinimumSize(new Dimension(10, 40));
		panel_1.setBorder(null);
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 7;
		contentPane.add(panel_1, gbc_panel_1);

		JButton btnApplyClose = new JButton("Apply and Close");
		btnApplyClose.setFocusCycleRoot(true);
		btnApplyClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String errors = null;
				try {
					errors = applyPreferences(false);
				} catch (IOException er) {
					lblError.setText("Error: Trouble saving settings to file.");
					lblError.setVisible(true);

				}
				
				if (errors != null) {
					lblError.setText(errors);
					lblError.setVisible(true);

				} else {
					removeDisplay();
				}
				
				

			}
		});
		btnApplyClose.setHorizontalAlignment(SwingConstants.RIGHT);

		JButton btnNewButton_1 = new JButton("Cancel");
		btnNewButton_1.setFocusable(false);
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				resetPreferences();
				removeDisplay();

			}
		});
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel_1.createSequentialGroup()
					.addContainerGap(346, Short.MAX_VALUE)
					.addComponent(btnNewButton_1)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnApplyClose)
					.addContainerGap())
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
						.addComponent(btnNewButton_1)
						.addComponent(btnApplyClose))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panel_1.setLayout(gl_panel_1);

		java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get (key);
			if (value != null && value instanceof java.awt.Font)
				UIManager.put (key, GUI.smallFont);
		}
	}

	public void removeDisplay() {
		setVisible(false);
		lblError.setVisible(false);
		mainGUI.refocus();
	}

	public void display(Component parent) {
		resetPreferences();
		setLocationRelativeTo(parent);
		setVisible(true);
	}
	
	public static void readSettingsIntoGUI() throws IOException{
		
		GUI.channelMap.clear();
		GUI.channelsToProcess.clear();
		List<Pref> prefs = new LinkedList<Pref>(Arrays.asList(Pref.ChannelMapping, Pref.ChannelToProcess, Pref.ChannelForROIDraw, Pref.OutputLocation, Pref.LastLocation));

		Scanner scanner = new Scanner(new File(GUI.folderName + File.separator + GUI.settingsFile));		
		scanner.nextLine(); // get past the do not touch
		
		Map<String, String> keyValues = new HashMap<String, String>();
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.equals(""))
				continue;
			String[] pieces = line.split(":",2);
			keyValues.put(pieces[0], pieces[1]);
		}
		
		scanner.close();
		
		Iterator<Pref> prefsItr = prefs.iterator();
		while (prefsItr.hasNext()) {
			Pref pref = prefsItr.next();
			
			if (!keyValues.containsKey(pref.tag)) {
				continue;
			}
			prefsItr.remove();
			
			switch (pref) {
			case ChannelMapping:
				String[] prefsChannelMap = keyValues.get(pref.tag).split(":");
				for (int i = 0; i < prefsChannelMap.length; i++) {
					String[] channelMapping = prefsChannelMap[i].split("-");
					if (channelMapping[1].equals("None")) {
						continue;
					}
					GUI.channelMap.put(Integer.valueOf(channelMapping[0]), Channel.getChannelByAbbreviation(channelMapping[1]));
				}
				break;
			case ChannelToProcess:
				String[] prefsChannelProc = keyValues.get(pref.tag).split(",");
				for (int i = 0; i < prefsChannelProc.length; i++) {
					GUI.channelsToProcess.add(Channel.getChannelByAbbreviation(prefsChannelProc[i]));
				}
				break;
			case ChannelForROIDraw:
				GUI.channelForROIDraw = Channel.getChannelByAbbreviation(keyValues.get(pref.tag));
				break;
			case OutputLocation:
				String stringOutputLocation = keyValues.get(pref.tag);
				if (stringOutputLocation.equals("-")) {
					GUI.outputLocation = null;
				} else {
					File file = new File(stringOutputLocation);
					if (file.exists() && file.isDirectory()) {
						GUI.outputLocation = file;
					} else {
						GUI.outputLocation = null;
						Preferences.writeSettingsFromGUI();
					}
				}
				break;
			case LastLocation:
				String stringLastLocation = keyValues.get(pref.tag);
				if (stringLastLocation.equals("-")) {
					GUI.lastSelectFileLocation = null;
				} else {
					File file = new File(stringLastLocation);
					if (file.exists() && file.isDirectory()) {
						GUI.lastSelectFileLocation = file;
					} else {
						GUI.lastSelectFileLocation = null;
						Preferences.writeSettingsFromGUI();
					}
				}
				break;
			}
			
		}
		
		if (!prefs.isEmpty()) {
			for (Pref pref : prefs) {
				switch (pref) {
				case ChannelMapping:
					GUI.channelMap.put(0, Channel.GREEN);
					GUI.channelMap.put(1, Channel.WHITE);
					GUI.channelMap.put(2, Channel.RED);
					GUI.channelMap.put(3, Channel.BLUE);
					break;
				case ChannelToProcess:
					GUI.channelsToProcess.add(Channel.RED);
					GUI.channelsToProcess.add(Channel.GREEN);
					break;
				case ChannelForROIDraw:
					GUI.channelForROIDraw = Channel.BLUE;
					break;
				case OutputLocation:
					GUI.outputLocation = null;
					break;
				case LastLocation:
					GUI.lastSelectFileLocation = null;
					break;
				}
			}
			
			writeSettingsFromGUI();
		}

		
	}

	public String applyPreferences(boolean resetIfIncorrect) throws IOException {
		
		List<String> values = new ArrayList<String>();
		for (Channel chan : Channel.values()) {
			values.add(chan.toString());
		}
		values.add("None");
		values.add("None");
		values.add("None");
		values.add("None");
		if (!values.remove(this.comBoxCh0.getSelectedItem()) || !values.remove(this.comBoxCh1.getSelectedItem()) ||
				!values.remove(this.comBoxCh2.getSelectedItem()) || !values.remove(this.comBoxCh3.getSelectedItem())) {
			if (resetIfIncorrect) resetPreferences();
			return "Incorrect channel config. Each channel # must have a unique color.";
		} else if (!this.chkB.isSelected() && !this.chkG.isSelected() && !this.chkR.isSelected() && !this.chkW.isSelected()) {
			if (resetIfIncorrect) resetPreferences();
			return "Incorrect channel config. At least 1 one channel must be processed.";
		}
		
		GUI.channelsToProcess.clear();
		GUI.channelForROIDraw = (Channel) this.comBoxChSelectingROI.getSelectedItem();
		Map<Integer, Channel> newChanMap = new HashMap<Integer, Channel>();
		if (!this.comBoxCh0.getSelectedItem().equals("None")) {
			newChanMap.put(0, Channel.getChannelByAbbreviation((String) this.comBoxCh0.getSelectedItem()));
		}
		if (!this.comBoxCh1.getSelectedItem().equals("None")) {
			newChanMap.put(1, Channel.getChannelByAbbreviation((String) this.comBoxCh1.getSelectedItem()));
		}
		if (!this.comBoxCh2.getSelectedItem().equals("None")) {
			newChanMap.put(2, Channel.getChannelByAbbreviation((String) this.comBoxCh2.getSelectedItem()));
		}
		if (!this.comBoxCh3.getSelectedItem().equals("None")) {
			newChanMap.put(3, Channel.getChannelByAbbreviation((String) this.comBoxCh3.getSelectedItem()));
		}
		
		if (this.chkB.isSelected() && GUI.channelMap.containsValue(Channel.BLUE)) {
			GUI.channelsToProcess.add(Channel.BLUE);
		}
		if (this.chkR.isSelected() && GUI.channelMap.containsValue(Channel.RED)) {
			GUI.channelsToProcess.add(Channel.RED);
		}
		if (this.chkG.isSelected() && GUI.channelMap.containsValue(Channel.GREEN)) {
			GUI.channelsToProcess.add(Channel.GREEN);
		}
		if (this.chkW.isSelected() && GUI.channelMap.containsValue(Channel.WHITE)) {
			GUI.channelsToProcess.add(Channel.WHITE);
		}
		
		if (!newChanMap.values().contains(GUI.channelForROIDraw)) {
			return "The channel chosen for drawing ROIs was not assigned a number.";
		}
		
		GUI.channelMap.clear();
		GUI.channelMap.putAll(newChanMap);
		
		writeSettingsFromGUI();
		
		return null;


	}

	public void resetPreferences() {
		if (GUI.channelMap.get(0) == null) {
			this.comBoxCh0.setSelectedItem("None");
		} else {
			this.comBoxCh0.setSelectedItem(GUI.channelMap.get(0).getAbbreviation());

		}
		
		if (GUI.channelMap.get(1) == null) {
			this.comBoxCh1.setSelectedItem("None");
		} else {
			this.comBoxCh1.setSelectedItem(GUI.channelMap.get(1).getAbbreviation());

		}
		
		if (GUI.channelMap.get(2) == null) {
			this.comBoxCh2.setSelectedItem("None");
		} else {
			this.comBoxCh2.setSelectedItem(GUI.channelMap.get(2).getAbbreviation());

		}
		
		if (GUI.channelMap.get(3) == null) {
			this.comBoxCh3.setSelectedItem("None");
		} else {
			this.comBoxCh3.setSelectedItem(GUI.channelMap.get(3).getAbbreviation());

		}
		this.chkG.setSelected(GUI.channelsToProcess.contains(Channel.GREEN));
		this.chkR.setSelected(GUI.channelsToProcess.contains(Channel.RED));
		this.chkW.setSelected(GUI.channelsToProcess.contains(Channel.WHITE));
		this.chkB.setSelected(GUI.channelsToProcess.contains(Channel.BLUE));
		this.comBoxChSelectingROI.setSelectedItem(GUI.channelForROIDraw);
		if (GUI.outputLocation == null) {
			this.txtSavePath.setText("");
		} else {
			this.txtSavePath.setText(GUI.outputLocation.getPath());
		}

	}
	
	public boolean isDisplaying() {
		return this.isVisible();
	}
	
	public static void writeSettingsFromGUI() throws IOException {
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(GUI.folderName + File.separator + GUI.settingsFile)));
		
		StringBuilder sb = new StringBuilder();
		sb.append("DO NOT TOUCH");
		sb.append("\n").append(Pref.ChannelMapping.tag);
		for (int i = 0; i < 4; i++) {
			sb.append(":").append(i).append("-").append(GUI.channelMap.containsKey(i) ? GUI.channelMap.get(i).getAbbreviation() : "None");
		}
		sb.append("\n").append(Pref.ChannelToProcess.tag).append(":").append(StringUtils.join(GUI.channelsToProcess, ","));
		sb.append("\n").append(Pref.ChannelForROIDraw.tag).append(":").append(GUI.channelForROIDraw.getAbbreviation());
		sb.append("\n").append(Pref.OutputLocation.tag).append(":").append(GUI.outputLocation == null ? "-" : GUI.outputLocation.getPath());
		sb.append("\n").append(Pref.LastLocation.tag).append(":").append(GUI.lastSelectFileLocation == null ? "-" : GUI.lastSelectFileLocation.getPath());
		writer.write(sb.toString());
		
		writer.close();

	}
	
}
