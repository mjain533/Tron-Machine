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
package com.typicalprojects.TronMachine.util;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.util.FileBrowser.IconName;

import java.awt.Font;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.BevelBorder;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.JOptionPane;

public class FileBrowser extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1353356043757622829L;
	private JPanel contentPane;

	public int focusedList = 1;
	private SimpleJList<File> listFiles1;
	private SimpleJList<File> listFiles2;
	private SimpleJList<File> listFiles3;

	public static final int MODE_DIRECTORIES = 1;
	public static final int MODE_FILES = 2;
	public static final int MODE_BOTH = 3;
	private boolean holdChanges = false;
	private JButton btnSelect;
	private int mode;
	private List<String> requiredExtensions;

	protected static Map<IconName, ImageIcon> fileIconMap = new HashMap<IconName, ImageIcon>();
	private JTextField txtFilePath;
	private boolean selectMultiple;
	private JComboBox<File> cbRecents;

	private List<File> selectedFiles = null;
	private List<File> newRecents = null;

	public enum IconName {
		Back("backicon.png"), ViewHorizont("fileviewhorizonticon.png"), ViewVert("fileviewverticon.png"), NewFolder("open.png"),
		Home("homeicon.png"), PDF("fileimages/pdficon.png"), TIFF("fileimages/tifficon.png"),
		Unknown("unknownfileicon.png"), Folder("foldericon.png");
		private String msg;
		private IconName(String msg) {
			this.msg = msg;
		}

	}

	static {
		fileIconMap.clear();
		for (IconName icons : IconName.values()) {
			fileIconMap.put(icons, new ImageIcon(FileBrowser.class.getClassLoader().getResource(icons.msg)));
		}

		//FileBrowser.getResourceFiles(path);
	}

	/**xf
	 * Launch the application.
	 */
	/*public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					try {
						UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
					} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
							| UnsupportedLookAndFeelException e1) {
						e1.printStackTrace();
					}

					FileBrowser frame = new FileBrowser(MODE_BOTH, Arrays.asList("czi"), false);
					frame.startBrowsing(Arrays.asList(new File("/Users/justincarrington/"), new File("/Users/justincarrington/Documents/")));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}*/

	/**
	 * Create the frame.
	 */

	public FileBrowser(int mode, boolean test, List<String> requiredExtensions, boolean selectMultiple) {

		this.mode = mode;
		this.requiredExtensions = requiredExtensions;
		this.selectMultiple = selectMultiple;

		//setAlwaysOnTop(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setModal(true);

		setBounds(100, 100, 600, 450);
		setTitle("File Select");
		setMinimumSize(new Dimension(600, 450));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.add(panel, BorderLayout.CENTER);

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_2.setBackground(new Color(211, 211, 211));

		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		JButton btnCancel = new JButton("Cancel");
		btnCancel.setFocusable(false);
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				dispose();
			}
		});

		btnSelect = new JButton("Select");
		btnSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SimpleJList<File> listToMod = focusedList == 1 ? listFiles1 : (focusedList == 2 ? listFiles2 : listFiles3);
				List<File> selected = listToMod.getSelectedValuesList();
				if (selected == null || selected.isEmpty()) {
					return;
				}

				selectedFiles = selected;
				newRecents = new ArrayList<File>();
				newRecents.add(selectedFiles.get(0).getParentFile());
				for (int i = 0; i < cbRecents.getModel().getSize() && i < 5; i++) {
					File fileRecent = cbRecents.getItemAt(i);
					if (fileRecent.exists() && !newRecents.contains(fileRecent)) {
						newRecents.add(cbRecents.getItemAt(i));
					}
				}
				dispose();
			}
		});
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addGroup(Alignment.TRAILING, gl_panel.createSequentialGroup()
										.addContainerGap(412, Short.MAX_VALUE)
										.addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 86, GroupLayout.PREFERRED_SIZE)
										.addGap(5)
										.addComponent(btnSelect, GroupLayout.PREFERRED_SIZE, 81, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_panel.createSequentialGroup()
										.addGap(6)
										.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
												.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 578, Short.MAX_VALUE)
												.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 578, Short.MAX_VALUE))))
						.addContainerGap())
				);
		gl_panel.setVerticalGroup(
				gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
						.addContainerGap()
						.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)
						.addGap(8)
						.addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 324, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
								.addComponent(btnCancel)
								.addComponent(btnSelect))
						.addContainerGap())
				);

		JLabel lblRecentLocations = new JLabel("Recent Locations:");

		cbRecents = new JComboBox<File>();
		cbRecents.setFocusable(false);
		cbRecents.setRenderer(new SimpleFileRenderer<File>());
		cbRecents.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!holdChanges)
					navigateStartingAt((File) cbRecents.getSelectedItem()); 
			}
		});

		JButton btnNewFolder = new JButton("");
		btnNewFolder.setToolTipText("New Folder");
		btnNewFolder.setFocusable(false);
		btnNewFolder.setIcon(new ImageIcon(new ImageIcon(getClass().getClassLoader().getResource("open.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
		btnNewFolder.setHorizontalAlignment(SwingConstants.CENTER);
		btnNewFolder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int focusedList = getFocusedList();
				if (focusedList == 3) {
					return;
				} else if (focusedList == 2) {
					File selected = listFiles2.getSelectedValue();
					if (selected != null && selected.isDirectory()) {
						String foldername = GUI.getInput("Select Folder Name:", "New Folder", contentPane);
						if (foldername == null || foldername.equals("")) {
							return;
						}
						File toCreate = new File(selected.getPath() + File.separator + foldername);
						try {
							if (toCreate.exists()) {
								GUI.displayMessage("This folder already exists!", "New Folder Error", contentPane, JOptionPane.ERROR_MESSAGE);
								return;
							}
							toCreate.mkdir();
							//throw new Exception();

							listFiles3.setItems(_l(selected));
							navigateInto(3, toCreate);
						} catch (Exception ex) {
							ex.printStackTrace();
							GUI.displayMessage("Could not create file!", "New Folder Error", contentPane, JOptionPane.ERROR_MESSAGE);


						}
					}
				} else if (focusedList == 1) {
					File selected = listFiles1.getSelectedValue();
					if (selected != null && selected.isDirectory()) {
						String foldername = GUI.getInput("Select Folder Name:", "New Folder", contentPane);

						if (foldername == null || foldername.equals("")) {
							return;
						}
						File toCreate = new File(selected.getPath() + File.separator + foldername);
						try {
							toCreate.mkdir();
							//throw new Exception();

							listFiles2.setItems(_l(selected));
							navigateInto(2, toCreate);
						} catch (Exception ex) {
							ex.printStackTrace();
							GUI.displayMessage("Could not create file!", "New Folder Error", contentPane, JOptionPane.ERROR_MESSAGE);
						}
					}
				}

			}
		});

		JButton btnHome = new JButton("");
		btnHome.setToolTipText("Go to Home");
		btnHome.setFocusable(false);
		btnHome.setIcon(fileIconMap.get(IconName.Home));
		btnHome.setHorizontalAlignment(SwingConstants.CENTER);
		btnHome.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String home = System.getProperty("user.home");
				if (home == null) {
					GUI.displayMessage("Could not find home file.", "File Locate Error", contentPane, JOptionPane.ERROR_MESSAGE);

					return;
				}

				File homeFile = new File(home);
				if (homeFile == null || !homeFile.exists()) {
					GUI.displayMessage("Could not find home file.", "File Locate Error", contentPane, JOptionPane.ERROR_MESSAGE);
					return;
				}
				navigateStartingAt(homeFile);
			}
		});

		JButton btnBack = new JButton("");
		btnBack.setToolTipText("Move back a directory");
		btnBack.setFocusable(false);
		btnBack.setIcon(new ImageIcon(fileIconMap.get(IconName.Back).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
		btnBack.setHorizontalAlignment(SwingConstants.CENTER);
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				navigateBack(true);
			}
		});


		JScrollPane scrFiles1 = new JScrollPane();

		JLabel lblFilePath = new JLabel("File Path:");

		JScrollPane scrFiles2 = new JScrollPane();

		JScrollPane scrFiles3 = new JScrollPane();

		scrFiles1.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrFiles2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrFiles3.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		JScrollPane scrollPane = new JScrollPane();

		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
				gl_panel_1.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_1.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel_1.createSequentialGroup()
										.addComponent(lblRecentLocations, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(cbRecents, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addGap(18)
										.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnHome, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(btnNewFolder, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_panel_1.createSequentialGroup()
										.addComponent(lblFilePath)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 482, Short.MAX_VALUE))
								.addGroup(gl_panel_1.createSequentialGroup()
										.addComponent(scrFiles1, GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(scrFiles2, GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
										.addPreferredGap(ComponentPlacement.RELATED)
										.addComponent(scrFiles3, GroupLayout.DEFAULT_SIZE, 179, Short.MAX_VALUE)))
						.addContainerGap())
				);
		gl_panel_1.setVerticalGroup(
				gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
						.addContainerGap()
						.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
										.addComponent(cbRecents, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(lblRecentLocations, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING, false)
										.addComponent(btnNewFolder, 0, 0, Short.MAX_VALUE)
										.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
												.addComponent(btnHome, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE)
												.addComponent(btnBack, GroupLayout.PREFERRED_SIZE, 24, GroupLayout.PREFERRED_SIZE))))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
								.addComponent(scrFiles1, GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
								.addComponent(scrFiles2, GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE)
								.addComponent(scrFiles3, GroupLayout.DEFAULT_SIZE, 218, Short.MAX_VALUE))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING, false)
								.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(lblFilePath, GroupLayout.DEFAULT_SIZE, 18, Short.MAX_VALUE))
						.addContainerGap())
				);

		txtFilePath = new JTextField();
		scrollPane.setViewportView(txtFilePath);
		txtFilePath.setEditable(false);
		txtFilePath.setBackground(Color.WHITE);
		txtFilePath.setColumns(10);

		FileViewRenderer<File> renderer = new FileViewRenderer<File>(mode, requiredExtensions);
		listFiles3 = new SimpleJList<File>(null, renderer);
		listFiles3.setFocusable(false);
		//listFiles3.setModel(new DefaultListModel<File>());
		scrFiles3.setViewportView(listFiles3);

		listFiles2 = new SimpleJList<File>(null, renderer);
		listFiles2.setFocusable(false);
		scrFiles2.setViewportView(listFiles2);

		listFiles1 = new SimpleJList<File>(null, renderer);
		listFiles1.setFocusable(false);
		scrFiles1.setViewportView(listFiles1);
		panel_1.setLayout(gl_panel_1);


		listFiles1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent evt) {
				if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 1) {
					if (listFiles1.getSelectedIndex() != -1 && listFiles1.getSelectedIndex() == listFiles1.locationToIndex(evt.getPoint()) && getFocusedList() != 1) {
						navigateInto(1, listFiles1.getSelectedValue());
					}
				}
			}
		});
		listFiles2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent evt) {
				if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 1) {
					if (listFiles2.getSelectedIndex() != -1 && listFiles2.getSelectedIndex() == listFiles2.locationToIndex(evt.getPoint()) && getFocusedList() != 2) {
						navigateInto(2, listFiles2.getSelectedValue());
					}
				}
			}
		});
		listFiles3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent evt) {
				if (SwingUtilities.isLeftMouseButton(evt) && evt.getClickCount() == 1) {
					if (listFiles3.getSelectedIndex() != -1 && listFiles3.getSelectedIndex() == listFiles3.locationToIndex(evt.getPoint()) && getFocusedList() != 3) {
						navigateInto(3, listFiles3.getSelectedValue());
					}
				}
			}
		});

		listFiles1.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (holdChanges)
					return;
				if (e.getValueIsAdjusting()) {
					if (!isValidSelection(listFiles1.getSelectedMult())) {
						if (e.getLastIndex() == listFiles1.getSelectedIndex()) {
							listFiles1.clearSelection();
						} else {
							listFiles1.setSelectedIndex(e.getLastIndex());
						}
					}
				} else {

					navigateInto(1, listFiles1.getSelectedValue());
				}
			}

		});

		listFiles2.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (holdChanges)
					return;
				if (e.getValueIsAdjusting()) {
					if (!isValidSelection(listFiles2.getSelectedMult())) {
						if (e.getLastIndex() == listFiles2.getSelectedIndex()) {
							listFiles2.clearSelection();
						} else {
							listFiles2.setSelectedIndex(e.getLastIndex());
						}

					}
				} else {

					navigateInto(2, listFiles2.getSelectedValue());
				}
			}

		});

		listFiles3.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if (holdChanges)
					return;
				if (e.getValueIsAdjusting()) {
					if (!isValidSelection(listFiles3.getSelectedMult())) {
						if (e.getLastIndex() == listFiles3.getSelectedIndex()) {
							listFiles3.clearSelection();
						} else {
							listFiles3.setSelectedIndex(e.getLastIndex());
						}
					}
				} else {

					navigateInto(3, listFiles3.getSelectedValue());
				}
			}

		});
		JLabel lblInstruction;
		if (this.mode == MODE_FILES) {
			if (this.requiredExtensions != null && !this.requiredExtensions.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				String separator = "";
				for (String extension : this.requiredExtensions) {
					sb.append(separator).append(".").append(extension);
					separator = ", ";
				}
				lblInstruction = new JLabel("Please Select Files (" + sb.toString() + " only)");
			} else {
				lblInstruction = new JLabel("Please select files.");
			}

		} else if (this.mode == MODE_BOTH) {
			if (this.requiredExtensions != null && !this.requiredExtensions.isEmpty()) {
				StringBuilder sb = new StringBuilder();
				String separator = "";
				for (String extension : this.requiredExtensions) {
					sb.append(separator).append(".").append(extension);
					separator = ", ";
				}
				lblInstruction = new JLabel("Please select a folder or files (" + sb.toString() + " only)");
			} else {
				lblInstruction = new JLabel("Please select a folder or files");
			}

		} else {
			lblInstruction = new JLabel("Select a directory.");
		}
		lblInstruction.setFont(GUI.mediumBoldFont);
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
				gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
						.addContainerGap()
						.addComponent(lblInstruction)
						.addContainerGap(316, Short.MAX_VALUE))
				);
		gl_panel_2.setVerticalGroup(
				gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addComponent(lblInstruction, GroupLayout.DEFAULT_SIZE, 25, Short.MAX_VALUE)
				);
		panel_2.setLayout(gl_panel_2);
		panel.setLayout(gl_panel);

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				synchronized(FileBrowser.class) {
					switch (e.getID()) {
					case KeyEvent.KEY_RELEASED:
						if (!isVisible()) {
							break;

						}
						if(e.getKeyCode() == KeyEvent.VK_KP_RIGHT || e.getKeyCode() == KeyEvent.VK_RIGHT){
							e.consume();
							moveIntoCurrent();
						} else if(e.getKeyCode() == KeyEvent.VK_KP_LEFT || e.getKeyCode() == KeyEvent.VK_LEFT){
							e.consume();
							navigateBack(false);
							break;
						} else if (e.getKeyCode() == KeyEvent.VK_KP_DOWN || e.getKeyCode() == KeyEvent.VK_DOWN) {
							e.consume();
							SimpleJList<File> listToMod = focusedList == 1 ? listFiles1 : (focusedList == 2 ? listFiles2 : listFiles3);
							listToMod.setAutoscrolls(true);
							if (listToMod.isSelectionEmpty()) {
								int index = getFirstValidIndex(listToMod, 0, true);
								if (index >= 0)
									listToMod.setSelectedIndexScroll(index);
							} else {
								int index = getFirstValidIndex(listToMod, listToMod.getSelectedIndex() + 1, true);
								if (index >= 0)
									listToMod.setSelectedIndexScroll(index);
							}
						} else if (e.getKeyCode() == KeyEvent.VK_KP_UP || e.getKeyCode() == KeyEvent.VK_UP) {
							e.consume();
							SimpleJList<File> listToMod = focusedList == 1 ? listFiles1 : (focusedList == 2 ? listFiles2 : listFiles3);
							listToMod.setAutoscrolls(true);
							if (listToMod.isSelectionEmpty()) {
								int index = getFirstValidIndex(listToMod, listToMod.getListSize() - 1, false);
								if (index >= 0)
									listToMod.setSelectedIndexScroll(index);
							} else {
								int index = getFirstValidIndex(listToMod, listToMod.getSelectedIndex() - 1, false);
								if (index >= 0)
									listToMod.setSelectedIndexScroll(index);
							}

						}
					}
				}
				return false;
			}});


		getRootPane().setDefaultButton(btnSelect);

	}

	public void startBrowsing(List<File> recents, Component component) {

		getRootPane().setDefaultButton(btnSelect); // Needed on any subsequent browses after the first one.
		setLocationRelativeTo(component);
		this.holdChanges = true;
		this.newRecents = null;
		this.selectedFiles = null;
		this.pack();
		if (recents == null || recents.isEmpty()) {
			String home = System.getProperty("user.home");
			if (home == null) {
				GUI.displayMessage("Could not find home file.", "File Locate Error", contentPane, JOptionPane.ERROR_MESSAGE);

				return;
			}

			File homeFile = new File(home);
			if (homeFile == null || !homeFile.exists()) {
				GUI.displayMessage("Could not find home file.", "File Locate Error", contentPane, JOptionPane.ERROR_MESSAGE);
				return;
			}
			this.cbRecents.removeAllItems();
			this.cbRecents.addItem(homeFile);
			this.cbRecents.setSelectedIndex(0);
			navigateStartingAt(homeFile);
		} else {
			this.cbRecents.removeAllItems();
			for (File file : recents) {
				if (file.exists() && file.isDirectory()) {
					this.cbRecents.addItem(file);
				}
			}
			if (cbRecents.getModel().getSize() == 0) {
				String home = System.getProperty("user.home");
				if (home == null) {
					GUI.displayMessage("Could not find home file.", "File Locate Error", contentPane, JOptionPane.ERROR_MESSAGE);
					return;
				}

				File homeFile = new File(home);
				if (homeFile == null || !homeFile.exists()) {
					GUI.displayMessage("Could not find home file.", "File Locate Error", contentPane, JOptionPane.ERROR_MESSAGE);
					return;
				}
				this.cbRecents.removeAllItems();
				this.cbRecents.addItem(homeFile);
				this.cbRecents.setSelectedIndex(0);
				navigateStartingAt(homeFile);
			} else {
				this.cbRecents.setSelectedIndex(0);
				navigateStartingAt(this.cbRecents.getItemAt(0));
			}


		}
		this.holdChanges = false;
		setVisible(true);
	}

	public List<File> getSelectedFiles() {
		return this.selectedFiles;
	}

	public List<File> getRecents() {
		return this.newRecents;
	}

	private void _reNavigateDueToNonExistentFile() {
		List<File> filesToRemove = new ArrayList<File>();
		for (int i = 0; i < cbRecents.getModel().getSize(); i++) {
			if (cbRecents.getItemAt(i).exists()) {
				File newFileLocation = cbRecents.getItemAt(i);
				for (File fileToRemove : filesToRemove) {
					cbRecents.removeItem(fileToRemove);
				}
				cbRecents.setSelectedItem(newFileLocation);
				navigateStartingAt(newFileLocation);
				return;
			} else {
				filesToRemove.add(cbRecents.getItemAt(i));
			}
		}
	}

	private boolean isSymlink(File file) {
		File canon;
		if (file.getParent() == null) {
			canon = file;
		} else {
			File canonDir;
			try {
				canonDir = file.getParentFile().getCanonicalFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			canon = new File(canonDir, file.getName());
		}
		
		try {
			return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	private boolean checkFileExists(File file) {
		if (!file.exists()) {
			if (!isSymlink(file)) {
				GUI.displayMessage("A file no longer exists.", "File Error", contentPane, JOptionPane.ERROR_MESSAGE);

				_reNavigateDueToNonExistentFile();
			}

			return false;
		}
		return true;
	}

	private boolean checkFilesInListsExist() {
		Enumeration<File> filesEnumeration = this.listFiles1.getElements();

		while (filesEnumeration.hasMoreElements()) {
			File file = filesEnumeration.nextElement();
			if (!file.exists() && !isSymlink(file)) {
				GUI.displayMessage("A file in this view no longer exists.", "File Error", contentPane, JOptionPane.ERROR_MESSAGE);

				_reNavigateDueToNonExistentFile();
				return false;
			}
		}
		filesEnumeration = this.listFiles2.getElements();

		while (filesEnumeration.hasMoreElements()) {
			File file = filesEnumeration.nextElement();
			if (!file.exists() && !isSymlink(file)) {
				GUI.displayMessage("A file in this view no longer exists.", "File Error", contentPane, JOptionPane.ERROR_MESSAGE);
				_reNavigateDueToNonExistentFile();
				return false;
			}
		}
		filesEnumeration = this.listFiles3.getElements();

		while (filesEnumeration.hasMoreElements()) {
			File file = filesEnumeration.nextElement();
			if (!file.exists() && !isSymlink(file)) {
				GUI.displayMessage("A file in this view no longer exists.", "File Error", contentPane, JOptionPane.ERROR_MESSAGE);
				_reNavigateDueToNonExistentFile();
				return false;
			}
		}
		return true;
	}

	private void navigateInto(int listNum, File selectedValue) {
		if (selectedValue == null)
			return;


		this.holdChanges = true;
		if (!checkFileExists(selectedValue)) {
			this.holdChanges = false;
			return;
		}
		if (!selectedValue.isDirectory()) {
			if (listNum == 1) {
				this.listFiles2.clear();
				this.listFiles3.clear();
				setFocusedList(1);
			} else if (listNum == 2) {
				this.listFiles3.clear();
				setFocusedList(2);
			} else {
				setFocusedList(3);
			}
			checkFilesInListsExist();
			this.holdChanges = false;
			return;
		}

		List<File> subFiles = _l(selectedValue);
		if (subFiles == null ) {
			subFiles = new ArrayList<File>();
		}

		if (listNum == 1) {
			this.listFiles3.clear();
			this.listFiles2.setItems(subFiles);
			setFocusedList(1);
		} else if (listNum == 2) {
			this.listFiles3.setItems(subFiles);
			this.listFiles2.setSelectedValue(selectedValue, true);
			setFocusedList(2);
		} else if (listNum == 3) {
			this.listFiles2.copyTo(this.listFiles1, true);
			this.listFiles3.copyTo(this.listFiles2, true);
			this.listFiles2.setSelectedValue(selectedValue, true);
			this.listFiles3.setItems(subFiles);
			setFocusedList(2);



		}
		checkFilesInListsExist();
		this.holdChanges= false;

	}

	private void moveIntoCurrent() {
		if (this.focusedList == 2) {
			if (this.listFiles2.isSelectionEmpty()) {
				int index = getFirstValidIndex(listFiles2, 0, true);
				if (index > -1) {
					this.listFiles2.setSelectedIndex(index);
				}
			} else if (!this.listFiles3.isEmpty()){
				int index = getFirstValidIndex(listFiles3, 0, true);
				if (index > -1) {
					this.listFiles3.setSelectedIndex(index);
				}
			}
		} else if (this.focusedList == 1) {
			if (this.listFiles1.isSelectionEmpty()) {
				int index = getFirstValidIndex(listFiles1, 0, true);
				if (index > -1) {
					this.listFiles1.setSelectedIndex(index);
				}
			} else if (!this.listFiles2.isEmpty()){
				int index = getFirstValidIndex(listFiles2, 0, true);
				if (index > -1) {
					this.listFiles2.setSelectedIndex(index);
				}
			}
		}
	}

	private int getFirstValidIndex(SimpleJList<File> list, int index, boolean forward) {

		if (index < 0 || index >= list.getListSize())
			return -1;


		if (this.mode == MODE_DIRECTORIES) {
			for (int i = index; i >= 0 && i < list.getListSize();  ) {
				File f = list.getElementAt(i);
				if (f.isDirectory()) {
					return i;
				}

				if (forward)
					i++;
				else
					i--;
			}

		} else if (this.mode == MODE_BOTH) {
			for (int i = index; i >= 0 && i < list.getListSize();  ) {
				File f = list.getElementAt(i);

				if (f.isDirectory()) {
					return i;
				} else if (this.requiredExtensions != null) {
					String name = f.getName();
					int lastIndex = name.lastIndexOf('.');
					if (this.requiredExtensions.contains(name.substring(lastIndex + 1))) {
						return i;
					}
				} else {
					return i;
				}

				if (forward)
					i++;
				else
					i--;
			}

		} else {

			for (int i = index; i >= 0 && i < list.getListSize();  ) {
				File f = list.getElementAt(i);

				if (this.requiredExtensions != null) {
					String name = f.getName();
					int lastIndex = name.lastIndexOf('.');
					if (this.requiredExtensions.contains(name.substring(lastIndex + 1))) {
						return i;
					}
				} else {
					return i;
				}

				if (forward)
					i++;
				else
					i--;
			}

		}

		return -1;

	}

	private void navigateStartingAt(File file) {
		this.holdChanges = true;

		File parentFile = file.getParentFile();

		if (parentFile == null) {
			this.listFiles1.setItems(_l(file));
			this.listFiles2.clear();
			this.listFiles3.clear();
			setFocusedList(1);
			this.holdChanges = false;
			return;
		}


		this.listFiles1.setItems(_l(parentFile));
		this.listFiles1.setSelectedValue(file, true);
		List<File> files = _l(file);
		if (files != null) {
			this.listFiles2.setItems(files);
		}
		this.listFiles3.clear();
		setFocusedList(2);

		this.holdChanges = false;
	}

	private void navigateBack(boolean shiftWhole) {
		checkFilesInListsExist();
		if (shiftWhole) {
			this.holdChanges = true;
			File file = this.listFiles1.getElements().nextElement();
			if (file == null) {
				this.holdChanges = false;
				return;
			}


			File parentFile = file.getParentFile();
			if (parentFile == null) {
				this.holdChanges = false;

				return;
			}

			List<File> itemsToSet = null;
			File parentOfParentFile= parentFile.getParentFile();

			if (parentOfParentFile == null) {
				File[] files = File.listRoots();
				if (files.length < 2) {
					this.holdChanges = false;
					return;
				}
				itemsToSet = Arrays.asList(files);
				if (!itemsToSet.contains(parentFile) ) {
					this.holdChanges = false;
					return;
				}
			} else {
				itemsToSet = _l(parentOfParentFile);
			}

			this.listFiles3.clearSelection();

			this.listFiles2.copyTo(this.listFiles3, false);
			this.listFiles1.copyTo(this.listFiles2, true);
			this.listFiles1.setItems(itemsToSet);
			this.listFiles1.setSelectedValue(parentFile, true);
			setFocusedList(2);

			this.holdChanges = false;

		} else {
			this.holdChanges = true;
			int focused = getFocusedList();

			if (focused == 3) {
				// must be clicked on a file
				this.listFiles3.clearSelection();
				setFocusedList(2);

			} else if (focused == 2) {
				this.listFiles3.clear();
				this.listFiles2.clearSelection();
				setFocusedList(1);
			} else {
				if (this.listFiles1.getListSize() == 0) {
					this.holdChanges = false;
					return;
				}
				File parentFile = this.listFiles1.getElements().nextElement().getParentFile();

				if (parentFile == null) {
					this.holdChanges = false;
					return;
				}

				File parentOfParentFile= parentFile.getParentFile();

				List<File> itemsToSet = null;
				if (parentOfParentFile == null) {
					File[] files = File.listRoots();
					if (files.length == 0) {
						this.holdChanges = false;
						return;
					}
					itemsToSet = Arrays.asList(files);
					if (!itemsToSet.contains(parentFile) ) {
						this.holdChanges = false;
						return;
					}
				} else {
					itemsToSet = _l(parentOfParentFile);
				}

				this.listFiles3.clear();
				this.listFiles1.copyTo(this.listFiles2, false);
				this.listFiles1.setItems(itemsToSet);
				this.listFiles1.setSelectedValue(parentFile, true);
				setFocusedList(1);
			}
			this.holdChanges = false;

		}
		checkFilesInListsExist();

	}

	private boolean isValidSelection(List<File> files) {
		boolean includesFile = false;
		boolean includesFolder = false;
		for (File file : files) {
			if (file.isDirectory()) {
				if (includesFolder || includesFile)
					return false;
				else {
					includesFolder = true;
					continue;
				}
			} else if (includesFolder || this.mode == MODE_DIRECTORIES) {
				return false;
			} else if (includesFile && !this.selectMultiple) {
				return false;
			}

			if (this.requiredExtensions != null) {
				String name = file.getName();
				int lastIndex = name.lastIndexOf('.');
				if (!this.requiredExtensions.contains(name.substring(lastIndex + 1))) {
					return false;
				}
			}
			includesFile = true;
		}

		return true;
	}


	public static List<File> _l(File file) {

		file.listFiles();


		File[] files = file.listFiles(new FilenameFilter() {
			public boolean accept(File directory, String fileName) {
				if (directory == null || fileName == null)
					return false;

				File file = new File(directory.getPath() + File.separator + fileName);
				if (!file.isDirectory() && !fileName.contains("."))
					return false;

				return !fileName.startsWith(".");
			}
		});
		if (files == null)
			return null;

		List<File> list = Arrays.asList(files);
		list.sort(new Comparator<File>(){
			public int compare(File fileOne, File fileTwo) {
				if (fileOne.isDirectory()) {
					if (fileTwo.isDirectory()) {
						return fileOne.getName().compareToIgnoreCase(fileTwo.getName());
					} else {
						return 1;
					}
				} else if (fileTwo.isDirectory()) {
					return -1;
				} else {
					return fileOne.getName().compareToIgnoreCase(fileTwo.getName());
				}
			}
		});

		return list;

	}

	public int getFocusedList() {
		return this.focusedList;
	}

	public void setFocusedList(int listNum) {
		switch (listNum) {
		case 1:
			this.focusedList = 1;
			this.listFiles1.setSelectionBackground(Color.RED);
			setSelectButtonAndFilePath(listFiles1);
			this.listFiles2.setSelectionBackground(Color.LIGHT_GRAY);
			this.listFiles3.setSelectionBackground(Color.LIGHT_GRAY);	
			break;
		case 2:
			this.focusedList = 2;
			this.listFiles2.setSelectionBackground(Color.RED);
			setSelectButtonAndFilePath(listFiles2);
			this.listFiles1.setSelectionBackground(Color.LIGHT_GRAY);
			this.listFiles3.setSelectionBackground(Color.LIGHT_GRAY);	
			break;
		case 3:
			this.focusedList = 3;

			this.listFiles3.setSelectionBackground(Color.RED);
			setSelectButtonAndFilePath(listFiles3);
			this.listFiles1.setSelectionBackground(Color.LIGHT_GRAY);
			this.listFiles2.setSelectionBackground(Color.LIGHT_GRAY);	
			break;
		}

	}

	private void setSelectButtonAndFilePath(SimpleJList<File> list) {
		if (list.isSelectionEmpty()) {
			this.txtFilePath.setText("");
			this.btnSelect.setEnabled(false);
		} else {
			List<File> files = list.getSelectedValuesList();
			StringBuilder sb = new StringBuilder();
			sb.append(files.get(0));
			for (int i = 1; i < files.size(); i++) {
				sb.append(" ; ").append(files.get(i));
			}
			this.txtFilePath.setText(sb.toString());
			if ((this.mode == MODE_DIRECTORIES && !files.get(0).isDirectory()) || (this.mode == MODE_FILES && files.get(0).isDirectory())) {
				this.btnSelect.setEnabled(false);
			} else {
				this.btnSelect.setEnabled(true);

			}

		}

	}

	public void getFiles() {

	}

}
class FileViewRenderer<K> implements ListCellRenderer<K> {

	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	private static final Font fileListFont = GUI.mediumBoldFont;
	private static final Color normalColor = Color.BLACK;
	private static final Color deemphasizedColor = new Color(196, 196, 196);



	private final List<String> requiredExtensions;
	private final int mode;

	public FileViewRenderer(int mode, List<String> requiredExtensionsNoDot) {
		this.mode = mode;
		this.requiredExtensions = requiredExtensionsNoDot;

	}

	@SuppressWarnings("rawtypes")
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {


		Icon theIcon = null;
		String theText = null;
		Color color = null;

		JLabel renderer = (JLabel) defaultRenderer
				.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);

		if (value instanceof File) {

			File file = (File) value;

			if (file.isDirectory()) {
				theIcon = FileBrowser.fileIconMap.get(IconName.Folder);
				color = normalColor;
			} else {

				theIcon = FileBrowser.fileIconMap.get(IconName.Unknown);


				color = normalColor;

				if (this.mode == FileBrowser.MODE_DIRECTORIES) {
					color = deemphasizedColor;
				} else if (this.requiredExtensions != null) {
					int dotIndex = file.getName().lastIndexOf('.');
					if (dotIndex > 0 && !this.requiredExtensions.contains(file.getName().substring(dotIndex + 1))) {
						color = deemphasizedColor;
					}

				}
			}


			if (file.getName().length()==0) {
				theText = file.getPath();
			} else {
				theText = file.getName();
			}

		} else {
			color = normalColor;
			theText = "Error";
		}
		if (theIcon != null) {
			renderer.setIcon(theIcon);
		}

		renderer.setFont(fileListFont);
		renderer.setText(theText);
		renderer.setForeground(color);
		return renderer;


	}

}
class SimpleFileRenderer<K> implements ListCellRenderer<K> {

	protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

	@SuppressWarnings("rawtypes")
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {


		JLabel renderer = (JLabel) defaultRenderer
				.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);

		if (value instanceof File) {
			renderer.setText(((File) value).getName());
		} else if (value != null) {
			renderer.setText(value.toString());
		}

		return renderer;


	}

}


