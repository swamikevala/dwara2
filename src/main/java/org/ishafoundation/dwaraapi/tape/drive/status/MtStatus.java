package org.ishafoundation.dwaraapi.tape.drive.status;

public class MtStatus {

	private boolean busy; // if response has - Device or resource busy busy = true
	private int fileNumber;
	private int blockNumber;
	private int softErrorCount;
	private boolean ready; // if ONLINE ready = true, if DR_OPEN ready = false
	private TapeDriveStatusCode statusCode;
	private boolean isWriteProtected;
	
	public boolean isBusy() {
		return busy;
	}
	public void setBusy(boolean busy) {
		this.busy = busy;
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
	public boolean isReady() {
		return ready;
	}
	public void setReady(boolean ready) {
		this.ready = ready;
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
		return "busy : " + busy + " fileNumber : " + fileNumber + " blockNumber : " + blockNumber + " softErrorCount : " + softErrorCount + " ready : " + ready + " statusCode : " + statusCode + " isWriteProtected : " + isWriteProtected;
	}
}
