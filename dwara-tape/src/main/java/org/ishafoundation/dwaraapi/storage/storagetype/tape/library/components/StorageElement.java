package org.ishafoundation.dwaraapi.storage.storagetype.tape.library.components;

public class StorageElement {
	private int sNo;
	private boolean importExport;
	private boolean empty;
	private String volumeTag;

	public int getsNo() {
		return sNo;
	}

	public void setsNo(int sNo) {
		this.sNo = sNo;
	}

	public boolean isImportExport() {
		return importExport;
	}

	public void setImportExport(boolean importExport) {
		this.importExport = importExport;
	}

	public boolean isEmpty() {
		return empty;
	}

	public void setEmpty(boolean empty) {
		this.empty = empty;
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
