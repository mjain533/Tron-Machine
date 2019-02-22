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
import java.io.File;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Desktop;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;

import org.json.JSONException;
import org.json.JSONObject;

import com.typicalprojects.TronMachine.MainFrame;
import com.typicalprojects.TronMachine.neuronal_migration.Settings.SettingsManager;
import com.typicalprojects.TronMachine.neuronal_migration.Wizard.Status;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlDisplay;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlInstructions;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlLog;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlOptions;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlSelectFiles;
import com.typicalprojects.TronMachine.popup.BrightnessAdjuster;
import com.typicalprojects.TronMachine.util.Logger;
import com.typicalprojects.TronMachine.util.Toolbox;

import java.awt.Font;
import java.awt.Toolkit;
import javax.swing.JSeparator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ListSelectionModel;

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

	private Preferences prefs;
	private IntermediateProcessingGUI itmdProcessingGUI;
	private BrightnessAdjuster brightnessAdjuster;
	private volatile JLabel lblAttributes = null;
	private JMenuItem mntmItmdProcessingGUI;
	public static GUI SINGLETON = null;

	/**
	 * Create the application.
	 * @throws IOException 
	 * @throws FormatException 
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
	
	public class RectentFileList extends JPanel {

        private final JList<File> list;
        private final FileListModel listModel;
        private final JFileChooser fileChooser;

        public RectentFileList(JFileChooser chooser) {
            fileChooser = chooser;
            listModel = new FileListModel();
            list = new JList<File>(listModel);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            list.setCellRenderer(new FileListCellRenderer());

            setLayout(new BorderLayout());
            add(new JScrollPane(list));

            list.addListSelectionListener(new ListSelectionListener() {
                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        File file = list.getSelectedValue();
                        // You might like to check to see if the file still exists...
                        fileChooser.setSelectedFile(file);
                    }
                }
            });
        }

        public void clearList() {
            listModel.clear();
        }

        public void add(File file) {
            listModel.add(file);
        }

        public class FileListModel extends AbstractListModel<File> {

            private List<File> files;

            public FileListModel() {
                files = new ArrayList<File>();
            }

            public void add(File file) {
                if (!files.contains(file)) {
                    if (files.isEmpty()) {
                        files.add(file);
                    } else {
                        files.add(0, file);
                    }
                    fireIntervalAdded(this, 0, 0);
                }
            }

            public void clear() {
                int size = files.size() - 1;
                if (size >= 0) {
                    files.clear();
                    fireIntervalRemoved(this, 0, size);
                }
            }

            @Override
            public int getSize() {
                return files.size();
            }

            @Override
            public File getElementAt(int index) {
                return files.get(index);
            }
        }

        public class FileListCellRenderer extends DefaultListCellRenderer {

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof File) {
                    File file = (File) value;
                    Icon ico = FileSystemView.getFileSystemView().getSystemIcon(file);
                    setIcon(ico);
                    setToolTipText(file.getParent());
                    setText(file.getName());
                }
                return this;
            }

        }

    }
  

	public void show() {

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

		JMenuBar menuBar = new JMenuBar();
		quantFrame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		mnFile.setFont(smallBoldFont.deriveFont(Font.BOLD, 16));
		menuBar.add(mnFile);

		JMenuItem mntmExit = new JMenuItem("Exit");
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (GUI.confirmWithUser("Do you really want to quit?", "Confirm Quit", quantFrame, JOptionPane.ERROR_MESSAGE)) {
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
		mnOptions.setFont(smallBoldFont.deriveFont(Font.BOLD, 16));
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
		mnProcess.setFont(smallBoldFont.deriveFont(Font.BOLD, 16));
		menuBar.add(mnProcess);


		this.itmdProcessingGUI = new IntermediateProcessingGUI(this);
		this.mntmItmdProcessingGUI = new JMenuItem("Process from Intermediates");
		mnProcess.add(this.mntmItmdProcessingGUI);
		this.mntmItmdProcessingGUI.addActionListener(new ActionListener() {
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
		mntmItmdProcessingGUI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));


		JMenu mnNavigation = new JMenu("Navigation");
		mnNavigation.setFont(smallBoldFont.deriveFont(Font.BOLD, 16));
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
		lblAttributes.setFont(smallBoldFont);
		lblAttributes.setBorder(new EmptyBorder(6, 10, 10, 10));
		quantFrame.getContentPane().add(lblAttributes, BorderLayout.SOUTH);

		JMenu mnHelp = new JMenu("Help");
		mnHelp.setFont(smallBoldFont.deriveFont(Font.BOLD, 16));
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

		setUIFont(smallBoldFont);
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
		try {
			Runtime.getRuntime().exec("defaults write -g ApplePressAndHoldEnabled -bool true");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		this.prefs.setEnabled(enabled);
		this.prefs.resetPreferences(enabled);
		this.mntmItmdProcessingGUI.setEnabled(enabled);
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

	/**
	 * @return dimensions of current screen. [width, height]
	 */
	public int[] updateResolution() {
		DisplayMode mode = this.quantFrame.getGraphicsConfiguration().getDevice().getDisplayMode();
		return new int[] {mode.getWidth(), mode.getHeight()};
	}

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

	public static void displayMessage(String msg, String title, Component relative, int joptionpaneMsgType) {
		String string;
		JLabel label = new JLabel(msg);
		if (label.getPreferredSize().width > 300) {
			string = "<html><body><p style='width:" + 300 + "px;'>"+msg+"</p></body></html>";
		} else {
			string = "<html><body><p>" + msg+ "</p></body></html>";
		}
		if (relative == null)
			relative = GUI.SINGLETON.quantFrame;
		JOptionPane.showMessageDialog(relative,
				string, title, joptionpaneMsgType);


	}
	
	public static String getTooltipText(String text) {
		JLabel label = new JLabel(text);
		if (label.getPreferredSize().width > 200) {
			text = "<html><body><p style='width:" + 200 + "px;'>"+text+"</p></body></html>";
		} else {
			text = "<html><body><p>" + text+ "</p></body></html>";
		}
		return text;

	}

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
