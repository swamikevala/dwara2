package org.ishafoundation.dwaraapi.db.model;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="tapedrive")
public class Tapedrive {

	@Id
	@Column(name="tapedrive_id")
	private int tapedriveId;
	
	@Column(name="device_wwid", unique=true)
	private String deviceWwid;

	@Column(name="element_address", unique=true)
	private int elementAddress;

	@Column(name="tapelibrary_id")
	private int tapelibraryId;

	@Column(name="status")
	private String status;

	@Column(name="job_id", unique=true)
	private int jobId;

	@Column(name="tape_id", unique=true)
	private int tapeId;

	@Column(name="serial_number", unique=true)
	private String serialNumber;

		
	public int getTapedriveId() {
		return tapedriveId;
	}

	public void setTapedriveId(int tapedriveId) {
		this.tapedriveId = tapedriveId;
	}
	
	public String getDeviceWwid() {
		return deviceWwid;
	}

	public void setDeviceWwid(String deviceWwid) {
		this.deviceWwid = deviceWwid;
	}
	
	public int getElementAddress() {
		return elementAddress;
	}

	public void setElementAddress(int elementAddress) {
		this.elementAddress = elementAddress;
	}
	
	public int getTapelibraryId() {
		return tapelibraryId;
	}

	public void setTapelibraryId(int tapelibraryId) {
		this.tapelibraryId = tapelibraryId;
	}
	
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public int getTapeId() {
		return tapeId;
	}

	public void setTapeId(int tapeId) {
		this.tapeId = tapeId;
	}
	
	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}

}