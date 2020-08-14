package org.ishafoundation.dwaraapi.storage.storagetype.tape.library;

public class TapeOnLibrary{
	
	private boolean isLoaded;
	
	private int address;
	
	private String volumeTag;

	public boolean isLoaded() {
		return isLoaded;
	}

	public void setLoaded(boolean isLoaded) {
		this.isLoaded = isLoaded;
	}

	public int getAddress() {
		return address;
	}

	public void setAddress(int address) {
		this.address = address;
	}

	public String getVolumeTag() {
		return volumeTag;
	}

	public void setVolumeTag(String volumeTag) {
		this.volumeTag = volumeTag;
	}
}

