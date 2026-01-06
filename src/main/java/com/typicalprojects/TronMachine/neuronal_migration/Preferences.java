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
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.Color;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;

import javax.swing.UIManager;
import javax.swing.JButton;
import javax.swing.SwingConstants;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import javax.swing.JDialog;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;

import java.awt.Font;
import java.awt.Insets;

import javax.swing.AbstractCellEditor;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.commons.lang3.StringUtils;

import com.google.common.io.Files;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager.Channel;
import com.typicalprojects.TronMachine.neuronal_migration.Preferences.ChannelSettingsHandler;
import com.typicalprojects.TronMachine.neuronal_migration.Preferences.SettingsPanel;
import com.typicalprojects.TronMachine.neuronal_migration.Settings.SettingsManager;
import com.typicalprojects.TronMachine.neuronal_migration.processing.ObjectEditableImage;
import com.typicalprojects.TronMachine.neuronal_migration.processing.ROIEditableImage;
import com.typicalprojects.TronMachine.popup.MultiSelectPopup;
import com.typicalprojects.TronMachine.util.ColorChooserUI;
import com.typicalprojects.TronMachine.util.CustomFileChooser;

import com.typicalprojects.TronMachine.util.SimpleJList;
import com.typicalprojects.TronMachine.util.SimpleJList.ListDropReceiver;

/**
 * Creates the popup which allows users to modify preferences. Each settings page within the Preferences
 * popup is a JPanel, and is created as an additional class declaration in this file. Each of the Preferences
 * pages implements {@link SettingsPanel}, which has various useful methods.<br><br>
 * 
 * There are many checks to ensure correct output. All preferences here are mirrored in the settings file,
 * stored in the Neuronal_Migration_Resources folder in the same directory as this program.
 * 
 * @author Justin Carrington
 */
public class Preferences extends JDialog {

	private static final long serialVersionUID = 6738766494677442465L;
	private JPanel contentPane;
	private JPanel activePanel;
	private GUI mainGUI;
	private JLabel lblPageName;

	private JButton btnApplyAndClose;
	private JButton btnCancel;
	private JLabel lblCannotEdit;
	private GroupLayout gl_contentPane;
	private SimpleJList<SettingsPanel> menuList;
	private boolean listSelectionChanging = false;
	
	/** Should only ever be one Preferences panel, store here so can retrieve from the classes declared in this file **/
	public static Preferences SINGLETON_FRAME = null;

	/**
	 * Create the Preferences frame, setting up all the JPanels for each of the Settings Pages.
	 * 
	 * @param gui The main {@link GUI} component
	 */
	public Preferences(GUI gui) {
		super(gui.getComponent());

		SINGLETON_FRAME = this;
		this.mainGUI = gui;
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				
				if (!lblCannotEdit.getText().contains("cancel the current run") && GUI.confirmWithUser("Do you want to apply changes?", "Exit Preferences", SINGLETON_FRAME,
						JOptionPane.ERROR_MESSAGE)) {

						userTriedToApplyChanges();
				}
				 else {
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
		this.menuList = new SimpleJList<SettingsPanel>(new SettingsPanelRenderer<SettingsPanel>());
		this.menuList.addItem(new PnlChanOptions()); // MAKE SURE THIS IS FIRST!
		this.menuList.addItem(new PnlImageOptions());
		this.menuList.addItem(new PnlProcessingOptions());
		this.menuList.addItem(new PnlBinOptions());
		this.menuList.addItem(new PnlOutputOptions());
		this.menuList.addItem(new PnlSaveOptions());
		this.menuList.addItem(new PnlTemplates(this));
		this.menuList.addItem(new PnlReset(this));
		this.menuList.setSelectedIndex(0);
		this.menuList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


		lblCannotEdit = new JLabel("There were errors in your configuration.");
		lblCannotEdit.setForeground(new Color(255, 100, 100));
		lblCannotEdit.setFont(GUI.mediumBoldFont);

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
		pnlPageName.setBackground(new Color(40, 40, 40));
		this.activePanel = this.menuList.getElementAt(0).getRawComponent();
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
		scrollPane.setViewportView(menuList);
		menuList.addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting() && !listSelectionChanging) {
					SettingsPanel panel = menuList.getSelectedValue();
					setCurrentPage(panel);

				}

			}
		});


		lblPageName = new JLabel("Channel Configuration");
		lblPageName.setFont(new Font("PingFang TC", Font.BOLD | Font.ITALIC, 15));
		lblPageName.setForeground(new Color(255, 255, 255));
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
				UIManager.put (key, GUI.smallBoldFont);
		}
	}

	/**
	 * Remove the Preferences pane by setting it NOT visible, and then refocusing the main GUI. Does not
	 * destroy the frame, because it is resource-intensive to build.
	 */
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

	/**
	 * Displays the preferences pane by doing the following:<br>
	 * <ol>
	 * <li>Removing all error messages</li>
	 * <li>Set the current page to the first page</li>
	 * <li>Set the preferences enabled (but if not running, disable the ability to set any options)</li>
	 * <li>Reset values in all pages</li>
	 * <li>Pack, set location of pane, and then set visible</li>
	 * </ol>
	 * 
	 * @param parent		The component which the Preferences pane should be centered on
	 * @param running	Whether the TRON machine is currently running (in which case, option setting will be
	 * 	disabled)
	 */
	public void display(Component parent, boolean running) {
		removeErrorMessages();
		setCurrentPage(this.menuList.getElementAt(0));
		setEnabled(!running);
		resetPreferences(!running);
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}


	/**
	 * Called whenever the user tries to apply changes in the preferences, such as on close (either via
	 * cancel or the 'X' in the menu bar). Will try to apply changes, displaying any errors in the process
	 * and moving the user to the Settings Page with the error.
	 */
	public void userTriedToApplyChanges() {
		removeErrorMessages();

		Object errors = applyPreferences(null);

		if (errors != null) {
			displayError();

			if (errors instanceof SettingsPanel) {
				setCurrentPage((SettingsPanel) errors);
			} else {
				GUI.displayMessage("Failure writing preferences:<br><br>" + errors.toString(), "I/O Failure", contentPane, JOptionPane.ERROR_MESSAGE);

			}

		} else {
			removeDisplay();
		}
	}

	/**
	 * Applies all settings currently in fields in the Preferences frame. First validates all fields and if
	 * there is an error, returns the panel which has an error (a {@link SettingsPanel}). It will then apply
	 * all changes to the current settings configuration. Then, saves the current settings to file. Optionally,
	 * if a customeSaveName is specified, the settings will ALSO be saved to that file (in addition to the
	 * normal save-to-file). <br><br>
	 * 
	 * If there are errors in saving, a {@link String} containing the error message will be returned (not wrapped
	 * in html tags).
	 * 
	 * @param customSaveName name of file to save settings to, in addition to normal save procedure. If null,
	 * only does normal save procedure.
	 * @return Object denoting errors. See above.
	 */
	protected Object applyPreferences(String customSaveName) {
		Settings settings = GUI.settings;


		settings.needsUpdate = true;
		Object errors = null;
		for (SettingsPanel settingsPanel : this.menuList.toList()) {
			if (!settingsPanel.validateFields()) {
				if (errors == null) {
					errors = settingsPanel;
				}
			}
		}
		this.menuList.refresh();

		if (errors != null)
			return errors;

		for (SettingsPanel settingsPanel : this.menuList.toList()) {
			settingsPanel.applyFields(GUI.settings);
		}

		try {
			SettingsManager.saveSettings(settings);

			if (customSaveName != null) {
				SettingsManager.saveSettings(settings, customSaveName);
			}
			settings.needsUpdate = false;
			return null;
		} catch (Exception e) {
			return e.getMessage();
		}



	}

	/**
	 * Resets all preferences pages by using the Settings file.
	 * 
	 * @param enabled	Whether the preferences pane should be enabled or disabled after the reset.
	 */
	public void resetPreferences(boolean enabled) {

		ChannelManager cmCopy = GUI.settings.channelMan.clone();

		for (SettingsPanel panel : this.menuList.toList()) {
			if (panel instanceof ChannelSettingsHandler) {
				((ChannelSettingsHandler) panel).setLocalCMCopy(cmCopy);
			}
			panel.reset(GUI.settings, enabled);
		}

	}
	
	/**
	 * Force resets the settings in Preferences
	 * 
	 * @param enabled	Whether Settings Pages should be enabled (i.e. TRON machine not running) after reset
	 */
	public void invokeReset(boolean enabled) {
		for (SettingsPanel panel : this.menuList.toList()) {
			panel.reset(GUI.settings, enabled);
		}
	}
	
	/**
	 * @return loaded settings pages
	 */
	public List<SettingsPanel> getSettingsPages() {
		return this.menuList.toList();
	}

	/**
	 * @return true of Preferences popup is visible
	 */
	public boolean isDisplaying() {
		return this.isVisible();
	}
	
	/**
	 * Displays an error saying there's error in the configuration in the main Preferences popup
	 */
	public void displayError() {
		this.lblCannotEdit.setText("There were errors in your configuration.");
		this.lblCannotEdit.setVisible(true);
	}

	/**
	 * Removes all error messages, from the main Preferences popup AND all of the settings pages.
	 */
	public void removeErrorMessages() {
		if (!this.lblCannotEdit.getText().equals("To make changes you must cancel the current run.")) {
			this.lblCannotEdit.setVisible(false);
		}

		for (SettingsPanel panel : this.menuList.toList()) {
			panel.removeError();
		}

	}

	private void setCurrentPage(SettingsPanel settings) {
		this.listSelectionChanging = true;
		gl_contentPane.replace(this.activePanel, settings.getRawComponent());
		this.lblPageName.setText(settings.getPageName());
		this.menuList.setSelectedValue(settings, true);
		this.activePanel = settings.getRawComponent();
		this.pack();
		settings.update();
		this.listSelectionChanging = false;

	}
	
	/**
	 * Designates a page in the Preferences, which can be navigated to in the navigation menu on the left
	 * side of the popup.
	 */
	public interface SettingsPanel {
		
		/** Remove an error from the page**/
		public void removeError();
		
		/**
		 * Reset settings in the page
		 * @param settings	Settings object to grab values from
		 * @param enabled	True if TRON machine isn't running.
		 */
		public void reset(Settings settings, boolean enabled);
		
		/**
		 * Applies all fields in the current page to the settings file.
		 * @param settings	The settings object to apply values.
		 */
		public void applyFields(Settings settings);

		/**
		 * Should validate the values currently in the fields of the settings panel. If there are any errors
		 * in validation, the {@link #displayError(String)} method should be called. IF there are no errors,
		 * the {@link #removeError()} should be called.
		 * 
		 * @return true if all fields have valid values, false otherwise
		 */
		public boolean validateFields();
		
		/**
		 * @param error error to display on settings page
		 */
		public void displayError(String error);
		
		/**
		 * @return true if there are errors on this page
		 */
		public boolean hasErrors();
		
		/**
		 * @return page name
		 */
		public String getPageName();
		
		/**
		 * @return JPanel representing the page
		 */
		public JPanel getRawComponent();
		
		/**
		 * Update the page, mainly to update the lists by calling their renderers.
		 */
		public void update();
	}

	/**
	 * Implemented by {@link SettingsPanel} which involve Channel settings.
	 */
	public interface ChannelSettingsHandler {
		/**
		 * A local copy is kept of all channel configuration so that these values can be changed, while
		 * retaining the original channel configuration in the Settings for backup in case values are entered
		 * incorrectly by the user. When setting a local copy, this should be a clone of the original
		 * ChannelManager object.
		 * 
		 * @param cm		ChannelManager copy
		 */
		public void setLocalCMCopy(ChannelManager cm);
	}

	private static class SettingsPanelRenderer<K> implements ListCellRenderer<K> {

		//private static Font smallFont = new Font("PingFang TC", Font.BOLD, 12);
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		private SettingsPanelRenderer() {
		}

		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {


			JLabel renderer = (JLabel) defaultRenderer
					.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);
			if (value instanceof SettingsPanel) {
				SettingsPanel panel = (SettingsPanel) value;
				renderer.setText(panel.getPageName());
				if (panel.hasErrors()) {
					renderer.setForeground(Color.RED);
				} else {
					renderer.setForeground(Color.BLACK);
				}

			}

			return renderer;


		}

	}

}

class PnlChanOptions extends JPanel implements SettingsPanel, ChannelSettingsHandler {

	private static final long serialVersionUID = -6622171153906374924L;
	private JLabel lblError;

	private JPanel jpanel;
	private JTextField txtChanName;
	private JTextField txtChanAbbrev;
	private JLabel lblImgColorDisp;
	private ColorChooserUI colorChooser;
	private JButton btnDeleteChan;
	private JButton btnAddChan;
	private JTable table;
	private JLabel lblTxtColorDisp;
	private JScrollPane scrollPane;

	private ChannelManager cmCopy;

	/**
	 * Create the panel.
	 */
	public PnlChanOptions() {

		this.jpanel = this;
		this.colorChooser = new ColorChooserUI("Please select a color.");

		JPanel pnlChannels = new JPanel();
		pnlChannels.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlChannels.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlChannels.setBackground(new Color(50, 50, 50));

		JLabel lblChannels = new JLabel("Channels");
		lblChannels.setForeground(new Color(0, 0, 0));

		GroupLayout gl_pnlChannels = new GroupLayout(pnlChannels);
		gl_pnlChannels.setHorizontalGroup(
				gl_pnlChannels.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlChannels.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblChannels, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlChannels.setVerticalGroup(
				gl_pnlChannels.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblChannels, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		pnlChannels.setLayout(gl_pnlChannels);

		lblError = new JLabel("Error");
		lblError.setVisible(false);
		lblError.setFont(GUI.mediumBoldFont);
		lblError.setForeground(new Color(255, 100, 100));

		JLabel lblInstructions = new JLabel("Channel list (double click to set meta and channel mapping):");

		scrollPane = new JScrollPane();

		btnDeleteChan = new JButton("Delete");
		btnDeleteChan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				btnDeleteChan.setEnabled(false);
				int selectedrow = table.getSelectedRow();
				if (selectedrow == -1)
					return;

				ChannelTableModel model = ((ChannelTableModel) table.getModel());
				Channel chan = cmCopy.getChannel((String) table.getValueAt(selectedrow, 0));
				cmCopy.removeChan(chan);
				model.setIgnoreValidation(true);
				model.removeRow(selectedrow);
				boolean hasProcess = false;
				boolean hasPrimROI = false;
				for (int row = 0; row < table.getRowCount(); row ++) {
					if ((Boolean) table.getValueAt(row, 4)) {
						hasProcess = true;
					}
					if ((Boolean) table.getValueAt(row, 5)) {
						hasPrimROI = true;
					}
				}
				if (table.getRowCount() > 0) {
					if (!hasProcess) {
						table.setValueAt(Boolean.TRUE, 0, 4);
					}
					if (!hasPrimROI) {
						table.setValueAt(Boolean.TRUE, 0, 5);
					}
				}
				btnDeleteChan.setEnabled(true);
				model.setIgnoreValidation(false);

			}
		});

		JPanel pnlCreateChannel = new JPanel();
		pnlCreateChannel.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlCreateChannel.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlCreateChannel.setBackground(new Color(50, 50, 50));

		JLabel lblCreateChannel = new JLabel("Create Channel");
		lblCreateChannel.setForeground(new Color(0, 0, 0));
		GroupLayout gl_pnlCreateChannel = new GroupLayout(pnlCreateChannel);
		gl_pnlCreateChannel.setHorizontalGroup(
				gl_pnlCreateChannel.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 492, Short.MAX_VALUE)
				.addGroup(gl_pnlCreateChannel.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblCreateChannel, GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlCreateChannel.setVerticalGroup(
				gl_pnlCreateChannel.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 23, Short.MAX_VALUE)
				.addComponent(lblCreateChannel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
				);
		pnlCreateChannel.setLayout(gl_pnlCreateChannel);

		JLabel lblChannelName = new JLabel("Channel name:");

		txtChanName = new JTextField();
		txtChanName.setColumns(10);

		JLabel lblAbbrev = new JLabel("Abbreviation (1 letter)");

		txtChanAbbrev = new JTextField();
		txtChanAbbrev.setColumns(10);

		JLabel lblImgColor = new JLabel("Image color:");

		lblImgColorDisp = new JLabel("     ");
		lblImgColorDisp.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblImgColorDisp.setOpaque(true);

		lblTxtColorDisp = new JLabel("     ");
		lblTxtColorDisp.setBorder(new LineBorder(new Color(0, 0, 0)));
		lblTxtColorDisp.setOpaque(true);

		lblImgColorDisp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Color color = colorChooser.display(jpanel, "Select a color for the image (false coloring, LUT)");
				if (color != null) {
					lblImgColorDisp.setBackground(color);
				}
			}
		});
		lblImgColorDisp.setBackground(Color.PINK);
		lblImgColorDisp.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));

		JLabel lblTextColor = new JLabel("Color in text:");
		lblTxtColorDisp.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				Color color = colorChooser.display(jpanel, "Select a color to be used for text (i.e. for a white channel, maybe choose black)");
				if (color != null) {
					lblTxtColorDisp.setBackground(color);
				}
			}
		});
		lblTxtColorDisp.setBorder(new LineBorder(new Color(0, 0, 0), 2, true));
		lblTxtColorDisp.setBackground(Color.PINK);



		btnAddChan = new JButton("Add Channel");
		btnAddChan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				removeError();
				String error = _addNewChannelToTable(txtChanName.getText(), txtChanAbbrev.getText(), lblImgColorDisp.getBackground(), lblTxtColorDisp.getBackground());
				if (error != null) {
					displayError(error);
				} else {
					txtChanAbbrev.setText("");
					txtChanName.setText("");
					lblTxtColorDisp.setBackground(Color.PINK);
					lblImgColorDisp.setBackground(Color.PINK);

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
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(pnlChannels, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
												.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
												.addComponent(pnlCreateChannel, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
												.addGroup(groupLayout.createSequentialGroup()
														.addComponent(lblChannelName)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(txtChanName, GroupLayout.PREFERRED_SIZE, 187, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(lblAbbrev, GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(txtChanAbbrev, GroupLayout.PREFERRED_SIZE, 42, GroupLayout.PREFERRED_SIZE))
												.addGroup(groupLayout.createSequentialGroup()
														.addComponent(lblImgColor)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(lblImgColorDisp, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
														.addGap(18)
														.addComponent(lblTextColor)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(lblTxtColorDisp, GroupLayout.PREFERRED_SIZE, 26, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED, 149, Short.MAX_VALUE)
														.addComponent(btnAddChan))))
								.addGroup(groupLayout.createSequentialGroup()
										.addGap(222)
										.addComponent(btnDeleteChan))
								.addGroup(groupLayout.createSequentialGroup()
										.addContainerGap()
										.addComponent(lblError))
								.addGroup(groupLayout.createSequentialGroup()
										.addContainerGap()
										.addComponent(lblInstructions, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)))
						.addContainerGap())
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(pnlChannels, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblInstructions)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 103, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(btnDeleteChan)
						.addGap(12)
						.addComponent(pnlCreateChannel, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(txtChanName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblChannelName)
								.addComponent(lblAbbrev)
								.addComponent(txtChanAbbrev, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(lblImgColor, GroupLayout.PREFERRED_SIZE, 16, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblImgColorDisp)
								.addComponent(lblTextColor)
								.addComponent(lblTxtColorDisp)
								.addComponent(btnAddChan))
						.addPreferredGap(ComponentPlacement.RELATED, 57, Short.MAX_VALUE)
						.addComponent(lblError)
						.addContainerGap())
				);

		table = new JTable(new ChannelTableModel());
		table.setFocusable(false);
		table.setDefaultRenderer(Color.class, new ColorRenderer(true));
		table.getTableHeader().setResizingAllowed(false);
		table.getTableHeader().setReorderingAllowed(false);
		table.setDefaultEditor(Color.class, new ColorEditor());
		table.getTableHeader().setBackground(new Color(60, 60, 60)); // Darker header
		table.getTableHeader().setForeground(new Color(200, 200, 200)); // Light text

		scrollPane.setViewportView(table);
		scrollPane.getViewport().setBackground(Color.WHITE);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);


		setLayout(groupLayout);

	}

	@Override
	public void update() {
		// Resizing columns
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

		TableColumn column = null;
		int totalWidth= 0;
		for (int i = 1; i <= 6; i++) {
			column = table.getColumnModel().getColumn(i);

			int width = headerRenderer.getTableCellRendererComponent(
					null, column.getHeaderValue(),
					false, false, 0, 0).getPreferredSize().width;
			if (i != 4 && i != 5) {
				width += 10;
			} else if (i == 6) {
				width +=10;
			} else {
				width += 10;
			}
			column.setPreferredWidth(width);
			totalWidth+=width;
		}
		column = table.getColumnModel().getColumn(0);
		scrollPane.setBackground(Color.LIGHT_GRAY);
		column.setPreferredWidth(scrollPane.getViewport().getWidth()- totalWidth);

	}

	@Override
	public void reset(Settings settings, boolean enabled) {

		update();

		removeError();

		_setExistingChannelsInTable(cmCopy.getOrderedChannels());
		this.lblImgColorDisp.setBackground(Color.PINK);
		this.lblTxtColorDisp.setBackground(Color.PINK);
		this.txtChanAbbrev.setText("");
		this.txtChanName.setText("");

		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}
		this.btnDeleteChan.setEnabled(enabled);
		this.btnAddChan.setEnabled(enabled);
		this.table.setEnabled(enabled);

	}

	public void setLocalCMCopy(ChannelManager cm) {
		this.cmCopy = cm;
	}

	@Override
	public String getPageName() {

		return "Channel Setup";
	}

	@Override
	public JPanel getRawComponent() {
		return this;
	}

	@Override
	public void applyFields(Settings settings) {

		if (!this.cmCopy.hasIdenticalChannels(settings.channelMan)) {

			settings.channelMan = this.cmCopy;
			settings.needsUpdate = true;

		}

	}

	@Override
	public boolean validateFields() {
		if (table.getRowCount() == 0) {
			displayError("There must be at least one channel.");
			return false;
		}
		String error = this.cmCopy.validate();
		if (error != null) {
			displayError(error);
			return false;
		}

		removeError();
		return true;
	}

	public void displayError(String error) {
		this.lblError.setText(error);
		this.lblError.setVisible(true);
	}

	public void removeError() {
		this.lblError.setVisible(false);
	}

	private void _setExistingChannelsInTable(List<Channel> channels) {
		ChannelTableModel model = (ChannelTableModel) table.getModel();
		model.setIgnoreValidation(true);
		while (table.getRowCount() > 0) {
			model.removeRow(0);
		}
		for (Channel chan : channels) {
			((ChannelTableModel) table.getModel()).addRow(new Object[] {chan.getName(), chan.getAbbrev() + "", chan.getImgColor(), 
					chan.getTxtColor(), this.cmCopy.isProcessChannel(chan), this.cmCopy.getPrimaryROIDrawChan().softEquals(chan), 
					this.cmCopy.getMappedIndex(chan)});
		}
		model.setIgnoreValidation(false);

	}

	private String _addNewChannelToTable(String name, String abbreviation, Color imgColor, Color txtColor) {
		ChannelTableModel model = (ChannelTableModel) table.getModel();
		model.setIgnoreValidation(true);

		Channel chan = null;
		try {
			chan = this.cmCopy.addChannel(name, abbreviation, imgColor, txtColor);
		} catch (IllegalArgumentException e) {
			return e.getMessage();

		}

		boolean drawROI = this.cmCopy.getPrimaryROIDrawChan() == null;
		if (drawROI) {
			this.cmCopy.setPrimaryROIDrawChan(chan);
		}
		boolean process = false;
		if (this.cmCopy.getProcessChannels().size() == 0) {
			process = true;
			this.cmCopy.setProcessChannel(chan, true);
		}
		model.addRow(new Object[] {chan.getName(), chan.getAbbrev() + "", chan.getImgColor(), chan.getTxtColor(), 
				process, drawROI, new Integer(-1)});
		model.setIgnoreValidation(false);
		return null;
	}

	private class ChannelTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 4884318560220466984L;
		private boolean dontValidateAdditions;

		public ChannelTableModel() {
			super();

			addColumn("Name");
			addColumn("Abbreviation");
			addColumn("Img Color");
			addColumn("Text Color");
			addColumn("Process");
			addColumn("ROI Select");
			addColumn("#");
		}

		/*
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the true/false columns would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Just return true because all columns are editable
		 */
		@Override
		public boolean isCellEditable(int row, int col) {
			return true;
		}

		@Override
		public void setValueAt(Object value, int row, int col) {
			Channel chan = cmCopy.getChannel((String) getValueAt(row, 0));
			if (dontValidateAdditions) {
				super.setValueAt(value, row, col);
				fireTableCellUpdated(row, col); // MAY NEED TO GET RID OF THIS TO AVOID INFINITE LOOPS.
				return;
			}
			try {
				switch (col) {
				case 0:	
					chan.setName((String) value);
					break;
				case 1:
					chan.setAbbrev((String) value);
					break;
					// column 2 and 3 are colors and don't need to be validated.
				case 2:
					chan.setImgColor((Color) value);
					break;
				case 3:
					chan.setTxtColor((Color) value);
					break;
				case 4: // Process
					Boolean process = (Boolean) value;
					if (!process) {
						// Setting false, make sure there's another channel marked for processing
						List<Channel> processChans = cmCopy.getProcessChannels();
						if (processChans.size() == 1 && processChans.get(0).equals(chan)) {
							throw new IllegalArgumentException("At least one channel must be set for processing!");
						}
						cmCopy.setProcessChannel(chan, false);
					} else {
						cmCopy.setProcessChannel(chan, true);
					}
					break;
				case 5: // Primary ROI select chan

					Boolean primaryROI = (Boolean) value;
					if (primaryROI) {
						// Set draw roi FALSE for all other channels
						for (int otherRow = 0; otherRow < getRowCount(); otherRow++) {
							if (otherRow != row) {
								super.setValueAt(Boolean.FALSE, otherRow, col);
							}
						}
						cmCopy.setPrimaryROIDrawChan(chan);
					} else {
						throw new IllegalArgumentException("Cannot unset a primary ROI channel");
					}
					break;
				case 6: // Channel mapping
					Integer valueToSet = -1;
					if ((value instanceof String) && ((String) value).equals("none")) {
						value = -1;
					} else {
						try {
							valueToSet = (Integer) value;
						} catch (Exception e) {
							throw new IllegalArgumentException("Invalid channel mapping number.");
						}
						if (valueToSet <= -1) {
							value = -1;	
						} else {
							value = valueToSet;
						}
					}
					cmCopy.setMappedIndex(chan, valueToSet);
					break;
				}

				super.setValueAt(value, row, col);
				fireTableCellUpdated(row, col); // MAY NEED TO GET RID OF THIS TO AVOID INFINITE LOOPS.
			} catch (IllegalArgumentException e) {
				// Value to set was not valid for the channel. Do not update.
			}

		}

		public void setIgnoreValidation(boolean ignore) {
			this.dontValidateAdditions = ignore;
		}

	}

	public class ColorRenderer extends JLabel
	implements TableCellRenderer {

		private static final long serialVersionUID = -7017205696451364571L;
		Border unselectedBorder = null;
		Border selectedBorder = null;
		boolean isBordered = true;

		public ColorRenderer(boolean isBordered) {
			this.isBordered = isBordered;
			setOpaque(true); //MUST do this for background to show up.
		}

		public Component getTableCellRendererComponent(
				JTable table, Object color,
				boolean isSelected, boolean hasFocus,
				int row, int column) {
			Color newColor = (Color)color;
			setBackground(newColor);
			if (isBordered) {
				if (isSelected) {
					if (selectedBorder == null) {
						selectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
								table.getSelectionBackground());
					}
					setBorder(selectedBorder);
				} else {
					if (unselectedBorder == null) {
						unselectedBorder = BorderFactory.createMatteBorder(2,5,2,5,
								table.getBackground());
					}
					setBorder(unselectedBorder);
				}
			}

			setToolTipText("RGB value: " + newColor.getRed() + ", "
					+ newColor.getGreen() + ", "
					+ newColor.getBlue());
			return this;
		}
	}

	public class ColorEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

		private static final long serialVersionUID = -6299579446154918577L;
		Color currentColor;
		JButton button;
		JColorChooser colorChooser;
		JDialog dialog;
		protected static final String EDIT = "edit";

		public ColorEditor() {
			button = new JButton();
			button.setActionCommand(EDIT);
			button.addActionListener(this);
			button.setBorderPainted(false);

			//Set up the dialog that the button brings up.
			colorChooser = new JColorChooser();
			dialog = JColorChooser.createDialog(button,
					"Pick a Color",
					true,  //modal
					colorChooser,
					this,  //OK button handler
					null); //no CANCEL button handler
		}

		public void actionPerformed(ActionEvent e) {
			if (EDIT.equals(e.getActionCommand())) {
				//The user has clicked the cell, so
				//bring up the dialog.
				button.setBackground(currentColor);
				colorChooser.setColor(currentColor);
				dialog.setVisible(true);

				fireEditingStopped(); //Make the renderer reappear.

			} else { //User pressed dialog's "OK" button.
				currentColor = colorChooser.getColor();
				currentColor = new Color(currentColor.getRed(), currentColor.getGreen(), currentColor.getBlue());
			}
		}

		//Implement the one CellEditor method that AbstractCellEditor doesn't.
		public Object getCellEditorValue() {
			return currentColor;
		}

		//Implement the one method defined by TableCellEditor.
		public Component getTableCellEditorComponent(JTable table,
				Object value,
				boolean isSelected,
				int row,
				int column) {
			currentColor = (Color)value;
			return button;
		}
	}

	@Override
	public boolean hasErrors() {
		return this.lblError.isVisible();
	}

}
class PnlSaveOptions extends JPanel implements SettingsPanel {


	private static final long serialVersionUID = -1576497004345094528L;
	protected JTextField fullPathName;
	protected JTextField folderName;
	private JLabel lblError;
	protected JButton btnBrowseFolders;
	protected CustomFileChooser fileChooser;
	private JPanel thisObject = this;
	private JPanel pnlSaveIntermediates;
	private JLabel lblSaveInt;
	private JCheckBox chkSaveInts;

	/**
	 * Create the panel.
	 */
	public PnlSaveOptions() {

		this.fileChooser = new CustomFileChooser(3, null, false);

		JPanel pnlOutputLocation = new JPanel();
		pnlOutputLocation.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlOutputLocation.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlOutputLocation.setBackground(new Color(50, 50, 50));

		JLabel lblOutputLocation = new JLabel("Save Location");
		lblOutputLocation.setForeground(new Color(0, 0, 0));

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
				List<File> files = null;
				if (settings != null && settings.recentOpenFileLocations != null) {
					files = fileChooser.open(thisObject, settings.recentOpenFileLocations);
				} else {
					files = fileChooser.open(thisObject, null);
				}

				if (files == null || files.size() == 0)
					return;

				List<File> fileRecents = fileChooser.getRecents();
				if (fileRecents != null && fileRecents.size() > 0) {
					if (settings.recentOpenFileLocations != null) {
						settings.recentOpenFileLocations.clear();
					} else {
						settings.recentOpenFileLocations = new ArrayList<File>();
					}
					settings.recentOpenFileLocations.addAll(fileRecents);
					settings.needsUpdate= true;
					boolean saved = SettingsManager.saveSettings(settings);

					if (!saved) {
						GUI.displayMessage("Could not save settings.", "Error Saving.", Preferences.SINGLETON_FRAME, JOptionPane.ERROR_MESSAGE);

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
		lblError.setFont(GUI.mediumBoldFont);
		lblError.setForeground(new Color(255, 100, 100));

		pnlSaveIntermediates = new JPanel();
		pnlSaveIntermediates.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlSaveIntermediates.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlSaveIntermediates.setBackground(new Color(50, 50, 50));

		lblSaveInt = new JLabel("Intermediate States");
		lblSaveInt.setForeground(new Color(0, 0, 0));
		GroupLayout gl_pnlSaveIntermediates = new GroupLayout(pnlSaveIntermediates);
		gl_pnlSaveIntermediates.setHorizontalGroup(
				gl_pnlSaveIntermediates.createParallelGroup(Alignment.LEADING)
				.addGap(0, 426, Short.MAX_VALUE)
				.addGap(0, 424, Short.MAX_VALUE)
				.addGroup(gl_pnlSaveIntermediates.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblSaveInt, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlSaveIntermediates.setVerticalGroup(
				gl_pnlSaveIntermediates.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 23, Short.MAX_VALUE)
				.addComponent(lblSaveInt, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
				);
		pnlSaveIntermediates.setLayout(gl_pnlSaveIntermediates);

		JLabel lblIntInstructions = new JLabel("<html>By checking the box below, the state of processing will be saved after object selection (including manual adjustments) and after ROI selection. You can then begin processing at one of these intermediate states at a later point. This may be useful, for instance, if you want to change the number of Bins for Binning Output but do not want to redo processing, object selection, and ROI selection.</html>");

		chkSaveInts = new JCheckBox("Save Intermediate States");

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(chkSaveInts)
								.addComponent(fullPathName, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addComponent(pnlOutputLocation, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
										.addComponent(lblFolderName)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(folderName, GroupLayout.DEFAULT_SIZE, 254, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.UNRELATED)
										.addComponent(btnBrowseFolders))
								.addComponent(lblIntInstructions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addComponent(pnlSaveIntermediates, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addComponent(lblFullPath)
								.addComponent(lblError, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE))
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
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(pnlSaveIntermediates, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(lblIntInstructions)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(chkSaveInts)
						.addPreferredGap(ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
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

		this.chkSaveInts.setSelected(settings.saveIntermediates);

		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}
		this.btnBrowseFolders.setEnabled(enabled);

	}

	@Override
	public void applyFields(Settings settings) {

		if (!fullPathName.getText().equals("")) {
			File file = new File(fullPathName.getText());
			settings.outputLocation = file;

		} else {
			settings.outputLocation = null;
		}
		settings.saveIntermediates = this.chkSaveInts.isSelected();
	}

	@Override
	public boolean validateFields() {
		if (!fullPathName.getText().equals("")) {
			File file = new File(fullPathName.getText());
			if (!file.exists()) {
				displayError("The selected output directory no longer exists.");
				return false;
			} else if (!file.canWrite()) {
				displayError("The directory you selected doesn't have write access.");
				return false;
			}
		}
		removeError();
		return true;
	}

	public String getPageName() {
		return "Saving";
	}

	public JPanel getRawComponent() {
		return this;
	}

	@Override
	public void update() {}

	@Override
	public boolean hasErrors() {
		return this.lblError.isVisible();
	}

}
class PnlBinOptions extends JPanel implements SettingsPanel {

	private static final long serialVersionUID = -6622171153906374924L;
	protected JSpinner spnNumBins;
	protected JCheckBox chkDrawBinLabels;
	protected JCheckBox chkCalcBins;
	protected JCheckBox chkExcludeOutsider;
	private JCheckBox chkCountOutsideAsOutermost;

	public PnlBinOptions() {

		JPanel pnlBinOptions = new JPanel();
		pnlBinOptions.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlBinOptions.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlBinOptions.setBackground(new Color(50, 50, 50));

		JLabel lblBinSettings = new JLabel("Bin Settings");
		lblBinSettings.setForeground(new Color(0, 0, 0));

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
		pnlBinOutput.setBackground(new Color(50, 50, 50));

		JLabel lblBinOutput = new JLabel("Binning Output");
		lblBinOutput.setForeground(new Color(0, 0, 0));
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
								.addComponent(chkExcludeOutsider)
								.addComponent(chkCountOutsideAsOutermost)
								.addComponent(pnlBinOptions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addComponent(pnlBinOutput, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE))
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
						.addComponent(chkCountOutsideAsOutermost))
				);
		setLayout(groupLayout);



	}

	@Override
	public void displayError(String error) {}

	@Override
	public void removeError() {}

	@Override
	public void applyFields(Settings settings) {

		settings.calculateBins = chkCalcBins.isSelected();
		settings.drawBinLabels = chkDrawBinLabels.isSelected();
		settings.excludePtsOutsideBin = chkExcludeOutsider.isSelected();
		settings.numberOfBins = (int) Math.round(((Number) spnNumBins.getValue()).doubleValue());
		settings.includePtsNearestBin = chkCountOutsideAsOutermost.isSelected();

	}

	@Override
	public boolean validateFields() {return true;}

	@Override
	public String getPageName() {return "Bins";}

	@Override
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
	public JPanel getRawComponent() {return this;}

	@Override
	public void update() {}

	@Override
	public boolean hasErrors() {
		return false;
	}

}
class PnlProcessingOptions extends JPanel implements SettingsPanel {

	private static final long serialVersionUID = -6622171153906374924L;
	private JLabel lblError;
	private JTextField txtMinThresh;
	private JTextField txtUnsharpRadius;
	private JTextField txtUnsharpWeight;
	private JTextField txtGaussianSigma;
	private JCheckBox chkPostProcessObj;
	private JCheckBox chkPostProcessObjDelete;

	public PnlProcessingOptions() {
		setPreferredSize(new Dimension(518, 384));

		JPanel pnlProcessingSettings = new JPanel();
		pnlProcessingSettings.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlProcessingSettings.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlProcessingSettings.setBackground(new Color(50, 50, 50));
		
		JPanel pnlObjectPostProcess = new JPanel();
		pnlObjectPostProcess.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlObjectPostProcess.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlObjectPostProcess.setBackground(new Color(50, 50, 50));

		JLabel lblProcessingSettings = new JLabel("Neuron Processing Parameter Values");
		lblProcessingSettings.setForeground(new Color(0, 0, 0));
		JLabel lblPostProcessingSettings = new JLabel("Neuron Overlap");
		lblPostProcessingSettings.setForeground(new Color(0, 0, 0));
		
		chkPostProcessObj = new JCheckBox("Enabled overlap detection features");
		chkPostProcessObjDelete = new JCheckBox("<html>Delete resources of post-processing (recommended--drastically reduces output file size, but then overlap "
				+ "analysis cannot be performed after the first run, i.e. via Intermediate Processing)</html>");
		chkPostProcessObjDelete.setVerticalTextPosition(SwingConstants.CENTER);
		
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
		
		GroupLayout gl_pnlPostProcessingSettings = new GroupLayout(pnlObjectPostProcess);
		gl_pnlPostProcessingSettings.setHorizontalGroup(
				gl_pnlPostProcessingSettings.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlPostProcessingSettings.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblPostProcessingSettings, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlPostProcessingSettings.setVerticalGroup(
				gl_pnlPostProcessingSettings.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblPostProcessingSettings, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		pnlObjectPostProcess.setLayout(gl_pnlPostProcessingSettings);

		lblError = new JLabel("Error:");
		lblError.setFont(GUI.mediumBoldFont);
		lblError.setForeground(new Color(255, 100, 100));

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
								.addComponent(pnlObjectPostProcess, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
								.addComponent(chkPostProcessObj)
								.addComponent(chkPostProcessObjDelete)
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


								)

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
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(pnlObjectPostProcess, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(chkPostProcessObj, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(chkPostProcessObjDelete, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
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
		this.chkPostProcessObj.setSelected(settings.processingPostObj);
		this.chkPostProcessObjDelete.setSelected(settings.processingPostObjDelete);

		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}
	}

	public void applyFields(Settings settings) {

		settings.processingMinThreshold = Integer.parseInt(this.txtMinThresh.getText());
		settings.processingUnsharpMaskRadius = Integer.parseInt(this.txtUnsharpRadius.getText());
		settings.processingUnsharpMaskWeight = Double.parseDouble(this.txtUnsharpWeight.getText());
		settings.processingGaussianSigma = Double.parseDouble(this.txtGaussianSigma.getText());
		settings.processingPostObj = this.chkPostProcessObj.isSelected();
		settings.processingPostObjDelete = this.chkPostProcessObjDelete.isSelected();

	}

	@Override
	public boolean validateFields() {
		try {
			int minThresh = Integer.parseInt(this.txtMinThresh.getText());
			if (minThresh < 0 || minThresh > 255)
				throw new Exception();
		} catch (Exception e) {
			displayError("Thresholding minimum must be an integer between 0 and 255.");
			return false;
		}

		try {
			int unsharpRadius = Integer.parseInt(this.txtUnsharpRadius.getText());
			if (unsharpRadius < 1 || unsharpRadius > 1000)
				throw new Exception();
		} catch (Exception e) {
			displayError("Unsharp mask radius must be an integer between 1 and 1000.");
			return false;
		}

		try {
			double unsharpWeight = Double.parseDouble(this.txtUnsharpWeight.getText());
			if (unsharpWeight < 0.1 || unsharpWeight > 0.9)
				throw new Exception();
		} catch (Exception e) {
			displayError("Unsharp mask radius must be a decimal between 0.1 and 0.9.");
			return false;
		}

		try {
			double gaussianSigma = Double.parseDouble(this.txtGaussianSigma.getText());
			if (gaussianSigma < 0.01 || gaussianSigma > 100)
				throw new Exception();
		} catch (Exception e) {
			displayError("Gaussian blur sigma must be a decimal between 0.01 and 100.");
			return false;
		}
		removeError();

		return true;
	}

	@Override
	public String getPageName() {
		return "Processing";
	}



	@Override
	public JPanel getRawComponent() {
		return this;
	}

	@Override
	public void update() {}



	@Override
	public boolean hasErrors() {
		return this.lblError.isVisible();
	}
}
class PnlImageOptions extends JPanel implements SettingsPanel {

	private static final long serialVersionUID = -6622171153906374924L;
	private JLabel lblError;

	private SimpleJList<String> calibrationList;
	private JPanel rawPanel;
	private boolean dontadjust = false;
	private JCheckBox chkEnforceLUTs;

	/**
	 * Create the panel.
	 */
	public PnlImageOptions() {

		this.rawPanel = this;

		JPanel pnlPixelConverstions = new JPanel();
		pnlPixelConverstions.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlPixelConverstions.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlPixelConverstions.setBackground(new Color(50, 50, 50));

		JLabel lblPixelConversions = new JLabel("Image Pixel Units");
		lblPixelConversions.setForeground(new Color(0, 0, 0));

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
		lblError.setFont(GUI.mediumBoldFont);
		lblError.setForeground(new Color(255, 100, 100));

		JLabel lblCalibration = new JLabel("Calibration (if not supplied by image file):");

		JScrollPane scrollPane = new JScrollPane();

		JButton btnNewCalibration = new JButton("New Calibration");
		btnNewCalibration.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String input = GUI.getInput("Select a name for this calibration:", "New Calibration", rawPanel);
				if (input == null || input.length() == 0)
					return;
				for (String otherCalibration : calibrationList.toList()) {
					if (otherCalibration.startsWith(input)) {
						GUI.displayMessage("This calibration name is already taken.", "New Calibration Error", rawPanel, JOptionPane.ERROR_MESSAGE);
						return;
					}
				}
				String calib = GUI.getInput("What is the name of the unit to convert pixels to (i.e. microns):", "New Calibration", rawPanel);
				if (calib == null || calib.length() == 0)
					return;
				String ratio = GUI.getInput("What is the conversion? (One pixel equals _____ " + calib + ")", "New Calibration", rawPanel);
				if (calib == null || calib.length() == 0)
					return;
				Double d;
				try {
					d = Double.parseDouble(ratio);
				} catch (Exception ex) {
					GUI.displayMessage("The conversion you provided was not a decimal number.", "New Calibration Error", rawPanel, JOptionPane.ERROR_MESSAGE);
					return;
				}

				if (d <= 0) {
					GUI.displayMessage("The conversion you provided was negative. This is invalid.", "New Calibration Error", rawPanel, JOptionPane.ERROR_MESSAGE);
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
					GUI.displayMessage("You must have at least one calibration", "Calibration Error", Preferences.SINGLETON_FRAME, JOptionPane.ERROR_MESSAGE);
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

		JPanel pnlColoring = new JPanel();
		pnlColoring.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlColoring.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlColoring.setBackground(new Color(50, 50, 50));

		JLabel lblColoring = new JLabel("Coloring");
		lblColoring.setForeground(new Color(0, 0, 0));
		GroupLayout gl_pnlColoring = new GroupLayout(pnlColoring);
		gl_pnlColoring.setHorizontalGroup(
				gl_pnlColoring.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 492, Short.MAX_VALUE)
				.addGroup(gl_pnlColoring.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblColoring, GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlColoring.setVerticalGroup(
				gl_pnlColoring.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 23, Short.MAX_VALUE)
				.addComponent(lblColoring, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
				);
		pnlColoring.setLayout(gl_pnlColoring);

		chkEnforceLUTs = new JCheckBox("Enforce LUTs (false coloring)");

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(chkEnforceLUTs)
								.addComponent(pnlPixelConverstions, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addComponent(lblCalibration)
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(btnRemove)
										.addPreferredGap(ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
										.addComponent(btnNewCalibration))
								.addComponent(pnlColoring, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblError))
						.addContainerGap())
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
								.addComponent(btnRemove)
								.addComponent(btnNewCalibration))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(pnlColoring, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addComponent(chkEnforceLUTs)
						.addPreferredGap(ComponentPlacement.RELATED, 86, Short.MAX_VALUE)
						.addComponent(lblError)
						.addContainerGap())
				);

		scrollPane.setViewportView(calibrationList);
		setLayout(groupLayout);


	}

	public void reset(Settings settings, boolean enabled) {

		this.calibrationList.setItems(settings.calibrations);
		this.calibrationList.setSelectedIndex(settings.calibrationNumber - 1);
		this.chkEnforceLUTs.setSelected(settings.enforceLUTs);
		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}
		this.calibrationList.setEnabled(enabled);
	}

	public String getPageName() {
		return "Image Metrics";
	}

	public JPanel getRawComponent() {
		return this;
	}

	public void applyFields(Settings settings) {
		settings.calibrations = this.calibrationList.toList();
		settings.calibrationNumber = this.calibrationList.getSelectedIndex() + 1;
		settings.enforceLUTs = this.chkEnforceLUTs.isSelected();
	}

	public boolean validateFields() {return true;}

	public void displayError(String error) {
		this.lblError.setText(error);
		this.lblError.setVisible(true);
	}

	public void removeError() {
		this.lblError.setVisible(false);
	}

	@Override
	public void update() {}

	@Override
	public boolean hasErrors() {
		return this.lblError.isVisible();
	}
}
class PnlReset extends JPanel implements SettingsPanel {

	private static final long serialVersionUID = 2016027296304991330L;
	public final Preferences prefs;

	public PnlReset(Preferences prefs) {
		this.prefs = prefs;
		JPanel pnlPixelConverstions = new JPanel();
		pnlPixelConverstions.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlPixelConverstions.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlPixelConverstions.setBackground(new Color(50, 50, 50));

		JLabel lblPixelConversions = new JLabel("Hard Reset");
		lblPixelConversions.setForeground(new Color(0, 0, 0));

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

		JLabel lblbyClickingreset = new JLabel("<html>By clicking 'Reset to Default' below, you will reset all settings to their default values. This action cannot be undone. This will affect your Channel configuration and you will likely need to re-map channels following a reset. In addition, you may also need to re-select an output folder.</html>");

		JButton btnReset = new JButton("Reset to Defaults");
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (GUI.confirmWithUser("Are you sure you want to reset settings?", "Confirm Reset", Preferences.SINGLETON_FRAME, JOptionPane.YES_NO_OPTION)) {
					Settings oldSettings = GUI.settings;
					try {
						GUI.settings = null;
						GUI.settings = SettingsManager.loadSettings(true);
						SettingsManager.saveSettings(GUI.settings);

						prefs.invokeReset(true);
						prefs.removeDisplay();
					} catch (Exception ex) {
						ex.printStackTrace();
						if (GUI.settings == null) {
							GUI.displayMessage("There was an error loading settings:<br><br>" + ex.getMessage() + "</html>", "Settings Load Error", Preferences.SINGLETON_FRAME, JOptionPane.ERROR_MESSAGE);
						} else {
							GUI.displayMessage("There was an error updating settings:<br><br>" + ex.getMessage() + "</html>", "Settings Update Error", Preferences.SINGLETON_FRAME, JOptionPane.ERROR_MESSAGE);

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
	public void applyFields(Settings settings) {}

	@Override
	public boolean validateFields() {return true;}

	@Override
	public void displayError(String errors) {}

	@Override
	public String getPageName() {
		return "Reset";
	}

	@Override
	public JPanel getRawComponent() {
		return this;
	}

	@Override
	public void update() {}

	@Override
	public boolean hasErrors() {
		return false;
	}

}
class PnlOutputOptions extends JPanel implements SettingsPanel, ChannelSettingsHandler {

	private static final long serialVersionUID = -6622171153906374924L;
	private SimpleJList<OutputParams> listOutputROI;
	private SimpleJList<OutputParams> listOptionsROI;
	private SimpleJList<OutputParams> listOutputObj;
	private SimpleJList<OutputParams> listOptionsObj;

	private JButton mvRightObj;
	private JButton mvLeftObj;
	private JButton mvRightROI;
	private JButton mvLeftROI;
	private MultiSelectPopup<Channel> chanSelectPopup;

	private ChannelManager cmCopy;

	/**
	 * Create the panel.
	 */
	public PnlOutputOptions() {

		this.chanSelectPopup = new MultiSelectPopup<Channel>();

		JPanel pnlPixelConverstions = new JPanel();
		pnlPixelConverstions.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlPixelConverstions.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlPixelConverstions.setBackground(new Color(211, 211, 211));

		JLabel lblPixelConversions = new JLabel("Slice Selection Output");

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


		JPanel panel = new JPanel();
		panel.setFont(new Font("Arial", Font.PLAIN, 13));
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel.setBackground(new Color(50, 50, 50));

		JLabel lblObjectSelectionOutput = new JLabel("Object Selection Output");
		lblObjectSelectionOutput.setForeground(new Color(0, 0, 0));
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 492, Short.MAX_VALUE)
				.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblObjectSelectionOutput, GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 23, Short.MAX_VALUE)
				.addComponent(lblObjectSelectionOutput, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
				);
		panel.setLayout(gl_panel);

		JScrollPane scrOptionsObj = new JScrollPane();

		JLabel lblOptions2 = new JLabel("Options");

		mvRightObj = new JButton(">>");
		mvRightObj.setMargin(new Insets(0, -30, 0, -30));

		mvLeftObj = new JButton("<<");
		mvLeftObj.setMargin(new Insets(0, -30, 0, -30));

		JScrollPane scrOutputObj = new JScrollPane();

		JLabel lblOutput2 = new JLabel("Output");

		JPanel panel_1 = new JPanel();
		panel_1.setFont(new Font("Arial", Font.PLAIN, 13));
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.setBackground(new Color(50, 50, 50));

		JLabel lblRoiLineSelection = new JLabel("ROI Line Selection Output");
		lblRoiLineSelection.setForeground(new Color(0, 0, 0));
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 492, Short.MAX_VALUE)
				.addGroup(gl_panel_1.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblRoiLineSelection, GroupLayout.DEFAULT_SIZE, 468, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_panel_1.setVerticalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 23, Short.MAX_VALUE)
				.addComponent(lblRoiLineSelection, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 23, Short.MAX_VALUE)
				);
		panel_1.setLayout(gl_panel_1);

		JScrollPane scrOptionsROI = new JScrollPane();

		JLabel lblOptions3 = new JLabel("Options");

		mvRightROI = new JButton(">>");
		mvRightROI.setMargin(new Insets(0, -30, 0, -30));

		mvLeftROI = new JButton("<<");
		mvLeftROI.setMargin(new Insets(0, -30, 0, -30));

		JScrollPane scrOutputROI = new JScrollPane();

		JLabel lblOutput3 = new JLabel("Output");

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING, false)
								.addComponent(panel, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(lblOptions2, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
										.addGap(235)
										.addComponent(lblOutput2, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(scrOptionsObj, GroupLayout.PREFERRED_SIZE, 224, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(mvRightObj, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
												.addComponent(mvLeftObj, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(scrOutputObj, GroupLayout.PREFERRED_SIZE, 225, GroupLayout.PREFERRED_SIZE))
								.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 494, GroupLayout.PREFERRED_SIZE)
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(lblOptions3, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
										.addGap(235)
										.addComponent(lblOutput3, GroupLayout.PREFERRED_SIZE, 44, GroupLayout.PREFERRED_SIZE))
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(scrOptionsROI, GroupLayout.PREFERRED_SIZE, 224, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(mvRightROI, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
												.addComponent(mvLeftROI, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(scrOutputROI, GroupLayout.PREFERRED_SIZE, 225, GroupLayout.PREFERRED_SIZE)))
						.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(panel, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(lblOptions2)
												.addComponent(lblOutput2))
										.addGap(6)
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(scrOptionsObj, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
												.addComponent(scrOutputObj, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE))
										.addPreferredGap(ComponentPlacement.RELATED))
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(mvRightObj, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
										.addGap(6)
										.addComponent(mvLeftObj, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
										.addGap(44)))
						.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(lblOptions3)
												.addComponent(lblOutput3))
										.addPreferredGap(ComponentPlacement.RELATED)
										.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(scrOutputROI, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
												.addComponent(scrOptionsROI, GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE))
										.addGap(20))
								.addGroup(groupLayout.createSequentialGroup()
										.addComponent(mvRightROI, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
										.addGap(6)
										.addComponent(mvLeftROI, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
										.addGap(58))))
				);
		OutputOptionRenderer<OutputParams> renderer = new OutputOptionRenderer<OutputParams>();
		MouseAdapter mouseAdaptor = new MouseAdapter() {
			public void mouseReleased(MouseEvent evt) {
				@SuppressWarnings("unchecked")
				SimpleJList<OutputParams> list = (SimpleJList<OutputParams>)evt.getSource();
				if (evt.getClickCount() == 2) {

					// Double-click detected
					int index = list.locationToIndex(evt.getPoint());
					if (index > -1) {
						OutputParams output = list.getElementAt(index);
						
						if (output.getOption().getRestrictedOption() == OutputOption.NO_CHANS)
							return; // don't need to select channels, doesn't make sense for this option
						
						List<Channel> newSelected = chanSelectPopup.getUserInput(Preferences.SINGLETON_FRAME, "Select channels:", cmCopy.getOrderedChannels(), output.getContainedChannels());
						if (newSelected != null) {
							cmCopy.setOutputParamChannels(output, new HashSet<Channel>(newSelected));
							list.refresh();
						}
					}
				}
			}
		};

		listOutputROI = new SimpleJList<OutputParams>();
		listOutputROI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOutputROI.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (((SimpleJList<?>) e.getSource()).getSelectedIndex() != -1) {
						listOptionsROI.clearSelection();
					}
				}
			}
		});
		listOutputROI.setCellRenderer(renderer);
		listOutputROI.addMouseListener(mouseAdaptor);
		scrOutputROI.setViewportView(listOutputROI);

		listOptionsROI = new SimpleJList<OutputParams>();
		listOptionsROI.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOptionsROI.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (((SimpleJList<?>) e.getSource()).getSelectedIndex() != -1) {
						listOutputROI.clearSelection();
					}
				}
			}
		});
		listOptionsROI.setCellRenderer(renderer);
		listOptionsROI.addMouseListener(mouseAdaptor);
		scrOptionsROI.setViewportView(listOptionsROI);

		listOutputObj = new SimpleJList<OutputParams>();
		listOutputObj.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOutputObj.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (((SimpleJList<?>) e.getSource()).getSelectedIndex() != -1) {
						listOptionsObj.clearSelection();
					}
				}
			}
		});
		listOutputObj.setCellRenderer(renderer);
		listOutputObj.addMouseListener(mouseAdaptor);
		scrOutputObj.setViewportView(listOutputObj);

		listOptionsObj = new SimpleJList<OutputParams>();
		listOptionsObj.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listOptionsObj.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (!e.getValueIsAdjusting()) {
					if (((SimpleJList<?>) e.getSource()).getSelectedIndex() != -1) {
						listOutputObj.clearSelection();
					}
				}
			}
		});
		listOptionsObj.setCellRenderer(renderer);
		listOptionsObj.addMouseListener(mouseAdaptor);
		scrOptionsObj.setViewportView(listOptionsObj);


		setLayout(groupLayout);

		this.mvLeftObj.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				OutputParams params = listOutputObj.getSelectedValue();
				if (params != null) {
					listOutputObj.removeItem(params);
					listOptionsObj.addItem(params);
					cmCopy.setOutputOptionEnabled(params.getOption(), params, false);
				}

			}
		});
		this.mvRightObj.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				OutputParams params = listOptionsObj.getSelectedValue();
				if (params != null) {
					listOptionsObj.removeItem(params);
					listOutputObj.addItem(params);
					cmCopy.setOutputOptionEnabled(params.getOption(), params, true);
				}

			}
		});
		this.mvLeftROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				OutputParams params = listOutputROI.getSelectedValue();
				if (params != null) {

					listOutputROI.removeItem(params);
					listOptionsROI.addItem(params);
					cmCopy.setOutputOptionEnabled(params.getOption(), params, false);
				}
			}
		});
		this.mvRightROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				OutputParams params = listOptionsROI.getSelectedValue();
				if (params != null) {
					listOptionsROI.removeItem(params);
					listOutputROI.addItem(params);
					cmCopy.setOutputOptionEnabled(params.getOption(), params, true);
				}

			}
		});


	}

	private static class OutputOptionRenderer<K> implements ListCellRenderer<K> {

		private static Font smallFont = new Font("PingFang TC", Font.BOLD, 12);
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {


			JLabel renderer = (JLabel) defaultRenderer
					.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);

			if (value instanceof OutputParams) {
				OutputParams option = (OutputParams) value;
				renderer.setFont(smallFont);
				renderer.setText(option.toString());
				renderer.setBorder(new EmptyBorder(0,0,0,0));
			} else if (value != null) {
				renderer.setText(value.toString());
			}

			return renderer;


		}


	}

	@Override
	public void removeError() {}

	@Override
	public void reset(Settings settings, boolean enabled) {
		this.mvLeftObj.setEnabled(enabled);
		this.mvRightObj.setEnabled(enabled);
		this.mvLeftROI.setEnabled(enabled);
		this.mvRightROI.setEnabled(enabled);

		this.listOptionsObj.setEnabled(enabled);
		this.listOptionsROI.setEnabled(enabled);
		this.listOutputObj.setEnabled(enabled);
		this.listOutputROI.setEnabled(enabled);
		this.listOptionsObj.clear();
		this.listOptionsROI.clear();
		this.listOutputObj.clear();
		this.listOutputROI.clear();
		LinkedHashMap<OutputOption, OutputParams> enabledOptions = this.cmCopy.getCopyOfOutputParamMapping(true);
		LinkedHashMap<OutputOption, OutputParams> disabledOptions = this.cmCopy.getCopyOfOutputParamMapping(false);

		for (OutputOption option : OutputOption.values()) {
			if (enabledOptions.containsKey(option)) {
				switch (option.getList()) {
				case 1:
					this.listOutputObj.addItem(enabledOptions.get(option));
					break;
				case 2:
					this.listOutputROI.addItem(enabledOptions.get(option));
					break;
				}
			} else {
				switch (option.getList()) {
				case 1:
					this.listOptionsObj.addItem(disabledOptions.get(option));
					break;
				case 2:
					this.listOptionsROI.addItem(disabledOptions.get(option));
					break;
				}
			}
		}

	}

	@Override
	public void applyFields(Settings settings) {

		this.cmCopy.correctOutputParams();
		if (settings != null && this.cmCopy.hasChangesInOutputParams()) {
			settings.channelMan = this.cmCopy;
			settings.needsUpdate = true;
			settings.channelMan.setChangesInOutputParams(false);
		} else {
			this.cmCopy.setChangesInOutputParams(false);
		}

	}

	@Override
	public boolean validateFields() {return true;}

	@Override
	public void displayError(String errors) {}

	@Override
	public String getPageName() {
		return "Output Files";
	}

	@Override
	public JPanel getRawComponent() {
		return this;
	}

	@Override
	public void update() {}

	@Override
	public void setLocalCMCopy(ChannelManager cm) {
		this.cmCopy = cm;	
	}

	@Override
	public boolean hasErrors() {
		return false;
	}

}
class PnlTemplates extends JPanel implements SettingsPanel, ListDropReceiver {

	private static final long serialVersionUID = -8280614585837669978L;
	private final SimpleJList<String> templateList;
	private final JButton btnLoadTemplate;
	private final JButton btnNewTemplate;
	private final JButton btnLoadFromRun;
	private boolean shouldUpdate = false;
	private JLabel lblError;
	private final Preferences prefs;
	private final CustomFileChooser fileChooser;

	public PnlTemplates(Preferences prefs) {
		this.prefs = prefs;
		this.fileChooser = new CustomFileChooser(2, new ArrayList<String>(Arrays.asList("ser")), false);
		JPanel pnlTemplates = new JPanel();
		pnlTemplates.setFont(new Font("Arial", Font.PLAIN, 13));
		pnlTemplates.setBorder(new LineBorder(new Color(0, 0, 0)));
		pnlTemplates.setBackground(new Color(50, 50, 50));

		JLabel lblTemplates = new JLabel("Settings Templates");
		lblTemplates.setForeground(new Color(0, 0, 0));

		GroupLayout gl_pnlTemplates = new GroupLayout(pnlTemplates);
		gl_pnlTemplates.setHorizontalGroup(
				gl_pnlTemplates.createParallelGroup(Alignment.LEADING)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGap(0, 494, Short.MAX_VALUE)
				.addGroup(gl_pnlTemplates.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblTemplates, GroupLayout.DEFAULT_SIZE, 492, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlTemplates.setVerticalGroup(
				gl_pnlTemplates.createParallelGroup(Alignment.LEADING)
				.addGap(0, 25, Short.MAX_VALUE)
				.addGap(0, 25, Short.MAX_VALUE)
				.addComponent(lblTemplates, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 33, Short.MAX_VALUE)
				);
		pnlTemplates.setLayout(gl_pnlTemplates);

		JScrollPane scrollPane = new JScrollPane();

		lblError = new JLabel("Error");
		lblError.setVisible(false);
		lblError.setFont(GUI.mediumBoldFont);
		lblError.setForeground(new Color(255, 100, 100));

		btnNewTemplate = new JButton("Create From Current");
		btnNewTemplate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				String name = GUI.getInput("Select a name for this settings template:", "Create Settings Template", prefs);

				if (name == null || name.isEmpty()) {
					return;
				} else if (!StringUtils.isAlphanumericSpace(name)) {
					GUI.displayMessage("The name of a settings template can only contain numbers, letters, and spaces.", 
							"Settings Tempalte Error", prefs, JOptionPane.ERROR_MESSAGE);
					return;
				}

				String fileName = "settingsTemplate" + name.replaceAll(" ", "_").trim() + ".yml";
				File file = new File(Settings.SettingsManager.settingsMainPath + File.separator + fileName);
				if (file.exists()) {
					GUI.displayMessage("A template with this name already exists.", 
							"Settings Template Error", prefs, JOptionPane.ERROR_MESSAGE);
					return;
				}

				Object errors = prefs.applyPreferences(fileName);

				if (errors != null) {

					if (errors instanceof SettingsPanel) {
						displayError("Could not create template – the current configuration has errors.");
						return;
					} else {
						GUI.displayMessage("Could not create template - could not write preferences:<br><br>" + errors.toString(), "I/O Failure", prefs, JOptionPane.ERROR_MESSAGE);
						return;
					}

				}

				refreshTemplatesList();

			}
		});


		btnLoadTemplate = new JButton("Load Selected");
		btnLoadTemplate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String value = templateList.getSelectedValue();
				if (value == null)
					return;

				String fileName = "settingsTemplate" + value.replaceAll(" ", "_") + ".yml";
				File file = new File(Settings.SettingsManager.settingsMainPath + File.separator + fileName);
				if (!file.isFile()) {
					GUI.displayMessage("Error: the template file no longer exists.", 
							"Settings Template Error", prefs, JOptionPane.ERROR_MESSAGE);
					templateList.setSelectedIndex(-1);
					templateList.removeItem(value);
					return;
				}

				if (!GUI.confirmWithUser("If you have not created a template from the current configuration, loading "
						+ "a template will cause these settings to be lost. Would you like to continue?", 
						"Potentialy Data Loss", prefs, JOptionPane.WARNING_MESSAGE))
					return;

				try {
					Files.copy(file, new File(SettingsManager.settingsMainPath + File.separator + SettingsManager.settingsFileName));
				} catch (IOException e1) {
					GUI.displayMessage("Fatal: an error occurred while writing settings. Please retry.", 
							"Write Failure", prefs, JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
					return;
				}

				Settings settings = null;
				try {
					settings = SettingsManager.loadSettings(false);
					if (settings.needsUpdate) {

						SettingsManager.saveSettings(settings);
						SettingsManager.saveSettings(settings, fileName);

					}
				} catch (Exception ex) {
					ex.printStackTrace();
					if (settings == null) {
						GUI.displayMessage("There was an error loading settings:<br><br>" + ex.getMessage(), "Settings Load Error", prefs,  JOptionPane.ERROR_MESSAGE);
					} else {
						GUI.displayMessage("<html>There was an error updating settings:<br><br>" + ex.getMessage(), "Settings Update Error", prefs,  JOptionPane.ERROR_MESSAGE);

					}
					return;
				}

				GUI.settings = settings;


				prefs.resetPreferences(true);

			}
		});

		btnLoadFromRun = new JButton("Load From Run");
		btnLoadFromRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				List<File> files = fileChooser.open(prefs, GUI.settings.recentOpenFileLocations);
				if (files == null || files.isEmpty())
					return;
				
				_processCreateFromRunButton(files);

			}
		});
		btnLoadFromRun.setToolTipText(GUI.getTooltipText("Loads Channel Setup and Output Files from a previous run, keeping other preferences."));
		
		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout.setHorizontalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addComponent(pnlTemplates, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE)
								.addGroup(Alignment.TRAILING, groupLayout.createSequentialGroup()
										.addComponent(btnLoadTemplate)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnLoadFromRun)
										.addPreferredGap(ComponentPlacement.RELATED, 282, Short.MAX_VALUE)
										.addComponent(btnNewTemplate))
								.addComponent(lblError, GroupLayout.DEFAULT_SIZE, 494, Short.MAX_VALUE))
						.addContainerGap())
				);
		groupLayout.setVerticalGroup(
				groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
						.addContainerGap()
						.addComponent(pnlTemplates, GroupLayout.PREFERRED_SIZE, 25, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 127, GroupLayout.PREFERRED_SIZE)
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
								.addComponent(btnLoadTemplate)
								.addComponent(btnLoadFromRun)
								.addComponent(btnNewTemplate))
						.addPreferredGap(ComponentPlacement.RELATED, 110, Short.MAX_VALUE)
						.addComponent(lblError)
						.addContainerGap())
				);

		templateList = new SimpleJList<String>(this);
		templateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(templateList);
		setLayout(groupLayout);
	}

	@Override
	public void removeError() {
		this.lblError.setVisible(false);
	}

	private void refreshTemplatesList() {
		this.templateList.clear();
		File settingsDir = new File(Settings.SettingsManager.settingsMainPath);
		if (settingsDir.exists() && settingsDir.isDirectory()) {
			for (File file : settingsDir.listFiles()) {
				if (file.exists() && file.isFile() && file.getName().startsWith("settingsTemplate")) {
					String rawName = file.getName().substring(0, file.getName().indexOf(".")).replaceFirst("settingsTemplate", "");
					this.templateList.addItem(rawName.replaceAll("_", " "));
				}
			}
		}
	}

	@Override
	public void reset(Settings settings, boolean enabled) {

		refreshTemplatesList();

		for (Component component : this.getComponents()) {
			component.setEnabled(enabled);
		}

		this.btnLoadTemplate.setEnabled(enabled);
		this.btnNewTemplate.setEnabled(enabled);
		this.btnLoadFromRun.setEnabled(enabled);
		removeError();

	}

	@Override
	public void applyFields(Settings settings) {
		if (shouldUpdate)
			settings.needsUpdate = true;
	}

	@Override
	public boolean validateFields() {
		removeError();
		return true;
	}

	@Override
	public void displayError(String errors) {
		this.lblError.setText(errors);
		this.lblError.setVisible(true);
	}

	@Override
	public String getPageName() {
		return "Templates";
	}

	@Override
	public JPanel getRawComponent() {
		return this;
	}

	@Override
	public void update() {}

	@Override
	public boolean hasErrors() {
		return false;
	}

	private void _processCreateFromRunButton(List<File> files) {
		
		if (!GUI.confirmWithUser("You are about to load configuration values from a previous run. If you have not saved current settings "
				+ "as a template, some of these will be lost. Do you want to continue?", "Confirm Template Creation", prefs, JOptionPane.WARNING_MESSAGE)) {
			return;
		}
		
		if (files == null || files.isEmpty())
			return;

		File file = files.get(0);

		File fileToOpen = null;
		
		if (file.isDirectory()) {
			File[] possibleSerFiles = null;
			if (file.getName().equals("Serialization")) {
				possibleSerFiles = file.listFiles();
			} else if (file.getName().equals("Intermediate Files")) {
				File serFileDir = new File(file.getPath() + File.separator + "Serialization");
				if (serFileDir.exists() && serFileDir.isDirectory()) {
					possibleSerFiles = serFileDir.listFiles();
				} else {
					GUI.displayMessage("The Intermediate Files folder (<em>" + file.getPath() + "</em>) does not contain a 'Serialization' folder.", "File Selection Error", null, JOptionPane.ERROR_MESSAGE);
					return;
				}
			} else {
				File serFileDir = new File(file.getPath() + File.separator + "Intermediate Files" + File.separator + "Serialization");
				if (serFileDir.exists() && serFileDir.isDirectory()) {
					possibleSerFiles = serFileDir.listFiles();
					
				} else {
					GUI.displayMessage("The image output folder (<em>" + file.getPath() + "</em>) does not contain a 'Serialization' folder within the 'Intermediate Files' folder.", "File Selection Error", null, JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			for (File possibleSerFile : possibleSerFiles) {
				if (possibleSerFile.getName().equals("postroistate.ser") || possibleSerFile.getName().equals("postobjstate.ser")) {
					fileToOpen = possibleSerFile;
					break;
				}

			}
			if (fileToOpen == null) {
				GUI.displayMessage("The folder at <em>" + file.getPath() + "</em> does not contain any intermediate state file in its child folders.", "File Selection Error", prefs, JOptionPane.ERROR_MESSAGE);
				return;
			}

		} else {
			if (!file.getName().endsWith(".ser")) {
				// shouldn't happen if FileBrowser functions correctly
				GUI.displayMessage("The file <em>" + file.getName() + "</em> does not have a valid extension (.ser).", "File Selection Error", prefs, JOptionPane.ERROR_MESSAGE);
				return;
			} else if (file.getName().equals("postroistate.ser") || file.getName().equals("postobjstate.ser")) {
				fileToOpen = file;
			} else {
				GUI.displayMessage("The file <em>" + file.getName() + "</em> does not have a valid name (postobjstate.ser or postroistate.ser).", "File Selection Error", prefs, JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
				
		ChannelManager cmToSet = null;
		
		if (fileToOpen.getName().equals("postroistate.ser")) {

			try {
				ROIEditableImage roiEditableImage = ROIEditableImage.loadROIEditableImage(fileToOpen);
				if (roiEditableImage == null)
					throw new NullPointerException();
				
				cmToSet = roiEditableImage.getRunConfig().channelMan;
			} catch (Exception e) {
				GUI.displayMessage("The file <em>" + fileToOpen.getName() + "</em> could not be opened for some reason.", "File Open Error", prefs, JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {

			try {
				ObjectEditableImage objEditableImage = ObjectEditableImage.loadObjEditableImage(fileToOpen);
				if (objEditableImage == null)
					throw new NullPointerException();
				
				cmToSet = objEditableImage.getRunConfig().channelMan;
			} catch (Exception e) {
				GUI.displayMessage("The file <em>" + fileToOpen.getName() + "</em> could not be opened for some reason.", "File Open Error", prefs, JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		String templateName = GUI.getInput("Select a name for this settings template:", "Create Settings Template", prefs);

		if (templateName == null || templateName.isEmpty()) {
			return;
		} else if (!StringUtils.isAlphanumericSpace(templateName)) {
			GUI.displayMessage("The name of a settings template can only contain numbers, letters, and spaces.", 
					"Settings Tempalte Error", prefs, JOptionPane.ERROR_MESSAGE);
			return;
		}

		String fileName = "settingsTemplate" + templateName.replaceAll(" ", "_").trim() + ".yml";
		File settingsFile = new File(Settings.SettingsManager.settingsMainPath + File.separator + fileName);
		if (settingsFile.exists()) {
			GUI.displayMessage("A template with this name already exists.", 
					"Settings Template Error", prefs, JOptionPane.ERROR_MESSAGE);
			return;
		}
				
		for (SettingsPanel settingsPanel : prefs.getSettingsPages()) {
			if (settingsPanel instanceof ChannelSettingsHandler) {
				((ChannelSettingsHandler) settingsPanel).setLocalCMCopy(cmToSet);
				settingsPanel.reset(GUI.settings, true);
			}
		}
		
		Object errors = prefs.applyPreferences(fileName);

		if (errors != null) {

			if (errors instanceof SettingsPanel) {
				displayError("Could not create template – the current configuration has errors.");
				return;
			} else {
				GUI.displayMessage("Could not create template - could not write preferences:<br><br>" + errors.toString(), "I/O Failure", prefs, JOptionPane.ERROR_MESSAGE);
				return;
			}

		}

		refreshTemplatesList();
		

	}
	
	public void _createTemplateFromCurrentConfig() {
		
		String name = GUI.getInput("Select a name for this settings template:", "Create Settings Template", prefs);

		if (name == null || name.isEmpty()) {
			return;
		} else if (!StringUtils.isAlphanumericSpace(name)) {
			GUI.displayMessage("The name of a settings template can only contain numbers, letters, and spaces.", 
					"Settings Tempalte Error", prefs, JOptionPane.ERROR_MESSAGE);
			return;
		}

		String fileName = "settingsTemplate" + name.replaceAll(" ", "_").trim() + ".yml";
		File file = new File(Settings.SettingsManager.settingsMainPath + File.separator + fileName);
		if (file.exists()) {
			GUI.displayMessage("A template with this name already exists.", 
					"Settings Template Error", prefs, JOptionPane.ERROR_MESSAGE);
			return;
		}

		Object errors = prefs.applyPreferences(fileName);

		if (errors != null) {

			if (errors instanceof SettingsPanel) {
				displayError("Could not create template – the current configuration has errors.");
				return;
			} else {
				GUI.displayMessage("Could not create template - could not write preferences:<br><br>" + errors.toString(), "I/O Failure", prefs, JOptionPane.ERROR_MESSAGE);
				return;
			}

		}

		refreshTemplatesList();
		
	}

	@Override
	public void dropped(List<Object> dropped) {

		if (!this.btnLoadFromRun.isEnabled()) {
			return;
		}

		List<File> files = new ArrayList<File>();
		for (Object obj : dropped) {
			if (obj instanceof File && ((File) obj).exists()) {
				files.add((File) obj);
			}
		}

		_processCreateFromRunButton(files);

	}

}

