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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

import javax.swing.JPanel;

import com.typicalprojects.TronMachine.util.Point;
import com.typicalprojects.TronMachine.util.Zoom;

import ij.ImagePlus;

public class ImagePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3219505730380857923L;
	private BufferedImage image;
	private Image scaled;
	private volatile int imgPartHeight;
	private volatile int imgPartWidth;
	private volatile int pastHeight;
	private volatile int pastWidth;
	private volatile int posX;
	private volatile int posY;
	private Zoom zoom = Zoom.ZOOM_100;
	private int zoomWidth = -1;
	private int zoomHeight = -1;
	private int zoomX = 0;
	private int zoomY = 0;
	private boolean needsUpdateZoom = false;
	
	private IndexColorModel heatScale = null;
	private String heatScaleMaxText = null;
	private String heatScaleMinText = null;

	public ImagePanel() {
		super();
	}
	
	public synchronized Point getPixelPoint(int x, int y) {
		
		if (x >= posX && x <= (posX + imgPartWidth - 1) && y >= posY && y <= (posY + imgPartHeight - 1)) {
			
			if (this.zoom != Zoom.ZOOM_100) {
				double modedX = (zoomWidth * ((x - posX) / (double) imgPartWidth)) + zoomX;
				double modedY = (zoomHeight * ((y - posY) / (double) imgPartHeight)) + zoomY;
				//double xImg = ((modedX - posX) / (double) imgWidth) * image.getWidth();
				//double yImg = ((modedY - posY) / (double) imgHeight) * (double) image.getHeight();

				return new Point((int) modedX, (int)modedY, null);
				
			} else {
				double xImg = ((x - posX) / (double) imgPartWidth) * (double) image.getWidth();
				double yImg = ((y - posY) / (double) imgPartHeight) * (double) image.getHeight();
				return new Point((int) xImg, (int)yImg, null);

			}
			

		} else {
			return null;
		}
		
	}
	
	/**
	 * 1 = left
	 * 2 = up
	 * 3 = right
	 * 4 = down
	 * 
	 * @param direction
	 */
	public void shiftImage(int direction) {
		
		if (this.zoom == null || this.zoom == Zoom.ZOOM_100)
			return;
		
		int shiftAmount = (int) (Math.max(this.zoomHeight, this.zoomWidth) / 10.0);
		switch (direction) {
		case 1:
			this.zoomX = Math.max(0, this.zoomX - shiftAmount);
			break;
		case 2:
			this.zoomY = Math.max(0, this.zoomY - shiftAmount);
			break;
		case 3:
			this.zoomX = Math.min(this.image.getWidth() - this.zoomWidth, this.zoomX + shiftAmount);
			break;
		case 4:
			this.zoomY = Math.min(this.image.getHeight() - this.zoomHeight, this.zoomY + shiftAmount);
			break;
		}
		this.needsUpdateZoom = true;
		repaint();
	}
	
	public BufferedImage getImage() {
		return this.image;
	}
	
	public boolean screenPositionIsInImage(int x, int y) {
		return x >= posX && x <= (posX + imgPartWidth - 1) && y >= posY && y <= (posY + imgPartHeight - 1);
	}
	
	public synchronized void setScaleBar(IndexColorModel heatScale, String heatScaleMinText, String heatScaleMaxText) {
		this.heatScale = heatScale;
		this.heatScaleMinText = heatScaleMinText;
		this.heatScaleMaxText = heatScaleMaxText;
	}
	
	/**
	 * See {@link #setImage(BufferedImage, boolean)}
	 * 
	 * @param ip			The image to set in the panel
	 * @param keepZoom	True of the current zoom should be kept, false to zoom out to full size
	 */
	public synchronized void setImage(ImagePlus ip, boolean keepZoom) {
		setImage(ip.getBufferedImage(), keepZoom);
	}
	
	/**
	 * Sets the current image being display in this image panel, updating the display (repainting it)
	 * 
	 * @param ip			The image to set in the panel
	 * @param keepZoom	True of the current zoom should be kept, false to zoom out to full size
	 */
	public synchronized void setImage(BufferedImage ig, boolean keepZoom) {
		this.image = ig;
		this.needsUpdateZoom = true;
		if (!keepZoom || zoom == Zoom.ZOOM_100) {
			zoomWidth = -1;
			zoomHeight = -1;
			zoomX = 0;
			zoomY = 0;
			this.zoom = Zoom.ZOOM_100;
			this.needsUpdateZoom = true;
		}
		repaint();
	}
	
	public Zoom getZoom() {
		return this.zoom;
	}
	
	public synchronized void zoomIn(int xCenter, int yCenter, boolean dontRepaint) {
		
		Zoom nextZoom = this.zoom.getNextZoomLevel();
		if (nextZoom == null)
			return;
		
		if (xCenter < posX || xCenter > (posX + imgPartWidth - 1) || yCenter < posY || yCenter > (posY + imgPartHeight - 1)) { // check if mouse is within image
			// mouse is not within image
			return;
		}
		
		double modedX;
		double modedY;
		if (this.zoom != Zoom.ZOOM_100) {
			modedX = (((double) zoomWidth) * ((xCenter - posX + 1) / ((double) imgPartWidth))) + zoomX;
			modedY = (((double) zoomHeight) * ((yCenter - posY + 1) / ((double) imgPartHeight))) + zoomY;
		} else {
			modedX = (((double) this.image.getWidth()) * ((xCenter - posX + 1) / ((double) imgPartWidth)));
			modedY = (((double) this.image.getHeight()) * ((yCenter - posY + 1) / ((double) imgPartHeight)));
		}
		
		int[] zoomStats = this.zoom.zoomIn(this.zoomX, this.zoomY, this.image.getWidth(), this.image.getHeight(), (int) modedX, (int) modedY);
		this.zoomX = zoomStats[0];
		this.zoomY = zoomStats[1];
		this.zoomWidth = zoomStats[2];
		this.zoomHeight = zoomStats[3];
		
		this.zoom = nextZoom;
		this.needsUpdateZoom = true;
		if (!dontRepaint) {
			repaint();
		}
		
	}
	
	public synchronized void zoomOut(boolean zoomToFull, boolean dontRepaint) {
		zoomOut(zoomToFull, -1, -1, dontRepaint);
	}
	
	public synchronized void zoomOut(boolean zoomToFull, int xCenter, int yCenter, boolean dontRepaint) {
		
		if (this.zoom == Zoom.ZOOM_100)
			return;
		
		if (zoomToFull) {
			this.zoomHeight = -1;
			this.zoomWidth = -1;
			this.zoomX = 0;
			this.zoomY = 0;
			this.zoom = Zoom.ZOOM_100;
			this.needsUpdateZoom = true;
			if (!dontRepaint) {
				repaint();
			}
			return;
		}
		
		Zoom nextZoom = this.zoom.getPreviousZoomLevel();
		
		int[] zoomStats = null;
		
		if (xCenter == -1 || yCenter == -1 || xCenter < posX || xCenter > (posX + imgPartWidth - 1) || yCenter < posY || yCenter > (posY + imgPartHeight - 1)) { // check if mouse is within image
			zoomStats = this.zoom.zoomOut(this.zoomX, this.zoomY, this.image.getWidth(), this.image.getHeight(), -1, -1);

		} else {
			
			double modedX = (((double) zoomWidth) * ((xCenter - posX + 1) / ((double) imgPartWidth))) + zoomX;
			double modedY = (((double) zoomHeight) * ((yCenter - posY + 1) / ((double) imgPartHeight))) + zoomY;
			
			zoomStats = this.zoom.zoomOut(this.zoomX, this.zoomY, this.image.getWidth(), this.image.getHeight(), (int) modedX, (int) modedY);
			// mouse is within image

		}
		
		this.zoomX = zoomStats[0];
		this.zoomY = zoomStats[1];
		this.zoomWidth = zoomStats[2];
		this.zoomHeight = zoomStats[3];
		
		this.zoom = nextZoom;
		this.needsUpdateZoom = true;

		if (!dontRepaint) {
			repaint();
		}
		
	}
	
	public void setZoom(Zoom zoom, int xCenter, int yCenter) {
		
		
		if (this.zoom == zoom)
			return;
		else if (zoom == Zoom.ZOOM_100) {
			this.zoomHeight = -1;
			this.zoomWidth = -1;
			this.zoomX = 0;
			this.zoomY = 0;
			this.zoom = Zoom.ZOOM_100;
			this.needsUpdateZoom = true;
			repaint();
			return;
		}
		
		int zoomDiff = Math.subtractExact(zoom.getLevel(), this.zoom.getLevel());
		
		if (zoomDiff > 0) {
			// zoom in
			// successively zoom in.
			
			if (xCenter == -1 || yCenter == -1)
				return;
			
			for (int numZooms = 0; numZooms < zoomDiff; numZooms++) {
				zoomIn(xCenter, yCenter, true);
			}
			
			this.needsUpdateZoom = true;
			repaint();
			
		} else {
			
			// zoom out
			for (int numZooms = 0; numZooms < (-1 * zoomDiff); numZooms++) {
				zoomOut(false, xCenter, yCenter, true);
			}
			
			this.needsUpdateZoom = true;
			repaint();
		}
		
	}
	

	public synchronized void paintComponent(Graphics g) {

		super.paintComponent(g);
		if (image != null) {
			BufferedImage zoomedIg = image;
			boolean drawZoomBox = false;
			if (this.getWidth() == pastWidth && this.getHeight() == pastHeight && !this.needsUpdateZoom) {
				// Image panel didn't change size and we don't need to update.
				g.drawImage(scaled, posX, posY, this);
				if (!this.zoom.equals(Zoom.ZOOM_100)) {
					drawZoomBox(g, this.posX, this.posY, this.imgPartWidth, this.imgPartHeight);
				}
				drawHeatScale(g, this.posX, this.posY, this.imgPartWidth, this.imgPartHeight);
				return;
			} else if (!this.zoom.equals(Zoom.ZOOM_100)) {
				drawZoomBox = true;
				

				zoomedIg = image.getSubimage(zoomX, zoomY, zoomWidth, zoomHeight);
			}
			this.needsUpdateZoom = false;

			pastHeight = getHeight();
			pastWidth = getWidth();
			
			double r = ((double) pastHeight) / image.getHeight();
			
			if (image.getWidth() * r < pastWidth) {
				// height is limiting
				this.imgPartHeight = pastHeight;
				this.imgPartWidth = (int) (image.getWidth() * r);
				this.posX = (pastWidth - this.imgPartWidth) / 2;
				this.posY = 0;
			} else {
				// width is limiting
				this.imgPartHeight = (int) (image.getHeight() * (((double) pastWidth) / image.getWidth()));
				this.imgPartWidth = pastWidth;
				this.posX = 0;
				this.posY = ((pastHeight - this.imgPartHeight) / 2);
			}

			

			scaled = zoomedIg.getScaledInstance(this.imgPartWidth, this.imgPartHeight, Image.SCALE_SMOOTH);
			g.drawImage(scaled, posX, posY, this); // see javadoc for more info on the parameters  
			
			if (drawZoomBox) {
				drawZoomBox(g, this.posX, this.posY, this.imgPartWidth, this.imgPartHeight);
			}
						
			return;
		}


	}
	
	private void drawZoomBox(Graphics g, int upperLeftImagePosX, int upperLeftImagePosY, int imagePnlWidth, int imagePnlHeight) {
		
		
		if (imagePnlWidth < 100 || imagePnlHeight < 100)
			return;
		int scaleFactor = (int) Math.ceil(imagePnlWidth / 8.0);
		int scaleFactor2 = (int) Math.ceil(imagePnlHeight / 8.0);

		Graphics2D g2= (Graphics2D) g;
		int strokeWidth = imagePnlWidth / 250;
		g2.setStroke(new BasicStroke(strokeWidth));
		g2.setColor(Color.MAGENTA);
		g2.drawRect(upperLeftImagePosX + 5, upperLeftImagePosY + 5, scaleFactor, scaleFactor2);
		g2.setColor(Color.CYAN);
		g2.drawRect((int) (upperLeftImagePosX + 5 + (scaleFactor * (this.zoomX / (double) this.image.getWidth()))), 
				(int) (upperLeftImagePosY + 5 + (scaleFactor2 * (this.zoomY / (double) this.image.getHeight()))), 
				(int) (scaleFactor * (this.zoomWidth / (double) this.image.getWidth())) + 1, 
				(int) (scaleFactor2 * (this.zoomHeight / (double) this.image.getHeight())) + 1);
		
	}
	
	private void drawHeatScale(Graphics g, int upperLeftImagePosX, int upperLeftImagePosY, int imagePnlWidth, int imagePnlHeight) {
		if (this.heatScale == null) {
			return;
		}
		
		
		int heatScaleWidth = imagePnlWidth / 4;
		int heatScaleHeight = heatScaleWidth / 6;
		
		
		Graphics2D g2= (Graphics2D) g;
		int strokeWidth = imagePnlWidth / 250;
		g2.setStroke(new BasicStroke(strokeWidth));
		byte[] reds = new byte[256];
		byte[] greens = new byte[256];
		byte[] blues = new byte[256];

		this.heatScale.getReds(reds);
		this.heatScale.getGreens(greens);
		this.heatScale.getBlues(blues);

		int startX = upperLeftImagePosX + (int) ((imagePnlWidth / 2.0) - (heatScaleWidth / 2.0));
		int startY = upperLeftImagePosY + (imagePnlHeight /50);
		for (int i = 0; i <= heatScaleWidth; i++) {
			int index = (int) ((i / (double) heatScaleWidth) * 255.0);
			g2.setColor(new Color((reds[index] & 0xFF), (greens[index] & 0xFF), (blues[index] & 0xFF)));
			g2.drawLine(startX + i, startY, startX + i, startY +heatScaleHeight );
		}

		g2.setColor(Color.RED);
		g2.drawRect(startX, startY, heatScaleWidth, heatScaleHeight);
		
		int fontHeight = (int) (heatScaleHeight / 1.5);
		
		g2.setFont(new Font("Arial", Font.BOLD, fontHeight));
		if (this.heatScaleMinText != null) {
			int stringWidth = g2.getFontMetrics().stringWidth(this.heatScaleMinText);
			
			if ((startX - stringWidth - (imagePnlWidth /80)) > ((imagePnlWidth / 8.0)+5)) {
				g2.drawString(this.heatScaleMinText, startX - stringWidth - (imagePnlWidth /80), startY + heatScaleHeight - g2.getFontMetrics().getDescent() - (int) ((heatScaleHeight - fontHeight)/ 2.0));
			}
		}
		
		if (this.heatScaleMaxText != null) {
			int stringWidth = g2.getFontMetrics().stringWidth(this.heatScaleMaxText);
			
			if ((startX + heatScaleWidth + stringWidth + (imagePnlWidth /80)) <= imagePnlWidth) {
				g2.drawString(this.heatScaleMaxText, startX + heatScaleWidth + (imagePnlWidth /80), startY + heatScaleHeight - g2.getFontMetrics().getDescent() - (int) ((heatScaleHeight - fontHeight)/ 2.0));
			}
		}

	}
	

}