package org.ishafoundation.dwaraapi.api.resp.clip;

import java.util.List;

public class ClipListResponse {
    String name;
    int createdBy;
    String createdOn;
    List<ClipArtifactResponse> clipArtifactResponseResponseList;
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public List<ClipArtifactResponse> getClipArtifactResponseResponseList() {
        return clipArtifactResponseResponseList;
    }

    public void setClipArtifactResponseResponseList(List<ClipArtifactResponse> clipArtifactResponseResponseList) {
        this.clipArtifactResponseResponseList = clipArtifactResponseResponseList;
    }



}
