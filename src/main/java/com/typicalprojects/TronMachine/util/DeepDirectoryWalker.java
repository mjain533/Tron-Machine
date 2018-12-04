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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.DirectoryWalker;

public class DeepDirectoryWalker extends DirectoryWalker{
	
	private final Set<String> endings;
	private final int depth;
	private final boolean enforceDepth;
	private final boolean enforceFileLimit;
	public DeepDirectoryWalker(Set<String> endings, int depth, boolean enforceDepth, boolean enforceFileLimit) {
		super();
		this.endings = endings;
		this.depth = depth;
		this.enforceDepth = enforceDepth;
		this.enforceFileLimit = enforceFileLimit;
	}

	public List<FileContainer> getFilteredFiles(File dir) throws IOException {
		List<File> results = new ArrayList<File>();
		walk(dir, results);
		List<FileContainer> containedResults = new ArrayList<FileContainer>();
		for (File result : results) {
			containedResults.add(new FileContainer(result, false));
		}
		return containedResults;
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected void handleFile(File file, int depth, Collection results) throws IOException {
		
		boolean justAccept = endings == null;
		
		if (!justAccept) {
			for (String ending : endings) {
				if (file.getPath().endsWith(ending)) {
					justAccept = true;
					break;
				}
			}
			
		}
		if (justAccept) {
			if (depth <= this.depth) {
				results.add(file);
			} else if (this.enforceDepth) {
				throw new IOException();
			}
		}
		

	}

	@SuppressWarnings("rawtypes")
	protected boolean handleDirectory(File directory, int depth, Collection results) throws IOException {
		if (directory.list().length > 500 && this.enforceFileLimit)
			throw new IOException();
		return true;

	}


}
