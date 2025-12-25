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
import java.util.List;
import javax.swing.GroupLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.BevelBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager.Channel;
import com.typicalprojects.TronMachine.util.ImageContainer;
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
	private PnlChannelInfo pnlChannelInfo;

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

		// Initialize channel info panel
		pnlChannelInfo = new PnlChannelInfo(null, this);

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
				ImagePlus ip = outputHandler.sliderSliceChanged(getSliderSelectedPage(), getSliderSelectedSlice());
				if (ip != null) {
					pnlImage.setImage(ip, true);
				}
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
				Object[] feedback = outputHandler.sliderPageChanged(page, getSliderSelectedSlice());
				if (feedback[0] != null) {
					if (feedback.length > 2 && feedback[2] != null && feedback[2] instanceof Boolean && (boolean) feedback[2]) {
						pnlImage.setScaleBar(ImageContainer.getInfernoLUT(), "No Overlap", "Max Overlap");
						pnlImage.setImage((ImagePlus) feedback[0], (boolean) feedback[1]);

					} else {
						pnlImage.setScaleBar(null, null, null);
						pnlImage.setImage((ImagePlus) feedback[0], (boolean) feedback[1]);

					}
				}

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
						case KeyEvent.VK_0:
						case KeyEvent.VK_1:
						case KeyEvent.VK_2:
						case KeyEvent.VK_3:
						case KeyEvent.VK_4:
						case KeyEvent.VK_5:
						case KeyEvent.VK_6:
						case KeyEvent.VK_7:
						case KeyEvent.VK_8:
						case KeyEvent.VK_9:
							outputHandler.keyboardCharPressed(ke.getKeyChar());
							break;
						case KeyEvent.VK_SHIFT:

							java.awt.Point javaPoint = MouseInfo.getPointerInfo().getLocation();
							SwingUtilities.convertPointFromScreen(javaPoint, pnlImage);
							
							if (pnlImage.screenPositionIsInImage(javaPoint.x, javaPoint.y) && gui.getComponent().isFocused()) {
								ke.consume();
								
								pnlImage.zoomIn(javaPoint.x, javaPoint.y, true);
								ImagePlus ip = outputHandler.zoomChanged(getSliderSelectedPage(), pnlImage.getZoom());
								if (ip != null) {
									pnlImage.setImage(ip, true);
								}
								pnlImage.repaint();
								
							}
							
							break;
						case KeyEvent.VK_SPACE:
						case KeyEvent.VK_ESCAPE:
							
							javaPoint = MouseInfo.getPointerInfo().getLocation();
							SwingUtilities.convertPointFromScreen(javaPoint, pnlImage);
							if (!rawPanel.contains(javaPoint) || !gui.getComponent().isFocused()) {
								break;
							}
							
							ke.consume();
							pnlImage.zoomOut(false, javaPoint.x, javaPoint.y, true);
							ImagePlus ip = outputHandler.zoomChanged(getSliderSelectedPage(), pnlImage.getZoom());
							if (ip != null) {
								pnlImage.setImage(ip, true);
							}
							pnlImage.repaint();

							break;
						case KeyEvent.VK_R:
						case KeyEvent.VK_E:
							ke.consume();
							outputHandler.keyboardCharPressed(ke.getKeyChar());
							break;
						case KeyEvent.VK_A:

							if (isMouseWithinImgPanel()) {
								getImagePanel().grabFocus();
								ke.consume();
								getImagePanel().shiftImage(1);
							}
							break;
						case KeyEvent.VK_W:
							if (isMouseWithinImgPanel()) {

								ke.consume();
								getImagePanel().grabFocus();
								getImagePanel().shiftImage(2);
							}
							break;
						case KeyEvent.VK_S:
							if (isMouseWithinImgPanel()) {

								ke.consume();
								getImagePanel().grabFocus();
								getImagePanel().shiftImage(4);
							}
							break;
						case KeyEvent.VK_D:
							if (isMouseWithinImgPanel()) {

								ke.consume();
								getImagePanel().grabFocus();
								getImagePanel().shiftImage(3);
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
				Point p = pnlImage.getPixelPoint(e.getX(), e.getY());
				outputHandler.mouseClickOnImage(p, getSliderSelectedPage(), getSliderSelectedSlice(), !SwingUtilities.isRightMouseButton(e));

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
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
									.addComponent(pnlImage, GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE)
									.addComponent(pnlChannelInfo.getRawPanel(), GroupLayout.DEFAULT_SIZE, 341, Short.MAX_VALUE))
							.addContainerGap())
					);
			gl_panel.setVerticalGroup(
					gl_panel.createParallelGroup(Alignment.TRAILING)
					.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
							.addContainerGap()
							.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
									.addGroup(gl_panel.createSequentialGroup()
											.addComponent(pnlImage, GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(pnlChannelInfo.getRawPanel(), GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE))
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

	public void repaintImage() {
		if (pnlImage != null) {
			pnlImage.repaint();
		}
	}

	public void refreshImageWithNewColors() {
		if (outputHandler != null && pnlImage != null) {
			Object[] feedback = outputHandler.sliderPageChanged(getSliderSelectedPage(), getSliderSelectedSlice());
			if (feedback != null && feedback.length > 0) {
				pnlImage.setImage((ImagePlus) feedback[0], (boolean) feedback[1]);
				pnlImage.repaint();
			}
		}
	}
	
	public void disableSliceSlider(boolean keepSliceBounds) {
		changing = true;
		if (!keepSliceBounds) {
			this.sldrSlice.setMinimum(0);
			this.sldrSlice.setMaximum(1);
			this.sldrSlice.setValue(0);
			this.lblSliceNum.setText("--");
		} else {
			this.lblSliceNum.setText("--");
		}
		this.sldrSlice.setEnabled(false);
		this.lastSelectedSlice = this.sldrSlice.getValue();
		changing = false;
	}

	public void enableSliceSlider(int min, int max, int value, boolean usePreviousValues) {
		
		if (min < 1 || max <= min) {
			throw new IllegalArgumentException();
		}
		changing = true;
		
		if (usePreviousValues && this.sldrSlice.getMinimum() != 0) {
			this.lblSliceNum.setText(this.sldrSlice.getValue() + "");
		} else {
			this.sldrSlice.setMinimum(min);
			this.sldrSlice.setMaximum(max);
			this.sldrSlice.setValue(value);
			this.lblSliceNum.setText(value + "");

		}
		this.sldrSlice.setEnabled(true);
		this.sldrSlice.setMinorTickSpacing(1);
		this.sldrSlice.setSnapToTicks(true);
		this.lastSelectedSlice = this.sldrSlice.getValue();
		changing = false;


	}
	
	public void setSelectedPage(int page) {
		page = page - 1;
		if (page <= this.sldrPage.getMaximum() || page >= this.sldrPage.getMinimum()) {
			this.sldrPage.setValue(page);
		}
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

	public void setChannelManager(ChannelManager cm) {
		if (pnlChannelInfo != null) {
			pnlChannelInfo.setChannelManager(cm);
		}
	}
	
	public interface PnlDisplayPage {
		
		public String getDisplayAbbrev();
		
	}

	public interface PnlDisplayFeedbackReceiver {

		public ImagePlus sliderSliceChanged(PnlDisplayPage chan, int slice);

		public Object[] sliderPageChanged(PnlDisplayPage chan, int slice);

		public void mouseClickOnImage(Point p, PnlDisplayPage displayPage, int slice, boolean wasLeftClick);
		
		public ImagePlus zoomChanged(PnlDisplayPage dspPage, Zoom newZoom);
		
		public void keyboardCharPressed(char pressed);
		
	}


}
