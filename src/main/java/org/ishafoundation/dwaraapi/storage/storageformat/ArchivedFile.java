package org.ishafoundation.dwaraapi.storage.storageformat;

public class ArchivedFile {
	private int blockNumber;
	private String filePathName;
	
	public int getBlockNumber() {
		return blockNumber;
	}
	public void setBlockNumber(int blockNumber) {
		this.blockNumber = blockNumber;
	}
	public String getFilePathName() {
		return filePathName;
	}
	public void setFilePathName(String filePathName) {
		this.filePathName = filePathName;
	}

	@Override
	public String toString() {
		return "blockNumber : " + blockNumber + " filePathName : " + filePathName;
	}
}
