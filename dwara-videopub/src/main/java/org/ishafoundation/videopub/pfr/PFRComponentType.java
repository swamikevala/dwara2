package org.ishafoundation.videopub.pfr;

public enum PFRComponentType {

	HEADER("hdr"), 
	FOOTER("ftr"),
	INDEX("idx"),
	ESSENCE("ess");

	private final String extension;
	
	PFRComponentType(String extension) {
		this.extension = extension;
	} 
	
	public String getExtension() {
		return this.extension;
	}
	
}
