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
import javax.persistence.TableGenerator;

import org.ishafoundation.dwaraapi.constants.Status;



@Entity
@Table(name="subrequest")
public class Subrequest {

	@Id
	@GeneratedValue(generator = "dwara_seq_generator", strategy=GenerationType.TABLE)
	@TableGenerator(name="dwara_seq_generator", 
	 table="dwara_sequences", 
	 pkColumnName="primary_key_fields", 
	 valueColumnName="current_val", 
	 pkColumnValue="subrequest_id", allocationSize = 1)
	@Column(name="id")
	private int id;
	
	// Many Subrequests in a request
	@ManyToOne(fetch = FetchType.LAZY)
    private Request request;
	
	@Column(name="file_id")
	private Integer fileId;

	@Column(name="source_path")
	private String sourcePath;

	@Column(name="status_id")
	private Status status;

	@Column(name="skip_tasks")
	private String skipTasks;

	@Column(name="rerun")
	private Boolean rerun;

	@Column(name="rerun_no")
	private Integer rerunNo;

	@Column(name="priority")
	private Integer priority;

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

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	public Integer getFileId() {
		return fileId;
	}

	public void setFileId(Integer fileId) {
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

	public Boolean isRerun() {
		return rerun;
	}

	public void setRerun(Boolean rerun) {
		this.rerun = rerun;
	}

	public Integer getRerunNo() {
		return rerunNo;
	}

	public void setRerunNo(Integer rerunNo) {
		this.rerunNo = rerunNo;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
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