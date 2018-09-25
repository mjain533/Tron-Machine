package com.typicalprojects.CellQuant.util;

public interface Logger {
	
	public void setCurrentTask(String task);
	
	public void setCurrentTaskComplete();
	
	public void setCurrentTaskProgress(int progress, int total);
	
}
