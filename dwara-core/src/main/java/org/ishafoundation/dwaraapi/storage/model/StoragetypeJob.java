package org.ishafoundation.dwaraapi.storage.model;

public class StoragetypeJob {
	
	private StorageJob storageJob;
	
	private String deviceUid;

	public StorageJob getStorageJob() {
		return storageJob;
	}

	public void setStorageJob(StorageJob storageJob) {
		this.storageJob = storageJob;
	}

	public String getDeviceUid() {
		return deviceUid;
	}

	public void setDeviceUid(String deviceUid) {
		this.deviceUid = deviceUid;
	}
}
