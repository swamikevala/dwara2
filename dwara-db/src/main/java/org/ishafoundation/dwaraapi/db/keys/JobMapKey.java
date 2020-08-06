package org.ishafoundation.dwaraapi.db.keys;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class JobMapKey implements Serializable {

	private static final long serialVersionUID = -686387883012132733L;

	@Column(name = "id")
    private int jobId;

    @Column(name = "id_ref")
    private int jobRefId;

    
    public JobMapKey() {}
    
    public JobMapKey(
        int jobId,
        int jobRefId) {

        this.jobId = jobId;
        this.jobRefId = jobRefId;
    }
 
 	public int getJobId() {
		return jobId;
	}

	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public int getJobRefId() {
		return jobRefId;
	}

	public void setJobRefId(int jobRefId) {
		this.jobRefId = jobRefId;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        JobMapKey that = (JobMapKey) o;
        return Objects.equals(jobId, that.jobId) && 
        		Objects.equals(jobRefId, that.jobRefId);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(jobId, jobRefId);
    }
}