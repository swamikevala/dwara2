package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.db.keys.JobRunKey;
import org.ishafoundation.dwaraapi.db.model.master.configuration.Device;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Volume;
import org.ishafoundation.dwaraapi.enumreferences.Status;

@Entity
@Table(name="jobrun")
public class JobRun {

	@EmbeddedId
	private JobRunKey id;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jobId")
	private Job job;

	@Column(name="completed_at")
	private LocalDateTime completedAt;

	@Column(name="started_at")
	private LocalDateTime startedAt;

	@Enumerated(EnumType.STRING)
	@Column(name="status")
	private Status status;
	
	@Lob
	@Column(name="message")
	private String message; 

	@OneToOne(fetch = FetchType.LAZY)
	private Volume volume;
	
	@OneToOne(fetch = FetchType.LAZY)
	private Device device;
	

	public JobRunKey getId() {
		return id;
	}

	public void setId(JobRunKey id) {
		this.id = id;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public LocalDateTime getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(LocalDateTime startedAt) {
		this.startedAt = startedAt;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Volume getVolume() {
		return volume;
	}

	public void setVolume(Volume volume) {
		this.volume = volume;
	}

	public Device getDevice() {
		return device;
	}

	public void setDevice(Device device) {
		this.device = device;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobRun job = (JobRun) o;
        return Objects.equals(id, job.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}