package com.typicalprojects.CellQuant.neuronal_migration;


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
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.border.EmptyBorder;

import com.typicalprojects.CellQuant.MainFrame;
import com.typicalprojects.CellQuant.neuronal_migration.panels.PnlDisplay;
import com.typicalprojects.CellQuant.neuronal_migration.panels.PnlInstructions;
import com.typicalprojects.CellQuant.neuronal_migration.panels.PnlLog;
import com.typicalprojects.CellQuant.neuronal_migration.panels.PnlOptions;
import com.typicalprojects.CellQuant.neuronal_migration.panels.PnlSelectFiles;
import com.typicalprojects.CellQuant.popup.BrightnessAdjuster;
import com.typicalprojects.CellQuant.util.ImageContainer;
import com.typicalprojects.CellQuant.util.SynchronizedProgress;
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.JSeparator;

import java.awt.event.InputEvent;
import javax.swing.JPanel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class GUI  {


	private JFrame quantFrame;
	private final MainFrame parentFrame;
	public static final Font smallFont = new Font("PingFang TC", Font.BOLD, 13);
	public static final Font smallPlainFont = new Font("PingFang TC", Font.PLAIN, 13);
	public static final Font medium_smallPlainFont = new Font("PingFang TC", Font.PLAIN, 10);

	public static final String folderName = "Neuronal_Migration_Resources";
	public static final String settingsFile = "settings.txt";

	public static String dateString = null;

	public static Map<Integer, ImageContainer.Channel> channelMap = new HashMap<Integer, ImageContainer.Channel>();
	public static List<Channel> channelsToProcess = new ArrayList<Channel>();
	public static Channel channelForROIDraw = null;
	public static File outputLocation = null;
	public static File lastSelectFileLocation = null;
	static {
		channelMap.clear();
		channelMap.put(0, Channel.GREEN);
		channelMap.put(1, Channel.WHITE);
		channelMap.put(2, Channel.RED);
		channelMap.put(3, Channel.BLUE);
		channelsToProcess.clear();
		channelsToProcess.add(Channel.RED);
		channelsToProcess.add(Channel.GREEN);
		channelForROIDraw = Channel.BLUE;

	}


	private PnlInstructions pnlInstructions;
	private PnlSelectFiles pnlSelectFiles;
	private PnlLog pnlLog;
	private PnlDisplay pnlDisplay;
	private PnlOptions pnlOptions;

	private SynchronizedProgress progressReporter;

	private volatile Wizard wizard;
	private JMenuItem mntmPreferences;
	private JMenuItem mntmBrightnessAdj;
	private JMenuItem mntmSummaryStats;

	private Preferences prefs;
	private BrightnessAdjuster brightnessAdjuster;
	private StatsGUI statsGUI;


	/**
	 * Create the application.
	 * @throws IOException 
	 * @throws FormatException 
	 */
	public GUI(MainFrame parent) throws IOException {
		

		this.parentFrame = parent;
		DateFormat dateFormat = new SimpleDateFormat("ddMMyyyy h-m-s a");
		Date date = new Date();
		dateString = dateFormat.format(date);
		
		File baseDir = new File(folderName);
		if (!baseDir.exists() || !baseDir.isDirectory()) {
			baseDir.mkdir();

		}

		File settings = new File(folderName + File.separator + settingsFile);
		if (!settings.exists()) {
			boolean fileCreated = false;

			while (!fileCreated) {
				try {
					settings.createNewFile();
					fileCreated = true;
					channelMap.clear();
					channelMap.put(0, Channel.GREEN);
					channelMap.put(1, Channel.WHITE);
					channelMap.put(2, Channel.RED);
					channelMap.put(3, Channel.BLUE);
					channelsToProcess.clear();
					channelsToProcess.add(Channel.RED);
					channelsToProcess.add(Channel.GREEN);
					channelForROIDraw = Channel.BLUE;
					GUI.outputLocation = null;
					Preferences.writeSettingsFromGUI();
				} catch (IOException e) {
					e.printStackTrace();
					System.out.println(e.getMessage());
					if (JOptionPane.showConfirmDialog(null, "A directory for holding contents of this application could not be created on your computer."
							+ " Would you like to retry?", "Could not create file.", JOptionPane.OK_CANCEL_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.CANCEL_OPTION ) {
						doExit();
					}

				}
				

			}

		} else {

			Preferences.readSettingsIntoGUI();	

		}
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
		prefs = new Preferences(this);
		mntmPreferences.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				EventQueue.invokeLater(new Runnable() {
					public void run() {
						try {
							prefs.display(quantFrame);
							unfocusTemporarily();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});

			}
		});
		mntmPreferences.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.META_MASK));
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
		mntmBrightnessAdj.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.META_MASK));
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
		mntmSummaryStats.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.META_MASK));

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
		mntmBackToMain.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.META_MASK));
		
		final Properties properties = new Properties();
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream("project.properties"));
		} catch (IOException e1) {
			// shouldn't happen
			e1.printStackTrace();
		}
		JLabel lblAttributes = new JLabel("Developed by Justin Carrington, Russ Taylor, Kendra Taylor, and Erik Dent. Copyright 2018. Version "+ properties.getProperty("version")+".");
		lblAttributes.setFont(new Font("PingFang TC", Font.BOLD, 13));
		lblAttributes.setBorder(new EmptyBorder(6, 10, 10, 10));
		quantFrame.getContentPane().add(lblAttributes, BorderLayout.SOUTH);


		// main content area
		JPanel pnlCONTENT = new JPanel();
		quantFrame.getContentPane().add(pnlCONTENT, BorderLayout.CENTER);


		//Instructions
		pnlInstructions = new PnlInstructions();

		// Select Files
		pnlSelectFiles = new PnlSelectFiles(this);

		// Log
		pnlLog = new PnlLog(this);
		this.progressReporter = new SynchronizedProgress(null, pnlLog);

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
										.addComponent(pnlSelectFiles.getRawPanel(), GroupLayout.PREFERRED_SIZE, /*GroupLayout.DEFAULT_SIZE*/300, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(pnlLog.getRawPanel(), GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(pnlOptions.getRawPanel(), GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)))
						.addGap(5))
				);


		pnlCONTENT.setLayout(gl_pnlCONTENT);

		setUIFont(smallFont);

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
		//TODO
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

	public synchronized void log(String message) {
		this.progressReporter.setProgress(message, -1, -1);
	}

	public synchronized void log(String message, int progressSoFar, int totalProgress) {
		this.progressReporter.setProgress(message, progressSoFar, totalProgress);
	}

	public Wizard getWizard() {
		return this.wizard;
	}

	public SynchronizedProgress getProgressReporter() {
		return this.progressReporter;
	}

	public BrightnessAdjuster getBrightnessAdjuster() {
		return this.brightnessAdjuster;
	}

	public void setBrightnessAdjustOptionEnabled(boolean enabled) {
		this.mntmBrightnessAdj.setEnabled(enabled);

	}

	public void setMenuItemsEnabledDuringRun(boolean enabled) {
		this.mntmPreferences.setEnabled(enabled);
		
		this.prefs.removeDisplay();
		this.statsGUI.removeDisplay();
		this.statsGUI.resetFields();
		this.prefs.resetPreferences();
	}
	

}
