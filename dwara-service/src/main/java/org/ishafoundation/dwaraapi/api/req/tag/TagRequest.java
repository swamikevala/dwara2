package org.ishafoundation.dwaraapi.api.req.tag;

public class TagRequest {
    private String[] tags;
    private int[] requestIds;

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public int[] getRequestIds() {
        return requestIds;
    }

    public void setRequestIds(int[] requestIds) {
        this.requestIds = requestIds;
    }
}
