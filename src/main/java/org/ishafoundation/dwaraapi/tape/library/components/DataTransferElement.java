package org.ishafoundation.dwaraapi.tape.library.components;

public class DataTransferElement {
	private int sNo;
	private boolean isEmpty;
	private int storageElementNo;
	private String volumeTag;

	public int getsNo() {
		return sNo;
	}

	public void setsNo(int sNo) {
		this.sNo = sNo;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

	public int getStorageElementNo() {
		return storageElementNo;
	}

	public void setStorageElementNo(int storageElementNo) {
		this.storageElementNo = storageElementNo;
	}

	public String getVolumeTag() {
		return volumeTag;
	}

	public void setVolumeTag(String volumeTag) {
		this.volumeTag = volumeTag;
	}
	
	@Override
	public String toString() {
		return "sNo : " + sNo + "isEmpty : " + isEmpty + " sen : " + storageElementNo + " vt : " + volumeTag;
	}
}
