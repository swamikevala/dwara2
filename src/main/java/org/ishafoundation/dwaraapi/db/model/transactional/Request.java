package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.constants.Requesttype;
import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Targetvolume;
import org.ishafoundation.dwaraapi.db.model.master.User;

import com.fasterxml.jackson.annotation.JsonIgnore;


@Entity
@Table(name="request")
public class Request {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="id")
	private int id;
	
	@Column(name="requesttype_id")
	private Requesttype requesttype;

	@OneToOne(optional = true)
	private Libraryclass libraryclass;

	// One request can be raised by only one user
	@OneToOne(fetch = FetchType.LAZY)
	private User user;
	
	@Column(name="requested_at")
	private LocalDateTime requestedAt;

	@OneToOne(optional = true)
	private Targetvolume targetvolume;

	@Column(name="output_folder")
	private String outputFolder;
	
	@Column(name="copy_number")
	private int copyNumber;	

	
	// Many Requests like hold and release can all be referencing the same parent request
	// holds the parent request that needs to be actioned(secondary actions like cancel etc.,)
	//@Column(name="request_ref_id")
	@ManyToOne(fetch = FetchType.LAZY)
    private Request requestRef;

	// Many Requests on a subrequest like deleted/cancelled etc., are possible - Hence ManyToOne
	// holds the primary subrequest that is requested to be canceled/held/release
	@ManyToOne(fetch = FetchType.LAZY) 
    private Subrequest subrequest;
	
	//library_id (fk)
	// Eg. Many rename requests could have been requested on the same library
	@ManyToOne(fetch = FetchType.LAZY) 
	private Library library;
	
	//job_id (fk)
	// Eg. job abortion - only one job per request...
	@OneToOne
	private Job job;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Requesttype getRequesttype() {
		return requesttype;
	}

	public void setRequesttype(Requesttype requesttype) {
		this.requesttype = requesttype;
	}

	public Libraryclass getLibraryclass() {
		return libraryclass;
	}

	public void setLibraryclass(Libraryclass libraryclass) {
		this.libraryclass = libraryclass;
	}

	@JsonIgnore
	public User getUser() {
		return user;
	}

	@JsonIgnore
	public void setUser(User user) {
		this.user = user;
	}

	public int getUserId() {
		return user.getId();
	}
	
	public LocalDateTime getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(LocalDateTime requestedAt) {
		this.requestedAt = requestedAt;
	}

	public Targetvolume getTargetvolume() {
		return targetvolume;
	}

	public void setTargetvolume(Targetvolume targetvolume) {
		this.targetvolume = targetvolume;
	}

	public String getOutputFolder() {
		return outputFolder;
	}

	public void setOutputFolder(String outputFolder) {
		this.outputFolder = outputFolder;
	}

	public int getCopyNumber() {
		return copyNumber;
	}

	public void setCopyNumber(int copyNumber) {
		this.copyNumber = copyNumber;
	}

	public Request getRequestRef() {
		return requestRef;
	}

	public void setRequestRef(Request requestRef) {
		this.requestRef = requestRef;
	}

	public Subrequest getSubrequest() {
		return subrequest;
	}

	public void setSubrequest(Subrequest subrequest) {
		this.subrequest = subrequest;
	}

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		this.library = library;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}
}