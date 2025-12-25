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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager;
import com.typicalprojects.TronMachine.neuronal_migration.ChannelManager.Channel;
import com.typicalprojects.TronMachine.neuronal_migration.GUI;
import com.typicalprojects.TronMachine.ui.ModernCardPanel;

/**
 * Panel displaying channel information (name, abbreviation, and color).
 * Shown below the image renderer in the display panel.
 * 
 * @author Justin Carrington
 */
public class PnlChannelInfo {

	private JPanel rawPanel;
	private JPanel channelListPanel;
	private ChannelManager channelManager;
	private PnlDisplay pnlDisplay;

	public PnlChannelInfo(ChannelManager cm, PnlDisplay display) {
		this.channelManager = cm;
		this.pnlDisplay = display;
		initializePanel();
	}

	private void initializePanel() {
		rawPanel = new JPanel();
		rawPanel.setBackground(GUI.colorPnlEnabled);
		rawPanel.setLayout(new BorderLayout(10, 10));
		rawPanel.setBorder(BorderFactory.createLineBorder(new Color(58, 100, 160), 1));

		// Title
		JLabel lblTitle = new JLabel("Channels");
		lblTitle.setFont(GUI.smallBoldFont);
		lblTitle.setForeground(new Color(255, 255, 255));
		rawPanel.add(lblTitle, BorderLayout.WEST);

		// Channel list - horizontal layout for 4 channels in one row
		channelListPanel = new JPanel();
		channelListPanel.setBackground(GUI.colorPnlEnabled);
		channelListPanel.setLayout(new BoxLayout(channelListPanel, BoxLayout.X_AXIS));
		rawPanel.add(channelListPanel, BorderLayout.CENTER);

		updateChannels();
	}

	public void updateChannels() {
		channelListPanel.removeAll();

		if (channelManager == null) {
			return;
		}

		for (Channel channel : channelManager.getOrderedChannels()) {
			JPanel channelRow = createChannelRow(channel);
			channelListPanel.add(channelRow);
		}

		channelListPanel.revalidate();
		channelListPanel.repaint();
	}

	private JPanel createChannelRow(Channel channel) {
		JPanel channelItem = new JPanel();
		channelItem.setBackground(GUI.colorPnlEnabled);
		channelItem.setLayout(new BoxLayout(channelItem, BoxLayout.Y_AXIS));
		channelItem.setPreferredSize(new Dimension(60, 40));
		channelItem.setMaximumSize(new Dimension(60, 40));

		// Small clickable image color button
		JPanel imgColorBox = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				Color imgColor = channel.getImgColor();
				if (imgColor != null) {
					g2.setColor(imgColor);
					g2.fillRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 3, 3);
					g2.setColor(new Color(100, 100, 100));
					g2.setStroke(new java.awt.BasicStroke(1));
					g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 3, 3);
				}
				g2.dispose();
			}
		};
		imgColorBox.setBackground(GUI.colorPnlEnabled);
		imgColorBox.setPreferredSize(new Dimension(40, 20));
		imgColorBox.setMaximumSize(new Dimension(40, 20));
		imgColorBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
		imgColorBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color newColor = JColorChooser.showDialog(channelItem, "Choose Image Color for " + channel.getName(), channel.getImgColor());
				if (newColor != null) {
					channel.setImgColor(newColor);
					imgColorBox.repaint();
					if (pnlDisplay != null) {
						pnlDisplay.refreshImageWithNewColors();
					}
				}
			}
		});
		channelItem.add(imgColorBox);

		// Channel name
		JLabel lblChannelInfo = new JLabel(String.valueOf(channel.getAbbrev()));
		lblChannelInfo.setFont(GUI.smallPlainFont);
		lblChannelInfo.setForeground(new Color(255, 255, 255));
		lblChannelInfo.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		channelItem.add(lblChannelInfo);

		return channelItem;
	}

	public JPanel getRawPanel() {
		return rawPanel;
	}

	public void setChannelManager(ChannelManager cm) {
		this.channelManager = cm;
		updateChannels();
	}

	public void setPnlDisplay(PnlDisplay display) {
		this.pnlDisplay = display;
	}
}
