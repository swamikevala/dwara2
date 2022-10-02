package org.ishafoundation.dwaraapi.db.model.transactional;
		
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.VolumeArtifactServerNameKey;

@Entity
@Table(name="t_artifactnamejobmap")
public class TArtifactnameJobMap {
	
	@EmbeddedId
	private VolumeArtifactServerNameKey id;
	
	@Column(name="job_id")
	private Integer jobId;
	
	public VolumeArtifactServerNameKey getId() {
		return id;
	}

	public void setId(VolumeArtifactServerNameKey id) {
		this.id = id;
	}

	public Integer getJobId() {
		return jobId;
	}

	public void setJobId(Integer jobId) {
		this.jobId = jobId;
	}
	
}