package org.ishafoundation.dwaraapi.api.resp.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RestoreFile {
	String name;
	long size;
	long eta;
	String status;
	String tape;

	int jobId;

	public int getSystemRequestId() {
		return systemRequestId;
	}

	public void setSystemRequestId(int systemRequestId) {
		this.systemRequestId = systemRequestId;
	}

	int systemRequestId;

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public long getEta() {
		return eta;
	}
	public void setEta(long eta) {
		this.eta = eta;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getTape() {
		return tape;
	}
	public void setTape(String tape) {
		this.tape = tape;
	}


}
