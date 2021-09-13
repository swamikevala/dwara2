package org.ishafoundation.dwaraapi.api.resp.request;

import java.util.List;

import org.ishafoundation.dwaraapi.api.resp.job.GroupedJobResponse;
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
	"jobList",
	"tags"
})
public class RequestResponse {

    private Integer id;
    private String type;
    private Integer userRequestId; // if requestType is system
    private String requestedAt;
    private String completedAt;
    private String requestedBy;
    private String action;
    private String status;
    private String message;
    
    private List<RequestResponse> request;
    
    // For ingest
    private Artifact artifact;

    // For restore
    private Integer copyId;
	private String destinationPath;
	private String outputFolder;
    private File file;
   
    // The job List for the each request
    private List<JobResponse> job;
    
    private List<GroupedJobResponse> groupedJob;
    
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

	public String getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(String completedAt) {
		this.completedAt = completedAt;
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

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<RequestResponse> getRequest() {
		return request;
	}

	public void setRequest(List<RequestResponse> request) {
		this.request = request;
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

	public List<JobResponse> getJob() {
		return job;
	}

	public void setJob(List<JobResponse> job) {
		this.job = job;
	}
	
	public List<GroupedJobResponse> getGroupedJob() {
		return groupedJob;
	}

	public void setGroupedJob(List<GroupedJobResponse> groupedJob) {
		this.groupedJob = groupedJob;
	}

	public VolumeResponse getVolume() {
		return volume;
	}

	public void setVolume(VolumeResponse volume) {
		this.volume = volume;
	}
}

