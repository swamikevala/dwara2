package org.ishafoundation.dwaraapi.api.resp.artifactclass;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class Actionelement {
	private int id;
	private boolean active;
	private String processingTask;
	private String storagetaskAction;
	private String volume;
	private List<Integer> prerequisites;
	 
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public boolean isActive() {
		return active;
	}
	public void setActive(boolean active) {
		this.active = active;
	}
	public String getProcessingTask() {
		return processingTask;
	}
	public void setProcessingTask(String processingTask) {
		this.processingTask = processingTask;
	}
	public String getStoragetaskAction() {
		return storagetaskAction;
	}
	public void setStoragetaskAction(String storagetaskAction) {
		this.storagetaskAction = storagetaskAction;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public List<Integer> getPrerequisites() {
		return prerequisites;
	}
	public void setPrerequisites(List<Integer> prerequisites) {
		this.prerequisites = prerequisites;
	}

}
