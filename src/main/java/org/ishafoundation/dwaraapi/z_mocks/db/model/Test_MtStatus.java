package org.ishafoundation.dwaraapi.z_mocks.db.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.storage.storagetype.tape.drive.status.TapeDriveStatusCode;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="test_mt_status")
public class Test_MtStatus {

	@Id
	private int id;
	
	private boolean busy; // if response has - Device or resource busy isBusy = true
	private Integer fileNumber;
	private Integer blockNumber;
	private Integer softErrorCount;
	private boolean ready; // if ONLINE isDriveReady = true, if DR_OPEN isDriveReady = false
	private TapeDriveStatusCode statusCode;
	private boolean isWriteProtected;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
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
		return "isBusy : " + busy + " fileNumber : " + fileNumber + " blockNumber : " + blockNumber + " softErrorCount : " + softErrorCount + " isDriveReady : " + ready + " statusCode : " + statusCode + " isWriteProtected : " + isWriteProtected;
	}
}
