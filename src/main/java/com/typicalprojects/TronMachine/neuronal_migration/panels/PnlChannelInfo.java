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
import javax.swing.ImageIcon;
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
		rawPanel = new ModernCardPanel();
		rawPanel.setLayout(new BorderLayout(15, 10));

		// Left side - Title and channels
		JPanel leftPanel = new JPanel();
		leftPanel.setOpaque(false);
		leftPanel.setLayout(new BorderLayout(10, 5));

		// Title
		JLabel lblTitle = new JLabel("Channels");
		lblTitle.setFont(GUI.smallBoldFont);
		lblTitle.setForeground(new Color(255, 255, 255));
		leftPanel.add(lblTitle, BorderLayout.NORTH);

		// Channel list - horizontal layout for 4 channels in one row
		channelListPanel = new JPanel();
		channelListPanel.setOpaque(false);
		channelListPanel.setLayout(new BoxLayout(channelListPanel, BoxLayout.X_AXIS));
		channelListPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		leftPanel.add(channelListPanel, BorderLayout.CENTER);

		rawPanel.add(leftPanel, BorderLayout.CENTER);

		// Right side - Histogram placeholder
		JPanel histogramPanel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				// Draw a simple histogram background with gradient
				int width = getWidth();
				int height = getHeight();
				
				// Dark background
				g2.setColor(new Color(50, 50, 50));
				g2.fillRect(0, 0, width, height);
				
				// Title
				g2.setColor(new Color(200, 200, 200));
				g2.setFont(GUI.smallBoldFont);
				g2.drawString("Histogram", 10, 20);
				
				// Draw sample histogram bars
				g2.setColor(new Color(100, 200, 100));
				int barWidth = (width - 20) / 16;
				int[] values = {10, 20, 35, 50, 70, 85, 90, 95, 92, 80, 60, 40, 25, 15, 8, 5};
				for (int i = 0; i < values.length; i++) {
					int barHeight = (height - 40) * values[i] / 100;
					g2.fillRect(10 + (i * barWidth), height - 20 - barHeight, barWidth - 2, barHeight);
				}
				
				g2.dispose();
			}
		};
		histogramPanel.setOpaque(false);
		histogramPanel.setPreferredSize(new Dimension(150, 125));
		rawPanel.add(histogramPanel, BorderLayout.EAST);

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
		channelItem.setOpaque(false);
		channelItem.setLayout(new BoxLayout(channelItem, BoxLayout.Y_AXIS));
		channelItem.setPreferredSize(new Dimension(70, 75));
		channelItem.setMaximumSize(new Dimension(70, 75));
		channelItem.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		// Large clickable image color button with gradient effect
		JPanel imgColorBox = new JPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				Graphics2D g2 = (Graphics2D) g.create();
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				Color imgColor = channel.getImgColor();
				if (imgColor != null) {
					// Fill with channel color
					g2.setColor(imgColor);
					g2.fillRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 5, 5);
					
					// Add highlight effect
					g2.setColor(new Color(255, 255, 255, 60));
					g2.fillRoundRect(2, 2, getWidth() - 5, (getHeight() - 5) / 2, 5, 5);
					
					// Border
					g2.setColor(new Color(150, 150, 150));
					g2.setStroke(new java.awt.BasicStroke(2));
					g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 5, 5);
				}
				g2.dispose();
			}
		};
		imgColorBox.setOpaque(false);
		imgColorBox.setPreferredSize(new Dimension(60, 40));
		imgColorBox.setMaximumSize(new Dimension(60, 40));
		imgColorBox.setAlignmentX(JPanel.CENTER_ALIGNMENT);
		imgColorBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
		imgColorBox.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Color newColor = JColorChooser.showDialog(channelItem, "Choose Image Color for " + channel.getName(), channel.getImgColor());
				if (newColor != null) {
					channel.setImgColor(newColor);
					imgColorBox.repaint();
					if (pnlDisplay != null) {
						pnlDisplay.duplicateAndTintDisplayedImage(newColor);
					}
				}
			}
		});
		channelItem.add(imgColorBox);

		// Channel abbreviation with border
		JLabel lblChannelInfo = new JLabel(String.valueOf(channel.getAbbrev()));
		lblChannelInfo.setFont(new Font("Arial", Font.BOLD, 14));
		lblChannelInfo.setForeground(new Color(255, 255, 255));
		lblChannelInfo.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		lblChannelInfo.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
		channelItem.add(lblChannelInfo);

		// Channel name tooltip
		channelItem.setToolTipText(channel.getName());

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
