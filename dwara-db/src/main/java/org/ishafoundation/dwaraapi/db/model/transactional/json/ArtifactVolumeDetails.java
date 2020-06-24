package org.ishafoundation.dwaraapi.db.model.transactional.json;

public class ArtifactVolumeDetails {
	private String archive_id;
	private Integer start_volume_block;
	private Integer total_volume_blocks;
	
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
	public Integer getTotal_volume_blocks() {
		return total_volume_blocks;
	}
	public void setTotal_volume_blocks(Integer total_volume_blocks) {
		this.total_volume_blocks = total_volume_blocks;
	}
}
