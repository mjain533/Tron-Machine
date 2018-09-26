package com.typicalprojects.CellQuant.util;

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
			containedResults.add(new FileContainer(result));
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
