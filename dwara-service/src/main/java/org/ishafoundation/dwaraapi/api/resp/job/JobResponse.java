package org.ishafoundation.dwaraapi.api.resp.job;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "jobId",
    "storagetaskAction",
    "processingTask",
    "requestId",
    "flowelementId",
    "inputArtifactId",
    "outputArtifactId",
    "dependencies",
    "createdAt",
    "startedAt",
    "completedAt",
    "status",
    "volume",
    "copy",
    "message",
    "fileFailures"
})
public class JobResponse {

    @JsonProperty("id")
    private String id;
    @JsonProperty("jobId")
    private String jobId;
    @JsonProperty("storagetaskAction")
    private String storagetaskAction;
    @JsonProperty("processingTask")
    private String processingTask;
    @JsonProperty("requestId")
    private Integer requestId;
    @JsonProperty("flowelementId")
    private String flowelementId;
    @JsonProperty("inputArtifactId")
    private Integer inputArtifactId;
    @JsonProperty("outputArtifactId")
    private Integer outputArtifactId;
    @JsonProperty("dependencies")
    private List<String> dependencies;
    @JsonProperty("createdAt")
    private String createdAt;
    @JsonProperty("startedAt")
    private String startedAt;
    @JsonProperty("completedAt")
    private String completedAt;
    @JsonProperty("status")
    private String status;
    @JsonProperty("volume")
    private String volume;
    @JsonProperty("copy")
    private Integer copy;
    @JsonProperty("message")
    private String message;
    @JsonProperty("fileFailures")
    private List<String> fileFailures; // failure reason
    
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty("jobId")
    public String getJobId() {
		return jobId;
	}

    @JsonProperty("jobId")
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	@JsonProperty("storagetaskAction")
    public String getStoragetaskAction() {
        return storagetaskAction;
    }

    @JsonProperty("storagetaskAction")
    public void setStoragetaskAction(String storagetaskAction) {
        this.storagetaskAction = storagetaskAction;
    }

    @JsonProperty("processingTask")
    public String getProcessingTask() {
        return processingTask;
    }

    @JsonProperty("processingTask")
    public void setProcessingTask(String processingTask) {
        this.processingTask = processingTask;
    }

    @JsonProperty("requestId")
    public Integer getRequestId() {
        return requestId;
    }

    @JsonProperty("requestId")
    public void setRequestId(Integer requestId) {
        this.requestId = requestId;
    }

    @JsonProperty("flowelementId")
    public String getFlowelementId() {
        return flowelementId;
    }

    @JsonProperty("flowelementId")
    public void setFlowelementId(String flowelementId) {
        this.flowelementId = flowelementId;
    }

    @JsonProperty("inputArtifactId")
    public Integer getInputArtifactId() {
        return inputArtifactId;
    }

    @JsonProperty("inputArtifactId")
    public void setInputArtifactId(Integer inputArtifactId) {
        this.inputArtifactId = inputArtifactId;
    }

    @JsonProperty("outputArtifactId")
    public Integer getOutputArtifactId() {
        return outputArtifactId;
    }

    @JsonProperty("outputArtifactId")
    public void setOutputArtifactId(Integer outputArtifactId) {
        this.outputArtifactId = outputArtifactId;
    }

    @JsonProperty("dependencies")
	public List<String> getDependencies() {
		return dependencies;
	}

    @JsonProperty("dependencies")
	public void setDependencies(List<String> dependencies) {
		this.dependencies = dependencies;
	}

    @JsonProperty("createdAt")
    public String getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("createdAt")
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("startedAt")
    public String getStartedAt() {
        return startedAt;
    }

    @JsonProperty("startedAt")
    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    @JsonProperty("completedAt")
    public String getCompletedAt() {
        return completedAt;
    }

    @JsonProperty("completedAt")
    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("volume")
    public String getVolume() {
        return volume;
    }

    @JsonProperty("volume")
    public void setVolume(String volume) {
        this.volume = volume;
    }

	public Integer getCopy() {
		return copy;
	}

	public void setCopy(Integer copy) {
		this.copy = copy;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public List<String> getFileFailures() {
		return fileFailures;
	}

	public void setFileFailures(List<String> fileFailures) {
		this.fileFailures = fileFailures;
	}
}
