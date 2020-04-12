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

import org.ishafoundation.dwaraapi.constants.Status;



@Entity
@Table(name="subrequest")
public class Subrequest {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="id")
	private int id;
	
	// Many Subrequests in a request
	@ManyToOne(fetch = FetchType.LAZY)
    private Request request;
	
	@Column(name="file_id")
	private int fileId;

	@Column(name="source_path")
	private String sourcePath;

	@Column(name="status_id")
	private Status status;

	@Column(name="skip_tasks")
	private String skipTasks;

	@Column(name="rerun")
	private boolean rerun;

	@Column(name="rerun_no")
	private int rerunNo;

	@Column(name="priority")
	private int priority;

	@OneToOne
	private Library library;

	@Column(name="library_name")
	private String libraryName;

	@Column(name="prev_sequence_code")
	private String prevSequenceCode;
	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	//@JsonIgnore
	public Request getRequest() {
		return request;
	}

	//@JsonIgnore
	public void setRequest(Request request) {
		this.request = request;
	}
	
	/*
	public int getRequestId() {
		return requestId = this.request.getId();
	}
	*/
	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getSkipTasks() {
		return skipTasks;
	}

	public void setSkipTasks(String skipTasks) {
		this.skipTasks = skipTasks;
	}

	public boolean isRerun() {
		return rerun;
	}

	public void setRerun(boolean rerun) {
		this.rerun = rerun;
	}

	public int getRerunNo() {
		return rerunNo;
	}

	public void setRerunNo(int rerunNo) {
		this.rerunNo = rerunNo;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		this.library = library;
	}

	public String getLibraryName() {
		return libraryName;
	}

	public void setLibraryName(String libraryName) {
		this.libraryName = libraryName;
	}

	public String getPrevSequenceCode() {
		return prevSequenceCode;
	}

	public void setPrevSequenceCode(String prevSequenceCode) {
		this.prevSequenceCode = prevSequenceCode;
	}
}