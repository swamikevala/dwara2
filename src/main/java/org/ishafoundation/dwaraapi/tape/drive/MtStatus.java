package org.ishafoundation.dwaraapi.tape.drive;

public class MtStatus {

	private boolean isBusy; // if response has - Device or resource busy isBusy = true
	private int fileNumber;
	private int blockNumber;
	private int softErrorCount;
	private boolean isDriveReady; // if ONLINE isDriveReady = true, if DR_OPEN isDriveReady = false
	private TapeDriveStatusCode statusCode;
	private boolean isWriteProtected;
	
	public boolean isBusy() {
		return isBusy;
	}
	public void setBusy(boolean isBusy) {
		this.isBusy = isBusy;
	}
	public int getFileNumber() {
		return fileNumber;
	}
	public void setFileNumber(int fileNumber) {
		this.fileNumber = fileNumber;
	}
	public int getBlockNumber() {
		return blockNumber;
	}
	public void setBlockNumber(int blockNumber) {
		this.blockNumber = blockNumber;
	}
	public int getSoftErrorCount() {
		return softErrorCount;
	}
	public void setSoftErrorCount(int softErrorCount) {
		this.softErrorCount = softErrorCount;
	}
	public boolean isDriveReady() {
		return isDriveReady;
	}
	public void setDriveReady(boolean isDriveReady) {
		this.isDriveReady = isDriveReady;
	}
	public TapeDriveStatusCode getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(TapeDriveStatusCode statusCode) {
		this.statusCode = statusCode;
	}
	public boolean isWriteProtected() {
		return isWriteProtected;
	}
	public void setWriteProtected(boolean isWriteProtected) {
		this.isWriteProtected = isWriteProtected;
	}

	@Override
	public String toString() {
		return "isBusy : " + isBusy + " fileNumber : " + fileNumber + " blockNumber : " + blockNumber + " softErrorCount : " + softErrorCount + " isDriveReady : " + isDriveReady + " statusCode : " + statusCode + " isWriteProtected : " + isWriteProtected;
	}
}
