package com.typicalprojects.TronMachine.util;

import java.io.File;

public class FileContainer {

	public File file;

	public FileContainer(File file) {
		this.file = file;
	}

	public String toString() {
		return file.getName();
	}
	
	public boolean equals(Object other) {
		if (other == null || !(other instanceof FileContainer))
			return false;
		
		return file.getName().equals(((FileContainer) other).file.getName());
	}

}
