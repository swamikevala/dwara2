package org.ishafoundation.dwaraapi.storage.archiveformat.tar.response.components;

public class File {

	private String filePathName;
	private Long fileSize;
	private Long archiveBlock;
//	private int volumeBlock;


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
	public Long getArchiveBlock() {
		return archiveBlock;
	}
	public void setArchiveBlock(Long archiveBlock) {
		this.archiveBlock = archiveBlock;
	}
//	public int getVolumeBlock() {
//		return volumeBlock;
//	}
//	public void setVolumeBlock(int volumeBlock) {
//		this.volumeBlock = volumeBlock;
//	}
	@Override
	public String toString() {
		//return "filePathName : " + filePathName + " archiveBlock : " + archiveBlock + " volumeBlock : " + volumeBlock;
		return "filePathName : " + filePathName + " archiveBlock : " + archiveBlock;
	}
}
