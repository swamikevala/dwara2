package org.ishafoundation.videopub.pfr;

import java.io.File;

public class PFRComponentFile {
	
	private File file;
	private PFRComponentType type;
	
	public PFRComponentFile(File file, PFRComponentType type) {
		this.file = file;
		this.type = type;
	}
	
	public File getFile() {
		return file;
	};
	
	public void setFile(File file) {
		this.file = file;
	};
	
	public PFRComponentType getType() {
		return type;
	};
	
	public void setType(PFRComponentType type) {
		this.type = type;
	}
}
