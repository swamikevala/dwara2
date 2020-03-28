package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.model.master.Libraryclass;
import org.ishafoundation.dwaraapi.db.model.master.Targetvolume;
import org.ishafoundation.dwaraapi.db.model.master.reference.Requesttype;


@Entity
@Table(name="request")
public class Request {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="id")
	private int id;
	
	@OneToOne
	private Requesttype requesttype;

	@OneToOne(optional = true)
	private Libraryclass libraryclass;

	@Column(name="requested_by")
	private String requestedBy;

	@Column(name="requested_at")
	private long requestedAt;

	@OneToOne(optional = true)
	private Targetvolume targetvolume;

	@Column(name="output_folder")
	private String outputFolder;
	
	@Column(name="copy_number")
	private int copyNumber;	

	// request_ref_id (fk)
	// Many Requests like hold and release can all be referencing the same parent request
	// TODO or we act only on the previous subrequest always?
	@ManyToOne(fetch = FetchType.LAZY)
    private Request requestRef;

	// when a primary/secondary subrequest is requested to be canceled/held/release
	// subrequest_id (fk)
	// TODO for now its many to one - need to revisit
	@ManyToOne(fetch = FetchType.LAZY)
    private Subrequest subrequest;
	
	//library_id (fk)
	// Eg. rename requested on a library
	@OneToOne
	private Library library;
	
	//job_id (fk)
	// Eg. job aborted
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

	public String getRequestedBy() {
		return requestedBy;
	}

	public void setRequestedBy(String requestedBy) {
		this.requestedBy = requestedBy;
	}

	public long getRequestedAt() {
		return requestedAt;
	}

	public void setRequestedAt(long requestedAt) {
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