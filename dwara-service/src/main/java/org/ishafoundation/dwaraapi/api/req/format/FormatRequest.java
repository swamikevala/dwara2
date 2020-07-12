package org.ishafoundation.dwaraapi.api.req.format;

public class FormatRequest {
	// format
	private String volume_uid;
	
	private String volume_group_uid; // TODO : Schema deviation - Schema to change
	
	private Long capacity;
	
	private String archiveformat;
	
	private String checksum_algorithm;
	
	private String encryption_algorithm;

	private Integer volume_blocksize;

	private Integer generation; // Only needed for storagetype = tape // TODO : Schema deviation - Schema to change
	
	private Boolean force;

	public String getVolume_uid() {
		return volume_uid;
	}

	public void setVolume_uid(String volume_uid) {
		this.volume_uid = volume_uid;
	}

	public String getVolume_group_uid() {
		return volume_group_uid;
	}

	public void setVolume_group_uid(String volume_group_uid) {
		this.volume_group_uid = volume_group_uid;
	}

	public Long getCapacity() {
		return capacity;
	}

	public void setCapacity(Long capacity) {
		this.capacity = capacity;
	}

	public String getArchiveformat() {
		return archiveformat;
	}

	public void setArchiveformat(String archiveformat) {
		this.archiveformat = archiveformat;
	}

	public String getChecksum_algorithm() {
		return checksum_algorithm;
	}

	public void setChecksum_algorithm(String checksum_algorithm) {
		this.checksum_algorithm = checksum_algorithm;
	}

	public String getEncryption_algorithm() {
		return encryption_algorithm;
	}

	public void setEncryption_algorithm(String encryption_algorithm) {
		this.encryption_algorithm = encryption_algorithm;
	}

	public Integer getVolume_blocksize() {
		return volume_blocksize;
	}

	public void setVolume_blocksize(Integer volume_blocksize) {
		this.volume_blocksize = volume_blocksize;
	}

	public Integer getGeneration() {
		return generation;
	}

	public void setGeneration(Integer generation) {
		this.generation = generation;
	}

	public Boolean getForce() {
		return force;
	}

	public void setForce(Boolean force) {
		this.force = force;
	}
}
