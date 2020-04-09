package org.ishafoundation.dwaraapi.api.resp.restore;

import java.util.List;

public class Request {
	private int requestId;
	
	private int actionId;

	private String requestedBy;

	private long requestedAt;

	private int copyNumber;
	
	private int targetvolumeId;

	private String outputFolder;

	private List<Subrequest> subrequestList;

	
	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public int getActionId() {
		return actionId;
	}

	public void setActionId(int actionId) {
		this.actionId = actionId;
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

	public List<Subrequest> getSubrequestList() {
		return subrequestList;
	}

	public void setSubrequestList(List<Subrequest> subrequestList) {
		this.subrequestList = subrequestList;
	}
}
