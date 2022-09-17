package org.ishafoundation.dwaraapi.api.req;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenerateMezzanineProxiesRequest {
	   
	private String artifactclassRegex;
	private String artifactRegex;
	
	public String getArtifactclassRegex() {
		return artifactclassRegex;
	}
	public void setArtifactclassRegex(String artifactclassRegex) {
		this.artifactclassRegex = artifactclassRegex;
	}
	public String getArtifactRegex() {
		return artifactRegex;
	}
	public void setArtifactRegex(String artifactRegex) {
		this.artifactRegex = artifactRegex;
	}
}
