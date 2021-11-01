package org.ishafoundation.dwaraapi.db.model.transactional;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.JsonNode;

import javax.persistence.Embeddable;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestoreBucketFile {
    @JsonUnwrapped
    private JsonNode body;
    @JsonProperty("artifactId")
    int artifactId;
    @JsonProperty("artifactClass")
    String artifactClass;
    @JsonProperty("fileId")
    int fileID;
    @JsonProperty("pathname")
    String filePathName;
    @JsonProperty("size")
    String fileSize;
    @JsonProperty("previewProxyPath")
    List<String> previewProxyPath;
    public int getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(int artifactId) {
        this.artifactId = artifactId;
    }

    public String getArtifactClass() {
        return artifactClass;
    }

    public void setArtifactClass(String artifactClass) {
        this.artifactClass = artifactClass;
    }

    public int getFileID() {
        return fileID;
    }

    public void setFileID(int fileID) {
        this.fileID = fileID;
    }

    public String getFilePathName() {
        return filePathName;
    }

    public void setFilePathName(String filePathName) {
        this.filePathName = filePathName;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public List<String> getPreviewProxyPath() {
        return previewProxyPath;
    }

    public void setPreviewProxyPath(List<String> previewProxyPath) {
        this.previewProxyPath = previewProxyPath;
    }


}
