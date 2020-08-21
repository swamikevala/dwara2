package org.ishafoundation.dwaraapi.api.resp.request;

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
    "volume"
})
public class RequestResponse {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("type")
    private String type;
    @JsonProperty("userRequestId")
    private Integer userRequestId;
    @JsonProperty("requestedAt")
    private String requestedAt;
    @JsonProperty("requestedBy")
    private String requestedBy;
    @JsonProperty("action")
    private String action;
    @JsonProperty("status")
    private String status;
    @JsonProperty("artifact")
    private Artifact artifact;

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("userRequestId")
    public Integer getUserRequestId() {
        return userRequestId;
    }

    @JsonProperty("userRequestId")
    public void setUserRequestId(Integer userRequestId) {
        this.userRequestId = userRequestId;
    }

    @JsonProperty("requestedAt")
    public String getRequestedAt() {
        return requestedAt;
    }

    @JsonProperty("requestedAt")
    public void setRequestedAt(String requestedAt) {
        this.requestedAt = requestedAt;
    }

    @JsonProperty("requestedBy")
    public String getRequestedBy() {
        return requestedBy;
    }

    @JsonProperty("requestedBy")
    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }

    @JsonProperty("action")
    public String getAction() {
        return action;
    }

    @JsonProperty("action")
    public void setAction(String action) {
        this.action = action;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("artifact")
    public Artifact getArtifact() {
        return artifact;
    }

    @JsonProperty("artifact")
    public void setArtifact(Artifact artifact) {
        this.artifact = artifact;
    }

}

