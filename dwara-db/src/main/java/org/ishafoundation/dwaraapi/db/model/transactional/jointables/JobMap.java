package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import java.util.Objects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.JobMapKey;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;

@Entity(name = "JobMap")
@Table(name="job_map")
public class JobMap {

	@EmbeddedId
	private JobMapKey id;

//	@ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("jobId")
//	private Job job;
//	
//	@ManyToOne(fetch = FetchType.LAZY)
//    @MapsId("jobRefId")
//	private Job jobRef;
	
	public JobMap() {
		
	}

	public JobMap(Job job, Job jobRef) {
		this.id = new JobMapKey(job.getId(), jobRef.getId());
	}

	public JobMapKey getId() {
		return id;
	}

	public void setId(JobMapKey id) {
		this.id = id;
	}
	
	

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        JobMap that = (JobMap) o;
        return Objects.equals(id, that.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}