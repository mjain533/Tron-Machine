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

/**
 * An interface for any panel which allows logging in the TRON machine.
 * 
 * @author Justin Carrington
 */
public interface Logger {
	
	/**
	 * Sets the current tasking being display by the logger, wiping any current progress on the currently
	 * displayed task.
	 * 
	 * @param task The task to display
	 */
	public void setCurrentTask(String task);
	
	/**
	 * Marks the current task as complete. The specific implementation is not specified by this interface.
	 */
	public void setCurrentTaskComplete();
	
	/**
	 * Sets the progress for the current task.
	 * 
	 * @param progress The progress on the current task
	 * @param total	   Total progress, presumably more than or equal to the the progress parameter.
	 */
	public void setCurrentTaskProgress(int progress, int total);
	
	/**
	 * Marks a task as complete, but complete with an error.
	 */
	public void setCurrentTaskCompleteWithError();
	
}
