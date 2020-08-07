package org.ishafoundation.dwaraapi.enumreferences;

public enum Storagesubtype {
	lto7("LTO-7");
	
	private String javaStyleStoragesubtype;
	
	Storagesubtype(String javaStyleStoragesubtype) {
	    this.javaStyleStoragesubtype = javaStyleStoragesubtype;
	}
	
	public String getJavaStyleStoragesubtype() {
		return javaStyleStoragesubtype;
	}
	
	public static Storagesubtype getStoragesubtype(String javaStyleStoragesubtype){
		Storagesubtype storagesubtype = null;
	    for (Storagesubtype ct : Storagesubtype.values()) {
	        if (ct.javaStyleStoragesubtype.equals(javaStyleStoragesubtype)) {
	        	storagesubtype = ct;
	        	break;
	        }
	    }
		return storagesubtype;
	}
}
