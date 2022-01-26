package org.ishafoundation.dwaraapi.artifact;

public class ArtifactMeta {
	private String artifactName = null;
	private String sequenceCode =  null;
	private String prevSequenceCode =  null;

	public String getArtifactName() {
		return artifactName;
	}
	public void setArtifactName(String artifactName) {
		this.artifactName = artifactName;
	}
	public String getSequenceCode() {
		return sequenceCode;
	}
	public void setSequenceCode(String sequenceCode) {
		this.sequenceCode = sequenceCode;
	}
	public String getPrevSequenceCode() {
		return prevSequenceCode;
	}
	public void setPrevSequenceCode(String prevSequenceCode) {
		this.prevSequenceCode = prevSequenceCode;
	}
}
