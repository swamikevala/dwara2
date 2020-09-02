package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.ProcessingFailureKey;


@Entity
@Table(name="processingfailure")
public class ProcessingFailure {

	@EmbeddedId
	private ProcessingFailureKey id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jobId")
	private Job job;
	
	@Lob
	@Column(name="reason")
	private String reason;

	public ProcessingFailure() {
		
	}

	public ProcessingFailure(int fileId, Job job, String failureReason) {
		this.job = job;
		this.id = new ProcessingFailureKey(fileId, job.getId());
		this.reason = failureReason;
	}
	
    public ProcessingFailureKey getId() {
		return id;
	}

	public void setId(ProcessingFailureKey id) {
		this.id = id;
	}

	public int getFileId() {
		return this.id.getFileId();
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}