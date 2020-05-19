package org.ishafoundation.dwaraapi.db.model;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.Tape;
import org.ishafoundation.dwaraapi.db.model.master.Tapelibrary;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Tapedrivetype;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;

@Entity
@Table(name="tapedrive")
public class Tapedrive {

	@Id
	@Column(name="id")
	private int id;
	
	@ManyToOne // Many tapedrives of a same type 
	private Tapedrivetype tapedrivetype;
	
	@Column(name="device_wwid", unique=true)
	private String deviceWwid;

	@Column(name="element_address", unique=true)
	private Integer elementAddress;

	// Many tapedrives for a library - hence ManytoOne
	// unidirectional reference on client side is enough
	@ManyToOne
	private Tapelibrary tapelibrary;

	@Column(name="drive_status")
	private String status;

	// This is needed to getTheCurrentlyRunningTapeJobs
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

	public Tapedrivetype getTapedrivetype() {
		return tapedrivetype;
	}

	public void setTapedrivetype(Tapedrivetype tapedrivetype) {
		this.tapedrivetype = tapedrivetype;
	}

	public String getDeviceWwid() {
		return deviceWwid;
	}

	public void setDeviceWwid(String deviceWwid) {
		this.deviceWwid = deviceWwid;
	}

	public Integer getElementAddress() {
		return elementAddress;
	}

	public void setElementAddress(Integer elementAddress) {
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