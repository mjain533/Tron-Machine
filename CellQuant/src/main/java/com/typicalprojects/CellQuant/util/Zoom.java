package com.typicalprojects.CellQuant.util;

public enum Zoom {
	
	ZOOM_100(1), ZOOM_50(2), ZOOM_25(3);
	
	private int level;
	
	private Zoom(int level) {
		this.level = level;
	}
	
	public static int[] zoomIn(int upperLeftX, int upperLeftY, int xDim, int yDim, int width, int height, int xCenter, int yCenter) {
		
		int[] output = new int[4];
		
		output[2] = (int) (width / 2.0);
		output[3] = (int) (height / 2.0);
		
		double amountToSubtractX = output[2] * ( (xCenter - upperLeftX) / ((double) width));
		double amountToSubtractY = output[3] * ( (yCenter - upperLeftY) / ((double) height));

		
		int newUpperLeftX = (int) (xCenter - amountToSubtractX);
		int newUpperLeftY = (int) (yCenter - amountToSubtractY);


		if (newUpperLeftX < 0) {
			newUpperLeftX = 0;
		} else if ((newUpperLeftX + output[2]) > xDim) {
			newUpperLeftX = xDim - output[2];
		}
		
		if (newUpperLeftY < 0) {
			newUpperLeftY = 0;
		} else if ((newUpperLeftY + output[3]) > yDim) {
			newUpperLeftY = yDim - output[3];
		}
		
		output[0] = newUpperLeftX;
		output[1] = newUpperLeftY;
		return output;
	}
	
	public static int[] zoomOut(int upperLeftX, int upperLeftY, int xDim, int yDim, int width, int height, int xCenter, int yCenter) {
		int[] output = new int[4];

		output[2] = width * 2;
		output[3] = height * 2;
		
		int newUpperLeftX;
		int newUpperLeftY;
		if (xCenter != -1 && yCenter != -1) {
			double amountToSubtractX = output[2] * ( (xCenter - upperLeftX) / ((double) width));
			double amountToSubtractY = output[3] * ( (yCenter - upperLeftY) / ((double) height));

			newUpperLeftX = (int) (xCenter - amountToSubtractX);
			newUpperLeftY = (int) (yCenter - amountToSubtractY);
		} else {
			newUpperLeftX = (int) (upperLeftX - (width / 2.0));
			newUpperLeftY = (int) (upperLeftY - (height / 2.0));
		}
		

		if (newUpperLeftX < 0) {
			newUpperLeftX = 0;
		} else if ((newUpperLeftX + output[2]) > xDim) {
			newUpperLeftX = xDim - output[2];
		}
		
		if (newUpperLeftY < 0) {
			newUpperLeftY = 0;
		} else if ((newUpperLeftY + output[3]) > yDim) {
			newUpperLeftY = yDim - output[3];
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
