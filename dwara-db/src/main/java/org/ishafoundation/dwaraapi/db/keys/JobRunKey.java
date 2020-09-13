package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class JobRunKey implements Serializable {

	private static final long serialVersionUID = 3094076589216752917L;

	@Column(name = "job_id")
    private int jobId;

    @Column(name = "requeue_id")
    private int requeueId;
 
 
    public JobRunKey() {}
    
    public JobRunKey(int jobId, int requeueId) {
        this.jobId = jobId;
        this.requeueId = requeueId;
    }


	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public int getRequeueId() {
		return requeueId;
	}

	public void setRequeueId(int requeueId) {
		this.requeueId = requeueId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        JobRunKey that = (JobRunKey) o;
        return Objects.equals(requeueId, that.requeueId) &&
               Objects.equals(jobId, that.jobId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(requeueId, jobId);
    }
}