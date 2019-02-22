package com.typicalprojects.TronMachine.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;



import ij.ImagePlus;
import ij.plugin.ZProjector;
import ij.process.ImageProcessor;

public class Toolbox {

	public static void drawString(ImageProcessor ip, Font font, String s, int x, int y, Color textColor, Color background) {
		ip.setFont(font);
		ip.setColor(textColor);
		FontMetrics fm = ip.getFontMetrics();
		int newX = x - (fm.stringWidth(s) / 2);
		int newY = y + ((fm.getHeight()) / 2);
		if (background == null) {
			ip.drawString(s, newX, newY);
		} else {
			ip.drawString(s, newX, newY, background);
		}
	}

	public static void drawStringWithBorderBox(ImageProcessor ip, Font font, String s, int x, int y, Color textColor, Color background, Color borderBoxColor) {
		ip.setFont(font);
		ip.setColor(textColor);
		FontMetrics fm = ip.getFontMetrics();
		int width = fm.stringWidth(s);
		int height = fm.getHeight();
		int newX = x - (width / 2);
		int newY = y + (height / 2);
		if (background == null) {
			ip.drawString(s, newX, newY);
		} else {
			ip.drawString(s, newX, newY, background);
		}
		ip.setColor(borderBoxColor);
		ip.drawRect(newX - 1, newY - height - 1, width + 1, height + 1);

		ip.setColor(textColor);
		ip.drawDot(newX, newY);

	}

	public static ImagePlus maxProject(ImagePlus image) {
		ImagePlus newI = image.duplicate();
		newI.setTitle(image.getTitle());
		ZProjector projector = new ZProjector();
		projector.setImage(newI);
		projector.setMethod(ZProjector.MAX_METHOD);
		projector.doProjection();
		return projector.getProjection();
	}

	public static Cursor createCursor()
	{
		int size = 15;
		BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB );
		Graphics2D g2d = (Graphics2D)image.getGraphics();
		g2d.setColor( Color.WHITE );
		g2d.fillRect(0, ((size - 1) / 2) - 1, size, 3);
		g2d.fillRect(((size - 1) / 2) - 1, 0, 3, size);
		g2d.setColor(Color.RED);
		g2d.fillRect(1, ((size - 1) / 2), size - 2, 1);
		g2d.fillRect(((size - 1) / 2), 1, 1, size - 2);

		java.awt.Point hotSpot = new java.awt.Point((size - 1) / 2, (size - 1) / 2);

		return Toolkit.getDefaultToolkit().createCustomCursor(image, hotSpot, "PaintBrush" );
	}

}
