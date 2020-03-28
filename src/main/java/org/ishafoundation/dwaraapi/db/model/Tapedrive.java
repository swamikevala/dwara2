package org.ishafoundation.dwaraapi.db.model;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;

@Entity
@Table(name="tapedrive")
public class Tapedrive {

	@Id
	@Column(name="id")
	private int id;
	
	@Column(name="device_wwid", unique=true)
	private String deviceWwid;

	@Column(name="element_address", unique=true)
	private int elementAddress;

	// unidirectional reference on client side is enough
	@ManyToOne
	private Tapelibrary tapelibrary;

	@Column(name="status")
	private String status;
	
	// unidirectional reference is enough
	@OneToOne(optional=true)
	private Job job;
	
	// unidirectional reference is enough
	@OneToOne(optional=true)
	private Tape tape;
	
	@Column(name="serial_number", unique=true)
	private String serialNumber;
	
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public Tapelibrary getTapelibrary() {
		return tapelibrary;
	}

	public void setTapelibrary(Tapelibrary tapelibrary) {
		this.tapelibrary = tapelibrary;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Tape getTape() {
		return tape;
	}

	public void setTape(Tape tape) {
		this.tape = tape;
	}

	public String getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(String serialNumber) {
		this.serialNumber = serialNumber;
	}
}