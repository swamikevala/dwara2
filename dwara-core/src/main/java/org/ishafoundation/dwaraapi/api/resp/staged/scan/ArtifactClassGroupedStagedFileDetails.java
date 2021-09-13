package org.ishafoundation.dwaraapi.api.resp.staged.scan;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ArtifactClassGroupedStagedFileDetails {
	private String artifactclass;
	private List<String> volumeGroup;
	private Integer artifactTotalCount;
	private Integer artifactWarnCount;
	private Integer artifactErrorCount;
	private List<StagedFileDetails> artifact;

	public String getArtifactclass() {
		return artifactclass;
	}
	public void setArtifactclass(String artifactclass) {
		this.artifactclass = artifactclass;
	}
	public List<String> getVolumeGroup() {
		return volumeGroup;
	}
	public void setVolumeGroup(List<String> volumeGroup) {
		this.volumeGroup = volumeGroup;
	}
	public Integer getArtifactTotalCount() {
		return artifactTotalCount;
	}
	public void setArtifactTotalCount(Integer artifactTotalCount) {
		this.artifactTotalCount = artifactTotalCount;
	}
	public Integer getArtifactWarnCount() {
		return artifactWarnCount;
	}
	public void setArtifactWarnCount(Integer artifactWarnCount) {
		this.artifactWarnCount = artifactWarnCount;
	}
	public Integer getArtifactErrorCount() {
		return artifactErrorCount;
	}
	public void setArtifactErrorCount(Integer artifactErrorCount) {
		this.artifactErrorCount = artifactErrorCount;
	}
	public List<StagedFileDetails> getArtifact() {
		return artifact;
	}
	public void setArtifact(List<StagedFileDetails> artifact) {
		this.artifact = artifact;
	}
}
