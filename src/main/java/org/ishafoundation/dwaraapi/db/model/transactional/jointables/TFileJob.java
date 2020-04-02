package org.ishafoundation.dwaraapi.db.model.transactional.jointables;
		
import java.time.LocalDateTime;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.db.keys.TFileJobKey;
import org.ishafoundation.dwaraapi.db.model.transactional.File;
import org.ishafoundation.dwaraapi.db.model.transactional.Job;
import org.ishafoundation.dwaraapi.db.model.transactional.Library;

@Entity(name = "TFileJob")
@Table(name="t_file_job")
public class TFileJob {

	@EmbeddedId
	private TFileJobKey id;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("fileId")
	private File file;

	@ManyToOne(fetch = FetchType.LAZY)
    @MapsId("jobId")
	private Job job;

	// Many files on a job referring to the same library
	// Many jobs on a particular file referring to the same library
	// So Many Filejob combination to one library
	@ManyToOne(fetch = FetchType.LAZY)
	@MapsId("libraryId")
	private Library library;
	
	@Column(name="pid")
	private int pid;

	@Column(name="status_id")
	private Status status;

	@Column(name="started_at")
	private LocalDateTime startedAt;
	
	public TFileJob() {
		
	}

	public TFileJob(File file, Job job) {
		this.file = file;
		this.job = job;
		this.id = new TFileJobKey(file.getId(), job.getId());
	}
	
    public TFileJobKey getId() {
		return id;
	}

	public void setId(TFileJobKey id) {
		this.id = id;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		this.library = library;
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
 
        TFileJob that = (TFileJob) o;
        return Objects.equals(file, that.file) &&
               Objects.equals(job, that.job);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(file, job);
    }

}