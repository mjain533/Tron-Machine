package com.typicalprojects.TronMachine.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
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
	private volatile int imgPartHeight;
	private volatile int imgPartWidth;
	private volatile int pastHeight;
	private volatile int pastWidth;
	private volatile int posX;
	private volatile int posY;
	private Zoom zoom;
	private int zoomWidth;
	private int zoomHeight;
	private int zoomX;
	private int zoomY;
	private boolean needsUpdate = false;

	public ImagePanel() {
		super();
	}
	
	public synchronized Point getPixelPoint(int x, int y) {
		
		if (x >= posX && x <= (posX + imgPartWidth - 1) && y >= posY && y <= (posY + imgPartHeight - 1)) {
			
			if (this.zoom != null && this.zoom != Zoom.ZOOM_100) {
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
		this.needsUpdate = true;
		repaint();
	}
	
	public BufferedImage getImage() {
		return this.image;
	}
	
	public boolean screenPositionIsInImage(int x, int y) {
		return x >= posX && x <= (posX + imgPartWidth - 1) && y >= posY && y <= (posY + imgPartHeight - 1);
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
				this.zoom = zoom;
				this.needsUpdate = true;
			} else  {

				if (this.zoom == null || this.zoom.equals(Zoom.ZOOM_100)) {
					zoomWidth = this.image.getWidth();
					zoomHeight = this.image.getHeight();
					zoomX = 0;
					zoomY = 0;
					this.zoom = Zoom.ZOOM_100; // will be changed
				}

				int zoomDiff = Math.subtractExact(zoom.getLevel(), this.zoom.getLevel());
				
				if (xCenter >= posX && xCenter <= (posX + imgPartWidth - 1) && yCenter >= posY && yCenter <= (posY + imgPartHeight - 1)) {
					if (zoomDiff > 0) {
						// zoom in
						if (xCenter >= posX && xCenter <= (posX + imgPartWidth - 1) && yCenter >= posY && yCenter <= (posY + imgPartHeight - 1)) {
							for (int numZooms = 0; numZooms < zoomDiff; numZooms++) {
								//if (x >= posX && x <= (posX + imgPartWidth - 1) && y >= posY && y <= (posY + imgPartHeight - 1))
								//double modedX = (((double) zoomWidth) * ((xCenter + 1) / ((double) this.getWidth()))) + zoomX;
								double modedX = (((double) zoomWidth) * ((xCenter - posX + 1) / ((double) imgPartWidth))) + zoomX;
								double modedY = (((double) zoomHeight) * ((yCenter - posY + 1) / ((double) imgPartHeight))) + zoomY;
								int[] zoomStats = Zoom.zoomIn(zoomX, zoomY, this.image.getWidth() - 1, this.image.getHeight() - 1, zoomWidth, zoomHeight, (int) modedX, (int) modedY);
								this.zoomX = zoomStats[0];
								this.zoomY = zoomStats[1];
								this.zoomWidth = zoomStats[2];
								this.zoomHeight = zoomStats[3];
							}
						}
						
					} else {
						double modedX = (((double) zoomWidth) * ((xCenter - posX + 1) / ((double) imgPartWidth))) + zoomX;
						double modedY = (((double) zoomHeight) * ((yCenter - posY + 1) / ((double) imgPartHeight))) + zoomY;

						for (int numZooms = 0; numZooms < (-1 * zoomDiff); numZooms++) {
							int[] zoomStats = Zoom.zoomOut(zoomX, zoomY, this.image.getWidth() - 1, this.image.getHeight() - 1, zoomWidth, zoomHeight, (int) modedX, (int) modedY);
							this.zoomX = zoomStats[0];
							this.zoomY = zoomStats[1];
							this.zoomWidth = zoomStats[2];
							this.zoomHeight = zoomStats[3];
						}

					}
					this.zoom = zoom;
					this.needsUpdate = true;

				} else if (zoomDiff < 0) {
					for (int numZooms = 0; numZooms < (-1 * zoomDiff); numZooms++) {
						int[] zoomStats = Zoom.zoomOut(zoomX, zoomY, this.image.getWidth() - 1, this.image.getHeight() - 1, zoomWidth, zoomHeight, -1, -1);
						this.zoomX = zoomStats[0];
						this.zoomY = zoomStats[1];
						this.zoomWidth = zoomStats[2];
						this.zoomHeight = zoomStats[3];
					}
				}
				
			}
		}
		repaint();
	}

	public synchronized void paintComponent(Graphics g) {

		super.paintComponent(g);
		if (image != null) {
			BufferedImage zoomedIg = image;
			boolean drawZoomBox = false;
			if (this.getWidth() == pastWidth && this.getHeight() == pastHeight && !this.needsUpdate) {
				g.drawImage(scaled, posX, posY, this); // see javadoc for more info on the parameters 
				if (this.zoom != null && !zoom.equals(Zoom.ZOOM_100)) {
					drawZoomBox(g, this.posX, this.posY, this.imgPartWidth, this.imgPartHeight);
				}
				return;
			} else if (this.zoom != null && !zoom.equals(Zoom.ZOOM_100)) {
				drawZoomBox = true;
				zoomedIg = image.getSubimage(zoomX, zoomY, zoomWidth, zoomHeight);
			}
			this.needsUpdate = false;

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
		Graphics2D g2= (Graphics2D) g;
		int strokeWidth = imagePnlWidth / 250;
		g2.setStroke(new BasicStroke(strokeWidth));
		g2.setColor(Color.MAGENTA);
		g2.drawRect(upperLeftImagePosX + 5, upperLeftImagePosY + 5, scaleFactor, scaleFactor);
		g2.setColor(Color.CYAN);
		g2.drawRect((int) (upperLeftImagePosX + 5 + (scaleFactor * (this.zoomX / (double) this.image.getWidth()))), (int) (upperLeftImagePosY + 5 + (scaleFactor * (this.zoomY / (double) this.image.getHeight()))), (int) (scaleFactor * (this.zoomWidth / (double) this.image.getWidth())), (int) (scaleFactor * (this.zoomHeight / (double) this.image.getHeight())));
		//g.setColor(Color.YELLOW);
		
	}

}