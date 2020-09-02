package org.ishafoundation.dwaraapi.db.model.transactional.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArtifactVolumeDetails {
	@JsonProperty("archive_id")
	private String archiveId;
	@JsonProperty("start_volume_block")
	private Integer startVolumeBlock;
	@JsonProperty("end_volume_block")
	private Integer endVolumeBlock;
	
	public String getArchiveId() {
		return archiveId;
	}
	public void setArchiveId(String archiveId) {
		this.archiveId = archiveId;
	}
	public Integer getStartVolumeBlock() {
		return startVolumeBlock;
	}
	public void setStartVolumeBlock(Integer startVolumeBlock) {
		this.startVolumeBlock = startVolumeBlock;
	}
	public Integer getEndVolumeBlock() {
		return endVolumeBlock;
	}
	public void setEndVolumeBlock(Integer endVolumeBlock) {
		this.endVolumeBlock = endVolumeBlock;
	}

	// TODO : equals and hashCode
}
