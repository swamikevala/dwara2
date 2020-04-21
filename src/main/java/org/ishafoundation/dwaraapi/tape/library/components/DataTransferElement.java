package org.ishafoundation.dwaraapi.tape.library.components;

public class DataTransferElement {
	private int sNo;
	private boolean empty;
	private int storageElementNo;
	private String volumeTag;

	public int getsNo() {
		return sNo;
	}

	public void setsNo(int sNo) {
		this.sNo = sNo;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
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
		return "sNo : " + sNo + " empty : " + empty + " sen : " + storageElementNo + " vt : " + volumeTag;
	}
}
