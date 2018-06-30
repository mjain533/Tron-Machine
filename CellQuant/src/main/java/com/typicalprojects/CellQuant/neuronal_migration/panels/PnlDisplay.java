package com.typicalprojects.CellQuant.neuronal_migration.panels;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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

import com.typicalprojects.CellQuant.neuronal_migration.GUI;
import com.typicalprojects.CellQuant.neuronal_migration.Wizard.Status;
import com.typicalprojects.CellQuant.neuronal_migration.processing.ObjectEditableImage;
import com.typicalprojects.CellQuant.util.ImagePanel;
import com.typicalprojects.CellQuant.util.Point;
import com.typicalprojects.CellQuant.util.Zoom;
import com.typicalprojects.CellQuant.util.ImageContainer.Channel;

import ij.ImagePlus;

public class PnlDisplay  {




	private JLabel lblDisabled;
	private Color colorDisabled = new Color(169, 169, 169);
	private Color colorEnabled = new Color(220, 220, 220);

	private JPanel rawPanel;	



	private ImagePanel pnlImage;

	private JSlider sldrSlice;
	private JSlider sldrChan;
	private List<Channel> chanSelection = null;
	private JLabel lblChanNum;
	private JLabel lblSliceNum;
	private JLabel lblChanSlider;
	private JPanel pnlSliderChan;
	private JComponent pnlSliderSlice;

	private PnlDisplayFeedbackReceiver outputHandler;
	private boolean changing = false;

	private boolean shiftPressed = false;

	public PnlDisplay(GUI gui, PnlDisplayFeedbackReceiver sliderOutputHandler) {
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
				lblSliceNum.setText(slice + "");
				outputHandler.sliderSliceChanged(getSliderSelectedSlice());
			}
		});
		this.sldrChan.addChangeListener(new ChangeListener() {

			public void stateChanged(ChangeEvent e) {
				if (changing) return;

				Channel chan = getSliderSelectedChannel();
				lblChanNum.setText(chan.getAbbreviation());
				outputHandler.sliderChanChanged(chan);


			}
		});
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {

			@Override
			public boolean dispatchKeyEvent(KeyEvent ke) {
				synchronized (PnlDisplay.class) {
					switch (ke.getID()) {
					case KeyEvent.KEY_PRESSED:
						if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
							shiftPressed = true;
						}
						break;

					case KeyEvent.KEY_RELEASED:
						if (ke.getKeyCode() == KeyEvent.VK_SHIFT) {
							shiftPressed = false;
						} else if (ke.getKeyCode() == KeyEvent.VK_DOWN || ke.getKeyCode() == KeyEvent.VK_BACK_SPACE || ke.getKeyCode() == KeyEvent.VK_ESCAPE) {
							if (gui.getWizard().getStatus() == Status.SELECT_OB) {
								ObjectEditableImage oei = gui.getPanelOptions().getObjectEditableImage();
								if (oei != null) {
									if (!oei.getZoom().equals(Zoom.ZOOM_100)) {
										Zoom zoom = oei.getPreviousZoomLevel();
										oei.setZoom(zoom);
										pnlImage.setImage(oei.getImgWithDots(getSliderSelectedChannel()).getBufferedImage(), -1, -1, zoom);

									}

								}
							}
						} else if (ke.getKeyCode() == KeyEvent.VK_SPACE)
						break;
					}
					return false;
				}
			}
		});


		this.pnlImage.addMouseListener(new MouseListener() {

			public void mouseClicked(MouseEvent e) {


			}

			public void mousePressed(MouseEvent e) {	
			}

			public void mouseReleased(MouseEvent e) {
				if (gui.getWizard().getStatus() == Status.SELECT_OB && shiftPressed) {
					ObjectEditableImage oei = gui.getPanelOptions().getObjectEditableImage();
					if (oei != null) {
						if (SwingUtilities.isRightMouseButton(e)) {
							if (!oei.getZoom().equals(Zoom.ZOOM_100)) {
								Zoom zoom = oei.getPreviousZoomLevel();
								oei.setZoom(zoom);
								pnlImage.setImage(pnlImage.getImage(), -1, -1, zoom);
							}
						} else {
							Zoom zoom = oei.getNextZoom();

							if (zoom != null) {

								oei.setZoom(zoom);

								pnlImage.setImage(oei.getImgWithDots(getSliderSelectedChannel()).getBufferedImage(), e.getX(), e.getY(), zoom);
							}
						}


					}
				} else {
					int x = e.getX();
					int y = e.getY();
					Point p = pnlImage.getPixelPoint(x, y);
					outputHandler.mouseClickOnImage(p);
				}
			}

			public void mouseEntered(MouseEvent e) {
				pnlImage.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			}

			public void mouseExited(MouseEvent e) {	
				pnlImage.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			}

			
		});

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
			this.lblChanNum.setText(chans.get(0).getAbbreviation());
			this.sldrChan.setEnabled(true);

		} else {
			this.chanSelection = null;
			this.sldrChan.setMinimum(1);
			this.sldrChan.setMaximum(2);
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
