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
	
	public static Checksumtype getChecksumtype(String javaStyleChecksumtype){
		Checksumtype checksumtype = null;
	    for (Checksumtype ct : Checksumtype.values()) {
	        if (ct.javaStyleChecksumtype.equals(javaStyleChecksumtype)) {
	        	checksumtype = ct;
	        }
	    }
		return checksumtype;
	}
}
