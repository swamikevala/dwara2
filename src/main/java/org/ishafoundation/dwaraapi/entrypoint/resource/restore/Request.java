package org.ishafoundation.dwaraapi.entrypoint.resource.restore;

import java.time.LocalDateTime;
import java.util.List;

public class Request {
	private int requestId;
	
	private String action;

	private String requestedBy;

	private LocalDateTime requestedAt;

	private int copyNumber;
	
	private String targetvolumeName;

	private String outputFolder;

	private List<Subrequest> subrequest;
	

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(LocalDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}

	public int getCopyNumber() {
		return copyNumber;
	}

	public void setCopyNumber(int copyNumber) {
		this.copyNumber = copyNumber;
	}

	public String getTargetvolumeName() {
		return targetvolumeName;
	}

	public void setTargetvolumeName(String targetvolumeName) {
		this.targetvolumeName = targetvolumeName;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public List<Subrequest> getSubrequest() {
		return subrequest;
	}

	public void setSubrequest(List<Subrequest> subrequest) {
		this.subrequest = subrequest;
	}


}
