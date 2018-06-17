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
	
	public synchronized void setImage(BufferedImage ig) {
		this.image = ig;
		this.pastHeight = -1;
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
			
			scaled = image.getScaledInstance(this.imgWidth, this.imgHeight, Image.SCALE_SMOOTH);
			g.drawImage(scaled, posX, posY, this); // see javadoc for more info on the parameters  
		}


	}

}