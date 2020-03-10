package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="tmp_job_file")
public class TmpJobFile {
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="tmp_job_file_id")
	private int tmpJobFileId;
	
	@Column(name="file_id")
	private int fileId;
	
	@Column(name="library_id")
	private int libraryId;
	
	@Column(name="job_id")
	private int jobId;
	
	@Column(name="pid")
	private int pid;

	@Column(name="started_at")
	private long startedAt;

	@Column(name="status_id")
	private int statusId;


	public int getTmpJobFileId() {
		return tmpJobFileId;
	}

	public void setTmpJobFileId(int tmpJobFileId) {
		this.tmpJobFileId = tmpJobFileId;
	}

	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}

	public int getLibraryId() {
		return libraryId;
	}

	public void setLibraryId(int libraryId) {
		this.libraryId = libraryId;
	}

	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public long getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(long startedAt) {
		this.startedAt = startedAt;
	}

	public int getStatusId() {
		return statusId;
	}

	public void setStatusId(int statusId) {
		this.statusId = statusId;
	}
}