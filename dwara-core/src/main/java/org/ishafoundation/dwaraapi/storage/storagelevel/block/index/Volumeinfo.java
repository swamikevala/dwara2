package org.ishafoundation.dwaraapi.storage.storagelevel.block.index;

public class Volumeinfo {
	 private String uid;
	 private int blocksize;
	 private String checksumalgorithm;
	 private String encryptionalgorithm;
	 //private String libraryclassuid; // TODO is this still needed
	 
	 
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public int getBlocksize() {
		return blocksize;
	}
	public void setBlocksize(int blocksize) {
		this.blocksize = blocksize;
	}
	public String getChecksumalgorithm() {
		return checksumalgorithm;
	}
	public void setChecksumalgorithm(String checksumalgorithm) {
		this.checksumalgorithm = checksumalgorithm;
	}
	public String getEncryptionalgorithm() {
		return encryptionalgorithm;
	}
	public void setEncryptionalgorithm(String encryptionalgorithm) {
		this.encryptionalgorithm = encryptionalgorithm;
	}
}

