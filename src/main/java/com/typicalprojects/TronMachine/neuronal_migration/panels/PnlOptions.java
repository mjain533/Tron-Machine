/*
 * (C) Copyright 2018 Justin Carrington.
 *
 *  This file is part of TronMachine.
 *
 *  TronMachine is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TronMachine is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
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

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

import com.typicalprojects.TronMachine.ui.ModernCardPanel;
import com.typicalprojects.TronMachine.util.ImageContainer;
import com.typicalprojects.TronMachine.util.ImagePhantom;
import com.typicalprojects.TronMachine.util.Point;
import com.typicalprojects.TronMachine.util.PolarizedPolygonROI;
import com.typicalprojects.TronMachine.util.SimpleJList;
import com.typicalprojects.TronMachine.util.Zoom;

import ij.ImagePlus;
import ij.ImageStack;
import com.typicalprojects.TronMachine.neuronal_migration.Wizard.Status;
import com.typicalprojects.TronMachine.neuronal_migration.processing.ObjectEditableImage.PostProcessImage;
import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.OutputOption;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager.Channel;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlDisplay.PnlDisplayFeedbackReceiver;
import com.typicalprojects.TronMachine.neuronal_migration.panels.PnlDisplay.PnlDisplayPage;
import com.typicalprojects.TronMachine.popup.BrightnessAdjuster.BrightnessChangeReceiver;
import com.typicalprojects.TronMachine.neuronal_migration.processing.PreprocessedEditableImage;
import com.typicalprojects.TronMachine.neuronal_migration.processing.ObjectEditableImage;
import com.typicalprojects.TronMachine.neuronal_migration.processing.ROIEditableImage;
import com.typicalprojects.TronMachine.neuronal_migration.processing.NeuronProcessor;
import com.typicalprojects.TronMachine.neuronal_migration.processing.RoiProcessor;
import java.io.File;
import java.util.LinkedList;
import com.typicalprojects.TronMachine.neuronal_migration.RunConfiguration;

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
	private volatile ObjectEditableImage imgCurrObjEditing = null;
	private volatile ROIEditableImage imageCurrentlyROIEditing = null;

	private JLabel lblDisabled;



	private JTextField distTxtCurrDisp;
	private JButton distBtnAddROI;
	private JButton distBtnCancelROI;
	private JButton distBtnDeleteROI;
	private JButton distBtnNext;
	private JButton distBtnHelp;
	private String distHelpPopupText;



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
	private String objHelpPopupText;

	private JLabel pstObLblCurrDisp;
	private JLabel pstObLblCurrView;
	private JLabel pstObLblInstructions;
	private JLabel pstObLblOptions;
	private JTextField pstObTxtCurrDisp;
	private JTextField pstObTxtCurrView;
	private JButton pstObBtnNext;
	private JButton pstObBtnHelp;
	private JCheckBox pstObChkDots;
	private JCheckBox pstObChkMax;
	private String pstObHelpPopupText;

	private java.util.List<File> imagesForObjectAnalysis = new LinkedList<File>();
	private java.util.List<File> imagesForObjectSelection = new LinkedList<File>();
	private java.util.List<File> imagesForROICreation = new LinkedList<File>();
	private java.util.List<File> imagesForROIAnalysis = new LinkedList<File>();
	private java.util.List<ImagePhantom> imagesForSliceSelection = new LinkedList<ImagePhantom>();
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

		this.rawPanel =  new ModernCardPanel();
		this.rawPanel.setBorder(new EmptyBorder(16, 16, 16, 16));

		this.gui = gui;

		this.objHelpPopupText = "<h3><em><font color='red'>Removing Objects</font></em></h3>"
				+ "<p>"
				+ "Type object numbers in the text field after 'Rmv:' and click Remove to delete objects. You may enter a single number, a range of numbers (i.e. 1-5), or a combination of both separated by commas and no spaces (i.e. 1-5,7,9,11-15). "
				+ "Points can also be removed by right clicking them on the image itself. You may remove large chunks of objects in a region by clicking 'Pick Mult.' and then drawing a polygon by clicking points on the image. Points within the polygon will be removed. "
				+ "The polygon's start and end points don't need to line up exactly. A line will be drawn between your start and end points to finish the shape, but make sure the start and end points are close. Points in this region will be removed from all processed channels."
				+ "</p>"
				+ "<h3><em><font color='red'>Adding Objects</font></em></h3>"
				+ "<p>"
				+ "To add points, simply (left) click on the image."
				+ "</p>"
				+ "<h3><em><font color='red'>Modifying the View</font></em></h3>"
				+ "<p>"
				+ "You can resize the program window to enlarge the image. If this is insufficient, you may zoom in by hitting the Shift key (will zoom in where your mouse lies, so the mouse must be within the image boundaries). "
				+ "You can navigate while zoomed using A (left), W (up), S (down), D (right), and zoom out using the space bar.<br><br>"
				+ "You may also toggle the image dots (and numbers), object mask, and original image using the checkboxes in the options panel."
				+ "</p>";

		this.distHelpPopupText = "<p>Click on the image to start adding points to the ROI. Then, click 'Add' in the "
				+ "panel below the image to add the region. You can delete this region via the 'Delete' button.<br><br>"
				+ "To RESET the points you've selected, click the 'Cancel' button.</p>";
		
		this.pstObHelpPopupText = "<h3><em><font color='red'>Overlap Detection</font></em></h3>"
				+ "<p>"
				+ "This feature allows you to exclude objects (neurons) which were counted twice, once in each of two "
				+ "channels designated for neuron processing. Like in the previous view, "
				+ "you can resize the program window to enlarge the image. If this is insufficient, you may zoom in by hitting the Shift key (will zoom in where your mouse lies, so the mouse must be within the image boundaries). "
				+ "You can navigate while zoomed using A (left), W (up), S (down), D (right), and zoom out using the space bar.<br><br>"
				+ "You may also toggle the image dots which represent objects using the checkbox in the options panel or by pressing E."
				+ "</p>"
				+ "<h3><em><font color='red'>Images</font></em></h3>"
				+ "<p>"
				+ "Four images are provided for overlap detection. All images can either be displayed in Z-project (max method) "
				+ "or stack version, which is toggled using the checkbox in the options panel or by pressing R. Dots representing objects "
				+ "are colored either blue or magenta for each channel, where color assignments are at random (to reduce bias). "
				+ "<br><br>"
				+ "The first image is a ROUGH prediction of overlapping cells based on image processing techniques. The second image is a depiction of "
				+ "overlap overlap in objects calculated by the TRON program, where yellow is overlap and green and red are the two "
				+ "channels for neuron processing (green and red do NOT necessarily correlate with actual colors of channels). "
				+ "The third image is a raw depiction of all overlapping pixels between the two channels, where black is no overlap and "
				+ "yellow is most overlap (according to scale at top of image). This should give a pretty good depiction of the degree of "
				+ "overlap. The fourth image is a merge of the two original channels, where yellow pixels indicate overlap."
				+ "</p>"
				+ "<h3><em><font color='red'>Removing an Object</font></em></h3>"
				+ "<p>"
				+ "When you want to remove an object that has been double clicked, right-click near the dot (object). The object "
				+ "in each channel closest to your crosshair will be removed, unless there are no object(s) close to the crosshair."
				+ "</p>";
		
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

		distBtnAddROI = new JButton("<html><p style='text-align: center;'>Add (<a style='text-decoration:underline'>R</a>)</p></html>");
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
		distBtnHelp.setForeground(new Color(255, 255, 255));
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
		objBtnHelp.setForeground(new Color(255, 255, 255));
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
		pstObBtnHelp.setForeground(new Color(255, 255, 255));
		pstObBtnHelp.setBorderPainted(false);
		pstObBtnHelp.setOpaque(false);
		pstObBtnHelp.setBackground(Color.WHITE);
		pstObBtnHelp.setFocusable(false);

		pstObLblInstructions = new JLabel("Right click to remove double points.");

		pstObLblOptions = new JLabel("Options:");
		pstObChkDots = new JCheckBox("<html>Dots (<a style='text-decoration:underline'>E</a>)</p></html>");
		pstObChkDots.setBackground(GUI.colorPnlEnabled);
		pstObChkDots.setFocusable(false);
		pstObChkDots.setSelected(true);
		pstObChkDots.setEnabled(false);
		pstObChkMax = new JCheckBox("<html>Max Project (<a style='text-decoration:underline'>R</a>)</p></html>");
		pstObChkMax.setBackground(GUI.colorPnlEnabled);
		pstObChkMax.setFocusable(false);
		pstObChkMax.setSelected(true);
		pstObChkMax.setEnabled(false);

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
		sliceLblERR.setForeground(new Color(255, 100, 100));
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

				GUI.displayPopupMessage(objHelpPopupText, "Help", gui.getComponent(), JOptionPane.INFORMATION_MESSAGE);
			}

		});

		this.objBtnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (objBtnRemove.getText().equals("Cancel")) {
					objBtnNext.setEnabled(true);
					objBtnPick.setText("Pick Mult.");
					objBtnRemove.setText("Remove");
					objTxtRemove.setEnabled(true);

					imgCurrObjEditing.cancelDeletionZone();
					gui.getImageDisplay().setImage(imgCurrObjEditing.getImgWithDots(gui.getPanelDisplay().getSliderSelectedPageAsChannel(), 
							gui.getImageDisplay().getZoom(), true).getBufferedImage(), true);

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
								GUI.displayMessage("Error: For value ranges, the lower bound must be lower than the higher bound.", "Invalid Input", gui.getImageDisplay(), JOptionPane.ERROR_MESSAGE);
								return;
							}
							for (int i = lower; i <= higher; i++) {
								objectsToRemove.add(i);
							}

						}
					}
				} catch (Exception exc) {
					GUI.displayMessage("Error: Found text that isn't valid format (not ','  '-'  or an integer).", "Invalid Input", gui.getImageDisplay(), JOptionPane.ERROR_MESSAGE);

					return;
				}

				Channel chan = gui.getPanelDisplay().getSliderSelectedPageAsChannel();
				boolean validPoints = imgCurrObjEditing.removePoints(chan, objectsToRemove);
				objTxtRemove.setText("");
				if (!validPoints) {
					GUI.displayMessage("Warning: Your input contained at least one object number that doesn't exist.", "Odd Input", gui.getImageDisplay(), JOptionPane.WARNING_MESSAGE);
				}

				gui.getImageDisplay().setImage(imgCurrObjEditing.getImgWithDots(gui.getPanelDisplay().getSliderSelectedPageAsChannel(), 
						gui.getImageDisplay().getZoom(), true), true);

			}
		});

		this.objChkDots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (imgCurrObjEditing != null) {
					try {

						imgCurrObjEditing.setDisplayOptions(objChkOriginal.isSelected(), objChkMask.isSelected(), objChkDots.isSelected());
						gui.getImageDisplay().setImage(imgCurrObjEditing.getImgWithDots(gui.getPanelDisplay().getSliderSelectedPageAsChannel(), 
								gui.getImageDisplay().getZoom(), true), true);
					} catch (IllegalArgumentException ex) {
						GUI.displayMessage("You must have at least one layer selected.", "Layering Error", gui.getImageDisplay(), JOptionPane.ERROR_MESSAGE);

						objChkDots.setSelected(true);
					}
				}
			}
		});

		this.objChkOriginal.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (imgCurrObjEditing != null) {
					try {

						imgCurrObjEditing.setDisplayOptions(objChkOriginal.isSelected(), objChkMask.isSelected(), objChkDots.isSelected());
						gui.getImageDisplay().setImage(imgCurrObjEditing.getImgWithDots(gui.getPanelDisplay().getSliderSelectedPageAsChannel(), 
								gui.getImageDisplay().getZoom(), true), true);
					} catch (IllegalArgumentException ex) {
						GUI.displayMessage("You must have at least one layer selected.", "Layering Error", gui.getImageDisplay(), JOptionPane.ERROR_MESSAGE);

						objChkOriginal.setSelected(true);
					}
				}
			}
		});

		this.objChkMask.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (imgCurrObjEditing != null) {
					try {

						imgCurrObjEditing.setDisplayOptions(objChkOriginal.isSelected(), objChkMask.isSelected(), objChkDots.isSelected());
						gui.getImageDisplay().setImage(imgCurrObjEditing.getImgWithDots(gui.getPanelDisplay().getSliderSelectedPageAsChannel(), 
								gui.getImageDisplay().getZoom(), true), true);
					} catch (IllegalArgumentException ex) {
						GUI.displayMessage("You must have at least one layer selected.", "Layering Error", gui.getImageDisplay(), JOptionPane.ERROR_MESSAGE);

						objChkMask.setSelected(true);
					}
				}
			}
		});

		this.objBtnPick.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (objBtnPick.getText().equals("Pick Mult.")) {
					if (imgCurrObjEditing != null) {
						objBtnNext.setEnabled(false);
						objBtnRemove.setText("Cancel");
						objBtnPick.setText("Done");
						objTxtRemove.setEnabled(false);
						imgCurrObjEditing.setCreatingDeletionZone(true);
					}

				} else {
					objBtnNext.setEnabled(true);
					objBtnPick.setText("Pick Mult.");
					objBtnRemove.setText("Remove");
					objTxtRemove.setEnabled(true);
					boolean occurred = imgCurrObjEditing.deleteObjectsWithinDeletionZone();
					if (!occurred) {
						GUI.displayMessage("No objects were removed because you didn't select at least 3 points for the bounding region.", "Error Removing Points", gui.getImageDisplay(), JOptionPane.ERROR_MESSAGE);
					}
					gui.getImageDisplay().setImage(imgCurrObjEditing.getImgWithDots(gui.getPanelDisplay().getSliderSelectedPageAsChannel(), 
							gui.getImageDisplay().getZoom(), true).getBufferedImage(), true);

				}

			}
		});



		this.objBtnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				nextObjImage(false);

			}
		});

		this.pstObChkDots.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				if (imgCurrObjEditing != null && imgCurrObjEditing.isPostProcessing()) {
					imgCurrObjEditing.setDisplayPostObjectDots(pstObChkDots.isSelected());
					gui.getImageDisplay().setImage(imgCurrObjEditing.getPostObjectImage(gui.getPanelDisplay().getSliderSelectedPage().getDisplayAbbrev(),
							gui.getPanelDisplay().getSliderSelectedSlice(), gui.getImageDisplay().getZoom()), true);

				}

			}
		});
		this.pstObChkMax.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean maxed = pstObChkMax.isSelected();
				if (imgCurrObjEditing != null && imgCurrObjEditing.isPostProcessing()) {
					imgCurrObjEditing.setDisplayPostObjectMax(pstObChkMax.isSelected());

					String selectedPage = gui.getPanelDisplay().getSliderSelectedPage().getDisplayAbbrev();
					if (maxed) {
						gui.getPanelDisplay().disableSliceSlider(true); 
					} else {
						gui.getPanelDisplay().enableSliceSlider(1, imgCurrObjEditing.getPostObjectImageStackSize(selectedPage), 1, true);
					}

					gui.getImageDisplay().setImage(imgCurrObjEditing.getPostObjectImage(selectedPage,
							gui.getPanelDisplay().getSliderSelectedSlice(), gui.getImageDisplay().getZoom()), true);


				}


			}
		});
		
		this.pstObBtnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUI.displayPopupMessage(pstObHelpPopupText, "Help", gui.getComponent(), JOptionPane.INFORMATION_MESSAGE);
				
			}
		});

		this.pstObBtnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				nextObjImage(true);
			}
		});

		this.distBtnCancelROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (imageCurrentlyROIEditing != null) {
					imageCurrentlyROIEditing.clearPoints();
					gui.getImageDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(gui.getPanelDisplay().getSliderSelectedPageAsChannel()), false);
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
						String input = GUI.getInput("To create the ROI, type its name below and then select the side of the ROI to be designated as POSITIVE:", "Select ROI Name", gui.getImageDisplay());

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


							gui.getImageDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(gui.getPanelDisplay().getSliderSelectedPageAsChannel()), false);

						}
					} else {
						GUI.displayMessage("You must select at least 2 points on he image before creating an ROI.<br><br>The ROI line must not overlap with existing lines.", "ROI Creation Error", gui.getImageDisplay(), JOptionPane.ERROR_MESSAGE);

					}
				}
			}
		});

		this.distBtnDeleteROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				java.util.List<PolarizedPolygonROI> selectedRois = roiListROI.getSelectedMult();
				if (selectedRois != null && !selectedRois.isEmpty() && imageCurrentlyROIEditing != null) {
					for (PolarizedPolygonROI roi : selectedRois) {
						imageCurrentlyROIEditing.removeROI(roi);
						roiListROI.removeItem(roi);
					}
					roiListROI.clearSelection();

					gui.getImageDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(gui.getPanelDisplay().getSliderSelectedPageAsChannel()), false);
				}


			}
		});

		this.distBtnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GUI.displayPopupMessage(distHelpPopupText, "Help", gui.getComponent(), JOptionPane.INFORMATION_MESSAGE);

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
						gui.getImageDisplay(), JOptionPane.WARNING_MESSAGE)) {

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

					gui.getPanelDisplay().disableSliceSlider(false);

					Channel roiDrawChan = runConfig.channelMan.getPrimaryROIDrawChan();
					java.util.List<Channel> channelsForROISelection = runConfig.channelMan.getOrderedChannels();
					channelsForROISelection.remove(roiDrawChan);
					channelsForROISelection.add(0, roiDrawChan);


					gui.getPanelDisplay().setPageSlider(true, channelsForROISelection, "Chan");

					gui.getImageDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(roiDrawChan), false);

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

		java.util.List<Channel> listToSendSlider = this.imageCurrentlyPreprocessing.getRunConfig().channelMan.getOrderedChannels();

		int stackSize = imageCurrentlyPreprocessing.getOrigStackSize(listToSendSlider.get(0));
		this.gui.getPanelDisplay().enableSliceSlider(1, stackSize, 1, false);
		this.gui.getPanelDisplay().setPageSlider(true, listToSendSlider, "Chan");
		this.gui.getImageDisplay().setImage(imageCurrentlyPreprocessing.getSlice(listToSendSlider.get(0), 1, false), false);
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


		objBtnNext.setEnabled(false);
		objTxtRemove.setEnabled(false);
		objBtnRemove.setEnabled(false);
		objChkDots.setEnabled(false);
		objChkOriginal.setEnabled(false);
		objChkMask.setEnabled(false);

		boolean cancelNext = false;

		if (this.imgCurrObjEditing != null) {
			if (!this.imgCurrObjEditing.isPostProcessing()) {
				Channel chan = imgCurrObjEditing.getSelectionStateMeta().getChannelNotLookedAt();
				if (chan != null) {

					if (!GUI.confirmWithUser("You have not yet looked at the "+ chan.getName() + 
							" channel yet. Are you sure you want to continue?", 
							"Confirm Skip Channel", 
							gui.getImageDisplay(), JOptionPane.WARNING_MESSAGE)) {
						cancelNext = true;

					}
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

					if (imgCurrObjEditing != null) {

						if (!returnFromPostObjProcessing && GUI.settings.processingPostObj) {

							if (imgCurrObjEditing.checkPostObjectImages()) {

								_nextPostObjImageHelper();
								return;
							}
						}

						if (GUI.settings.processingPostObjDelete) {
							imgCurrObjEditing.getContainer().removeImageFromIC(OutputOption.Channel, null);
							imgCurrObjEditing.getContainer().removeImageFromIC(OutputOption.ProcessedObjectsStack, null);
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
						gui.getWizard().nextState(imagesForROICreation);
						return;

					}

					ImageContainer ic = imgCurrObjEditing.getContainer();

					gui.getPanelDisplay().disableSliceSlider(false);
					java.util.List<Channel> chans = new ArrayList<Channel>();
					int processedInsertIndex = 0;
					for (Channel chan : ic.getRunConfig().channelMan.getOrderedChannels()) {

						if (imgCurrObjEditing.getRunConfig().channelMan.isProcessChannel(chan)) {
							chans.add(processedInsertIndex, chan); // Add to beginning
							processedInsertIndex++;
						} else {
							chans.add(chan); // Add to end
						}
					}

					gui.getPanelDisplay().setPageSlider(true, chans, "Chan");
					gui.getImageDisplay().setImage(imgCurrObjEditing.getImgWithDots(chans.get(0), Zoom.ZOOM_100, true).getBufferedImage(), false);
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


					imgCurrObjEditing.getSelectionStateMeta().lookAt(gui.getPanelDisplay().getSliderSelectedPageAsChannel());
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

		java.util.List<PostProcessImage> ppis = imgCurrObjEditing.createPostObjectImages();

		gui.getPanelDisplay().setPageSlider(true, ppis, "Img");
		imgCurrObjEditing.setDisplayPostObjectDots(true);
		imgCurrObjEditing.setDisplayPostObjectMax(true);
		gui.getImageDisplay().setImage(imgCurrObjEditing.getPostObjectImage(ppis.get(0).getDisplayAbbrev(), 1, Zoom.ZOOM_100), false);
		pstObTxtCurrDisp.setText(imgCurrObjEditing.getContainer().getImageTitle());
		pstObTxtCurrView.setText(ppis.get(0).getTitle(true));
		pstObChkDots.setSelected(true);
		pstObChkDots.setEnabled(true);
		pstObChkMax.setSelected(true);
		pstObChkMax.setEnabled(true);
		pstObBtnNext.setEnabled(true);
		setDisplayState(STATE_POST_OBJ, null);
		gui.getPanelDisplay().setDisplayState(true, null);
		this.gui.getLogger().setCurrentTaskComplete();
	}

	/**
	 * This is a helper method. <br><br>
	 * 
	 * Saves the current object image and then opens the next one, storing in {@link #imgCurrObjEditing}.
	 * Returns an integer corresponding to the result of these operations.
	 * 
	 * @return 1 = there are no more images to process. 2 = the next image could not be opened for some reason 
	 * (user is informed of this). 3 = successfully picked next, which is now set as the current.
	 */
	private synchronized int _nextObjImageHelper() {

		if (this.imgCurrObjEditing != null) {

			this.gui.getLogger().setCurrentTask("Creating new images...");

			this.imgCurrObjEditing.createAndAddNewImagesToIC();
			this.gui.getLogger().setCurrentTaskComplete();

			this.gui.getLogger().setCurrentTask("Saving state...");

			// convert
			ROIEditableImage roiei = this.imgCurrObjEditing.convertToROIEditableImage();
			// delete obj if not serve ints
			imgCurrObjEditing.getContainer().getSerializeFile(ImageContainer.STATE_OBJ);
			imgCurrObjEditing.deleteSerializedVersion();
			if (GUI.settings.saveIntermediates) {
				ObjectEditableImage.saveObjEditableImage(this.imgCurrObjEditing, this.imgCurrObjEditing.getContainer().getSerializeFile(ImageContainer.STATE_OBJ));
			}

			// save roi
			File serializeFile = roiei.getContainer().getSerializeFile(ImageContainer.STATE_ROI);
			ROIEditableImage.saveROIEditableImage(roiei, serializeFile);
			this.imagesForROICreation.add(serializeFile);
			this.gui.getLogger().setCurrentTaskComplete();

			this.imgCurrObjEditing = null;
		}


		if (this.imagesForObjectSelection.isEmpty())
			return 1;

		this.gui.getLogger().setCurrentTask("Opening image...");

		this.imgCurrObjEditing = ObjectEditableImage.loadObjEditableImage(this.imagesForObjectSelection.remove(0));

		if (imgCurrObjEditing == null) {
			this.gui.getLogger().setCurrentTaskCompleteWithError();
			GUI.displayMessage("There was an error opening saved object state.", "File Open Error", null, JOptionPane.ERROR_MESSAGE);

			return 2; // returning true allows it to just skip to the next image.
		} else if (!imgCurrObjEditing.getRunConfig().channelMan.hasIdenticalChannels(GUI.settings.channelMan)) {
			this.gui.getLogger().setCurrentTaskCompleteWithError();
			GUI.displayMessage("The saved Object state couldn't be opened because the Channel Setup does not match the current Channel Setup "
					+ "in the Preferences. To fix this, use the Templates option in the Preferences to load the Channel Setup from a run.", "File Open Error", null, JOptionPane.ERROR_MESSAGE);
			imgCurrObjEditing = null;
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
	public synchronized void setImagesForSliceSelection(java.util.List<ImagePhantom> images) {
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
	public synchronized boolean startImageObjectSelecting(java.util.List<File> files) {

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
	public synchronized boolean startImageROISelecting(java.util.List<File> images) {
		if (images.isEmpty()) {
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
								.addGroup(gl_objPnl.createParallelGroup(Alignment.BASELINE)
										.addComponent(objLblShow)
										.addComponent(objChkMask)
										.addComponent(objChkOriginal)
										.addComponent(objChkDots)
										.addComponent(objBtnNext))
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
										.addGroup(Alignment.TRAILING,  gl_pnlPostObj.createSequentialGroup()
												.addComponent(pstObLblOptions, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)	
												.addComponent(pstObChkDots, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)		
												.addComponent(pstObChkMax, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
												.addComponent(pstObBtnNext))
										)
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
								.addGroup(gl_pnlPostObj.createParallelGroup(Alignment.BASELINE)
										.addComponent(pstObTxtCurrView, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(pstObLblCurrView))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pnlPostObj.createParallelGroup(Alignment.LEADING)
										.addComponent(pstObLblInstructions)
										.addComponent(pstObBtnHelp, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
								.addGroup(gl_pnlPostObj.createParallelGroup(Alignment.BASELINE)
										.addComponent(pstObLblOptions)
										.addComponent(pstObChkDots)
										.addComponent(pstObChkMax)
										.addComponent(pstObBtnNext))

								.addContainerGap())
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
	public synchronized ImagePlus sliderSliceChanged(PnlDisplayPage currentPage, int slice) {
		if (currentState == STATE_SLICE) {
			if(this.imageCurrentlyPreprocessing != null) {
				return this.imageCurrentlyPreprocessing.getSlice((Channel) currentPage, slice, false);
			}
		} else if (currentState == STATE_POST_OBJ && this.imgCurrObjEditing != null) {
			return this.imgCurrObjEditing.getPostObjectImage(currentPage.getDisplayAbbrev(), slice, gui.getImageDisplay().getZoom());
		}

		return null;

	}

	@Override
	public synchronized Object[] sliderPageChanged(PnlDisplayPage displayPage, int slice) {
		ImagePlus ipToSet = null;
		boolean keepZoom = false;
		boolean useScaleBar = false;
		if (currentState == STATE_SLICE) {
			if(this.imageCurrentlyPreprocessing != null) {
				ipToSet = this.imageCurrentlyPreprocessing.getSlice((Channel) displayPage, slice, false);
			}

		} else if (currentState == STATE_OBJ) {
			if(imgCurrObjEditing != null)
			{

				Channel chan = (Channel) displayPage;
				this.imgCurrObjEditing.getSelectionStateMeta().lookAt(chan);
				gui.getImageDisplay().zoomOut(true, false);
				if (imgCurrObjEditing.getRunConfig().channelMan.isProcessChannel(chan)) {
					ipToSet = imgCurrObjEditing.getImgWithDots(chan, Zoom.ZOOM_100, true);
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
					ipToSet = imgCurrObjEditing.getContainer().getImage(OutputOption.MaxedChannel, chan, false);
				}

			}
		} else if (currentState == STATE_POST_OBJ) {

			if(imgCurrObjEditing != null) {
				keepZoom = true;
				pstObTxtCurrView.setText(((PostProcessImage) displayPage).getTitle(pstObChkMax.isSelected()));
				ipToSet = imgCurrObjEditing.getPostObjectImage(displayPage.getDisplayAbbrev(), slice, gui.getImageDisplay().getZoom());
				useScaleBar = displayPage.getDisplayAbbrev().equalsIgnoreCase("ov");
			}
		} else if (currentState == STATE_ROI) {
			if (imageCurrentlyROIEditing != null) {
				Channel chan = (Channel) displayPage;

				if (!imageCurrentlyROIEditing.getRunConfig().channelMan.isProcessChannel(chan)) {
					this.gui.getBrightnessAdjuster().setModifying(imageCurrentlyROIEditing.getContainer(), OutputOption.MaxedChannel, chan);
					ipToSet = this.imageCurrentlyROIEditing.getPaintedCopy(chan);
				} else {
					this.gui.getBrightnessAdjuster().setModifying(null, null, null);
					ipToSet = this.imageCurrentlyROIEditing.getPaintedCopy(chan);
				}
			}
		}

		return new Object[] {ipToSet, keepZoom, useScaleBar};
	}

	@Override
	public void requestTintChannel(PnlDisplayPage displayPage, int slice, Color tintColor) {

		// Only act if we have a preprocessed image container available
		if (this.imageCurrentlyPreprocessing == null) return;

		if (!(displayPage instanceof Channel)) return;
		Channel chan = (Channel) displayPage;

		try {
			ImageContainer ic = this.imageCurrentlyPreprocessing.getContainer();
			// Iterate all OutputOption types and replace any stored image for this channel
			for (com.typicalprojects.TronMachine.neuronal_migration.OutputOption opt : com.typicalprojects.TronMachine.neuronal_migration.OutputOption.values()) {
				try {
					if (opt.getRestrictedOption() == com.typicalprojects.TronMachine.neuronal_migration.OutputOption.NO_CHANS)
						continue;
					if (!ic.containsImage(opt, chan))
						continue;
					ImagePlus src = ic.getImage(opt, chan, false);
					if (src == null) continue;
					int w = src.getWidth();
					int h = src.getHeight();
					int stackSize = src.getStackSize();
					ij.ImageStack newStack = new ij.ImageStack(w, h);
					for (int s = 1; s <= Math.max(1, stackSize); s++) {
						ij.process.ImageProcessor p = (stackSize > 1) ? src.getStack().getProcessor(s) : src.getProcessor().duplicate();
						int[] pixels = new int[w * h];
						int idx = 0;
						float maxVal = (float) p.getMax();
						// Hue-shift algorithm for persistent replacement: preserve brightness, set hue to tint color
						float[] tintHSB = Color.RGBtoHSB(tintColor.getRed(), tintColor.getGreen(), tintColor.getBlue(), null);
						float tintHue = tintHSB[0];
						for (int y = 0; y < h; y++) {
							for (int x = 0; x < w; x++) {
								float pv = p.getPixelValue(x, y);
								int intensity = 0;
								if (maxVal > 0) intensity = Math.round((pv / maxVal) * 255.0f);
								float value = Math.max(0f, Math.min(1f, intensity / 255f));
								int newRgbNoAlpha = Color.HSBtoRGB(tintHue, 1.0f, value) & 0xFFFFFF;
								pixels[idx++] = newRgbNoAlpha;
							}
						}
						ij.process.ColorProcessor cp = new ij.process.ColorProcessor(w, h, pixels);
						newStack.addSlice(cp);
					}
					ImagePlus newImg = new ImagePlus(src.getTitle(), newStack);
					newImg.setCalibration(src.getCalibration());
					ic.addImage(opt, chan, newImg);
				} catch (Exception e) {
					// ignore single-option failures and continue
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			// If anything goes wrong, log and continue (do not crash UI)
			e.printStackTrace();
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
		this.imgCurrObjEditing = null;
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
	public ImagePlus zoomChanged(PnlDisplayPage dspPage, Zoom newZoom) {

		if (gui.getWizard().getStatus() == Status.SELECT_OB) {
			if (imgCurrObjEditing != null) {
				if (imgCurrObjEditing.isPostProcessing()) {
					return imgCurrObjEditing.getPostObjectImage(dspPage.getDisplayAbbrev(), gui.getPanelDisplay().getSliderSelectedSlice(), newZoom);
				}
				Channel chan = (Channel) dspPage;

				if (imgCurrObjEditing.getRunConfig().channelMan.isProcessChannel(chan)) {
					return imgCurrObjEditing.getImgWithDots(chan, newZoom, true);
				}
			}
		}
		return null;
	}

	@Override
	public synchronized void mouseClickOnImage(Point p, PnlDisplayPage displayPage, int slice, boolean isLeftClick) {

		if (currentState == STATE_OBJ) {

			if (p != null && imgCurrObjEditing != null && imgCurrObjEditing.getRunConfig().channelMan.isProcessChannel(gui.getPanelDisplay().getSliderSelectedPageAsChannel())) {

				if (isLeftClick) {
					if (this.imgCurrObjEditing.isCreatingDeletionZone()) {
						imgCurrObjEditing.addDeletionZonePoint(p);
					} else {
						imgCurrObjEditing.addPoint(gui.getPanelDisplay().getSliderSelectedPageAsChannel(), p);
					}
				} else {
					Channel chan = gui.getPanelDisplay().getSliderSelectedPageAsChannel();
					Point closest = imgCurrObjEditing.getNearestPoint(chan, gui.getImageDisplay().getZoom(), p);
					if (closest != null) {
						imgCurrObjEditing.removePoint(chan, closest);
					}

				}

				gui.getImageDisplay().setImage(imgCurrObjEditing.getImgWithDots(gui.getPanelDisplay().getSliderSelectedPageAsChannel(), 
						gui.getImageDisplay().getZoom(), true).getBufferedImage(), true);


			}
		} else if (currentState == STATE_POST_OBJ) {
			
			if (p != null && imgCurrObjEditing != null && imgCurrObjEditing.isPostProcessing() && !isLeftClick) {
				Point closest1 = null;
				Point closest2 = null;
				if (this.pstObChkMax.isSelected()) {
					closest1 = imgCurrObjEditing.getNearestPoint(imgCurrObjEditing.getPostProcessingChannel(true), Zoom.ZOOM_100, p);
					closest2 = imgCurrObjEditing.getNearestPoint(imgCurrObjEditing.getPostProcessingChannel(false), Zoom.ZOOM_100, p);

				} else {
					closest1 = imgCurrObjEditing.getNearestPoint(imgCurrObjEditing.getPostProcessingChannel(true), Zoom.ZOOM_100, p, slice);
					closest2 = imgCurrObjEditing.getNearestPoint(imgCurrObjEditing.getPostProcessingChannel(false), Zoom.ZOOM_100, p, slice);

				}
				
				if (closest1 != null) {
					imgCurrObjEditing.removePoint(imgCurrObjEditing.getPostProcessingChannel(true), closest1);
				
				}
				if (closest2 != null) {
					imgCurrObjEditing.removePoint(imgCurrObjEditing.getPostProcessingChannel(false), closest2);

				}
				
				gui.getImageDisplay().setImage(imgCurrObjEditing.getPostObjectImage(displayPage.getDisplayAbbrev(), slice, gui.getImageDisplay().getZoom()), true);

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
				gui.getImageDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(gui.getPanelDisplay().getSliderSelectedPageAsChannel()), true);
			}

		}		
	}


	@Override
	public void keyboardCharPressed(char character) {
		if (Character.isLetter(character)) {
			character = Character.toUpperCase(character);
		}
		if (currentState == STATE_ROI) {
			if (character == 'R') {
				if (this.distBtnAddROI.isEnabled()) {
					this.distBtnAddROI.doClick();
				}
			}
		} else if (currentState == STATE_POST_OBJ) {
			if (character == 'E') {
				this.pstObChkDots.doClick();
			} else if (character == 'R') {
				this.pstObChkMax.doClick();
			} else {
				gui.getPanelDisplay().setSelectedPage(Character.getNumericValue(character));

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
		gui.getImageDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(chan), true);
	}

	/**
	 * @return image currently object editing, will be null if not in the object edit phase.
	 */
	public ObjectEditableImage getObjectEditableImage() {
		return this.imgCurrObjEditing;
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
					renderer.setBorder(new EmptyBorder(16, 16, 16, 16));
				} else {
					renderer.setText(value.toString());
				}
			} else if (value != null) {
			}

			return renderer;


		}

	}

}
