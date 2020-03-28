package org.ishafoundation.dwaraapi.db.model.transactional;
		
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.ishafoundation.dwaraapi.constants.Status;
import org.ishafoundation.dwaraapi.constants.StatusAttributeConverter;
import org.ishafoundation.dwaraapi.db.model.master.Task;
import org.ishafoundation.dwaraapi.db.model.transactional.jointables.TFileJob;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name="job")
public class Job {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="id")
	private int id;
	
	@OneToOne
	private Task task;

	@OneToOne
	@JoinColumn(name="input_library_id")
	private Library inputLibrary;

	@OneToOne(optional = true)
	@JoinColumn(name="output_library_id")
	private Library outputLibrary;
	
	@Column(name="completed_at")
	private long completedAt;

	@Column(name="created_at")
	private long createdAt;

	@ManyToOne(fetch = FetchType.LAZY)
	private Subrequest subrequest;
	
	/*
	@Transient
	private int subrequestId;
	*/
	
	@Column(name="started_at")
	private long startedAt;

	@Convert(converter = StatusAttributeConverter.class)
	private Status status;
	
    @OneToMany(mappedBy = "job",
            cascade = CascadeType.MERGE,
            orphanRemoval = true)
    private List<TFileJob> tFileJob = new ArrayList<>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Library getInputLibrary() {
		return inputLibrary;
	}

	public void setInputLibrary(Library inputLibrary) {
		this.inputLibrary = inputLibrary;
	}

	public Library getOutputLibrary() {
		return outputLibrary;
	}

	public void setOutputLibrary(Library outputLibrary) {
		this.outputLibrary = outputLibrary;
	}

	public long getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(long completedAt) {
		this.completedAt = completedAt;
	}

	public long getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(long createdAt) {
		this.createdAt = createdAt;
	}

	//@JsonIgnore
	public Subrequest getSubrequest() {
		return subrequest;
	}
	
	//@JsonIgnore
	public void setSubrequest(Subrequest subrequest) {
		this.subrequest = subrequest;
	}
	
	/*
	public int getSubrequestId() {
		return subrequestId = this.subrequest.getId();
	}
	*/
	public long getStartedAt() {
		return startedAt;
	}

	public void setStartedAt(long startedAt) {
		this.startedAt = startedAt;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	@JsonIgnore
	public List<TFileJob> gettFileJob() {
		return tFileJob;
	}
	
	@JsonIgnore
	public void settFileJob(List<TFileJob> tFileJob) {
		this.tFileJob = tFileJob;
	}
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(id, job.id);
    }
 
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}