package org.ishafoundation.dwaraapi.api.resp.staged.ingest;

import java.util.ArrayList;
import java.util.List;

public class IngestSystemRequest {

	private int id;
	private String stagedFilePath;
	private List<Integer> skippedActionElements = new ArrayList<Integer>();
	private int rerunNo;
	private Artifact artifact;

	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getStagedFilePath() {
		return stagedFilePath;
	}
	public void setStagedFilePath(String stagedFilePath) {
		this.stagedFilePath = stagedFilePath;
	}
	public List<Integer> getSkippedActionElements() {
		return skippedActionElements;
	}
	public void setSkippedActionElements(List<Integer> skippedActionElements) {
		this.skippedActionElements = skippedActionElements;
	}
	public int getRerunNo() {
		return rerunNo;
	}
	public void setRerunNo(int rerunNo) {
		this.rerunNo = rerunNo;
	}
	public Artifact getArtifact() {
		return artifact;
	}
	public void setArtifact(Artifact artifact) {
		this.artifact = artifact;
	}
}

