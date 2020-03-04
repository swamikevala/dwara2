package org.ishafoundation.dwaraapi.api.resp.restore;

import java.util.List;

public class Request {
	private int requestId;
	
	private int requesttypeId;

	private String requestedBy;

	private long requestedAt;

	private int copyNumber;
	
	private int targetvolumeId;

	private String outputFolder;

	private List<SubrequestResp> subrequestResp;

	
	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getRequesttypeId() {
		return requesttypeId;
	}

	public void setRequesttypeId(int requesttypeId) {
		this.requesttypeId = requesttypeId;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public long getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(long requestedAt) {
		this.requestedAt = requestedAt;
	}

	public int getCopyNumber() {
		return copyNumber;
	}

	public void setCopyNumber(int copyNumber) {
		this.copyNumber = copyNumber;
	}

	public int getTargetvolumeId() {
		return targetvolumeId;
	}

	public void setTargetvolumeId(int targetvolumeId) {
		this.targetvolumeId = targetvolumeId;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public List<SubrequestResp> getSubrequestResp() {
		return subrequestResp;
	}

	public void setSubrequestResp(List<SubrequestResp> subrequestResp) {
		this.subrequestResp = subrequestResp;
	}
}
