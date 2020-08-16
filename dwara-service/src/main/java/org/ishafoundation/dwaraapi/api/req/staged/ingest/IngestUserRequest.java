package org.ishafoundation.dwaraapi.api.req.staged.ingest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngestUserRequest {
	private String artifactclass;
	
	private List<Integer> skipActionelements;

	private List<StagedFile> stagedFiles;

	public String getArtifactclass() {
		return artifactclass;
	}

	public void setArtifactclass(String artifactclass) {
		this.artifactclass = artifactclass;
	}

	public List<Integer> getSkipActionelements() {
		return skipActionelements;
	}

	public void setSkipActionelements(List<Integer> skipActionelements) {
		this.skipActionelements = skipActionelements;
	}

	public List<StagedFile> getStagedFiles() {
		return stagedFiles;
	}

	public void setStagedFiles(List<StagedFile> stagedFiles) {
		this.stagedFiles = stagedFiles;
	}
}
