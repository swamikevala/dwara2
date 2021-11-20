package org.ishafoundation.dwaraapi.api.req.ingest;

import java.util.List;

public class UserRequest {
	
	private String artifactclass;

	private List<RequestParams> artifact;

	
	public String getArtifactclass() {
		return artifactclass;
	}

	public void setArtifactclass(String artifactclass) {
		this.artifactclass = artifactclass;
	}

	public List<RequestParams> getArtifact() {
		return artifact;
	}

	public void setArtifact(List<RequestParams> artifact) {
		this.artifact = artifact;
	}
}
