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

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.SpinnerNumberModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.typicalprojects.TronMachine.neuronal_migration.Preferences2.SettingPage;
import com.typicalprojects.TronMachine.neuronal_migration.Preferences2.SettingsPanel;
import com.typicalprojects.TronMachine.neuronal_migration.Settings.SettingsLoader;
import com.typicalprojects.TronMachine.util.FileBrowser;
import com.typicalprojects.TronMachine.util.SimpleJList;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;

public class Preferences2 extends JFrame {


	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					try {
						UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException e1) {
						e1.printStackTrace();
					}

					Preferences2 frame = new Preferences2(null);
					frame.display(null, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

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
	private JPanel activePanel;
	private GUI mainGUI;
	private JLabel lblPageName;

	private JButton btnApplyAndClose;
	private JButton btnCancel;
	private JLabel lblCannotEdit;
	private GroupLayout gl_contentPane;
	private SimpleJList<SettingPage> menuList;
	private List<SettingsPanel> settingsPanel = new ArrayList<SettingsPanel>();
	private boolean listSelectionChanging = false;
	public static Preferences2 SINGLETON_FRAME = null;

	/**
	 * Create the frame.
	 */
	public Preferences2(GUI gui) {

		SINGLETON_FRAME = this;
		this.mainGUI = gui;

		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent windowEvent) {

				if (JOptionPane.showConfirmDialog(null, "Do you want to apply changes?", "Exit Preferences", 
						JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {

					userTriedToApplyChanges();

				} else {
					resetPreferences(true);
					removeDisplay();
				}

			}
		});


		setTitle("Preferences");
		setBounds(100, 100, 700, 500);
		setResizable(false);
		contentPane = new JPanel();
		contentPane.setVisible(true);
		contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
		setContentPane(contentPane);

		JScrollPane scrollPane = new JScrollPane();


		JPanel pnlSepBottom = new JPanel();
		pnlSepBottom.setBorder(new LineBorder(Color.GRAY));

		JPanel pnlSepMiddle = new JPanel();
		pnlSepMiddle.setBorder(new LineBorder(Color.GRAY));

		this.settingsPanel.add(new PnlChanOptions());
		this.settingsPanel.add(new PnlProcessingOptions());
		this.settingsPanel.add(new PnlBinOptions());
		this.settingsPanel.add(new PnlSaveOptions());
		this.settingsPanel.add(new PnlImageOptions());
		this.settingsPanel.add(new PnlReset(this));


		lblCannotEdit = new JLabel("There were errors in your configuration.");
		lblCannotEdit.setForeground(Color.RED);
		lblCannotEdit.setFont(GUI.mediumFont);

		btnApplyAndClose = new JButton("Apply and Close");
		btnApplyAndClose.setEnabled(false);

		btnApplyAndClose.setFocusCycleRoot(true);
		btnApplyAndClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				userTriedToApplyChanges();

			}
		});
		btnApplyAndClose.setHorizontalAlignment(SwingConstants.RIGHT);

		btnCancel = new JButton("Cancel");
		btnCancel.setFocusable(false);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				resetPreferences(true);
				removeDisplay();

			}
		});

		JPanel pnlSepTop = new JPanel();
		pnlSepTop.setBorder(new LineBorder(Color.GRAY));

		JPanel pnlPageName = new JPanel();

		this.activePanel = this.settingsPanel.get(0).getRawComponent();
		gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
				gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(lblCannotEdit, GroupLayout.DEFAULT_SIZE, 451, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnCancel)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnApplyAndClose))
				.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 160, GroupLayout.PREFERRED_SIZE)
						.addComponent(pnlSepMiddle, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(pnlSepTop, GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
								.addComponent(this.activePanel, GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
								.addComponent(pnlPageName, GroupLayout.DEFAULT_SIZE, 512, Short.MAX_VALUE)
								))
				.addComponent(pnlSepBottom, GroupLayout.DEFAULT_SIZE, 680, Short.MAX_VALUE)
				);
		gl_contentPane.setVerticalGroup(
				gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addComponent(pnlSepMiddle, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
								.addComponent(scrollPane, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 419, Short.MAX_VALUE)
								.addGroup(gl_contentPane.createSequentialGroup()
										.addComponent(pnlPageName, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
										.addComponent(pnlSepTop, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
										.addComponent(this.activePanel, GroupLayout.DEFAULT_SIZE, 384, Short.MAX_VALUE)))
						.addComponent(pnlSepBottom, GroupLayout.PREFERRED_SIZE, 2, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnApplyAndClose)
								.addComponent(lblCannotEdit, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
								.addComponent(btnCancel)))
				);
		menuList = new SimpleJList<SettingPage>();
		menuList.addItems(SettingPage.values());
		scrollPane.setViewportView(menuList);
		menuList.setSelectedIndex(SettingPage.ChannelConfiguration.ordinal());
		menuList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() && !listSelectionChanging) {
					SettingPage sp = menuList.getSelectedValue();
					for (SettingsPanel panel : settingsPanel) {
						if (panel.getPageDesignation().equals(sp))
							setCurrentPage(panel);
					}
				}

			}
		});


		lblPageName = new JLabel("Channel Configuration");
		lblPageName.setFont(new Font("PingFang TC", Font.BOLD | Font.ITALIC, 15));
		GroupLayout gl_pnlPageName = new GroupLayout(pnlPageName);
		gl_pnlPageName.setHorizontalGroup(
				gl_pnlPageName.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlPageName.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblPageName, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlPageName.setVerticalGroup(
				gl_pnlPageName.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlPageName.createSequentialGroup()
						.addGap(4)
						.addComponent(lblPageName)
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		pnlPageName.setLayout(gl_pnlPageName);

		contentPane.setLayout(gl_contentPane);



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
		mainGUI.refocus();
	}

	public void setEnabled(boolean enabled) {
		if (enabled) {
			this.btnApplyAndClose.setEnabled(true);
			if (this.lblCannotEdit.getText().equals("To make changes you must cancel the current run.")) {
				this.lblCannotEdit.setVisible(false);
			}
		} else {
			this.btnApplyAndClose.setEnabled(false);
			this.lblCannotEdit.setText("To make changes you must cancel the current run.");
			this.lblCannotEdit.setVisible(true);
		}
	}

	public void display(Component parent, boolean running) {
		removeErrorMessages();
		setCurrentPage(this.settingsPanel.get(0));
		setEnabled(!running);
		resetPreferences(!running);
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	public Object[] applyPreferences(boolean resetIfIncorrect) {

		Settings settings = getSettings();


		settings.needsUpdate = true;

		for (SettingsPanel settingsPanel : settingsPanel) {
			if (!settingsPanel.applyFields(GUI.settings)) {
				return new Object[] {settingsPanel, ""};
			}
		}

		try {
			SettingsLoader.saveSettings(settings);
			settings.needsUpdate = false;
			return null;
		} catch (Exception e) {
			return new Object[] {null, "<html>The settings could not be saved:<br><br>" + e.getMessage() + "</html>"};
		}



	}

	public void resetPreferences(boolean enabled) {

		for (SettingsPanel panel : this.settingsPanel) {
			panel.reset(GUI.settings, enabled);
		}

	}

	protected void invokeReset(boolean enabled) {
		for (SettingsPanel panel : this.settingsPanel) {
			panel.reset(GUI.settings, enabled);
		}
	}

	public boolean isDisplaying() {
		return this.isVisible();
	}

	private void displayError() {
		this.lblCannotEdit.setText("There were errors in your configuration.");
		this.lblCannotEdit.setVisible(true);
	}

	public void removeErrorMessages() {
		if (!this.lblCannotEdit.getText().equals("To make changes you must cancel the current run.")) {
			this.lblCannotEdit.setVisible(false);
		}

		for (SettingsPanel panel : this.settingsPanel) {
			panel.removeError();
		}

	}


	public void setCurrentPage(SettingsPanel settings) {
		this.listSelectionChanging = true;
		gl_contentPane.replace(this.activePanel, settings.getRawComponent());
		this.lblPageName.setText(settings.getPageDesignation().friendly());
		this.menuList.setSelectedValue(settings.getPageDesignation(), true);
		this.activePanel = settings.getRawComponent();
		this.listSelectionChanging = false;

	}

	public Settings getSettings() {
		return GUI.settings;
	}

	private void userTriedToApplyChanges() {
		removeErrorMessages();

		Object[] errors = applyPreferences(false);;

		if (errors != null) {
			displayError();

			if (errors[0] == null) {
				JOptionPane.showMessageDialog(contentPane, "<html>Failure writing preferences:<br><br>" + errors[1] + "</html>", "I/O Failure", JOptionPane.ERROR_MESSAGE);
			} else {
				setCurrentPage((SettingsPanel) errors[0]);
			}	

		} else {
			removeDisplay();
		}
	}

	public enum SettingPage {
		ChannelConfiguration("Channel Setup"), ImageSettings("Image Settings"), Processing("Processing"), BinConfiguration("Bins"), Saving("Save Options"), Reset("Reset");

		private String friendlyName;

		private SettingPage(String friendlyName) {
			this.friendlyName = friendlyName;
		}

		public String friendly() {
			return this.friendlyName;
		}

		public String toString() {
			return this.friendlyName;
		}

	}

	public interface SettingsPanel {
		public void removeError();
		public void reset(Settings settings, boolean enabled);
		public boolean applyFields(Settings settings);
		public void displayError(String errors);
		public SettingPage getPageDesignation();
		public JPanel getRawComponent();
	}

}

@SuppressWarnings("rawtypes")
class ChannelRenderer<K> implements ListCellRenderer {

	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	private static final Color normalColor = Color.BLACK;



	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {



		JLabel renderer = (JLabel) defaultRenderer
				.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
		String valueString = (String) value;
		if (valueString.equals("<none>")) {
			renderer.setForeground(normalColor);
		} else {
			for (Channel chan : Channel.values()) {
				if (chan.toReadableString().equalsIgnoreCase(valueString)) {
					renderer.setForeground(chan.getColor());
					break;
				}
			}
		}

		return renderer;


	}



}
class PnlChanOptions extends JPanel implements SettingsPanel {


	private static final long serialVersionUID = 1423951480886612209L;
	private JCheckBox chkSelectGreen;
	private JCheckBox chkSelectRed;
	private JCheckBox chkSelectBlue;
	private JCheckBox chkSelectWhite;
	private JComboBox<String> comBoxCh3;
	private JComboBox<String> comBoxCh2;
	private JComboBox<String> comBoxCh1;
	private JComboBox<String> comBoxCh0;
	private JComboBox<String> comBoxChanROI;
	private JLabel lblError;
	private static final SettingPage setttingPage = SettingPage.ChannelConfiguration;

	@SuppressWarnings("unchecked")
	public PnlChanOptions() {

		JPanel pnlChanMap = new JPanel();
		pnlChanMap.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlChanMap.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlChanMap.setBackground(new Color(211, 211, 211));

		JLabel lblChanMap0 = new JLabel("Chan 0:");

		JLabel lblChanMap1 = new JLabel("Chan 1:");

		JLabel lblChanMap2 = new JLabel("Chan 2:");

		JLabel lblChanMap3 = new JLabel("Chan 3:");

		List<String> values = new ArrayList<String>();
		for (Channel chan : Channel.values()) {
			values.add(chan.toReadableString());
		}

		ChannelRenderer<String> chanRend = new ChannelRenderer<String>();
		values.add("<none>");
		comBoxCh0 = new JComboBox<String>();

		comBoxCh0.setRenderer(chanRend);
		comBoxCh0.setFocusable(false);
		comBoxCh0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeComboxColor(comBoxCh0, (String) comBoxCh0.getSelectedItem());
			}
		});
		comBoxCh0.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));
		comBoxCh0.setSelectedIndex(0);

		JLabel lblChan1 = new JLabel("Chan 1:");
		lblChan1.setFont(GUI.smallPlainFont);

		comBoxCh1 = new JComboBox<String>();
		comBoxCh1.setRenderer(chanRend);
		comBoxCh1.setFocusable(false);
		comBoxCh1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeComboxColor(comBoxCh1, (String) comBoxCh1.getSelectedItem());
			}
		});
		comBoxCh1.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));

		JLabel lblChan2 = new JLabel("Chan 2:");
		lblChan2.setFont(GUI.smallPlainFont);

		comBoxCh2 = new JComboBox<String>();
		comBoxCh2.setRenderer(chanRend);
		comBoxCh2.setFocusable(false);
		comBoxCh2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeComboxColor(comBoxCh2, (String) comBoxCh2.getSelectedItem());
			}
		});
		comBoxCh2.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));


		JLabel lblChan3 = new JLabel("Chan 3:");
		lblChan3.setFont(GUI.smallPlainFont);

		comBoxCh3 = new JComboBox<String>();
		comBoxCh3.setRenderer(chanRend);
		comBoxCh3.setFocusable(false);
		comBoxCh3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeComboxColor(comBoxCh3, (String) comBoxCh3.getSelectedItem());
			}
		});
		comBoxCh3.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));



		JPanel pnlChanProcess = new JPanel();
		pnlChanProcess.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlChanProcess.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlChanProcess.setBackground(new Color(211, 211, 211));

		JLabel lblChanProcess = new JLabel("Channels to Process");
		GroupLayout gl_pnlChanProcess = new GroupLayout(pnlChanProcess);
		gl_pnlChanProcess.setHorizontalGroup(
				gl_pnlChanProcess.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlChanProcess.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblChanProcess, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlChanProcess.setVerticalGroup(
				gl_pnlChanProcess.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblChanProcess, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		pnlChanProcess.setLayout(gl_pnlChanProcess);

		JLabel lblProcessChanSelect = new JLabel("Select Channels:");
		Settings settings = GUI.settings;
		chkSelectGreen = new JCheckBox("Green");
		chkSelectGreen.setFocusable(false);
		chkSelectGreen.setForeground(Channel.GREEN.getColor());
		if (settings.channelsToProcess.contains(Channel.GREEN)) {
			chkSelectGreen.setSelected(true);
		}

		chkSelectRed = new JCheckBox("Red");
		chkSelectRed.setFocusable(false);
		chkSelectRed.setForeground(Channel.RED.getColor());
		if (settings.channelsToProcess.contains(Channel.RED)) {
			chkSelectRed.setSelected(true);
		}

		chkSelectBlue = new JCheckBox("Blue");
		chkSelectBlue.setFocusable(false);
		chkSelectBlue.setForeground(Channel.BLUE.getColor());
		if (settings.channelsToProcess.contains(Channel.BLUE)) {
			chkSelectBlue.setSelected(true);
		}

		chkSelectWhite = new JCheckBox("White");
		chkSelectWhite.setFocusable(false);
		chkSelectWhite.setForeground(Channel.WHITE.getColor());
		if (settings.channelsToProcess.contains(Channel.WHITE)) {
			chkSelectWhite.setSelected(true);
		}

		JPanel lblChanForROISelect = new JPanel();
		lblChanForROISelect.setFont(new Font("Arial", Font.PLAIN, 13));
		lblChanForROISelect.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblChanForROISelect.setBackground(new Color(211, 211, 211));

		JLabel lblChanSelectROI = new JLabel("Channel for Selecting ROIs");

		GroupLayout gl_lblChanForROISelect = new GroupLayout(lblChanForROISelect);
		gl_lblChanForROISelect.setHorizontalGroup(
				gl_lblChanForROISelect.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_lblChanForROISelect.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblChanSelectROI, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_lblChanForROISelect.setVerticalGroup(
				gl_lblChanForROISelect.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblChanSelectROI, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		lblChanForROISelect.setLayout(gl_lblChanForROISelect);

		JLabel lblROISelectChan = new JLabel("Select Channel:");

		comBoxChanROI = new JComboBox<String>();
		comBoxChanROI.setFocusable(false);
		comBoxChanROI.setRenderer(chanRend);
		values.remove("<none>");
		comBoxChanROI.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				changeComboxColor(comBoxChanROI, (String) comBoxChanROI.getSelectedItem());
			}
		});
		comBoxChanROI.setModel(new DefaultComboBoxModel<String>(values.toArray(new String[values.size()])));
		comBoxChanROI.setSelectedItem(settings.channelForROIDraw.toReadableString());

		lblError = new JLabel("Error");
		lblError.setFont(GUI.mediumFont);
		lblError.setForeground(Color.RED);
		lblError.setVisible(false);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(pnlChanMap, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addGroup(groupLayout.createSequentialGroup()
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(lblChanMap0)
												.addComponent(lblChanMap1)
												.addComponent(lblChanMap2)
												.addComponent(lblChanMap3))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
												.addComponent(comBoxCh1, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(comBoxCh2, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(comBoxCh3, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(comBoxCh0, GroupLayout.PREFERRED_SIZE, 141, GroupLayout.PREFERRED_SIZE)))
								.addComponent(pnlChanProcess, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(lblProcessChanSelect)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(chkSelectGreen)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(chkSelectRed)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(chkSelectBlue)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(chkSelectWhite))
								.addComponent(lblChanForROISelect, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(lblROISelectChan)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(comBoxChanROI, GroupLayout.PREFERRED_SIZE, 141, GroupLayout.PREFERRED_SIZE))
								.addComponent(lblError))
						.addContainerGap())
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(pnlChanMap, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblChanMap0)
								.addComponent(comBoxCh0, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblChanMap1)
								.addComponent(comBoxCh1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblChanMap2)
								.addComponent(comBoxCh2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblChanMap3)
								.addComponent(comBoxCh3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(pnlChanProcess, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblProcessChanSelect)
								.addComponent(chkSelectGreen)
								.addComponent(chkSelectRed)
								.addComponent(chkSelectBlue)
								.addComponent(chkSelectWhite))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(lblChanForROISelect, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblROISelectChan)
								.addComponent(comBoxChanROI, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED, 72, Short.MAX_VALUE)
						.addComponent(lblError)
						.addContainerGap())
				);

		JLabel lblChanMap = new JLabel("Channel Mapping");
		GroupLayout gl_pnlChanMap = new GroupLayout(pnlChanMap);
		gl_pnlChanMap.setHorizontalGroup(
				gl_pnlChanMap.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlChanMap.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblChanMap, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlChanMap.setVerticalGroup(
				gl_pnlChanMap.createParallelGroup(Alignment.LEADING)
				.addComponent(lblChanMap, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		pnlChanMap.setLayout(gl_pnlChanMap);
		setLayout(groupLayout);

	}

	@Override
	public void reset(Settings settings, boolean enabled) {
		if (settings.channelMap.get(0) == null) {
			this.comBoxCh0.setSelectedItem("<none>");
		} else {
			this.comBoxCh0.setSelectedItem(settings.channelMap.get(0).toReadableString());

		}

		if (settings.channelMap.get(1) == null) {
			this.comBoxCh1.setSelectedItem("<none>");
		} else {
			this.comBoxCh1.setSelectedItem(settings.channelMap.get(1).toReadableString());

		}

		if (settings.channelMap.get(2) == null) {
			this.comBoxCh2.setSelectedItem("<none>");
		} else {
			this.comBoxCh2.setSelectedItem(settings.channelMap.get(2).toReadableString());

		}

		if (settings.channelMap.get(3) == null) {
			this.comBoxCh3.setSelectedItem("<none>");
		} else {
			this.comBoxCh3.setSelectedItem(settings.channelMap.get(3).toReadableString());

		}
		this.chkSelectGreen.setSelected(settings.channelsToProcess.contains(Channel.GREEN));
		this.chkSelectRed.setSelected(settings.channelsToProcess.contains(Channel.RED));
		this.chkSelectWhite.setSelected(settings.channelsToProcess.contains(Channel.WHITE));
		this.chkSelectBlue.setSelected(settings.channelsToProcess.contains(Channel.BLUE));
		this.comBoxChanROI.setSelectedItem(settings.channelForROIDraw.toReadableString());
		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}

	}

	@Override
	public void displayError(String error) {
		this.lblError.setText(error);
		this.lblError.setVisible(true);
	}

	@Override
	public void removeError() {
		this.lblError.setVisible(false);
	}

	@Override
	public boolean applyFields(Settings settings) {

		List<String> values = new ArrayList<String>();
		for (Channel chan : Channel.values()) {
			values.add(chan.toReadableString());
		}
		values.add("<none>");
		values.add("<none>");
		values.add("<none>");
		values.add("<none>");
		if (!values.remove(this.comBoxCh0.getSelectedItem()) || !values.remove(this.comBoxCh1.getSelectedItem()) ||
				!values.remove(this.comBoxCh2.getSelectedItem()) || !values.remove(this.comBoxCh3.getSelectedItem())) {
			displayError("Incorrect channel config. Each channel # must have a unique color.");
			return false;
		} else if (!this.chkSelectBlue.isSelected() && !this.chkSelectGreen.isSelected() && !this.chkSelectRed.isSelected() && !this.chkSelectWhite.isSelected()) {
			displayError("Incorrect channel config. At least 1 one channel must be processed.");
			return false;
		} else if (!values.contains("<none>")) {
			displayError("Incorrect channel config. At least one channel must be assigned.");
			return false;
		} else if (values.contains((String) this.comBoxChanROI.getSelectedItem())) {
			displayError("The channel chosen for drawing ROIs was not assigned a number.");
			return false;
		}

		settings.channelForROIDraw = Channel.parse((String) this.comBoxChanROI.getSelectedItem());
		Map<Integer, Channel> newChanMap = new HashMap<Integer, Channel>();
		if (!this.comBoxCh0.getSelectedItem().equals("<none>")) {
			newChanMap.put(0, Channel.parse((String) this.comBoxCh0.getSelectedItem()));
		}
		if (!this.comBoxCh1.getSelectedItem().equals("<none>")) {
			newChanMap.put(1, Channel.parse((String) this.comBoxCh1.getSelectedItem()));
		}
		if (!this.comBoxCh2.getSelectedItem().equals("<none>")) {
			newChanMap.put(2, Channel.parse((String) this.comBoxCh2.getSelectedItem()));
		}
		if (!this.comBoxCh3.getSelectedItem().equals("<none>")) {
			newChanMap.put(3, Channel.parse((String) this.comBoxCh3.getSelectedItem()));
		}

		List<Channel> channelForProcess = new ArrayList<Channel>();
		if (this.chkSelectBlue.isSelected()) {
			if (newChanMap.containsValue(Channel.BLUE)) {
				channelForProcess.add(Channel.BLUE);
			} else {
				displayError("All channels to be processed must be mapped.");
				return false;
			}
		}
		if (this.chkSelectRed.isSelected()) {
			if (newChanMap.containsValue(Channel.RED)) {
				channelForProcess.add(Channel.RED);
			} else {
				displayError("All channels to be processed must be mapped.");
				return false;
			}
		}
		if (this.chkSelectGreen.isSelected()) {
			if (newChanMap.containsValue(Channel.GREEN)) {
				channelForProcess.add(Channel.GREEN);
			} else {
				displayError("All channels to be processed must be mapped.");
				return false;
			}		

		}
		if (this.chkSelectWhite.isSelected()) {
			if (newChanMap.containsValue(Channel.WHITE)) {
				channelForProcess.add(Channel.WHITE);
			} else {
				displayError("All channels to be processed must be mapped.");
				return false;
			}	

		}
		settings.channelMap = newChanMap;
		settings.channelsToProcess = channelForProcess;
		settings.channelForROIDraw = (Channel.parse((String) this.comBoxChanROI.getSelectedItem()));
		return true;
	}


	@Override
	public SettingPage getPageDesignation() {
		return setttingPage;
	}

	public void changeComboxColor(JComboBox<String> comboBox, String channelString) {
		for (Channel chan : Channel.values()) {
			if (chan.toReadableString().equalsIgnoreCase(channelString)) {
				comboBox.setForeground(chan.getColor());
				return;
			}
		}

		comboBox.setForeground(Color.BLACK);
	}

	@Override
	public JPanel getRawComponent() {
		return this;
	}

}
class PnlSaveOptions extends JPanel implements SettingsPanel {

	private static final long serialVersionUID = -1576497004345094528L;
	protected JTextField fullPathName;
	protected JTextField folderName;
	private JLabel lblError;
	protected JButton btnBrowseFolders;
	protected FileBrowser fileBrowser;
	private JPanel thisObject = this;
	private static final SettingPage setttingPage = SettingPage.Saving;

	/**
	 * Create the panel.
	 */
	public PnlSaveOptions() {

		this.fileBrowser = new FileBrowser(FileBrowser.MODE_DIRECTORIES, null, false);

		JPanel pnlOutputLocation = new JPanel();
		pnlOutputLocation.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlOutputLocation.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlOutputLocation.setBackground(new Color(211, 211, 211));

		JLabel lblOutputLocation = new JLabel("Save Location");

		GroupLayout gl_pnlOutputLocation = new GroupLayout(pnlOutputLocation);
		gl_pnlOutputLocation.setHorizontalGroup(
				gl_pnlOutputLocation.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlOutputLocation.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblOutputLocation, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlOutputLocation.setVerticalGroup(
				gl_pnlOutputLocation.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblOutputLocation, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		pnlOutputLocation.setLayout(gl_pnlOutputLocation);

		JLabel lblFullPath = new JLabel("Full Path:");

		fullPathName = new JTextField();
		fullPathName.setBackground(Color.WHITE);
		fullPathName.setEditable(false);
		fullPathName.setColumns(10);

		btnBrowseFolders = new JButton("Browse Folders");
		btnBrowseFolders.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Settings settings = GUI.settings;
				if (settings != null && settings.recentOpenFileLocations != null) {
					fileBrowser.startBrowsing(settings.recentOpenFileLocations, thisObject);
				} else {
					fileBrowser.startBrowsing(null, thisObject);
				}

				List<File> files = fileBrowser.getSelectedFiles();
				if (files == null || files.size() == 0)
					return;

				List<File> fileRecents = fileBrowser.getRecents();
				if (fileRecents != null && fileRecents.size() > 0) {
					if (settings.recentOpenFileLocations != null) {
						settings.recentOpenFileLocations.clear();
					}
					settings.recentOpenFileLocations.addAll(fileRecents);
					settings.needsUpdate= true;
					boolean saved = SettingsLoader.saveSettings(settings);

					if (!saved) {
						JOptionPane.showMessageDialog(thisObject, "Could not save settings.", "Error Saving.", JOptionPane.ERROR_MESSAGE);

					} else {
						settings.needsUpdate = false;
						fullPathName.setText(files.get(0).getPath());
						folderName.setText(files.get(0).getName());
					}
				}

			}
		});

		JLabel lblFolderName = new JLabel("Output Folder");

		folderName = new JTextField();
		folderName.setBackground(Color.WHITE);
		folderName.setDisabledTextColor(Color.BLACK);
		folderName.setEditable(false);
		folderName.setColumns(10);

		lblError = new JLabel("Error");
		lblError.setVisible(false);
		lblError.setFont(GUI.mediumFont);
		lblError.setForeground(Color.RED);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(fullPathName, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addComponent(pnlOutputLocation, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
										.addComponent(lblFolderName)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(folderName, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(btnBrowseFolders))
								.addComponent(lblFullPath)
								.addComponent(lblError))
						.addContainerGap())
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(pnlOutputLocation, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnBrowseFolders)
								.addComponent(lblFolderName)
								.addComponent(folderName, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblFullPath)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(fullPathName, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED, 234, Short.MAX_VALUE)
						.addComponent(lblError)
						.addContainerGap())
				);
		setLayout(groupLayout);

	}

	public void displayError(String error) {
		this.lblError.setText(error);
		this.lblError.setVisible(true);
	}

	public void removeError() {
		this.lblError.setVisible(false);
	}

	public void reset(Settings settings, boolean enabled) {
		if (settings.outputLocation == null) {
			folderName.setText("");
			fullPathName.setText("");
		} else {
			folderName.setText(settings.outputLocation.getName());
			fullPathName.setText(settings.outputLocation.getPath());
		}
		this.btnBrowseFolders.setEnabled(enabled);

	}

	public boolean applyFields(Settings settings) {

		if (!fullPathName.getText().equals("")) {
			File file = new File(fullPathName.getText());
			if (!file.exists()) {
				displayError("The selected output directory no longer exists.");
				return false;
			} else if (!file.canWrite()) {
				displayError("The directory you selected doesn't have write access.");
				return false;
			} else {
				settings.outputLocation = file;
			}
		} else {
			settings.outputLocation = null;
		}
		return true;
	}

	public SettingPage getPageDesignation() {
		return setttingPage;
	}

	@Override
	public JPanel getRawComponent() {
		return this;
	}

}
class PnlBinOptions extends JPanel implements SettingsPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6622171153906374924L;
	private JLabel lblError;
	protected JCheckBox chkDrawGreen;
	protected JCheckBox chkDrawRed;
	protected JCheckBox chkDrawBlue;
	protected JCheckBox chkDrawWhite;
	protected JSpinner spnNumBins;
	protected JCheckBox chkDrawBinLabels;
	protected JCheckBox chkCalcBins;
	protected JCheckBox chkExcludeOutsider;
	private JCheckBox chkCountOutsideAsOutermost;
	private final static SettingPage settingPage = SettingPage.BinConfiguration;

	/**
	 * Create the panel.
	 */
	public PnlBinOptions() {

		JPanel pnlBinOptions = new JPanel();
		pnlBinOptions.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlBinOptions.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlBinOptions.setBackground(new Color(211, 211, 211));

		JLabel lblBinSettings = new JLabel("Bin Settings");

		GroupLayout gl_pnlBinOptions = new GroupLayout(pnlBinOptions);
		gl_pnlBinOptions.setHorizontalGroup(
				gl_pnlBinOptions.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlBinOptions.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblBinSettings, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlBinOptions.setVerticalGroup(
				gl_pnlBinOptions.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblBinSettings, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		pnlBinOptions.setLayout(gl_pnlBinOptions);

		chkCalcBins = new JCheckBox("Calculate Bins");
		chkCalcBins.setFocusable(false);

		JLabel lblNumberOfBins = new JLabel("Number of Bins:");

		SpinnerNumberModel model = new SpinnerNumberModel(5.0, 2.0, 100.0, 1.0);  

		spnNumBins = new JSpinner(model);
		spnNumBins.setFocusable(false);
		spnNumBins.setBackground(Color.WHITE);
		((DefaultEditor) spnNumBins.getEditor()).getTextField().setEditable(false);
		((DefaultEditor) spnNumBins.getEditor()).getTextField().setBackground(Color.WHITE);

		JPanel pnlBinOutput = new JPanel();
		pnlBinOutput.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlBinOutput.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlBinOutput.setBackground(new Color(211, 211, 211));

		JLabel lblBinOutput = new JLabel("Binning Output");
		GroupLayout gl_pnlBinOutput = new GroupLayout(pnlBinOutput);
		gl_pnlBinOutput.setHorizontalGroup(
				gl_pnlBinOutput.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 492, Short.MAX_VALUE)
				.addGroup(gl_pnlBinOutput.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblBinOutput, GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlBinOutput.setVerticalGroup(
				gl_pnlBinOutput.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 23, Short.MAX_VALUE)
				.addComponent(lblBinOutput, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
				);
		pnlBinOutput.setLayout(gl_pnlBinOutput);

		chkExcludeOutsider = new JCheckBox("Exclude points outside of bin region");
		chkExcludeOutsider.setFocusable(false);

		chkExcludeOutsider.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if (chkExcludeOutsider.isSelected()) {
					chkCountOutsideAsOutermost.setEnabled(false);
				} else {
					chkCountOutsideAsOutermost.setEnabled(true);
				}
			}
		});

		chkCountOutsideAsOutermost = new JCheckBox("Lump points outside bin region with nearest bin");

		chkDrawBinLabels = new JCheckBox("Draw bin labels");
		chkDrawBinLabels.setFocusable(false);

		JLabel lblChanDrawBin = new JLabel("Channels to Draw Bins:");

		chkDrawGreen = new JCheckBox("Green");
		chkDrawGreen.setForeground(Channel.GREEN.getColor());
		chkDrawGreen.setFocusable(false);

		chkDrawRed = new JCheckBox("Red");
		chkDrawRed.setForeground(Channel.RED.getColor());
		chkDrawRed.setFocusable(false);

		chkDrawBlue = new JCheckBox("Blue");
		chkDrawBlue.setForeground(Channel.BLUE.getColor());
		chkDrawBlue.setFocusable(false);

		chkDrawWhite = new JCheckBox("White");
		chkDrawWhite.setForeground(Channel.WHITE.getColor());
		chkDrawWhite.setFocusable(false);

		lblError = new JLabel("Error:");
		lblError.setFont(GUI.mediumFont);
		lblError.setForeground(Color.RED);


		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(chkDrawBinLabels)
								.addComponent(chkCalcBins)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(lblNumberOfBins)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(spnNumBins, GroupLayout.PREFERRED_SIZE, 45, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(chkDrawGreen)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(chkDrawRed)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(chkDrawBlue)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(chkDrawWhite))
								.addComponent(chkExcludeOutsider)
								.addComponent(chkCountOutsideAsOutermost)
								.addComponent(pnlBinOptions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addComponent(lblError)
								.addComponent(pnlBinOutput, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblChanDrawBin))
						.addContainerGap())
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(pnlBinOptions, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(chkCalcBins)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(chkDrawBinLabels)
						.addGap(10)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblNumberOfBins)
								.addComponent(spnNumBins, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addGap(12)
						.addComponent(pnlBinOutput, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(chkExcludeOutsider)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(chkCountOutsideAsOutermost)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(lblChanDrawBin)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(chkDrawGreen)
								.addComponent(chkDrawRed)
								.addComponent(chkDrawBlue)
								.addComponent(chkDrawWhite))
						.addPreferredGap(ComponentPlacement.RELATED, 105, Short.MAX_VALUE)
						.addComponent(lblError)
						.addContainerGap())
				);
		setLayout(groupLayout);



	}

	public void displayError(String error) {
		this.lblError.setText(error);
		this.lblError.setVisible(true);
	}

	public void removeError() {
		this.lblError.setVisible(false);
	}

	public boolean applyFields(Settings settings) {

		if (!this.chkDrawRed.isSelected() && !this.chkDrawGreen.isSelected() && !this.chkDrawBlue.isSelected() && !this.chkDrawWhite.isSelected()) {
			displayError("Bins must be drawn on at least one channel.");
			return false;
		} else {
			removeError();
		}
		settings.calculateBins = chkCalcBins.isSelected();
		settings.drawBinLabels = chkDrawBinLabels.isSelected();
		settings.excludePtsOutsideBin = chkExcludeOutsider.isSelected();
		settings.numberOfBins = (int) Math.round(((Number) spnNumBins.getValue()).doubleValue());
		List<Channel> selectedChans = new ArrayList<Channel>();
		if (chkDrawBlue.isSelected())
			selectedChans.add(Channel.BLUE);
		if (chkDrawGreen.isSelected())
			selectedChans.add(Channel.GREEN);
		if (chkDrawRed.isSelected())
			selectedChans.add(Channel.RED);
		if (chkDrawWhite.isSelected())
			selectedChans.add(Channel.WHITE);
		settings.channelToDrawBin = selectedChans;
		settings.includePtsNearestBin = chkCountOutsideAsOutermost.isSelected();
		return true;


	}

	public SettingPage getPageDesignation() {
		return settingPage;
	}

	public void reset(Settings settings, boolean enabled) {
		for (Component component : this.getComponents()) {
			if (!enabled) {
				component.setEnabled(false);
			} else {
				component.setEnabled(true);

			}
		}
		chkCalcBins.setSelected(settings.calculateBins);
		chkDrawBinLabels.setSelected(settings.drawBinLabels);
		chkDrawBlue.setSelected(settings.channelToDrawBin.contains(Channel.BLUE));
		chkDrawGreen.setSelected(settings.channelToDrawBin.contains(Channel.GREEN));
		chkDrawRed.setSelected(settings.channelToDrawBin.contains(Channel.RED));
		chkDrawWhite.setSelected(settings.channelToDrawBin.contains(Channel.WHITE));
		spnNumBins.setValue(settings.numberOfBins);
		chkExcludeOutsider.setSelected(settings.excludePtsOutsideBin);
		chkCountOutsideAsOutermost.setSelected(settings.includePtsNearestBin);
		if (enabled) {
			if (chkExcludeOutsider.isSelected()) {
				chkCountOutsideAsOutermost.setEnabled(false);
			} else {
				chkCountOutsideAsOutermost.setEnabled(true);

			}
		}
		

	}

	@Override
	public JPanel getRawComponent() {
		return this;
	}

}
class PnlProcessingOptions extends JPanel implements SettingsPanel {

	private static final long serialVersionUID = -6622171153906374924L;
	private JLabel lblError;
	private JTextField txtMinThresh;
	private JTextField txtUnsharpRadius;
	private JTextField txtUnsharpWeight;
	private JTextField txtGaussianSigma;
	private static final SettingPage settingPage = SettingPage.Processing;

	public PnlProcessingOptions() {
		setPreferredSize(new Dimension(518, 384));

		JPanel pnlProcessingSettings = new JPanel();
		pnlProcessingSettings.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlProcessingSettings.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlProcessingSettings.setBackground(new Color(211, 211, 211));

		JLabel lblProcessingSettings = new JLabel("Parameter Values");

		GroupLayout gl_pnlProcessingSettings = new GroupLayout(pnlProcessingSettings);
		gl_pnlProcessingSettings.setHorizontalGroup(
				gl_pnlProcessingSettings.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlProcessingSettings.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblProcessingSettings, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlProcessingSettings.setVerticalGroup(
				gl_pnlProcessingSettings.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblProcessingSettings, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		pnlProcessingSettings.setLayout(gl_pnlProcessingSettings);

		lblError = new JLabel("Error:");
		lblError.setFont(GUI.mediumFont);
		lblError.setForeground(Color.RED);

		JLabel lblMinimumThreshold = new JLabel("Auto-Threshold Minimum Value (0-255):");

		txtMinThresh = new JTextField();
		txtMinThresh.setText("0");
		txtMinThresh.setColumns(10);

		JLabel lblUnsharpMaskRadius = new JLabel("Unsharp Mask Pixel Radius (1-1000):");

		txtUnsharpRadius = new JTextField();
		txtUnsharpRadius.setText("20");
		txtUnsharpRadius.setColumns(10);

		JLabel lblUnsharpMaskPixel = new JLabel("Unsharp Mask Pixel Width (0.1-0.9):");

		txtUnsharpWeight = new JTextField();
		txtUnsharpWeight.setColumns(10);

		JLabel lblGaussianBlurSigma = new JLabel("Gaussian Blur Sigma Value (0.01-100):");

		txtGaussianSigma = new JTextField();
		txtGaussianSigma.setColumns(10);


		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(pnlProcessingSettings, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblError)

								.addGroup(groupLayout.createSequentialGroup()
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(lblMinimumThreshold, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
												.addComponent(lblUnsharpMaskRadius, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
												.addComponent(lblUnsharpMaskPixel, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
												.addComponent(lblGaussianBlurSigma, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
												.addComponent(txtMinThresh, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
												.addComponent(txtUnsharpWeight, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
												.addComponent(txtUnsharpRadius, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
												.addComponent(txtGaussianSigma, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE))

										.addGap(180))


								/*.addGroup(groupLayout.createSequentialGroup()
							.addComponent(pnlProcessingSettings, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
							.addContainerGap())
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblMinimumThreshold, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
								.addComponent(lblUnsharpMaskRadius, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
								.addComponent(lblUnsharpMaskPixel, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE)
								.addComponent(lblGaussianBlurSigma, GroupLayout.DEFAULT_SIZE, 257, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(txtMinThresh, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
								.addComponent(txtUnsharpWeight, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
								.addComponent(txtUnsharpRadius, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE)
								.addComponent(txtGaussianSigma, GroupLayout.DEFAULT_SIZE, 42, Short.MAX_VALUE))
							.addGap(198))

						.addComponent(lblError)*/)

						.addContainerGap())
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(pnlProcessingSettings, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblMinimumThreshold)
								.addComponent(txtMinThresh, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblUnsharpMaskRadius)
								.addComponent(txtUnsharpRadius, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblUnsharpMaskPixel)
								.addComponent(txtUnsharpWeight, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblGaussianBlurSigma)
								.addComponent(txtGaussianSigma, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						//.addPreferredGap(ComponentPlacement.UNRELATED)
						/*.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(2)
							.addComponent(lblGaussianBlurSigma))
						.addComponent(txtGaussianSigma, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))*/
						.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addComponent(lblError)
						.addContainerGap())
				);
		setLayout(groupLayout);



	}



	public void displayError(String error) {
		this.lblError.setText(error);
		this.lblError.setVisible(true);
	}

	public void removeError() {
		this.lblError.setVisible(false);
	}

	public void reset(Settings settings, boolean enabled) {
		this.lblError.setVisible(false);
		this.txtGaussianSigma.setText(settings.processingGaussianSigma + "");
		this.txtUnsharpRadius.setText(settings.processingUnsharpMaskRadius + "");
		this.txtUnsharpWeight.setText(settings.processingUnsharpMaskWeight + "");
		this.txtMinThresh.setText(settings.processingMinThreshold + "");
		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}
	}

	public boolean applyFields(Settings settings) {
		int minThresh = -1;
		int unsharpRadius = -1;
		double unsharpWeight = -1;
		double gaussianSigma = -1;

		try {
			String text = this.txtMinThresh.getText();
			minThresh = Integer.parseInt(text);
			if (minThresh < 0 || minThresh > 255)
				throw new Exception();
		} catch (Exception e) {
			displayError("Thresholding minimum must be an integer between 0 and 255.");
			return false;
		}

		try {
			String text = this.txtUnsharpRadius.getText();
			unsharpRadius = Integer.parseInt(text);
			if (unsharpRadius < 1 || unsharpRadius > 1000)
				throw new Exception();
		} catch (Exception e) {
			displayError("Unsharp mask radius must be an integer between 1 and 1000.");
			return false;
		}

		try {
			String text = this.txtUnsharpWeight.getText();
			unsharpWeight = Double.parseDouble(text);
			if (unsharpWeight < 0.1 || unsharpWeight > 0.9)
				throw new Exception();
		} catch (Exception e) {
			displayError("Unsharp mask radius must be a decimal between 0.1 and 0.9.");
			return false;
		}

		try {
			String text = this.txtGaussianSigma.getText();
			gaussianSigma = Double.parseDouble(text);
			if (gaussianSigma < 0.01 || gaussianSigma > 100)
				throw new Exception();
		} catch (Exception e) {
			displayError("Gaussian blur sigma must be a decimal between 0.01 and 100.");
			return false;
		}
		settings.processingMinThreshold = minThresh;
		settings.processingUnsharpMaskRadius = unsharpRadius;
		settings.processingUnsharpMaskWeight = unsharpWeight;
		settings.processingGaussianSigma = gaussianSigma;

		removeError();
		return true;

	}

	@Override
	public SettingPage getPageDesignation() {
		return settingPage;
	}



	@Override
	public JPanel getRawComponent() {
		return this;
	}
}
class PnlImageOptions extends JPanel implements SettingsPanel {


	private static final long serialVersionUID = -6622171153906374924L;
	private JLabel lblError;

	private SettingPage settingsPage;
	private SimpleJList<String> calibrationList;
	private JPanel jpanel;
	private boolean dontadjust = false;

	/**
	 * Create the panel.
	 */
	public PnlImageOptions() {

		this.settingsPage = SettingPage.ImageSettings;
		this.jpanel = this;

		JPanel pnlPixelConverstions = new JPanel();
		pnlPixelConverstions.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlPixelConverstions.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlPixelConverstions.setBackground(new Color(211, 211, 211));

		JLabel lblPixelConversions = new JLabel("Image Pixel Units");

		GroupLayout gl_pnlPixelConverstions = new GroupLayout(pnlPixelConverstions);
		gl_pnlPixelConverstions.setHorizontalGroup(
				gl_pnlPixelConverstions.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlPixelConverstions.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblPixelConversions, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlPixelConverstions.setVerticalGroup(
				gl_pnlPixelConverstions.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblPixelConversions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		pnlPixelConverstions.setLayout(gl_pnlPixelConverstions);

		lblError = new JLabel("Error");
		lblError.setVisible(false);
		lblError.setFont(GUI.mediumFont);
		lblError.setForeground(Color.RED);

		JLabel lblCalibration = new JLabel("Calibration (if not supplied by image file):");

		JScrollPane scrollPane = new JScrollPane();

		JButton btnNewCalibration = new JButton("New Calibration");
		btnNewCalibration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog(jpanel, "Select a name for this calibration:", "New Calibration", JOptionPane.INFORMATION_MESSAGE);
				if (input == null || input.length() == 0)
					return;
				String calib = JOptionPane.showInputDialog(jpanel, "What is the name of the unit to convert pixels to (i.e. microns):", "New Calibration", JOptionPane.INFORMATION_MESSAGE);
				if (calib == null || calib.length() == 0)
					return;
				String ratio = JOptionPane.showInputDialog(jpanel, "What is the conversion? (One pixel equals _____ " + calib + ")", "New Calibration", JOptionPane.INFORMATION_MESSAGE);
				if (calib == null || calib.length() == 0)
					return;
				Double d;
				try {
					d = Double.parseDouble(ratio);
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(jpanel, "The conversion you provided was not a decimal number.", "New Calibration Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (d <= 0) {
					JOptionPane.showMessageDialog(jpanel, "The conversion you provided was negative. This is invalid.", "New Calibration Error", JOptionPane.ERROR_MESSAGE);
					return;
				}

				String newCalibration = input + " (1 pixel : " + d + " " + calib + ")"; 
				calibrationList.addItem(newCalibration);
				calibrationList.setSelectedValue(newCalibration, true);

			}
		});

		JButton btnRemove = new JButton("Remove");
		btnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selectedString = calibrationList.getSelectedValue();
				if (selectedString == null)
					return;
				else if (calibrationList.getListSize() == 1) {
					JOptionPane.showMessageDialog(jpanel, "You must have at least one calibration", "Calibration Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				calibrationList.removeItem(selectedString);
				calibrationList.setSelectedIndex(0);
			}
		});
		calibrationList = new SimpleJList<String>();

		this.calibrationList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!dontadjust) {
					if (calibrationList.getSelectedIndex() == -1) {
						int index = e.getFirstIndex();
						dontadjust = true;
						if (index > -1) {
							calibrationList.setSelectedIndex(index);
						} else {
							calibrationList.setSelectedIndex(0);
						}
						dontadjust = false;
					} else if (calibrationList.getSelectedIndices().length > 1) {
						dontadjust = true;
						calibrationList.setSelectedIndex(e.getLastIndex());
						dontadjust = false;
					}

				}

			}
		});

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(pnlPixelConverstions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(lblError, Alignment.TRAILING)
												.addComponent(lblCalibration)
												.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE))
										.addContainerGap())
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(btnRemove)
										.addPreferredGap(ComponentPlacement.RELATED, 275, Short.MAX_VALUE)
										.addComponent(btnNewCalibration))))
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(pnlPixelConverstions, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblCalibration)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnNewCalibration)
								.addComponent(btnRemove))
						.addPreferredGap(ComponentPlacement.RELATED, 161, Short.MAX_VALUE)
						.addComponent(lblError)
						.addContainerGap())
				);

		scrollPane.setViewportView(calibrationList);
		setLayout(groupLayout);


	}

	@Override
	public void reset(Settings settings, boolean enabled) {

		this.calibrationList.setItems(settings.calibrations);
		this.calibrationList.setSelectedIndex(settings.calibrationNumber - 1);
		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}
		this.calibrationList.setEnabled(enabled);
	}



	@Override
	public SettingPage getPageDesignation() {
		return this.settingsPage;
	}



	@Override
	public JPanel getRawComponent() {
		return this;
	}

	@Override
	public boolean applyFields(Settings settings) {
		settings.calibrations = this.calibrationList.toList();
		settings.calibrationNumber = this.calibrationList.getSelectedIndex() + 1;
		return true;
	}

	public void displayError(String error) {
		this.lblError.setText(error);
		this.lblError.setVisible(true);
	}

	public void removeError() {
		this.lblError.setVisible(false);
	}
}
class PnlReset extends JPanel implements SettingsPanel {

	private static final long serialVersionUID = 2016027296304991330L;
	private JPanel self;
	public final Preferences2 prefs;

	public PnlReset(Preferences2 prefs) {
		this.self = this;
		this.prefs = prefs;
		JPanel pnlPixelConverstions = new JPanel();
		pnlPixelConverstions.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlPixelConverstions.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlPixelConverstions.setBackground(new Color(211, 211, 211));

		JLabel lblPixelConversions = new JLabel("Hard Reset");

		GroupLayout gl_pnlPixelConverstions = new GroupLayout(pnlPixelConverstions);
		gl_pnlPixelConverstions.setHorizontalGroup(
				gl_pnlPixelConverstions.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlPixelConverstions.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblPixelConversions, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlPixelConverstions.setVerticalGroup(
				gl_pnlPixelConverstions.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblPixelConversions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		pnlPixelConverstions.setLayout(gl_pnlPixelConverstions);

		JLabel lblbyClickingreset = new JLabel("<html>By clicking 'Reset to Default' below, you will reset all settings to their default values. This action cannot be undone. This will affect your Channel configuration and you will likely need to re-map channels following a reset. In addition, you will need to re-select an output folder; you will not be able to run the program until an output location has been set.</html>");

		JButton btnReset = new JButton("Reset to Defaults");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(self, "Are you sure you want to reset settings?", "Confirm Reset", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					Settings oldSettings = GUI.settings;
					try {
						GUI.settings = null;
						GUI.settings = SettingsLoader.loadSettings(true);
						SettingsLoader.saveSettings(GUI.settings);

						prefs.invokeReset(true);
					} catch (Exception ex) {
						ex.printStackTrace();
						if (GUI.settings == null) {
							JOptionPane.showMessageDialog(self, "<html>There was an error loading settings:<br><br>" + ex.getMessage() + "</html>", "Settings Load Error", JOptionPane.ERROR_MESSAGE);
						} else {
							JOptionPane.showMessageDialog(self, "<html>There was an error updating settings:<br><br>" + ex.getMessage() + "</html>", "Settings Update Error", JOptionPane.ERROR_MESSAGE);

						}
						GUI.settings = oldSettings;

					}
				}
			}
		});

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addGroup(groupLayout.createSequentialGroup()
										.addContainerGap()
										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
												.addComponent(lblbyClickingreset, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 506, Short.MAX_VALUE)
												.addComponent(pnlPixelConverstions, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
								.addGroup(groupLayout.createSequentialGroup()
										.addGap(183)
										.addComponent(btnReset)))
						.addContainerGap())
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(pnlPixelConverstions, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(lblbyClickingreset)
						.addGap(12)
						.addComponent(btnReset)
						.addContainerGap(220, Short.MAX_VALUE))
				);
		setLayout(groupLayout);
	}

	@Override
	public void removeError() {}

	@Override
	public void reset(Settings settings, boolean enabled) {
		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}
	}

	@Override
	public boolean applyFields(Settings settings) {return true;}

	@Override
	public void displayError(String errors) {}

	@Override
	public SettingPage getPageDesignation() {
		return SettingPage.Reset;
	}

	@Override
	public JPanel getRawComponent() {
		return this;
	}

}

