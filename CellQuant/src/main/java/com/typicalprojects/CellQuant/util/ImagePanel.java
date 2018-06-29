package com.typicalprojects.CellQuant.util;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;



public class ImagePanel extends JPanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3219505730380857923L;
	private BufferedImage image;
	private Image scaled;
	private volatile int imgHeight;
	private volatile int imgWidth;
	private volatile int pastHeight;
	private volatile int pastWidth;
	private volatile int posX;
	private volatile int posY;
	private Zoom zoom;
	private int zoomWidth;
	private int zoomHeight;
	private int zoomX;
	private int zoomY;

	public ImagePanel() {
		super();
	}
	
	public synchronized Point getPixelPoint(int x, int y) {
		
		if (x >= posX && x <= (posX + imgWidth - 1) && y >= posY && y <= (posY + imgHeight - 1)) {

			double xRatio = (x - posX) / (double) imgWidth;
			double yRatio = (y - posY) / (double) imgHeight;
			return new Point((int) (xRatio * (double) image.getWidth()), (int) (yRatio * (double) image.getHeight()), null);
			
		} else {
			return null;
		}
		
	}
	
	public BufferedImage getImage() {
		return this.image;
	}
	
	
	public synchronized void setImage(BufferedImage ig, int xCenter, int yCenter, Zoom zoom) {
		this.image = ig;
		this.pastHeight = -1;
		if (zoom != null && this.zoom != zoom) {
			if (zoom.equals(Zoom.ZOOM_100)) {
				this.zoom = null;
				zoomWidth = -1;
				zoomHeight = -1;
				zoomX = -1;
				zoomY = -1;
			} else  {

				if (this.zoom == null || this.zoom.equals(Zoom.ZOOM_100)) {
					zoomWidth = this.image.getWidth();
					zoomHeight = this.image.getHeight();
					zoomX = 0;
					zoomY = 0;
					this.zoom = Zoom.ZOOM_100;
				}

				int zoomDiff = Math.subtractExact(zoom.getLevel(), this.zoom.getLevel());
				if (zoomDiff > 0) {
					// zoom in
					for (int numZooms = 0; numZooms < zoomDiff; numZooms++) {
						
						double modedX = (zoomWidth * (xCenter / ((double) this.getWidth()))) + zoomX;
						double modedY = (zoomHeight * (yCenter / ((double) this.getHeight()))) + zoomY;
						int[] zoomStats = Zoom.zoomIn(zoomX, zoomY, this.image.getWidth() - 1, this.image.getHeight() - 1, zoomWidth, zoomHeight, (int) modedX, (int) modedY);
						this.zoomX = zoomStats[0];
						this.zoomY = zoomStats[1];
						this.zoomWidth = zoomStats[2];
						this.zoomHeight = zoomStats[3];
					}
				} else {
					for (int numZooms = 0; numZooms < (-1 * zoomDiff); numZooms++) {
						int[] zoomStats = Zoom.zoomOut(zoomX, zoomY, this.image.getWidth() - 1, this.image.getHeight() - 1, zoomWidth, zoomHeight);
						this.zoomX = zoomStats[0];
						this.zoomY = zoomStats[1];
						this.zoomWidth = zoomStats[2];
						this.zoomHeight = zoomStats[3];
					}
				}
				
			}
		} else {
			this.zoom = null;
			zoomWidth = -1;
			zoomHeight = -1;
			zoomX = -1;
			zoomY = -1;
		}
		this.zoom = zoom;
		repaint();
	}

	public synchronized void paintComponent(Graphics g) {

		super.paintComponent(g);
		
		if (image != null) {
			if (this.getWidth() == pastWidth && this.getHeight() == pastHeight) {
				g.drawImage(scaled, posX, posY, this); // see javadoc for more info on the parameters 
				return;
			}
			pastHeight = getHeight();
			pastWidth = getWidth();
			
			double r = ((double) pastHeight) / image.getHeight();
			
			if (image.getWidth() * r < pastWidth) {
				// height is limiting
				this.imgHeight = pastHeight;
				this.imgWidth = (int) (image.getWidth() * r);
				this.posX = (pastWidth - this.imgWidth) / 2;
				this.posY = 0;
			} else {
				// width is limiting
				this.imgHeight = (int) (image.getHeight() * (((double) pastWidth) / image.getWidth()));
				this.imgWidth = pastWidth;
				this.posX = 0;
				this.posY = ((pastHeight - this.imgHeight) / 2);
			}
			
			
			/*if (pastWidth > pastHeight) {
				this.imgHeight = pastHeight;
				this.imgWidth = pastHeight;
				this.posX = (pastWidth - pastHeight) / 2;
				this.posY = 0;

			} else if (pastWidth < pastHeight) {
				this.imgHeight = pastWidth;
				this.imgWidth = pastWidth;
				this.posX = 0;
				this.posY = (pastHeight - pastWidth) / 2;

			} else {
				this.imgHeight = pastHeight;
				this.imgWidth = pastWidth;
				this.posX = 0;
				this.posY = 0;
			}*/
			
			BufferedImage zoomedIg = image;
			if (this.zoom != null && !zoom.equals(Zoom.ZOOM_100)) {
				zoomedIg = image.getSubimage(zoomX, zoomY, zoomWidth, zoomHeight);
			
			}
			
			scaled = zoomedIg.getScaledInstance(this.imgWidth, this.imgHeight, Image.SCALE_SMOOTH);
			g.drawImage(scaled, posX, posY, this); // see javadoc for more info on the parameters  
		}


	}

}