package org.ishafoundation.dwaraapi.api.req.staged.ingest;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class IngestUserRequest {
	private List<StagedFile> stagedFiles;

	public List<StagedFile> getStagedFiles() {
		return stagedFiles;
	}

	public void setStagedFiles(List<StagedFile> stagedFiles) {
		this.stagedFiles = stagedFiles;
	}
}
