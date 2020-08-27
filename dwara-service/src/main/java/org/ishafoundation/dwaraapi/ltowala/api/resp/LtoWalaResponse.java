package org.ishafoundation.dwaraapi.ltowala.api.resp;

import java.util.List;

public class LtoWalaResponse {

	private String startDate;
	private String endDate;
	private List<Artifact> artifact;
	
	public String getStartDate() {
		return startDate;
	}
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}
	public String getEndDate() {
		return endDate;
	}
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}
	public List<Artifact> getArtifact() {
		return artifact;
	}
	public void setArtifact(List<Artifact> artifact) {
		this.artifact = artifact;
	}
}
