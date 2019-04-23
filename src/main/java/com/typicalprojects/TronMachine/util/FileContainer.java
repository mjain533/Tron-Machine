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

import java.io.File;

/**
 * Simple wrapper class. It's useful so I can control the effects of the {@link #toString()} method, as this
 * method for File obejcts by default displays the full path.
 * 
 * @author Justin Carrington
 */
public class FileContainer {

	/**
	 * The file contained.
	 */
	public File file;
	
	/**
	 * Whether the full path of this file should be printed by the {@link #toString()} method.
	 */
	public boolean printFullPath;

	/**
	 * Constructs a new File container.
	 * 
	 * @param file			Actual file
	 * @param printFullPath	True if the full path should be printed by the {@link #toString()} method.
	 */
	public FileContainer(File file, boolean printFullPath) {
		this.file = file;
		this.printFullPath = printFullPath;
	}

	/**
	 * @return the full path of just the file name, as specified during construction of this object.
	 */
	@Override
	public String toString() {
		if (this.printFullPath)
			return file.getPath();
		else
			return file.getName();
	}
	
	/**
	 * @return true if this container equals another. This is only true if the paths of the file within each
	 * file container is equal.
	 */
	@Override
	public boolean equals(Object other) {
		if (other == null || !(other instanceof FileContainer))
			return false;
		
		if (printFullPath) {
			return file.getPath().equals(((FileContainer) other).file.getPath());
		} else {
			return file.getName().equals(((FileContainer) other).file.getName());
		}
	}

}
