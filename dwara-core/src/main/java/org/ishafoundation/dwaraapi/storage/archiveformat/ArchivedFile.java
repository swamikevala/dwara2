package org.ishafoundation.dwaraapi.storage.archiveformat;

public class ArchivedFile {
	private String filePathName;
	private int archiveBlockOffset;
	private Integer volumeBlockOffset;
	
	public String getFilePathName() {
		return filePathName;
	}
	public void setFilePathName(String filePathName) {
		this.filePathName = filePathName;
	}
	public int getArchiveBlockOffset() {
		return archiveBlockOffset;
	}
	public void setArchiveBlockOffset(int archiveBlockOffset) {
		this.archiveBlockOffset = archiveBlockOffset;
	}
	public Integer getVolumeBlockOffset() {
		return volumeBlockOffset;
	}
	public void setVolumeBlockOffset(Integer volumeBlockOffset) {
		this.volumeBlockOffset = volumeBlockOffset;
	}
	@Override
	public String toString() {
		return "filePathName : " + filePathName + " archiveBlockOffset : " + archiveBlockOffset + " volumeBlockOffset : " + volumeBlockOffset;
	}
}
