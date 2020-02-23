package org.ishafoundation.dwaraapi.tape.library.components;

public class StorageElement {
	private int sNo;
	private String volumeTag;

	public int getsNo() {
		return sNo;
	}

	public void setsNo(int sNo) {
		this.sNo = sNo;
	}

	public String getVolumeTag() {
		return volumeTag;
	}

	public void setVolumeTag(String volumeTag) {
		this.volumeTag = volumeTag;
	}
	
	@Override
	public String toString() {
		return "sNo : " + sNo + " vt : " + volumeTag;
	}
}
