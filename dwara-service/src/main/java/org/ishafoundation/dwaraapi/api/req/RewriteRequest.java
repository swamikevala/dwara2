package org.ishafoundation.dwaraapi.api.req;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RewriteRequest {
	   
	private Integer rewriteCopy;
	private int goodCopy = 1; // defaulted to 1...
	private String purpose;
	private String artifactclassRegex;
	private Integer additionalCopy;

	public Integer getRewriteCopy() {
		return rewriteCopy;
	}
	public void setRewriteCopy(Integer rewriteCopy) {
		this.rewriteCopy = rewriteCopy;
	}
	public int getGoodCopy() {
		return goodCopy;
	}
	public void setGoodCopy(int goodCopy) {
		this.goodCopy = goodCopy;
	}
	public String getPurpose() {
		return purpose;
	}
	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}
	public String getArtifactclassRegex() {
		return artifactclassRegex;
	}
	public void setArtifactclassRegex(String artifactclassRegex) {
		this.artifactclassRegex = artifactclassRegex;
	}
	public Integer getAdditionalCopy() {
		return additionalCopy;
	}
	public void setAdditionalCopy(Integer additionalCopy) {
		this.additionalCopy = additionalCopy;
	}
}
