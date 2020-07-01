package org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.components;

public class FilesProcessed {
	private int totalNoOfFiles;
	private int regularCnt;
	private int otherCnt;
	
	public int getTotalNoOfFiles() {
		return totalNoOfFiles;
	}
	public void setTotalNoOfFiles(int totalNoOfFiles) {
		this.totalNoOfFiles = totalNoOfFiles;
	}
	public int getRegularCnt() {
		return regularCnt;
	}
	public void setRegularCnt(int regularCnt) {
		this.regularCnt = regularCnt;
	}
	public int getOtherCnt() {
		return otherCnt;
	}
	public void setOtherCnt(int otherCnt) {
		this.otherCnt = otherCnt;
	}
	@Override
	public String toString() {
		return "totalNoOfFiles : " + totalNoOfFiles + " regularCnt : " + regularCnt + " otherCnt : " + otherCnt;
	}
}
