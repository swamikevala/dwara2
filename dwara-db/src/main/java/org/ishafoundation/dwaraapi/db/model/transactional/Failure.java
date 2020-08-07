package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.ishafoundation.dwaraapi.db.keys.ActionelementMapKey;
import org.ishafoundation.dwaraapi.db.keys.FailureKey;
import org.ishafoundation.dwaraapi.db.keys.TFileJobKey;


@Entity
@Table(name="failure")
public class Failure {

	@EmbeddedId
	private FailureKey id;
	
	@ManyToOne(fetch = FetchType.LAZY) // TODO - To keep it consistent with File/Artifact should we remove FK here too
    @MapsId("jobId")
	private Job job;

	public Failure() {
		
	}

	public Failure(int fileId, Job job) {
		this.job = job;
		this.id = new FailureKey(fileId, job.getId());
	}
	
    public FailureKey getId() {
		return id;
	}

	public void setId(FailureKey id) {
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
}