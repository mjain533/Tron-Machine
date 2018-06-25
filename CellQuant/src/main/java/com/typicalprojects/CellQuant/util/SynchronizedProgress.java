package com.typicalprojects.CellQuant.util;

public class SynchronizedProgress {
	
	private volatile String task;
	private final SynchronizedProgressReceiver receiver;
	
	public SynchronizedProgress(String task, SynchronizedProgressReceiver receiver) {
		this.task = task;
		this.receiver = receiver;
	}
	
	public synchronized void setProgress(String task, int progressSoFar, int totalProgress) {
		this.receiver.applyProgress(this.task, task, progressSoFar, totalProgress);;
		this.task = task;
	}
	
	public interface SynchronizedProgressReceiver {
		
		public void applyProgress(String previousTask, String task, int progressSoFar, int totalProgress);
		
	}
	
	
}
