package org.ishafoundation.dwaraapi.api.req.restore;

import org.ishafoundation.dwaraapi.enumreferences.Priority;

import java.util.List;

public class RestoreUserRequest {
	private Integer copy;
	private String outputFolder;
	private String destinationPath;
	private String flow;
	private List<Integer> fileIds;
	private Priority priority;
	private String vpJiraTicket;
	private Boolean convert;

	public Priority getPriority() {
		return priority;
	}

	public void setPriority(Priority priority) {
		this.priority = priority;
	}

	public Integer getCopy() {
		return copy;
	}

	public void setCopy(Integer copy) {
		this.copy = copy;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public String getDestinationPath() {
		return destinationPath;
	}

	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;
	}

	public String getFlow() {
		return flow;
	}

	public void setFlow(String flow) {
		this.flow = flow;
	}

	public List<Integer> getFileIds() {
		return fileIds;
	}

	public void setFileIds(List<Integer> fileIds) {
		this.fileIds = fileIds;
	}

	public String getVpJiraTicket() {
		return vpJiraTicket;
	}

	public void setVpJiraTicket(String vpJiraTicket) {
		this.vpJiraTicket = vpJiraTicket;
	}

	public Boolean getConvert() {
		return convert;
	}

	public void setConvert(Boolean convert) {
		this.convert = convert;
	}
}
