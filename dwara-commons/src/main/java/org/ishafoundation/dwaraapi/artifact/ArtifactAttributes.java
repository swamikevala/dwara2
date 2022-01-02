package org.ishafoundation.dwaraapi.artifact;

public class ArtifactAttributes{
	private String previousCode;
	private Integer sequenceNumber;
	private Boolean keepCode;
	private Boolean replaceCode;
	
	public String getPreviousCode() {
		return previousCode;
	}
	public void setPreviousCode(String previousCode) {
		this.previousCode = previousCode;
	}
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public Boolean getKeepCode() {
		return keepCode;
	}
	public void setKeepCode(Boolean keepCode) {
		this.keepCode = keepCode;
	}
	public Boolean getReplaceCode() {
		return replaceCode;
	}
	public void setReplaceCode(Boolean replaceCode) {
		this.replaceCode = replaceCode;
	}
	
	@Override
	public String toString() {
		return "\tpreviousCode - " + previousCode + "\n\tsequenceNumber - " + sequenceNumber + "\n\tkeepCode - " + keepCode + "\n\treplaceCode - " + replaceCode;
	}		
} 
