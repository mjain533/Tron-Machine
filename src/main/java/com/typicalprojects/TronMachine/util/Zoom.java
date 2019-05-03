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
package com.typicalprojects.TronMachine.util;

public enum Zoom {
	
	ZOOM_100(1, 1.0), ZOOM_50(2, 0.5), ZOOM_25(3, 0.25), ZOOM_17(4, 0.125);
	
	private int level;
	private double scaleFactor;
	
	private Zoom(int level, double scaleFactor) {
		this.level = level;
		this.scaleFactor = scaleFactor;
	}
	
	public int[] zoomOut(int currUpperLeftX, int currUpperLeftY, int imageDimensionX, int imageDimensionY, int xCenter, int yCenter) {
		int[] output = new int[4];
		
		Zoom newZoom = getPreviousZoomLevel();
		if (newZoom == Zoom.ZOOM_100) {
			output[0] = 0;
			output[1] = 0;
			output[2] = imageDimensionX;
			output[3] = imageDimensionY;
			return output;
		}
		
		output[2] = (int) Math.round(newZoom.scaleFactor * imageDimensionX); // new zoom width
		output[3] = (int) Math.round(newZoom.scaleFactor * imageDimensionY); // new zoom height
		double currentZoomWidth = Math.round(scaleFactor * imageDimensionX);
		double currentZoomHeight = Math.round(scaleFactor * imageDimensionY);
		
		int newUpperLeftX;
		int newUpperLeftY;
		
		if (xCenter != -1 && yCenter != -1) {
			
			// new width * ratio of mouse in from left of current VIEW (zoom)
			double amountToSubtractX = output[2] * ( (xCenter - currUpperLeftX) / currentZoomWidth); 

			// new height * ratio of mouse in from left of current VIEW (zoom)
			double amountToSubtractY = output[3] * ( (yCenter - currUpperLeftY) / currentZoomHeight);

			newUpperLeftX = (int) Math.round(xCenter - amountToSubtractX); // subtract, cast long to int
			newUpperLeftY = (int) Math.round(yCenter - amountToSubtractY); // subtract, cast long to int
			
		} else {

			newUpperLeftX = (int) (currUpperLeftX - (output[2] / 4.0));
			newUpperLeftY = (int) (currUpperLeftY - (output[3] / 4.0));
			
		}
		
		if (newUpperLeftX < 0) {
			newUpperLeftX = 0;
		} else if ((newUpperLeftX + output[2]) > imageDimensionX) {
			newUpperLeftX = imageDimensionX - output[2];

		}
		
		if (newUpperLeftY < 0) {
			newUpperLeftY = 0;
		} else if ((newUpperLeftY + output[3]) > imageDimensionY) {
			newUpperLeftY = imageDimensionY - output[3];
		}
		
		output[0] = newUpperLeftX;
		output[1] = newUpperLeftY;
		
		return output;
	}
	
	public int[] zoomIn(int currUpperLeftX, int currUpperLeftY, int imageDimensionX, int imageDimensionY, int xCenter, int yCenter) {
		int[] output = new int[4];

		Zoom nextZoom = getNextZoomLevel();
		if (nextZoom == null)  {
			
			output[0] = 0;
			output[1] = 0;
			output[2] = imageDimensionX;
			output[3] = imageDimensionY;
			return output;
		}
		
		output[2] = (int) Math.round(nextZoom.scaleFactor * imageDimensionX); // new zoom width
		output[3] = (int) Math.round(nextZoom.scaleFactor * imageDimensionY); // new zoom height
		double currentZoomWidth = Math.round(scaleFactor * imageDimensionX);
		double currentZoomHeight = Math.round(scaleFactor * imageDimensionY);
	
		// new width * ratio of mouse in from left of current VIEW (zoom)
		double amountToSubtractX = output[2] * ( (xCenter - currUpperLeftX) / currentZoomWidth); 

		// new height * ratio of mouse in from left of current VIEW (zoom)
		double amountToSubtractY = output[3] * ( (yCenter - currUpperLeftY) / currentZoomHeight);
		
		int newUpperLeftX = (int) (xCenter - amountToSubtractX);
		int newUpperLeftY = (int) (yCenter - amountToSubtractY);
		
		if (newUpperLeftX < 0) {
			newUpperLeftX = 0;
		} else if ((newUpperLeftX + output[2]) > imageDimensionX) {
			newUpperLeftX = imageDimensionX - output[2];
		}
		
		if (newUpperLeftY < 0) {
			newUpperLeftY = 0;
		} else if ((newUpperLeftY + output[3]) > imageDimensionY) {
			newUpperLeftY = imageDimensionY - output[3];
		}
		
		output[0] = newUpperLeftX;
		output[1] = newUpperLeftY;
		return output;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public Zoom getNextZoomLevel() {
		
		for (Zoom zoom : values()) {
			if (zoom.level == this.level + 1)
				return zoom;
		}
		
		return null;
		
	}
	
	public Zoom getPreviousZoomLevel() {
		for (Zoom zoom : values()) {
			if (zoom.level == this.level - 1)
				return zoom;
		}
		
		return Zoom.ZOOM_100;
	}
	
}
