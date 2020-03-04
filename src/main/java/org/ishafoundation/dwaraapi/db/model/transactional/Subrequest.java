package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="subrequest")
public class Subrequest {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="subrequest_id")
	private int subrequestId;
	
	@Column(name="request_id")
	private int requestId;
	
	@Column(name="file_id")
	private int fileId;

	@Column(name="source_path")
	private String sourcePath;

	@Column(name="status_id")
	private int statusId;

	@Column(name="skip_tasks")
	private String skipTasks;

	@Column(name="rerun")
	private boolean rerun;

	@Column(name="rerun_no")
	private int rerunNo;

	@Column(name="optimize_tape_access")
	private boolean optimizeTapeAccess;

	@Column(name="priority")
	private int priority;

	@Column(name="subrequest_ref_id")
	private int subrequestRefId;

	@Column(name="library_id")
	private int libraryId;

	@Column(name="new_filename")
	private String newFilename;

	@Column(name="old_filename")
	private String oldFilename;

	@Column(name="prev_sequence_code")
	private String prevSequenceCode;

		
	public int getSubrequestId() {
		return subrequestId;
	}

	public void setSubrequestId(int subrequestId) {
		this.subrequestId = subrequestId;
	}

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}
	
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
	
	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
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
	
	public boolean isOptimizeTapeAccess() {
		return optimizeTapeAccess;
	}

	public void setOptimizeTapeAccess(boolean optimizeTapeAccess) {
		this.optimizeTapeAccess = optimizeTapeAccess;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public int getSubrequestRefId() {
		return subrequestRefId;
	}

	public void setSubrequestRefId(int subrequestRefId) {
		this.subrequestRefId = subrequestRefId;
	}

	public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}
	
	public String getNewFilename() {
		return newFilename;
	}

	public void setNewFilename(String newFilename) {
		this.newFilename = newFilename;
	}
	
	public String getOldFilename() {
		return oldFilename;
	}

	public void setOldFilename(String oldFilename) {
		this.oldFilename = oldFilename;
	}
	
	public String getPrevSequenceCode() {
		return prevSequenceCode;
	}

	public void setPrevSequenceCode(String prevSequenceCode) {
		this.prevSequenceCode = prevSequenceCode;
	}

}