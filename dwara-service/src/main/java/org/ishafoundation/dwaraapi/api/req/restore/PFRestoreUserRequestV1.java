package org.ishafoundation.dwaraapi.api.req.restore;

import java.util.List;

import org.ishafoundation.dwaraapi.enumreferences.Priority;

public class PFRestoreUserRequestV1 {
	private Integer copy;
	private String outputFolder;
	private String destinationPath;
	private List<FileDetailsV1> file;
	private String flow;
	private Priority priority;
	private String vpJiraTicket;

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
	public List<FileDetailsV1> getFile() {
		return file;
	}
	public void setFile(List<FileDetailsV1> file) {
		this.file = file;
	}
	public String getFlow() {
		return flow;
	}
	public void setFlow(String flow) {
		this.flow = flow;
	}
	public Priority getPriority() {
		return priority;
	}
	public void setPriority(Priority priority) {
		this.priority = priority;
	}
	public String getVpJiraTicket() {
		return vpJiraTicket;
	}
	public void setVpJiraTicket(String vpJiraTicket) {
		this.vpJiraTicket = vpJiraTicket;
	}
}
