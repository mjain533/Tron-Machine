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
import java.util.Date;
import java.util.Properties;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import org.json.JSONException;
import org.json.JSONObject;

import com.typicalprojects.TronMachine.MainFrame;
import com.typicalprojects.TronMachine.neuronal_migration.Settings.SettingsManager;
import com.typicalprojects.TronMachine.neuronal_migration.Wizard.Status;
import com.typicalprojects.TronMachine.neuronal_migration.panels.ImagePanel;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlDisplay;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlInstructions;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlLog;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlOptions;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlSelectFiles;
import com.typicalprojects.TronMachine.popup.BrightnessAdjuster;
import com.typicalprojects.TronMachine.util.Logger;

import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.JSeparator;

import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

/**
 * Main GUI component of TRON machine. Contains the main JFrame. Also has helper methods which are accessed
 * by many other classes in the TRON machine. The GUI is built at startup.
 * 
 * @author Justin Carrington
 */
public class GUI  {


	private JFrame quantFrame;
	private final MainFrame parentFrame;
	
	/** PingFangTC, bold, size 14 **/
	public static final Font mediumBoldFont = new Font("PingFang TC", Font.BOLD, 14);
	/** PingFangTC, bold, size 13 **/
	public static final Font smallBoldFont = new Font("PingFang TC", Font.BOLD, 13);
	/** PingFangTC, plain, size 13 **/
	public static final Font smallPlainFont = new Font("PingFang TC", Font.PLAIN, 13);
	/** PingFangTC, plain, size 10 **/
	public static final Font extraSmallPlainFont = new Font("PingFang TC", Font.PLAIN, 10);
	/** PingFangTC, bold, size 10 **/
	public static final Font extraSmallBoldFont = new Font("PingFang TC", Font.BOLD, 10);
	
	/** Background color of a panel of the GUI when it is disabled **/
	public static final Color colorPnlDisabled = new Color(50, 55, 65);
	/** Background color of a panel of the GUI when it is enabled **/
	public static final Color colorPnlEnabled = new Color(40, 45, 55);

	/** 
	 * The string representing the current date. It is in the form dd-MM-yyyy HH-mm-ss. Used for creating 
	 * output files.	
	 */
	public static String dateString = null;
	
	/** Main settings object, loaded from the settings file (YAML) stored within the neuronal migration resources folder **/
	public static Settings settings = null;


	private PnlInstructions pnlInstructions;
	private PnlSelectFiles pnlSelectFiles;
	private PnlLog pnlLog;
	private PnlDisplay pnlDisplay;
	private PnlOptions pnlOptions;

	/** Used for advancing from one step to another in the TRON machine. **/
	private volatile Wizard wizard;
	
	private JMenuItem menuItemPreferences;
	private JMenuItem mntmBrightnessAdj;
	private JMenuItem menuItemItmdProcessingGUI;

	/** Popup which allows modification of preferences (basically modifies the settings object in this class **/
	private Preferences prefs;
	
	/** Popup which allows beginning processing from an intermediate processing state in the TRON machine **/
	private IntermediateProcessingGUI itmdProcessingGUI;
	private BrightnessAdjuster brightnessAdjuster;
	
	/** Has author names, version number, copyright, and whether there are updates. Displayed at bottom of GUI **/
	private volatile JLabel lblAttributes = null;
	
	/** There should only ever be ONE GUI object, never multiple. SO store it as a SINGLETON so it can be accessed publicly **/
	public static GUI SINGLETON = null;

	/**
	 * Constructs the GUI frame. Does the following:
	 * <ul>
	 * <li>Turn off the press-and-hold feature on Mac (i.e. holding down a key to show accents, etc) as 
	 * this is a known bug that affects repeated key strokes in Java, such as for moving around images</li>
	 * <li>Load settings, saving any updates if need be.</li>
	 * <li>Set the current run date. For each subsequent run of TRON machine, this should be re-set.</li>
	 * <li>Call {@link #initialize()} to finish setup, constructing all panels and initializing the TRON machine.</li>
	 * </ul>
	 * 
	 * @param parent			The parent MainFrame which created the GUI. This reference is used in case the
	 *  user returns to the main menu.
	 * @throws IOException	Can be throw for many reasons. Can be thrown if settings can't be written, or 
	 * there was an error writing them, for instance.
	 */
	public GUI(MainFrame parent) throws IOException {

		try {
			Runtime.getRuntime().exec("defaults write -g ApplePressAndHoldEnabled -bool false");
		} catch (Exception e) {

		}

		SINGLETON = this;
		settings = null;
		try {
			settings = SettingsManager.loadSettings(false);
			
			if (settings.needsUpdate) {
				SettingsManager.saveSettings(settings);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (settings == null) {
				GUI.displayMessage("There was an error loading settings:<br><br>" + e.getMessage(), "Settings Load Error", parent,  JOptionPane.ERROR_MESSAGE);
			} else {
				GUI.displayMessage("<html>There was an error updating settings:<br><br>" + e.getMessage(), "Settings Update Error", parent,  JOptionPane.ERROR_MESSAGE);

			}
			throw new IOException();
		}
		this.parentFrame = parent;
		
		DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy h-m-s a");
		Date date = new Date();
		dateString = dateFormat.format(date);

		initialize();


	}
	
	/**
	 * Sets this frame visible by repainting it on the screen.
	 */
	public void show() {

		this.quantFrame.setVisible(true);
		this.quantFrame.repaint();
	}

	/**
	 * Initialize the contents of the frame. Performs quite a few operations, initializing all fields and
	 * variables in every panel, and preparing the TRON machine for its first run.
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
		quantFrame.getContentPane().setBackground(new Color(25, 28, 35));
		quantFrame.addWindowListener(new WindowAdapter(){
			@Override
			public void windowClosing(WindowEvent windowEvent) {
				if (wizard.getStatus() != Status.SELECT_FILES) {
					if (GUI.confirmWithUser("Do you really want to quit?", "Confirm Quit", quantFrame, JOptionPane.WARNING_MESSAGE)) {
						doExit();
					}
				} else {
					doExit();
				}
			}
		});

		setUIFont(smallBoldFont);
		
		// Create the menu bar at the top:
		
		JMenuBar menuBar = new JMenuBar();
		quantFrame.setJMenuBar(menuBar);

		JMenu menuFile = new JMenu("File");
		menuFile.setFont(smallBoldFont.deriveFont(Font.BOLD, 16));
		
		JMenu menuOptions = new JMenu("Options");
		menuOptions.setFont(smallBoldFont.deriveFont(Font.BOLD, 16));
		
		JMenu menuProcess = new JMenu("Process");
		menuProcess.setFont(smallBoldFont.deriveFont(Font.BOLD, 16));
		
		JMenu menuNavigation = new JMenu("Navigation");
		menuNavigation.setFont(smallBoldFont.deriveFont(Font.BOLD, 16));
		
		JMenu menuHelp = new JMenu("Help");
		menuHelp.setFont(smallBoldFont.deriveFont(Font.BOLD, 16));
		
		menuBar.add(menuFile);
		menuBar.add(menuOptions);
		menuBar.add(menuProcess);
		menuBar.add(menuNavigation);
		menuBar.add(menuHelp);


		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (GUI.confirmWithUser("Do you really want to quit?", "Confirm Quit", quantFrame, JOptionPane.ERROR_MESSAGE)) {
					doExit();

				}

			}
		});

		menuItemPreferences = new JMenuItem("Preferences");
		prefs = new Preferences(this);
		menuItemPreferences.addActionListener(new ActionListener() {
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
		menuItemPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFile.add(menuItemPreferences);

		JSeparator separator = new JSeparator();
		menuFile.add(separator);
		mntmExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
		menuFile.add(mntmExit);


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
		menuOptions.add(mntmBrightnessAdj);

		this.itmdProcessingGUI = new IntermediateProcessingGUI(this);
		this.menuItemItmdProcessingGUI = new JMenuItem("Process from Intermediates");
		menuProcess.add(this.menuItemItmdProcessingGUI);
		this.menuItemItmdProcessingGUI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							getWizard().cancel();
							itmdProcessingGUI.display(quantFrame);
							quantFrame.setVisible(false);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}
		});
		menuItemItmdProcessingGUI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));


		JMenuItem menuItemBackToMain = new JMenuItem("Back to Main Menu...");
		menuNavigation.add(menuItemBackToMain);
		quantFrame.getContentPane().setLayout(new BorderLayout(0, 0));
		menuItemBackToMain.addActionListener(new ActionListener() {
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
		menuItemBackToMain.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));


		JMenuItem mntmQuickStart = new JMenuItem("Quick Start Quide");
		menuHelp.add(mntmQuickStart);

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
		menuHelp.add(mntmDocuments);

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
		
		// Create GUI frame:
		
		final Properties properties = new Properties();
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e1) {
			// shouldn't happen
			e1.printStackTrace();
		}
		final String currentVersion = properties.getProperty("version");
		lblAttributes = new JLabel("Developed by J. Carrington, R. Taylor, K. Taylor, and E. Dent. Copyright 2018. Version "+ currentVersion+".");
		lblAttributes.setFont(smallBoldFont);
		lblAttributes.setBorder(new EmptyBorder(6, 10, 10, 10));
		lblAttributes.setForeground(new Color(255, 255, 255));
		quantFrame.getContentPane().add(lblAttributes, BorderLayout.SOUTH);


		// main content of frame
		JPanel pnlCONTENT = new JPanel();
		quantFrame.getContentPane().add(pnlCONTENT, BorderLayout.CENTER);


		// Instructions panel
		pnlInstructions = new PnlInstructions();

		// Select Files panel
		pnlSelectFiles = new PnlSelectFiles(this);

		// Log panel
		pnlLog = new PnlLog();

		// Options panel
		pnlOptions = new PnlOptions(this);
		this.brightnessAdjuster = new BrightnessAdjuster(pnlOptions);

		// Display panel
		pnlDisplay = new PnlDisplay(this, pnlOptions);
		pnlDisplay.setChannelManager(settings.channelMan);

		// Create the wizard used to advaned between steps of the TRON machine
		wizard = new Wizard(this);
		
		// Create the layout for the main frame
		GroupLayout layout = new GroupLayout(pnlCONTENT);
		layout.setHorizontalGroup(
				layout.createParallelGroup(Alignment.LEADING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(pnlOptions.getRawPanel(), GroupLayout.PREFERRED_SIZE, 335, GroupLayout.PREFERRED_SIZE) // added
								.addComponent(pnlLog.getRawPanel(), GroupLayout.PREFERRED_SIZE, 335, GroupLayout.PREFERRED_SIZE)
								.addComponent(pnlSelectFiles.getRawPanel(), GroupLayout.PREFERRED_SIZE, 335, GroupLayout.PREFERRED_SIZE)
								.addComponent(pnlInstructions.getRawPanel(), GroupLayout.PREFERRED_SIZE, 335, GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addComponent(pnlDisplay.getRawPanel(), GroupLayout.DEFAULT_SIZE, 397, Short.MAX_VALUE)
						.addContainerGap())
				);
		layout.setVerticalGroup(
				layout.createParallelGroup(Alignment.TRAILING)
				.addGroup(layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(layout.createParallelGroup(Alignment.LEADING)
								.addComponent(pnlDisplay.getRawPanel(), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
								.addGroup(layout.createSequentialGroup()
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


		pnlCONTENT.setLayout(layout);

		setUIFont(smallBoldFont);
		/* 
		// Create the thread for determining if there are updates available.
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
		 */

	}

	/**
	 * Sets the font for all components of the TRON machine so everything matches.
	 * @param f the font to set.
	 */
	private void setUIFont (java.awt.Font f){
		java.util.Enumeration<Object> keys = UIManager.getLookAndFeelDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get (key);
			if (value != null && value instanceof java.awt.Font)
				UIManager.put (key, f);
		}
	}
	
	/**
	 * Sets options disabled and focus off, used for when a popup is displayed so that the user cannot mess
	 * with this frame while using the popup.
	 */
	public void unfocusTemporarily() {
		quantFrame.setFocusable(false);
		quantFrame.setEnabled(false);
	}
	
	/**
	 * Called after a called to {@link #unfocusTemporarily()}, putting focus back on the GUI frame instead of
	 * a popup.
	 */
	public void refocus() {
		quantFrame.setFocusable(true);
		quantFrame.setEnabled(true);
	}

	/**
	 * Quit the program. Basically just calls the standard System.exit() method, first re-writing apple settings
	 * for Mac users so that they can use the hold feature for keys to bring up special characters
	 */
	public void doExit() {
		try {
			Runtime.getRuntime().exec("defaults write -g ApplePressAndHoldEnabled -bool true");
		} catch (IOException e) {
			// do nothing in this instance
			e.printStackTrace();
		}
		System.exit(0);
	}

	/**
	 * @return instructions panel of the GUI
	 */
	public PnlInstructions getInstructionPanel() {
		return this.pnlInstructions;
	}

	/**
	 * @return file select panel of the GUI
	 */
	public PnlSelectFiles getSelectFilesPanel() {
		return this.pnlSelectFiles;
	}
	
	/**
	 * @return image panel contained within the displayu panel of the GUI
	 */
	public ImagePanel getImageDisplay() {
		return this.pnlDisplay.getImagePanel();
	}
	
	/**
	 * @return display panel of the GUI
	 */
	public PnlDisplay getPanelDisplay() {
		return this.pnlDisplay;
	}

	/**
	 * @return options panel of the GUI
	 */
	public PnlOptions getPanelOptions() {
		return this.pnlOptions;
	}

	/**
	 * @return raw panel behind the GUI component
	 */
	public JFrame getComponent() {
		return this.quantFrame;
	}

	/**
	 * @return Logger, which is the log panel of the GUI component
	 */
	public Logger getLogger() {
		return this.pnlLog;
	}

	/**
	 * @return loggin panel of the GUI
	 */
	public PnlLog getLogPanel() {
		return this.pnlLog;
	}

	/**
	 * @return Wizard used for advancing between steps of the TRON machine
	 */
	public Wizard getWizard() {
		return this.wizard;
	}

	/**
	 * @return pop-up for controlling brightness of a channel during ROI selection
	 */
	public BrightnessAdjuster getBrightnessAdjuster() {
		return this.brightnessAdjuster;
	}
	
	/**
	 * @param enabled true if the brightness pop-up should be accessible. Only should be made available during
	 * the ROI selection phase of the TRON machine processing.
	 */
	public void setBrightnessAdjustOptionEnabled(boolean enabled) {
		this.mntmBrightnessAdj.setEnabled(enabled);

	}

	/**
	 * Sets the state of various items. For instance, whether preferences should be enabled. Does NOT
	 * disable the Preferences option in the menu bar, but does not allow the user to change any settings
	 * in the settings pages.
	 * 
	 * @param enabled true if should be enabled.
	 */
	public void setMenuItemsEnabledDuringRun(boolean enabled) {
		this.prefs.setEnabled(enabled);
		this.prefs.resetPreferences(enabled);
		this.menuItemItmdProcessingGUI.setEnabled(enabled);
	}

	/**
	 * Opens a web-page in the user's default browser.
	 * 
	 * @param uri	The URI to navigate to.
	 * @return		true if opening the web page was successful.
	 */
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

	/**
	 * Opens a web-page in the user's defualt browser.
	 * 
	 * @param url	The URl to navigate to.
	 * @return		true if opening the web page was successful.
	 */
	public static boolean openWebpage(URL url) {
		try {
			return openWebpage(url.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
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
	
	/**
	 * Helper method used for reading information from URL. Used in determining if there is a newer version
	 * of the TRON machine available.
	 * 
	 * @param url	The URL for requesting JSON
	 * @return	JSON output retrieved from URL
	 * @throws IOException	If could not establish a connection
	 * @throws JSONException	If JSON syntax is malformed.
	 */
	private static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
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
	
	/**
	 * Brings up a pop-up which asks the user to confirm something. The popup has a fixed max width of 300
	 * pixels.
	 * 
	 * @param msg			Message to ask user
	 * @param title			Title of the pop-up
	 * @param relative		The component on which the pop-up should be centered. If null is passed, then
	 * 						it will be placed in the screen's center.
	 * @param joptionMsgType	The message type, which should be one of the JOptionPane settings.
	 * @return true if the user confirmed the message. False otherwise (including if the user pressed the "x"
	 *  on the pop-up window.
	 */
	public static boolean confirmWithUser(String msg, String title, Component relative, int joptionMsgType){
		String string;
		JLabel label = new JLabel(msg);
		if (label.getPreferredSize().width > 300) {
			string = "<html><body><p style='width:" + 300 + "px;'>"+msg+"</p></body></html>";
		} else {
			string = "<html><body><p>" + msg+ "</p></body></html>";
		}
		if (relative == null)
			relative = GUI.SINGLETON.quantFrame;
		return JOptionPane.showConfirmDialog(relative, string, title, JOptionPane.YES_NO_OPTION, joptionMsgType) == JOptionPane.YES_OPTION;
	}

	/**
	 * Sends a larger popup message to the user with an 'OK' button.
	 * 
	 * @param msg				The message to send the user
	 * @param title				Title at the top of the message pop-up
	 * @param relative			The component on which the pop-up should be centered. If null is passed, then
	 * 							it will be placed in the screen's center.
	 * @param joptionpaneMsgType	The message type, which should be one of the JOptionPane settings.
	 */
	public static void displayPopupMessage(String msg, String title, Component relative, int joptionpaneMsgType) {
		String string;
		JLabel label = new JLabel(msg);
		if (label.getPreferredSize().width > 400) {
			string = "<html><body style='width:" + 400 + "px;'>"+msg+"</body></html>";
		} else {
			string = "<html><body><p>" + msg+ "</p></body></html>";
		}
		if (relative == null)
			relative = GUI.SINGLETON.quantFrame;
		JOptionPane.showMessageDialog(relative,
				string, title, joptionpaneMsgType);


	}
	
	/**
	 * Sends a simple message to the user with an 'OK' button.
	 * 
	 * @param msg				The message to send the user
	 * @param title				Title at the top of the message pop-up
	 * @param relative			The component on which the pop-up should be centered. If null is passed, then
	 * 							it will be placed in the screen's center.
	 * @param joptionpaneMsgType	The message type, which should be one of the JOptionPane settings.
	 */
	public static void displayMessage(String msg, String title, Component relative, int joptionpaneMsgType) {
		String string;
		JLabel label = new JLabel(msg);
		if (label.getPreferredSize().width > 300) {
			string = "<html><body style='width:" + 300 + "px;'><p>"+msg+"</p></body></html>";
		} else {
			string = "<html><body><p>" + msg+ "</p></body></html>";
		}
		if (relative == null)
			relative = GUI.SINGLETON.quantFrame;
		JOptionPane.showMessageDialog(relative,
				string, title, joptionpaneMsgType);


	}
	
	/**
	 * Creates the text for a tooltip. This is useful because it enforces a max size for the tooltip display.
	 * 
	 * @param text	The text for the tooltip
	 * @return string of text for the tooltip, with formatting included (i.e. html tags)
	 */
	public static String getTooltipText(String text) {
		JLabel label = new JLabel(text);
		if (label.getPreferredSize().width > 200) {
			text = "<html><body><p style='width:" + 200 + "px;'>"+text+"</p></body></html>";
		} else {
			text = "<html><body><p>" + text+ "</p></body></html>";
		}
		return text;

	}
	
	/**
	 * Uses a pop-up to get input from the user.
	 * 
	 * @param msg		Them message displayed to the user when prompting them for input.
	 * @param title		The title of the input pop-up
	 * @param relative	The component on which the pop-up should be centered. If null is passed, then
	 * 					it will be placed in the screen's center.
	 * @return the input that the user entered.
	 */
	public static String getInput(String msg, String title, Component relative) {
		String string;
		JLabel label = new JLabel(msg);
		if (label.getPreferredSize().width > 300) {
			string = "<html><body><p style='width:" + 300 + "px;'>"+msg+"</p></body></html>";
		} else {
			string = "<html><body><p>" + msg+ "</p></body></html>";
		}
		if (relative == null)
			relative = GUI.SINGLETON.quantFrame;
		return JOptionPane.showInputDialog(relative,
				string, title, JOptionPane.QUESTION_MESSAGE);

	}


}
