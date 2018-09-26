package com.typicalprojects.TronMachine.util;

public interface Logger {
	
	public void setCurrentTask(String task);
	
	public void setCurrentTaskComplete();
	
	public void setCurrentTaskProgress(int progress, int total);
	
}
