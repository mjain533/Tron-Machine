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
package com.typicalprojects.TronMachine.neuronal_migration.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.OutputOption;
import com.typicalprojects.TronMachine.neuronal_migration.RunConfiguration;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager.Channel;
import com.typicalprojects.TronMachine.neuronal_migration.Wizard.Status;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlDisplay.PnlDisplayFeedbackReceiver;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlDisplay.PnlDisplayPage;
import com.typicalprojects.TronMachine.neuronal_migration.processing.NeuronProcessor;
import com.typicalprojects.TronMachine.neuronal_migration.processing.ObjectEditableImage;
import com.typicalprojects.TronMachine.neuronal_migration.processing.PreprocessedEditableImage;
import com.typicalprojects.TronMachine.neuronal_migration.processing.ROIEditableImage;
import com.typicalprojects.TronMachine.neuronal_migration.processing.RoiProcessor;
import com.typicalprojects.TronMachine.popup.HelpPopup;
import com.typicalprojects.TronMachine.popup.BrightnessAdjuster.BrightnessChangeReceiver;
import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.ImagePhantom;
import com.typicalprojects.TronMachine.util.Point;
import com.typicalprojects.TronMachine.util.PolarizedPolygonROI;
import com.typicalprojects.TronMachine.util.SimpleJList;
import com.typicalprojects.TronMachine.util.Zoom;



/**
 * The panel used to display options during each phase of the neuron processing. On the left side of the GUI.
 * 
 * @author Justin Carrington
 */
public class PnlOptions implements PnlDisplayFeedbackReceiver, BrightnessChangeReceiver {
	
	/** Constant designating {@link PnlOptions} is disabled **/
	public static final int STATE_DISABLED = 1;
	
	/** Constant designating {@link PnlOptions} is displaying slice selection **/
	public static final int STATE_SLICE = 2;
	
	/** Constant designating {@link PnlOptions} is displaying object selection fields **/
	public static final int STATE_OBJ = 3;
	
	/** Constant designating {@link PnlOptions} is displaying post-object-selection fields **/
	public static final int STATE_POST_OBJ = 4;
	
	/** Constant designating {@link PnlOptions} is displaying ROI selection fields **/
	public static final int STATE_ROI = 5;
	
	/** Constant designating the layout for {@link PnlOptions} is slice selection **/
	public static final int LAYOUT_TYPE_SLICE = 1;
	
	/** Constant designating the layout for {@link PnlOptions} is object selection **/
	public static final int LAYOUT_TYPE_OBJ = 2;
	
	/** Constant designating the layout for {@link PnlOptions} is post-object selection **/
	public static final int LAYOUT_TYPE_POST_OBJ = 3;
	
	/** Constant designating the layout for {@link PnlOptions} is ROI selection **/
	public static final int LAYOUT_TYPE_ROI = 4;
	


	private final JPanel rawPanel;
	private final GUI gui;

	private volatile PreprocessedEditableImage imageCurrentlyPreprocessing = null;
	private volatile ObjectEditableImage imageCurrentlyObjEditing = null;
	private volatile ROIEditableImage imageCurrentlyROIEditing = null;

	private JLabel lblDisabled;



	private JTextField distTxtCurrDisp;
	private JButton distBtnAddROI;
	private JButton distBtnCancelROI;
	private JButton distBtnDeleteROI;
	private JButton distBtnNext;
	private JButton distBtnHelp;
	private HelpPopup distHelpPopup;



	private JTextField sliceTxtDisplaying;
	private JTextField sliceTxtLowSlice;
	private JTextField sliceTxtHighSlice;
	private JLabel sliceLblCurrDisp;
	private JLabel sliceLblSelectSlices;
	private JLabel sliceLblThru;
	private JButton sliceBtnNext;
	private JLabel sliceLblERR;
	private JCheckBox sliceChkApplyToAll;

	private JLabel objLblCurrDisp;
	private JLabel objLblInstructions;
	private JLabel objLblRemove;
	private JLabel objLblShow;
	private JCheckBox objChkMask;
	private JCheckBox objChkOriginal;
	private JCheckBox objChkDots;
	private JButton objBtnRemove;
	private JButton objBtnNext;
	private JTextField objTxtCurrDisp;
	private JTextField objTxtRemove;
	private JButton objBtnHelp;
	private JButton objBtnPick;
	private HelpPopup objHelpPopup;
	
	private JLabel pstObLblCurrDisp;
	private JLabel pstObLblCurrView;
	private JLabel pstObLblInstructions;
	private JTextField pstObTxtCurrDisp;
	private JTextField pstObTxtCurrView;
	private JButton pstObBtnNext;
	private JButton pstObBtnHelp;

	private List<File> imagesForObjectAnalysis = new LinkedList<File>();
	private List<File> imagesForObjectSelection = new LinkedList<File>();
	private List<File> imagesForROICreation = new LinkedList<File>();
	private List<File> imagesForROIAnalysis = new LinkedList<File>();
	private List<ImagePhantom> imagesForSliceSelection = new LinkedList<ImagePhantom>();
	private int currentState = STATE_DISABLED;
	private NeuronProcessor neuronProcessor;
	private RoiProcessor roiProcessor;

	private JScrollPane roiSP;
	private SimpleJList<PolarizedPolygonROI> roiListROI;
	private JLabel roiLblInstruction;
	private JLabel roiLblCurrDisp;

	/** Stores the current worker. Useful because may need to cancel processing at some point. **/
	private volatile Thread currWorker;


	/**
	 * Constructs the options panel. It is fully constructed after this constructor finishes, and is ready
	 * to be added to a GUI component. This can be done via the {@link #getRawPanel()} method.
	 * 
	 * @param gui the main GUI frame. This is used for many purposes, including accessing other panels in the
	 * 	GUI.
	 */
	public PnlOptions(GUI gui) {

		this.rawPanel =  new JPanel();
		this.rawPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		this.gui = gui;

		String message = "<html><h3><em>Removing Objects</em></h3>"
				+ "Type object numbers in the text field after 'Rmv:' and click Remove to delete objects. You may enter a single number, a range of numbers (i.e. 1-5), or a combination of both separated by commas and no spaces (i.e. 1-5,7,9,11-15). "
				+ "Points can also be removed by right clicking them on the image itself. You may remove large chunks of objects in a region by clicking 'Pick Mult.' and then drawing a polygon by clicking points on the image. Points within the polygon will be removed. "
				+ "The polygon's start and end points don't need to line up exactly. A line will be drawn between your start and end points to finish the shape, but make sure the start and end points are close. Points in this region will be removed from all processed channels."
				+ "<br>"
				+ "<h3><em>Adding Objects</em></h3>"
				+ "To add points, simply (left) click on the image."
				+ "<br>"
				+ "<h3><em>Modifying the View</em></h3>"
				+ "You can resize the program window to enlarge the image. If this is insufficient, you may zoom in by hitting the Shift key (will zoom in where your mouse lies, so the mouse must be within the image boundaries). "
				+ "You can navigate while zoomed using A (left), W (up), S (down), D (right), and zoom out using the space bar.<br><br>"
				+ "You may also toggle the image dots (and numbers), object mask, and original image using the checkboxes in the options panel."
				+ "</html>";

		this.objHelpPopup = new HelpPopup(580, 615, message);

		String message2 = "<html>Click on the image to start adding points to the ROI. Then, click 'Add' in the "
				+ "panel below the image to add the region. You can delete this region via the 'Delete' button.<br><br>"
				+ "To RESET the points you've selected, click the 'Cancel' button.</html>";

		this.distHelpPopup = new HelpPopup(250, 350, message2);

		lblDisabled = new JLabel("<html><body><p style='width: 100px; text-align: center;'>Please select images using the interface above.</p></body></html>");
		lblDisabled.setHorizontalAlignment(SwingConstants.CENTER);

		initializeInnerPanels();
		setDisplayState(STATE_DISABLED, null);

	}

	/**
	 * This panel works by actually completing replacing an inner JPanel based on the current display state.
	 * This method does all of the initialization of these Panels.
	 */
	private void initializeInnerPanels() {


		// distance 

		roiLblCurrDisp = new JLabel("Currently Displaying");

		distTxtCurrDisp = new JTextField();
		distTxtCurrDisp.setColumns(10);
		distTxtCurrDisp.setFocusable(false);
		distTxtCurrDisp.setEditable(false);

		roiLblInstruction = new JLabel("Please create at least one ROI below.");

		distBtnAddROI = new JButton("<html><p style='text-align: center;'><a style='text-decoration:underline'>A</a>dd</p></html>");
		distBtnAddROI.setFont(GUI.extraSmallBoldFont);
		distBtnAddROI.setFocusable(false);
		distBtnAddROI.setMargin(new Insets(0, -30, 0, -30));

		distBtnCancelROI = new JButton("Clear");
		distBtnCancelROI.setFont(GUI.extraSmallBoldFont);
		distBtnCancelROI.setFocusable(false);
		distBtnAddROI.setMnemonic(KeyEvent.VK_C);
		distBtnCancelROI.setMargin(new Insets(0, -30, 0, -30));

		distBtnDeleteROI = new JButton("Delete");
		distBtnDeleteROI.setFont(GUI.extraSmallBoldFont);
		distBtnDeleteROI.setFocusable(false);
		distBtnDeleteROI.setMargin(new Insets(0, -30, 0, -30));


		roiSP = new JScrollPane();
		roiSP.setFocusable(false);

		distBtnNext = new JButton("Next");
		distBtnNext.setFocusable(false);


		distBtnHelp = new JButton("");
		distBtnHelp.setIcon(new ImageIcon(new ImageIcon(PnlOptions.class.getResource("/question.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
		distBtnHelp.setForeground(Color.BLUE);
		distBtnHelp.setBorderPainted(false);
		distBtnHelp.setOpaque(false);
		distBtnHelp.setBackground(Color.WHITE);
		distBtnHelp.setFocusable(false);




		roiListROI = new SimpleJList<PolarizedPolygonROI>(new PolygonROIRenderer<PolarizedPolygonROI>(this));
		roiSP.setViewportView(roiListROI);
		roiListROI.setFocusable(false);
		MouseAdapter mouseAdaptor = new MouseAdapter() {
			public void mouseClicked(MouseEvent evt) {
				@SuppressWarnings("unchecked")
				SimpleJList<PolarizedPolygonROI> list = (SimpleJList<PolarizedPolygonROI>)evt.getSource();
				if (evt.getClickCount() == 2) {

					// Double-click detected
					int index = list.locationToIndex(evt.getPoint());
					if (index > -1 && imageCurrentlyROIEditing != null) {
						PolarizedPolygonROI output = list.getElementAt(index);
						imageCurrentlyROIEditing.promptUserForTag(output);
						list.updateUI();

					}
				}
			}
		};
		roiListROI.addMouseListener(mouseAdaptor);

		// Objects

		objLblCurrDisp = new JLabel("Displaying:");

		objTxtCurrDisp = new JTextField();
		objTxtCurrDisp.setColumns(10);
		objTxtCurrDisp.setFocusable(false);
		objTxtCurrDisp.setEditable(false);

		objLblInstructions = new JLabel("Click image to add points. Remove below.");

		objLblRemove = new JLabel("<html>Rmv:</html>");
		objLblRemove.setHorizontalTextPosition(SwingConstants.LEFT);
		objLblRemove.setHorizontalAlignment(SwingConstants.LEFT);

		objBtnHelp = new JButton("");
		objBtnHelp.setIcon(new ImageIcon(new ImageIcon(PnlOptions.class.getResource("/question.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
		objBtnHelp.setForeground(Color.BLUE);
		objBtnHelp.setBorderPainted(false);
		objBtnHelp.setOpaque(false);
		objBtnHelp.setBackground(Color.WHITE);
		objBtnHelp.setFocusable(false);

		objTxtRemove = new JTextField();
		objTxtRemove.setColumns(10);
		objTxtRemove.setFocusable(true);
		objTxtRemove.setEnabled(true);
		objTxtRemove.setEditable(true);

		objBtnRemove = new JButton("Remove");

		objBtnNext = new JButton("Next");
		objBtnNext.setMargin(new Insets(0,5,0,5));
		objBtnPick = new JButton("Pick Mult");
		objBtnPick.setMargin(new Insets(0,0,0,0));
		objBtnRemove.setFocusable(false);
		objBtnRemove.setMargin(new Insets(0,0,0,0));
		objBtnPick.setFocusable(false);
		objBtnNext.setFocusable(false);

		objLblShow = new JLabel("Show:");
		objChkMask = new JCheckBox("Mask");
		objChkMask.setFocusable(false);
		objChkMask.setBackground(GUI.colorPnlEnabled);
		objChkOriginal = new JCheckBox("Original");
		objChkOriginal.setFocusable(false);
		objChkOriginal.setBackground(GUI.colorPnlEnabled);

		objChkDots = new JCheckBox("Dots");
		objChkDots.setFocusable(false);
		objChkDots.setBackground(GUI.colorPnlEnabled);
		
		// Post-object processing
		
		pstObBtnNext = new JButton("Next");
		pstObBtnNext.setMargin(new Insets(0,5,0,5));
		pstObBtnNext.setFocusable(false);
		
		pstObLblCurrDisp = new JLabel("Displaying:");

		pstObTxtCurrDisp = new JTextField();
		pstObTxtCurrDisp.setColumns(10);
		pstObTxtCurrDisp.setEditable(false);
		
		pstObLblCurrView = new JLabel("Curr. View:");

		pstObTxtCurrView = new JTextField();
		pstObTxtCurrView.setColumns(10);
		pstObTxtCurrView.setEditable(false);
		
		pstObBtnHelp = new JButton("");
		pstObBtnHelp.setIcon(new ImageIcon(new ImageIcon(PnlOptions.class.getResource("/question.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
		pstObBtnHelp.setForeground(Color.BLUE);
		pstObBtnHelp.setBorderPainted(false);
		pstObBtnHelp.setOpaque(false);
		pstObBtnHelp.setBackground(Color.WHITE);
		pstObBtnHelp.setFocusable(false);

		pstObLblInstructions = new JLabel("Right click to remove double-counted points.");

		// Slices

		sliceLblCurrDisp = new JLabel("Displaying:");

		sliceLblERR = new JLabel("Invalid selection.");

		sliceTxtDisplaying = new JTextField();
		sliceTxtDisplaying.setColumns(10);
		sliceTxtDisplaying.setEditable(false);

		sliceLblSelectSlices = new JLabel("Select Slices:");

		sliceTxtLowSlice = new JTextField();
		sliceTxtLowSlice.setColumns(10);

		sliceLblThru = new JLabel("thru");

		sliceTxtHighSlice = new JTextField();
		sliceTxtHighSlice.setColumns(10);
		sliceChkApplyToAll = new JCheckBox("Apply to all (if possible)");
		sliceChkApplyToAll.setBackground(GUI.colorPnlEnabled);

		sliceBtnNext = new JButton("Next");
		sliceBtnNext.setFocusable(false);

		sliceLblERR = new JLabel("ERROR: Invalid slice selection.");
		sliceLblERR.setForeground(Color.RED);
		sliceLblERR.setVisible(false);

		this.sliceBtnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {


				currWorker = new Thread(new Runnable() {
					public void run(){
						sliceBtnNext.setEnabled(false);
						int lower = Integer.parseInt(sliceTxtLowSlice.getText());
						int higher = Integer.parseInt(sliceTxtHighSlice.getText());

						if (lower < 1 || lower >= higher ||
								!imageCurrentlyPreprocessing.isValidSliceSelection(lower, higher)) {
							sliceBtnNext.setEnabled(true);
							sliceLblERR.setVisible(true);
							return;
						}
						sliceLblERR.setVisible(false);


						if (currWorker != null && !nextPreprocessedImage(lower, higher, sliceChkApplyToAll.isSelected())) {
							sliceChkApplyToAll.setSelected(false);
							gui.getWizard().nextState();
							imagesForSliceSelection.clear();
						}

						sliceBtnNext.setEnabled(true);

					}
				});
				currWorker.setDaemon(true);
				currWorker.start();

			}
		});



		this.objBtnHelp.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				objHelpPopup.display(gui.getComponent());				
			}

		});

		this.objBtnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (objBtnRemove.getText().equals("Cancel")) {
					objBtnNext.setEnabled(true);
					objBtnPick.setText("Pick Mult.");
					objBtnRemove.setText("Remove");
					objTxtRemove.setEnabled(true);
					if (imageCurrentlyObjEditing != null) {
						imageCurrentlyObjEditing.cancelDeletionZone(gui.getPanelDisplay().getSliderSelectedPageAsChannel());
					}
					return;
				}

				String text = objTxtRemove.getText();

				if (text.equals("")) return;

				Set<Integer> objectsToRemove = new HashSet<Integer>();
				String[] candidates = text.split(",");
				try {
					for (String candidate : candidates) {
						if (!candidate.contains("-")) {
							objectsToRemove.add(Integer.parseInt(candidate));
						} else {
							String[] upperLower = candidate.split("-", 2);
							Integer lower = Integer.parseInt(upperLower[0]);
							Integer higher = Integer.parseInt(upperLower[1]);
							if (lower > higher) {
								GUI.displayMessage("Error: For value ranges, the lower bound must be lower than the higher bound.", "Invalid Input", gui.getPanelDisplay().getImagePanel(), JOptionPane.ERROR_MESSAGE);
								return;
							}
							for (int i = lower; i <= higher; i++) {
								objectsToRemove.add(i);
							}

						}
					}
				} catch (Exception exc) {
					GUI.displayMessage("Error: Found text that isn't valid format (not ','  '-'  or an integer).", "Invalid Input", gui.getPanelDisplay().getImagePanel(), JOptionPane.ERROR_MESSAGE);

					return;
				}

				Channel chan = gui.getPanelDisplay().getSliderSelectedPageAsChannel();
				boolean validPoints = imageCurrentlyObjEditing.removePoints(chan, objectsToRemove);
				objTxtRemove.setText("");
				if (!validPoints) {
					GUI.displayMessage("Warning: Your input contained at least one object number that doesn't exist.", "Odd Input", gui.getPanelDisplay().getImagePanel(), JOptionPane.WARNING_MESSAGE);
				}


			}
		});

		this.objChkDots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (imageCurrentlyObjEditing != null) {
					try {

						imageCurrentlyObjEditing.setDisplaying(objChkOriginal.isSelected(), objChkMask.isSelected(), objChkDots.isSelected(), gui.getPanelDisplay().getSliderSelectedPageAsChannel());

					} catch (IllegalArgumentException ex) {
						GUI.displayMessage("You must have at least one layer selected.", "Layering Error", gui.getPanelDisplay().getImagePanel(), JOptionPane.ERROR_MESSAGE);

						objChkDots.setSelected(true);
					}
				}
			}
		});

		this.objChkOriginal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (imageCurrentlyObjEditing != null) {
					try {

						imageCurrentlyObjEditing.setDisplaying(objChkOriginal.isSelected(), objChkMask.isSelected(), objChkDots.isSelected(), gui.getPanelDisplay().getSliderSelectedPageAsChannel());

					} catch (IllegalArgumentException ex) {
						GUI.displayMessage("You must have at least one layer selected.", "Layering Error", gui.getPanelDisplay().getImagePanel(), JOptionPane.ERROR_MESSAGE);

						objChkOriginal.setSelected(true);
					}
				}
			}
		});

		this.objChkMask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (imageCurrentlyObjEditing != null) {
					try {

						imageCurrentlyObjEditing.setDisplaying(objChkOriginal.isSelected(), objChkMask.isSelected(), objChkDots.isSelected(), gui.getPanelDisplay().getSliderSelectedPageAsChannel());

					} catch (IllegalArgumentException ex) {
						GUI.displayMessage("You must have at least one layer selected.", "Layering Error", gui.getPanelDisplay().getImagePanel(), JOptionPane.ERROR_MESSAGE);

						objChkMask.setSelected(true);
					}
				}
			}
		});

		this.objBtnPick.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (objBtnPick.getText().equals("Pick Mult.")) {
					if (imageCurrentlyObjEditing != null) {
						objBtnNext.setEnabled(false);
						objBtnRemove.setText("Cancel");
						objBtnPick.setText("Done");
						objTxtRemove.setEnabled(false);
						imageCurrentlyObjEditing.setCreatingDeletionZone(true);
					}

				} else {
					objBtnNext.setEnabled(true);
					objBtnPick.setText("Pick Mult.");
					objBtnRemove.setText("Remove");
					objTxtRemove.setEnabled(true);
					boolean occurred = imageCurrentlyObjEditing.deleteObjectsWithinDeletionZone(gui.getPanelDisplay().getSliderSelectedPageAsChannel());
					if (!occurred) {
						GUI.displayMessage("No objects were removed because you didn't select at least 3 points for the bounding region.", "Error Removing Points", gui.getPanelDisplay().getImagePanel(), JOptionPane.ERROR_MESSAGE);

					}
				}

			}
		});



		this.objBtnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				nextObjImage(false);

			}
		});

		this.distBtnCancelROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (imageCurrentlyROIEditing != null) {
					imageCurrentlyROIEditing.clearPoints();
					gui.getPanelDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(gui.getPanelDisplay().getSliderSelectedPageAsChannel()), Zoom.ZOOM_100, -1, -1);
				}
			}
		});

		this.distBtnAddROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (imageCurrentlyROIEditing != null) {
					if (imageCurrentlyROIEditing.hasCreatedValidROIPoints()) { 
						distBtnAddROI.setEnabled(false);
						distBtnDeleteROI.setEnabled(false);
						distBtnCancelROI.setEnabled(false);
						distBtnNext.setEnabled(false);
						String input = GUI.getInput("To create the ROI, type its name below and then select the side of the ROI to be designated as POSITIVE:", "Select ROI Name", gui.getPanelDisplay().getImagePanel());
						
						if (input == null || input.equals("")) {

							distBtnAddROI.setEnabled(true);
							distBtnDeleteROI.setEnabled(true);
							distBtnCancelROI.setEnabled(true);
							distBtnNext.setEnabled(true);
						} else {

							if (!imageCurrentlyROIEditing.convertSelectionToRoi(input)) {

								GUI.displayMessage("Error: Either this name is already taken, you didn't select any points, or the line you drew overlapped (or would overlap when extended to the image side) with an existing ROI line.", "ROI naming error.", gui.getComponent(), JOptionPane.ERROR_MESSAGE);

								distBtnAddROI.setEnabled(true);
								distBtnDeleteROI.setEnabled(true);
								distBtnCancelROI.setEnabled(true);
								distBtnNext.setEnabled(true);
							} else {

								imageCurrentlyROIEditing.setSelectingPositive();
							}


							gui.getPanelDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(gui.getPanelDisplay().getSliderSelectedPageAsChannel()), Zoom.ZOOM_100, -1, -1);

						}
					} else {
						GUI.displayMessage("You must select at least 2 points on he image before creating an ROI.<br><br>The ROI line must not overlap with existing lines.", "ROI Creation Error", gui.getPanelDisplay().getImagePanel(), JOptionPane.ERROR_MESSAGE);

					}
				}
			}
		});

		this.distBtnDeleteROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				List<PolarizedPolygonROI> selectedRois = roiListROI.getSelectedMult();
				if (selectedRois != null && !selectedRois.isEmpty() && imageCurrentlyROIEditing != null) {
					for (PolarizedPolygonROI roi : selectedRois) {
						imageCurrentlyROIEditing.removeROI(roi);
						roiListROI.removeItem(roi);
					}
					roiListROI.clearSelection();

					gui.getPanelDisplay().getImagePanel().setImage(imageCurrentlyROIEditing.getPaintedCopy(gui.getPanelDisplay().getSliderSelectedPageAsChannel()), -1, -1, Zoom.ZOOM_100);
				}


			}
		});

		this.distBtnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				distHelpPopup.display(gui.getComponent());

			}
		});

		this.distBtnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				nextROIImage();

			}
		});
	}
	
	/**
	 * @return the swing component for this panel.
	 */
	public JPanel getRawPanel() {
		return this.rawPanel;
	}

	/**
	 * First checks to make sure the first ROI image is valid (has at least one ROI, has valid Tags, has edited
	 * tags if user requires this). If these checks pass, the current ROI selection selection is opened. Then opens the next. 
	 * If it fails, it will skip the image and immediately open the next image. <br><br>
	 * 
	 * All steps after the initial validation are performed asynchronously in a worker thread.
	 * 
	 */
	private synchronized void nextROIImage() { 
		
		distBtnAddROI.setEnabled(false);
		distBtnDeleteROI.setEnabled(false);
		distBtnCancelROI.setEnabled(false);
		distBtnNext.setEnabled(false);
		
		boolean cancelNext = false;
		
		if (imageCurrentlyROIEditing != null) {
			String errorsInTagValidation = null;
			if (!imageCurrentlyROIEditing.hasROIs()) {
				GUI.displayMessage("Error: You must create at least one ROI first.", "Processing Error", gui.getComponent(), JOptionPane.ERROR_MESSAGE);
				cancelNext = true;
			} else if ((errorsInTagValidation = imageCurrentlyROIEditing.validateTags()) != null) {
				GUI.displayMessage("Error: You must apply appropriate tags. Message:<br><br>"+errorsInTagValidation, "Processing Error", gui.getComponent(), JOptionPane.ERROR_MESSAGE);
				cancelNext = true;
			} else if (!imageCurrentlyROIEditing.getSelectionStateMeta().haveTagsBeenEdited()) {

				if (!GUI.confirmWithUser("You have not edited any ROI line tags tags (i.e. setting which is the Reference roi). "+
						"Are you sure you want to continue?", 
						"Confirm Skip Tag Selection", 
						gui.getPanelDisplay().getImagePanel(), JOptionPane.WARNING_MESSAGE)) {

					cancelNext = true;

				}
			}
		}
		
		if (cancelNext) {
			distBtnAddROI.setEnabled(true);
			distBtnDeleteROI.setEnabled(true);
			distBtnCancelROI.setEnabled(true);
			distBtnNext.setEnabled(true);
			return;
		}
		
		currWorker = new Thread(new Runnable() {
			public void run(){

				try {

					if (currWorker == null)
						return;
					
					gui.getBrightnessAdjuster().reset(true);
					gui.getBrightnessAdjuster().removeDisplay();
					setDisplayState(STATE_DISABLED, "Please wait...");
					gui.getPanelDisplay().setDisplayState(false, "Please wait...");
					
					int status = _nextROIImageHelper();
					
					boolean goToNextState = false;
					if (status == 1)
						goToNextState = true;
					else if (status == 2) {

						do {

							status = _nextROIImageHelper();

						} while (status == 2);

						if (status == 1)
							goToNextState = true;

					} else if (status != 3)
						throw new RuntimeException(); // shouldn't occur, a safety measure.
					
					if (currWorker == null)
						return;
					
					if (goToNextState) {

						gui.getWizard().nextState();
						return;

					}
					
					
					ImageContainer ic = imageCurrentlyROIEditing.getContainer();
					RunConfiguration runConfig = ic.getRunConfig();

					gui.getPanelDisplay().setSliceSlider(false, -1, -1);

					Channel roiDrawChan = runConfig.channelMan.getPrimaryROIDrawChan();
					List<Channel> channelsForROISelection = runConfig.channelMan.getOrderedChannels();
					channelsForROISelection.remove(roiDrawChan);
					channelsForROISelection.add(0, roiDrawChan);


					gui.getPanelDisplay().setPageSlider(true, channelsForROISelection, "Chan");

					gui.getPanelDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(roiDrawChan), Zoom.ZOOM_100, -1, -1);

					distTxtCurrDisp.setText(ic.getImageTitle());
					distBtnNext.setEnabled(true);
					distBtnCancelROI.setEnabled(true);
					distBtnAddROI.setEnabled(true);
					distBtnDeleteROI.setEnabled(true);
					distBtnHelp.setEnabled(true);
					roiListROI.clear();
					if (imageCurrentlyROIEditing.hasROIs()) {
						for (PolarizedPolygonROI roi : imageCurrentlyROIEditing.getROIs()) {
							roiListROI.addItem(roi);
						}
					}

					if (!runConfig.channelMan.isProcessChannel(roiDrawChan)) {
						gui.getBrightnessAdjuster().setModifying(imageCurrentlyROIEditing.getContainer(), OutputOption.MaxedChannel, roiDrawChan);
					}
					setDisplayState(STATE_ROI, null);
					
					distBtnAddROI.setEnabled(true);
					distBtnDeleteROI.setEnabled(true);
					distBtnCancelROI.setEnabled(true);
					distBtnNext.setEnabled(true);
					
					gui.getPanelDisplay().setDisplayState(true, null);
					gui.getLogger().setCurrentTaskComplete();
					
					
				} catch (Exception ex) {
					GUI.displayMessage("An unknown error has occurred.<br>" + ex.getMessage() + "<br>", "Display Next Error", gui.getComponent(), JOptionPane.ERROR_MESSAGE);
					ex.printStackTrace();
					distBtnNext.setEnabled(true);
				}
			}
		});
		currWorker.setDaemon(true);
		currWorker.start();

	}

	/**
	 * First selects slices and saves the current pre-processed image, if it exists. Then, opens a preprocessed
	 * image and starts slice selection. However, if <code>reuseLastSettings</code> is selected, then this
	 * method becomes iterative (not recursive). It will continue to process the remainder of images using the 
	 * same settings passed for <code>lastLowSliceSelection</code> and <code>lastHighSliceSelection</code>.
	 * 
	 * When this method is called, it is assumed that all fields in the currently displayed layout are
	 * correct. Therefore, any validation of fields should be done before calling on this method.
	 * 
	 * @param lastLowSliceSelection The minimum slice bound for the current pre-processed image
	 * @param lastHighSliceSelection The maximum slice bound for the current pre-processed image
	 * @param reuseLastSettings true if this method should be iterative, applying the previous parameters to 
	 *  all successful imagess.
	 * @return true if there are more images to be processed, false if finished.
	 * @throws IllegalArgumentException if <code>reuseLastSettings</code>
	 */
	private synchronized boolean nextPreprocessedImage(int lastLowSliceSelection, int lastHighSliceSelection, boolean reuseLastSettings) throws IllegalArgumentException {

		setDisplayState(STATE_DISABLED, "Please wait...");
		this.gui.getPanelDisplay().setDisplayState(false, "Please wait...");

		// Since we assume the current image slice selection was already validated, we should only get a value
		// of 1,2, or 4.
		int status = _nextPreprocessedImageHelper(lastLowSliceSelection, lastHighSliceSelection);

		if (status == 1)
			return false;	
		else if (status != 4) {

			if (status != 2)
				throw new RuntimeException(); // shouldn't occur

			do {

				status = _nextPreprocessedImageHelper(-1, -1);

			} while (status == 2);

			if (status == 1)
				return false;
		}

		// Check if batch
		if (reuseLastSettings) {

			boolean breakAndDisplay = false;
			while (!breakAndDisplay) {
				status = _nextPreprocessedImageHelper(lastLowSliceSelection, lastHighSliceSelection);
				if (status == 1)
					return false;
				else if (status == 3) {
					// Slice selection was invalid for this image.
					GUI.displayMessage("The slice selection you chose to apply to all images is out of range for an image. Apply-All has been cancelled.", 
							"Apply-All Error", this.gui.getComponent(), JOptionPane.ERROR_MESSAGE);
					this.sliceChkApplyToAll.setSelected(false);
					breakAndDisplay = true;
				}
				// status should be 2 or 4, just continue to next.
			}
			// This part reached, must mean that slice selection was invalid.
		}

		List<Channel> listToSendSlider = this.imageCurrentlyPreprocessing.getRunConfig().channelMan.getOrderedChannels();
		
		int stackSize = imageCurrentlyPreprocessing.getOrigStackSize(listToSendSlider.get(0));
		this.gui.getPanelDisplay().setSliceSlider(true, 1, stackSize);
		this.gui.getPanelDisplay().setPageSlider(true, listToSendSlider, "Chan");
		this.gui.getPanelDisplay().setImage(imageCurrentlyPreprocessing.getSlice(listToSendSlider.get(0), 1, false), Zoom.ZOOM_100, -1, -1);
		this.sliceTxtDisplaying.setText(imageCurrentlyPreprocessing.getContainer().getImageTitle());
		this.sliceTxtLowSlice.setText("" + 1);
		this.sliceTxtHighSlice.setText("" + stackSize);

		setDisplayState(STATE_SLICE, null);
		this.gui.getPanelDisplay().setDisplayState(true, null);
		return true;
	}

	/**
	 * This is a helper method.
	 * 
	 * Does batch opening and slice selection of pre-processed image. Occurs when the Apply to all option is
	 * selected upon slice selection of the first image.
	 * 
	 * @param lastLowSliceSelection The minimum slice bound for the current pre-processed image
	 * @param lastHighSliceSelection The maximum slice bound for the current pre-processed image
	 * @return integer corresponding to the state. 1 = there are no more images to pre-process. 2 = the next
	 *  image could not be opened for some reason (user is informed of this). 3 = last slice selection was invalid.
	 *  Leaves last image set as current. 4 = successfully picked next, which is now set as the current.
	 */
	private synchronized int _nextPreprocessedImageHelper(int lastLowSliceSelection, int lastHighSliceSelection) {

		if (this.imageCurrentlyPreprocessing != null && lastLowSliceSelection != -1 && lastHighSliceSelection != -1) {
			this.gui.getLogger().setCurrentTask("Selecting slices & saving state...");

			try {
				this.imageCurrentlyPreprocessing.setSliceRegion(lastLowSliceSelection, lastHighSliceSelection);
			} catch (IllegalArgumentException e) {
				this.gui.getLogger().setCurrentTask("Error: Slice selection out of range.");
				return 3;
			}

			File serializeFile = this.imageCurrentlyPreprocessing.getContainer().getSerializeFile(ImageContainer.STATE_SLC);
			PreprocessedEditableImage.savePreprocessedImage(this.imageCurrentlyPreprocessing, serializeFile);
			this.imagesForObjectAnalysis.add(serializeFile);

			this.gui.getLogger().setCurrentTaskComplete();
			this.imageCurrentlyPreprocessing = null;
		}


		if (this.imagesForSliceSelection.isEmpty())
			return 1;

		ImagePhantom pi = this.imagesForSliceSelection.remove(0);
		this.gui.getLogger().setCurrentTask("Opening " + pi.getTitle() + "...");
		String errors = pi.openOriginal(GUI.settings.outputLocation, GUI.dateString);

		if (errors != null) {
			GUI.displayMessage("There was an error opening file " + pi.getTitle() + ":<br><br>" + errors, "File Open Error", gui.getComponent(), JOptionPane.ERROR_MESSAGE);
			this.gui.getLogger().setCurrentTaskCompleteWithError();
			this.imageCurrentlyPreprocessing = null;
			return 2; // return true because we want to continue processing other images.
		} else {
			this.gui.getLogger().setCurrentTaskComplete();
		}
		

		this.imageCurrentlyPreprocessing = new PreprocessedEditableImage(pi.getIC(), this.gui);

		return 4;

	}

	/**
	 * First saves the current object selection. Then opens the next. If it fails, it will skip the image and
	 * immediately open the next image.
	 * 
	 * @param returnFromPostObjProcessing true if this method is being called because the TRON machine is
	 * returning from post-object processing steps.
	 * 
	 */
	private synchronized void nextObjImage(final boolean returnFromPostObjProcessing) {
		

		// TODO: When done with post-processing, check if should keep (i.e. OutputOption selected in preferences
		// or save resources from post-processing is selected) or remove the object stack. If not remove, make
		// sure to apply the appropriate LUT to it.
		
		objBtnNext.setEnabled(false);
		objTxtRemove.setEnabled(false);
		objBtnRemove.setEnabled(false);
		objChkDots.setEnabled(false);
		objChkOriginal.setEnabled(false);
		objChkMask.setEnabled(false);
		
		boolean cancelNext = false;
		
		if (this.imageCurrentlyObjEditing != null) {
			Channel chan = imageCurrentlyObjEditing.getSelectionStateMeta().getChannelNotLookedAt();
			if (chan != null) {

				if (!GUI.confirmWithUser("You have not yet looked at the "+ chan.getName() + 
						" channel yet. Are you sure you want to continue?", 
						"Confirm Skip Channel", 
						gui.getPanelDisplay().getImagePanel(), JOptionPane.WARNING_MESSAGE)) {
					cancelNext = true;

				}
			}
			
			
			
		}
		
		if (cancelNext) {
			objBtnNext.setEnabled(true);
			objTxtRemove.setEnabled(true);
			objBtnRemove.setEnabled(true);
			objChkDots.setEnabled(true);
			objChkOriginal.setEnabled(true);
			objChkMask.setEnabled(true);
			return;
			
		}
		

		
		currWorker = new Thread(new Runnable() {
			public void run(){
				try {
					
					if (currWorker == null)
						return;
					
					setDisplayState(STATE_DISABLED, "Please wait...");
					gui.getPanelDisplay().setDisplayState(false, "Pleast wait...");
					
					if (imageCurrentlyObjEditing != null) {
						
						if (!returnFromPostObjProcessing && GUI.settings.processingPostObj) {
							
							if (imageCurrentlyObjEditing.checkPostObjectImages()) {
								_nextPostObjImageHelper();
							}
						}
						
						if (GUI.settings.processingPostObjDelete) {
							imageCurrentlyObjEditing.getContainer().removeImageFromIC(OutputOption.Channel, null);
							imageCurrentlyObjEditing.getContainer().removeImageFromIC(OutputOption.ProcessedObjectsStack, null);
						}
						
					}
					
					
					
					int status = _nextObjImageHelper();
					
					boolean goToNextState = false;
					if (status == 1)
						goToNextState = true; // no more images
					else if (status == 2) {

						do {

							status = _nextObjImageHelper();

						} while (status == 2);

						if (status == 1) {
							goToNextState = true;
						}
					} else if (status != 3)
						throw new RuntimeException(); // shouldn't occur, a safety measure.
					
					if (currWorker == null)
						return;
					
					if (goToNextState) {
						System.out.println("Go to next");
						gui.getWizard().nextState(imagesForROICreation);
						return;

					}
					
					ImageContainer ic = imageCurrentlyObjEditing.getContainer();

					gui.getPanelDisplay().setSliceSlider(false, -1, -1);
					List<Channel> chans = new ArrayList<Channel>();
					int processedInsertIndex = 0;
					for (Channel chan : ic.getRunConfig().channelMan.getOrderedChannels()) {

						if (imageCurrentlyObjEditing.getRunConfig().channelMan.isProcessChannel(chan)) {
							chans.add(processedInsertIndex, chan); // Add to beginning
							processedInsertIndex++;
						} else {
							chans.add(chan); // Add to end
						}
					}

					gui.getPanelDisplay().setPageSlider(true, chans, "Chan");
					gui.getPanelDisplay().setImage(imageCurrentlyObjEditing.getImgWithDots(chans.get(0)).getBufferedImage(), Zoom.ZOOM_100, -1, -1);
					objTxtCurrDisp.setText(ic.getImageTitle());
					objTxtRemove.setText("");
					objTxtRemove.setEnabled(true);
					objBtnNext.setEnabled(true);
					objBtnRemove.setEnabled(true);
					objChkDots.setEnabled(true);
					objChkDots.setSelected(true);
					objChkOriginal.setEnabled(true);
					objChkOriginal.setSelected(true);
					objChkMask.setEnabled(true);
					objChkMask.setSelected(true);


					imageCurrentlyObjEditing.getSelectionStateMeta().lookAt(gui.getPanelDisplay().getSliderSelectedPageAsChannel());
					setDisplayState(STATE_OBJ, null);
					gui.getPanelDisplay().setDisplayState(true, null);
					gui.getLogger().setCurrentTaskComplete();

				} catch (Exception ex) {
					ex.printStackTrace();
					objBtnNext.setEnabled(true);
				}
			}
		});
		currWorker.setDaemon(true);
		currWorker.start();
		

	}
	
	/**
	 * This is a helper method. <br><br>
	 * 
	 * Performs post-object processing on an object. This assumes all the requirements for post-object
	 * processing are met and have been verified. This also requires that the image for post-object processing
	 * is already set as the current image currently object editing.
	 */
	private synchronized void _nextPostObjImageHelper() {
		this.gui.getLogger().setCurrentTask("Beginning post-object processing...");
		
		
		// TODO: Create the required images and set them.
		pstObBtnNext.setEnabled(true);
		pstObTxtCurrDisp.setText(imageCurrentlyObjEditing.getContainer().getImageTitle());
		setDisplayState(STATE_POST_OBJ, null);
		gui.getPanelDisplay().setDisplayState(true, null);
		this.gui.getLogger().setCurrentTaskComplete();
	}
	
	/**
	 * This is a helper method. <br><br>
	 * 
	 * Saves the current object image and then opens the next one, storing in {@link #imageCurrentlyObjEditing}.
	 * Returns an integer corresponding to the result of these operations.
	 * 
	 * @return 1 = there are no more images to process. 2 = the next image could not be opened for some reason 
	 * (user is informed of this). 3 = successfully picked next, which is now set as the current.
	 */
	private synchronized int _nextObjImageHelper() {
		
		if (this.imageCurrentlyObjEditing != null) {

			this.gui.getLogger().setCurrentTask("Creating new images...");

			this.imageCurrentlyObjEditing.createAndAddNewImagesToIC();
			this.gui.getLogger().setCurrentTaskComplete();

			this.gui.getLogger().setCurrentTask("Saving state...");

			// convert
			ROIEditableImage roiei = this.imageCurrentlyObjEditing.convertToROIEditableImage();
			// delete obj if not serve ints
			imageCurrentlyObjEditing.getContainer().getSerializeFile(ImageContainer.STATE_OBJ);
			imageCurrentlyObjEditing.deleteSerializedVersion();
			if (GUI.settings.saveIntermediates) {
				ObjectEditableImage.saveObjEditableImage(this.imageCurrentlyObjEditing, this.imageCurrentlyObjEditing.getContainer().getSerializeFile(ImageContainer.STATE_OBJ));
			}

			// save roi
			File serializeFile = roiei.getContainer().getSerializeFile(ImageContainer.STATE_ROI);
			ROIEditableImage.saveROIEditableImage(roiei, serializeFile);
			this.imagesForROICreation.add(serializeFile);
			this.gui.getLogger().setCurrentTaskComplete();

			this.imageCurrentlyObjEditing = null;
		}


		if (this.imagesForObjectSelection.isEmpty())
			return 1;

		this.gui.getLogger().setCurrentTask("Opening image...");

		this.imageCurrentlyObjEditing = ObjectEditableImage.loadObjEditableImage(this.imagesForObjectSelection.remove(0));

		if (imageCurrentlyObjEditing == null) {
			this.gui.getLogger().setCurrentTaskCompleteWithError();
			GUI.displayMessage("There was an error opening saved object state.", "File Open Error", null, JOptionPane.ERROR_MESSAGE);

			return 2; // returning true allows it to just skip to the next image.
		} else if (!imageCurrentlyObjEditing.getRunConfig().channelMan.hasIdenticalChannels(GUI.settings.channelMan)) {
			this.gui.getLogger().setCurrentTaskCompleteWithError();
			GUI.displayMessage("The saved Object state couldn't be opened because the Channel Setup does not match the current Channel Setup "
					+ "in the Preferences. To fix this, use the Templates option in the Preferences to load the Channel Setup from a run.", "File Open Error", null, JOptionPane.ERROR_MESSAGE);
			imageCurrentlyObjEditing = null;
			return 2;
		} else {
			this.gui.getLogger().setCurrentTaskComplete();
		}

		return 3;

	}
	

	/**
	 * This is a helper method. <br><br>
	 * 
	 * Saves the current ROI image and then opens the next one, storing in {@link #imageCurrentlyROIEditing}.
	 * Returns an integer corresponding to the result of these operations.
	 * 
	 * @return 1 = there are no more images to process. 2 = the next image could not be opened for some reason 
	 * (user is informed of this). 3 = successfully picked next, which is now set as the current.
	 */
	private synchronized int _nextROIImageHelper() {

		if (this.imageCurrentlyROIEditing != null) {

			this.gui.getLogger().setCurrentTask("Saving state...");

			imageCurrentlyROIEditing.deleteSerializedVersion();

			ROIEditableImage.saveROIEditableImage(imageCurrentlyROIEditing, imageCurrentlyROIEditing.getContainer().getSerializeFile(ImageContainer.STATE_ROI));

			this.gui.getLogger().setCurrentTaskComplete();
			this.imagesForROIAnalysis.add(imageCurrentlyROIEditing.getContainer().getSerializeFile(ImageContainer.STATE_ROI));

			this.imageCurrentlyROIEditing = null;
		}


		if (this.imagesForROICreation.isEmpty()) {
			return 1;

		}

		this.gui.getLogger().setCurrentTask("Opening image...");

		this.imageCurrentlyROIEditing = ROIEditableImage.loadROIEditableImage(this.imagesForROICreation.remove(0));
		if (imageCurrentlyROIEditing == null) {
			GUI.displayMessage("There was an error opening saved ROI state.", "File Open Error", null, JOptionPane.ERROR_MESSAGE);
			this.gui.getLogger().setCurrentTaskCompleteWithError();
			return 2;
		} else if (!imageCurrentlyROIEditing.getRunConfig().channelMan.hasIdenticalChannels(GUI.settings.channelMan)) {
			GUI.displayMessage("The saved ROI state couldn't be opened because the Channel Setup does not match the current Channel Setup "
					+ "in the Preferences. To fix this, use the Templates option in the Preferences to load the Channel Setup from a run.", "File Open Error", null, JOptionPane.ERROR_MESSAGE);
			GUI.displayMessage("There was an error opening saved ROI state.", "File Open Error", null, JOptionPane.ERROR_MESSAGE);
			imageCurrentlyROIEditing = null;
			return 2;
		} else {
			this.gui.getLogger().setCurrentTaskComplete();
		}
		
		return 3;
		
	}
	
	/**
	 * Sets the images which will be displayed for slice selection. This does NOT begin slice selection.
	 * To do that, use {@link #startSliceSelecting()}
	 * 
	 * @param images the phantom images from which we can derive and ImagePlus.
	 */
	public synchronized void setImagesForSliceSelection(List<ImagePhantom> images) {
		this.imagesForSliceSelection = images;
	}
	
	/**
	 * Begins slice selection
	 */
	public synchronized void startSliceSelecting() {
		if (this.gui.getWizard().getStatus() != Status.SELECT_FILES)
			nextPreprocessedImage(-1,-1, false);
	}

	/**
	 * Begins processing image object by creating a worker.
	 */
	public synchronized void startProcessingImageObjects() {
		
		if (this.imagesForObjectAnalysis.isEmpty()) {
			this.gui.getWizard().cancel();
			return;
		}
		if (this.gui.getWizard().getStatus() != Status.SELECT_FILES) {
			this.neuronProcessor = new NeuronProcessor(new ArrayList<File>(this.imagesForObjectAnalysis), this.gui.getLogger(),this.gui.getWizard());
			this.imagesForObjectAnalysis.clear();
			this.neuronProcessor.run();
		}

	}

	/**
	 * Starts processing ORIS by creating a worker.
	 */
	public synchronized void startAnalyzingROIs() {
		
		if (this.imagesForROIAnalysis.isEmpty()) {
			this.gui.getWizard().cancel();
			return;
		}
		if (this.gui.getWizard().getStatus() != Status.SELECT_FILES) {
			this.roiProcessor = new RoiProcessor(new ArrayList<File>(this.imagesForROIAnalysis), this.gui.getLogger(),this.gui.getWizard());
			this.imagesForROIAnalysis.clear();
			this.roiProcessor.run();
		}

	}
	
	/**
	 * Starts image object selection by user.
	 * 
	 * @param files image serialization files designating images for object selection
	 * @return true if there were images to start processing
	 */
	public synchronized boolean startImageObjectSelecting(List<File> files) {
		
		if (files.isEmpty()) {
			return false;
		}
		if (this.gui.getWizard().getStatus() != Status.SELECT_FILES) {
			this.imagesForObjectSelection = files;
			nextObjImage(false);
		}
		return true;

	}
	
	/**
	 * Starts ROI selection by user.
	 * @param images the serialization files (.ser) which designate serialized ObjectEditableImages
	 * @return true if there were images to start processing
	 */
	public synchronized boolean startImageROISelecting(List<File> images) {
		if (images.isEmpty()) {
			System.out.println("Decision to cancel");
			return false;
		}
		if (this.gui.getWizard().getStatus() != Status.SELECT_FILES) {
			this.imagesForROICreation = images;
			nextROIImage();

		}
		return true;

	}
	
	/**
	 * The current display state.
	 * 
	 * @return One of {@link #STATE_DISABLED}, {@link #STATE_OBJ}, {@link #STATE_ROI}, or {@link #STATE_SLICE}
	 */
	public int getState() {
		return this.currentState;
	}
	
	/**
	 * Sets the current display state.
	 * 
	 * @param state One of {@link #STATE_DISABLED}, {@link #STATE_OBJ}, {@link #STATE_ROI}, or {@link #STATE_SLICE}
	 * @param disabledMsg The disabled message to be put over the clear panel (only when in a disabled state)
	 */
	public synchronized void setDisplayState(int state, String disabledMsg) {

		this.currentState = state;
		switch (state) {
		case STATE_DISABLED:
			if (disabledMsg != null) 
				this.lblDisabled.setText(disabledMsg);
			else
				this.lblDisabled.setText("<html><body><p style='width: 100px; text-align: center;'>Please select images using the interface above.</p></body></html>");
			setLayout(false, -1);
			this.objBtnPick.setText("Pick Mult.");
			this.objBtnRemove.setText("Remove");
			this.sliceLblERR.setVisible(false);
			this.sliceTxtDisplaying.setText("");
			this.sliceTxtHighSlice.setText("");
			this.sliceTxtLowSlice.setText("");
			break;
		case STATE_SLICE:
			this.sliceLblERR.setVisible(false);
			setLayout(true, LAYOUT_TYPE_SLICE);
			break;
		case STATE_OBJ:
			setLayout(true, LAYOUT_TYPE_OBJ);
			break;
		case STATE_POST_OBJ:
			setLayout(true, LAYOUT_TYPE_POST_OBJ);
			break;
		case STATE_ROI:
			setLayout(true, LAYOUT_TYPE_ROI);
			break;
		default:
			throw new IllegalStateException();
		}

	}

	private void setLayout(boolean enabled, int type) {

		this.rawPanel.removeAll();

		if (enabled) {
			this.rawPanel.setBackground(GUI.colorPnlEnabled);

			switch (type) {
			case LAYOUT_TYPE_SLICE:
				GroupLayout gl_pnlInfo = new GroupLayout(this.rawPanel);
				gl_pnlInfo.setHorizontalGroup(
						gl_pnlInfo.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlInfo.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_pnlInfo.createParallelGroup(Alignment.TRAILING)
										.addGroup(gl_pnlInfo.createSequentialGroup()
												.addComponent(sliceLblCurrDisp)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(sliceTxtDisplaying, GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE))
										.addGroup(Alignment.LEADING, gl_pnlInfo.createSequentialGroup()
												.addComponent(sliceLblSelectSlices)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(sliceTxtLowSlice, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(sliceLblThru)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(sliceTxtHighSlice, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE))
										.addGroup(Alignment.LEADING, gl_pnlInfo.createSequentialGroup()
												.addComponent(sliceChkApplyToAll))
										.addGroup(gl_pnlInfo.createSequentialGroup()
												.addComponent(sliceLblERR, GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(sliceBtnNext)))
								.addContainerGap())
						);
				gl_pnlInfo.setVerticalGroup(
						gl_pnlInfo.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlInfo.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
										.addComponent(sliceTxtDisplaying, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(sliceLblCurrDisp))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
										.addComponent(sliceLblSelectSlices)
										.addComponent(sliceTxtLowSlice, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(sliceLblThru)
										.addComponent(sliceTxtHighSlice, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addComponent(sliceChkApplyToAll)
								.addPreferredGap(ComponentPlacement.RELATED, /*24*/24, Short.MAX_VALUE)
								.addGroup(gl_pnlInfo.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(sliceLblERR, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(sliceBtnNext, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addContainerGap())
						);
				this.rawPanel.setLayout(gl_pnlInfo);
				break;
			case LAYOUT_TYPE_OBJ:

				GroupLayout gl_objPnl = new GroupLayout(this.rawPanel);
				gl_objPnl.setHorizontalGroup(
						gl_objPnl.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_objPnl.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_objPnl.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_objPnl.createSequentialGroup()
												.addComponent(objLblCurrDisp)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(objTxtCurrDisp, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
										.addGroup(gl_objPnl.createSequentialGroup()
												.addComponent(objLblInstructions)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(objBtnHelp, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE))
										.addGroup(gl_objPnl.createSequentialGroup()
												.addComponent(objLblRemove, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(objTxtRemove, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(objBtnRemove, GroupLayout.PREFERRED_SIZE, /*75*/60, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(objBtnPick, GroupLayout.PREFERRED_SIZE, /*65*/84, GroupLayout.PREFERRED_SIZE))
										.addGroup(Alignment.TRAILING,  gl_objPnl.createSequentialGroup()
												.addComponent(objLblShow, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)	
												.addComponent(objChkMask, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)		
												.addComponent(objChkOriginal, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)		
												.addComponent(objChkDots, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(objBtnNext))
										/*.addComponent(objBtnNext, Alignment.TRAILING)*/
										)

								.addContainerGap())

						);
				gl_objPnl.setVerticalGroup(
						gl_objPnl.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_objPnl.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_objPnl.createParallelGroup(Alignment.BASELINE)
										.addComponent(objTxtCurrDisp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(objLblCurrDisp))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_objPnl.createParallelGroup(Alignment.LEADING)
										.addComponent(objLblInstructions)
										.addComponent(objBtnHelp, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.UNRELATED)
								.addGroup(gl_objPnl.createParallelGroup(Alignment.BASELINE)
										.addComponent(objLblRemove)
										.addComponent(objTxtRemove, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(objBtnRemove).addComponent(objBtnPick))
								.addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
								//
								.addGroup(gl_objPnl.createParallelGroup(Alignment.BASELINE)
										.addComponent(objLblShow)

										//.addGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										//.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)

										.addComponent(objChkMask)
										.addComponent(objChkOriginal)
										.addComponent(objChkDots)
										.addComponent(objBtnNext))
								/*.addComponent(objBtnNext)*/
								.addContainerGap())
						);
				this.rawPanel.setLayout(gl_objPnl);
				break;
			case LAYOUT_TYPE_POST_OBJ:
				GroupLayout gl_pnlPostObj = new GroupLayout(this.rawPanel);
				gl_pnlPostObj.setHorizontalGroup(
						gl_pnlPostObj.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlPostObj.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_pnlPostObj.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_pnlPostObj.createSequentialGroup()
												.addComponent(pstObLblCurrDisp)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(pstObTxtCurrDisp, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
										.addGroup(gl_pnlPostObj.createSequentialGroup()
												.addComponent(pstObLblCurrView)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(pstObTxtCurrView, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
										.addGroup(gl_pnlPostObj.createSequentialGroup()
												.addComponent(pstObLblInstructions)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(pstObBtnHelp, GroupLayout.PREFERRED_SIZE, 17, GroupLayout.PREFERRED_SIZE))
										/*.addGroup(gl_pnlDist.createSequentialGroup()
												.addGroup(gl_pnlDist.createParallelGroup(Alignment.TRAILING, false)
														.addComponent(distBtnAddROI, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
														.addGroup(Alignment.LEADING, gl_pnlDist.createParallelGroup(Alignment.TRAILING, false)
																.addComponent(distBtnDeleteROI, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
																.addComponent(distBtnCancelROI, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 43, Short.MAX_VALUE)))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(roiSP, GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(distBtnNext))*/)
								.addContainerGap())
						);
				gl_pnlPostObj.setVerticalGroup(
						gl_pnlPostObj.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlPostObj.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_pnlPostObj.createParallelGroup(Alignment.BASELINE)
										.addComponent(pstObTxtCurrDisp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(pstObLblCurrDisp))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pnlPostObj.createParallelGroup(Alignment.LEADING)  //TODO changed from BASELINE, may need to be BASELINE
										.addComponent(pstObTxtCurrView, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(pstObLblCurrView))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pnlPostObj.createParallelGroup(Alignment.LEADING, false)
										.addComponent(distBtnHelp, 0, 0, Short.MAX_VALUE)
										.addComponent(roiLblInstruction, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								/*.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pnlDist.createParallelGroup(Alignment.LEADING, false)
										.addComponent(roiSP, 0, 0, Short.MAX_VALUE)
										.addGroup(gl_pnlDist.createParallelGroup(Alignment.TRAILING)
												.addGroup(gl_pnlDist.createSequentialGroup()
														.addComponent(distBtnAddROI, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(distBtnDeleteROI, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(distBtnCancelROI, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
												.addComponent(distBtnNext)))*/
								.addContainerGap(74, Short.MAX_VALUE))
						);
				this.rawPanel.setLayout(gl_pnlPostObj);
				break;
			case LAYOUT_TYPE_ROI:
				GroupLayout gl_pnlDist = new GroupLayout(this.rawPanel);
				gl_pnlDist.setHorizontalGroup(
						gl_pnlDist.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlDist.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_pnlDist.createParallelGroup(Alignment.LEADING)
										.addGroup(gl_pnlDist.createSequentialGroup()
												.addComponent(roiLblCurrDisp)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(distTxtCurrDisp, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
										.addGroup(gl_pnlDist.createSequentialGroup()
												.addComponent(roiLblInstruction)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(distBtnHelp, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
										.addGroup(gl_pnlDist.createSequentialGroup()
												.addGroup(gl_pnlDist.createParallelGroup(Alignment.TRAILING, false)
														.addComponent(distBtnAddROI, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
														.addGroup(Alignment.LEADING, gl_pnlDist.createParallelGroup(Alignment.TRAILING, false)
																.addComponent(distBtnDeleteROI, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
																.addComponent(distBtnCancelROI, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 43, Short.MAX_VALUE)))
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(roiSP, GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(distBtnNext)))
								.addContainerGap())
						);
				gl_pnlDist.setVerticalGroup(
						gl_pnlDist.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlDist.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_pnlDist.createParallelGroup(Alignment.BASELINE)
										.addComponent(distTxtCurrDisp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(roiLblCurrDisp))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pnlDist.createParallelGroup(Alignment.LEADING, false)
										.addComponent(distBtnHelp, 0, 0, Short.MAX_VALUE)
										.addComponent(roiLblInstruction, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pnlDist.createParallelGroup(Alignment.LEADING, false)
										.addComponent(roiSP, 0, 0, Short.MAX_VALUE)
										.addGroup(gl_pnlDist.createParallelGroup(Alignment.TRAILING)
												.addGroup(gl_pnlDist.createSequentialGroup()
														.addComponent(distBtnAddROI, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(distBtnDeleteROI, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE)
														.addPreferredGap(ComponentPlacement.RELATED)
														.addComponent(distBtnCancelROI, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
												.addComponent(distBtnNext)))
								.addContainerGap(74, Short.MAX_VALUE))
						);
				this.rawPanel.setLayout(gl_pnlDist);
				break;
			default:
				throw new IllegalArgumentException();
			}

		} else {
			this.rawPanel.removeAll();
			this.rawPanel.setBackground(GUI.colorPnlDisabled);
			this.rawPanel.setLayout(new BorderLayout(0,0));
			this.rawPanel.add(lblDisabled, BorderLayout.CENTER);
		}

	}
	
	@Override
	public synchronized void sliderSliceChanged(int slice) {
		if(this.imageCurrentlyPreprocessing != null)
		{
			this.gui.getPanelDisplay().setImage(this.imageCurrentlyPreprocessing.getSlice(gui.getPanelDisplay().getSliderSelectedPageAsChannel(), slice, false), Zoom.ZOOM_100, -1, -1);
		}
	}

	@Override
	public synchronized void sliderPageChanged(PnlDisplayPage displayPage) {
		if (currentState == STATE_SLICE) {
			if(this.imageCurrentlyPreprocessing != null)
			{
				this.gui.getPanelDisplay().setImage(this.imageCurrentlyPreprocessing.getSlice((Channel) displayPage, gui.getPanelDisplay().getSliderSelectedSlice(), false).getBufferedImage(), Zoom.ZOOM_100, -1, -1);
			}

		} else if (currentState == STATE_OBJ) {
			if(imageCurrentlyObjEditing != null)
			{
				Channel chan = (Channel) displayPage;
				this.imageCurrentlyObjEditing.getSelectionStateMeta().lookAt(chan);
				imageCurrentlyObjEditing.setZoom(Zoom.ZOOM_100);
				if (imageCurrentlyObjEditing.getRunConfig().channelMan.isProcessChannel(chan)) {
					this.gui.getPanelDisplay().setImage(imageCurrentlyObjEditing.getImgWithDots(chan).getBufferedImage(), Zoom.ZOOM_100, -1, -1);
					this.objBtnPick.setEnabled(true);
					this.objBtnRemove.setEnabled(true);
					this.objChkDots.setEnabled(true);
					this.objChkMask.setEnabled(true);
					this.objChkOriginal.setEnabled(true);
				} else {
					this.objBtnPick.setEnabled(false);
					this.objBtnRemove.setEnabled(false);
					this.objChkDots.setEnabled(false);
					this.objChkMask.setEnabled(false);
					this.objChkOriginal.setEnabled(false);
					this.gui.getPanelDisplay().setImage(imageCurrentlyObjEditing.getContainer().getImage(OutputOption.MaxedChannel, chan, false), Zoom.ZOOM_100, -1, -1);
				}
			}
		} else if (currentState == STATE_ROI) {
			if (imageCurrentlyROIEditing != null) {
				Channel chan = (Channel) displayPage;

				if (!imageCurrentlyROIEditing.getRunConfig().channelMan.isProcessChannel(chan)) {
					this.gui.getBrightnessAdjuster().setModifying(imageCurrentlyROIEditing.getContainer(), OutputOption.MaxedChannel, chan);
					this.gui.getPanelDisplay().setImage(this.imageCurrentlyROIEditing.getPaintedCopy(chan), Zoom.ZOOM_100, -1, -1);
				} else {
					this.gui.getBrightnessAdjuster().setModifying(null, null, null);
					this.gui.getPanelDisplay().setImage(this.imageCurrentlyROIEditing.getPaintedCopy(chan), Zoom.ZOOM_100, -1, -1);
				}
			}
		}
	}
	
	/**
	 * Cancels neuron processing by resetting all fields and canceling any workers. Not synchronized, meaning
	 * this will regardless of what is being done elsewhere in the panel.
	 */
	public void cancelNeuronProcessing() {
		
		if (this.currWorker != null) {
			this.currWorker = null; // should stop the thread, because these are always made daemon
		}
		
		if (this.neuronProcessor != null) {
			this.neuronProcessor.cancelProcessing();
			this.neuronProcessor = null;
		}
		if (this.roiProcessor != null) {
			this.roiProcessor.cancelProcessing();
			this.roiProcessor = null;
		}
		this.imageCurrentlyPreprocessing = null;
		this.imageCurrentlyObjEditing = null;
		this.imageCurrentlyROIEditing = null;
		this.imagesForObjectAnalysis = new LinkedList<File>();
		this.imagesForObjectSelection = new LinkedList<File>();
		this.imagesForROIAnalysis = new LinkedList<File>();
		this.imagesForROICreation = new LinkedList<File>();
		this.imagesForSliceSelection = new LinkedList<ImagePhantom>();

		this.sliceChkApplyToAll.setSelected(false);

		this.sliceLblERR.setVisible(false);
	}

	@Override
	public synchronized void mouseClickOnImage(Point p) {

		if (currentState == STATE_OBJ) {

			if (p != null && imageCurrentlyObjEditing != null && imageCurrentlyObjEditing.getRunConfig().channelMan.isProcessChannel(gui.getPanelDisplay().getSliderSelectedPageAsChannel())) {
				if (this.imageCurrentlyObjEditing.isCreatingDeletionZone()) {
					imageCurrentlyObjEditing.addDeletionZonePoint(p, gui.getPanelDisplay().getSliderSelectedPageAsChannel());
				} else {
					imageCurrentlyObjEditing.addPoint(gui.getPanelDisplay().getSliderSelectedPageAsChannel(), p);
				}

			}
		} else if (currentState == STATE_ROI) {

			if (p != null && imageCurrentlyROIEditing != null) {
				if (imageCurrentlyROIEditing.isSelectingPositiveRegion()) {
					this.distBtnAddROI.setEnabled(true);
					this.distBtnNext.setEnabled(true);
					this.distBtnCancelROI.setEnabled(true);
					this.distBtnDeleteROI.setEnabled(true);
					this.roiListROI.addItem(imageCurrentlyROIEditing.selectPositiveRegionForCurrentROI(p));
				} else {

					imageCurrentlyROIEditing.addPoint(p);
				}
				gui.getPanelDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(gui.getPanelDisplay().getSliderSelectedPageAsChannel()), Zoom.ZOOM_100, -1, -1);
			}

		}		
	}
	
	@Override
	public synchronized void updateImage(int min, int max) {
		if (this.imageCurrentlyROIEditing == null) {
			return;
		}
		Channel chan = gui.getPanelDisplay().getSliderSelectedPageAsChannel();
		this.imageCurrentlyROIEditing.applyMinMax(chan, min, max);
		gui.getPanelDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(chan), Zoom.ZOOM_100, -1, -1);
	}

	/**
	 * @return image currently object editing, will be null if not in the object edit phase.
	 */
	public ObjectEditableImage getObjectEditableImage() {
		return this.imageCurrentlyObjEditing;
	}

	/**
	 * Does same thing as manually clicking add ROI button. Useful so that keystrokes can trigger the add
	 * ROI button, rather than needing to manually press it.
	 */
	public synchronized void triggerROIAddButton() {
		if (this.distBtnAddROI.isEnabled()) {
			this.distBtnAddROI.doClick();
		}
	}


	/**
	 * Renderer which handles the display of ROI objects in the list of ROIs in the panel during ROI selection
	 * 
	 * @author justincarrington
	 * 
	 * @param <K> the type for the renderer
	 */
	private static class PolygonROIRenderer<K> implements ListCellRenderer<K> {

		private static Font smallFont = new Font("PingFang TC", Font.BOLD, 12);
		protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
		private PnlOptions pnlOp;

		private PolygonROIRenderer(PnlOptions op) {
			this.pnlOp = op;
		}

		@SuppressWarnings("rawtypes")
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {


			JLabel renderer = (JLabel) defaultRenderer
					.getListCellRendererComponent(list, value, index, isSelected,
							cellHasFocus);
			if (value instanceof PolarizedPolygonROI) {
				PolarizedPolygonROI roi = (PolarizedPolygonROI) value;
				if (this.pnlOp.imageCurrentlyROIEditing != null) {
					renderer.setFont(smallFont);
					String tags = this.pnlOp.imageCurrentlyROIEditing.getTags(roi, true);
					if (tags != null) {
						renderer.setText("<html>"+roi.getName()+" ("+tags+")</html>");
					} else {
						renderer.setText("<html>"+roi.getName()+"</html>");

					}
					renderer.setBorder(new EmptyBorder(0,0,0,0));
				} else {
					renderer.setText(value.toString());
				}
			} else if (value != null) {
			}

			return renderer;


		}

	}

}
