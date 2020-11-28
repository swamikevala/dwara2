package org.ishafoundation.dwaraapi.process.request;
		
import java.util.List;

public class Job {

	private int id;
	
	private String storagetaskActionId;

	private String processingtaskId;
	
    private int flowelementId;
	
	private List<Job> dependencies;
	
	private Artifact inputArtifact; // can contain one of the domain artifacts id
	
	private Artifact outputArtifact;
	
	private Volume volume;
	
	private boolean encrypted;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getStoragetaskActionId() {
		return storagetaskActionId;
	}

	public void setStoragetaskActionId(String storagetaskActionId) {
		this.storagetaskActionId = storagetaskActionId;
	}

	public String getProcessingtaskId() {
		return processingtaskId;
	}

	public void setProcessingtaskId(String processingtaskId) {
		this.processingtaskId = processingtaskId;
	}

	public int getFlowelementId() {
		return flowelementId;
	}

	public void setFlowelementId(int flowelementId) {
		this.flowelementId = flowelementId;
	}

	public List<Job> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Job> dependencies) {
		this.dependencies = dependencies;
	}

	public Artifact getInputArtifact() {
		return inputArtifact;
	}

	public void setInputArtifact(Artifact inputArtifact) {
		this.inputArtifact = inputArtifact;
	}

	public Artifact getOutputArtifact() {
		return outputArtifact;
	}

	public void setOutputArtifact(Artifact outputArtifact) {
		this.outputArtifact = outputArtifact;
	}

	public Volume getVolume() {
		return volume;
	}

	public void setVolume(Volume volume) {
		this.volume = volume;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public void setEncrypted(boolean encrypted) {
		this.encrypted = encrypted;
	}
}