package com.typicalprojects.CellQuant.neuronal_migration;

import java.awt.Component;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.typicalprojects.CellQuant.util.ImageContainer.Channel;
import com.typicalprojects.CellQuant.neuronal_migration.Settings.SettingsLoader;
import com.typicalprojects.CellQuant.util.FileBrowser;
import com.typicalprojects.CellQuant.util.SimpleJList;

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
	private JPanel pnlOptionsChanConfig;
	private GUI mainGUI;
	private JLabel lblPageName;
	private JCheckBox chkSelectGreen;
	private JCheckBox chkSelectRed;
	private JCheckBox chkSelectBlue;
	private JCheckBox chkSelectWhite;
	private JComboBox<String> comBoxCh3;
	private JComboBox<String> comBoxCh2;
	private JComboBox<String> comBoxCh1;
	private JComboBox<String> comBoxCh0;
	private JComboBox<String> comBoxChanROI;
	private JButton btnApplyAndClose;
	private JButton btnCancel;
	private JLabel lblCannotEdit;
	private GroupLayout gl_contentPane;
	private SimpleJList<SettingPage> menuList;
	private JLabel lblChanConfigError;
	private PnlSaveOptions pnlOptionsSaving;
	private boolean listSelectionChanging = false;
	private PnlBinOptions pnlOptionsBin;
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
					resetPreferences();
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

		_createPanelChanConfig();

		this.pnlOptionsSaving = new PnlSaveOptions();
		this.pnlOptionsBin = new PnlBinOptions();

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

				resetPreferences();
				removeDisplay();

			}
		});

		JPanel pnlSepTop = new JPanel();
		pnlSepTop.setBorder(new LineBorder(Color.GRAY));

		JPanel pnlPageName = new JPanel();

		this.activePanel = this.pnlOptionsChanConfig;
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
					setCurrentPage(menuList.getSelectedValue());
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

		List<String> values = new ArrayList<String>();
		for (Channel chan : Channel.values()) {
			values.add(chan.toReadableString());
		}
		values.add("<none>");
		if (getSettings().channelsToProcess.contains(Channel.GREEN)) {
			this.chkSelectGreen.setSelected(true);
		}
		if (getSettings().channelsToProcess.contains(Channel.WHITE)) {
			this.chkSelectWhite.setSelected(true);
		}
		if (getSettings().channelsToProcess.contains(Channel.RED)) {
			this.chkSelectRed.setSelected(true);
		}
		if (getSettings().channelsToProcess.contains(Channel.BLUE)) {
			this.chkSelectBlue.setSelected(true);
		}

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
			this.pnlOptionsSaving.btnBrowseFolders.setEnabled(true);
		} else {
			this.btnApplyAndClose.setEnabled(false);
			this.lblCannotEdit.setText("To make changes you must cancel the current run.");
			this.lblCannotEdit.setVisible(true);
			this.pnlOptionsSaving.btnBrowseFolders.setEnabled(false);

		}
	}

	public void display(Component parent, boolean running) {
		removeErrorMessages();
		setCurrentPage(SettingPage.ChannelConfiguration);
		setEnabled(!running);
		resetPreferences();
		pack();
		setLocationRelativeTo(parent);
		setVisible(true);
	}

	public Object[] applyPreferences(boolean resetIfIncorrect) {
		
		Settings settings = getSettings();
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
			if (resetIfIncorrect) resetPreferences();
			return new Object[] {SettingPage.ChannelConfiguration, "Incorrect channel config. Each channel # must have a unique color."};
		} else if (!this.chkSelectBlue.isSelected() && !this.chkSelectGreen.isSelected() && !this.chkSelectRed.isSelected() && !this.chkSelectWhite.isSelected()) {
			if (resetIfIncorrect) resetPreferences();
			return new Object[] {SettingPage.ChannelConfiguration, "Incorrect channel config. At least 1 one channel must be processed."};
		} else if (!values.contains("<none>")) {
			return new Object[] {SettingPage.ChannelConfiguration, "Incorrect channel config. At least one channel must be assigned."};
		} else if (values.contains((String) this.comBoxChanROI.getSelectedItem())) {
			return new Object[] {SettingPage.ChannelConfiguration, "The channel chosen for drawing ROIs was not assigned a number."};
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
				return new Object[] {SettingPage.ChannelConfiguration, "All channels to be processed must be mapped."};
			}
		}
		if (this.chkSelectRed.isSelected()) {
			if (newChanMap.containsValue(Channel.RED)) {
				channelForProcess.add(Channel.RED);
			} else {
				return new Object[] {SettingPage.ChannelConfiguration, "All channels to be processed must be mapped."};
			}
		}
		if (this.chkSelectGreen.isSelected()) {
			if (newChanMap.containsValue(Channel.GREEN)) {
				channelForProcess.add(Channel.GREEN);
			} else {
				return new Object[] {SettingPage.ChannelConfiguration, "All channels to be processed must be mapped."};
			}		
			
		}
		if (this.chkSelectWhite.isSelected()) {
			if (newChanMap.containsValue(Channel.WHITE)) {
				channelForProcess.add(Channel.WHITE);
			} else {
				return new Object[] {SettingPage.ChannelConfiguration, "All channels to be processed must be mapped."};
			}	
			
		}
		
		if (!this.pnlOptionsBin.validateFields()) {
			return new Object[] {SettingPage.BinConfiguration, ""};
		}
		settings.calculateBins = this.pnlOptionsBin.chkCalcBins.isSelected();
		settings.drawBinLabels = this.pnlOptionsBin.chkDrawBinLabels.isSelected();
		settings.excludePtsOutsideBin = this.pnlOptionsBin.chkExcludeOutsider.isSelected();
		settings.numberOfBins = (int) Math.round(((Number) this.pnlOptionsBin.spnNumBins.getValue()).doubleValue());
		List<Channel> selectedChans = new ArrayList<Channel>();
		if (this.pnlOptionsBin.chkDrawBlue.isSelected())
			selectedChans.add(Channel.BLUE);
		if (this.pnlOptionsBin.chkDrawGreen.isSelected())
			selectedChans.add(Channel.GREEN);
		if (this.pnlOptionsBin.chkDrawRed.isSelected())
			selectedChans.add(Channel.RED);
		if (this.pnlOptionsBin.chkDrawWhite.isSelected())
			selectedChans.add(Channel.WHITE);
		settings.channelToDrawBin = selectedChans;
		settings.channelMap = newChanMap;
		settings.channelsToProcess = channelForProcess;
		settings.channelForROIDraw = (Channel.parse((String) this.comBoxChanROI.getSelectedItem()));
		settings.needsUpdate = true;

		if (!pnlOptionsSaving.fullPathName.getText().equals("")) {
			File file = new File(pnlOptionsSaving.fullPathName.getText());
			if (file.exists()) {
				settings.outputLocation = file;
			}
		} else {
			settings.outputLocation = null;
		}
		
		
		try {
			SettingsLoader.saveSettings(settings);
			settings.needsUpdate = false;
			return null;
		} catch (Exception e) {
			return new Object[] {null, "<html>The settings could not be saved:<br><br>" + e.getMessage() + "</html>"};
		}



	}

	public void resetPreferences() {
		Settings settings = getSettings();
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
		if (settings.outputLocation == null) {
			this.pnlOptionsSaving.folderName.setText("");
			this.pnlOptionsSaving.fullPathName.setText("");
		} else {
			this.pnlOptionsSaving.folderName.setText(settings.outputLocation.getName());
			this.pnlOptionsSaving.fullPathName.setText(settings.outputLocation.getPath());
		}
		this.pnlOptionsBin.chkCalcBins.setSelected(settings.calculateBins);
		this.pnlOptionsBin.chkDrawBinLabels.setSelected(settings.drawBinLabels);
		this.pnlOptionsBin.chkDrawBlue.setSelected(settings.channelToDrawBin.contains(Channel.BLUE));
		this.pnlOptionsBin.chkDrawGreen.setSelected(settings.channelToDrawBin.contains(Channel.GREEN));
		this.pnlOptionsBin.chkDrawRed.setSelected(settings.channelToDrawBin.contains(Channel.RED));
		this.pnlOptionsBin.chkDrawWhite.setSelected(settings.channelToDrawBin.contains(Channel.WHITE));
		this.pnlOptionsBin.spnNumBins.setValue(settings.numberOfBins);
		this.pnlOptionsBin.chkExcludeOutsider.setSelected(settings.excludePtsOutsideBin);

	}

	public boolean isDisplaying() {
		return this.isVisible();
	}

	@SuppressWarnings("unchecked")
	private void _createPanelChanConfig() {
		this.pnlOptionsChanConfig = new JPanel();
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
		Settings settings = getSettings();
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

		lblChanConfigError = new JLabel("Error");
		lblChanConfigError.setFont(GUI.mediumFont);
		lblChanConfigError.setForeground(Color.RED);
		lblChanConfigError.setVisible(false);

		GroupLayout groupLayout = new GroupLayout(this.pnlOptionsChanConfig);
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
								.addComponent(lblChanConfigError))
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
						.addComponent(lblChanConfigError)
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
		this.pnlOptionsChanConfig.setLayout(groupLayout);
	}


	private void displayError() {
		this.lblCannotEdit.setText("There were errors in your configuration.");
		this.lblCannotEdit.setVisible(true);
	}

	private void displayChanConfigError(String error) {
		this.lblChanConfigError.setText(error);
		this.lblChanConfigError.setVisible(true);
		setCurrentPage(SettingPage.ChannelConfiguration);
		displayError();
	}

	public void removeErrorMessages() {
		if (!this.lblCannotEdit.getText().equals("To make changes you must cancel the current run.")) {
			this.lblCannotEdit.setVisible(false);
		}
		
		this.lblChanConfigError.setVisible(false);
		this.pnlOptionsSaving.removeError();
		this.pnlOptionsBin.removeError();
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

	public void setCurrentPage(SettingPage settings) {
		this.listSelectionChanging = true;
		switch (settings) {
		case ChannelConfiguration:
			gl_contentPane.replace(this.activePanel, this.pnlOptionsChanConfig);
			this.lblPageName.setText(SettingPage.ChannelConfiguration.friendly());
			this.menuList.setSelectedValue(SettingPage.ChannelConfiguration, true);
			this.activePanel = this.pnlOptionsChanConfig;
			break;
		case Saving:
			gl_contentPane.replace(this.activePanel, this.pnlOptionsSaving);
			this.lblPageName.setText(SettingPage.Saving.friendly());
			this.menuList.setSelectedValue(SettingPage.Saving, true);
			this.activePanel = this.pnlOptionsSaving;
			break;
		case BinConfiguration:
			gl_contentPane.replace(this.activePanel, this.pnlOptionsBin);
			this.lblPageName.setText(SettingPage.BinConfiguration.friendly());
			this.menuList.setSelectedValue(SettingPage.BinConfiguration, true);
			this.activePanel = this.pnlOptionsBin;
			break;

		}
		this.listSelectionChanging = false;

	}
	
	public Settings getSettings() {
		return GUI.settings;
	}
	
	private void userTriedToApplyChanges() {
		removeErrorMessages();

		Object[] errors = applyPreferences(false);;

		if (errors != null) {
			if (errors[0] == null) {
				displayError();

				JOptionPane.showMessageDialog(contentPane, "<html>Failure writing preferences:<br><br>" + errors[1] + "</html>", "I/O Failure", JOptionPane.ERROR_MESSAGE);
			} else {
				switch ((SettingPage) errors[0]) {
				case ChannelConfiguration:
					displayChanConfigError((String) errors[1]);
					break;
				case Saving:
					pnlOptionsSaving.displayError((String) errors[1]);
					displayError();
					setCurrentPage(SettingPage.ChannelConfiguration);
					break;
				case BinConfiguration:
					displayError();
					setCurrentPage(SettingPage.BinConfiguration);
					break;
				}
			}	

		} else {
			removeDisplay();
		}
	}

	private enum SettingPage {
		ChannelConfiguration("Channel Configuration"), BinConfiguration("Bin Configuration"), Saving("Save Options");

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
class PnlSaveOptions extends JPanel {

	private static final long serialVersionUID = -1576497004345094528L;
	protected JTextField fullPathName;
	protected JTextField folderName;
	private JLabel lblError;
	protected JButton btnBrowseFolders;
	protected FileBrowser fileBrowser;
	private JPanel thisObject = this;

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
					if (settings.recentOpenFileLocations == null) {
						settings.recentOpenFileLocations = new ArrayList<File>();
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
}
class PnlBinOptions extends JPanel {
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
	
	public boolean validateFields() {
		if (!this.chkDrawRed.isSelected() && !this.chkDrawGreen.isSelected() && !this.chkDrawBlue.isSelected() && !this.chkDrawWhite.isSelected()) {
			displayError("Bins must be drawn on at least one channel.");
			return false;
		} else {
			removeError();
			return true;
		}
	}
}

