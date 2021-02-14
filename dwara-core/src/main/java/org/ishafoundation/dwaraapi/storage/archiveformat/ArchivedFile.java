package org.ishafoundation.dwaraapi.storage.archiveformat;

public class ArchivedFile {
	private String filePathName;
	private Long archiveBlock; // running total start Archive block of a file
	private Integer volumeBlock; // running total start Volume block of a file (entire volume level)
	
	public String getFilePathName() {
		return filePathName;
	}
	public void setFilePathName(String filePathName) {
		this.filePathName = filePathName;
	}
	public Long getArchiveBlock() {
		return archiveBlock;
	}
	public void setArchiveBlock(Long archiveBlock) {
		this.archiveBlock = archiveBlock;
	}
	public Integer getVolumeBlock() {
		return volumeBlock;
	}
	public void setVolumeBlock(Integer volumeBlock) {
		this.volumeBlock = volumeBlock;
	}
	@Override
	public String toString() {
		return "filePathName : " + filePathName + " archiveBlock : " + archiveBlock + " volumeBlock : " + volumeBlock;
	}
}
