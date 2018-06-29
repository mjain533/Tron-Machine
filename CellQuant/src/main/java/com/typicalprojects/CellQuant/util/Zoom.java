package com.typicalprojects.CellQuant.util;

public enum Zoom {
	
	ZOOM_100(1), ZOOM_50(2), ZOOM_25(3);
	
	private int level;
	
	private Zoom(int level) {
		this.level = level;
	}
	
	public static int[] zoomIn(int upperLeftX, int upperLeftY, int xDim, int yDim, int width, int height, int xCenter, int yCenter) {
		int[] output = new int[4];
		
		int newUpperLeftX = (int) (xCenter - (width / 4.0));
		int newUpperLeftY = (int) (yCenter - (height / 4.0));
		output[2] = (int) (width / 2.0);
		output[3] = (int) (height / 2.0);

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
	
	public static int[] zoomOut(int upperLeftX, int upperLeftY, int xDim, int yDim, int width, int height) {
		int[] output = new int[4];

		int newUpperLeftX = (int) (upperLeftX - (width / 2.0));
		int newUpperLeftY = (int) (upperLeftY - (height / 2.0));
		output[2] = width * 2;
		output[3] = height * 2;

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
