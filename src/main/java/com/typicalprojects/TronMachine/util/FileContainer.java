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

public class FileContainer {

	public File file;
	public boolean printFullPath;

	public FileContainer(File file, boolean printFullPath) {
		this.file = file;
		this.printFullPath = printFullPath;
	}

	public String toString() {
		if (this.printFullPath)
			return file.getPath();
		else
			return file.getName();
	}
	
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
