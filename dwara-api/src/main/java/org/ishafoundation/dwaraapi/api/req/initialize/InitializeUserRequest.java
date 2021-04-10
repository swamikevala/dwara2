package org.ishafoundation.dwaraapi.api.req.initialize;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class InitializeUserRequest {
	// format
	private String volume;
	
	private String volumeGroup;
	
	private String storagesubtype;

	private Integer volumeBlocksize;
	
	private Boolean force = false;

	public String getVolume() {
		return volume;
	}

	public void setVolume(String volume) {
		this.volume = volume;
	}

	public String getVolumeGroup() {
		return volumeGroup;
	}

	public void setVolumeGroup(String volumeGroup) {
		this.volumeGroup = volumeGroup;
	}

	public String getStoragesubtype() {
		return storagesubtype;
	}

	public void setStoragesubtype(String storagesubtype) {
		this.storagesubtype = storagesubtype;
	}

	public Integer getVolumeBlocksize() {
		return volumeBlocksize;
	}

	public void setVolumeBlocksize(Integer volumeBlocksize) {
		this.volumeBlocksize = volumeBlocksize;
	}

	public Boolean getForce() {
		return force;
	}

	public void setForce(Boolean force) {
		this.force = force;
	}
}
