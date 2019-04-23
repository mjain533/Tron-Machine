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
import java.awt.Cursor;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.GroupLayout;
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
import com.typicalprojects.TronMachine.util.ImagePanel;
import com.typicalprojects.TronMachine.util.Point;
import com.typicalprojects.TronMachine.util.Toolbox;
import com.typicalprojects.TronMachine.util.Zoom;

import ij.ImagePlus;

/**
 * <p>The panel used to display the image being worked with. The display panel actually has an {@link ImagePanel}
 * contained within, and this is where the image is displayed. The Image Panel will be the size of the image,
 * whereas this Image Panel will dynamically resize as the user resizes the TRON machine window.</p>
 * 
 * <p>The only panel located on the right side of the GUI.</p>
 * 
 * @author Justin Carrington
 */
public class PnlDisplay  {

	private JLabel lblDisabled;

	private JPanel rawPanel;	
	private Cursor cursor;

	private ImagePanel pnlImage;

	private JSlider sldrSlice;
	private JSlider sldrPage;
	private List<? extends PnlDisplayPage> pageOptions = null;
	private JLabel lblPageNum;
	private JLabel lblSliceNum;
	private JLabel lblPageSlider;
	private JPanel pnlSliderPage;
	private JComponent pnlSliderSlice;
	private int lastSelectedSlice;
	private PnlDisplayPage lastSelectedPage;

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
		pnlSliderSlice.setBackground(GUI.colorPnlEnabled);
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
		sldrSlice.setBackground(GUI.colorPnlEnabled);


		lblSliceNum = new JLabel("10");
		lblSliceNum.setHorizontalTextPosition(SwingConstants.CENTER);
		lblSliceNum.setHorizontalAlignment(SwingConstants.CENTER);
		lblSliceNum.setBounds(0, 225, 35, 19);
		pnlSliderSlice.add(lblSliceNum);




		pnlSliderPage = new JPanel();
		pnlSliderPage.setBackground(GUI.colorPnlEnabled);
		pnlSliderPage.setLayout(null);

		lblPageSlider = new JLabel("Chan");
		lblPageSlider.setHorizontalTextPosition(SwingConstants.CENTER);
		lblPageSlider.setHorizontalAlignment(SwingConstants.CENTER);
		lblPageSlider.setBounds(0, 0, 35, 25);
		pnlSliderPage.add(lblPageSlider);

		sldrPage = new JSlider();
		sldrPage.setBounds(0, 23, 35, 190);
		sldrPage.setInverted(true);
		sldrPage.setOrientation(SwingConstants.VERTICAL);
		sldrPage.setFocusable(false);
		sldrPage.setBackground(GUI.colorPnlEnabled);
		pnlSliderPage.add(sldrPage);

		lblPageNum = new JLabel("10");
		lblPageNum.setHorizontalTextPosition(SwingConstants.CENTER);
		lblPageNum.setHorizontalAlignment(SwingConstants.CENTER);
		lblPageNum.setBounds(0, 225, 35, 19);

		pnlSliderPage.add(lblPageNum);

		pnlImage = new ImagePanel();
		pnlImage.setBackground(GUI.colorPnlEnabled);
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
		this.sldrPage.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				if (changing) return;

				PnlDisplayPage page = getSliderSelectedPage();
				if (page == lastSelectedPage)
					return;

				lastSelectedPage = page;
				lblPageNum.setText(page.getDisplayAbbrev() + "");
				outputHandler.sliderPageChanged(page);


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
								
								Channel chan = (Channel) getSliderSelectedPage();
								if (!oei.getRunConfig().channelMan.isProcessChannel(chan))
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

										pnlImage.setImage(oei.getImgWithDots(chan).getBufferedImage(), javaPoint.x, javaPoint.y, zoom);

									}
								}

							}
							break;
						case KeyEvent.VK_SPACE:
						case KeyEvent.VK_ESCAPE:
							if (gui.getWizard().getStatus() == Status.SELECT_OB) {
								ObjectEditableImage oei = gui.getPanelOptions().getObjectEditableImage();
								if (oei != null) {
									Channel chan = (Channel) getSliderSelectedPage();

									if (!oei.getRunConfig().channelMan.isProcessChannel(chan))
										break;
									java.awt.Point javaPoint = MouseInfo.getPointerInfo().getLocation();
									SwingUtilities.convertPointFromScreen(javaPoint, pnlImage);
									getImagePanel().grabFocus();
									ke.consume();
									if (!oei.getZoom().equals(Zoom.ZOOM_100)) {
										Zoom zoom = oei.getPreviousZoomLevel();
										oei.setZoom(zoom);
										if (pnlImage.screenPositionIsInImage(javaPoint.x, javaPoint.y)) {
											pnlImage.setImage(oei.getImgWithDots(chan).getBufferedImage(), javaPoint.x, javaPoint.y, zoom);
										} else {
											pnlImage.setImage(oei.getImgWithDots(chan).getBufferedImage(), -1, -1, zoom);
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
							Channel chan = (Channel) getSliderSelectedPage();
							if (oei.getRunConfig().channelMan.isProcessChannel(chan)) {
								Point p = pnlImage.getPixelPoint(e.getX(), e.getY());
								Point closest = oei.getNearestPoint(chan, p);
								if (closest != null) {
									oei.removePoint(chan, closest);
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
			this.rawPanel.setBackground(GUI.colorPnlEnabled);
			GroupLayout gl_panel = new GroupLayout(this.rawPanel);
			gl_panel.setHorizontalGroup(
					gl_panel.createParallelGroup(Alignment.LEADING)
					.addGroup(gl_panel.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
									.addComponent(pnlSliderPage, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
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
											.addComponent(pnlSliderPage, GroupLayout.PREFERRED_SIZE, 247, GroupLayout.PREFERRED_SIZE)))
							.addContainerGap())
					);

			this.rawPanel.setLayout(gl_panel);

		} else {
			if (disabledMessage != null) 
				this.lblDisabled.setText(disabledMessage);
			else
				this.lblDisabled.setText("<html><body><p style='width: 100px; text-align: center;'>Please select images using the interface to the left.</p></body></html>");
			this.rawPanel.setBackground(GUI.colorPnlDisabled);
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

	public void setPageSlider(boolean enabled, List<? extends PnlDisplayPage> displayPages, String pagesLabel) {
		changing = true;
		this.lblPageSlider.setText(pagesLabel);
		if (enabled) {
			this.pageOptions = displayPages;
			this.sldrPage.setMinimum(0);
			this.sldrPage.setMaximum(displayPages.size()-1);
			this.sldrPage.setValue(0);
			this.lastSelectedPage = displayPages.get(0);

			this.lblPageNum.setText(displayPages.get(0).getDisplayAbbrev() + "");
			this.sldrPage.setEnabled(true);

		} else {
			this.pageOptions = null;
			this.sldrPage.setMinimum(1);
			this.sldrPage.setMaximum(2);
			this.lastSelectedPage = null;
			this.sldrPage.setValue(1);
			this.lblPageNum.setText("--");
			this.sldrPage.setEnabled(false);
		}
		this.sldrPage.setMinorTickSpacing(1);
		this.sldrPage.setSnapToTicks(true);
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

	public PnlDisplayPage getSliderSelectedPage() {
		if (this.pageOptions == null) return null;
		return this.pageOptions.get(this.sldrPage.getValue()); 
	}
	
	public Channel getSliderSelectedPageAsChannel() {
		try {
			return (Channel) getSliderSelectedPage();
		} catch (Exception e) {
			return null;
		}
	}
	
	public interface PnlDisplayPage {
		
		public String getDisplayAbbrev();
		
	}

	public interface PnlDisplayFeedbackReceiver {

		public void sliderSliceChanged(int slice);

		public void sliderPageChanged(PnlDisplayPage chan);

		public void mouseClickOnImage(Point p);

	}


}
