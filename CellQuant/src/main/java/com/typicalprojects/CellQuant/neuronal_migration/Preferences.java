package com.typicalprojects.CellQuant.neuronal_migration;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.apache.commons.lang.StringUtils;

import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.JSeparator;
import java.awt.Insets;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JCheckBox;
import java.awt.Font;

import javax.swing.DefaultComboBoxModel;

public class Preferences extends JFrame {


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

	/**
	 * Create the frame.
	 */
	public Preferences(GUI gui) {

		this.mainGUI = gui;

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				setAlwaysOnTop(false);

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
					}
					
				} else {
					resetPreferences();
					removeDisplay();
				}

				setAlwaysOnTop(true);
			}
		});


		setTitle("Preferences");
		setBounds(100, 100, 600, 350);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setVisible(true);
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);
		setVisible(false);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
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
		separator.setMinimumSize(new Dimension(1, 12));
		separator.setPreferredSize(new Dimension(1, 12));

		separator.setForeground(Color.BLACK);
		separator.setBackground(Color.BLACK);
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.insets = new Insets(0, 0, 5, 0);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 1;
		contentPane.add(separator, gbc_separator);

		JLabel lblChannelConfig = new JLabel("1. Channel Configuration");
		lblChannelConfig.setFont(new Font("PingFang TC", Font.BOLD, 13));
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
		lblChan0.setFont(new Font("PingFang TC", Font.PLAIN, 13));
		
		List<String> values = new ArrayList<String>();
		for (Channel chan : Channel.values()) {
			values.add(chan.toString());
		}
		values.add("None");
		comBoxCh0 = new JComboBox<String>();
		comBoxCh0.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));
		comBoxCh0.setSelectedItem(values.get(0));

		JLabel lblChan1 = new JLabel("Chan 1:");
		lblChan1.setFont(new Font("PingFang TC", Font.PLAIN, 13));

		comBoxCh1 = new JComboBox<String>();
		comBoxCh1.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));
		comBoxCh1.setSelectedItem(values.get(1));

		JLabel lblChan2 = new JLabel("Chan 2:");
		lblChan2.setFont(new Font("PingFang TC", Font.PLAIN, 13));

		comBoxCh2 = new JComboBox<String>();
		comBoxCh2.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));
		comBoxCh2.setSelectedItem(values.get(2));


		JLabel lblChan3 = new JLabel("Chan 3:");
		lblChan3.setFont(new Font("PingFang TC", Font.PLAIN, 13));

		comBoxCh3 = new JComboBox<String>();
		comBoxCh3.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));
		comBoxCh3.setSelectedItem(values.get(3));

		JLabel lblChannelProcess = new JLabel("Channels to Process:");
		lblChannelProcess.setFont(new Font("PingFang TC", Font.PLAIN, 13));

		chkG = new JCheckBox("G");
		if (GUI.channelsToProcess.contains(Channel.GREEN)) {
			chkG.setSelected(true);
		}

		chkW = new JCheckBox("W");
		if (GUI.channelsToProcess.contains(Channel.WHITE)) {
			chkW.setSelected(true);
		}

		chkR = new JCheckBox("R");
		if (GUI.channelsToProcess.contains(Channel.RED)) {
			chkR.setSelected(true);
		}

		chkB = new JCheckBox("B");
		if (GUI.channelsToProcess.contains(Channel.BLUE)) {
			chkB.setSelected(true);
		}

		JLabel lblChannelSelectROI = new JLabel("Channel to Select ROIs");
		lblChannelSelectROI.setFont(new Font("PingFang TC", Font.PLAIN, 13));

		comBoxChSelectingROI = new JComboBox<Channel>();
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
		lblError.setFont(GUI.smallFont);
		lblError.setForeground(Color.RED);
		GridBagConstraints gbc_lblError = new GridBagConstraints();
		gbc_lblError.insets = new Insets(0, 0, 5, 0);
		gbc_lblError.gridx = 0;
		gbc_lblError.gridy = 5;
		contentPane.add(lblError, gbc_lblError);

		JPanel panel_1 = new JPanel();
		panel_1.setMinimumSize(new Dimension(10, 5));
		panel_1.setBorder(null);
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 6;
		contentPane.add(panel_1, gbc_panel_1);

		JButton btnApplyClose = new JButton("Apply and Close");
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
		panel_1.add(btnApplyClose);

		JButton btnNewButton_1 = new JButton("Cancel");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				resetPreferences();
				removeDisplay();

			}
		});
		panel_1.add(btnNewButton_1);
		setAlwaysOnTop(true);

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
		
		GUI.channelMap.clear();
		GUI.channelsToProcess.clear();
		GUI.channelForROIDraw = (Channel) this.comBoxChSelectingROI.getSelectedItem();
		if (!this.comBoxCh0.getSelectedItem().equals("None")) {
			GUI.channelMap.put(0, Channel.getChannelByAbbreviation((String) this.comBoxCh0.getSelectedItem()));
		}
		if (!this.comBoxCh1.getSelectedItem().equals("None")) {
			GUI.channelMap.put(1, Channel.getChannelByAbbreviation((String) this.comBoxCh1.getSelectedItem()));
		}
		if (!this.comBoxCh2.getSelectedItem().equals("None")) {
			GUI.channelMap.put(2, Channel.getChannelByAbbreviation((String) this.comBoxCh2.getSelectedItem()));
		}
		if (!this.comBoxCh3.getSelectedItem().equals("None")) {
			GUI.channelMap.put(3, Channel.getChannelByAbbreviation((String) this.comBoxCh3.getSelectedItem()));
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
		
		if (!GUI.channelMap.values().contains(GUI.channelForROIDraw)) {
			return "The channel chosen for drawing ROIs was not assigned a number.";
		}

		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(GUI.folderName + File.separator + GUI.settingsFile)));
		writer.write("DO NOT TOUCH\nChans:0-"+ (String) this.comBoxCh0.getSelectedItem() + ":1-"
		+ (String) this.comBoxCh1.getSelectedItem() +":2-" 
				+ (String) this.comBoxCh2.getSelectedItem() +":3-" 
		+(String) this.comBoxCh3.getSelectedItem() +"\nChProc:"+StringUtils.join(GUI.channelsToProcess, ",") + "\nChROI:" + GUI.channelForROIDraw.getAbbreviation());

		writer.close();
		
		return null;


	}

	public void resetPreferences() {
		if (GUI.channelMap.get(0) == null) {
			this.comBoxCh0.setSelectedItem("None");
		} else {
			this.comBoxCh0.setSelectedItem(GUI.channelMap.get(0));

		}
		
		if (GUI.channelMap.get(1) == null) {
			this.comBoxCh0.setSelectedItem("None");
		} else {
			this.comBoxCh0.setSelectedItem(GUI.channelMap.get(1));

		}
		
		if (GUI.channelMap.get(2) == null) {
			this.comBoxCh0.setSelectedItem("None");
		} else {
			this.comBoxCh0.setSelectedItem(GUI.channelMap.get(2));

		}
		
		if (GUI.channelMap.get(3) == null) {
			this.comBoxCh0.setSelectedItem("None");
		} else {
			this.comBoxCh0.setSelectedItem(GUI.channelMap.get(3));

		}
		this.chkG.setSelected(GUI.channelsToProcess.contains(Channel.GREEN));
		this.chkR.setSelected(GUI.channelsToProcess.contains(Channel.RED));
		this.chkW.setSelected(GUI.channelsToProcess.contains(Channel.WHITE));
		this.chkB.setSelected(GUI.channelsToProcess.contains(Channel.BLUE));
		this.comBoxChSelectingROI.setSelectedItem(GUI.channelForROIDraw);

	}

}
