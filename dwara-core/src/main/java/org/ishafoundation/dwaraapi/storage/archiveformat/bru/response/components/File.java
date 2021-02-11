package org.ishafoundation.dwaraapi.storage.archiveformat.bru.response.components;

public class File {

	private String filePathName;
	private String linkName;
	private Long archiveRunningTotalDataInKB; //running total of the amount of space used in kilobytes thus far - excludes the current file size - start of the 
	private int volumeBlockOffset;


	public String getFilePathName() {
		return filePathName;
	}
	public void setFilePathName(String filePathName) {
		this.filePathName = filePathName;
	}
	public String getLinkName() {
		return linkName;
	}
	public void setLinkName(String linkName) {
		this.linkName = linkName;
	}
	public Long getArchiveRunningTotalDataInKB() {
		return archiveRunningTotalDataInKB;
	}
	public void setArchiveRunningTotalDataInKB(Long archiveRunningTotalDataInKB) {
		this.archiveRunningTotalDataInKB = archiveRunningTotalDataInKB;
	}
	public int getVolumeBlockOffset() {
		return volumeBlockOffset;
	}
	public void setVolumeBlockOffset(int volumeBlockOffset) {
		this.volumeBlockOffset = volumeBlockOffset;
	}
	@Override
	public String toString() {
		return "filePathName : " + filePathName + " archiveRunningTotalDataInKB : " + archiveRunningTotalDataInKB + " volumeBlockOffset : " + volumeBlockOffset;
	}
}
