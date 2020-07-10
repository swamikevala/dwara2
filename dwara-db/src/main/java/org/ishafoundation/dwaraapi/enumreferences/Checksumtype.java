package org.ishafoundation.dwaraapi.enumreferences;

public enum Checksumtype {
	sha256("SHA-256");
	
	private String javaStyleChecksumtype;
	
	Checksumtype(String javaStyleChecksumtype) {
	    this.javaStyleChecksumtype = javaStyleChecksumtype;
	}
	
	public String getJavaStyleChecksumtype() {
		return javaStyleChecksumtype;
	}
}
