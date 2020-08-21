package org.ishafoundation.dwaraapi.api.resp.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "storagetaskAction",
    "processingTask",
    "requestId",
    "actionelementId",
    "inputArtifactId",
    "outputArtifactId",
    "createdAt",
    "startedAt",
    "completedAt",
    "status",
    "volume"
})
public class JobResponse {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("storagetaskAction")
    private String storagetaskAction;
    @JsonProperty("processingTask")
    private String processingTask;
    @JsonProperty("requestId")
    private Integer requestId;
    @JsonProperty("actionelementId")
    private Integer actionelementId;
    @JsonProperty("inputArtifactId")
    private Integer inputArtifactId;
    @JsonProperty("outputArtifactId")
    private Integer outputArtifactId;
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

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
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

    @JsonProperty("actionelementId")
    public Integer getActionelementId() {
        return actionelementId;
    }

    @JsonProperty("actionelementId")
    public void setActionelementId(Integer actionelementId) {
        this.actionelementId = actionelementId;
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
}
