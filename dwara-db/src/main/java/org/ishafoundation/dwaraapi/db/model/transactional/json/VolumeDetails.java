package org.ishafoundation.dwaraapi.db.model.transactional.json;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class VolumeDetails {

	private Boolean barcoded; // Only applicable for tape volumes. Needed for the tape library to automatically load/unload tapes. This field specifies whether or not the volume has a machine readable barcode with the UID. Tapes without barcodes can be imported, however Dwara will not be able to restore any data from them. (Useful for keeping track of old tapes, which can serve as emergency copies and be barcoded on demand).
	private Integer blocksize; // Only applicable for block based storage volumes
	private String mountpoint; // Mount point for disks
	@JsonProperty("remote_destination")
	private String remoteDestination;// like destination.path entry
	private Integer provider;
	@JsonProperty("remove_after_job")
	private Boolean removeAfterJob; // Set this for tapes which should be removed from the autoloader as soon as any job is completed. Used for managing the security of tapes that hold confidential data.
	@JsonProperty("minimum_free_space")
	private Long minimumFreeSpace; 
	@JsonProperty("written_at")
	private LocalDateTime writtenAt; 
	
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
	public String getRemoteDestination() {
		return remoteDestination;
	}
	public void setRemoteDestination(String remoteDestination) {
		this.remoteDestination = remoteDestination;
	}
	public Integer getProvider() {
		return provider;
	}
	public void setProvider(Integer provider) {
		this.provider = provider;
	}
	public Boolean getRemoveAfterJob() {
		return removeAfterJob;
	}
	public void setRemoveAfterJob(Boolean removeAfterJob) {
		this.removeAfterJob = removeAfterJob;
	}
	public Long getMinimumFreeSpace() {
		return minimumFreeSpace;
	}
	public void setMinimumFreeSpace(Long minimumFreeSpace) {
		this.minimumFreeSpace = minimumFreeSpace;
	}
	public LocalDateTime getWrittenAt() {
		return writtenAt;
	}
	public void setWrittenAt(LocalDateTime writtenAt) {
		this.writtenAt = writtenAt;
	}
	// TODO : equals and hashCode
}
