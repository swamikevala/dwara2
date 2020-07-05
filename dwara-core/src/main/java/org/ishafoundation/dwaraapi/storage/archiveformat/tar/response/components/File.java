package org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.components;

public class File {

	private String filePathName;
	private Long fileSize;
	private int archiveBlockOffset;
//	private int volumeBlockOffset;


	public String getFilePathName() {
		return filePathName;
	}
	public void setFilePathName(String filePathName) {
		this.filePathName = filePathName;
	}
	public Long getFileSize() {
		return fileSize;
	}
	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}
	public int getArchiveBlockOffset() {
		return archiveBlockOffset;
	}
	public void setArchiveBlockOffset(int archiveBlockOffset) {
		this.archiveBlockOffset = archiveBlockOffset;
	}
//	public int getVolumeBlockOffset() {
//		return volumeBlockOffset;
//	}
//	public void setVolumeBlockOffset(int volumeBlockOffset) {
//		this.volumeBlockOffset = volumeBlockOffset;
//	}
	@Override
	public String toString() {
		//return "filePathName : " + filePathName + " archiveBlockOffset : " + archiveBlockOffset + " volumeBlockOffset : " + volumeBlockOffset;
		return "filePathName : " + filePathName + " archiveBlockOffset : " + archiveBlockOffset;
	}
}
