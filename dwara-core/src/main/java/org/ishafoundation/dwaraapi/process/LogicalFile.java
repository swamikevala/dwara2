package org.ishafoundation.dwaraapi.process;

import java.io.File;
import java.util.HashMap;

public class LogicalFile extends File{

	private static final long serialVersionUID = 1L;

	public LogicalFile(String pathname) {
		super(pathname);
	}

	private HashMap<String, File> sidecarFiles; // extn as key and the file as its value.

	public HashMap<String, File> getSidecarFiles() {
		return sidecarFiles;
	}

	public void setSidecarFiles(HashMap<String, File> sidecarFiles) {
		this.sidecarFiles = sidecarFiles;
	}
	
	public File getSidecarFile(String extension) {
		if(sidecarFiles != null)
			return sidecarFiles.get(extension);
		return null;
	}
}
