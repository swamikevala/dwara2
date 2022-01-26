package org.ishafoundation.dwaraapi.api.resp.clip;

import java.util.List;

public class ClipArtifactResponse {
    String name;
    List<ClipResponse> clipResponseList;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ClipResponse> getClipResponseList() {
        return clipResponseList;
    }

    public void setClipResponseList(List<ClipResponse> clipResponseList) {
        this.clipResponseList = clipResponseList;
    }
}
