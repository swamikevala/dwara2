package org.ishafoundation.dwaraapi.enumreferences;

public enum TapeStoragesubtype {
	lto7("LTO-7"),
	lto6("LTO-6"),
	lto5("LTO-5"),
	lto4("LTO-4"),
	lto3("LTO-3");
	
	private String javaStyleStoragesubtype;
	
	TapeStoragesubtype(String javaStyleStoragesubtype) {
	    this.javaStyleStoragesubtype = javaStyleStoragesubtype;
	}
	
	public String getJavaStyleStoragesubtype() {
		return javaStyleStoragesubtype;
	}
	
	public static TapeStoragesubtype getStoragesubtype(String javaStyleStoragesubtype){
		TapeStoragesubtype storagesubtype = null;
	    for (TapeStoragesubtype ct : TapeStoragesubtype.values()) {
	        if (ct.javaStyleStoragesubtype.equals(javaStyleStoragesubtype)) {
	        	storagesubtype = ct;
	        	break;
	        }
	    }
		return storagesubtype;
	}
}
