package org.ishafoundation.dwaraapi.db.model.mocks;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.TapeDriveStatusCode;

@Entity
@Table(name="zmock_mt_status")
public class MockMtStatus{// TODO Find why inheritance isnt bringing the fields to table.. extends MtStatus{

	@Id
	private int id;
	
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	private boolean busy; // if response has - Device or resource busy busy = true
	private Integer fileNumber;
	private Integer blockNumber;
	private Integer softErrorCount;
	private boolean ready; // if ONLINE ready = true, if DR_OPEN ready = false
	private TapeDriveStatusCode statusCode;
	private boolean isWriteProtected;
	
	public boolean isBusy() {
		return busy;
	}
	public void setBusy(boolean busy) {
		this.busy = busy;
	}
	public Integer getFileNumber() {
		return fileNumber;
	}
	public void setFileNumber(Integer fileNumber) {
		this.fileNumber = fileNumber;
	}
	public Integer getBlockNumber() {
		return blockNumber;
	}
	public void setBlockNumber(Integer blockNumber) {
		this.blockNumber = blockNumber;
	}
	public Integer getSoftErrorCount() {
		return softErrorCount;
	}
	public void setSoftErrorCount(Integer softErrorCount) {
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
		return "isBusy : " + isBusy() + " fileNumber : " + getFileNumber() + " blockNumber : " + getBlockNumber() + " softErrorCount : " + getSoftErrorCount() + " isDriveReady : " + isReady() + " statusCode : " + getStatusCode() + " isWriteProtected : " + isWriteProtected();
	}
}
