package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.TTFileJobKey;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.enumreferences.Status;

@Entity(name = "TTFileJob")
@Table(name="t_t_file_job")
public class TTFileJob {

	@EmbeddedId
	private TTFileJobKey id;

	// FK removed to support multi domains
//	@Column(name="file_id")
//	private int fileId;
	
	@ManyToOne(fetch = FetchType.LAZY) // TODO - To keep it consistent with File/Artifact should we remove FK here too
    @MapsId("jobId")
	private Job job;

	// FK removed to support multi domains
	@Column(name="artifact_id")
	private int artifactId;
	
//	// Many files on a job referring to the same artifact
//	// Many jobs on a particular file referring to the same artifact
//	// So Many Filejob combination to one artifact
//	// but the combination is just one on one
//	@OneToOne(fetch = FetchType.LAZY)
//	private Artifact artifact;
	
	@Column(name="pid")
	private int pid;

	@Enumerated(EnumType.STRING)
	@Column(name="status")
	private Status status;

	@Column(name="started_at")
	private LocalDateTime startedAt;
	
	public TTFileJob() {
		
	}

	public TTFileJob(int fileId, Job job) {
		this.job = job;
		this.id = new TTFileJobKey(fileId, job.getId());
	}
	
    public TTFileJobKey getId() {
		return id;
	}

	public void setId(TTFileJobKey id) {
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

	public int getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(int artifactId) {
		this.artifactId = artifactId;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(LocalDateTime startedAt) {
		this.startedAt = startedAt;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
 
        if (o == null || getClass() != o.getClass())
            return false;
 
        TTFileJob that = (TTFileJob) o;
        return Objects.equals(id, that.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}