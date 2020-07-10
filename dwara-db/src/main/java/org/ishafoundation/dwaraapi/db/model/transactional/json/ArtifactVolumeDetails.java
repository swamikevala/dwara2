package org.ishafoundation.dwaraapi.db.model.transactional.json;

public class ArtifactVolumeDetails {
	private String archive_id;
	private Integer start_volume_block;
	private Integer end_volume_block;
	
	public String getArchive_id() {
		return archive_id;
	}
	public void setArchive_id(String archive_id) {
		this.archive_id = archive_id;
	}
	public Integer getStart_volume_block() {
		return start_volume_block;
	}
	public void setStart_volume_block(Integer start_volume_block) {
		this.start_volume_block = start_volume_block;
	}
	public Integer getEnd_volume_block() {
		return end_volume_block;
	}
	public void setEnd_volume_block(Integer end_volume_block) {
		this.end_volume_block = end_volume_block;
	}

	// TODO : equals and hashCode
}
