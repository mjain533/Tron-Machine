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
import java.awt.Cursor;
import java.awt.Image;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager.Channel;
import com.typicalprojects.TronMachine.neuronal_migration.Wizard.Status;
import com.typicalprojects.TronMachine.neuronal_migration.processing.ObjectEditableImage;
import com.typicalprojects.TronMachine.util.FileBrowser;
import com.typicalprojects.TronMachine.util.ImagePanel;
import com.typicalprojects.TronMachine.util.Point;
import com.typicalprojects.TronMachine.util.Toolbox;
import com.typicalprojects.TronMachine.util.Zoom;

import ij.ImagePlus;

public class PnlDisplay  {

	private JLabel lblDisabled;
	public static Color colorDisabled = new Color(169, 169, 169);
	public static Color colorEnabled = new Color(220, 220, 220);

	private JPanel rawPanel;	
	private Cursor cursor;

	private ImagePanel pnlImage;

	private JSlider sldrSlice;
	private JSlider sldrChan;
	private List<Channel> chanSelection = null;
	private JLabel lblChanNum;
	private JLabel lblSliceNum;
	private JLabel lblChanSlider;
	private JPanel pnlSliderChan;
	private JComponent pnlSliderSlice;
	private int lastSelectedSlice;
	private Channel lastSelectedChan;

	private PnlDisplayFeedbackReceiver outputHandler;
	private boolean changing = false;


	public PnlDisplay(GUI gui, PnlDisplayFeedbackReceiver sliderOutputHandler) {
		

		cursor = Toolbox.createCursor();

		
		
		this.outputHandler = sliderOutputHandler;
		rawPanel = new JPanel();
		rawPanel.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));


		lblDisabled = new JLabel("<html><body><p style='width: 150px; text-align: center;'>Please select images using the interface to the left.</p></body></html>");
		lblDisabled.setHorizontalAlignment(SwingConstants.CENTER);


		pnlSliderSlice = new JPanel();
		pnlSliderSlice.setBackground(colorEnabled);
		pnlSliderSlice.setLayout(null);

		JLabel lblSliceSlider = new JLabel("Slice");
		lblSliceSlider.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSliceSlider.setHorizontalAlignment(SwingConstants.CENTER);
		lblSliceSlider.setBounds(0, 0, 35, 25);
		pnlSliderSlice.add(lblSliceSlider);

		sldrSlice = new JSlider();
		sldrSlice.setInverted(true);
		sldrSlice.setOrientation(SwingConstants.VERTICAL);
		sldrSlice.setBounds(0, 23, 35, 190);
		pnlSliderSlice.add(sldrSlice);
		sldrSlice.setFocusable(false);
		sldrSlice.setBackground(colorEnabled);


		lblSliceNum = new JLabel("10");
		lblSliceNum.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSliceNum.setHorizontalAlignment(SwingConstants.CENTER);
		lblSliceNum.setBounds(0, 225, 35, 19);
		pnlSliderSlice.add(lblSliceNum);




		pnlSliderChan = new JPanel();
		pnlSliderChan.setBackground(colorEnabled);
		pnlSliderChan.setLayout(null);

		lblChanSlider = new JLabel("Chan");
		lblChanSlider.setHorizontalTextPosition(SwingConstants.CENTER);
		lblChanSlider.setHorizontalAlignment(SwingConstants.CENTER);
		lblChanSlider.setBounds(0, 0, 35, 25);
		pnlSliderChan.add(lblChanSlider);

		sldrChan = new JSlider();
		sldrChan.setBounds(0, 23, 35, 190);
		sldrChan.setInverted(true);
		sldrChan.setOrientation(SwingConstants.VERTICAL);
		sldrChan.setFocusable(false);
		sldrChan.setBackground(colorEnabled);
		pnlSliderChan.add(sldrChan);

		lblChanNum = new JLabel("10");
		lblChanNum.setHorizontalTextPosition(SwingConstants.CENTER);
		lblChanNum.setHorizontalAlignment(SwingConstants.CENTER);
		lblChanNum.setBounds(0, 225, 35, 19);

		pnlSliderChan.add(lblChanNum);

		pnlImage = new ImagePanel();
		pnlImage.setBackground(colorEnabled);
		//pnlImage.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));





		this.sldrSlice.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {

				if (changing) return;


				int slice = getSliderSelectedSlice();
				if (slice == lastSelectedSlice)
					return;

				lastSelectedSlice = slice;
				lblSliceNum.setText(slice + "");
				outputHandler.sliderSliceChanged(getSliderSelectedSlice());
			}
		});
		this.sldrChan.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				if (changing) return;

				Channel chan = getSliderSelectedChannel();
				if (chan == lastSelectedChan)
					return;

				lastSelectedChan = chan;
				lblChanNum.setText(chan.getAbbrev() + "");
				outputHandler.sliderChanChanged(chan);


			}
		});
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent ke) {

				synchronized (PnlDisplay.class) {
					
					switch (ke.getID()) {
					case KeyEvent.KEY_RELEASED:

						break;

					case KeyEvent.KEY_PRESSED:
						switch (ke.getKeyCode()) {
						case KeyEvent.VK_SHIFT:

							if (gui.getWizard().getStatus() == Status.SELECT_OB) {
								ObjectEditableImage oei = gui.getPanelOptions().getObjectEditableImage();
								if (oei == null)
									break;
								if (!oei.getRunConfig().channelMan.isProcessChannel(getSliderSelectedChannel()))
									break;

								java.awt.Point javaPoint = MouseInfo.getPointerInfo().getLocation();
								SwingUtilities.convertPointFromScreen(javaPoint, pnlImage);
								//if  (javaPoint.x < 0 || javaPoint.x >= pnlImage.getWidth() || javaPoint.y < 0 || javaPoint.y >= pnlImage.getHeight()) {
								//	break;
								//}
								Zoom zoom = oei.getNextZoom();

								if (zoom != null) {

									if (pnlImage.screenPositionIsInImage(javaPoint.x, javaPoint.y)) {

										oei.setZoom(zoom);

										pnlImage.setImage(oei.getImgWithDots(getSliderSelectedChannel()).getBufferedImage(), javaPoint.x, javaPoint.y, zoom);

									}
								}

							}
							break;
						case KeyEvent.VK_SPACE:
						case KeyEvent.VK_ESCAPE:
							if (gui.getWizard().getStatus() == Status.SELECT_OB) {
								ObjectEditableImage oei = gui.getPanelOptions().getObjectEditableImage();
								if (oei != null) {
									if (!oei.getRunConfig().channelMan.isProcessChannel(getSliderSelectedChannel()))
										break;
									java.awt.Point javaPoint = MouseInfo.getPointerInfo().getLocation();
									SwingUtilities.convertPointFromScreen(javaPoint, pnlImage);
									getImagePanel().grabFocus();
									ke.consume();
									if (!oei.getZoom().equals(Zoom.ZOOM_100)) {
										Zoom zoom = oei.getPreviousZoomLevel();
										oei.setZoom(zoom);
										if (pnlImage.screenPositionIsInImage(javaPoint.x, javaPoint.y)) {
											pnlImage.setImage(oei.getImgWithDots(getSliderSelectedChannel()).getBufferedImage(), javaPoint.x, javaPoint.y, zoom);
										} else {
											pnlImage.setImage(oei.getImgWithDots(getSliderSelectedChannel()).getBufferedImage(), -1, -1, zoom);
										}

									}

								}
							}
							break;
						case KeyEvent.VK_A:

							if (gui.getWizard().getStatus() == Status.SELECT_ROI) {
								ke.consume();
								gui.getPanelOptions().triggerROIAddButton();
							} else if (gui.getWizard().getStatus() == Status.SELECT_OB && gui.getPanelOptions().getObjectEditableImage() != null) {
								if (isMouseWithinImgPanel()) {
									getImagePanel().grabFocus();
									ke.consume();
									getImagePanel().shiftImage(1);
								}

							}
							break;
						case KeyEvent.VK_W:
							if (gui.getWizard().getStatus() == Status.SELECT_OB && gui.getPanelOptions().getObjectEditableImage() != null) {
								if (isMouseWithinImgPanel()) {

									ke.consume();
									getImagePanel().grabFocus();

									getImagePanel().shiftImage(2);
								}
							}
							break;
						case KeyEvent.VK_S:
							if (gui.getWizard().getStatus() == Status.SELECT_OB && gui.getPanelOptions().getObjectEditableImage() != null) {
								if (isMouseWithinImgPanel()) {

									ke.consume();
									getImagePanel().grabFocus();
									getImagePanel().shiftImage(4);
								}
							}
							break;
						case KeyEvent.VK_D:
							if (gui.getWizard().getStatus() == Status.SELECT_OB && gui.getPanelOptions().getObjectEditableImage() != null) {
								if (isMouseWithinImgPanel()) {

									ke.consume();
									getImagePanel().grabFocus();
									getImagePanel().shiftImage(3);
								}
							}
							break;
						default:
							break;
						}
						break;

					}
					return false;
				}
			}
		});

		this.pnlImage.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseDragged(MouseEvent e) {
				
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				if (e.getX() >= 0 && e.getX() < pnlImage.getWidth() && 
						e.getY() >= 0 && e.getY() < pnlImage.getHeight()) {
					pnlImage.setCursor(cursor);
				}/* else {
					pnlImage.setCursor(Cursor.getDefaultCursor());
				}*/
			}
			
		});
		this.pnlImage.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {
				pnlImage.setCursor(cursor);

			}

			public void mousePressed(MouseEvent e) {	
				pnlImage.setCursor(cursor);

			}

			public void mouseReleased(MouseEvent e) {
				pnlImage.setCursor(cursor);

				if (gui.getWizard().getStatus() == Status.SELECT_OB) {
					if (SwingUtilities.isRightMouseButton(e)){

						ObjectEditableImage oei = gui.getPanelOptions().getObjectEditableImage();
						
						if (oei != null) {
							Channel chan = getSliderSelectedChannel();
							if (oei.getRunConfig().channelMan.isProcessChannel(chan)) {
								Point p = pnlImage.getPixelPoint(e.getX(), e.getY());
								Point closest = oei.getNearestPoint(getSliderSelectedChannel(), p);
								if (closest != null) {
									oei.removePoint(getSliderSelectedChannel(), closest);
								}
							}
							
						}

					} else {
						int x = e.getX();
						int y = e.getY();
						Point p = pnlImage.getPixelPoint(x, y);
						outputHandler.mouseClickOnImage(p);
					}

				} else if (gui.getWizard().getStatus() == Status.SELECT_ROI) {
					int x = e.getX();
					int y = e.getY();
					Point p = pnlImage.getPixelPoint(x, y);
					outputHandler.mouseClickOnImage(p);
				}
			}

			public void mouseEntered(MouseEvent e) {
				pnlImage.setCursor(cursor);
			}

			public void mouseExited(MouseEvent e) {	
				pnlImage.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}


		});

	}
	
	public boolean isMouseWithinImgPanel() {
	    java.awt.Point mousePos = MouseInfo.getPointerInfo().getLocation();
	    Rectangle bounds = this.pnlImage.getBounds();
	    bounds.setLocation(this.pnlImage.getLocationOnScreen());
	    return bounds.contains(mousePos);
	}

	public void setDisplayState(boolean enabled, String disabledMessage) {

		this.rawPanel.removeAll();
		if (enabled) {
			this.rawPanel.setBackground(colorEnabled);
			GroupLayout gl_panel = new GroupLayout(this.rawPanel);
			gl_panel.setHorizontalGroup(
					gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
									.addComponent(pnlSliderChan, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
									.addComponent(pnlSliderSlice, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(pnlImage, GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
							.addContainerGap())
					);
			gl_panel.setVerticalGroup(
					gl_panel.createParallelGroup(Alignment.TRAILING)
					.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
									.addComponent(pnlImage, GroupLayout.DEFAULT_SIZE, 589, Short.MAX_VALUE)
									.addGroup(gl_panel.createSequentialGroup()
											.addComponent(pnlSliderSlice, GroupLayout.PREFERRED_SIZE, 247, GroupLayout.PREFERRED_SIZE)
											.addPreferredGap(ComponentPlacement.RELATED, 95, Short.MAX_VALUE)
											.addComponent(pnlSliderChan, GroupLayout.PREFERRED_SIZE, 247, GroupLayout.PREFERRED_SIZE)))
							.addContainerGap())
					);

			this.rawPanel.setLayout(gl_panel);

		} else {
			if (disabledMessage != null) 
				this.lblDisabled.setText(disabledMessage);
			else
				this.lblDisabled.setText("<html><body><p style='width: 100px; text-align: center;'>Please select images using the interface to the left.</p></body></html>");
			this.rawPanel.setBackground(colorDisabled);
			this.rawPanel.updateUI();
			this.rawPanel.setLayout(new BorderLayout(0,0));
			this.rawPanel.add(lblDisabled, BorderLayout.CENTER);
		}
	}

	public JPanel getRawPanel() {
		return this.rawPanel;
	}

	public void setSliceSlider(boolean enabled, int min, int max) {
		changing = true;
		if (enabled) {
			this.sldrSlice.setMinimum(min);
			this.sldrSlice.setMaximum(max);
			this.sldrSlice.setValue(min);
			this.lblSliceNum.setText(min + "");
			this.sldrSlice.setEnabled(true);

		} else {
			this.sldrSlice.setMinimum(1);
			this.sldrSlice.setMaximum(2);
			this.sldrSlice.setValue(1);
			this.lblSliceNum.setText("--");
			this.sldrSlice.setEnabled(false);
		}
		this.lastSelectedSlice = this.sldrSlice.getValue();

		this.sldrSlice.setMinorTickSpacing(1);
		this.sldrSlice.setSnapToTicks(true);
		changing = false;
	}

	public void setSliceSlider(boolean enabled, int min, int max, int value) {
		changing = true;
		if (enabled) {
			this.sldrSlice.setMinimum(min);
			this.sldrSlice.setMaximum(max);
			this.sldrSlice.setValue(value);
			this.lblSliceNum.setText(min + "");
			this.sldrSlice.setEnabled(true);

		} else {
			this.sldrSlice.setMinimum(1);
			this.sldrSlice.setMaximum(2);
			this.sldrSlice.setValue(value);
			this.lblSliceNum.setText("--");
			this.sldrSlice.setEnabled(false);
		}


		this.lastSelectedSlice = this.sldrSlice.getValue();

		this.sldrSlice.updateUI();

		this.sldrSlice.setMinorTickSpacing(1);
		this.sldrSlice.setSnapToTicks(true);
		changing = false;
	}

	public void setChannelSlider(boolean enabled, List<Channel> chans) {
		changing = true;
		if (enabled) {
			this.chanSelection = chans;
			this.sldrChan.setMinimum(0);
			this.sldrChan.setMaximum(chans.size()-1);
			this.sldrChan.setValue(0);
			this.lastSelectedChan = chans.get(0);

			this.lblChanNum.setText(chans.get(0).getAbbrev() + "");
			this.sldrChan.setEnabled(true);

		} else {
			this.chanSelection = null;
			this.sldrChan.setMinimum(1);
			this.sldrChan.setMaximum(2);
			this.lastSelectedChan = null;
			this.sldrChan.setValue(1);
			this.lblChanNum.setText("--");
			this.sldrChan.setEnabled(false);
		}
		this.sldrChan.setMinorTickSpacing(1);
		this.sldrChan.setSnapToTicks(true);
		changing = false;

	}

	public void setImage(ImagePlus image, Zoom zoom, int clickX, int clickY) {
		this.pnlImage.setImage(image.getBufferedImage(), clickX, clickY, zoom);
	}

	public void setImage(BufferedImage image, Zoom zoom, int clickX, int clickY) {
		this.pnlImage.setImage(image, clickX, clickY, zoom);
	}


	public ImagePanel getImagePanel() {
		return this.pnlImage;
	}

	public int getSliderSelectedSlice() {
		return this.sldrSlice.getValue();
	}

	public boolean isSliderSliceEnabled() {
		return this.sldrSlice.isEnabled();
	}

	public Channel getSliderSelectedChannel() {
		if (this.chanSelection == null) return null;
		return this.chanSelection.get(this.sldrChan.getValue()); 
	}

	public interface PnlDisplayFeedbackReceiver {

		public void sliderSliceChanged(int slice);

		public void sliderChanChanged(Channel chan);

		public void mouseClickOnImage(Point p);

	}


}
