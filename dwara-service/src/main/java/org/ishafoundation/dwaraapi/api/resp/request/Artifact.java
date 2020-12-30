package org.ishafoundation.dwaraapi.api.resp.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "artifactclass",
    "stagedFilename",
    "stagedFilepath",
    "skipActionElements",
    "rerunNo",
    "prevSequenceCode"
})
public class Artifact {
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("deleted")
    private boolean deleted;
    @JsonProperty("artifactclass")
    private String artifactclass;
    @JsonProperty("stagedFilename")
    private String stagedFilename;
    @JsonProperty("stagedFilepath")
    private String stagedFilepath;
    @JsonProperty("skipActionElements")
    private List<Integer> skipActionElements = null;
    @JsonProperty("rerunNo")
    private Integer rerunNo;
    @JsonProperty("prevSequenceCode")
    private String prevSequenceCode;
    @JsonProperty("sequenceCode")
    private String sequenceCode;
    public Integer getId() {
		return id;
	}

	public String getSequenceCode() {
		return sequenceCode;
	}

	public void setSequenceCode(String sequenceCode) {
		this.sequenceCode = sequenceCode;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	@JsonProperty("artifactclass")
    public String getArtifactclass() {
        return artifactclass;
    }

    @JsonProperty("artifactclass")
    public void setArtifactclass(String artifactclass) {
        this.artifactclass = artifactclass;
    }

    @JsonProperty("stagedFilename")
    public String getStagedFilename() {
        return stagedFilename;
    }

    @JsonProperty("stagedFilename")
    public void setStagedFilename(String stagedFilename) {
        this.stagedFilename = stagedFilename;
    }

    @JsonProperty("stagedFilepath")
    public String getStagedFilepath() {
        return stagedFilepath;
    }

    @JsonProperty("stagedFilepath")
    public void setStagedFilepath(String stagedFilepath) {
        this.stagedFilepath = stagedFilepath;
    }

    @JsonProperty("skipActionElements")
    public List<Integer> getSkipActionElements() {
        return skipActionElements;
    }

    @JsonProperty("skipActionElements")
    public void setSkipActionElements(List<Integer> skipActionElements) {
        this.skipActionElements = skipActionElements;
    }

    @JsonProperty("rerunNo")
    public Integer getRerunNo() {
        return rerunNo;
    }

    @JsonProperty("rerunNo")
    public void setRerunNo(Integer rerunNo) {
        this.rerunNo = rerunNo;
    }

    @JsonProperty("prevSequenceCode")
    public String getPrevSequenceCode() {
        return prevSequenceCode;
    }

    @JsonProperty("prevSequenceCode")
    public void setPrevSequenceCode(String prevSequenceCode) {
        this.prevSequenceCode = prevSequenceCode;
    }
}
