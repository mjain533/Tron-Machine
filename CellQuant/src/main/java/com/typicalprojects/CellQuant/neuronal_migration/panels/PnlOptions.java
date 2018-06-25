package com.typicalprojects.CellQuant.neuronal_migration.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;

import com.typicalprojects.CellQuant.neuronal_migration.GUI;
import com.typicalprojects.CellQuant.neuronal_migration.Wizard.Status;
import com.typicalprojects.CellQuant.neuronal_migration.panels.PnlDisplay.PnlDisplayFeedbackReceiver;
import com.typicalprojects.CellQuant.popup.HelpPopup;
import com.typicalprojects.CellQuant.popup.TextInputPopup;
import com.typicalprojects.CellQuant.popup.TextInputPopupReceiver;
import com.typicalprojects.CellQuant.popup.BrightnessAdjuster.BrightnessChangeReceiver;
import com.typicalprojects.CellQuant.processing.NeuronProcessor;
import com.typicalprojects.CellQuant.processing.ObjectEditableImage;
import com.typicalprojects.CellQuant.processing.ROIEditableImage;
import com.typicalprojects.CellQuant.util.ImageContainer;
import com.typicalprojects.CellQuant.util.Point;
import com.typicalprojects.CellQuant.util.ProspectiveImage;
import com.typicalprojects.CellQuant.util.SimpleJList;
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import ij.ImagePlus;
import ij.measure.ResultsTable;

public class PnlOptions implements TextInputPopupReceiver, PnlDisplayFeedbackReceiver, BrightnessChangeReceiver {
	
	public static final int STATE_DISABLED = 1;
	public static final int STATE_INFO = 2;
	public static final int STATE_OBJ = 3;
	public static final int STATE_COUNT_DIST = 4;
	public static final int LAYOUT_TYPE_INFO = 1;
	public static final int LAYOUT_TYPE_OBJ = 2;
	public static final int LAYOUT_TYPE_COUNT_DIST = 3;
	
	private JPanel rawPanel;
	private GUI gui;
	
	private ImageContainer imageCurrentlyDisplayed = null;
	private ObjectEditableImage imageCurrentlyObjEditing = null;
	private ROIEditableImage imageCurrentlyROIEditing = null;
	
	private JLabel lblDisabled;
	private Color colorDisabled = new Color(169, 169, 169);
	private Color colorEnabled = new Color(220, 220, 220);
	
	
	private JTextField distTxtCurrDisp;
	private JButton distBtnAddROI;
	private JButton distBtnCancelROI;
	private JButton distBtnDeleteROI;
	private JButton distBtnNext;
	private JButton distBtnHelp;
	private HelpPopup distHelpPopup;

	

	private JTextField infoTxtDisplaying;
	private JTextField infoTxtLowSlice;
	private JTextField infoTxtHighSlice;
	private JLabel infoLblCurrDisp;
	private JLabel infoLblSelectSlices;
	private JLabel infoLblThru;
	private JButton infoBtnNext;
	private JLabel infoLblERR;
	
	private JButton objBtnRemove;
	private JButton objBtnNext;
	private JTextField objTxtCurrDisp;
	private JTextField objTxtRemove;
	private JButton objBtnHelp;
	private HelpPopup objHelpPopup;
	
	
	private TextInputPopup roiNamePopup;
	
	private List<ProspectiveImage> imagesForObjectAnalysis = new LinkedList<ProspectiveImage>();
	private List<ProspectiveImage> imagesForObjectSelection = new LinkedList<ProspectiveImage>();
	private List<ProspectiveImage> imagesForROICreation = new LinkedList<ProspectiveImage>();
	private Map<Channel, Integer[]> defMinsMaxes = new HashMap<Channel, Integer[]>();
	private List<ProspectiveImage> imagesForSliceSelection = null;
	private int currentState = STATE_DISABLED;
	private NeuronProcessor neuronProcessor;
	private JLabel objLblCurrDisp;
	private JLabel objLblInstructions;
	private JLabel objLblRemove;
	private JScrollPane distSP;
	private SimpleJList<String> distListROI;
	private JLabel distLblInstruction;
	private JLabel distLblCurrDisp;
	
	public volatile Thread currThread;

	
	public PnlOptions(GUI gui) {
		
		this.rawPanel =  new JPanel();
		this.rawPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));

		this.gui = gui;
		
		String message = "<html>Type a number in the text field after 'Obj. Num.:' and click the Remove button "
				+ "to remove that object in the image. Click on the image itself to manually add objects. Make sure "
				+ "to select objects for all relevants channels of the image. You can toggle between channels using "
				+ "the Channel slider beneath the image."
				+ "<br><br>"
				+ "Points can be manually edited by clicking the 'Open Points Editor' button.</html>";
		
		this.objHelpPopup = new HelpPopup(300, 400, message, gui.getComponent());
		
		String message2 = "<html>Click on the image to start adding points to the ROI. Then, click 'Add' in the "
				+ "panel below the image to add the region. You can delete this region via the 'Delete' button.<br><br>"
				+ "To RESET the points you've selected, click the 'Cancel' button.</html>";
		
		this.distHelpPopup = new HelpPopup(250, 350, message2, gui.getComponent());
		
		lblDisabled = new JLabel("<html><body><p style='width: 100px; text-align: center;'>Please select images using the interface above.</p></body></html>");
		lblDisabled.setHorizontalAlignment(SwingConstants.CENTER);
		
		this.roiNamePopup = new TextInputPopup("Select a name for this ROI:", this);
		
		setUpDirectionsPanels();
		setDisplayState(STATE_DISABLED, null);

	}
	
	private void setUpDirectionsPanels() {


		// distance 

		distLblCurrDisp = new JLabel("Currently Displaying");

		distTxtCurrDisp = new JTextField();
		distTxtCurrDisp.setColumns(10);
		distTxtCurrDisp.setFocusable(false);
		distTxtCurrDisp.setEditable(false);

		distLblInstruction = new JLabel("Please create at least one ROI below.");

		distBtnAddROI = new JButton("Add");
		distBtnAddROI.setFont(new Font("PingFang TC", Font.BOLD, 10));
		distBtnAddROI.setFocusable(false);

		distBtnCancelROI = new JButton("Clear");
		distBtnCancelROI.setFont(new Font("PingFang TC", Font.BOLD, 10));
		distBtnCancelROI.setFocusable(false);

		distBtnDeleteROI = new JButton("Delete");
		distBtnDeleteROI.setFont(new Font("PingFang TC", Font.BOLD, 10));
		distBtnCancelROI.setFocusable(false);

		distSP = new JScrollPane();
		distSP.setFocusable(false);

		distBtnNext = new JButton("Next");
		distBtnNext.setFocusable(false);

		
		distBtnHelp = new JButton("");
		distBtnHelp.setIcon(new ImageIcon(new ImageIcon(PnlOptions.class.getResource("/question.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
		distBtnHelp.setForeground(Color.BLUE);
		distBtnHelp.setBorderPainted(false);
		distBtnHelp.setOpaque(false);
		distBtnHelp.setBackground(Color.WHITE);
		distBtnHelp.setFocusable(false);
		
		

		
		distListROI = new SimpleJList<String>();
		distSP.setViewportView(distListROI.getGUIComponent());
		distListROI.getGUIComponent().setFocusable(false);

		// Objects
		
		objLblCurrDisp = new JLabel("Displaying:");

		objTxtCurrDisp = new JTextField();
		objTxtCurrDisp.setColumns(10);
		objTxtCurrDisp.setEditable(false);
		

		objLblInstructions = new JLabel("Click image to add points. Remove below.");

		objLblRemove = new JLabel("<html>Obj. Num.:</html>");
		objLblRemove.setHorizontalTextPosition(SwingConstants.LEFT);
		objLblRemove.setHorizontalAlignment(SwingConstants.LEFT);

		objBtnHelp = new JButton("");
		objBtnHelp.setIcon(new ImageIcon(new ImageIcon(GUI.class.getResource("/javax/swing/plaf/metal/icons/ocean/question.png")).getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
		objBtnHelp.setForeground(Color.BLUE);
		objBtnHelp.setBorderPainted(false);
		objBtnHelp.setOpaque(false);
		objBtnHelp.setBackground(Color.WHITE);

		objTxtRemove = new JTextField();
		objTxtRemove.setColumns(10);

		objBtnRemove = new JButton("Remove");

		objBtnNext = new JButton("Next");
		
		// Slices
		
		infoLblCurrDisp = new JLabel("Displaying:");
		
		infoLblERR = new JLabel("Invalid selection.");
		
		infoTxtDisplaying = new JTextField();
		infoTxtDisplaying.setColumns(10);
		infoTxtDisplaying.setEditable(false);

		infoLblSelectSlices = new JLabel("Select Slices:");

		infoTxtLowSlice = new JTextField();
		infoTxtLowSlice.setColumns(10);

		infoLblThru = new JLabel("thru");

		infoTxtHighSlice = new JTextField();
		infoTxtHighSlice.setColumns(10);

		infoBtnNext = new JButton("Next");

		infoLblERR = new JLabel("ERROR: Invalid slice selection.");
		infoLblERR.setForeground(Color.RED);
		infoLblERR.setVisible(false);


		

		

		this.infoBtnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {


				currThread = new Thread(new Runnable() {
					public void run(){
						try {
							infoBtnNext.setEnabled(false);
							int lower = Integer.parseInt(infoTxtLowSlice.getText());
							int higher = Integer.parseInt(infoTxtHighSlice.getText());

							if (higher > imageCurrentlyDisplayed.getStackSize(GUI.channelForROIDraw) || lower < 1 || lower >= higher) {
								throw new Exception();
							}
							infoLblERR.setVisible(false);

							if (currThread != null && !displayNextInfoImage(lower, higher)) {

								gui.getWizard().nextState();
								imagesForSliceSelection = null;
							}

							infoBtnNext.setEnabled(true);
						} catch (Exception ex) {
							infoBtnNext.setEnabled(true);
							infoLblERR.setVisible(true);
						}
					}
				});
				currThread.setDaemon(true);
				currThread.start();

			}
		});
		
		
		
		this.objBtnHelp.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				objHelpPopup.display();				
			}
			
		});
		
		this.objBtnRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				String text = objTxtRemove.getText();
				
				if (text.equals("")) return;
				
				int num = -1;
				try {
					num = Integer.parseInt(text);
				} catch (Exception exc) {
					JOptionPane.showConfirmDialog(null,  "Error: Value must be an integer.", "Invalid Input", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				Channel chan = gui.getPanelDisplay().getSliderSelectedChannel();
				boolean validPoint = imageCurrentlyObjEditing.removePoint(chan, num);
				if (!validPoint) {
					JOptionPane.showConfirmDialog(null,  "Error: An object with this number doesn't exist.", "Invalid Input", JOptionPane.OK_OPTION, JOptionPane.ERROR_MESSAGE);
				} else {
					objTxtRemove.setText("");
				}
				
				
			}
		});
		
		this.objBtnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				currThread = new Thread(new Runnable() {
					public void run(){
						try {
							objBtnNext.setEnabled(false);

							if (currThread != null && !displayNextObjImage()) {

								gui.getWizard().nextState();

							}

							objBtnNext.setEnabled(true);
						} catch (Exception ex) {
							objBtnNext.setEnabled(true);
						}
					}
				});
				currThread.setDaemon(true);
				currThread.start();

			}
		});
		
		this.distBtnCancelROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (imageCurrentlyROIEditing != null) {
					imageCurrentlyROIEditing.clearPints();
					gui.getPanelDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(gui.getPanelDisplay().getSliderSelectedChannel()));
				}
			}
		});
		
		this.distBtnAddROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (imageCurrentlyROIEditing != null) {
					if (imageCurrentlyROIEditing.hasPoints()) {
						distBtnAddROI.setEnabled(false);
						distBtnDeleteROI.setEnabled(false);
						distBtnCancelROI.setEnabled(false);
						distBtnNext.setEnabled(false);
						roiNamePopup.display(gui.getComponent());
					}
				}
			}
		});
		
		this.distBtnDeleteROI.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String selected = distListROI.getSelected();
				if (selected != null && imageCurrentlyROIEditing != null) {
					imageCurrentlyROIEditing.removeROI(selected);
					distListROI.removeItem(selected);
				}

			}
		});
		
		this.distBtnHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				distHelpPopup.display();

			}
		});
		
		this.distBtnNext.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				currThread = new Thread(new Runnable() {
					public void run(){
						
						try {
							if (currThread != null && !imageCurrentlyROIEditing.hasROIs()) {
								JOptionPane.showMessageDialog(gui.getComponent(), "Error: You must create at least one ROI first.", "Processing Error", JOptionPane.ERROR_MESSAGE);
								return;
							}
							distBtnAddROI.setEnabled(false);
							distBtnDeleteROI.setEnabled(false);
							distBtnCancelROI.setEnabled(false);
							distBtnNext.setEnabled(false);

							if (currThread != null && !displayNextROISelectImage()) {

								gui.getWizard().nextState();

							}

							distBtnNext.setEnabled(true);
						} catch (Exception ex) {
							distBtnNext.setEnabled(true);
						}
					}
				});
				currThread.setDaemon(true);
				currThread.start();
				
			}
		});
	}
	
	public JPanel getRawPanel() {
		return this.rawPanel;
	}
	
	public void processInputFromTextPopup(String text) {
		
		if (text == null || text.equals("")) {
			

		} else {
			
			if (!imageCurrentlyROIEditing.convertSelectionToRoi(text)) {
				JOptionPane.showMessageDialog(gui.getComponent(), "Error: Either this name is already taken, or you didn't select any points.", "ROI naming error.", JOptionPane.ERROR_MESSAGE);
			}
			this.distListROI.addItem(text);
			this.gui.getPanelDisplay().setImage(this.imageCurrentlyROIEditing.getPaintedCopy(this.gui.getPanelDisplay().getSliderSelectedChannel()));
		}
		
		distBtnAddROI.setEnabled(true);
		distBtnDeleteROI.setEnabled(true);
		distBtnCancelROI.setEnabled(true);
		distBtnNext.setEnabled(true);
		
	}
	
	private boolean displayNextInfoImage(int lastLowSliceSelection, int lastHighSliceSelection) {
		
		setDisplayState(STATE_DISABLED, "Processing...");
		
		if (this.imageCurrentlyDisplayed != null && lastLowSliceSelection != -1 && lastHighSliceSelection != -1) {
			this.gui.log("Selecting slices...");

			ImageContainer newIC = this.imageCurrentlyDisplayed.setSliceRegion(lastLowSliceSelection, lastHighSliceSelection);
			newIC.save(GUI.dateString);
			this.imagesForObjectAnalysis.add(new ProspectiveImage(newIC.getImgFile(), newIC.getTotalImageTitle(), this.gui, this.imageCurrentlyDisplayed.getSaveDir(), true, newIC.getCalibration()));
			this.gui.log("Success.");
			this.imageCurrentlyDisplayed = null;
		}
		

		if (this.imagesForSliceSelection.isEmpty())
			return false;
		
		ProspectiveImage pi = this.imagesForSliceSelection.remove(0);
		String errors = pi.open();

		if (errors != null) {
			JOptionPane.showMessageDialog(null, "<html>There was an error opening file " + pi.getTitle() + ":<br><br>" + errors+ "</html>", "File Open Error", JOptionPane.ERROR_MESSAGE);
			this.gui.getSelectFilesPanel().cancel();
			return true;
		}

		ImageContainer ic = pi.getIC();
		this.gui.getPanelDisplay().setSliceSlider(true, 1, ic.getStackSize(GUI.channelForROIDraw));
		this.gui.getPanelDisplay().setChannelSlider(true, ic.getChannels());
		this.gui.getPanelDisplay().setImage(ic.getImage(0, 1, false));
		this.infoTxtDisplaying.setText(ic.getTotalImageTitle());
		this.infoTxtLowSlice.setText("" + 1);
		this.infoTxtHighSlice.setText("" + ic.getStackSize(GUI.channelForROIDraw));
		this.imageCurrentlyDisplayed = ic;
		setDisplayState(STATE_INFO, null);
		this.gui.getPanelDisplay().setDisplayState(true, null);
		return true;
	}
	
	private boolean displayNextObjImage() {
		setDisplayState(STATE_DISABLED, "Opening image...");

		if (this.imageCurrentlyObjEditing != null) {
			this.gui.log("Selecting objects...");
			ImageContainer newIC = this.imageCurrentlyObjEditing.createNewImage();
			newIC.save(GUI.dateString);
			newIC.saveResultsTable(this.imageCurrentlyObjEditing.createNewResultsTables(), GUI.dateString, false);
			
			this.imagesForROICreation.add(new ProspectiveImage(newIC.getImgFile(), newIC.getTotalImageTitle(), this.gui, newIC.getSaveDir(), true, newIC.getCalibration()));
			this.gui.log("Success.");
			this.imageCurrentlyObjEditing = null;
		}
		

		if (this.imagesForObjectSelection.isEmpty())
			return false;
		
		ProspectiveImage pi = this.imagesForObjectSelection.remove(0);
		String errors = pi.open();

		if (errors != null) {
			JOptionPane.showMessageDialog(null, "<html>There was an error opening file " + pi.getTitle() + ":<br><br>" + errors+ "</html>", "File Open Error", JOptionPane.ERROR_MESSAGE);
			this.gui.getSelectFilesPanel().cancel();
			return true;
		}
		
		ImageContainer ic = pi.getIC();
		
		pi.tryToOpenResultsTables();
		Map<Channel, ImagePlus> objectImages = new HashMap<Channel, ImagePlus>();
		for (Channel chan : GUI.channelsToProcess) {
			objectImages.put(chan, ic.getImageChannel(chan, false));
		}
		
		this.imageCurrentlyObjEditing = new ObjectEditableImage(gui.getPanelDisplay().getImagePanel(), ic, objectImages, pi.tryToOpenResultsTables());
		this.gui.getPanelDisplay().setSliceSlider(false, -1, -1);
		List<Channel> chans = new ArrayList<Channel>();
		for (int i = 0; i < Channel.values().length && i < GUI.channelMap.size(); i++) {
			if (GUI.channelsToProcess.contains(GUI.channelMap.get(i))) {
				chans.add(GUI.channelMap.get(i));
			}
		}
		
		this.gui.getPanelDisplay().setChannelSlider(true, chans);
		this.gui.getPanelDisplay().setImage(this.imageCurrentlyObjEditing.getImgWithDots(chans.get(0)).getBufferedImage());
		this.objTxtCurrDisp.setText(ic.getTotalImageTitle());
		this.objTxtRemove.setText("");
		this.objBtnNext.setEnabled(true);
		this.objBtnRemove.setEnabled(true);
		
		setDisplayState(STATE_OBJ, null);
		this.gui.getPanelDisplay().setDisplayState(true, null);
		return true;
	}
	
	private boolean displayNextROISelectImage() {
		this.gui.getBrightnessAdjuster().removeDisplay();
		this.gui.getBrightnessAdjuster().reset();
		setDisplayState(STATE_DISABLED, "Processing...");
		this.gui.getPanelDisplay().setDisplayState(false, "Processing...");

		if (this.imageCurrentlyROIEditing != null) {
			this.gui.log("Calculating Distances from Rois...");
			
			Map<Channel, ResultsTable> results = imageCurrentlyROIEditing.process(this.gui.getProgressReporter());
			this.gui.log("Saving resources...");
			this.imageCurrentlyROIEditing.saveROIs();
			ImageContainer newIC = this.imageCurrentlyROIEditing.getNewImage();

			newIC.save(GUI.dateString);
			newIC.saveResultsTable(results, GUI.dateString, true);

			this.gui.log("Success.");
			this.imageCurrentlyROIEditing = null;
		}
		

		if (this.imagesForROICreation.isEmpty()) {
			return false;

		}

		ProspectiveImage pi = this.imagesForROICreation.remove(0);

		String errors = pi.open();

		if (errors != null) {
			JOptionPane.showMessageDialog(null, "<html>There was an error opening file " + pi.getTitle() + ":<br><br>" + errors+ "</html>", "File Open Error", JOptionPane.ERROR_MESSAGE);
			this.gui.getSelectFilesPanel().cancel();
			return true;
		}

		ImageContainer ic = pi.getIC();

		Map<Channel, ResultsTable> tables = pi.tryToOpenResultsTables();

		this.imageCurrentlyROIEditing = new ROIEditableImage(ic, GUI.channelForROIDraw, tables, this.gui);

		this.gui.getPanelDisplay().setSliceSlider(false, -1, -1);
		
		List<Channel> channelsForROISelection = new ArrayList<Channel>();
		channelsForROISelection.add(GUI.channelForROIDraw);
		for (int i = 0; i < GUI.channelMap.size(); i++) {
			Channel chan = GUI.channelMap.get((Integer) i);
			if (!chan.equals(GUI.channelForROIDraw)) {
				channelsForROISelection.add(chan);
			}
		}
		this.gui.getPanelDisplay().setChannelSlider(true, channelsForROISelection);

		this.gui.getPanelDisplay().setImage(this.imageCurrentlyROIEditing.getPaintedCopy(GUI.channelForROIDraw));
		this.distTxtCurrDisp.setText(ic.getTotalImageTitle());
		this.distBtnNext.setEnabled(true);
		this.distBtnCancelROI.setEnabled(true);
		this.distBtnAddROI.setEnabled(true);
		this.distBtnDeleteROI.setEnabled(true);
		this.distBtnHelp.setEnabled(true);
		this.distListROI.clear();

		this.gui.getPanelDisplay().setDisplayState(true, null);
		for (Channel chan : channelsForROISelection) {
			if (!GUI.channelsToProcess.contains(chan)) {
				this.defMinsMaxes.put(chan, new Integer[] {imageCurrentlyROIEditing.getContainer().getMin(chan), imageCurrentlyROIEditing.getContainer().getMax(chan)});
			}
		}
				
		this.gui.getBrightnessAdjuster().setValues(this.defMinsMaxes.get(GUI.channelForROIDraw)[0], this.defMinsMaxes.get(GUI.channelForROIDraw)[1]/*(int) Math.pow(2, ip.getBitDepth())*/,this.imageCurrentlyROIEditing.getContainer().getMin(GUI.channelForROIDraw), 
				this.imageCurrentlyROIEditing.getContainer().getMax(GUI.channelForROIDraw));
		setDisplayState(STATE_COUNT_DIST, null);
		return true;
	}

	public void setImagesForSliceSelection(List<ProspectiveImage> images) {
		this.imagesForSliceSelection = images;
	}
	
	public void setImagesForObjSelection(List<ProspectiveImage> images) {
		
		if (this.gui.getWizard().getStatus() != Status.SELECT_FILES) {
			this.imagesForObjectAnalysis.clear();;
			this.imagesForObjectSelection = images;
		}
	}

	public void startSliceSelecting() {
		if (this.gui.getWizard().getStatus() != Status.SELECT_FILES)
			displayNextInfoImage(-1,-1);
	}
	
	public void startProcessingImageObjects() {
		
		if (this.gui.getWizard().getStatus() != Status.SELECT_FILES) {
			this.neuronProcessor = new NeuronProcessor(this.imagesForObjectAnalysis, this.gui, this.gui.getProgressReporter(),this.gui.getWizard(), new HashSet<Channel>(GUI.channelsToProcess));
			this.neuronProcessor.run();
		}
		
	}
	
	public void startImageObjectSelecting() {
		if (this.gui.getWizard().getStatus() != Status.SELECT_FILES)
			displayNextObjImage();
	}
	
	public void startImageROISelecting() {
		
		if (this.gui.getWizard().getStatus() != Status.SELECT_FILES) {
			displayNextROISelectImage();
		}
		
	}
	
	public int getState() {
		return this.currentState;
	}
	
	public void setDisplayState(int state, String disabledMsg) {
		
		this.currentState = state;
		switch (state) {
		case STATE_DISABLED:
			if (disabledMsg != null) 
				this.lblDisabled.setText(disabledMsg);
			else
				this.lblDisabled.setText("<html><body><p style='width: 100px; text-align: center;'>Please select images using the interface above.</p></body></html>");
			setLayout(false, -1);
			this.infoLblERR.setVisible(false);
			this.infoTxtDisplaying.setText("");
			this.infoTxtHighSlice.setText("");
			this.infoTxtLowSlice.setText("");
			break;
		case STATE_INFO:
			this.infoLblERR.setVisible(false);
			setLayout(true, LAYOUT_TYPE_INFO);
			break;
		case STATE_OBJ:
			setLayout(true, LAYOUT_TYPE_OBJ);
			break;
		case STATE_COUNT_DIST:
			setLayout(true, LAYOUT_TYPE_COUNT_DIST);
			break;
		default:
			throw new IllegalStateException();
		}

	}
	
	private void setLayout(boolean enabled, int type) {
		
		this.rawPanel.removeAll();
		
		if (enabled) {
			this.rawPanel.setBackground(colorEnabled);

			switch (type) {
			case LAYOUT_TYPE_INFO:
				GroupLayout gl_pnlInfo = new GroupLayout(this.rawPanel);
				gl_pnlInfo.setHorizontalGroup(
						gl_pnlInfo.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlInfo.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_pnlInfo.createParallelGroup(Alignment.TRAILING)
										.addGroup(gl_pnlInfo.createSequentialGroup()
												.addComponent(infoLblCurrDisp)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(infoTxtDisplaying, GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE))
										.addGroup(Alignment.LEADING, gl_pnlInfo.createSequentialGroup()
												.addComponent(infoLblSelectSlices)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(infoTxtLowSlice, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(infoLblThru)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(infoTxtHighSlice, GroupLayout.PREFERRED_SIZE, 54, GroupLayout.PREFERRED_SIZE))
										.addGroup(gl_pnlInfo.createSequentialGroup()
												.addComponent(infoLblERR, GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
												.addPreferredGap(ComponentPlacement.RELATED)
												.addComponent(infoBtnNext)))
								.addContainerGap())
						);
				gl_pnlInfo.setVerticalGroup(
						gl_pnlInfo.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlInfo.createSequentialGroup()
								.addContainerGap()
								.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
										.addComponent(infoTxtDisplaying, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(infoLblCurrDisp))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_pnlInfo.createParallelGroup(Alignment.BASELINE)
										.addComponent(infoLblSelectSlices)
										.addComponent(infoTxtLowSlice, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
										.addComponent(infoLblThru)
										.addComponent(infoTxtHighSlice, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
								.addPreferredGap(ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
								.addGroup(gl_pnlInfo.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(infoLblERR, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(infoBtnNext, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
									.addComponent(objLblRemove, GroupLayout.PREFERRED_SIZE, 70, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(objTxtRemove, GroupLayout.PREFERRED_SIZE, 59, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(objBtnRemove, GroupLayout.PREFERRED_SIZE, 78, GroupLayout.PREFERRED_SIZE))
								.addComponent(objBtnNext, Alignment.TRAILING))
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
								.addComponent(objBtnRemove))
							.addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
							.addComponent(objBtnNext)
							.addContainerGap())
				);
				this.rawPanel.setLayout(gl_objPnl);
				break;
			case LAYOUT_TYPE_COUNT_DIST:
				GroupLayout gl_pnlDist = new GroupLayout(this.rawPanel);
				gl_pnlDist.setHorizontalGroup(
					gl_pnlDist.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_pnlDist.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_pnlDist.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_pnlDist.createSequentialGroup()
									.addComponent(distLblCurrDisp)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(distTxtCurrDisp, GroupLayout.DEFAULT_SIZE, 247, Short.MAX_VALUE))
								.addGroup(gl_pnlDist.createSequentialGroup()
									.addComponent(distLblInstruction)
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(distBtnHelp, GroupLayout.PREFERRED_SIZE, 22, GroupLayout.PREFERRED_SIZE))
								.addGroup(gl_pnlDist.createSequentialGroup()
									.addGroup(gl_pnlDist.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(distBtnAddROI, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
										.addGroup(Alignment.LEADING, gl_pnlDist.createParallelGroup(Alignment.TRAILING, false)
											.addComponent(distBtnDeleteROI, Alignment.LEADING, 0, 0, Short.MAX_VALUE)
											.addComponent(distBtnCancelROI, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 43, Short.MAX_VALUE)))
									.addPreferredGap(ComponentPlacement.RELATED)
									.addComponent(distSP, GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE)
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
								.addComponent(distLblCurrDisp))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_pnlDist.createParallelGroup(Alignment.LEADING, false)
								.addComponent(distBtnHelp, 0, 0, Short.MAX_VALUE)
								.addComponent(distLblInstruction, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(gl_pnlDist.createParallelGroup(Alignment.LEADING, false)
								.addComponent(distSP, 0, 0, Short.MAX_VALUE)
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
			this.rawPanel.setBackground(colorDisabled);
			this.rawPanel.setLayout(new BorderLayout(0,0));
			this.rawPanel.add(lblDisabled, BorderLayout.CENTER);
		}

	}

	public void sliderSliceChanged(int slice) {
		if(imageCurrentlyDisplayed != null)
		{
			this.gui.getPanelDisplay().setImage(imageCurrentlyDisplayed.getImage(gui.getPanelDisplay().getSliderSelectedChannel(), slice, false));
		}
	}

	public void sliderChanChanged(Channel chan) {
		if (currentState == STATE_INFO) {
			if(imageCurrentlyDisplayed != null)
			{
				this.gui.getPanelDisplay().setImage(imageCurrentlyDisplayed.getImage(chan, gui.getPanelDisplay().getSliderSelectedSlice(), false).getBufferedImage());
			}

		} else if (currentState == STATE_OBJ) {
			if(imageCurrentlyObjEditing != null)
			{
				this.gui.getPanelDisplay().setImage(imageCurrentlyObjEditing.getImgWithDots(chan).getBufferedImage());
			}
		} else if (currentState == STATE_COUNT_DIST) {
			if (imageCurrentlyROIEditing != null) {
				if (!GUI.channelsToProcess.contains(chan)) {
					Integer[] minMax = this.defMinsMaxes.get(chan);
					this.gui.getBrightnessAdjuster().setValues(minMax[0], minMax[1], this.imageCurrentlyROIEditing.getContainer().getMin(chan), this.imageCurrentlyROIEditing.getContainer().getMax(chan));
					this.gui.getPanelDisplay().setImage(this.imageCurrentlyROIEditing.getPaintedCopy(chan));
				} else {
					this.gui.getBrightnessAdjuster().reset();
					this.gui.getPanelDisplay().setImage(this.imageCurrentlyROIEditing.getPaintedCopy(chan));
				}
			}
		}
	}
	
	public void cancelNeuronProcessing() {
		this.imageCurrentlyDisplayed = null;
		this.imageCurrentlyObjEditing = null;
		this.imageCurrentlyROIEditing = null;
		if (this.imagesForObjectAnalysis != null) {
			this.imagesForObjectAnalysis.clear();
		}
		if (this.imagesForObjectSelection != null) {
			this.imagesForObjectSelection.clear();
		}
		
		if (this.imagesForROICreation != null) {
			this.imagesForROICreation.clear();
		}
		
		if (this.imagesForSliceSelection != null) {
			this.imagesForSliceSelection.clear();
		}
		if (this.neuronProcessor != null) {
			this.neuronProcessor.cancelProcessing();
		}
	}
	
	public void mouseClickOnImage(Point p) {
		
		if (currentState == STATE_OBJ) {
			
			if (p != null) {
				imageCurrentlyObjEditing.addPoint(gui.getPanelDisplay().getSliderSelectedChannel(), p);

			}
		} else if (currentState == STATE_COUNT_DIST) {
			
			if (p != null && imageCurrentlyROIEditing != null) {
				imageCurrentlyROIEditing.addPoint(p);
				gui.getPanelDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(gui.getPanelDisplay().getSliderSelectedChannel()));
			}

		}		
	}

	public void adjustMinMax(int min, int max) {
		if (this.imageCurrentlyROIEditing == null) {
			return;
		}
		Channel chan = gui.getPanelDisplay().getSliderSelectedChannel();
		this.imageCurrentlyROIEditing.getContainer().applyMinMax(chan, min, max);
		gui.getPanelDisplay().setImage(imageCurrentlyROIEditing.getPaintedCopy(chan));
	}
	
}
