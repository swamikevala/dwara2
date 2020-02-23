package org.ishafoundation.dwaraapi.storage.storageformat.bru.response.components;

public class File {
	private int blockNumber;
	private String fileName;
	
	public int getBlockNumber() {
		return blockNumber;
	}
	public void setBlockNumber(int blockNumber) {
		this.blockNumber = blockNumber;
	}
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	@Override
	public String toString() {
		return "blockNumber : " + blockNumber + " fileName : " + fileName;
	}
}
