package org.ishafoundation.dwara.import_.bru;

public class BruFile {
    public long size;
    public String name;
    public String category;
    public Long startVolumeBlock;
    public String archiveBlock;
    public String archiveId;
    //calculated data
    public boolean isArtifact;
    public boolean isDirectory;
    public int numFiles;
    public Long endVolumeBlock;
    public String artifactName;
	public String sequenceCode;
	public String prevSequenceCode;
    
    public String toString() {
        return "name: " + name + ", category: " + category + ",size: " + size + ", startVolumeBlock: " + startVolumeBlock + "archiveBlock: " + archiveBlock + ", archiveId: " + archiveId +
        ", isDirectory: " + isArtifact + ", numFiles: " + numFiles + ", endVolumeBlock: " + endVolumeBlock + ", artifactName: " + artifactName + ", prevSequence: "
        + prevSequenceCode + ", sequenceCode: " + sequenceCode;
    }
}