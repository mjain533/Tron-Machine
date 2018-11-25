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


import javax.swing.JFrame;


import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.WindowConstants;


import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Properties;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Desktop;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.json.JSONException;
import org.json.JSONObject;

import com.typicalprojects.TronMachine.MainFrame;
import com.typicalprojects.TronMachine.neuronal_migration.Settings.SettingsLoader;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlDisplay;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlInstructions;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlLog;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlOptions;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlSelectFiles;
import com.typicalprojects.TronMachine.popup.BrightnessAdjuster;
import com.typicalprojects.TronMachine.util.ImageContainer.Channel;
import com.typicalprojects.TronMachine.util.Logger;

import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.JSeparator;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class GUI  {


	private JFrame quantFrame;
	private final MainFrame parentFrame;
	public static final Font mediumFont = new Font("PingFang TC", Font.BOLD, 14);
	public static final Font smallFont = new Font("PingFang TC", Font.BOLD, 13);
	public static final Font smallPlainFont = new Font("PingFang TC", Font.PLAIN, 13);
	public static final Font medium_smallPlainFont = new Font("PingFang TC", Font.PLAIN, 10);


	public static String dateString = null;

	public static Settings settings = null;


	private PnlInstructions pnlInstructions;
	private PnlSelectFiles pnlSelectFiles;
	private PnlLog pnlLog;
	private PnlDisplay pnlDisplay;
	private PnlOptions pnlOptions;

	private volatile Wizard wizard;
	private JMenuItem mntmPreferences;
	private JMenuItem mntmBrightnessAdj;
	private JMenuItem mntmSummaryStats;

	private Preferences2 prefs;
	private BrightnessAdjuster brightnessAdjuster;
	private StatsGUI statsGUI;
	private volatile JLabel lblAttributes = null;
	public static GUI SINGLETON = null;

	/**
	 * Create the application.
	 * @throws IOException 
	 * @throws FormatException 
	 */
	public GUI(MainFrame parent) throws IOException {
		



		SINGLETON = this;
		settings = null;
		try {
			settings = SettingsLoader.loadSettings(false);
			if (settings.needsUpdate) {
				SettingsLoader.saveSettings(settings);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (settings == null) {
				JOptionPane.showMessageDialog(parent, "<html>There was an error loading settings:<br><br>" + e.getMessage() + "</html>", "Settings Load Error", JOptionPane.ERROR_MESSAGE);
			} else {
				JOptionPane.showMessageDialog(parent, "<html>There was an error updating settings:<br><br>" + e.getMessage() + "</html>", "Settings Update Error", JOptionPane.ERROR_MESSAGE);

			}
			throw new IOException();
		}
		this.parentFrame = parent;
		DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy h-m-s a");
		Date date = new Date();
		dateString = dateFormat.format(date);


		initialize();


	}

	public void show() throws IOException {

		this.quantFrame.setVisible(true);
		this.quantFrame.repaint();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		quantFrame = new JFrame();
		quantFrame.setTitle("The TRON Machine - Neuronal Migration");
		quantFrame.setFocusable(false);
		quantFrame.setMinimumSize(new Dimension(750, 750));
		quantFrame.setPreferredSize(new Dimension(750, 720));
		quantFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		quantFrame.setLocationRelativeTo(null);
		quantFrame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		quantFrame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				if (JOptionPane.showConfirmDialog(quantFrame, "Do you really want to quit?", "Confirm Quit", 
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION) {
					doExit();
				}
			}
		});

		setUIFont(smallFont);

		JMenuBar menuBar = new JMenuBar();
		quantFrame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		mnFile.setFont(smallFont.deriveFont(Font.BOLD, 16));
		menuBar.add(mnFile);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int selection = JOptionPane.showConfirmDialog(quantFrame, "Do you really want to quit?", "Confirm Quit", 
						JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE);
				if (selection == 0) {
					doExit();
				}
			}
		});

		mntmPreferences = new JMenuItem("Preferences");
		prefs = new Preferences2(this);
		mntmPreferences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							prefs.display(quantFrame, !wizard.getStatus().equals(Wizard.Status.SELECT_FILES));
							unfocusTemporarily();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}
		});
		mntmPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mnFile.add(mntmPreferences);

		JSeparator separator = new JSeparator();
		mnFile.add(separator);
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		//mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, 0));
		mnFile.add(mntmExit);

		JMenu mnOptions = new JMenu("Options");
		mnOptions.setFont(smallFont.deriveFont(Font.BOLD, 16));
		menuBar.add(mnOptions);
		mntmBrightnessAdj = new JMenuItem("Adjust Brightness");
		mntmBrightnessAdj.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							brightnessAdjuster.display(quantFrame);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}
		});
		mntmBrightnessAdj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		mnOptions.add(mntmBrightnessAdj);

		JMenu mnProcess = new JMenu("Process");
		mnProcess.setFont(smallFont.deriveFont(Font.BOLD, 16));
		menuBar.add(mnProcess);

		this.mntmSummaryStats = new JMenuItem("Summary Stats");
		mnProcess.add(this.mntmSummaryStats);
		this.statsGUI = new StatsGUI(this);
		this.mntmSummaryStats.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							statsGUI.display(quantFrame);
							unfocusTemporarily();
						} catch (Exception e) {
						}
					}
				});

			}
		});
		mntmSummaryStats.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));

		JMenu mnNavigation = new JMenu("Navigation");
		mnNavigation.setFont(smallFont.deriveFont(Font.BOLD, 16));
		menuBar.add(mnNavigation);

		JMenuItem mntmBackToMain = new JMenuItem("Back to Main Menu...");
		mnNavigation.add(mntmBackToMain);
		quantFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		mntmBackToMain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							getWizard().cancel();
							parentFrame.reshow();
							quantFrame.setVisible(false);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}
		});
		mntmBackToMain.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));


		final Properties properties = new Properties();
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e1) {
			// shouldn't happen
			e1.printStackTrace();
		}
		final String currentVersion = properties.getProperty("version");
		lblAttributes = new JLabel("Developed by J. Carrington, R. Taylor, K. Taylor, and E. Dent. Copyright 2018. Version "+ currentVersion+".");
		lblAttributes.setFont(new Font("PingFang TC", Font.BOLD, 13));
		lblAttributes.setBorder(new EmptyBorder(6, 10, 10, 10));
		quantFrame.getContentPane().add(lblAttributes, BorderLayout.SOUTH);

		JMenu mnHelp = new JMenu("Help");
		mnHelp.setFont(smallFont.deriveFont(Font.BOLD, 16));
		menuBar.add(mnHelp);

		JMenuItem mntmQuickStart = new JMenuItem("Quick Start Quide");
		mnHelp.add(mntmQuickStart);

		mntmQuickStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							openWebpage(new URL("https://bitbucket.org/JustinCarr/tronmachine/wiki/Quick%20Start"));
						}catch (Exception e) {

						}
					}
				});

			}
		});

		JMenuItem mntmDocuments = new JMenuItem("Documentation");
		mnHelp.add(mntmDocuments);

		mntmDocuments.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							openWebpage(new URL("https://bitbucket.org/JustinCarr/tronmachine/wiki/Documentation"));
						}catch (Exception e) {

						}
					}
				});

			}
		});


		// main content area
		JPanel pnlCONTENT = new JPanel();
		quantFrame.getContentPane().add(pnlCONTENT, BorderLayout.CENTER);


		//Instructions
		pnlInstructions = new PnlInstructions();

		// Select Files
		pnlSelectFiles = new PnlSelectFiles(this);

		// Log
		pnlLog = new PnlLog(this);

		pnlOptions = new PnlOptions(this);
		this.brightnessAdjuster = new BrightnessAdjuster(pnlOptions);

		// Display
		pnlDisplay = new PnlDisplay(this, pnlOptions);

		// Options

		wizard = new Wizard(this);

		GroupLayout gl_pnlCONTENT = new GroupLayout(pnlCONTENT);
		gl_pnlCONTENT.setHorizontalGroup(
				gl_pnlCONTENT.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_pnlCONTENT.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pnlCONTENT.createParallelGroup(Alignment.LEADING)
								.addComponent(pnlOptions.getRawPanel(), GroupLayout.PREFERRED_SIZE, 335, GroupLayout.PREFERRED_SIZE) // added
								.addComponent(pnlLog.getRawPanel(), GroupLayout.PREFERRED_SIZE, 335, GroupLayout.PREFERRED_SIZE)
								.addComponent(pnlSelectFiles.getRawPanel(), GroupLayout.PREFERRED_SIZE, 335, GroupLayout.PREFERRED_SIZE)
								.addComponent(pnlInstructions.getRawPanel(), GroupLayout.PREFERRED_SIZE, 335, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(pnlDisplay.getRawPanel(), GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
						.addContainerGap())
				);
		gl_pnlCONTENT.setVerticalGroup(
				gl_pnlCONTENT.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_pnlCONTENT.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_pnlCONTENT.createParallelGroup(Alignment.LEADING)
								.addComponent(pnlDisplay.getRawPanel(), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(gl_pnlCONTENT.createSequentialGroup()
										.addComponent(pnlInstructions.getRawPanel(), GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(pnlSelectFiles.getRawPanel(), GroupLayout.PREFERRED_SIZE, /*GroupLayout.DEFAULT_SIZE*/270, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(pnlOptions.getRawPanel(), GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(pnlLog.getRawPanel(), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)))
						.addGap(5))
				);


		pnlCONTENT.setLayout(gl_pnlCONTENT);

		setUIFont(smallFont);
		Thread updateRetrieveThread = new Thread(new Runnable() {
			public void run(){
				try {
					String url = "https://api.bitbucket.org/2.0/repositories/JustinCarr/tronmachine/downloads/";

					JSONObject objjson = readJsonFromUrl(url);
					String recentName = objjson.getJSONArray("values").getJSONObject(0).getString("name");
					recentName = recentName.substring(recentName.lastIndexOf("-") + 1, recentName.lastIndexOf("."));

					String[] latestVersionTags = recentName.split("\\.");
					String[] currentVersionTags = currentVersion.split("\\.");
					boolean update = false;
					for (int k = 0; k < latestVersionTags.length; k++){
						if (k >= currentVersionTags.length) {
							update = true;
							break;
						} else {
							int latestvers = Integer.parseInt(latestVersionTags[k]);
							int currentvers = Integer.parseInt(currentVersionTags[k]);
							if (latestvers < currentvers) {
								break;
							} else if (latestvers > currentvers) {
								update = true;
								break;
							}

						}
						
					}
					
					if (update) {
						lblAttributes.setText("<html>" + lblAttributes.getText() + " <font color='RED'>(Updates Available)</font></html>");
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		updateRetrieveThread.setDaemon(true);
		updateRetrieveThread.start();
		

	}

	public static void setUIFont (java.awt.Font f){
		java.util.Enumeration<Object> keys = UIManager.getLookAndFeelDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get (key);
			if (value != null && value instanceof java.awt.Font)
				UIManager.put (key, f);
		}
	}

	public void unfocusTemporarily() {
		quantFrame.setFocusable(false);
		quantFrame.setEnabled(false);
	}

	public void refocus() {
		quantFrame.setFocusable(true);
		quantFrame.setEnabled(true);
	}

	public void doExit() {
		System.exit(0);
	}

	public PnlInstructions getInstructionPanel() {
		return this.pnlInstructions;
	}

	public PnlSelectFiles getSelectFilesPanel() {
		return this.pnlSelectFiles;
	}

	public PnlDisplay getPanelDisplay() {
		return this.pnlDisplay;
	}

	public PnlOptions getPanelOptions() {
		return this.pnlOptions;
	}

	public JFrame getComponent() {
		return this.quantFrame;
	}

	public Logger getLogger() {
		return this.pnlLog;
	}

	public PnlLog getLogPanel() {
		return this.pnlLog;
	}

	public Wizard getWizard() {
		return this.wizard;
	}

	public BrightnessAdjuster getBrightnessAdjuster() {
		return this.brightnessAdjuster;
	}

	public void setBrightnessAdjustOptionEnabled(boolean enabled) {
		this.mntmBrightnessAdj.setEnabled(enabled);

	}

	public void setMenuItemsEnabledDuringRun(boolean enabled) {
		//this.mntmPreferences.setEnabled(enabled);
		this.prefs.setEnabled(enabled);
		this.statsGUI.removeDisplay();
		this.statsGUI.resetFields();
		this.prefs.resetPreferences(enabled);
	}

	private static boolean openWebpage(URI uri) {
		Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
		if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
			try {
				desktop.browse(uri);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean openWebpage(URL url) {
		try {
			return openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean outputOptionContainsChannel(OutputOption option, Channel chan) {
		return (GUI.settings.enabledOptions.containsKey(option) && GUI.settings.enabledOptions.get(option).includedChannels.contains(chan));
	}

	public static boolean outputOptionsContainChannel(Collection<OutputOption> options, Channel chan) {
		for (OutputOption option : options) {
			if (outputOptionContainsChannel(option, chan))
				return true;
		}
		return false;
	}

	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
		int cp;
		while ((cp = rd.read()) != -1) {
			sb.append((char) cp);
		}
		return sb.toString();
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try {
			BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
			String jsonText = readAll(rd);
			JSONObject json = new JSONObject(jsonText);
			return json;
		} finally {
			is.close();
		}
	}


}
