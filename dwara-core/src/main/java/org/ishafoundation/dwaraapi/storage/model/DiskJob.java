package org.ishafoundation.dwaraapi.storage.model;

public class DiskJob extends SelectedStorageJob {
	
	// something related to disk goes here...
	private String mountPoint;

	public String getMountPoint() {
		return mountPoint;
	}

	public void setMountPoint(String mountPoint) {
		this.mountPoint = mountPoint;
	}
}
