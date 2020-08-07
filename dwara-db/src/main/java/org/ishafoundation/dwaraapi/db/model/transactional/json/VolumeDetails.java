package org.ishafoundation.dwaraapi.db.model.transactional.json;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeDetails {

	private boolean barcoded; // Only applicable for tape volumes. Needed for the tape library to automatically load/unload tapes. This field specifies whether or not the volume has a machine readable barcode with the UID. Tapes without barcodes can be imported, however Dwara will not be able to restore any data from them. (Useful for keeping track of old tapes, which can serve as emergency copies and be barcoded on demand).
	private int blocksize; // Only applicable for block based storage volumes
	private int generation; // Tape generation (for LTO)
	private String mountpoint; // Mount point for disks
	private Integer provider_id;
	private Boolean remove_after_job; // Set this for tapes which should be removed from the autoloader as soon as any job is completed. Used for managing the security of tapes that hold confidential data.
	
	public boolean isBarcoded() {
		return barcoded;
	}
	public void setBarcoded(boolean barcoded) {
		this.barcoded = barcoded;
	}
	public int getBlocksize() {
		return blocksize;
	}
	public void setBlocksize(int blocksize) {
		this.blocksize = blocksize;
	}
	public int getGeneration() {
		return generation;
	}
	public void setGeneration(int generation) {
		this.generation = generation;
	}
	public String getMountpoint() {
		return mountpoint;
	}
	public void setMountpoint(String mountpoint) {
		this.mountpoint = mountpoint;
	}
	public Integer getProvider_id() {
		return provider_id;
	}
	public void setProvider_id(Integer provider_id) {
		this.provider_id = provider_id;
	}
	public Boolean getRemove_after_job() {
		return remove_after_job;
	}
	public void setRemove_after_job(Boolean remove_after_job) {
		this.remove_after_job = remove_after_job;
	}
	// TODO : equals and hashCode
}
