package org.ishafoundation.dwaraapi.api.resp.request;

import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.job.JobResponse;
import org.ishafoundation.dwaraapi.api.resp.restore.File;
import org.ishafoundation.dwaraapi.api.resp.volume.VolumeResponse;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "type",
    "userRequestId",
    "requestedAt",
    "requestedBy",
    "action",
    "status",
    "artifact",
    "file",
    "volume",
    "jobList"
})
public class RequestResponse {

    private Integer id;
    private String type;
    private Integer userRequestId;
    private String requestedAt;
    private String requestedBy;
    private String action;
    private String status;
    
    // For ingest
    private Artifact artifact;

    // For restore
    private Integer copyId;
	private String destinationPath;
	private String outputFolder;
    private File file;
   
    // The job List for the each request
    private List<JobResponse> jobList;
    // For volume relate actions like initialize, map_drives etc.
    @JsonProperty("volume")
    private VolumeResponse volume;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getUserRequestId() {
		return userRequestId;
	}

	public void setUserRequestId(Integer userRequestId) {
		this.userRequestId = userRequestId;
	}

	public String getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(String requestedAt) {
		this.requestedAt = requestedAt;
	}

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Artifact getArtifact() {
		return artifact;
	}

	public void setArtifact(Artifact artifact) {
		this.artifact = artifact;
	}

	public Integer getCopyId() {
		return copyId;
	}

	public void setCopyId(Integer copyId) {
		this.copyId = copyId;
	}

	public String getDestinationPath() {
		return destinationPath;
	}

	public void setDestinationPath(String destinationPath) {
		this.destinationPath = destinationPath;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public VolumeResponse getVolume() {
		return volume;
	}

	public void setVolume(VolumeResponse volume) {
		this.volume = volume;
	}

	public List<JobResponse> getJobList() {
		return jobList;
	}

	public void setJobList(List<JobResponse> jobList) {
		this.jobList = jobList;
	}
	 
}

