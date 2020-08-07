package org.ishafoundation.dwaraapi.api.req.format;

import org.ishafoundation.dwaraapi.enumreferences.Storagesubtype;

public class FormatRequest {
	// format
	private String volumeId;
	
	private String volumeGroupId;
	
	private Storagesubtype storagesubtype;
	
	private Integer volumeBlocksize;
	
	private Boolean force;

	public String getVolumeId() {
		return volumeId;
	}

	public void setVolumeId(String volumeId) {
		this.volumeId = volumeId;
	}

	public String getVolumeGroupId() {
		return volumeGroupId;
	}

	public void setVolumeGroupId(String volumeGroupId) {
		this.volumeGroupId = volumeGroupId;
	}

	public Storagesubtype getStoragesubtype() {
		return storagesubtype;
	}

	public void setStoragesubtype(Storagesubtype storagesubtype) {
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
