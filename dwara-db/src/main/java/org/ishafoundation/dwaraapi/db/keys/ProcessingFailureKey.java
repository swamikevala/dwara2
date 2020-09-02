package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class ProcessingFailureKey implements Serializable {

	private static final long serialVersionUID = 8367155075402352731L;

	@Column(name = "file_id")
    private int fileId;
 
    @Column(name = "job_id")
    private int jobId;
 
    public ProcessingFailureKey() {}
    
    public ProcessingFailureKey(
        int fileId,
        int jobId) {
        this.fileId = fileId;
        this.jobId = jobId;
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

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        ProcessingFailureKey that = (ProcessingFailureKey) o;
        return Objects.equals(fileId, that.fileId) &&
               Objects.equals(jobId, that.jobId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(fileId, jobId);
    }
}