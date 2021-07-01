package org.ishafoundation.dwaraapi.api.req;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RewriteRequest {
	   
	private Integer rewriteCopy;
	private int sourceCopy = 1; // defaulted to 1...
	private String mode;
	private String artifactclassRegex;
	private Integer destinationCopy;

	public Integer getRewriteCopy() {
		return rewriteCopy;
	}
	public void setRewriteCopy(Integer rewriteCopy) {
		this.rewriteCopy = rewriteCopy;
	}
	public int getSourceCopy() {
		return sourceCopy;
	}
	public void setSourceCopy(int sourceCopy) {
		this.sourceCopy = sourceCopy;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public String getArtifactclassRegex() {
		return artifactclassRegex;
	}
	public void setArtifactclassRegex(String artifactclassRegex) {
		this.artifactclassRegex = artifactclassRegex;
	}
	public Integer getDestinationCopy() {
		return destinationCopy;
	}
	public void setDestinationCopy(Integer destinationCopy) {
		this.destinationCopy = destinationCopy;
	}
}
