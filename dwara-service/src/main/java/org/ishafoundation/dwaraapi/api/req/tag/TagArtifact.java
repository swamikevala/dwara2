package org.ishafoundation.dwaraapi.api.req.tag;

public class TagArtifact {
    private String[] tags;
    private int[] artifactIds;

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    public int[] getArtifactIds() {
        return artifactIds;
    }

    public void setArtifactIds(int[] artifactIds) {
        this.artifactIds = artifactIds;
    }
}
