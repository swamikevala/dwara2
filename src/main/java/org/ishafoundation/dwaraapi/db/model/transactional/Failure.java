package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name="failure")
public class Failure {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="failure_id")
	private int failureId;
	
	@Column(name="file_id")
	private int fileId;

	@Column(name="job_id")
	private int jobId;

		
	public int getFailureId() {
		return failureId;
	}

	public void setFailureId(int failureId) {
		this.failureId = failureId;
	}
	
	public int getFileId() {
		return fileId;
	}

	public void setFileId(int fileId) {
		this.fileId = fileId;
	}
	
	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}

}