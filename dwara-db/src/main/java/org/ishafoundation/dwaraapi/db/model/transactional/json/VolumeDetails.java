package org.ishafoundation.dwaraapi.db.model.transactional.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeDetails {

	private Boolean barcoded; // Only applicable for tape volumes. Needed for the tape library to automatically load/unload tapes. This field specifies whether or not the volume has a machine readable barcode with the UID. Tapes without barcodes can be imported, however Dwara will not be able to restore any data from them. (Useful for keeping track of old tapes, which can serve as emergency copies and be barcoded on demand).
	private Integer blocksize; // Only applicable for block based storage volumes
	private String mountpoint; // Mount point for disks
	private Integer provider;
	private Boolean remove_after_job; // Set this for tapes which should be removed from the autoloader as soon as any job is completed. Used for managing the security of tapes that hold confidential data.
	public Boolean getBarcoded() {
		return barcoded;
	}
	public void setBarcoded(Boolean barcoded) {
		this.barcoded = barcoded;
	}
	public Integer getBlocksize() {
		return blocksize;
	}
	public void setBlocksize(Integer blocksize) {
		this.blocksize = blocksize;
	}
	public String getMountpoint() {
		return mountpoint;
	}
	public void setMountpoint(String mountpoint) {
		this.mountpoint = mountpoint;
	}
	public Integer getProvider() {
		return provider;
	}
	public void setProvider(Integer provider) {
		this.provider = provider;
	}
	public Boolean getRemove_after_job() {
		return remove_after_job;
	}
	public void setRemove_after_job(Boolean remove_after_job) {
		this.remove_after_job = remove_after_job;
	}
	// TODO : equals and hashCode
}
